package com.application.bibleapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.bibleapp.components.BibleText
import com.application.bibleapp.components.BookPickerBar
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.viewmodel.BibleViewModel

@Composable
fun BibleView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {

    val currentVerse by bibleViewModel.currentVerse.collectAsState()
    val verses by bibleViewModel.verses.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        BibleText(
            verses = verses,
            scrollToIndex = currentVerse,
            modifier = Modifier
                .fillMaxSize() // BibleText takes full size of Column
        )
    }

}
