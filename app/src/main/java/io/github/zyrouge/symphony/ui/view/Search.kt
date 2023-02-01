package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.*
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    var terms by rememberSaveable { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val songs = remember { mutableStateListOf<Song>() }
    val artists = remember { mutableStateListOf<Artist>() }
    val albums = remember { mutableStateListOf<Album>() }
    val albumArtists = remember { mutableStateListOf<Artist>() }
    val genres = remember { mutableStateListOf<Genre>() }
    val playlists = remember { mutableStateListOf<Playlist>() }

    var selectedChip by rememberSaveable { mutableStateOf<GrooveKinds?>(null) }
    fun isChipSelected(kind: GrooveKinds) = selectedChip == null || selectedChip == kind

    var currentTermsRoutine: Job? = null
    fun setTerms(nTerms: String) {
        terms = nTerms
        isSearching = true
        currentTermsRoutine?.cancel()
        currentTermsRoutine = coroutineScope.launch {
            withContext(Dispatchers.Default) {
                delay(250)
                songs.clear()
                artists.clear()
                albums.clear()
                albumArtists.clear()
                genres.clear()
                if (nTerms.isNotEmpty()) {
                    if (isChipSelected(GrooveKinds.SONG)) {
                        songs.addAll(
                            context.symphony.groove.song.search(terms).map { it.entity }
                        )
                    }
                    if (isChipSelected(GrooveKinds.ARTIST)) {
                        artists.addAll(
                            context.symphony.groove.artist.search(terms).map { it.entity }
                        )
                    }
                    if (isChipSelected(GrooveKinds.ALBUM)) {
                        albums.addAll(
                            context.symphony.groove.album.search(terms).map { it.entity }
                        )
                    }
                    if (isChipSelected(GrooveKinds.ALBUM_ARTIST)) {
                        albumArtists.addAll(
                            context.symphony.groove.albumArtist.search(terms).map { it.entity }
                        )
                    }
                    if (isChipSelected(GrooveKinds.GENRE)) {
                        genres.addAll(
                            context.symphony.groove.genre.search(terms).map { it.entity }
                        )
                    }
                    if (isChipSelected(GrooveKinds.PLAYLIST)) {
                        playlists.addAll(
                            context.symphony.groove.playlist.search(terms).map { it.entity }
                        )
                    }
                }
                isSearching = false
            }
        }
    }

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
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = selectedChip == null,
                        label = {
                            Text(context.symphony.t.all)
                        },
                        onClick = {
                            selectedChip = null
                            setTerms(terms)
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
                                setTerms(terms)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        content = { contentPadding ->
            val hasSongs = isChipSelected(GrooveKinds.SONG) && songs.isNotEmpty()
            val hasArtists = isChipSelected(GrooveKinds.ARTIST) && artists.isNotEmpty()
            val hasAlbums = isChipSelected(GrooveKinds.ALBUM) && albums.isNotEmpty()
            val hasAlbumArtists =
                isChipSelected(GrooveKinds.ALBUM_ARTIST) && albumArtists.isNotEmpty()
            val hasPlaylists =
                isChipSelected(GrooveKinds.PLAYLIST) && playlists.isNotEmpty()
            val hasGenres = isChipSelected(GrooveKinds.GENRE) && genres.isNotEmpty()
            val hasNoResults =
                !hasSongs && !hasArtists && !hasAlbums && !hasAlbumArtists && !hasPlaylists && !hasGenres

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
                        hasNoResults -> {
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
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                if (hasSongs) {
                                    SideHeading(context, GrooveKinds.SONG)
                                    songs.forEach { song ->
                                        SongCard(context, song) {
                                            context.symphony.radio.shorty.playQueue(song)
                                        }
                                    }
                                }
                                if (hasArtists) {
                                    SideHeading(context, GrooveKinds.ARTIST)
                                    artists.forEach { artist ->
                                        GenericGrooveCard(
                                            image = artist
                                                .createArtworkImageRequest(context.symphony)
                                                .build(),
                                            title = {
                                                Text(artist.name)
                                            },
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
                                                    RoutesBuilder.buildArtistRoute(artist.name)
                                                )
                                            }
                                        )
                                    }
                                }
                                if (hasAlbums) {
                                    SideHeading(context, GrooveKinds.ALBUM)
                                    albums.forEach { album ->
                                        GenericGrooveCard(
                                            image = album
                                                .createArtworkImageRequest(context.symphony)
                                                .build(),
                                            title = {
                                                Text(album.name)
                                            },
                                            subtitle = album.artist?.let { { Text(it) } },
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
                                                    RoutesBuilder.buildAlbumRoute(album.id)
                                                )
                                            }
                                        )
                                    }
                                }
                                if (hasAlbumArtists) {
                                    SideHeading(context, GrooveKinds.ALBUM_ARTIST)
                                    albumArtists.forEach { artist ->
                                        GenericGrooveCard(
                                            image = artist
                                                .createArtworkImageRequest(context.symphony)
                                                .build(),
                                            title = {
                                                Text(artist.name)
                                            },
                                            options = { expanded, onDismissRequest ->
                                                AlbumArtistDropdownMenu(
                                                    context,
                                                    artist,
                                                    expanded = expanded,
                                                    onDismissRequest = onDismissRequest,
                                                )
                                            },
                                            onClick = {
                                                context.navController.navigate(
                                                    RoutesBuilder.buildAlbumArtistRoute(artist.name)
                                                )
                                            }
                                        )
                                    }
                                }
                                if (hasPlaylists) {
                                    SideHeading(context, GrooveKinds.PLAYLIST)
                                    playlists.forEach { playlist ->
                                        GenericGrooveCard(
                                            image = playlist
                                                .createArtworkImageRequest(context.symphony)
                                                .build(),
                                            title = {
                                                Text(playlist.title)
                                            },
                                            options = { expanded, onDismissRequest ->
                                                PlaylistDropdownMenu(
                                                    context,
                                                    playlist,
                                                    expanded = expanded,
                                                    onDismissRequest = onDismissRequest,
                                                )
                                            },
                                            onClick = {
                                                context.navController.navigate(
                                                    RoutesBuilder.buildPlaylistRoute(playlist.id)
                                                )
                                            }
                                        )
                                    }
                                }
                                if (hasGenres) {
                                    SideHeading(context, GrooveKinds.GENRE)
                                    genres.forEach { genre ->
                                        GenericGrooveCard(
                                            image = null,
                                            title = { Text(genre.name) },
                                            subtitle = {
                                                Text(context.symphony.t.XSongs(genre.numberOfTracks))
                                            },
                                            options = null,
                                            onClick = {
                                                context.navController.navigate(
                                                    RoutesBuilder.buildGenreRoute(genre.name)
                                                )
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SideHeading(context: ViewContext, kind: GrooveKinds) {
    SideHeading(kind.label(context))
}

@Composable
private fun SideHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 4.dp)
    )
}

private fun GrooveKinds.label(context: ViewContext) = when (this) {
    GrooveKinds.SONG -> context.symphony.t.songs
    GrooveKinds.ALBUM -> context.symphony.t.albums
    GrooveKinds.ARTIST -> context.symphony.t.artists
    GrooveKinds.ALBUM_ARTIST -> context.symphony.t.albumArtists
    GrooveKinds.GENRE -> context.symphony.t.genres
    GrooveKinds.PLAYLIST -> context.symphony.t.playlists
}
