/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

package app.skyward.sky

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.TabPosition
import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.skyward.design.SkywardPreview
import app.skyward.dial.buildDialModel
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import app.skyward.domain.SkyNow
import app.skyward.sky.dial.DialCanvas
import app.skyward.sky.dial.DialPreviewFixtures
import app.skyward.sky.dial.toColor
import app.skyward.sky.tabs.AboutTab
import app.skyward.sky.tabs.CurrentTab
import app.skyward.sky.tabs.EventsTab
import app.skyward.sky.tabs.StatesTab
import app.skyward.sky.tabs.eventRows
import app.skyward.sky.tabs.moonRows
import app.skyward.sky.tabs.sunRows
import dev.jamesyox.kastro.sol.SolarEvent
import kotlinx.coroutines.launch

private val MaxContentWidth = 600.dp
private val BelowDialTopPadding = 16.dp
private const val DETAIL_CARD_FADE_MS = 220

/** Connects [SkyScreen] to [viewModel], the location permission and the app coming forward. */
@Composable
fun SkyRoute(
    viewModel: SkyViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberLocationPermissionRequester(onResult = viewModel::onPermissionResult)
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onForeground() }
    AskForLocationOnce(state = state, permission = permission)
    SkyScreen(
        state = state,
        onDialTap = viewModel::onDialTap,
        onSelect = viewModel::onSelect,
        onTabSelected = viewModel::onTabSelected,
        onRequestPermission = {
            if ((state as? SkyUiState.NeedsLocation)?.denied == true) {
                permission.openSettings()
            } else {
                permission.request()
            }
        },
        modifier = modifier,
    )
}

/** Shows the permission prompt the first time the screen needs a location. */
@Composable
private fun AskForLocationOnce(
    state: SkyUiState,
    permission: LocationPermissionRequester,
) {
    var asked by rememberSaveable { mutableStateOf(false) }
    if (!asked && state is SkyUiState.NeedsLocation && !state.denied) {
        LaunchedEffect(permission) {
            asked = true
            permission.request()
        }
    }
}

/**
 * Today's dial pinned at the top, then the selection's detail card and the tab bar, with only the
 * tab's content scrolling.
 */
