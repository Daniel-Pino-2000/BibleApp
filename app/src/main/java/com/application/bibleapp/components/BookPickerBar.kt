package com.application.bibleapp.components

import android.R.attr.onClick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.application.bibleapp.data.model.BibleBooks

@Composable
fun BookPickerBar(
    currentBook: Int,
    currentChapter: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectBook: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Chapter")
        }

        Button(onClick = onSelectBook) {
            // Compute reactively, recomposes automatically when currentBook or currentChapter changes
            Text("${BibleBooks.getBookById(currentBook)?.name ?: "Unknown"} $currentChapter")
        }

        IconButton(onClick = onNext) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next Chapter")
        }
    }
}
