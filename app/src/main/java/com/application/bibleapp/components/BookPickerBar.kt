package com.application.bibleapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.ui.theme.Spacing

@Composable
fun BookPickerBar(
    currentBook: Int,
    currentChapter: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectBook: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Previous Chapter",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        FilledTonalButton(
            onClick = onSelectBook,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            // Compute reactively, recomposes automatically when currentBook or currentChapter changes
            Text("${BibleBooks.getBookById(currentBook)?.name ?: "Unknown"} $currentChapter")
        }

        IconButton(onClick = onNext) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Next Chapter",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
