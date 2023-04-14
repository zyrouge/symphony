package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.SettingsKeys
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.PlaybackPosition
import io.github.zyrouge.symphony.services.radio.RadioLoopMode
import io.github.zyrouge.symphony.services.radio.RadioSleepTimer
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.*
import io.github.zyrouge.symphony.ui.view.settings.SettingsTileDefaults
import io.github.zyrouge.symphony.utils.DurationFormatter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*

@Immutable
private data class PlayerStateData(
    val song: Song,
    val isPlaying: Boolean,
    val currentSongIndex: Int,
    val queueSize: Int,
    val currentLoopMode: RadioLoopMode,
    val currentShuffleMode: Boolean,
    val currentSpeed: Float,
    val currentPitch: Float,
    val persistedSpeed: Float,
    val persistedPitch: Float,
    val hasSleepTimer: Boolean,
    val showSongAdditionalInfo: Boolean,
    val enableSeekControls: Boolean,
    val seekBackDuration: Int,
    val seekForwardDuration: Int,
    val controlsLayout: NowPlayingControlsLayout,
)

private data class NowPlayingStates(
    val showLyrics: MutableStateFlow<Boolean>,
)

private object NowPlayingDefaults {
    var showLyrics = false
}

enum class NowPlayingControlsLayout {
    Default,
    Traditional,
}

@Composable
fun NowPlayingView(context: ViewContext) {
    var song by remember { mutableStateOf(context.symphony.radio.queue.currentPlayingSong) }
    var isPlaying by remember { mutableStateOf(context.symphony.radio.isPlaying) }
    var currentSongIndex by remember { mutableStateOf(context.symphony.radio.queue.currentSongIndex) }
    var queueSize by remember { mutableStateOf(context.symphony.radio.queue.originalQueue.size) }
    var currentLoopMode by remember { mutableStateOf(context.symphony.radio.queue.currentLoopMode) }
    var currentShuffleMode by remember { mutableStateOf(context.symphony.radio.queue.currentShuffleMode) }
    var currentSpeed by remember { mutableStateOf(context.symphony.radio.currentSpeed) }
    var currentPitch by remember { mutableStateOf(context.symphony.radio.currentPitch) }
    var persistedSpeed by remember { mutableStateOf(context.symphony.radio.persistedSpeed) }
    var persistedPitch by remember { mutableStateOf(context.symphony.radio.persistedPitch) }
    var hasSleepTimer by remember { mutableStateOf(context.symphony.radio.hasSleepTimer()) }
    var showSongAdditionalInfo by remember {
        mutableStateOf(context.symphony.settings.getNowPlayingAdditionalInfo())
    }
    var enableSeekControls by remember {
        mutableStateOf(context.symphony.settings.getNowPlayingSeekControls())
    }
    var seekBackDuration by remember {
        mutableStateOf(context.symphony.settings.getSeekBackDuration())
    }
    var seekForwardDuration by remember {
        mutableStateOf(context.symphony.settings.getSeekForwardDuration())
    }
    var controlsLayout by remember {
        mutableStateOf(context.symphony.settings.getNowPlayingControlsLayout())
    }
    var isViable by remember { mutableStateOf(song != null) }

    BackHandler {
        context.navController.popBackStack()
    }

    EventerEffect(context.symphony.radio.onUpdate) {
        song = context.symphony.radio.queue.currentPlayingSong
        isPlaying = context.symphony.radio.isPlaying
        currentSongIndex = context.symphony.radio.queue.currentSongIndex
        queueSize = context.symphony.radio.queue.originalQueue.size
        currentLoopMode = context.symphony.radio.queue.currentLoopMode
        currentShuffleMode = context.symphony.radio.queue.currentShuffleMode
        currentSpeed = context.symphony.radio.currentSpeed
        currentPitch = context.symphony.radio.currentPitch
        persistedSpeed = context.symphony.radio.persistedSpeed
        persistedPitch = context.symphony.radio.persistedPitch
        hasSleepTimer = context.symphony.radio.hasSleepTimer()
        isViable = song != null
    }

    EventerEffect(context.symphony.settings.onChange) { key ->
        when (key) {
            SettingsKeys.nowPlayingAdditionalInfo -> {
                showSongAdditionalInfo = context.symphony.settings.getNowPlayingAdditionalInfo()
            }
            SettingsKeys.nowPlayingSeekControls -> {
                enableSeekControls = context.symphony.settings.getNowPlayingSeekControls()
            }
            SettingsKeys.seekBackDuration -> {
                seekBackDuration = context.symphony.settings.getSeekBackDuration()
            }
            SettingsKeys.seekForwardDuration -> {
                seekForwardDuration = context.symphony.settings.getSeekForwardDuration()
            }
            SettingsKeys.nowPlayingControlsLayout -> {
                controlsLayout = context.symphony.settings.getNowPlayingControlsLayout()
            }
            else -> {}
        }
    }

    when {
        isViable -> NowPlayingBody(
            context,
            PlayerStateData(
                song = song!!,
                isPlaying = isPlaying,
                currentSongIndex = currentSongIndex,
                queueSize = queueSize,
                currentLoopMode = currentLoopMode,
                currentShuffleMode = currentShuffleMode,
                currentSpeed = currentSpeed,
                currentPitch = currentPitch,
                persistedSpeed = persistedSpeed,
                persistedPitch = persistedPitch,
                hasSleepTimer = hasSleepTimer,
                showSongAdditionalInfo = showSongAdditionalInfo,
                enableSeekControls = enableSeekControls,
                seekBackDuration = seekBackDuration,
                seekForwardDuration = seekForwardDuration,
                controlsLayout = controlsLayout,
            )
        )
        else -> NothingPlaying(context)
    }
}

