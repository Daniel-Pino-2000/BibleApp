package com.application.bibleapp.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.SearchBar
import com.application.bibleapp.viewmodel.BibleViewModel
import androidx.compose.foundation.lazy.items

@Composable
fun SearchView(
    bibleViewModel: BibleViewModel,
    padding: PaddingValues,
    onVerseClicked: (bookId: Int, chapter: Int, verse: Int) -> Unit
) {

    val searchText by bibleViewModel.searchQuery.collectAsState()

    val searchedVerses by bibleViewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(padding)
    ) {

        SearchBar(
            searchText = searchText,
            onTextChange = { newText ->
                bibleViewModel.onSearchQueryChange(newText)
            },
            placeholder = "Search Bible Verse"
        )

        LazyColumn {
            items(searchedVerses, key = { it.id }) { verse ->
                Text(
                    text = "${verse.verse} ${verse.text}",
                    modifier = Modifier.clickable {
                        onVerseClicked(verse.bookId!!, verse.chapter!!, verse.verse!!)
                    }
                )

            }
        }
    }

}