package com.application.bibleapp.components

import android.widget.TextView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun BibleText(
    verses: List<VerseUI>,
    scrollToIndex: Int,
    modifier: Modifier = Modifier
) {
    // Create a new LazyListState each time the verses list changes
    val listState = remember(verses) { androidx.compose.foundation.lazy.LazyListState() }

    // Scroll to the desired verse whenever verses or scroll index change
    LaunchedEffect(verses.size, scrollToIndex) {
        val index = (scrollToIndex - 1).coerceIn(verses.indices)
        listState.scrollToItem(index)
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 5.dp, horizontal = 5.dp)
    ) {
        items(verses) { verse ->
            Text(text = "${verse.verse} ${verse.text}")
        }
    }
}