private val defaultHorizontalPadding = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingAppBar(context: ViewContext) {
    CenterAlignedTopAppBar(
        title = {
            TopAppBarMinimalTitle {
                Text(context.symphony.t.NowPlaying)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        navigationIcon = {
            IconButton(
                onClick = {
                    context.navController.popBackStack()
                }
            ) {
                Icon(
                    Icons.Default.ExpandMore,
                    null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    )
}

@Composable
fun NowPlayingLandscapeAppBar(context: ViewContext) {
    Row(
        modifier = Modifier.padding(defaultHorizontalPadding, 4.dp, 12.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopAppBarMinimalTitle(
            modifier = Modifier.weight(1f),
            fillMaxWidth = false,
        ) {
            Text(context.symphony.t.NowPlaying)
        }
        IconButton(
            onClick = {
                context.navController.popBackStack()
            }
        ) {
            Icon(
                Icons.Default.ExpandMore,
                null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun NowPlayingBody(context: ViewContext, data: PlayerStateData) {
    val states = remember {
        NowPlayingStates(
            showLyrics = MutableStateFlow(NowPlayingDefaults.showLyrics),
        )
    }

    data.run {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val orientation = ScreenOrientation.fromConstraints(this@BoxWithConstraints)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (orientation.isPortrait) {
                        NowPlayingAppBar(context)
                    }
                },
                content = { contentPadding ->
                    BoxWithConstraints(modifier = Modifier.padding(contentPadding)) {
                        when (orientation) {
                            ScreenOrientation.PORTRAIT -> Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f))
                                NowPlayingBodyCover(context, data, states)
                                Box(modifier = Modifier.weight(1f))
                                Column {
                                    NowPlayingBodyContent(context, data)
                                    NowPlayingBodyBottomBar(context, data, states)
                                }
                            }
                            ScreenOrientation.LANDSCAPE -> Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceAround,
                            ) {
                                Box(modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)) {
                                    NowPlayingBodyCover(context, data, states)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    Column {
                                        NowPlayingLandscapeAppBar(context)
                                        Box(modifier = Modifier.weight(1f))
                                        NowPlayingBodyContent(context, data)
                                        NowPlayingBodyBottomBar(context, data, states)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun NowPlayingBodyCover(
    context: ViewContext,
    data: PlayerStateData,
    states: NowPlayingStates,
) {
    val coroutineScope = rememberCoroutineScope()
    val showLyrics by states.showLyrics.collectAsState()
    val currentSong by rememberUpdatedState(data.song)
    var lyricsState by remember { mutableStateOf(0) }
    var lyricsSongId by remember { mutableStateOf<Long?>(null) }
    var lyrics by remember { mutableStateOf<String?>(null) }

    val fetchLyrics = { check: Boolean ->
        if (check && (lyricsSongId != currentSong.id || lyricsState == 0)) {
            lyricsState = 1
            coroutineScope.launch {
                lyricsSongId = currentSong.id
                lyrics = context.symphony.groove.lyrics.getLyrics(currentSong)
                lyricsState = 2
            }
        }
    }

    LaunchedEffect(LocalContext.current) {
        awaitAll(
            async { snapshotFlow { currentSong }.collect { fetchLyrics(showLyrics) } },
            async { snapshotFlow { showLyrics }.collect { fetchLyrics(it) } },
        )
    }

    data.run {
        BoxWithConstraints(modifier = Modifier.padding(defaultHorizontalPadding, 0.dp)) {
            val dimension = min(maxHeight, maxWidth)

            Box(
                modifier = Modifier
                    .size(dimension)
                    .aspectRatio(1f)
            ) {
                AnimatedContent(
                    modifier = Modifier.matchParentSize(),
                    targetState = showLyrics,
                    transitionSpec = {
                        FadeTransition.enterTransition()
                            .with(FadeTransition.exitTransition())
                    },
                ) { targetStateShowLyrics ->
                    when {
                        targetStateShowLyrics -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(16.dp, 12.dp)
                        ) {
                            ProvideTextStyle(
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            ) {
                                when {
                                    lyricsState == 2 && lyrics != null -> AnimatedContent(
                                        targetState = lyrics ?: "",
                                        transitionSpec = {
                                            FadeTransition.enterTransition()
                                                .with(FadeTransition.exitTransition())
                                        },
                                    ) {
                                        Text(
                                            it,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState()),
                                        )
                                    }
                                    else -> Text(
                                        if (lyricsState == 1) context.symphony.t.Loading
                                        else context.symphony.t.NoLyrics,
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                }
                            }
                        }
                        else -> AnimatedContent(
                            modifier = Modifier.matchParentSize(),
                            targetState = song,
                            transitionSpec = {
                                FadeTransition.enterTransition()
                                    .with(FadeTransition.exitTransition())
                            },
                        ) { targetStateSong ->
                            AsyncImage(
                                targetStateSong
                                    .createArtworkImageRequest(context.symphony)
                                    .build(),
                                null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .noRippleClickable {
                                        context.navController.navigate(
                                            RoutesBuilder.buildAlbumRoute(song.albumId)
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun NowPlayingBodyContent(context: ViewContext, data: PlayerStateData) {
    var isInFavorites by remember(data.song.id) {
        mutableStateOf(context.symphony.groove.playlist.isInFavorites(data.song.id))
    }

    EventerEffect(context.symphony.groove.playlist.onFavoritesUpdate) { favorites ->
        isInFavorites = favorites.contains(data.song.id)
    }

    data.run {
        Column {
            Row {
                AnimatedContent(
                    modifier = Modifier.weight(1f),
                    targetState = song,
                    transitionSpec = {
                        FadeTransition.enterTransition()
                            .with(FadeTransition.exitTransition())
                    },
                ) { targetStateSong ->
                    Column(modifier = Modifier.padding(defaultHorizontalPadding, 0.dp)) {
                        Text(
                            targetStateSong.title,
                            style = MaterialTheme.typography.headlineSmall
                                .copy(fontWeight = FontWeight.Bold),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        targetStateSong.artistName?.let {
                            Text(
                                it,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.noRippleClickable {
                                    context.navController.navigate(
                                        RoutesBuilder.buildArtistRoute(it)
                                    )
                                },
                            )
                        }
                        if (data.showSongAdditionalInfo) {
                            targetStateSong.additional.toSamplingInfoString(context.symphony)?.let {
                                val localContentColor = LocalContentColor.current
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelSmall
                                        .copy(color = localContentColor.copy(alpha = 0.7f)),
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
                Row {
                    IconButton(
                        modifier = Modifier.offset(4.dp),
                        onClick = {
                            context.symphony.groove.playlist.run {
                                when {
                                    isInFavorites -> removeFromFavorites(song.id)
                                    else -> addToFavorites(song.id)
                                }
                            }
                        }
                    ) {
                        when {
                            isInFavorites -> Icon(
                                Icons.Default.Favorite,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            else -> Icon(Icons.Default.FavoriteBorder, null)
                        }
                    }

                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = {
                            showOptionsMenu = !showOptionsMenu
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, null)
                        SongDropdownMenu(
                            context,
                            song,
                            expanded = showOptionsMenu,
                            onDismissRequest = {
                                showOptionsMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(defaultHorizontalPadding + 8.dp))
            when (controlsLayout) {
                NowPlayingControlsLayout.Default -> Row(
                    modifier = Modifier.padding(defaultHorizontalPadding, 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NowPlayingPlayPauseButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Primary,
                        ),
                    )
                    NowPlayingSkipPreviousButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Surface,
                        ),
                    )
                    if (enableSeekControls) {
                        NowPlayingFastRewindButton(
                            context,
                            data = data,
                            style = NowPlayingControlButtonStyle(
                                color = NowPlayingControlButtonColors.Surface,
                            ),
                        )
                        NowPlayingFastForwardButton(
                            context,
                            data = data,
                            style = NowPlayingControlButtonStyle(
                                color = NowPlayingControlButtonColors.Surface,
                            ),
                        )
                    }
                    NowPlayingSkipNextButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Surface,
                        ),
                    )
                }
                NowPlayingControlsLayout.Traditional -> Row(
                    modifier = Modifier
                        .padding(defaultHorizontalPadding, 0.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    NowPlayingSkipPreviousButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Transparent,
                        ),
                    )
                    if (enableSeekControls) {
                        NowPlayingFastRewindButton(
                            context,
                            data = data,
                            style = NowPlayingControlButtonStyle(
                                color = NowPlayingControlButtonColors.Transparent,
                            ),
                        )
                    }
                    NowPlayingPlayPauseButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Surface,
                            size = NowPlayingControlButtonSize.Large,
                        ),
                    )
                    if (enableSeekControls) {
                        NowPlayingFastForwardButton(
                            context,
                            data = data,
                            style = NowPlayingControlButtonStyle(
                                color = NowPlayingControlButtonColors.Transparent,
                            ),
                        )
                    }
                    NowPlayingSkipNextButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Transparent,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(defaultHorizontalPadding))
            Row(
                modifier = Modifier.padding(defaultHorizontalPadding, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                var sliderPosition by remember { mutableStateOf<Int?>(null) }
                var duration by remember {
                    mutableStateOf(
                        context.symphony.radio.currentPlaybackPosition
                            ?: PlaybackPosition.zero
                    )
                }
                EventerEffect(context.symphony.radio.onPlaybackPositionUpdate) {
                    duration = it
                }
                Text(
                    DurationFormatter.formatMs(sliderPosition ?: duration.played),
                    style = MaterialTheme.typography.labelMedium
                )
                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = (sliderPosition ?: duration.played).toFloat(),
                        valueRange = 0f..duration.total.toFloat(),
                        onValueChange = {
                            sliderPosition = it.toInt()
                        },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                context.symphony.radio.seek(it)
                                sliderPosition = null
                            }
                        },
                        interactionSource = interactionSource,
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = interactionSource,
                                thumbSize = DpSize(12.dp, 12.dp),
                                // NOTE: pad top to fix stupid layout
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    )
                }
                Text(
                    DurationFormatter.formatMs(duration.total),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun NowPlayingPlayPauseButton(
    context: ViewContext,
    data: PlayerStateData,
    style: NowPlayingControlButtonStyle
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = when {
                !isPlaying -> Icons.Default.PlayArrow
                else -> Icons.Default.Pause
            },
            onClick = {
                context.symphony.radio.shorty.playPause()
            }
        )
    }
}

@Composable
private fun NowPlayingSkipPreviousButton(
    context: ViewContext,
    data: PlayerStateData,
    style: NowPlayingControlButtonStyle
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = Icons.Default.SkipPrevious,
            onClick = {
                context.symphony.radio.shorty.previous()
            }
        )
    }
}

@Composable
private fun NowPlayingSkipNextButton(
    context: ViewContext,
    data: PlayerStateData,
    style: NowPlayingControlButtonStyle
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = Icons.Default.SkipNext,
            onClick = {
                context.symphony.radio.shorty.skip()
            }
        )
    }
}

@Composable
private fun NowPlayingFastRewindButton(
    context: ViewContext,
    data: PlayerStateData,
    style: NowPlayingControlButtonStyle
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = Icons.Default.FastRewind,
            onClick = {
                context.symphony.radio.shorty
                    .seekFromCurrent(-seekBackDuration)
            }
        )
    }
}

@Composable
private fun NowPlayingFastForwardButton(
    context: ViewContext,
    data: PlayerStateData,
    style: NowPlayingControlButtonStyle
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = Icons.Default.FastForward,
            onClick = {
                context.symphony.radio.shorty
                    .seekFromCurrent(seekForwardDuration)
            }
        )
    }
}

private enum class NowPlayingControlButtonColors {
    Primary,
    Surface,
    Transparent,
}

private enum class NowPlayingControlButtonSize {
    Default,
    Large,
}

private data class NowPlayingControlButtonStyle(
    val color: NowPlayingControlButtonColors,
    val size: NowPlayingControlButtonSize = NowPlayingControlButtonSize.Default,
)

@Composable
private fun NowPlayingControlButton(
    style: NowPlayingControlButtonStyle,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val backgroundColor = when (style.color) {
        NowPlayingControlButtonColors.Primary -> MaterialTheme.colorScheme.primary
        NowPlayingControlButtonColors.Surface -> MaterialTheme.colorScheme.surfaceVariant
        NowPlayingControlButtonColors.Transparent -> Color.Transparent
    }
    val contentColor = when (style.color) {
        NowPlayingControlButtonColors.Primary -> MaterialTheme.colorScheme.onPrimary
        else -> LocalContentColor.current
    }
    val iconSize = when (style.size) {
        NowPlayingControlButtonSize.Default -> 24.dp
        NowPlayingControlButtonSize.Large -> 32.dp
    }

    IconButton(
        modifier = Modifier.background(backgroundColor, CircleShape),
        onClick = onClick,
    ) {
        Icon(
            icon,
            null,
            tint = contentColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingBodyBottomBar(
    context: ViewContext,
    data: PlayerStateData,
    states: NowPlayingStates,
) {
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showPitchDialog by remember { mutableStateOf(false) }
    var showExtraOptions by remember { mutableStateOf(false) }

    data.run {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 4.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    context.navController.navigate(Routes.Queue)
                }
            ) {
                Icon(
                    Icons.Default.Sort,
                    null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    context.symphony.t.PlayingXofY(
                        (currentSongIndex + 1).toString(),
                        queueSize.toString(),
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            states.showLyrics.let { showLyricsState ->
                val showLyrics by showLyricsState.collectAsState()

                IconButton(
                    onClick = {
                        val nShowLyrics = !showLyricsState.value
                        showLyricsState.value = nShowLyrics
                        NowPlayingDefaults.showLyrics = nShowLyrics
                    }
                ) {
                    Icon(
                        Icons.Outlined.Article,
                        null,
                        tint = when {
                            showLyrics -> MaterialTheme.colorScheme.primary
                            else -> LocalContentColor.current
                        }
                    )
                }
            }
            IconButton(
                onClick = {
                    context.symphony.radio.queue.toggleLoopMode()
                }
            ) {
                Icon(
                    when (currentLoopMode) {
                        RadioLoopMode.Song -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    null,
                    tint = when (currentLoopMode) {
                        RadioLoopMode.None -> LocalContentColor.current
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
            IconButton(
                onClick = {
                    context.symphony.radio.queue.toggleShuffleMode()
                }
            ) {
                Icon(
                    Icons.Default.Shuffle,
                    null,
                    tint = if (!currentShuffleMode) LocalContentColor.current
                    else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = {
                    showExtraOptions = !showExtraOptions
                }
            ) {
                Icon(Icons.Outlined.MoreHoriz, null)
            }
        }

        if (showSleepTimerDialog) {
            when {
                hasSleepTimer -> context.symphony.radio.getSleepTimer()?.let {
                    NowPlayingSleepTimerDialog(
                        context,
                        sleepTimer = it,
                        onDismissRequest = {
                            showSleepTimerDialog = false
                        }
                    )
                }
                else -> NowPlayingSleepTimerSetDialog(
                    context,
                    onDismissRequest = {
                        showSleepTimerDialog = false
                    }
                )
            }
        }

        if (showSpeedDialog) {
            NowPlayingSpeedDialog(
                context,
                currentSpeed = data.currentSpeed,
                persistedSpeed = data.persistedSpeed,
                onDismissRequest = {
                    showSpeedDialog = false
                }
            )
        }

        if (showPitchDialog) {
            NowPlayingPitchDialog(
                context,
                currentPitch = data.currentPitch,
                persistedPitch = data.persistedPitch,
                onDismissRequest = {
                    showPitchDialog = false
                }
            )
        }

        if (showExtraOptions) {
            ModalBottomSheet(
                onDismissRequest = {
                    showExtraOptions = false
                }
            ) {
                Card(
                    onClick = {
                        showExtraOptions = false
                        showSleepTimerDialog = !showSleepTimerDialog
                    }
                ) {
                    ListItem(
                        leadingContent = {
                            Icon(
                                Icons.Outlined.Timer,
                                null,
                                tint = when {
                                    hasSleepTimer -> MaterialTheme.colorScheme.primary
                                    else -> LocalContentColor.current
                                }
                            )
                        },
                        headlineContent = {
                            Text(context.symphony.t.SleepTimer)
                        },
                        supportingContent = {
                            Text(
                                if (hasSleepTimer) context.symphony.t.Enabled
                                else context.symphony.t.Disabled
                            )
                        },
                    )
                }
                Card(
                    onClick = {
                        showExtraOptions = false
                        showSpeedDialog = !showSpeedDialog
                    }
                ) {
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Outlined.Speed, null)
                        },
                        headlineContent = {
                            Text(context.symphony.t.Speed)
                        },
                        supportingContent = {
                            Text("x${data.currentSpeed}")
                        },
                    )
                }
                Card(
                    onClick = {
                        showExtraOptions = false
                        showPitchDialog = !showPitchDialog
                    }
                ) {
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Outlined.Speed, null)
                        },
                        headlineContent = {
                            Text(context.symphony.t.Pitch)
                        },
                        supportingContent = {
                            Text("x${data.currentPitch}")
                        },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NowPlayingSleepTimerDialog(
    context: ViewContext,
    sleepTimer: RadioSleepTimer,
    onDismissRequest: () -> Unit,
) {
    var updateTimer by remember { mutableStateOf<Timer?>(null) }
    val endsAtMs by remember { mutableStateOf(sleepTimer.endsAt) }
    var endsIn by remember { mutableStateOf(0L) }

    LaunchedEffect(LocalContext.current) {
        updateTimer = kotlin.concurrent.timer(period = 500L) {
            endsIn = endsAtMs - System.currentTimeMillis()
        }
    }

    DisposableEffect(LocalContext.current) {
        onDispose {
            updateTimer?.cancel()
            updateTimer = null
        }
    }

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.SleepTimer)
        },
        content = {
            Text(
                DurationFormatter.formatMs(endsIn),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 12.dp,
                    ),
            )
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                var quitOnEnd by remember { mutableStateOf(sleepTimer.quitOnEnd) }

                Checkbox(
                    checked = quitOnEnd,
                    onCheckedChange = {
                        quitOnEnd = it
                        sleepTimer.quitOnEnd = it
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    context.symphony.t.QuitAppOnEnd,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    context.symphony.radio.clearSleepTimer()
                    onDismissRequest()
                }
            ) {
                Text(context.symphony.t.Stop)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingSleepTimerSetDialog(
    context: ViewContext,
    onDismissRequest: () -> Unit,
) {
    val minDurationMs = Duration.ofMinutes(1).toMillis()
    val presetDurations = remember {
        listOf(0L to 15L, 0L to 30L, 1L to 0L, 2L to 0L, 3L to 0L)
    }
    var inputHours by remember { mutableStateOf(0L) }
    var inputMinutes by remember { mutableStateOf(10L) }
    var quitOnEnd by remember { mutableStateOf(false) }
    val inputDuration by remember {
        derivedStateOf {
            Duration
                .ofHours(inputHours)
                .plusMinutes(inputMinutes)
                .toMillis()
        }
    }
    val isValidDuration by remember {
        derivedStateOf { inputDuration >= minDurationMs }
    }

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.SleepTimer)
        },
        content = {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        4.dp,
                        Alignment.CenterHorizontally
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    presetDurations.forEach { x ->
                        val hours = x.first
                        val minutes = x.second
                        val shape = RoundedCornerShape(4.dp)

                        Text(
                            DurationFormatter.formatMinSec(hours, minutes),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape,
                                )
                                .clip(shape)
                                .clickable {
                                    inputHours = hours
                                    inputMinutes = minutes
                                }
                                .padding(8.dp, 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.padding(20.dp, 0.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = DividerDefaults.color,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = {
                            Text(context.symphony.t.Hours)
                        },
                        value = inputHours.toString(),
                        onValueChange = {
                            inputHours = it.toLongOrNull() ?: 0
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = DividerDefaults.color,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = {
                            Text(context.symphony.t.Minutes)
                        },
                        value = inputMinutes.toString(),
                        onValueChange = {
                            inputMinutes = it.toLongOrNull() ?: 0
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp, 0.dp),
                ) {
                    Checkbox(
                        checked = quitOnEnd,
                        onCheckedChange = {
                            quitOnEnd = it
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(context.symphony.t.QuitAppOnEnd)
                }
            }
        },
        actions = {
            TextButton(onClick = onDismissRequest) {
                Text(context.symphony.t.Cancel)
            }
            TextButton(
                enabled = isValidDuration,
                onClick = {
                    context.symphony.radio.setSleepTimer(
                        duration = inputDuration,
                        quitOnEnd = quitOnEnd,
                    )
                    onDismissRequest()
                }
            ) {
                Text(context.symphony.t.Done)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingSpeedDialog(
    context: ViewContext,
    currentSpeed: Float,
    persistedSpeed: Float,
    onDismissRequest: () -> Unit,
) {
    val allowedSpeeds = listOf(0.5f, 1f, 1.5f, 2f, 3f)
    var isPersistent by remember {
        mutableStateOf(currentSpeed == persistedSpeed)
    }

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.Speed)
        },
        content = {
            Column(modifier = Modifier.padding(0.dp, 8.dp)) {
                allowedSpeeds.map { speed ->
                    val onClick = {
                        onDismissRequest()
                        context.symphony.radio.setSpeed(speed, isPersistent)
                    }

                    Card(
                        colors = SettingsTileDefaults.cardColors(),
                        shape = MaterialTheme.shapes.small,
                        onClick = onClick,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = currentSpeed == speed,
                                onClick = onClick,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("x${speed}")
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(12.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isPersistent,
                        onCheckedChange = {
                            isPersistent = !isPersistent
                            context.symphony.radio.setSpeed(currentSpeed, isPersistent)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(context.symphony.t.PersistUntilQueueEnd)
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingPitchDialog(
    context: ViewContext,
    currentPitch: Float,
    persistedPitch: Float,
    onDismissRequest: () -> Unit,
) {
    val allowedPitches = listOf(0.5f, 1f, 1.5f, 2f, 3f)
    var isPersistent by remember {
        mutableStateOf(currentPitch == persistedPitch)
    }

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.Pitch)
        },
        content = {
            Column(modifier = Modifier.padding(0.dp, 8.dp)) {
                allowedPitches.map { pitch ->
                    val onClick = {
                        onDismissRequest()
                        context.symphony.radio.setPitch(pitch, isPersistent)
                    }

                    Card(
                        colors = SettingsTileDefaults.cardColors(),
                        shape = MaterialTheme.shapes.small,
                        onClick = onClick,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = currentPitch == pitch,
                                onClick = onClick,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("x${pitch}")
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(12.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isPersistent,
                        onCheckedChange = {
                            isPersistent = !isPersistent
                            context.symphony.radio.setPitch(currentPitch, isPersistent)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(context.symphony.t.PersistUntilQueueEnd)
                }
            }
        },
    )
}
