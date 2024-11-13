package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Recommend
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
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.components.settings.ConsiderContributingTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsMultiOptionTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsOptionTile
import io.github.zyrouge.symphony.ui.components.settings.SettingsSideHeading
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.HomePageBottomBarLabelVisibility
import io.github.zyrouge.symphony.ui.view.HomePages
import io.github.zyrouge.symphony.ui.view.home.ForYou
import kotlinx.serialization.Serializable

@Serializable
object HomePageSettingsViewRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageSettingsView(context: ViewContext) {
    val scrollState = rememberScrollState()
    val homeTabs by context.symphony.settings.homeTabs.flow.collectAsState()
    val forYouContents by context.symphony.settings.forYouContents.flow.collectAsState()
    val homePageBottomBarLabelVisibility by context.symphony.settings.homePageBottomBarLabelVisibility.flow.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TopAppBarMinimalTitle {
                        Text("${context.symphony.t.Settings} - ${context.symphony.t.Home}")
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
                    ConsiderContributingTile(context)
                    SettingsSideHeading(context.symphony.t.Home)
                    SettingsMultiOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.Home, null)
                        },
                        title = {
                            Text(context.symphony.t.HomeTabs)
                        },
                        note = {
                            Text(context.symphony.t.SelectAtleast2orAtmost5Tabs)
                        },
                        value = homeTabs,
                        values = HomePages.entries.associateWith { it.label(context) },
                        satisfies = { it.size in 2..5 },
                        onChange = { value ->
                            context.symphony.settings.homeTabs.setValue(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsMultiOptionTile(
                        context,
                        icon = {
                            Icon(Icons.Filled.Recommend, null)
                        },
                        title = {
                            Text(context.symphony.t.ForYou)
                        },
                        value = forYouContents,
                        values = ForYou.entries.associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.forYouContents.setValue(value)
                        }
                    )
                    HorizontalDivider()
                    SettingsOptionTile(
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.Label, null)
                        },
                        title = {
                            Text(context.symphony.t.BottomBarLabelVisibility)
                        },
                        value = homePageBottomBarLabelVisibility,
                        values = HomePageBottomBarLabelVisibility.entries
                            .associateWith { it.label(context) },
                        onChange = { value ->
                            context.symphony.settings.homePageBottomBarLabelVisibility.setValue(
                                value,
                            )
                        }
                    )
                }
            }
        }
    )
}

fun HomePageBottomBarLabelVisibility.label(context: ViewContext) = when (this) {
    HomePageBottomBarLabelVisibility.ALWAYS_VISIBLE -> context.symphony.t.AlwaysVisible
    HomePageBottomBarLabelVisibility.VISIBLE_WHEN_ACTIVE -> context.symphony.t.VisibleWhenActive
    HomePageBottomBarLabelVisibility.INVISIBLE -> context.symphony.t.Invisible
}