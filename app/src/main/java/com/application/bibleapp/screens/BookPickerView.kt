package com.application.bibleapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.application.bibleapp.components.ChapterGrid
import com.application.bibleapp.components.SearchBar
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.viewmodel.BibleViewModel

/** How long the chapter grid takes to fade in/out — quick and deliberate, in the
 *  same 150-250ms range as this app's other UI transitions (e.g. the ~180ms
 *  chapter-label cross-fade in ChapterNavBar). Deliberately a plain fade rather
 *  than an expand/shrink — see the comment on the scroll effect below for why an
 *  animated *size* change isn't safe to pair with the auto-scroll fix. */
private const val CHAPTER_GRID_ANIM_MS = 200


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

        val listState = rememberLazyListState()

        // Root cause of the grid not appearing for a book near the bottom of the
        // list (Revelation, being last, always is): expanding it grows that list
        // item's height, but there's no list content left below to push down and
        // make room, so the new content renders entirely below the viewport.
        //
        // Two prior fixes didn't hold up, both for the same underlying reason —
        // racing a scroll against a still-animating layout size:
        //  1. BringIntoViewRequester.bringIntoView() fires the instant isExpanded
        //     flips true, while the grid is still ~0 height, so it computes its
        //     scroll target from the *collapsed* size and never re-fires as the
        //     grid keeps growing.
        //  2. Swapping to listState.animateScrollToItem() on its own still raced
        //     an expandVertically() enter animation — that API genuinely animates
        //     the *measured* height (not just a visual clip), so at the moment the
        //     scroll ran, the list's total scrollable content was still close to
        //     its collapsed size. Revelation, being last, was already close to the
        //     list's max scroll extent, so the scroll got clamped there instead of
        //     reaching the top, and never got a second chance once the grid
        //     finished growing. Verified on device both times: the grid stayed
        //     off-screen (once revealing only a sliver, once not at all).
        //
        // The fix below has two parts: use fadeIn/fadeOut (not expand/shrink) —
        // pure alpha animations reserve their full layout size on the very first
        // frame, so there's no growing target left to race — and scroll the
        // tapped book to the top of the viewport, which is always enough room for
        // any chapter grid. Only scrolling when the tapped row is in the lower
        // half of the viewport (rather than on every tap) avoids force-scrolling
        // books that already had plenty of room below them.
        LaunchedEffect(expandedBookId.value) {
            val expandedId = expandedBookId.value ?: return@LaunchedEffect
            val index = filteredBooks.indexOfFirst { it.id == expandedId }
            if (index < 0) return@LaunchedEffect

            val layoutInfo = listState.layoutInfo
            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val visibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
            val needsScroll = visibleItem == null || visibleItem.offset > viewportHeight / 2
            if (needsScroll) {
                listState.animateScrollToItem(index)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredBooks, key = { it.id }) { book ->
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

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(CHAPTER_GRID_ANIM_MS)),
                    exit = fadeOut(tween(CHAPTER_GRID_ANIM_MS))
                ) {
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
