package com.tejasnair.mediaplayer.data.local.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.tejasnair.mediaplayer.data.local.dao.PlaylistDao
import com.tejasnair.mediaplayer.data.local.dao.SongDao
import com.tejasnair.mediaplayer.data.local.dao.VinylDao
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
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = MusicDatabase.Migration4To5::class)
    ]
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun vinylDao(): VinylDao

    @DeleteColumn(tableName = "songToPlaylist", columnName = "position")
    class Migration4To5 : AutoMigrationSpec

    companion object {
        @Volatile private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(lock = this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    klass = MusicDatabase::class.java,
                    name = "music_database"
                )
                    .fallbackToDestructiveMigrationFrom(true, 1, 2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}