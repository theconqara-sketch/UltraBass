package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.UtlaBassDatabase
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.player.MusicPlayerManager
import com.example.player.PlaybackState
import com.example.ui.theme.MaterialYouSongPalette
import com.example.ui.theme.PaletteUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UtlaBassViewModel(application: Application) : AndroidViewModel(application) {

    private val database = UtlaBassDatabase.getDatabase(application)
    private val repository = MusicRepository(application, database.favoriteSongDao())
    val playerManager = MusicPlayerManager(application)

    private val _hasStoragePermission = MutableStateFlow(false)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    val songs: StateFlow<List<Song>> = _hasStoragePermission
        .flatMapLatest { granted ->
            repository.getSongsFlow(granted)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MusicRepository.getSampleSongs()
        )

    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState

    val currentPalette: StateFlow<MaterialYouSongPalette> = playbackState
        .combine(_hasStoragePermission) { state, _ ->
            PaletteUtils.getPaletteForSong(state.currentSong)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaletteUtils.getPaletteForSong(null)
        )

    init {
        // Preload initial playlist
        viewModelScope.launch {
            songs.collect { songList ->
                if (songList.isNotEmpty() && playerManager.playbackState.value.currentSong == null) {
                    playerManager.setPlaylist(songList, 0)
                }
            }
        }
    }

    fun setPermissionStatus(granted: Boolean) {
        _hasStoragePermission.value = granted
    }

    fun playSong(song: Song) {
        playerManager.playSong(song, songs.value)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun playNext() {
        playerManager.playNext()
    }

    fun playPrevious() {
        playerManager.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerManager.cycleRepeatMode()
    }

    fun setBassBoostLevel(level: Int) {
        playerManager.setBassBoostLevel(level)
    }

    fun setEqualizerPreset(preset: String) {
        playerManager.setEqualizerPreset(preset)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    fun expandNowPlaying() {
        _isNowPlayingExpanded.value = true
    }

    fun collapseNowPlaying() {
        _isNowPlayingExpanded.value = false
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
