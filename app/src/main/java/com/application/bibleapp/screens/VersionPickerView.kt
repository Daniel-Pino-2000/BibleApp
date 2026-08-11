package com.application.bibleapp.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.bibleapp.viewmodel.BibleViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VersionPickerView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onVersionClicked: () -> Unit
) {
    val versions by bibleViewModel.availableVersions.collectAsState()
    val groupedVersions by bibleViewModel.groupedVersions.collectAsState()
    val searchQuery by bibleViewModel.versionSearchQuery.collectAsState()
    val isLoading by bibleViewModel.isLoadingVersions.collectAsState()
    val error by bibleViewModel.versionsError.collectAsState()
    val selectedVersion by bibleViewModel.selectedVersion.collectAsState()
    val downloadedVersions by bibleViewModel.downloadedVersions.collectAsState()
    val downloadingVersionId by bibleViewModel.downloadingVersionId.collectAsState()
    val downloadProgress by bibleViewModel.downloadProgress.collectAsState()
    val downloadError by bibleViewModel.downloadError.collectAsState()
    val downloadInfo by bibleViewModel.downloadInfo.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

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

        if (versions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No versions available")
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

        OutlinedTextField(
            value = searchQuery,
            onValueChange = bibleViewModel::onVersionSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search by language or version") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        // Download error banner
        downloadError?.let {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = "Download failed: $it", color = Color.Red)
                Text(text = "Tap a version below to retry", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // Download info banner (success, but some content wasn't available for this version)
        downloadInfo?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (groupedVersions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No versions match \"$searchQuery\"")
            }
            return@Column
        }

        LazyColumn {
            groupedVersions.forEach { group ->
                stickyHeader {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = group.languageName,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                items(group.translations, key = { it.id }) { version ->
                    val isSelected = version.id == selectedVersion.id
                    val isDownloading = version.id == downloadingVersionId
                    val downloadInfoForVersion = downloadedVersions[version.id]
                    val isDownloaded = downloadInfoForVersion != null
                    val needsUpdate = downloadInfoForVersion != null && !downloadInfoForVersion.isUpToDate

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = downloadingVersionId == null) {
                                // Only leave this screen once the version is actually ready —
                                // otherwise the download progress/error never gets seen. Tapping
                                // the already-active version is a no-op (no re-fetch/re-switch),
                                // it just finishes immediately and lets the caller dismiss the picker.
                                bibleViewModel.selectVersion(version.id) { success ->
                                    if (success) onVersionClicked()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = version.displayName)
                            version.nativeName.takeIf { it.isNotBlank() && it != version.displayName }?.let { nativeName ->
                                Text(text = nativeName, fontSize = 12.sp, color = Color.Gray)
                            }
                            if (needsUpdate) {
                                Text(
                                    text = "Update available — tap ⟳ for footnotes and formatting",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            } else if (isDownloaded && !isSelected) {
                                Text(text = "Downloaded", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (isDownloading) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        if (needsUpdate) {
                            IconButton(
                                enabled = downloadingVersionId == null,
                                onClick = { bibleViewModel.redownloadVersion(version.id) }
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Re-download ${version.displayName} for footnotes and formatting added since it was downloaded"
                                )
                            }
                        } else if (isDownloaded && !isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Already downloaded",
                                tint = Color.Gray,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    HorizontalDivider()
                }
            }
        }
    }
}