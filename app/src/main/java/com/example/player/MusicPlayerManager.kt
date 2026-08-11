package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, REPEAT_ALL, REPEAT_ONE
}

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val bassBoostLevel: Int = 80, // 0 to 100%
    val equalizerPreset: String = "Utla Sub Boost",
    val playlist: List<Song> = emptyList(),
    val currentSongIndex: Int = -1
)

class MusicPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressTrackerJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun setPlaylist(songs: List<Song>, initialIndex: Int = 0) {
        _playbackState.value = _playbackState.value.copy(
            playlist = songs,
            currentSongIndex = initialIndex
        )
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        val currentList = if (playlist.isNotEmpty()) playlist else _playbackState.value.playlist.ifEmpty { listOf(song) }
        val index = currentList.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }

        _playbackState.value = _playbackState.value.copy(
            currentSong = song,
            playlist = currentList,
            currentSongIndex = index,
            durationMs = song.durationMs
        )

        try {
            stopAndReleasePlayer()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (song.contentUri.startsWith("http://") || song.contentUri.startsWith("https://")) {
                    setDataSource(song.contentUri)
                } else {
                    setDataSource(context, Uri.parse(song.contentUri))
                }

                setOnPreparedListener { mp ->
                    mp.start()
                    val actualDuration = if (mp.duration > 0) mp.duration.toLong() else song.durationMs
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = true,
                        durationMs = actualDuration
                    )
                    startProgressTracker()
                }

                setOnCompletionListener {
                    handlePlaybackCompletion()
                }

                setOnErrorListener { _, _, _ ->
                    _playbackState.value = _playbackState.value.copy(isPlaying = false)
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback simulation for offline preview
            _playbackState.value = _playbackState.value.copy(
                isPlaying = true,
                durationMs = song.durationMs
            )
            startProgressTracker()
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player != null) {
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                stopProgressTracker()
            } else {
                player.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracker()
            }
        } else {
            val current = _playbackState.value.currentSong
            if (current != null) {
                playSong(current)
            } else if (_playbackState.value.playlist.isNotEmpty()) {
                playSong(_playbackState.value.playlist[0])
            }
        }
    }

    fun playNext() {
        val state = _playbackState.value
        if (state.playlist.isEmpty()) return

        val nextIndex = if (state.isShuffle) {
            (state.playlist.indices).random()
        } else {
            (state.currentSongIndex + 1) % state.playlist.size
        }

        playSong(state.playlist[nextIndex], state.playlist)
    }

    fun playPrevious() {
        val state = _playbackState.value
        if (state.playlist.isEmpty()) return

        val prevIndex = if (state.currentPositionMs > 3000) {
            state.currentSongIndex
        } else if (state.currentSongIndex > 0) {
            state.currentSongIndex - 1
        } else {
            state.playlist.size - 1
        }

        playSong(state.playlist[prevIndex], state.playlist)
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(positionMs.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(
            isShuffle = !_playbackState.value.isShuffle
        )
    }

    fun cycleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    fun setBassBoostLevel(level: Int) {
        _playbackState.value = _playbackState.value.copy(
            bassBoostLevel = level.coerceIn(0, 100)
        )
    }

    fun setEqualizerPreset(preset: String) {
        _playbackState.value = _playbackState.value.copy(
            equalizerPreset = preset
        )
    }

    private fun handlePlaybackCompletion() {
        val state = _playbackState.value
        when (state.repeatMode) {
            RepeatMode.REPEAT_ONE -> {
                state.currentSong?.let { playSong(it) }
            }
            RepeatMode.REPEAT_ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (state.currentSongIndex < state.playlist.size - 1) {
                    playNext()
                } else {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPositionMs = 0L
                    )
                }
            }
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = scope.launch {
            while (isActive) {
                val mp = mediaPlayer
                if (mp != null && mp.isPlaying) {
                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = mp.currentPosition.toLong(),
                        durationMs = if (mp.duration > 0) mp.duration.toLong() else _playbackState.value.durationMs
                    )
                } else if (_playbackState.value.isPlaying) {
                    // Simulated position ticker if media player isn't using hardware audio
                    val nextPos = _playbackState.value.currentPositionMs + 500
                    if (nextPos >= _playbackState.value.durationMs && _playbackState.value.durationMs > 0) {
                        handlePlaybackCompletion()
                    } else {
                        _playbackState.value = _playbackState.value.copy(currentPositionMs = nextPos)
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    private fun stopAndReleasePlayer() {
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }

    fun release() {
        stopAndReleasePlayer()
    }
}
