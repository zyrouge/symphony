package io.github.zyrouge.symphony.ui.view.settings

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.ui.components.AdaptiveSnackbar
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.components.settings.SettingsSideHeading
import io.github.zyrouge.symphony.ui.components.settings.SettingsSliderTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsSwitchTile
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.ActivityUtils
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
object PlayerSettingsViewRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsView(context: ViewContext) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val fadePlayback by context.symphony.settings.fadePlayback.flow.collectAsState()
    val fadePlaybackDuration by context.symphony.settings.fadePlaybackDuration.flow.collectAsState()
    val requireAudioFocus by context.symphony.settings.requireAudioFocus.flow.collectAsState()
    val ignoreAudioFocusLoss by context.symphony.settings.ignoreAudioFocusLoss.flow.collectAsState()
    val playOnHeadphonesConnect by context.symphony.settings.playOnHeadphonesConnect.flow.collectAsState()
    val pauseOnHeadphonesDisconnect by context.symphony.settings.pauseOnHeadphonesDisconnect.flow.collectAsState()
    val seekBackDuration by context.symphony.settings.seekBackDuration.flow.collectAsState()
    val seekForwardDuration by context.symphony.settings.seekForwardDuration.flow.collectAsState()

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
                    val seekDurationRange = 3f..60f

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
                    SettingsSideHeading(context.symphony.t.Player)
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.GraphicEq, null)
                        },
                        title = {
                            Text(context.symphony.t.FadePlaybackInOut)
                        },
                        value = fadePlayback,
                        onChange = { value ->
                            context.symphony.settings.fadePlayback.setValue(value)
                        }
                    )
                    SettingsSliderTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.GraphicEq, null)
                        },
                        title = {
                            Text(context.symphony.t.FadePlaybackInOut)
                        },
                        label = { value ->
                            Text(context.symphony.t.XSecs(value.toString()))
                        },
                        range = 0.5f..6f,
                        initialValue = fadePlaybackDuration,
                        onValue = { value ->
                            value.times(2).roundToInt().toFloat().div(2)
                        },
                        onChange = { value ->
                            context.symphony.settings.fadePlaybackDuration.setValue(value)
                        },
                        onReset = {
                            context.symphony.settings.fadePlaybackDuration.setValue(
                                context.symphony.settings.fadePlaybackDuration.defaultValue,
                            )
                        },
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.CenterFocusWeak, null)
                        },
                        title = {
                            Text(context.symphony.t.RequireAudioFocus)
                        },
                        value = requireAudioFocus,
                        onChange = { value ->
                            context.symphony.settings.requireAudioFocus.setValue(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.CenterFocusWeak, null)
                        },
                        title = {
                            Text(context.symphony.t.IgnoreAudioFocusLoss)
                        },
                        value = ignoreAudioFocusLoss,
                        onChange = { value ->
                            context.symphony.settings.ignoreAudioFocusLoss.setValue(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.Headset, null)
                        },
                        title = {
                            Text(context.symphony.t.PlayOnHeadphonesConnect)
                        },
                        value = playOnHeadphonesConnect,
                        onChange = { value ->
                            context.symphony.settings.playOnHeadphonesConnect.setValue(value)
                        }
                    )
                    SettingsSwitchTile(
                        icon = {
                            Icon(Icons.Filled.HeadsetOff, null)
                        },
                        title = {
                            Text(context.symphony.t.PauseOnHeadphonesDisconnect)
                        },
                        value = pauseOnHeadphonesDisconnect,
                        onChange = { value ->
                            context.symphony.settings.pauseOnHeadphonesDisconnect.setValue(value)
                        }
                    )
                    SettingsSliderTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.FastRewind, null)
                        },
                        title = {
                            Text(context.symphony.t.FastRewindDuration)
                        },
                        label = { value ->
                            Text(context.symphony.t.XSecs(value.toString()))
                        },
                        range = seekDurationRange,
                        initialValue = seekBackDuration.toFloat(),
                        onValue = { value ->
                            value.roundToInt().toFloat()
                        },
                        onChange = { value ->
                            context.symphony.settings.seekBackDuration.setValue(value.toInt())
                        },
                        onReset = {
                            context.symphony.settings.seekBackDuration.setValue(
                                context.symphony.settings.seekBackDuration.defaultValue,
                            )
                        },
                    )
                    SettingsSliderTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.FastForward, null)
                        },
                        title = {
                            Text(context.symphony.t.FastForwardDuration)
                        },
                        label = { value ->
                            Text(context.symphony.t.XSecs(value.toString()))
                        },
                        range = seekDurationRange,
                        initialValue = seekForwardDuration.toFloat(),
                        onValue = { value ->
                            value.roundToInt().toFloat()
                        },
                        onChange = { value ->
                            context.symphony.settings.seekForwardDuration.setValue(value.toInt())
                        },
                        onReset = {
                            context.symphony.settings.seekForwardDuration.setValue(
                                context.symphony.settings.seekForwardDuration.defaultValue,
                            )
                        },
                    )
                }
            }
        }
    )
}
