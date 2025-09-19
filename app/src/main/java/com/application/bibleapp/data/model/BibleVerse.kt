package com.application.bibleapp.data.model

import androidx.room.PrimaryKey

data class BibleVerse(
    val id: Int,
    val text: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int
)

