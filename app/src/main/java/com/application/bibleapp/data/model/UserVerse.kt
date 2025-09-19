package com.application.bibleapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserVerse(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val text: String,
    val bookId: Int? = null,
    val chapter: Int? = null,
    val verse: Int? = null,
    val isHighlighted: Boolean = false,
    val highlightColor: Int = 0x00000000 // ARGB as Int, default transparent
)
