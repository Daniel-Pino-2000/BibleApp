package com.application.bibleapp.worker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyVerseSchedulerTest {

    private fun calendarAt(hour: Int, minute: Int, second: Int = 0): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }

    @Test
    fun `target time later today schedules for later today`() {
        val now = calendarAt(6, 0)

        val delay = DailyVerseScheduler.delayUntilNext(hour = 8, minute = 0, now = now)

        assertEquals(TimeUnit.HOURS.toMillis(2), delay)
    }

    @Test
    fun `target time already passed today schedules for tomorrow`() {
        val now = calendarAt(9, 0)

        val delay = DailyVerseScheduler.delayUntilNext(hour = 8, minute = 0, now = now)

        assertEquals(TimeUnit.HOURS.toMillis(23), delay)
    }

    @Test
    fun `target time exactly now schedules for tomorrow, not an immediate rerun`() {
        // The exact-equality edge case: without the <= (vs strictly <) comparison, a
        // self-rescheduling worker finishing precisely on its own target time would
        // compute a delay of zero and re-run itself immediately instead of tomorrow.
        val now = calendarAt(8, 0)

        val delay = DailyVerseScheduler.delayUntilNext(hour = 8, minute = 0, now = now)

        assertEquals(TimeUnit.DAYS.toMillis(1), delay)
    }

    @Test
    fun `never returns zero or negative`() {
        val now = calendarAt(23, 59, 59)

        val delay = DailyVerseScheduler.delayUntilNext(hour = 0, minute = 0, now = now)

        assertTrue(delay > 0)
    }
}
