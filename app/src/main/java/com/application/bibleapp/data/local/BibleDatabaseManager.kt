package com.application.bibleapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.model.decodeVerseContentOrNull
import com.application.bibleapp.data.model.encodeToJson
import com.application.bibleapp.data.remote.BibleRemoteDataSource
import com.application.bibleapp.data.remote.DownloadedTranslation
import com.application.bibleapp.utils.NetworkUtils
import com.application.bibleapp.utils.TextUtils.normalizeForSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/** What actually got written to disk after a translation download. */
data class VersionDownloadSummary(
    val downloadedVerseCount: Int,
    val downloadedChapterCount: Int,
    val skippedBookCount: Int
)

object BibleDatabaseManager {

    private const val DB_NAME = "bible_default.db"
    private const val DB_VERSION = 2 // bump this whenever you change the schema
    private var dbInstance: SQLiteDatabase? = null

    /**
     * Copies DB from assets if it doesn't exist, or if the stored version is outdated.
     * Version is tracked in a separate tiny file next to the DB so we don't touch the DB itself.
     */
    private fun copyDatabaseIfNeeded(context: Context): File {
        val dbFile = context.getDatabasePath(DB_NAME)
        val versionFile = File(dbFile.parent, "${DB_NAME}.ver")

        val installedVersion = if (versionFile.exists()) versionFile.readText().trim().toIntOrNull() ?: 0 else 0

        if (!dbFile.exists() || installedVersion < DB_VERSION) {
            dbFile.parentFile?.mkdirs()
            try {
                context.assets.open(DB_NAME).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
                versionFile.writeText(DB_VERSION.toString())
                Log.d("BibleDB", "Database copied (schema version $DB_VERSION)")
            } catch (e: Exception) {
                Log.e("BibleDB", "Error copying database: ${e.message}")
                throw e
            }
        } else {
            Log.d("BibleDB", "Database up to date (version $installedVersion)")
        }

        return dbFile
    }

