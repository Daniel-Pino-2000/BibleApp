package com.application.bibleapp.components

import android.R.attr.contentDescription
import android.R.attr.label
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.bibleapp.navigation.Screen
import com.application.bibleapp.navigation.bottomNavigationItems
import com.application.bibleapp.viewmodel.BibleViewModel
import androidx.compose.runtime.getValue


@Composable
fun MainBottomBar(
    currentRoute: String?,
    bibleViewModel: BibleViewModel,
    hideBar: Boolean,
    onItemSelected: (String) -> Unit,
    onBookPickerClicked: () -> Unit
) {
    Column(

    ) {

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color.LightGray
        )

        if (currentRoute == Screen.Bible.route) {

            val currentBook by bibleViewModel.currentBook.collectAsState()
            val currentChapter by bibleViewModel.currentChapter.collectAsState()

            BookPickerBar(
                currentBook = currentBook,
                currentChapter = currentChapter,
                onPrevious = {
                    bibleViewModel.previousChapter()
                },
                onNext = {
                    bibleViewModel.nextChapter()
                },
                onSelectBook = {
                    onBookPickerClicked()
                }
            )
        }

        NavigationBar(
            containerColor = Color.Transparent
        ) {
            bottomNavigationItems.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        onItemSelected(item.route)
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    },
                    label = {
                        Text(item.title)
                    },

                    )
            }
        }
    }
}