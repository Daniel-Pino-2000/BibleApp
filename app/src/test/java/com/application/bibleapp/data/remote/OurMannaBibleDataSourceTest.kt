package com.application.bibleapp.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OurMannaBibleDataSourceTest {

    private val dataSource = OurMannaBibleDataSource()

    @Test
    fun `parses a verse range reference`() {
        val ref = dataSource.parseReference("John 3:20-21")

        assertEquals(43, ref.bookId)
        assertEquals(3, ref.chapter)
        assertEquals(20, ref.startVerse)
        assertEquals(21, ref.endVerse)
    }

    @Test
    fun `parses a single-verse reference with no range`() {
        val ref = dataSource.parseReference("John 3:16")

        assertEquals(43, ref.bookId)
        assertEquals(3, ref.chapter)
        assertEquals(16, ref.startVerse)
        assertNull(ref.endVerse)
    }

    @Test
    fun `parses a numbered book name`() {
        // The regex's book group is non-greedy — this is the case that could break if
        // it stopped too early at the "1".
        val ref = dataSource.parseReference("1 John 4:18")

        assertEquals(62, ref.bookId)
        assertEquals(4, ref.chapter)
        assertEquals(18, ref.startVerse)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a reference with an unrecognized book name`() {
        dataSource.parseReference("Not A Book 1:1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a reference that does not match the expected shape`() {
        dataSource.parseReference("John 3")
    }
}
