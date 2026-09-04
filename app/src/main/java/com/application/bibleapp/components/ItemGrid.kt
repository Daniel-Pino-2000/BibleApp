package com.application.bibleapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.application.bibleapp.ui.theme.Spacing

/** Minimum recommended touch target on Android — every cell stays at least this size
 *  regardless of how many items share a row, even on narrow phones. */
private val MIN_TOUCH_TARGET = 48.dp

@Composable
fun ItemGrid(
    modifier: Modifier = Modifier,
    items: List<Int>,
    itemsPerRow: Int = 5,
    selectedItem: Int? = null,
    onItemClick: (Int) -> Unit
) {
    Column(modifier = modifier.padding(Spacing.sm)) {
        items.chunked(itemsPerRow).forEach { row ->
            Row {
                row.forEach { number ->
                    val isSelected = number == selectedItem
                    Surface(
                        modifier = Modifier
                            .padding(Spacing.xs)
                            .weight(1f)
                            .aspectRatio(1f)
                            .sizeIn(minWidth = MIN_TOUCH_TARGET, minHeight = MIN_TOUCH_TARGET),
                        shape = MaterialTheme.shapes.small,
                        onClick = { onItemClick(number) },
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = number.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
fun ChapterGrid(chapterCount: Int, selectedChapter: Int? = null, onChapterClick: (Int) -> Unit) {
    ItemGrid(
        items = (1..chapterCount).toList(),
        itemsPerRow = 5,
        selectedItem = selectedChapter,
        onItemClick = onChapterClick
    )
}

@Composable
fun VerseGrid(verseCount: Int, selectedVerse: Int? = null, onVerseClick: (Int) -> Unit) {
    // Generate a list of verse numbers from 1 to the total number of verses in the chapter
    val verseNumbers = (1..verseCount).toList()

    // 6/row (not 8) so cells stay at or above the 48dp minimum touch target on common phone widths.
    ItemGrid(items = verseNumbers, itemsPerRow = 6, selectedItem = selectedVerse, onItemClick = onVerseClick)
}
