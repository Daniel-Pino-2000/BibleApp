package com.application.bibleapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.remote.ChapterDownloadOutcome
import com.application.bibleapp.data.remote.ChapterFetchResult
import com.application.bibleapp.data.remote.DownloadSummary
import com.application.bibleapp.data.remote.RemoteBibleDataSource
import com.application.bibleapp.data.remote.summarizeDownload
import com.application.bibleapp.utils.NetworkUtils
import com.application.bibleapp.utils.TextUtils.normalizeForSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object BibleDatabaseManager {

    private const val DB_NAME = "bible_default.db"
    private const val DB_VERSION = 2 // bump this whenever you change the schema
    private const val DOWNLOAD_CONCURRENCY = 6 // bounded concurrent chapter fetches
    private const val MAX_CONSECUTIVE_FAILURES = 15 // bail out early on a sustained outage instead of grinding through every remaining chapter
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

        // Separate table for downloaded verses — never mixed with bundled KJV
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS downloaded_verses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                version TEXT NOT NULL,
                book_id INTEGER NOT NULL,
                chapter INTEGER NOT NULL,
                verse INTEGER NOT NULL,
                text TEXT NOT NULL,
                UNIQUE(version, book_id, chapter, verse)
            )
            """.trimIndent()
        )

        Log.d("BibleDB", "Downloaded versions/verses tables ready")
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
     * Downloads every chapter for [versionId] and persists it in one transaction.
     *
     * IMPORTANT: all network I/O happens in the "fetch phase" below, before any
     * transaction is opened. `SQLiteDatabase` ties an open transaction to the
     * calling thread via a thread-local session; `Dispatchers.IO` is free to
     * resume a coroutine on a *different* pool thread after it suspends on I/O,
     * so awaiting network calls inside `beginTransaction()/endTransaction()`
     * intermittently makes a later `execSQL`/`endTransaction()` run on a thread
     * that has no record of the transaction, throwing
     * "Cannot perform this operation because there is no current transaction"
     * (and, since the connection stays checked out the whole time, starves any
     * other caller trying to touch the DB — including ones on the main thread).
     * The "write phase" below only ever does synchronous DB calls with no
     * suspension points in between, so it can't hop threads mid-transaction.
     *
     * A chapter that genuinely doesn't exist for this version (real 404, e.g. an
     * NT-only translation) is skipped, not treated as a failure — only chapters
     * that fail with a real network/server error (after [RemoteBibleDataSource]'s
     * own retries) fail the whole download, and in that case nothing is written
     * at all so [versionId] is left un-downloaded for a clean retry.
     */
    suspend fun downloadAndSaveVersion(
        context: Context,
        versionId: String,
        versionName: String,
        bookNames: List<String>,
        chapterCounts: List<Int>,
        remote: RemoteBibleDataSource,
        onProgress: (Float) -> Unit = {}
    ): Result<DownloadSummary> = withContext(Dispatchers.IO) {
        require(bookNames.size == chapterCounts.size)

        if (versionId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Version id must not be blank"))
        }

        if (isVersionDownloaded(context, versionId)) {
            Log.d("BibleDB", "Version $versionId already downloaded, skipping")
            return@withContext Result.success(DownloadSummary(0, 0, 0, emptyList()))
        }

        if (!NetworkUtils.isOnline(context)) {
            return@withContext Result.failure(IOException("No internet connection. Connect to the internet and try again."))
        }

        // --- Fetch phase: network only, no transaction open. ---
        val totalChapters = chapterCounts.sum().toFloat()
        val chaptersFetched = AtomicInteger(0)
        val downloadSemaphore = Semaphore(DOWNLOAD_CONCURRENCY)

        // If we rack up too many consecutive network failures (each of which already
        // survived its own retries), the connection is probably down for good — stop
        // burning through the remaining chapters one slow retry-loop at a time.
        val consecutiveFailures = AtomicInteger(0)
        val connectionLost = AtomicBoolean(false)

        val outcomes: List<ChapterDownloadOutcome> = coroutineScope {
            bookNames.mapIndexed { bookIndex, bookSlug ->
                val bookId = bookIndex + 1
                val chapters = chapterCounts[bookIndex]
                (1..chapters).map { chapterNum ->
                    async {
                        val result = if (connectionLost.get()) {
                            ChapterFetchResult.Error("Download aborted: connection appears to be down")
                        } else {
                            downloadSemaphore.withPermit {
                                remote.getChapterDto(version = versionId, book = bookSlug, chapter = chapterNum)
                            }
                        }

                        when (result) {
                            is ChapterFetchResult.Error -> {
                                if (consecutiveFailures.incrementAndGet() >= MAX_CONSECUTIVE_FAILURES) {
                                    connectionLost.set(true)
                                }
                            }
                            is ChapterFetchResult.Success, is ChapterFetchResult.NotFound -> consecutiveFailures.set(0)
                        }

                        onProgress(chaptersFetched.incrementAndGet() / totalChapters)
                        ChapterDownloadOutcome(bookId, bookSlug, chapterNum, result)
                    }
                }
            }.flatten().awaitAll()
        }

        val summary = summarizeDownload(outcomes)

        if (summary.hasFailures) {
            Log.e(
                "BibleDB",
                "Version $versionId download failed: ${summary.failedChapters} network error(s) after retries"
            )
            return@withContext Result.failure(
                IOException(
                    "Network error: couldn't download ${summary.failedChapters} of ${totalChapters.toInt()} chapters " +
                        "after retrying (${summary.sampleErrors.joinToString("; ")})"
                )
            )
        }

        if (!summary.hasContent) {
            Log.e("BibleDB", "Version $versionId has no downloadable content (all ${summary.skippedChapters} chapters 404'd)")
            return@withContext Result.failure(IOException("No content is available for this version."))
        }

        if (summary.skippedChapters > 0) {
            Log.d(
                "BibleDB",
                "Version $versionId is a partial translation: ${summary.downloadedChapters} downloaded, " +
                    "${summary.skippedChapters} not available"
            )
        }

        // --- Write phase: synchronous DB calls only, no suspension points. ---
        val db = getDatabase(context)
        try {
            db.beginTransaction()
            try {
                outcomes.forEach { outcome ->
                    val success = outcome.result as? ChapterFetchResult.Success ?: return@forEach
                    success.chapter.data.forEach { verseDto ->
                        db.execSQL(
                            """
                            INSERT OR REPLACE INTO downloaded_verses (version, book_id, chapter, verse, text)
                            VALUES (?, ?, ?, ?, ?)
                            """.trimIndent(),
                            arrayOf(
                                versionId,
                                outcome.bookId,
                                outcome.chapterNumber,
                                verseDto.verse.toIntOrNull() ?: 0,
                                verseDto.text
                            )
                        )
                    }
                }

                db.execSQL(
                    "INSERT OR REPLACE INTO downloaded_versions (id, name, downloaded_at) VALUES (?, ?, ?)",
                    arrayOf(versionId, versionName, System.currentTimeMillis())
                )
                db.setTransactionSuccessful()
                Log.d(
                    "BibleDB",
                    "Version $versionId saved successfully (${summary.downloadedChapters} chapters, " +
                        "${summary.skippedChapters} unavailable)"
                )
                Result.success(summary)
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e("BibleDB", "Failed to save version $versionId: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Routes to KJV_verses for "kjv", downloaded_verses for everything else.
     */
    fun getVersesByChapter(context: Context, bookId: Int, chNum: Int, version: String): List<BibleVerse> {
        val db = getDatabase(context)

        val (table, versionClause) = if (version == "kjv") {
            "KJV_verses" to ""                          // KJV has no version column
        } else {
            "downloaded_verses" to "AND version = ?"
        }

        val args = if (version == "kjv") {
            arrayOf(bookId.toString(), chNum.toString())
        } else {
            arrayOf(bookId.toString(), chNum.toString(), version)
        }

        val cursor = db.rawQuery(
            "SELECT id, text, book_id, chapter, verse FROM $table WHERE book_id = ? AND chapter = ? $versionClause ORDER BY verse",
            args
        )

        val verses = mutableListOf<BibleVerse>()
        while (cursor.moveToNext()) {
            verses.add(
                BibleVerse(
                    id = cursor.getInt(0),
                    text = cursor.getString(1),
                    bookId = cursor.getInt(2),
                    chapter = cursor.getInt(3),
                    verse = cursor.getInt(4)
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