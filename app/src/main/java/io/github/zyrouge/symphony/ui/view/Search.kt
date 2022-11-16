package io.github.zyrouge.symphony.ui.view

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.components.AlbumDropdownMenu
import io.github.zyrouge.symphony.ui.components.ArtistDropdownMenu
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.SongCard
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(context: ViewContext) {
    val scope = rememberCoroutineScope()
    var terms by rememberSaveable { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val songs = remember { mutableStateListOf<Song>() }
    val artists = remember { mutableStateListOf<Artist>() }
    val albums = remember { mutableStateListOf<Album>() }
    var selectedChip by remember { mutableStateOf<GrooveKinds?>(null) }

    var currentTermsRoutine: Job? = null
    fun setTerms(nTerms: String) {
        terms = nTerms
        isSearching = true
        currentTermsRoutine?.cancel()
        currentTermsRoutine = scope.launch {
            withContext(Dispatchers.Default) {
                delay(250)
                songs.clear()
                artists.clear()
                albums.clear()
                if (nTerms.isNotEmpty()) {
                    songs.addAll(context.symphony.groove.song.search(terms).map { it.entity })
                    artists.addAll(context.symphony.groove.artist.search(terms).map { it.entity })
                    albums.addAll(context.symphony.groove.album.search(terms).map { it.entity })
                }
                isSearching = false
            }
        }
    }

    fun isChipSelected(kind: GrooveKinds) = selectedChip == null || selectedChip == kind

    val textFieldFocusRequester = FocusRequester()
    val configuration = LocalConfiguration.current
    LaunchedEffect(LocalContext.current) {
        textFieldFocusRequester.requestFocus()
        snapshotFlow { configuration.orientation }.collect {
            setTerms(terms)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(textFieldFocusRequester),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true,
                    value = terms,
                    onValueChange = { setTerms(it) },
                    placeholder = {
                        Text(context.symphony.t.searchYourMusic)
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = {
                                context.navController.popBackStack()
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    },
                    trailingIcon = {
                        if (terms.isNotEmpty()) {
                            IconButton(
                                onClick = { setTerms("") }
                            ) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(12.dp, 0.dp)
                ) {
                    FilterChip(
                        selected = selectedChip == null,
                        label = {
                            Text(context.symphony.t.all)
                        },
                        onClick = {
                            selectedChip = null
                        }
                    )
                    GrooveKinds.values().map {
                        FilterChip(
                            selected = selectedChip == it,
                            label = {
                                Text(it.label(context))
                            },
                            onClick = {
                                selectedChip = it
                            }
                        )
                    }
                }
            }
        },
        content = { contentPadding ->
            val hasSongs = isChipSelected(GrooveKinds.SONG) && songs.isNotEmpty()
            val hasArtists = isChipSelected(GrooveKinds.ARTIST) && artists.isNotEmpty()
            val hasAlbums = isChipSelected(GrooveKinds.ALBUM) && albums.isNotEmpty()

            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
            ) {
                if (terms.isNotEmpty()) {
                    when {
                        isSearching -> {
                            Box(modifier = Modifier.align(Alignment.Center)) {
                                IconTextBody(
                                    icon = { modifier ->
                                        Icon(
                                            Icons.Default.Search,
                                            null,
                                            modifier = modifier
                                        )
                                    },
                                    content = {
                                        Text(context.symphony.t.filteringResults)
                                    }
                                )
                            }
                        }
                        !hasSongs && !hasArtists && !hasAlbums -> {
                            Box(modifier = Modifier.align(Alignment.Center)) {
                                IconTextBody(
                                    icon = { modifier ->
                                        Icon(
                                            Icons.Default.PriorityHigh,
                                            null,
                                            modifier = modifier
                                        )
                                    },
                                    content = {
                                        Text(context.symphony.t.noResultsFound)
                                    }
                                )
                            }
                        }
                        else -> {
                            Column {
                                if (hasSongs) {
                                    SideHeading(context.symphony.t.songs)
                                    songs.forEach { song ->
                                        SongCard(context, song) {
                                            context.symphony.radio.shorty.playQueue(song)
                                        }
                                    }
                                }
                                if (hasArtists) {
                                    SideHeading(context.symphony.t.artists)
                                    artists.forEach { artist ->
                                        GenericGrooveCard(
                                            image = { artist.getArtworkUri(context.symphony) },
                                            title = { Text(artist.artistName) },
                                            options = { expanded, onDismissRequest ->
                                                ArtistDropdownMenu(
                                                    context,
                                                    artist,
                                                    expanded = expanded,
                                                    onDismissRequest = onDismissRequest,
                                                )
                                            },
                                            onClick = {
                                                context.navController.navigate(
                                                    RoutesBuilder.buildArtistRoute(artist.artistName)
                                                )
                                            }
                                        )
                                    }
                                }
                                if (hasAlbums) {
                                    SideHeading(context.symphony.t.albums)
                                    albums.forEach { album ->
                                        GenericGrooveCard(
                                            image = { album.getArtworkUri(context.symphony) },
                                            title = { Text(album.albumName) },
                                            subtitle = album.artistName?.let { { Text(it) } },
                                            options = { expanded, onDismissRequest ->
                                                AlbumDropdownMenu(
                                                    context,
                                                    album,
                                                    expanded = expanded,
                                                    onDismissRequest = onDismissRequest,
                                                )
                                            },
                                            onClick = {
                                                context.navController.navigate(
                                                    RoutesBuilder.buildAlbumRoute(album.albumId)
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
        }
    )
}

@Composable
private fun SideHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenericGrooveCard(
    image: () -> Uri,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    options: @Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick
    ) {
        Box(modifier = Modifier.padding(12.dp, 12.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    rememberAsyncImagePainter(image()),
                    null,
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        title()
                    }
                    subtitle?.let {
                        ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                            it()
                        }
                    }
                }
                Spacer(modifier = Modifier.width(15.dp))

                var showOptionsMenu by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showOptionsMenu = !showOptionsMenu }
                ) {
                    Icon(Icons.Default.MoreVert, null)
                    options(showOptionsMenu) {
                        showOptionsMenu = false
                    }
                }
            }
        }
    }
}

private fun GrooveKinds.label(context: ViewContext) = when (this) {
    GrooveKinds.SONG -> context.symphony.t.songs
    GrooveKinds.ALBUM -> context.symphony.t.albums
    GrooveKinds.ARTIST -> context.symphony.t.artists
}
