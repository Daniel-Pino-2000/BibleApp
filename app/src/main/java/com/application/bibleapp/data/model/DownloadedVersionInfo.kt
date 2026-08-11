package com.application.bibleapp.data.model

/**
 * What we know locally about a translation that has already been downloaded.
 *
 * [schemaVersion] records which shape of [BibleDatabaseManager.downloadAndSaveVersion]
 * wrote this data. It lets the version picker tell a translation downloaded before
 * a feature like footnotes existed apart from one that already has it, without
 * guessing from row counts.
 */
data class DownloadedVersionInfo(
    val id: String,
    val schemaVersion: Int
) {
    val isUpToDate: Boolean get() = schemaVersion >= CURRENT_DOWNLOAD_SCHEMA_VERSION

    companion object {
        /**
         * Bump this whenever a download starts capturing genuinely new data (not just a
         * new way of rendering data already stored). 1 = plain text only. 2 = adds
         * rich_content (red-letter/poems/headings) and the footnotes table.
         */
        const val CURRENT_DOWNLOAD_SCHEMA_VERSION = 2
    }
}
