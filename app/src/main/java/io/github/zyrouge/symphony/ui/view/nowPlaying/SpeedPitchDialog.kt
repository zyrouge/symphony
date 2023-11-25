package io.github.zyrouge.symphony.ui.view.nowPlaying

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.settings.SettingsTileDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSpeedDialog(
    context: ViewContext,
    currentSpeed: Float,
    persistedSpeed: Float,
    onDismissRequest: () -> Unit,
) {
    val allowedSpeeds = listOf(0.5f, 1f, 1.5f, 2f, 3f)
    var persistent by remember {
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
                        context.symphony.radio.setSpeed(speed, persistent)
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
                        checked = persistent,
                        onCheckedChange = {
                            persistent = !persistent
                            context.symphony.radio.setSpeed(currentSpeed, persistent)
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
fun NowPlayingPitchDialog(
    context: ViewContext,
    currentPitch: Float,
    persistedPitch: Float,
    onDismissRequest: () -> Unit,
) {
    val allowedPitches = listOf(0.5f, 1f, 1.5f, 2f, 3f)
    var persistent by remember {
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
                        context.symphony.radio.setPitch(pitch, persistent)
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
                        checked = persistent,
                        onCheckedChange = {
                            persistent = !persistent
                            context.symphony.radio.setPitch(currentPitch, persistent)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(context.symphony.t.PersistUntilQueueEnd)
                }
            }
        },
    )
}