@Suppress("ParameterNaming")
@Composable
fun SkyScreen(
    state: SkyUiState,
    onDialTap: (Double, Double) -> Unit,
    onSelect: (SkyItem?) -> Unit,
    onTabSelected: (SkyTab) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialModifier = Modifier.widthIn(max = MaxContentWidth).fillMaxWidth().aspectRatio(1f)
    Column(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (state) {
            SkyUiState.Loading -> Spacer(dialModifier)
            SkyUiState.Unavailable -> SkyUnavailable(modifier = dialModifier)
            is SkyUiState.NeedsLocation ->
                LocationPrompt(
                    denied = state.denied,
                    locating = state.locating,
                    couldNotLocate = state.couldNotLocate,
                    onRequestPermission = onRequestPermission,
                    modifier = dialModifier,
                )
            is SkyUiState.Ready -> {
                DialCanvas(
                    model = state.dial,
                    onTap = onDialTap,
                    modifier = dialModifier,
                )
                BelowDial(
                    state = state,
                    onSelect = onSelect,
                    onSelectTab = onTabSelected,
                    modifier = Modifier.widthIn(max = MaxContentWidth).fillMaxWidth().weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BelowDial(
    state: SkyUiState.Ready,
    onSelect: (SkyItem?) -> Unit,
    onSelectTab: (SkyTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeZone = state.day.timeZone
    Column(modifier = modifier.padding(top = BelowDialTopPadding)) {
        // Keyed on whether there's a selection, so choosing a different item updates the card in
        // place
        // while showing and dismissing it animates. Exiting content keeps the item it was showing.
        AnimatedContent(
            targetState = state.selected,
            contentKey = { it != null },
            transitionSpec = {
                (fadeIn(
                        tween(DETAIL_CARD_FADE_MS, delayMillis = DETAIL_CARD_FADE_MS / 2),
                    ) togetherWith fadeOut(tween(DETAIL_CARD_FADE_MS / 2)))
                    .using(SizeTransform(clip = false))
            },
            label = "detailCard",
        ) { item ->
            if (item == null) {
                Spacer(Modifier.fillMaxWidth())
            } else {
                val timing = item.timing
                DetailCard(
                    title = item.title,
                    timing = timing.format(timeZone, state.use24Hour),
                    duration = timing.duration,
                    description = item.description,
                    swatch = item.ringColor?.toColor() ?: Color.Unspecified,
                    onDismiss = { onSelect(null) },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .animateContentSize(),
                )
            }
        }
        val pagerState =
            rememberPagerState(initialPage = state.tab.ordinal) { SkyTab.entries.size }
        val scope = rememberCoroutineScope()
        val currentOnSelectTab by rememberUpdatedState(onSelectTab)
        // The pager owns which page shows; the view model hears about it once a swipe settles.
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }
                .collect { currentOnSelectTab(SkyTab.entries[it]) }
        }
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.background,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = followingSwipe(pagerState),
                    width = Dp.Unspecified,
                )
            },
        ) {
            SkyTab.entries.forEach { tab ->
                Tab(
                    selected = tab.ordinal == pagerState.currentPage,
                    onClick = { scope.launch { pagerState.animateScrollToPage(tab.ordinal) } },
                    text = { Text(tab.label) },
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier.fillMaxWidth().weight(1f).background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.Top,
        ) { page ->
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                TabContent(
                    tab = SkyTab.entries[page],
                    state = state,
                    onSelect = onSelect,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
                Spacer(Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }
    }
}

/**
 * Places the tab indicator between the two tabs a swipe is between, in proportion to how far the
 * swipe has gone, at the width of the tab text.
 */
private fun TabIndicatorScope.followingSwipe(pagerState: PagerState): Modifier =
    Modifier.tabIndicatorLayout { measurable, constraints, tabPositions ->
        val position =
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, tabPositions.lastIndex.toFloat())
        val from = tabPositions[position.toInt()]
        val to = tabPositions[ceil(position).toInt()]
        val fraction = position - position.toInt()
        val width = lerp(from.contentWidth, to.contentWidth, fraction).roundToPx()
        val left = lerp(from.contentLeft, to.contentLeft, fraction).roundToPx()
        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        // As wide as the slot the tab row offers, or it would centre the indicator in it.
        layout(constraints.maxWidth, placeable.height) { placeable.place(left, 0) }
    }

private val TabPosition.contentLeft: Dp
    get() = left + (width - contentWidth) / 2

@Composable
private fun TabContent(
    tab: SkyTab,
    state: SkyUiState.Ready,
    onSelect: (SkyItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val day = state.day
    when (tab) {
        SkyTab.STATES -> {
            val sunRows = remember(day, state.use24Hour) { day.sunRows(state.use24Hour) }
            val moonRows = remember(day, state.use24Hour) { day.moonRows(state.use24Hour) }
            StatesTab(
                sunRows = sunRows,
                moonRows = moonRows,
                selected = state.selected,
                onSelect = onSelect,
                modifier = modifier,
            )
        }
        SkyTab.EVENTS -> {
            val rows = remember(day, state.use24Hour) { day.eventRows(state.use24Hour) }
            EventsTab(
                rows = rows,
                selected = state.selected,
                onSelect = onSelect,
                modifier = modifier,
            )
        }
        SkyTab.CURRENT ->
            CurrentTab(
                now = state.now,
                timeZone = day.timeZone,
                use24Hour = state.use24Hour,
                modifier = modifier,
            )
        SkyTab.ABOUT -> AboutTab(modifier = modifier)
    }
}

private val SkyTab.label: String
    get() =
        when (this) {
            SkyTab.STATES -> "States"
            SkyTab.EVENTS -> "Events"
            SkyTab.CURRENT -> "Current"
            SkyTab.ABOUT -> "About"
        }

private fun previewReady(
    tab: SkyTab,
    selected: SkyItem? = null,
    day: SkyDay = DialPreviewFixtures.London,
    now: SkyNow = DialPreviewFixtures.LondonNow,
) =
    SkyUiState.Ready(
        day = day,
        now = now,
        dial = buildDialModel(day, now, selected, use24Hour = false),
        selected = selected,
        tab = tab,
        use24Hour = false,
    )

@Composable
private fun PreviewSkyScreen(state: SkyUiState) {
    SkywardPreview(padding = 0.dp) {
        SkyScreen(
            state = state,
            onDialTap = { _, _ -> },
            onSelect = {},
            onTabSelected = {},
            onRequestPermission = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(widthDp = 390, heightDp = 1100)
@Composable
private fun Preview_SkyScreen_States() {
    PreviewSkyScreen(previewReady(SkyTab.STATES))
}

@Preview(widthDp = 390, heightDp = 1100)
@Composable
private fun Preview_SkyScreen_Events() {
    PreviewSkyScreen(previewReady(SkyTab.EVENTS))
}

@Preview(widthDp = 390, heightDp = 1100)
@Composable
private fun Preview_SkyScreen_Current() {
    PreviewSkyScreen(previewReady(SkyTab.CURRENT))
}

@Preview(widthDp = 390, heightDp = 1100)
@Composable
private fun Preview_SkyScreen_About() {
    PreviewSkyScreen(previewReady(SkyTab.ABOUT))
}

@Preview(widthDp = 390, heightDp = 1100)
@Composable
private fun Preview_SkyScreen_SelectedSpan() {
    val span = DialPreviewFixtures.London.lightSpans.first()
    PreviewSkyScreen(previewReady(SkyTab.STATES, selected = SkyItem.Light(span)))
}

@Preview(widthDp = 390, heightDp = 1100)
@Composable
private fun Preview_SkyScreen_SelectedEvent() {
    val sunset = DialPreviewFixtures.London.solarEvents.first { it is SolarEvent.Sunset }
    PreviewSkyScreen(previewReady(SkyTab.EVENTS, selected = SkyItem.SolarEventItem(sunset)))
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun Preview_SkyScreen_NeedsLocation() {
    PreviewSkyScreen(SkyUiState.NeedsLocation(denied = false))
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun Preview_SkyScreen_NeedsLocationDenied() {
    PreviewSkyScreen(SkyUiState.NeedsLocation(denied = true))
}
