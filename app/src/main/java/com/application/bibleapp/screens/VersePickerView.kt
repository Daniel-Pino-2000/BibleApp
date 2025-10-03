package com.application.bibleapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.VerseGrid
import com.application.bibleapp.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersePickerView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onVerseClicked: () -> Unit
) {


    Column(
        modifier = modifier.fillMaxSize()
    ) {

        VerseGrid(
            bookId = bibleViewModel.currentBook.value,
            chapterId = bibleViewModel.currentChapter.value
        ) { verse -> // verse number return by the ItemGrid
            bibleViewModel.setVerse(verse)
            onVerseClicked()
        }
    }

}
