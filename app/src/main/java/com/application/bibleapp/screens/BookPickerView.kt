package com.application.bibleapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.ChapterGrid
import com.application.bibleapp.components.SearchBar
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.viewmodel.BibleViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPickerView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onChapterClick: (Int, Int) -> Unit
) {

    val currentBook by bibleViewModel.currentBook.collectAsState()
    val currentChapter by bibleViewModel.currentChapter.collectAsState()
    val expandedBookId = rememberSaveable { mutableStateOf<Int?>(null) }

    var searchText by rememberSaveable { mutableStateOf("") }


    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SearchBar(
            searchText,
            onTextChange = { newText ->
                searchText = newText
            },
            onSearchClick = {
                bibleViewModel.onSearchButtonClick()
            },
            placeholder = "Search Book"
        )

        val filteredBooks = BibleBooks.allBooks.filter {
            it.name.contains(searchText, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredBooks) { book ->
                val isExpanded = expandedBookId.value == book.id

                Text(
                    text = book.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(true, onClick = {
                            expandedBookId.value =
                                if (expandedBookId.value == book.id) null else book.id
                        })
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                )

                if (isExpanded) {
                    ChapterGrid(
                        book = book,
                        selectedChapter = if (book.id == currentBook) currentChapter else null
                    ) { chapterNumber ->

                        // Trigger the callback to navigate to VersePickerView
                        onChapterClick(book.id, chapterNumber)

                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

        }

    }
}
