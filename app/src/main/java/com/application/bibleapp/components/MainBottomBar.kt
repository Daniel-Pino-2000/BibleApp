package com.application.bibleapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
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
 * Two independent behaviors share [chromeHiddenFraction] — a single, already-smoothed and
 * debounced 0..1 value (see MainActivity) — so everything that moves, moves together and
 * with identical timing on one scroll gesture:
 * - The current book/chapter label ([BookPickerBar]) always stays laid out and visible —
 *   it's the reader's only anchor once the rest of the chrome recedes — only its prev/next
 *   arrows fade out.
 * - The tab navigation row collapses (shrinks + fades) via [CollapsingTabBar], freeing the
 *   space it occupied for the reading content below.
 *
 * [navigationBarsPadding] is applied once, here, at the outermost level — not inside
 * [NavigationBar] (its own default inset handling is turned off via `windowInsets =
 * WindowInsets(0)`) — so there's exactly one place reserving room for the system
 * navigation bar. That one reservation covers [BookPickerBar] too even after the tab row
 * has fully collapsed out from under it, which is what actually keeps the persistent
 * chapter label from ending up underneath the system bar: it was previously relying on
 * the tab row's own inset padding, which vanished along with the tab row itself.
 */
@Composable
fun MainBottomBar(
    currentRoute: String?,
    bibleViewModel: BibleViewModel,
    hideBar: Boolean,
    chromeHiddenFraction: Float = 0f,
    onItemSelected: (String) -> Unit,
    onBookPickerClicked: () -> Unit
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        if (currentRoute == Screen.Bible.route) {
            val currentBook by bibleViewModel.currentBook.collectAsState()
            val currentChapter by bibleViewModel.currentChapter.collectAsState()

            BookPickerBar(
                currentBook = currentBook,
                currentChapter = currentChapter,
                arrowsAlpha = 1f - chromeHiddenFraction,
                arrowsEnabled = chromeHiddenFraction < 0.5f,
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
            collapsedFraction = chromeHiddenFraction,
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    // Insets for the system nav bar are reserved once, at the MainBottomBar
                    // level, so they still apply once this row has collapsed away — see the
                    // class doc above.
                    windowInsets = WindowInsets(0, 0, 0, 0)
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
