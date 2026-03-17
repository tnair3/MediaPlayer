package com.tejasnair.mediaplayer.data.local

import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import com.tejasnair.mediaplayer.data.model.*
import com.tejasnair.mediaplayer.data.local.entities.*
import com.tejasnair.mediaplayer.data.local.dao.MusicDao
@Database(
    entities = [
        Song::class,
        Artist::class,
        Album::class,
        ArtistsForSong::class,
        ArtistsForAlbum::class,
        SongsInAlbum::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_player_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}