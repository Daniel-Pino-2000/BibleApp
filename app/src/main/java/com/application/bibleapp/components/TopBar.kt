package com.application.bibleapp.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: @Composable () -> Unit = { Text("BibleApp") },
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        // Match the screen background exactly (M3 defaults this to colorScheme.surface,
        // a tone off from background) so the bar — and the status bar drawn behind it —
        // reads as part of the page instead of a separate colored strip. A faint tonal
        // shift on scroll still gives scroll feedback.
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}