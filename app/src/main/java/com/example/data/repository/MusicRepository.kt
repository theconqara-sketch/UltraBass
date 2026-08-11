package com.example.data.repository

import android.content.Context
import com.example.data.MediaScanner
import com.example.data.db.FavoriteSongDao
import com.example.data.db.FavoriteSongEntity
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class MusicRepository(
    private val context: Context,
    private val favoriteSongDao: FavoriteSongDao
) {
    private val mediaScanner = MediaScanner(context)

    fun getSongsFlow(permissionGranted: Boolean): Flow<List<Song>> = flow {
        val scannedSongs = if (permissionGranted) {
            mediaScanner.scanAudioFiles()
        } else {
            emptyList()
        }

        val allSongs = if (scannedSongs.isNotEmpty()) {
            scannedSongs
        } else {
            getSampleSongs()
        }

        emit(allSongs)
    }.combine(favoriteSongDao.getFavoriteSongIds()) { songs, favIds ->
        val favSet = favIds.toSet()
        songs.map { song ->
            song.copy(isFavorite = favSet.contains(song.id))
        }
    }

    suspend fun toggleFavorite(song: Song) {
        if (song.isFavorite) {
            favoriteSongDao.removeFavorite(song.id)
        } else {
            favoriteSongDao.addFavorite(
                FavoriteSongEntity(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist
                )
            )
        }
    }

    companion object {
        fun getSampleSongs(): List<Song> {
            return listOf(
                Song(
                    id = 101L,
                    title = "Utla Bass - Subwoofer Bounce",
                    artist = "Utla Bass Crew",
                    album = "Deep Sub Explosion",
                    durationMs = 214000L,
                    contentUri = "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3?filename=lofi-study-112191.mp3",
                    albumArtUri = null,
                    genre = "Sub Bass"
                ),
                Song(
                    id = 102L,
                    title = "Acrobatic Bass Line",
                    artist = "Dj Utla",
                    album = "Material Wave",
                    durationMs = 188000L,
                    contentUri = "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3?filename=beat-of-trumpets-110287.mp3",
                    albumArtUri = null,
                    genre = "Afro Bass"
                ),
                Song(
                    id = 103L,
                    title = "Swahili Groove Sub",
                    artist = "Bongo Beats Studio",
                    album = "Dar Bass Night",
                    durationMs = 245000L,
                    contentUri = "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3?filename=smooth-waters-11597.mp3",
                    albumArtUri = null,
                    genre = "Bongo Bass"
                ),
                Song(
                    id = 104L,
                    title = "Heavy Sub Impact 808",
                    artist = "Low Frequency Lab",
                    album = "Subwoofer Test 2026",
                    durationMs = 196000L,
                    contentUri = "https://cdn.pixabay.com/download/audio/2022/10/14/audio_993910c0e7.mp3?filename=energy-124808.mp3",
                    albumArtUri = null,
                    genre = "Trap Bass"
                ),
                Song(
                    id = 105L,
                    title = "Night Drive Bassline",
                    artist = "Synthesizer Pro",
                    album = "Neon Highways",
                    durationMs = 230000L,
                    contentUri = "https://cdn.pixabay.com/download/audio/2022/11/06/audio_c36e4f3a03.mp3?filename=chill-abstract-intention-12099.mp3",
                    albumArtUri = null,
                    genre = "Deep House"
                ),
                Song(
                    id = 106L,
                    title = "Acoustic Sub Resonance",
                    artist = "Vocal & Bass Duo",
                    album = "Unplugged Bass Session",
                    durationMs = 175000L,
                    contentUri = "https://cdn.pixabay.com/download/audio/2022/08/02/audio_884fe92c21.mp3?filename=ambient-piano-11881.mp3",
                    albumArtUri = null,
                    genre = "Acoustic"
                )
            )
        }
    }
}
