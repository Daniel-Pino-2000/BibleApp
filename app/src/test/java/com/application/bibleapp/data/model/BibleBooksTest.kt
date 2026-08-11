package com.application.bibleapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * apiSlug() must match the exact directory names the wldeh Bible API (CDN) uses,
 * e.g. https://cdn.jsdelivr.net/gh/wldeh/bible-api/bibles/en-kjv/books/1samuel/...
 * Expected slugs below were confirmed against the live API.
 */
class BibleBooksTest {

    @Test
    fun `multi-word book names produce space-free slugs`() {
        val expected = mapOf(
            "1 Samuel" to "1samuel",
            "2 Samuel" to "2samuel",
            "1 Kings" to "1kings",
            "2 Kings" to "2kings",
            "1 Chronicles" to "1chronicles",
            "2 Chronicles" to "2chronicles",
            "Song of Solomon" to "songofsolomon",
            "1 Corinthians" to "1corinthians",
            "2 Corinthians" to "2corinthians",
            "1 Thessalonians" to "1thessalonians",
            "2 Thessalonians" to "2thessalonians",
            "1 Timothy" to "1timothy",
            "2 Timothy" to "2timothy",
            "1 Peter" to "1peter",
            "2 Peter" to "2peter",
            "1 John" to "1john",
            "2 John" to "2john",
            "3 John" to "3john"
        )

        expected.forEach { (name, slug) ->
            val book = BibleBook(id = 0, name = name, chapters = emptyList())
            assertEquals(slug, book.apiSlug())
        }
    }

    @Test
    fun `single-word book names are just lowercased`() {
        val book = BibleBook(id = 1, name = "Genesis", chapters = emptyList())
        assertEquals("genesis", book.apiSlug())
    }

    @Test
    fun `every canonical book produces a slug with no spaces`() {
        assertEquals(66, BibleBooks.allBooks.size)
        BibleBooks.allBooks.forEach { book ->
            val slug = book.apiSlug()
            assertTrue("slug for '${book.name}' should not contain spaces: $slug", !slug.contains(" "))
            assertTrue("slug for '${book.name}' should not be blank", slug.isNotBlank())
        }
    }
}
