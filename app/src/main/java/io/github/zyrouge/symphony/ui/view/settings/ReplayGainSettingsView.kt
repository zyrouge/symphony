package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.zyrouge.symphony.services.replayGain.ReplayGainNormalizationMode
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.components.settings.InfoTile
import io.github.zyrouge.symphony.ui.components.settings.PrimarySectionToggle
import io.github.zyrouge.symphony.ui.components.settings.SettingsOptionTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsSliderTile
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data object ReplayGainSettingsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplayGainSettingsView(context: ViewContext) {
    val replayGainEnabled by context.symphony.settings.replayGainEnabled.flow.collectAsState()
    val replayGainNormalizationMode by context.symphony.settings.replayGainNormalizationMode.flow.collectAsState()
    val replayGainPreAmp by context.symphony.settings.replayGainPreAmp.flow.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TopAppBarMinimalTitle {
                        Text("${context.symphony.t.Settings} - ReplayGain")
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
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButtonPlaceholder()
                },
            )
        },
        content = { paddingValues ->
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.padding(paddingValues).verticalScroll(scrollState)) {
                PrimarySectionToggle(replayGainEnabled, {
                    context.symphony.settings.replayGainEnabled.setValue(it)
                }) { Text("Use ReplayGain") }
                InfoTile {
                    Text("ReplayGain is a technique to normalize the volume of songs. It is supported for songs, that contain ReplayGain metadata.")
                }
                HorizontalDivider()
                SettingsOptionTile(
                    icon = { Icon(Icons.Default.Headset, null) },
                    title = { Text("Normalization mode") },
                    value = replayGainNormalizationMode,
                    values = mapOf(
                        ReplayGainNormalizationMode.AUTOMATIC to "Automatic",
                        ReplayGainNormalizationMode.ALBUM to "Preserve Album dynamics",
                        ReplayGainNormalizationMode.TRACK to "All songs equally loud",
                    ),
                    captions = mapOf(
                        ReplayGainNormalizationMode.AUTOMATIC to "Use album gain only when playing albums",
                        ReplayGainNormalizationMode.ALBUM to "Always use album gain",
                        ReplayGainNormalizationMode.TRACK to "Always use song gain",
                    ),
                    enabled = replayGainEnabled,
                    onChange = { context.symphony.settings.replayGainNormalizationMode.setValue(it) }
                )
                SettingsSliderTile(
                    context,
                    icon = {
                        Icon(Icons.AutoMirrored.Default.VolumeUp, null)
                    },
                    title = {
                        Text("Pre Amplify")
                    },
                    label = { value ->
                        Text(value.toString() + "dB")
                    },
                    range = -15F..15F,
                    initialValue = replayGainPreAmp,
                    enabled = replayGainEnabled,
                    onValue = { value -> (value * 10).roundToInt().toFloat().div(10) },
                    onChange = { value ->
                        context.symphony.settings.replayGainPreAmp.setValue(value)
                    },
                    onReset = {
                        context.symphony.settings.replayGainPreAmp.setValue(
                            context.symphony.settings.replayGainPreAmp.defaultValue,
                        )
                    },
                )
            }
        }
    )
}
