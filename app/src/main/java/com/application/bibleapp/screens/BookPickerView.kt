package com.application.bibleapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.ChapterGrid
import com.application.bibleapp.components.SearchBar
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.newTestamentBooks
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

        // Open the list already scrolled to the book being read, instead of
        // animating to it after the first (top-of-list) frame is visible —
        // that animation is what caused the visible "jump" before the user
        // could interact with the screen. initialFirstVisibleItemIndex is
        // read once, when the state is created, so this only positions the
        // list on entry and never fights the user's own scrolling.
        val initialBookIndex = remember {
            BibleBooks.allBooks.indexOfFirst { it.id == currentBook }.coerceAtLeast(0)
        }
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialBookIndex)

        // The last book in the list (Revelation, normally) has no list content
        // below it to push down and make room when its grid expands, so the grid
        // would otherwise render entirely below the viewport with nothing on
        // screen to show it happened. Scrolling just that book into view — with
        // the same animated scroll LazyColumn already uses elsewhere — fixes it
        // without touching how every other book expands.
        LaunchedEffect(expandedBookId.value) {
            val expandedId = expandedBookId.value ?: return@LaunchedEffect
            val index = filteredBooks.indexOfFirst { it.id == expandedId }
            if (index == filteredBooks.lastIndex) {
                listState.animateScrollToItem(index)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredBooks) { book ->
                val isExpanded = expandedBookId.value == book.id

                // Mark where the New Testament begins so the otherwise-uniform
                // list of book names gives the reader a sense of where they are,
                // the way a printed Bible's edge often does.
                if (book.id == newTestamentBooks.first().id) {
                    TestamentDivider(label = "New Testament")
                }

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

/**
 * Slim "· label ·" rule used to mark the Old/New Testament boundary in the
 * book list. Kept as its own composable, rather than inline, so the label
 * text can be reused if another section boundary is ever added.
 */
@Composable
private fun TestamentDivider(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = Spacing.sm)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline
        )
    }
}
