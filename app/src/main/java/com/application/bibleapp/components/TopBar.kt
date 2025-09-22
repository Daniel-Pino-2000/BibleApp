package com.application.bibleapp.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    actions: @Composable () -> Unit = {}, // default empty action
    scrollBehavior: TopAppBarScrollBehavior? = null
) {

    TopAppBar(
        title = {
            Text(text = title)
        },
        actions = { actions() },
        scrollBehavior = scrollBehavior
    )
}