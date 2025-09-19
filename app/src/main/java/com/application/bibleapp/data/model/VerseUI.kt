package com.application.bibleapp.data.model

data class VerseUI(
    val id: Int,
    val text: String,
    val bookId: Int?,
    val chapter: Int?,
    val verse: Int?,
    val isUserVerse: Boolean = false,
    val isHighlighted: Boolean = false,
    val highlightColor: Int = 0x00000000
)
