package com.application.bibleapp.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Bible : Screen("bible", "Bible")
    object Search : Screen("search", "Search")
    object More : Screen("more", "More")
}
