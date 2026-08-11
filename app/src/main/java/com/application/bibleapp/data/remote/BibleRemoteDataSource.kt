package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.BibleTranslation
import com.application.bibleapp.data.model.StoredVerseContent

/**
 * A single verse ready to persist, already mapped onto the app's internal
 * 1–66 canonical book numbering — source-specific book codes/slugs never
 * leave the data source implementation. [richContent] is null for a source
 * that can't offer formatting beyond plain text; [text] is always plain and
 * always populated, so callers that don't care about formatting never need
 * to check [richContent].
 */
data class MappedVerse(
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val richContent: StoredVerseContent? = null
)

/**
 * Result of a full-translation download: every verse the source provided,
 * plus counts describing what was skipped (e.g. apocryphal books outside the
 * app's supported 1–66 canon) so the caller can decide whether/how to inform
 * the user without needing to know why anything was skipped.
 */
data class DownloadedTranslation(
    val translationId: String,
    val translationName: String,
    val verses: List<MappedVerse>,
    val downloadedChapterCount: Int,
    val skippedBookCount: Int
)

/**
 * Abstracts wherever Bible text actually comes from. UI, the ViewModel, and
 * the local DB layer depend only on this interface and on [BibleTranslation]/
 * [DownloadedTranslation] — swapping the backing API means writing a new
 * implementation of this interface, nothing else.
 */
interface BibleRemoteDataSource {
    suspend fun getAvailableTranslations(): List<BibleTranslation>

    /**
     * Downloads and fully parses [translationId] into memory before returning —
     * no partial/streamed writes. [onProgress] fires 0f..1f as bytes arrive.
     */
    suspend fun downloadTranslation(translationId: String, onProgress: (Float) -> Unit = {}): DownloadedTranslation
}
