package com.application.bibleapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.application.bibleapp.navigation.Screen
import com.application.bibleapp.navigation.bottomNavigationItems

@Composable
fun MainBottomBar(
    currentRoute: String?,
    selectedItemIndex: Int,
    hideBar: Boolean,
    onItemSelected: (Int) -> Unit
) {
    Column(

    ) {

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = Color.LightGray
        )

        if (currentRoute == Screen.Bible.route) {
            BookPickerBar(
                currentBook = 1,
                currentChapter = 1,
                onPrevious = {
                },
                onNext = {
                },
                onSelectBook = { /* TODO */ }
            )
        }

        NavigationBar(
            containerColor = Color.Transparent
        ) {
            bottomNavigationItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = index == selectedItemIndex,
                    onClick = {
                        onItemSelected(index)
                    },
                    icon = {
                        Icon(
                            imageVector = if (index == selectedItemIndex) {
                                item.selectedIcon
                            } else item.unselectedIcon,
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