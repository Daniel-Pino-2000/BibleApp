package com.application.bibleapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.application.bibleapp.data.model.BibleBook
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.ui.theme.Spacing

@Composable
fun ItemGrid(
    modifier: Modifier = Modifier,
    items: List<Int>,
    itemsPerRow: Int = 5,
    onItemClick: (Int) -> Unit
) {
    Column(modifier = modifier.padding(Spacing.sm)) {
        items.chunked(itemsPerRow).forEach { row ->
            Row {
                row.forEach { number ->
                    Surface(
                        modifier = Modifier
                            .padding(Spacing.xs)
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onItemClick(number) },
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = number.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
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
fun VerseGrid(bookId: Int, chapterId: Int, onVerseClick: (Int) -> Unit) {
    val numberOfVerses = BibleBooks.getVerseCount(bookId, chapterId)

    // Generate a list of verse numbers from 1 to the total number of verses in the chapter
    val verseNumbers = (1..numberOfVerses).toList()

    ItemGrid(items = verseNumbers, itemsPerRow = 8, onItemClick = onVerseClick)
}
