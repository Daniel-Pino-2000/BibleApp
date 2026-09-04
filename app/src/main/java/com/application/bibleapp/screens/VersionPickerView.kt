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
import com.application.bibleapp.data.model.BibleTranslation
import com.application.bibleapp.data.model.DownloadedVersionInfo
import com.application.bibleapp.data.model.SelectedBibleVersion
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.viewmodel.BibleViewModel

/**
 * Renders each translation with one of three states, purely from what
 * [BibleViewModel.selectedVersion] and [BibleViewModel.downloadedVersions] say —
 * this composable never queries local storage itself:
 * - active (blue checkmark) — `version.id == selectedVersion.id`.
 * - downloaded, not active (gray check + "Downloaded", or a refresh icon +
 *   "Update available" if [com.application.bibleapp.data.model.DownloadedVersionInfo.isUpToDate]
 *   is false) — `version.id` is a key in `downloadedVersions`.
 * - neither — tapping it downloads before switching.
 *
 * Downloaded translations are pulled out into their own "Downloaded" section at the
 * top, regardless of language, so a user's already-available versions are never
 * buried inside a language group they'd have to scroll to find. The language
 * groups below only list the not-yet-downloaded translations for that language.
 */
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
                    Text("Failed to load versions", color = MaterialTheme.colorScheme.error)
                    Text(
                        text = "Tap to retry",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = Spacing.sm)
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = Spacing.sm)
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
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            placeholder = { Text("Search by language or version") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        // Download error banner
        downloadError?.let {
            Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
                Text(text = "Download failed: $it", color = MaterialTheme.colorScheme.error)
                Text(
                    text = "Tap a version below to retry",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Download info banner (success, but some content wasn't available for this version)
        downloadInfo?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            )
        }

        if (groupedVersions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No versions match \"$searchQuery\"")
            }
            return@Column
        }

        // Downloaded translations are pulled out of their language groups into one
        // section up top (order preserved from the language-sorted groups, so it
        // still reads alphabetically by language/name rather than by download time).
        // Everything here is already search-filtered, since it's derived from
        // groupedVersions rather than the raw availableVersions list.
        val downloadedTranslations = groupedVersions
            .flatMap { it.translations }
            .filter { downloadedVersions.containsKey(it.id) }
        val languageGroupsWithoutDownloaded = groupedVersions.mapNotNull { group ->
            val remaining = group.translations.filterNot { downloadedVersions.containsKey(it.id) }
            if (remaining.isEmpty()) null else group.copy(translations = remaining)
        }

        LazyColumn {
            if (downloadedTranslations.isNotEmpty()) {
                stickyHeader {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = "Downloaded",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        )
                    }
                }

                items(downloadedTranslations, key = { it.id }) { version ->
                    VersionRow(
                        version = version,
                        selectedVersion = selectedVersion,
                        downloadingVersionId = downloadingVersionId,
                        downloadProgress = downloadProgress,
                        downloadedVersions = downloadedVersions,
                        bibleViewModel = bibleViewModel,
                        onVersionClicked = onVersionClicked
                    )
                }
            }

            languageGroupsWithoutDownloaded.forEach { group ->
                stickyHeader {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = group.languageName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        )
                    }
                }

                items(group.translations, key = { it.id }) { version ->
                    VersionRow(
                        version = version,
                        selectedVersion = selectedVersion,
                        downloadingVersionId = downloadingVersionId,
                        downloadProgress = downloadProgress,
                        downloadedVersions = downloadedVersions,
                        bibleViewModel = bibleViewModel,
                        onVersionClicked = onVersionClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionRow(
    version: BibleTranslation,
    selectedVersion: SelectedBibleVersion,
    downloadingVersionId: String?,
    downloadProgress: Float,
    downloadedVersions: Map<String, DownloadedVersionInfo>,
    bibleViewModel: BibleViewModel,
    onVersionClicked: () -> Unit
) {
    val isSelected = version.id == selectedVersion.id
    val isDownloading = version.id == downloadingVersionId
    val downloadInfoForVersion = downloadedVersions[version.id]
    val isDownloaded = downloadInfoForVersion != null
    val needsUpdate = downloadInfoForVersion != null && !downloadInfoForVersion.isUpToDate

    Column {
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
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = version.displayName, style = MaterialTheme.typography.bodyLarge)
                version.nativeName.takeIf { it.isNotBlank() && it != version.displayName }?.let { nativeName ->
                    Text(
                        text = nativeName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Every entry here already passed the standard-canon filter (see
                // BibleRepository.getAllVersions), so this always reads "66 books" — it's
                // a confirmation for the user, not a distinguishing detail between rows.
                Text(
                    text = "${version.numberOfBooks} books",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (needsUpdate) {
                    Text(
                        text = "Update available — tap ⟳ for footnotes and formatting",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (isDownloaded && !isSelected) {
                    Text(
                        text = "Downloaded",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isDownloading) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        contentDescription = "Re-download ${version.displayName} for footnotes and formatting added since it was downloaded",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (isDownloaded && !isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Already downloaded",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = Spacing.sm)
                )
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = Spacing.sm)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}