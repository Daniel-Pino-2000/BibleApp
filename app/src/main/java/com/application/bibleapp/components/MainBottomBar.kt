package com.application.bibleapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.application.bibleapp.navigation.Screen
import com.application.bibleapp.navigation.bottomNavigationItems
import com.application.bibleapp.viewmodel.BibleViewModel
import kotlin.math.roundToInt

/**
 * The whole bottom-of-screen chrome — chapter arrows, the persistent chapter anchor, and
 * the tab navigation row — reads as one cohesive group rather than three stacked strips:
 * - A single outer [Surface] supplies one background color and one base tonal elevation
 *   for the entire group (arrows toolbar, tab row, and the gap the system nav bar shows
 *   through via [navigationBarsPadding]), so there's no color or shadow seam between them.
 *   [NavigationBar] below no longer paints its own container color for the same reason.
 * - [ChapterAnchorBar] — the one piece that stays visible once everything else recedes on
 *   scroll — sits at a slightly higher tonal elevation than its surroundings, reading as
 *   the "anchor" the rest of the chrome is arranged around, with hairline dividers marking
 *   it off from the (collapsible) toolbar above and tab row below.
 * - [chromeHiddenFraction] — a single, already-smoothed and debounced 0..1 value (see
 *   MainActivity) — drives both the arrows toolbar and the tab row through the same
 *   [CollapsingSection], so the two collapsible pieces shrink/fade in perfect lockstep;
 *   only [ChapterAnchorBar] never collapses.
 *
 * [navigationBarsPadding] is applied once, here, at the outermost level — not inside
 * [NavigationBar] (its own default inset handling is turned off via `windowInsets =
 * WindowInsets(0)`) — so there's exactly one place reserving room for the system
 * navigation bar, and it still applies to [ChapterAnchorBar] even after the tab row has
 * fully collapsed out from under it. Combined with edge-to-edge leaving the system nav
 * bar transparent, the same surface color painted here is what shows through behind it,
 * so the transition into the system bar's own space is seamless rather than a color break.
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
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        Column {
            if (currentRoute == Screen.Bible.route) {
                val currentBook by bibleViewModel.currentBook.collectAsState()
                val currentChapter by bibleViewModel.currentChapter.collectAsState()

                CollapsingSection(collapsedFraction = chromeHiddenFraction) {
                    Column {
                        ChapterArrowsToolbar(
                            onPrevious = { bibleViewModel.previousChapter() },
                            onNext = { bibleViewModel.nextChapter() }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                }

                // A touch more tonal elevation than its surroundings — the anchor reads as
                // slightly "raised" relative to the toolbar/tab row around it, the same
                // system M3 already uses to signal elevation, no new color introduced.
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation + 2.dp
                ) {
                    ChapterAnchorBar(
                        currentBook = currentBook,
                        currentChapter = currentChapter,
                        onSelectBook = onBookPickerClicked
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
            }

            CollapsingSection(collapsedFraction = chromeHiddenFraction) {
                NavigationBar(
                    // Transparent: the color for this whole group comes from the single
                    // outer Surface above, so the tab row blends into it with no seam.
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
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
    }
}

/**
 * Shrinks its content's reported height to `0` at [collapsedFraction] `1f` (not just a
 * visual offset), so the Scaffold reclaims the freed space and the reading content below
 * can expand into it, and fades it out over the same range. Used for both the arrows
 * toolbar and the tab row so they collapse identically, driven by the same fraction.
 */
@Composable
private fun CollapsingSection(
    collapsedFraction: Float,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds(),
        content = {
            Box(modifier = Modifier.alpha(1f - collapsedFraction)) {
                content()
            }
        }
    ) { measurables, constraints ->
        // Measure at natural height (ignore the incoming height constraint) so shrinking
        // the reported layout height below never squashes the content.
        val placeable = measurables.first().measure(
            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        )
        val visibleHeight = (placeable.height * (1f - collapsedFraction))
            .roundToInt()
            .coerceIn(0, placeable.height)

        layout(placeable.width, visibleHeight) {
            // Anchor to the bottom of the shrinking box so the screen-edge-most (most
            // reachable) part of the content is the last to disappear as it collapses.
            placeable.placeRelative(0, visibleHeight - placeable.height)
        }
    }
}
