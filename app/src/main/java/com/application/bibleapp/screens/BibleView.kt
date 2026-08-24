package com.application.bibleapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.BibleText
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.ui.theme.ReadingStyle
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {

    val currentVerse by bibleViewModel.currentVerse.collectAsState()
    val currentBook by bibleViewModel.currentBook.collectAsState()
    val currentChapter by bibleViewModel.currentChapter.collectAsState()
    val verses by bibleViewModel.verses.collectAsState()
    val selectedFootnote by bibleViewModel.selectedFootnote.collectAsState()
    val verseTextScale by bibleViewModel.verseTextScale.collectAsState()
    val chapterTitle = "${BibleBooks.getBookById(currentBook)?.name ?: "Unknown"} $currentChapter"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BibleText(
            verses = verses,
            scrollToIndex = currentVerse,
            chapterTitle = chapterTitle,
            modifier = Modifier
                .fillMaxSize(), // BibleText takes full size of Column
            textScale = verseTextScale.multiplier,
            onFootnoteClick = { noteId -> bibleViewModel.selectFootnote(noteId) }
        )
    }

    selectedFootnote?.let { footnote ->
        ModalBottomSheet(
            onDismissRequest = { bibleViewModel.dismissFootnote() },
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.sm)) {
                Text(
                    text = "Verse ${footnote.verse}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = footnote.text,
                    style = ReadingStyle.FootnoteBody,
                    modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xl),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

}
