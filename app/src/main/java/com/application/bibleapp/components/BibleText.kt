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

@Composable
fun BibleText(
    verses: List<VerseUI>,
    scrollToIndex: Int,
    modifier: Modifier = Modifier
) {
    // Remember LazyListState to control scrolling
    val listState = rememberLazyListState()

    LaunchedEffect(verses.size, scrollToIndex) {
        val index = scrollToIndex - 1  // convert to 0-based
        if (index in verses.indices) {
            listState.scrollToItem(index)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 5.dp, horizontal = 5.dp) // internal content spacing only
    ) {
        items(verses) { verse ->
            Text(text = "${verse.verse} ${verse.text}")
        }
    }
}