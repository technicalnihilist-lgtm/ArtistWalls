package com.artistwalls.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LibraryEntry::class],
    version = 1,
    exportSchema = true
)
abstract class ArtistWallsDatabase : RoomDatabase() {

    abstract fun libraryDao(): LibraryDao

    companion object {
        @Volatile
        private var instance: ArtistWallsDatabase? = null

        fun get(context: Context): ArtistWallsDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ArtistWallsDatabase::class.java,
                "artistwalls.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
