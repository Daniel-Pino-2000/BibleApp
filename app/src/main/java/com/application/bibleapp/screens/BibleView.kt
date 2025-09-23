package com.application.bibleapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.BibleText
import com.application.bibleapp.components.BookPickerBar
import com.application.bibleapp.data.repository.BibleRepository

@Composable
fun BibleView(
    bibleRepository: BibleRepository,
    padding: PaddingValues
) {
    var book by remember { mutableIntStateOf(1) }
    var chapter by remember { mutableIntStateOf(1) }

    val verses = bibleRepository.getChapter(book, chapter)

    Column(modifier = Modifier.padding(padding)) {
        BibleText(verses, Modifier.weight(1f)) // take remaining space


    }
}
