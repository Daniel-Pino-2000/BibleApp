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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

            // The system status bar hides/shows together with the rest of the reading
            // chrome — driven by the same collapsedFraction the top/bottom bars use, so
            // it disappears and reappears on the same scroll gesture, not independently.
            // WindowInsetsController only has a discrete show()/hide() (no per-frame
            // animation to drive continuously like a Compose modifier), so it's toggled
            // at a threshold near each end of the collapse instead of every frame.
            LaunchedEffect(scrollBehavior) {
                val behavior = scrollBehavior ?: return@LaunchedEffect
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                snapshotFlow { behavior.state.collapsedFraction }
                    .collect { fraction ->
                        if (fraction > 0.9f) {
                            insetsController.hide(WindowInsetsCompat.Type.statusBars())
                        } else if (fraction < 0.1f) {
                            insetsController.show(WindowInsetsCompat.Type.statusBars())
                        }
                    }
            }

            // Guards against leaving the Bible screen mid-scroll with the status bar still
            // hidden — every other screen always shows it.
            LaunchedEffect(currentRoute) {
                if (currentRoute != Screen.Bible.route) {
                    WindowCompat.getInsetsController(window, window.decorView)
                        .show(WindowInsetsCompat.Type.statusBars())
                }
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
                            Screen.Bible.route -> {
                                val versionLabel by bibleViewModel.currentVersionLabel.collectAsState()
                                BibleTopBar(
                                    scrollBehavior = scrollBehavior,
                                    versionLabel = versionLabel,
                                    onVersionClick = { navController.navigate(Screen.VersionPicker.route) }
                                )
                            }
                            Screen.Home.route -> HomeTopBar()
                            Screen.Search.route -> SearchTopBar()
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
                                scrollBehavior = scrollBehavior,
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