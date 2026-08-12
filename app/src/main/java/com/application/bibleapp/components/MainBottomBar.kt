package com.application.bibleapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.application.bibleapp.navigation.Screen
import com.application.bibleapp.navigation.bottomNavigationItems
import com.application.bibleapp.viewmodel.BibleViewModel
import kotlin.math.roundToInt

/**
 * Two independent behaviors share the same scroll-driven [TopAppBarScrollBehavior.state]
 * fraction the top bar uses (so everything that moves, moves in lockstep with one gesture):
 * - The current book/chapter label ([BookPickerBar]) always stays laid out and visible —
 *   it's the reader's only anchor once the rest of the chrome recedes — only its prev/next
 *   arrows fade out.
 * - The tab navigation row collapses (shrinks + fades) via [CollapsingTabBar], freeing the
 *   space it occupied for the reading content below.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomBar(
    currentRoute: String?,
    bibleViewModel: BibleViewModel,
    hideBar: Boolean,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onItemSelected: (String) -> Unit,
    onBookPickerClicked: () -> Unit
) {
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction ?: 0f

    Column {
        if (currentRoute == Screen.Bible.route) {
            val currentBook by bibleViewModel.currentBook.collectAsState()
            val currentChapter by bibleViewModel.currentChapter.collectAsState()

            BookPickerBar(
                currentBook = currentBook,
                currentChapter = currentChapter,
                arrowsAlpha = 1f - collapsedFraction,
                arrowsEnabled = collapsedFraction < 0.5f,
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

        CollapsingTabBar(
            currentRoute = currentRoute,
            collapsedFraction = collapsedFraction,
            onItemSelected = onItemSelected
        )
    }
}

/**
 * The custom [Layout] reports a shrinking height (not just a visual offset) as it collapses,
 * so the Scaffold reclaims the freed space and the reading content below can expand into it,
 * rather than leaving an empty gap where the bar used to be.
 */
@Composable
private fun CollapsingTabBar(
    currentRoute: String?,
    collapsedFraction: Float,
    onItemSelected: (String) -> Unit
) {
    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds(),
        content = {
            Column(modifier = Modifier.alpha(1f - collapsedFraction)) {

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
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
    ) { measurables, constraints ->
        // Measure the bar at its natural height (ignore the incoming height constraint)
        // so shrinking the reported layout height below never squashes its contents.
        val placeable = measurables.first().measure(
            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        )
        val visibleHeight = (placeable.height * (1f - collapsedFraction))
            .roundToInt()
            .coerceIn(0, placeable.height)

        layout(placeable.width, visibleHeight) {
            // Anchor to the bottom of the shrinking box so the nav bar (screen-edge-most,
            // most reachable) is the last part to disappear as the bar collapses.
            placeable.placeRelative(0, visibleHeight - placeable.height)
        }
    }
}
