package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.MusicListScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.theme.UtlaBassTheme

@Composable
fun UtlaBassApp(
    viewModel: UtlaBassViewModel = viewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasStoragePermission.collectAsStateWithLifecycle()
    val isExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val palette by viewModel.currentPalette.collectAsStateWithLifecycle()

    // Permission Launcher for Android 13+ (READ_MEDIA_AUDIO) and older (READ_EXTERNAL_STORAGE)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val granted = permissionsMap.values.any { it }
        viewModel.setPermissionStatus(granted)
    }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissionsToRequest)
    }

    UtlaBassTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Base View: Music List Screen
                MusicListScreen(
                    songs = songs,
                    currentPlayingSong = playbackState.currentSong,
                    isPlaying = playbackState.isPlaying,
                    hasPermission = hasPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(permissionsToRequest)
                    },
                    onSongClick = { song ->
                        viewModel.playSong(song)
                        // Note: Per user request, clicking a song stays on Song List
                    },
                    onFavoriteToggle = { song ->
                        viewModel.toggleFavorite(song)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (playbackState.currentSong != null && !isExpanded) 96.dp else 0.dp)
                )

                // Bottom Docked Mini Player (Visible when not expanded)
                if (playbackState.currentSong != null && !isExpanded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 8.dp)
                    ) {
                        val duration = playbackState.durationMs.coerceAtLeast(1L).toFloat()
                        val currentPos = playbackState.currentPositionMs.toFloat()
                        val fraction = (currentPos / duration).coerceIn(0f, 1f)

                        MiniPlayer(
                            song = playbackState.currentSong,
                            isPlaying = playbackState.isPlaying,
                            progressFraction = fraction,
                            palette = palette,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNextClick = { viewModel.playNext() },
                            onPreviousClick = { viewModel.playPrevious() },
                            onExpandNowPlaying = { viewModel.expandNowPlaying() }
                        )
                    }
                }

                // Smooth Fluid Zoom-in / Expand Transition for Now Playing Sheet
                AnimatedVisibility(
                    visible = isExpanded && playbackState.currentSong != null,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(animationSpec = spring()) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeOut() + scaleOut(
                        targetScale = 0.9f,
                        animationSpec = spring()
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    NowPlayingScreen(
                        state = playbackState,
                        palette = palette,
                        onCollapse = { viewModel.collapseNowPlaying() },
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        onPrevious = { viewModel.playPrevious() },
                        onSeek = { pos -> viewModel.seekTo(pos) },
                        onShuffleToggle = { viewModel.toggleShuffle() },
                        onRepeatCycle = { viewModel.cycleRepeatMode() },
                        onFavoriteToggle = {
                            playbackState.currentSong?.let { viewModel.toggleFavorite(it) }
                        },
                        onBassBoostChange = { level -> viewModel.setBassBoostLevel(level) },
                        onEqualizerPresetChange = { preset -> viewModel.setEqualizerPreset(preset) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
