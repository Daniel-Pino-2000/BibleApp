package com.application.bibleapp.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.screens.BibleView
import com.application.bibleapp.screens.BookPickerScreen
import com.application.bibleapp.screens.HomeView
import com.application.bibleapp.viewmodel.BibleViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    padding: PaddingValues,
    bibleViewModel: BibleViewModel
) {
    val context = LocalContext.current
    val repository = BibleRepository(context)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeView(modifier = Modifier.padding(padding))
        }

        composable(Screen.Bible.route) {
            BibleView(bibleViewModel, padding)
        }

        composable(Screen.Search.route) {
            Toast.makeText(context, "Search Screen coming soon!", Toast.LENGTH_SHORT).show()
        }

        composable(Screen.More.route) {
            Toast.makeText(context, "Settings Screen coming soon!", Toast.LENGTH_SHORT).show()
        }

        composable(Screen.BookPicker.route) {
            BookPickerScreen(bibleViewModel) {
                navController.popBackStack()
            }
        }
    }
}
