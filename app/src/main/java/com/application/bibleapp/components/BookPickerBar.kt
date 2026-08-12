package com.application.bibleapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.ui.theme.Spacing

/**
 * The current book/chapter label always stays visible (it's the reader's only anchor once
 * the rest of the chrome hides on scroll) — only the prev/next arrows fade out, via
 * [arrowsAlpha] driven by the same scroll fraction the rest of the collapsing chrome uses.
 * [arrowsEnabled] disables the buttons once they're invisible so a scroll-triggered fade
 * can't leave an invisible-but-still-tappable target behind.
 */
@Composable
fun BookPickerBar(
    currentBook: Int,
    currentChapter: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectBook: () -> Unit,
    arrowsAlpha: Float = 1f,
    arrowsEnabled: Boolean = true
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilledTonalIconButton(
            onClick = onPrevious,
            enabled = arrowsEnabled,
            modifier = Modifier
                .size(48.dp)
                .alpha(arrowsAlpha),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Chapter")
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

        FilledTonalIconButton(
            onClick = onNext,
            enabled = arrowsEnabled,
            modifier = Modifier
                .size(48.dp)
                .alpha(arrowsAlpha),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Chapter")
        }
    }
}
