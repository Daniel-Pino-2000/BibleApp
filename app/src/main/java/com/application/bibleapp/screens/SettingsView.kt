package com.application.bibleapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.bibleapp.ui.theme.ReadingStyle
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.ui.theme.ThemeMode
import com.application.bibleapp.ui.theme.VerseTextScale
import com.application.bibleapp.ui.theme.scaledBy
import com.application.bibleapp.viewmodel.BibleViewModel

@Composable
fun SettingsView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by bibleViewModel.themeMode.collectAsState()
    val verseTextScale by bibleViewModel.verseTextScale.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        SettingsSection(title = "Appearance") {
            // Measured on device: "Small"+"Default"+"Large"+"Extra Large" together
            // need ~1024px on a 360dp-wide phone, ~40px more than the row actually
            // has — wrapping the widest chip onto its own line reads as an orphaned
            // button, and there's no font-size/padding trim that reliably closes a
            // 40px gap across devices and system text-scale settings without
            // breaking again. A single scrollable row is the same pattern already
            // used for the suggested-search chips in SearchView, so every chip stays
            // the same size as its siblings and the group still reads as one row.
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { bibleViewModel.setThemeMode(mode) },
                        label = { Text(mode.label) }
                    )
                }
            }
        }

        SettingsSection(title = "Verse Text Size") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                VerseTextScale.entries.forEach { scale ->
                    FilterChip(
                        selected = verseTextScale == scale,
                        onClick = { bibleViewModel.setVerseTextScale(scale) },
                        label = { Text(scale.label) }
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = "1 In the beginning God created the heavens and the earth.",
                    style = ReadingStyle.VerseText.scaledBy(verseTextScale.multiplier),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(Spacing.lg)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        SettingsSection(title = "About") {
            Text("BibleApp", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Version 1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}
