package com.application.bibleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.application.bibleapp.components.BibleTopBar
import com.application.bibleapp.components.BookPickerTopBar
import com.application.bibleapp.components.HomeTopBar
import com.application.bibleapp.components.MainBottomBar
import com.application.bibleapp.components.SearchTopBar
import com.application.bibleapp.components.SettingsTopBar
import com.application.bibleapp.components.TopBar
import com.application.bibleapp.components.VersePickerTopBar
import com.application.bibleapp.components.VersionPickerTopBar
import com.application.bibleapp.navigation.Navigation
import com.application.bibleapp.navigation.Screen
import com.application.bibleapp.ui.theme.BibleAppTheme
import com.application.bibleapp.ui.theme.ThemeMode
import com.application.bibleapp.viewmodel.BibleViewModel
import com.application.bibleapp.viewmodel.BibleViewModelFactory

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val bibleViewModel: BibleViewModel = viewModel(
                factory = BibleViewModelFactory(context = this)
            )

            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route

            val scrollBehavior = if (currentRoute == Screen.Bible.route) {
                TopAppBarDefaults.enterAlwaysScrollBehavior()
            } else {
                null
            }

            val themeMode by bibleViewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            BibleAppTheme(darkTheme = useDarkTheme) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            else Modifier
                        ),
                    topBar = {
                        when (currentRoute) {
                            Screen.Bible.route -> BibleTopBar(
                                scrollBehavior = scrollBehavior,
                                onVersionClick = { navController.navigate(Screen.VersionPicker.route) }
                            )
                            Screen.Home.route -> HomeTopBar()
                            Screen.Search.route -> SearchTopBar { }
                            Screen.BookPicker.route -> BookPickerTopBar { navController.popBackStack() }
                            Screen.VersePicker.route -> VersePickerTopBar { navController.popBackStack() }
                            Screen.VersionPicker.route -> VersionPickerTopBar { navController.popBackStack() }
                            Screen.More.route -> SettingsTopBar()
                            else -> TopBar()
                        }
                    },
                    bottomBar = {
                        if (currentRoute !in listOf(Screen.BookPicker.route, Screen.VersePicker.route)) {
                            MainBottomBar(
                                currentRoute = currentRoute,
                                bibleViewModel = bibleViewModel,
                                hideBar = false,
                                onItemSelected = { route -> navController.navigate(route) },
                                onBookPickerClicked = { navController.navigate(Screen.BookPicker.route) }
                            )
                        }
                    }
                ) { paddingValues ->
                    Navigation(navController, paddingValues, bibleViewModel)
                }
            }
        }
    }
}