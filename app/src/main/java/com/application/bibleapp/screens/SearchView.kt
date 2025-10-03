package com.application.bibleapp.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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