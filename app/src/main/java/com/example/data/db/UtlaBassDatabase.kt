package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteSongEntity::class], version = 1, exportSchema = false)
abstract class UtlaBassDatabase : RoomDatabase() {
    abstract fun favoriteSongDao(): FavoriteSongDao

    companion object {
        @Volatile
        private var INSTANCE: UtlaBassDatabase? = null

        fun getDatabase(context: Context): UtlaBassDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UtlaBassDatabase::class.java,
                    "utla_bass_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
