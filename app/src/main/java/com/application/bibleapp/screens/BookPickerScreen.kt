package com.application.bibleapp.screens

import android.R.attr.maxLines
import android.R.attr.minLines
import android.R.attr.singleLine
import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import com.application.bibleapp.data.model.BibleBook


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPickerScreen(
    bibleViewModel: BibleViewModel,
    onBackClick: () -> Unit
) {

    val selectedBook by (bibleViewModel.currentBook.collectAsState())
    val expandedBookId = rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TopAppBar(
            title = { Text("Select Book") },
            navigationIcon = {
                IconButton(
                    onClick = { onBackClick() }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        OutlinedTextField(
            value = "",
            onValueChange = {
                // I have to finish it later
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            label =  { Text("Search") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search Icon")
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )



        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(BibleBooks.allBooks) { book ->
                Text(book.name, Modifier.clickable(true, onClick = {
                    expandedBookId.value = if (expandedBookId.value == book.id) null else book.id
                 }))

                if (expandedBookId.value == book.id) {
                    ChapterGrid(book) { chapter ->
                        bibleViewModel.loadChapter(book.id, chapter)
                    }
                }
            }

        }

    }
}

@Composable
fun ChapterGrid(book: BibleBook, onChapterClick: (Int) -> Unit) {

    Column(modifier = Modifier.padding(8.dp)) {
        val chaptersPerRow = 5
        book.chapters.chunked(chaptersPerRow).forEach { row ->

            Row {
                row.forEach { chapter ->
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                            .aspectRatio(1f) // square
                            .clickable { onChapterClick(chapter.number) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(chapter.number.toString())
                        }
                    }
                }
                // Fill empty space if row is not full
                if (row.size < chaptersPerRow) {
                    repeat(chaptersPerRow - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
