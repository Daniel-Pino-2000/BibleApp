package com.application.bibleapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class BibleRepositoryCachingTest {

    private fun calendarFor(year: Int, month: Int, day: Int): Calendar =
        Calendar.getInstance().apply { set(year, month, day, 12, 0, 0) }

    @Test
    fun `same calendar day produces the same cache key`() {
        val morning = calendarFor(2026, Calendar.MARCH, 5).apply { set(Calendar.HOUR_OF_DAY, 1) }
        val night = calendarFor(2026, Calendar.MARCH, 5).apply { set(Calendar.HOUR_OF_DAY, 23) }

        assertEquals(dailyVerseCacheKey(morning), dailyVerseCacheKey(night))
    }

    @Test
    fun `different calendar days produce different cache keys`() {
        val today = calendarFor(2026, Calendar.MARCH, 5)
        val tomorrow = calendarFor(2026, Calendar.MARCH, 6)

        assertNotEquals(dailyVerseCacheKey(today), dailyVerseCacheKey(tomorrow))
    }

    @Test
    fun `year boundary does not collide - Dec 31 and the following Jan 1 differ`() {
        // Both dates are day-of-year 365 (or close to it) in their own year — day-of-year
        // alone would risk a same-key collision here if the year weren't included.
        val dec31 = calendarFor(2025, Calendar.DECEMBER, 31)
        val jan1 = calendarFor(2026, Calendar.JANUARY, 1)

        assertNotEquals(dailyVerseCacheKey(dec31), dailyVerseCacheKey(jan1))
    }

    @Test
    fun `encoding a single verse (no range) round-trips to null`() {
        assertNull(decodeEndVerse(encodeEndVerse(null)))
    }

    @Test
    fun `encoding a range end verse round-trips to the same value`() {
        assertEquals(21, decodeEndVerse(encodeEndVerse(21)))
    }

    @Test
    fun `the sentinel itself decodes to null, matching a missing SharedPreferences entry`() {
        assertNull(decodeEndVerse(NO_END_VERSE))
    }
}
