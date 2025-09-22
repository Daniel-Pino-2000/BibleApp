package com.application.bibleapp.screens

import android.widget.TextView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.BibleText
import com.application.bibleapp.data.repository.BibleRepository

@Composable
fun BibleView(
    bibleRepository: BibleRepository,
    padding: PaddingValues) {

    val verses = bibleRepository.getChapter(1, 1)

    BibleText(verses, Modifier.padding(padding))
}