package com.application.bibleapp.screens

import android.widget.TextView
import androidx.compose.runtime.Composable
import com.application.bibleapp.components.BibleText
import com.application.bibleapp.data.repository.BibleRepository

@Composable
fun BibleView(bibleRepository: BibleRepository) {
    val verses = bibleRepository.getChapter(1, 1)

    BibleText(verses)
}