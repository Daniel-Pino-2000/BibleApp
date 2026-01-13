package com.application.bibleapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.application.bibleapp.viewmodel.BibleViewModel

@Composable
fun VersionPickerView(
    bibleViewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onVersionClicked: () -> Unit
) {

    val versions by bibleViewModel.availableVersions.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(versions) { version ->
                Text(
                    text = "${version.version} - ${version.language.name}",
                    modifier = Modifier.clickable {
                        bibleViewModel.setSelectedVersion(version.id)
                    }
                )
            }
        }
    }
}