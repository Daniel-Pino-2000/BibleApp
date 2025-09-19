package com.application.bibleapp.components

import android.widget.TextView
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.bibleapp.data.model.BibleVerse
import com.application.bibleapp.data.model.VerseUI
import androidx.compose.foundation.lazy.items

@Composable
fun BibleText(
    verses: List<VerseUI>,
) {
    LazyColumn(modifier = Modifier.padding(5.dp)) {
        items(verses) { verse ->
            Text(text = "${verse.verse} ${verse.text}")
        }
    }
}