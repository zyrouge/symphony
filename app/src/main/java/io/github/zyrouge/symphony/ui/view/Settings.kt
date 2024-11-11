package io.github.zyrouge.symphony.ui.view

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Update
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.ui.components.AdaptiveSnackbar
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.components.settings.SettingsSimpleTile
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.settings.AppearanceSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.GrooveSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.HomePageSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.MiniPlayerSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.NowPlayingSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.PlayerSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.UpdateSettingsViewRoute
import io.github.zyrouge.symphony.utils.ActivityUtils
import kotlinx.serialization.Serializable

@Serializable
data class SettingsViewRoute(val initialElement: String? = null) {
    companion object {
        const val ELEMENT_MEDIA_FOLDERS = "media_folders"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(context: ViewContext, route: SettingsViewRoute) {
    val configuration = LocalConfiguration.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size((configuration.smallestScreenWidthDp * 0.25).dp)) {
                            AsyncImage(R.drawable.ic_launcher_foreground, null)
                        }
                        Column {
                            Text(AppMeta.appName, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(AppMeta.version, style = MaterialTheme.typography.labelMedium)
                            AppMeta.latestVersion?.takeIf { AppMeta.version != it }?.let {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    context.symphony.t.NewVersionAvailableX(it),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        }
                    }
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.LibraryMusic, null)
                        },
                        title = {
                            Text(context.symphony.t.Groove)
                        },
                        onClick = {
                            context.navController.navigate(
                                GrooveSettingsViewRoute(route.initialElement)
                            )
                        },
                    )
                    HorizontalDivider()
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.Radio, null)
                        },
                        title = {
                            Text(context.symphony.t.Player)
                        },
                        onClick = {
                            context.navController.navigate(PlayerSettingsViewRoute)
                        },
                    )
                    HorizontalDivider()
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.Palette, null)
                        },
                        title = {
                            Text(context.symphony.t.Appearance)
                        },
                        onClick = {
                            context.navController.navigate(AppearanceSettingsViewRoute)
                        },
                    )
                    HorizontalDivider()
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.Home, null)
                        },
                        title = {
                            Text(context.symphony.t.Home)
                        },
                        onClick = {
                            context.navController.navigate(HomePageSettingsViewRoute)
                        },
                    )
                    HorizontalDivider()
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.MusicNote, null)
                        },
                        title = {
                            Text(context.symphony.t.MiniPlayer)
                        },
                        onClick = {
                            context.navController.navigate(MiniPlayerSettingsViewRoute)
                        },
                    )
                    HorizontalDivider()
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.MusicNote, null)
                        },
                        title = {
                            Text(context.symphony.t.NowPlaying)
                        },
                        onClick = {
                            context.navController.navigate(NowPlayingSettingsViewRoute)
                        },
                    )
                    HorizontalDivider()
                    SettingsSimpleTile(
                        icon = {
                            Icon(Icons.Filled.Update, null)
                        },
                        title = {
                            Text(context.symphony.t.Updates)
                        },
                        onClick = {
                            context.navController.navigate(UpdateSettingsViewRoute)
                        },
                    )
                }
            }
        }
    )
}
