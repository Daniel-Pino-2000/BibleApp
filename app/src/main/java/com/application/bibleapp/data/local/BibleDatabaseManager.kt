package com.application.bibleapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
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

    fun searchVerses(query: String): List<VerseUI> {
        val db = dbInstance ?: throw IllegalStateException("DB not initialized")

        if (query.isBlank()) return emptyList()

        val cursor = db.rawQuery(
            "SELECT wordId, word, bookNum, chNum, verseNum FROM words WHERE word LIKE ? ORDER BY bookNum, chNum, verseNum",
            arrayOf("%$query%")
        )

        val results = mutableListOf<VerseUI>()
        while (cursor.moveToNext()) {
            results.add(
                VerseUI(
                    id = cursor.getInt(0),
                    text = cursor.getString(1),
                    bookId = cursor.getInt(2),
                    chapter = cursor.getInt(3),
                    verse = cursor.getInt(4),
                    isUserVerse = false,
                    isHighlighted = false,
                    highlightColor = 0x00000000
                )
            )
        }

        cursor.close()
        return results
    }
}
