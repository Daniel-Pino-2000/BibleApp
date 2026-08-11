package com.application.bibleapp.data.download

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.application.bibleapp.data.model.BibleVersion
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class BibleVersionDownloadException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class DownloadedBibleVersion(
    val version: BibleVersion,
    val databasePath: String,
    val bytesWritten: Long
)

object BibleVersionDownloadManager {
    private const val TAG = "BibleVersionDownload"
    private const val PREFS_NAME = "downloaded_bible_versions"
    private const val PREFS_KEY = "versions"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 30_000

    suspend fun downloadAndRegister(context: Context, version: BibleVersion): DownloadedBibleVersion =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            downloadAndRegisterBlocking(context, version)
        }

    fun downloadAndRegisterBlocking(context: Context, version: BibleVersion): DownloadedBibleVersion {
        val versionId = sanitizeVersionId(version.versionId)
        val downloadsDir = File(context.filesDir, "bible_versions")
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            throw BibleVersionDownloadException("Unable to create Bible version directory: ${downloadsDir.absolutePath}")
        }

        val tempFile = File.createTempFile("$versionId-", ".download", context.cacheDir)
        var candidateDb: File? = null
        try {
            val status = downloadToFile(version.url, tempFile)
            if (status !in 200..299) {
                throw BibleVersionDownloadException("Bible version download failed for $versionId: HTTP $status from ${version.url}")
            }
            if (tempFile.length() == 0L) {
                throw BibleVersionDownloadException("Bible version download failed for $versionId: empty response from ${version.url}")
            }

            val finalDb = File(downloadsDir, "$versionId.db")
            candidateDb = File.createTempFile("$versionId-", ".db", downloadsDir)
            val bytesWritten = if (looksLikeZip(tempFile)) {
                extractSingleDatabaseFromZip(tempFile, candidateDb)
            } else {
                tempFile.copyTo(candidateDb, overwrite = true).length()
            }
            validateSqliteDatabase(candidateDb, versionId)
            if (!candidateDb.renameTo(finalDb)) {
                candidateDb.copyTo(finalDb, overwrite = true)
                candidateDb.delete()
            }
            registerDownloadedVersion(context, version.copy(versionId = versionId), finalDb)
            return DownloadedBibleVersion(version.copy(versionId = versionId), finalDb.absolutePath, bytesWritten)
        } catch (e: BibleVersionDownloadException) {
            Log.e(TAG, e.message, e)
            throw e
        } catch (e: Exception) {
            val message = "Bible version download failed for ${version.versionId}: ${e.javaClass.simpleName}: ${e.message}"
            Log.e(TAG, message, e)
            throw BibleVersionDownloadException(message, e)
        } finally {
            tempFile.delete()
            candidateDb?.takeIf { it.exists() }?.delete()
        }
    }

    private fun downloadToFile(url: String, destination: File): Int {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("Accept", "application/octet-stream, application/zip, */*")
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) return status
            connection.inputStream.use { input ->
                FileOutputStream(destination).use { output -> input.copyTo(output) }
            }
            return status
        } finally {
            connection.disconnect()
        }
    }

    private fun extractSingleDatabaseFromZip(zipFile: File, destination: File): Long {
        ZipInputStream(zipFile.inputStream().buffered()).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                val name = entry.name.substringAfterLast('/')
                if (!entry.isDirectory && name.endsWith(".db", ignoreCase = true)) {
                    FileOutputStream(destination).use { output -> zip.copyTo(output) }
                    zip.closeEntry()
                    return destination.length()
                }
                zip.closeEntry()
            }
        }
        throw BibleVersionDownloadException("Downloaded archive does not contain a .db file")
    }

    private fun validateSqliteDatabase(dbFile: File, versionId: String) {
        if (!dbFile.exists() || dbFile.length() == 0L) {
            throw BibleVersionDownloadException("Downloaded database for $versionId was not written to ${dbFile.absolutePath}")
        }
        try {
            SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' LIMIT 1", emptyArray()).use { cursor ->
                    if (!cursor.moveToFirst()) throw BibleVersionDownloadException("Downloaded database for $versionId has no tables")
                }
            }
        } catch (e: BibleVersionDownloadException) {
            throw e
        } catch (e: Exception) {
            throw BibleVersionDownloadException("Downloaded file for $versionId is not a readable SQLite database", e)
        }
    }

    private fun registerDownloadedVersion(context: Context, version: BibleVersion, dbFile: File) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        registerDownloadedVersion(prefs, version, dbFile.absolutePath)
    }

    fun registerDownloadedVersion(prefs: SharedPreferences, version: BibleVersion, databasePath: String) {
        val registrations = prefs.getStringSet(PREFS_KEY, emptySet()).orEmpty().toMutableSet()
        registrations.removeAll { it.startsWith("${version.versionId}|") }
        registrations.add("${version.versionId}|${version.name}|${version.language}|$databasePath")
        prefs.edit().putStringSet(PREFS_KEY, registrations).apply()
        Log.i(TAG, "Registered downloaded Bible version ${version.versionId} at $databasePath")
    }

    fun sanitizeVersionId(versionId: String): String {
        val sanitized = versionId.lowercase().replace(Regex("[^a-z0-9._-]"), "_").trim('_', '.', '-')
        if (sanitized.isBlank()) throw BibleVersionDownloadException("Bible version id is empty or invalid: '$versionId'")
        if (sanitized == "." || sanitized == "..") throw BibleVersionDownloadException("Bible version id is an invalid path segment: '$versionId'")
        return sanitized
    }

    private fun looksLikeZip(file: File): Boolean = file.inputStream().use { input ->
        val header = ByteArray(4)
        input.read(header) == 4 && header[0] == 'P'.code.toByte() && header[1] == 'K'.code.toByte()
    }
}
