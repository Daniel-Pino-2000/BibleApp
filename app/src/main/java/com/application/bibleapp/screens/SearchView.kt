package com.application.bibleapp.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.SearchBar
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.viewmodel.BibleViewModel
import androidx.compose.foundation.lazy.items

@Composable
fun SearchView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onVerseClicked: (bookId: Int, chapter: Int, verse: Int) -> Unit
) {

    val searchText by bibleViewModel.searchQuery.collectAsState()

    val searchedVerses by bibleViewModel.searchResults.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        SearchBar(
            searchText = searchText,
            onTextChange = { bibleViewModel.onSearchQueryChange(it) },
            onSearchClick = { bibleViewModel.onSearchButtonClick() },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Search Bible Verse"
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(searchedVerses, key = { it.id ?: 0 }) { verse ->
                val bookName = BibleBooks.getBookById(verse.bookId ?: 0)?.name ?: "Unknown"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onVerseClicked(verse.bookId!!, verse.chapter!!, verse.verse!!)
                        }
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                ) {
                    Text(
                        text = "$bookName ${verse.chapter}:${verse.verse}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = verse.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }



}
