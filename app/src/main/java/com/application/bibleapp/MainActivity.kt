package com.application.bibleapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.application.bibleapp.components.TopBar
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.navigation.Navigation
import com.application.bibleapp.screens.BibleView
import com.application.bibleapp.ui.theme.BibleAppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.bibleapp.components.BookPickerBar
import com.application.bibleapp.components.MainBottomBar
import com.application.bibleapp.navigation.Screen
import com.application.bibleapp.navigation.bottomNavigationItems
import com.application.bibleapp.viewmodel.BibleViewModel
import com.application.bibleapp.viewmodel.BibleViewModelFactory
import kotlin.Boolean

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val bibleViewModel: BibleViewModel = viewModel(
                factory = BibleViewModelFactory(BibleRepository(this))
            )
            val navController = rememberNavController()

            BibleAppTheme {
                // Observe current screen
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

                var selectedItemIndex by rememberSaveable {
                    mutableIntStateOf(0)
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        when (currentRoute) {
                            "bible" -> TopBar(
                                title = "Bible",
                                actions = {
                                    // Button for version picker
                                    Button(onClick = {
                                    // TODO: open version picker
                                    }) {
                                        Text("Version")
                                    }
                                },
                                scrollBehavior = scrollBehavior)
                            "home" -> TopBar("Daily Verse")
                            "search" -> TopBar("Search")
                            "more" -> TopBar("More")
                            else -> TopBar("BibleApp")
                        }
                    },
                    bottomBar = {

                        MainBottomBar(
                            currentRoute,
                            selectedItemIndex,
                            bibleViewModel,
                            hideBar = false,
                            onItemSelected = { index ->
                                selectedItemIndex = index
                                navController.navigate(bottomNavigationItems[index].route)

                            },
                            onBookPickerClicked = {
                                navController.navigate(Screen.BookPicker.route)
                            }
                        )
                    }

                ) { paddingValues ->
                    Navigation(navController, paddingValues, bibleViewModel)
                }
            }
        }
    }
}