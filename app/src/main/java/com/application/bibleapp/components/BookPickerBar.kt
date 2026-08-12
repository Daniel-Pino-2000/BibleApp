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
import androidx.compose.ui.unit.dp
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.ui.theme.Spacing

/**
 * A compact toolbar of just the prev/next chapter arrows — sits directly above
 * [ChapterAnchorBar] as a related-but-distinct piece (see [MainBottomBar]'s cohesive
 * bottom-chrome grouping), not the label itself. Arrows sit at the row's edges for
 * one-handed thumb reach, matching how the anchor's label sits centered below.
 */
@Composable
fun ChapterArrowsToolbar(
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilledTonalIconButton(
            onClick = onPrevious,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Chapter")
        }

        FilledTonalIconButton(
            onClick = onNext,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Chapter")
        }
    }
}

/**
 * The persistent "you are here" anchor — always visible even once the rest of the
 * reading chrome hides on scroll (see [MainBottomBar]). Centered and set in a slightly
 * larger type than a typical toolbar label, since it's meant to read as the more
 * prominent of the two chapter-navigation pieces.
 */
@Composable
fun ChapterAnchorBar(
    currentBook: Int,
    currentChapter: Int,
    onSelectBook: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.Center
    ) {
        FilledTonalButton(
            onClick = onSelectBook,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            // Compute reactively, recomposes automatically when currentBook or currentChapter changes
            Text(
                text = "${BibleBooks.getBookById(currentBook)?.name ?: "Unknown"} $currentChapter",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
