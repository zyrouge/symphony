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
import io.github.zyrouge.symphony.utils.DurationFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*

private data class PlayerStateData(
    val song: Song,
    val isPlaying: Boolean,
    val currentSongIndex: Int,
    val queueSize: Int,
    val currentLoopMode: RadioLoopMode,
    val currentShuffleMode: Boolean,
    val hasSleepTimer: Boolean,
    val showSongAdditionalInfo: Boolean,
    val enableSeekControls: Boolean,
    val seekBackDuration: Int,
    val seekForwardDuration: Int,
)

private data class NowPlayingStates(
    val showLyrics: MutableStateFlow<Boolean>,
)

@Composable
fun NowPlayingView(context: ViewContext) {
    var song by remember { mutableStateOf(context.symphony.radio.queue.currentPlayingSong) }
    var isPlaying by remember { mutableStateOf(context.symphony.radio.isPlaying) }
    var currentSongIndex by remember { mutableStateOf(context.symphony.radio.queue.currentSongIndex) }
    var queueSize by remember { mutableStateOf(context.symphony.radio.queue.originalQueue.size) }
    var currentLoopMode by remember { mutableStateOf(context.symphony.radio.queue.currentLoopMode) }
    var currentShuffleMode by remember { mutableStateOf(context.symphony.radio.queue.currentShuffleMode) }
    var hasSleepTimer by remember { mutableStateOf(context.symphony.radio.hasSleepTimer()) }
    var showSongAdditionalInfo by remember {
        mutableStateOf(context.symphony.settings.getShowNowPlayingAdditionalInfo())
    }
    var enableSeekControls by remember {
        mutableStateOf(context.symphony.settings.getEnableSeekControls())
    }
    var seekBackDuration by remember {
        mutableStateOf(context.symphony.settings.getSeekBackDuration())
    }
    var seekForwardDuration by remember {
        mutableStateOf(context.symphony.settings.getSeekForwardDuration())
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
        hasSleepTimer = context.symphony.radio.hasSleepTimer()
        isViable = song != null
    }

    EventerEffect(context.symphony.settings.onChange) { key ->
        when (key) {
            SettingsKeys.showNowPlayingAdditionalInfo -> {
                showSongAdditionalInfo = context.symphony.settings.getShowNowPlayingAdditionalInfo()
            }
            SettingsKeys.enableSeekControls -> {
                enableSeekControls = context.symphony.settings.getEnableSeekControls()
            }
            SettingsKeys.seekBackDuration -> {
                seekBackDuration = context.symphony.settings.getSeekBackDuration()
            }
            SettingsKeys.seekForwardDuration -> {
                seekForwardDuration = context.symphony.settings.getSeekForwardDuration()
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
                hasSleepTimer = hasSleepTimer,
                showSongAdditionalInfo = showSongAdditionalInfo,
                enableSeekControls = enableSeekControls,
                seekBackDuration = seekBackDuration,
                seekForwardDuration = seekForwardDuration,
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
            showLyrics = MutableStateFlow(false),
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
                            ScreenOrientation.LANDSCAPE -> Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 12.dp, bottom = 20.dp)
                                ) {
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
    var lyricsState by remember { mutableStateOf(0) }
    var lyrics by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(LocalContext.current) {
        snapshotFlow { showLyrics }.collect {
            if (it && lyricsState == 0) {
                lyricsState = 1
                coroutineScope.launch {
                    lyrics = context.symphony.groove.song.getLyrics(data.song)
                    lyricsState = 2
                }
            }
        }
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
                    targetState = showLyrics,
                    modifier = Modifier.matchParentSize(),
                    transitionSpec = { fadeIn() with fadeOut() },
                ) {
                    when {
                        it -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(16.dp, 12.dp)
                        ) {
                            Text(
                                lyrics ?: "",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                        else -> AsyncImage(
                            song.createArtworkImageRequest(context.symphony).build(),
                            null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                Column(
                    modifier = Modifier
                        .padding(defaultHorizontalPadding, 0.dp)
                        .weight(1f)
                ) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.headlineSmall
                            .copy(fontWeight = FontWeight.Bold),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    song.artistName?.let {
                        Text(
                            it,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (data.showSongAdditionalInfo) {
                        song.additional.toSamplingInfoString(context.symphony)?.let {
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
            Row(
                modifier = Modifier.padding(defaultHorizontalPadding, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NowPlayingControlButton(
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    color = MaterialTheme.colorScheme.onPrimary,
                    icon = when {
                        !isPlaying -> Icons.Default.PlayArrow
                        else -> Icons.Default.Pause
                    },
                    onClick = {
                        context.symphony.radio.shorty.playPause()
                    }
                )
                NowPlayingControlButton(
                    icon = Icons.Default.SkipPrevious,
                    onClick = {
                        context.symphony.radio.shorty.previous()
                    }
                )
                if (enableSeekControls) {
                    NowPlayingControlButton(
                        icon = Icons.Default.FastRewind,
                        onClick = {
                            context.symphony.radio.shorty
                                .seekFromCurrent(-seekBackDuration)
                        }
                    )
                    NowPlayingControlButton(
                        icon = Icons.Default.FastForward,
                        onClick = {
                            context.symphony.radio.shorty
                                .seekFromCurrent(seekForwardDuration)
                        }
                    )
                }
                NowPlayingControlButton(
                    icon = Icons.Default.SkipNext,
                    onClick = {
                        context.symphony.radio.shorty.skip()
                    }
                )
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
private fun NowPlayingControlButton(
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    color: Color = LocalContentColor.current,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.background(
            backgroundColor,
            CircleShape
        ),
        onClick = onClick,
    ) {
        Icon(icon, null, tint = color)
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
                        showLyricsState.value = !showLyricsState.value
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
