package com.application.bibleapp.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.application.bibleapp.ui.theme.Spacing

@Composable
fun SearchBar(
    searchText: String,
    onTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search"
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = { onTextChange(it) },
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.sm),
        label = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Icon")
        },
        trailingIcon = {
            IconButton(onClick = { onSearchClick() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Search")
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(percent = 50)
    )
}
