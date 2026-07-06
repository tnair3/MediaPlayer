package com.tejasnair.mediaplayer.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tejasnair.mediaplayer.data.local.dao.MusicDao
import com.tejasnair.mediaplayer.data.model.*

@Database(
    entities = [
        Song::class,
        Playlist::class,
        SongToPlaylist::class,
        Vinyl::class,
        VinylSide::class,
        SongToVinylSide::class
               ],
    version = 4,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(lock = this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    klass = MusicDatabase::class.java,
                    name = "music_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}