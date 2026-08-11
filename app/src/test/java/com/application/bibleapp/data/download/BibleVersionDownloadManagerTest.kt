package com.application.bibleapp.data.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BibleVersionDownloadManagerTest {
    @Test
    fun sanitizeVersionId_keepsSafePathCharacters() {
        assertEquals("kjv_1769", BibleVersionDownloadManager.sanitizeVersionId("KJV 1769"))
        assertEquals("es-rvr.1960", BibleVersionDownloadManager.sanitizeVersionId("es-RVR.1960"))
    }

    @Test
    fun sanitizeVersionId_rejectsPathTraversalOnlyInput() {
        assertThrows(BibleVersionDownloadException::class.java) {
            BibleVersionDownloadManager.sanitizeVersionId("../")
        }
    }
}