    fun getDatabase(context: Context): SQLiteDatabase {
        if (dbInstance == null || dbInstance?.isOpen == false) {
            // Close stale instance before reopening
            dbInstance?.close()
            val dbFile = copyDatabaseIfNeeded(context)
            dbInstance = SQLiteDatabase.openDatabase(
                dbFile.path,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
            ensureDownloadedVersionsTable(dbInstance!!)
        }
        return dbInstance!!
    }

    /**
     * Creates a separate table for downloaded (non-KJV) verses.
     * We never touch KJV_verses — it stays exactly as bundled.
     */
    private fun ensureDownloadedVersionsTable(db: SQLiteDatabase) {
        // Tracks which versions have been fully downloaded
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS downloaded_versions (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                downloaded_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Separate table for downloaded verses — never mixed with bundled KJV.
        // rich_content is nullable JSON (StoredVerseContent) alongside plain `text` —
        // `text` stays plain on purpose so search's GROUP_CONCAT/LIKE keeps working
        // unchanged; only the reading screen looks at rich_content.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS downloaded_verses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                version TEXT NOT NULL,
                book_id INTEGER NOT NULL,
                chapter INTEGER NOT NULL,
                verse INTEGER NOT NULL,
                text TEXT NOT NULL,
                rich_content TEXT,
                UNIQUE(version, book_id, chapter, verse)
            )
            """.trimIndent()
        )
        addColumnIfMissing(db, "downloaded_verses", "rich_content", "TEXT")

        Log.d("BibleDB", "Downloaded versions/verses tables ready")
    }

    /**
     * SQLite has no `ADD COLUMN IF NOT EXISTS`; this makes adding a column to an
     * existing installation idempotent without a full migration framework. Safe to
     * call every time the DB opens — a no-op once the column exists.
     */
    private fun addColumnIfMissing(db: SQLiteDatabase, table: String, column: String, definition: String) {
        val columnExists = db.rawQuery("PRAGMA table_info($table)", null).use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            generateSequence { if (cursor.moveToNext()) cursor.getString(nameIndex) else null }.any { it == column }
        }
        if (!columnExists) {
            db.execSQL("ALTER TABLE $table ADD COLUMN $column $definition")
            Log.d("BibleDB", "Added missing column $table.$column")
        }
    }

    fun isVersionDownloaded(context: Context, versionId: String): Boolean {
        if (versionId == "kjv") return true // always available
        val db = getDatabase(context)
        val cursor = db.rawQuery(
            "SELECT 1 FROM downloaded_versions WHERE id = ? LIMIT 1",
            arrayOf(versionId)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    /**
     * Downloads a whole translation via [remote] — a single bulk request under the
     * hood — and writes it in one batched transaction.
     *
     * IMPORTANT: all network I/O + JSON parsing happens inside
     * [BibleRemoteDataSource.downloadTranslation], fully before any transaction is
     * opened here. `SQLiteDatabase` ties an open transaction to the calling thread
     * via a thread-local session; `Dispatchers.IO` is free to resume a coroutine on
     * a *different* pool thread after it suspends on I/O, so awaiting network calls
     * inside `beginTransaction()/endTransaction()` intermittently makes a later
     * `execSQL`/`endTransaction()` run on a thread that has no record of the
     * transaction, throwing "Cannot perform this operation because there is no
     * current transaction" (and, since the connection stays checked out the whole
     * time, starves any other caller trying to touch the DB — including ones on the
     * main thread). The write loop below is purely synchronous `execSQL` calls with
     * no suspension points in between, so it can't hop threads mid-transaction.
     *
     * If the download itself fails (network error, malformed JSON, translation not
     * found), nothing is written and [translationId] is left un-downloaded for a
     * clean retry.
     */
    suspend fun downloadAndSaveVersion(
        context: Context,
        translationId: String,
        remote: BibleRemoteDataSource,
        onProgress: (Float) -> Unit = {}
    ): Result<VersionDownloadSummary> = withContext(Dispatchers.IO) {
        if (translationId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Translation id must not be blank"))
        }

        if (isVersionDownloaded(context, translationId)) {
            Log.d("BibleDB", "Version $translationId already downloaded, skipping")
            return@withContext Result.success(VersionDownloadSummary(0, 0, 0))
        }

        if (!NetworkUtils.isOnline(context)) {
            return@withContext Result.failure(IOException("No internet connection. Connect to the internet and try again."))
        }

        val downloaded: DownloadedTranslation = try {
            remote.downloadTranslation(translationId, onProgress)
        } catch (e: Exception) {
            Log.e("BibleDB", "Failed to download $translationId: ${e.message}")
            return@withContext Result.failure(e)
        }

        if (downloaded.verses.isEmpty()) {
            Log.e("BibleDB", "Translation $translationId has no usable content")
            return@withContext Result.failure(IOException("No content is available for this translation."))
        }

        if (downloaded.skippedBookCount > 0) {
            Log.d(
                "BibleDB",
                "Translation $translationId is a partial canon: ${downloaded.verses.size} verses downloaded, " +
                    "${downloaded.skippedBookCount} book(s) outside the supported 1-66 canon skipped"
            )
        }

        // --- Write phase: synchronous DB calls only, no suspension points. ---
        val db = getDatabase(context)
        try {
            db.beginTransaction()
            try {
                downloaded.verses.forEach { verse ->
                    db.execSQL(
                        """
                        INSERT OR REPLACE INTO downloaded_verses (version, book_id, chapter, verse, text, rich_content)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                        arrayOf(
                            translationId,
                            verse.bookId,
                            verse.chapter,
                            verse.verse,
                            verse.text,
                            verse.richContent?.encodeToJson()
                        )
                    )
                }

