package com.application.bibleapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.viewmodel.BibleViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.application.bibleapp.components.ChapterGrid
import com.application.bibleapp.components.SearchBar
import com.application.bibleapp.components.VerseGrid
import com.application.bibleapp.data.model.BibleBook
import com.application.bibleapp.data.model.Chapter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPickerView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onChapterClick: (Int, Int) -> Unit
) {

    val selectedBook by (bibleViewModel.currentBook.collectAsState())
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
                Text(book.name, Modifier.clickable(true, onClick = {
                    expandedBookId.value =
                        if (expandedBookId.value == book.id) null else book.id
                }))

                if (expandedBookId.value == book.id) {
                    ChapterGrid(book) { chapterNumber ->

                        // Trigger the callback to navigate to VersePickerView
                        onChapterClick(book.id, chapterNumber)

                    }
                }
            }

        }

    }
}

