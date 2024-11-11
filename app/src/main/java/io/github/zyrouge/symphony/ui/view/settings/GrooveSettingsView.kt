package io.github.zyrouge.symphony.ui.view.settings

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.RuleFolder
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.ui.components.AdaptiveSnackbar
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.components.settings.SettingsMultiGrooveFolderTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsMultiSystemFolderTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsMultiTextOptionTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsSideHeading
import io.github.zyrouge.symphony.ui.components.settings.SettingsSimpleTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsTextInputTile
import io.github.zyrouge.symphony.ui.helpers.TransitionDurations
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.SettingsViewElements
import io.github.zyrouge.symphony.utils.ActivityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class GrooveSettingsViewRoute(val initialElement: SettingsViewElements?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrooveSettingsView(context: ViewContext, route: GrooveSettingsViewRoute) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val songsFilterPattern by context.symphony.settings.songsFilterPattern.flow.collectAsState()
    val blacklistFolders by context.symphony.settings.blacklistFolders.flow.collectAsState()
    val whitelistFolders by context.symphony.settings.whitelistFolders.flow.collectAsState()
    val artistTagSeparators by context.symphony.settings.artistTagSeparators.flow.collectAsState()
    val genreTagSeparators by context.symphony.settings.genreTagSeparators.flow.collectAsState()
    val mediaFolders by context.symphony.settings.mediaFolders.flow.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                AdaptiveSnackbar(it)
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TopAppBarMinimalTitle {
                        Text(context.symphony.t.Settings)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButtonPlaceholder()
                },
            )
        },
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    val contentColor = MaterialTheme.colorScheme.onPrimary
                    val defaultSongsFilterPattern = ".*"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                ActivityUtils.startBrowserActivity(
                                    context.activity,
                                    Uri.parse(AppMeta.contributingUrl)
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                null,
                                tint = contentColor,
                                modifier = Modifier.size(12.dp),
                            )
                            Box(modifier = Modifier.width(4.dp))
                            Text(
                                context.symphony.t.ConsiderContributing,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                ),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(8.dp, 0.dp)
                        ) {
                            Icon(
                                Icons.Filled.East,
                                null,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    SettingsSideHeading(context.symphony.t.Groove)
                    SpotlightTile(route.initialElement == SettingsViewElements.MediaFolders) {
                        SettingsMultiSystemFolderTile(
                            context,
                            icon = {
                                Icon(Icons.Filled.LibraryMusic, null)
                            },
                            title = {
                                Text(context.symphony.t.MediaFolders)
                            },
                            initialValues = mediaFolders,
                            onChange = { values ->
                                context.symphony.settings.mediaFolders.setValue(values)
                                refetchMediaLibrary(coroutineScope, context.symphony)
                            }
                        )
                    }
                    HorizontalDivider()
                    SettingsTextInputTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.FilterAlt, null)
                        },
                        title = {
                            Text(context.symphony.t.SongsFilterPattern)
                        },
                        value = songsFilterPattern ?: defaultSongsFilterPattern,
                        onReset = {
                            context.symphony.settings.songsFilterPattern.setValue(null)
                        },
                        onChange = { value ->
                            context.symphony.settings.songsFilterPattern.setValue(
                                when (value) {
                                    defaultSongsFilterPattern -> null
                                    else -> value
                                }
                            )
                            refetchMediaLibrary(coroutineScope, context.symphony)
                        }
                    )
                    HorizontalDivider()
                    SettingsMultiGrooveFolderTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.RuleFolder, null)
                        },
                        title = {
                            Text(context.symphony.t.BlacklistFolders)
                        },
                        explorer = context.symphony.groove.exposer.explorer,
                        initialValues = blacklistFolders,
                        onChange = { values ->
                            context.symphony.settings.blacklistFolders.setValue(values)
                            refetchMediaLibrary(coroutineScope, context.symphony)
                        }
                    )
                    HorizontalDivider()
                    SettingsMultiGrooveFolderTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.RuleFolder, null)
                        },
                        title = {
                            Text(context.symphony.t.WhitelistFolders)
                        },
                        explorer = context.symphony.groove.exposer.explorer,
                        initialValues = whitelistFolders,
                        onChange = { values ->
                            context.symphony.settings.whitelistFolders.setValue(values)
                            refetchMediaLibrary(coroutineScope, context.symphony)
                        }
                    )
                    HorizontalDivider()
                    SettingsMultiTextOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.SpaceBar, null)
                        },
                        title = {
                            Text(context.symphony.t.ArtistTagValueSeparators)
                        },
                        values = artistTagSeparators.toList(),
                        onChange = {
                            context.symphony.settings.artistTagSeparators.setValue(it.toSet())
                            refetchMediaLibrary(coroutineScope, context.symphony)
                        },
                    )
                    HorizontalDivider()
                    SettingsMultiTextOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.SpaceBar, null)
                        },
                        title = {
                            Text(context.symphony.t.GenreTagValueSeparators)
                        },
                        values = genreTagSeparators.toList(),
                        onChange = {
                            context.symphony.settings.genreTagSeparators.setValue(it.toSet())
                            refetchMediaLibrary(coroutineScope, context.symphony)
                        },
                    )
                    HorizontalDivider()
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.Storage, null)
                        },
                        title = {
                            Text(context.symphony.t.ClearSongCache)
                        },
                        onClick = {
                            coroutineScope.launch {
                                context.symphony.database.songCache.clear()
                                context.symphony.database.artworkCache.clear()
                                context.symphony.database.lyricsCache.clear()
                                refetchMediaLibrary(coroutineScope, context.symphony)
                                snackbarHostState.showSnackbar(
                                    context.symphony.t.SongCacheCleared,
                                    withDismissAction = true,
                                )
                            }
                        }
                    )
                }
            }
        }
    )
}

private fun refetchMediaLibrary(coroutineScope: CoroutineScope, symphony: Symphony) {
    symphony.radio.stop()
    coroutineScope.launch {
        symphony.groove.refetch()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpotlightTile(isInSpotlight: Boolean, content: @Composable (() -> Unit)) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var animateSpotlightEffect by remember { mutableStateOf(false) }
    val highlightAlphaAnimated by animateFloatAsState(
        targetValue = if (animateSpotlightEffect) 0.25f else 0f,
        label = "spotlight-outline-alpha",
        animationSpec = repeatable(2, TransitionDurations.Slow.asTween()),
    )
    val highlightColor = MaterialTheme.colorScheme.surfaceTint

    LaunchedEffect(isInSpotlight) {
        if (isInSpotlight) {
            animateSpotlightEffect = true
            bringIntoViewRequester.bringIntoView()
            animateSpotlightEffect = false
        }
    }

    Box(
        modifier = Modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .drawWithContent {
                drawContent()
                drawRect(color = highlightColor, alpha = highlightAlphaAnimated)
            }
    ) {
        content()
    }
}
