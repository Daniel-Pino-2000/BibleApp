package com.application.bibleapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.viewmodel.BibleViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.application.bibleapp.components.SearchBar
import com.application.bibleapp.data.model.BibleBook
import com.application.bibleapp.data.model.Chapter
import kotlin.collections.map

@Composable
fun ItemGrid(
    modifier: Modifier = Modifier,
    items: List<Int>,
    itemsPerRow: Int = 5,
    onItemClick: (Int) -> Unit
) {
    Column(modifier = modifier.padding(8.dp)) {
        items.chunked(itemsPerRow).forEach { row ->
            Row {
                row.forEach { number ->
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onItemClick(number) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(number.toString())
                        }
                    }
                }
                if (row.size < itemsPerRow) {
                    repeat(itemsPerRow - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
@Composable
fun ChapterGrid(book: BibleBook, onChapterClick: (Int) -> Unit) {
    ItemGrid(items = book.chapters.map { it.number }, itemsPerRow = 5, onItemClick = onChapterClick)
}

@Composable
fun VerseGrid(chapter: Chapter, onVerseClick: (Int) -> Unit) {
    // Generate a list of verse numbers from 1 to the total number of verses in the chapter
    val verseNumbers = (1..chapter.verses).toList()

    ItemGrid(items = verseNumbers, itemsPerRow = 8, onItemClick = onVerseClick)
}
