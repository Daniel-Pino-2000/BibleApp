package com.application.bibleapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.application.bibleapp.navigation.bottomNavigationItems

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

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
                        NavigationBar {
                            bottomNavigationItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = index == selectedItemIndex,
                                    onClick = {
                                        selectedItemIndex = index
                                        navController.navigate(item.route)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (index == selectedItemIndex) {
                                                item.selectedIcon
                                            } else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                )
                            }
                        }
                    }

                ) { paddingValues ->
                    Navigation(navController, paddingValues)
                }
            }
        }
    }
}