package com.application.bibleapp.data.remote

/**
 * Outcome of fetching a single chapter, distinguishing "genuinely doesn't exist"
 * from "couldn't reach it" so callers can decide whether to fail a download.
 */
sealed class ChapterFetchResult {
    data class Success(val chapter: ApiChapterDto) : ChapterFetchResult()
    data object NotFound : ChapterFetchResult()
    data class Error(val message: String) : ChapterFetchResult()
}
