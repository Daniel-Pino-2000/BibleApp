package com.application.bibleapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.VerseGrid
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersePickerView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onVerseClicked: () -> Unit
) {

    val currentBook by bibleViewModel.currentBook.collectAsState()
    val currentChapter by bibleViewModel.currentChapter.collectAsState()
    val currentVerse by bibleViewModel.currentVerse.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg)
    ) {

        VerseGrid(
            bookId = currentBook,
            chapterId = currentChapter,
            selectedVerse = currentVerse
        ) { verse -> // verse number return by the ItemGrid
            bibleViewModel.setVerse(verse)
            onVerseClicked()
        }
    }

}
