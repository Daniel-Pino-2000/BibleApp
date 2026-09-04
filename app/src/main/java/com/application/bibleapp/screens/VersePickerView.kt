package com.application.bibleapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.VerseGrid
import com.application.bibleapp.data.model.verseCount
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersePickerView(
    bibleViewModel: BibleViewModel,
    bookId: Int,
    chapter: Int,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onVerseClicked: (verse: Int) -> Unit
) {

    val currentBook by bibleViewModel.currentBook.collectAsState()
    val currentChapter by bibleViewModel.currentChapter.collectAsState()
    val currentVerse by bibleViewModel.currentVerse.collectAsState()
    val chapterStructure by bibleViewModel.chapterStructure.collectAsState()

    // Only highlight a pre-selected verse if this is the chapter currently open on the
    // reading screen — a freshly picked chapter has no verse "selected" yet.
    val selectedVerse = if (bookId == currentBook && chapter == currentChapter) currentVerse else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {

        VerseGrid(
            verseCount = chapterStructure.verseCount(bookId, chapter),
            selectedVerse = selectedVerse
        ) { verse -> // verse number return by the ItemGrid
            onVerseClicked(verse)
        }
    }

}
