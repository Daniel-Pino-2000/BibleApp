package com.application.bibleapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.utils.TextUtils.normalizeForSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object BibleDatabaseManager {

    private const val DB_NAME = "bible_default.db"   // Name of your prebuilt DB in assets
    private var dbInstance: SQLiteDatabase? = null

    /**
     * Ensure the database is copied from assets to internal storage
     */
    private fun copyDatabaseIfNeeded(context: Context): File {
        val dbFile = context.getDatabasePath(DB_NAME)

        if (!dbFile.exists()) {
            // Make sure parent folders exist
            dbFile.parentFile?.mkdirs()

            try {
                context.assets.open(DB_NAME).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("BibleDB", "Database copied successfully to ${dbFile.path}")
            } catch (e: Exception) {
                Log.e("BibleDB", "Error copying database: ${e.message}")
            }
        } else {
            Log.d("BibleDB", "Database already exists at ${dbFile.path}")
        }

        return dbFile
    }

    /**
     * Get a singleton instance of the prebuilt database (read-only)
     */
    fun getDatabase(context: Context): SQLiteDatabase {
        if (dbInstance == null) {
            val dbFile = copyDatabaseIfNeeded(context)
            dbInstance = SQLiteDatabase.openDatabase(
                dbFile.path,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        }
        return dbInstance!!
    }

    /**
     * Get all verses for a specific book and chapter
     */
    fun getVersesByChapter(context: Context, bookNum: Int, chNum: Int): List<BibleVerse> {
        val db = getDatabase(context)

        // Make sure table and column names match your DB
        val cursor = db.rawQuery(
            "SELECT wordId, word, bookNum, chNum, verseNum FROM words WHERE bookNum = ? AND chNum = ? ORDER BY verseNum",
            arrayOf(bookNum.toString(), chNum.toString())
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

    private fun buildNormalizedSql(): String {
        val replacements = listOf(
            "á" to "a", "é" to "e", "í" to "i",
            "ó" to "o", "ú" to "u", "ü" to "u", "ñ" to "n"
        )

        var sql = "GROUP_CONCAT(word, ' ')"
        replacements.forEach { (from, to) ->
            sql = "REPLACE($sql,'$from','$to')"
        }
        return "LOWER($sql)"
    }

    suspend fun searchVerses(query: String): List<VerseUI> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val db = dbInstance ?: throw IllegalStateException("DB not initialized")

        // Split normalized query into words
        val words = query.normalizeForSearch().split(" ").filter { it.isNotBlank() }

        if (words.isEmpty()) return@withContext emptyList()

        try {
            val normalizedColumn = buildNormalizedSql()

            // Build WHERE with all words required
            val conditions = words.joinToString(" AND ") { "$normalizedColumn LIKE ?" }
            val args = words.map { "%$it%" }.toTypedArray()

            val cursor = db.rawQuery(
                """
            SELECT bookNum, chNum, verseNum, GROUP_CONCAT(word, ' ') as fullText
            FROM words
            GROUP BY bookNum, chNum, verseNum
            HAVING $conditions
            ORDER BY bookNum, chNum, verseNum
            LIMIT 500
            """,
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
            emptyList()
        }
    }


}
