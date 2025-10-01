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
import com.application.bibleapp.components.BibleText
import com.application.bibleapp.components.BookPickerBar
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.viewmodel.BibleViewModel

@Composable
fun BibleView(
    bibleViewModel: BibleViewModel,
    padding: PaddingValues
) {

    val verses by bibleViewModel.verses.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        BibleText(
            verses = verses,
            modifier = Modifier.fillMaxSize()
        )
    }

}
