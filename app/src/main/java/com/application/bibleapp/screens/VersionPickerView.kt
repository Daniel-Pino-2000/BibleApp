package com.application.bibleapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.application.bibleapp.viewmodel.BibleViewModel

@Composable
fun VersionPickerView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onVersionClicked: () -> Unit
) {
    val versions by bibleViewModel.availableVersions.collectAsState()
    val isLoading by bibleViewModel.isLoadingVersions.collectAsState()
    val error by bibleViewModel.versionsError.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {

        // loading spinner while fetching
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        // show error message if fetch failed
        if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Failed to load versions", color = Color.Red)
                    Text(
                        text = "Tap to retry",
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { bibleViewModel.loadAvailableVersions() }
                    )
                }
            }
            return@Column
        }

        // empty state message
        if (versions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No versions available")
            }
            return@Column
        }

        Text("Versions count: ${versions.size}", modifier = Modifier.padding(8.dp))

        LazyColumn {
            items(versions) { version ->
                Text(
                    text = "${version.version} - ${version.language.name}",
                    modifier = Modifier
                        .clickable {
                            bibleViewModel.setSelectedVersion(version.id)
                            onVersionClicked()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
                HorizontalDivider()
            }
        }
    }
}