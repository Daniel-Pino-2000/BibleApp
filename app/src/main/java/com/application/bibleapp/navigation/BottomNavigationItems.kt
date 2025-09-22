package com.application.bibleapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import com.application.bibleapp.navigation.BottomNavigationItem

val bottomNavigationItems = listOf(
    BottomNavigationItem(
        Screen.Home.title,
        Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    BottomNavigationItem(
        Screen.Bible.title,
        Screen.Bible.route,
        selectedIcon = Icons.Filled.Book,
        unselectedIcon = Icons.Outlined.Book,
    ),
    BottomNavigationItem(
        Screen.Search.title,
        Screen.Search.route,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
    ),
    BottomNavigationItem(
        Screen.More.title,
        Screen.More.route,
        selectedIcon = Icons.Filled.Menu,
        unselectedIcon = Icons.Outlined.Menu,
    )

)