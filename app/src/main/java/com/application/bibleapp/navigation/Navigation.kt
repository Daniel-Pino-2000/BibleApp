package com.application.bibleapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.screens.BibleView
import com.application.bibleapp.screens.BookPickerView
import com.application.bibleapp.screens.HomeView
import com.application.bibleapp.screens.SearchView
import com.application.bibleapp.screens.SettingsView
import com.application.bibleapp.screens.VersePickerView
import com.application.bibleapp.screens.VersionPickerView
import com.application.bibleapp.viewmodel.BibleViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    padding: PaddingValues,
    bibleViewModel: BibleViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeView(
                bibleViewModel = bibleViewModel,
                modifier = Modifier.padding(padding),
                onContinueReadingClick = { navController.navigate(Screen.Bible.route) },
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(Screen.Bible.route) {
            BibleView(bibleViewModel, modifier = Modifier.padding(padding))
        }

        composable(Screen.Search.route) {
            SearchView(bibleViewModel, modifier = Modifier.padding(padding)) { bookId, chapter, verse ->
                // Clear search first to avoid rendering issues
                bibleViewModel.clearSearchResults()
                bibleViewModel.setBook(bookId, chapter, verse)

                // Navigate safely after clearing
                navController.navigate(Screen.Bible.route)
            }
        }


        composable(Screen.More.route) {
            SettingsView(bibleViewModel, modifier = Modifier.padding(padding))
        }

        composable(Screen.BookPicker.route) {
            BookPickerView(
                bibleViewModel,
                modifier = Modifier.padding(padding),
                onBackClick = {
                    navController.popBackStack()
                },
                onChapterClick = { bookId, chapterNumber ->
                    // 1. Update ViewModel with selected book & chapter
                    bibleViewModel.setBook(bookId, chapterNumber)

                    // 2. Navigate to VersePicker
                    navController.navigate(Screen.VersePicker.route)
                }
            )
        }

        composable(Screen.VersePicker.route) {
            VersePickerView(
                bibleViewModel,
                modifier = Modifier.padding(padding),
                onBackClick = {
                    navController.popBackStack()
                } ,
                onVerseClicked = {
                    navController.navigate(Screen.Bible.route)
                }
            )

        }

        composable(Screen.VersionPicker.route) {
            VersionPickerView(
                bibleViewModel,
                modifier = Modifier.fillMaxSize().padding(padding),
                onVersionClicked = {
                    navController.navigate(Screen.Bible.route)
                }
            )
        }
    }
}

