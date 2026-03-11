package com.application.bibleapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.remote.ApiChapterDto
import com.application.bibleapp.data.remote.RemoteBibleDataSource
import com.application.bibleapp.utils.TextUtils.normalizeForSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    suspend fun downloadAndSaveVersion(
        context: Context,
        versionId: String,
        versionName: String,
        bookNames: List<String>,
        chapterCounts: List<Int>,
        remote: RemoteBibleDataSource,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        require(bookNames.size == chapterCounts.size)

        val db = getDatabase(context)

        if (isVersionDownloaded(context, versionId)) {
            Log.d("BibleDB", "Version $versionId already downloaded, skipping")
            return@withContext Result.success(Unit)
        }

        val totalChapters = chapterCounts.sum().toFloat()
        var chaptersProcessed = 0

        return@withContext try {
            db.beginTransaction()
            try {
                bookNames.forEachIndexed { bookIndex, bookName ->
                    val bookId = bookIndex + 1
                    val chapters = chapterCounts[bookIndex]

                    for (chapterNum in 1..chapters) {
                        val apiChapter: ApiChapterDto? = remote.getChapterDto(
                            version = versionId,
                            book = bookName,
                            chapter = chapterNum
                        )

                        apiChapter?.data?.forEach { verseDto ->
                            db.execSQL(
                                """
                                INSERT OR REPLACE INTO downloaded_verses (version, book_id, chapter, verse, text)
                                VALUES (?, ?, ?, ?, ?)
                                """.trimIndent(),
                                arrayOf(
                                    versionId,
                                    bookId,
                                    chapterNum,
                                    verseDto.verse.toIntOrNull() ?: 0,
                                    verseDto.text
                                )
                            )
                        }

                        chaptersProcessed++
                        onProgress(chaptersProcessed / totalChapters)
                    }
                }

                db.execSQL(
                    "INSERT OR REPLACE INTO downloaded_versions (id, name, downloaded_at) VALUES (?, ?, ?)",
                    arrayOf(versionId, versionName, System.currentTimeMillis())
                )

                db.setTransactionSuccessful()
                Log.d("BibleDB", "Version $versionId saved successfully")
                Result.success(Unit)
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