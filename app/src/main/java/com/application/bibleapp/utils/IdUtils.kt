package com.application.bibleapp.utils

object IdUtils {
    fun generateVerseId(bookId: Int, chapter: Int, verse: Int): Int =
        bookId * 1_000_000 + chapter * 1_000 + verse
}