                db.execSQL(
                    "INSERT OR REPLACE INTO downloaded_versions (id, name, downloaded_at) VALUES (?, ?, ?)",
                    arrayOf(translationId, downloaded.translationName, System.currentTimeMillis())
                )
                db.setTransactionSuccessful()
                Log.d(
                    "BibleDB",
                    "Version $translationId saved successfully (${downloaded.verses.size} verses, " +
                        "${downloaded.skippedBookCount} book(s) skipped)"
                )
                onProgress(1f)
                Result.success(
                    VersionDownloadSummary(
                        downloadedVerseCount = downloaded.verses.size,
                        downloadedChapterCount = downloaded.downloadedChapterCount,
                        skippedBookCount = downloaded.skippedBookCount
                    )
                )
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e("BibleDB", "Failed to save version $translationId: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Routes to KJV_verses for "kjv", downloaded_verses for everything else.
     * KJV_verses has no rich_content column (it's a separate bundled schema, never
     * touched by the helloao download path), so it's selected as a literal NULL to
     * keep the cursor shape identical either way.
     */
    fun getVersesByChapter(context: Context, bookId: Int, chNum: Int, version: String): List<BibleVerse> {
        val db = getDatabase(context)

        val (table, richContentColumn, versionClause) = if (version == "kjv") {
            Triple("KJV_verses", "NULL", "")
        } else {
            Triple("downloaded_verses", "rich_content", "AND version = ?")
        }

        val args = if (version == "kjv") {
            arrayOf(bookId.toString(), chNum.toString())
        } else {
            arrayOf(bookId.toString(), chNum.toString(), version)
        }

        val cursor = db.rawQuery(
            "SELECT id, text, $richContentColumn, book_id, chapter, verse FROM $table " +
                "WHERE book_id = ? AND chapter = ? $versionClause ORDER BY verse",
            args
        )

        val verses = mutableListOf<BibleVerse>()
        while (cursor.moveToNext()) {
            verses.add(
                BibleVerse(
                    id = cursor.getInt(0),
                    text = cursor.getString(1),
                    richContent = decodeVerseContentOrNull(if (cursor.isNull(2)) null else cursor.getString(2)),
                    bookId = cursor.getInt(3),
                    chapter = cursor.getInt(4),
                    verse = cursor.getInt(5)
                )
            )
        }
        cursor.close()
        return verses
    }

    private fun buildNormalizedSql(textExpr: String): String {
        val replacements = listOf(
            "á" to "a", "é" to "e", "í" to "i",
            "ó" to "o", "ú" to "u", "ü" to "u", "ñ" to "n"
        )
        var sql = textExpr
        replacements.forEach { (from, to) ->
            sql = "REPLACE($sql,'$from','$to')"
        }
        return "LOWER($sql)"
    }

    suspend fun searchVerses(query: String, version: String): List<VerseUI> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val db = dbInstance ?: throw IllegalStateException("DB not initialized")
        val words = query.normalizeForSearch().split(" ").filter { it.isNotBlank() }
        if (words.isEmpty()) return@withContext emptyList()

        try {
            val isKjv = version == "kjv"
            val table = if (isKjv) "KJV_verses" else "downloaded_verses"
            val normalizedCol = buildNormalizedSql("GROUP_CONCAT(text, ' ')")
            val havingConditions = words.joinToString(" AND ") { "$normalizedCol LIKE ?" }

            val whereClause = if (isKjv) "" else "WHERE version = ?"
            val args = buildList {
                if (!isKjv) add(version)
                words.forEach { add("%$it%") }
            }.toTypedArray()

            val cursor = db.rawQuery(
                """
                SELECT book_id, chapter, verse, GROUP_CONCAT(text, ' ') as fullText
                FROM $table
                $whereClause
                GROUP BY book_id, chapter, verse
                HAVING $havingConditions
                ORDER BY book_id, chapter, verse
                LIMIT 500
                """.trimIndent(),
                args
            )

            val results = mutableListOf<VerseUI>()
            cursor.use {
                while (it.moveToNext()) {
                    results.add(
                        VerseUI(
                            id = "${it.getInt(0)}_${it.getInt(1)}_${it.getInt(2)}".hashCode(),
                            text = it.getString(3),
                            bookId = it.getInt(0),
                            chapter = it.getInt(1),
                            verse = it.getInt(2),
                            isUserVerse = false,
                            isHighlighted = false,
                            highlightColor = 0x00000000
                        )
                    )
                }
            }
            results
        } catch (e: Exception) {
            Log.e("BibleDB", "Search error: ${e.message}")
            emptyList()
        }
    }
}