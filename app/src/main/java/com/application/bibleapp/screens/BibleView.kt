package com.application.bibleapp.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.application.bibleapp.components.BibleText
import com.application.bibleapp.components.BookPickerBar
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {

    val currentVerse by bibleViewModel.currentVerse.collectAsState()
    val verses by bibleViewModel.verses.collectAsState()
    val selectedFootnote by bibleViewModel.selectedFootnote.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        BibleText(
            verses = verses,
            scrollToIndex = currentVerse,
            modifier = Modifier
                .fillMaxSize(), // BibleText takes full size of Column
            onFootnoteClick = { noteId -> bibleViewModel.selectFootnote(noteId) }
        )
    }

    selectedFootnote?.let { footnote ->
        ModalBottomSheet(onDismissRequest = { bibleViewModel.dismissFootnote() }) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(text = "Verse ${footnote.verse}", fontWeight = FontWeight.Bold)
                Text(
                    text = footnote.text,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

}
