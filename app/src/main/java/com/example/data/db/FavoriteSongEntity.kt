package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey val songId: Long,
    val title: String,
    val artist: String,
    val dateAdded: Long = System.currentTimeMillis()
)
