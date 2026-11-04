package com.artistwalls.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: LibraryEntry)

    @Query("SELECT * FROM library WHERE id = :id LIMIT 1")
    suspend fun find(id: String): LibraryEntry?

    @Query("SELECT * FROM library WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun favorites(): Flow<List<LibraryEntry>>

    @Query("SELECT * FROM library WHERE isDownloaded = 1 ORDER BY savedAt DESC")
    fun downloads(): Flow<List<LibraryEntry>>

    @Query("SELECT * FROM library WHERE lastViewedAt > 0 ORDER BY lastViewedAt DESC LIMIT 60")
    fun recentlyViewed(): Flow<List<LibraryEntry>>

    @Query("SELECT id FROM library WHERE isFavorite = 1")
    fun favoriteIds(): Flow<List<String>>

    @Query("SELECT id FROM library WHERE isDownloaded = 1")
    fun downloadedIds(): Flow<List<String>>

    @Query("SELECT * FROM library WHERE lastViewedAt > 0 ORDER BY lastViewedAt DESC LIMIT 200")
    suspend fun creditRows(): List<LibraryEntry>

    @Query("UPDATE library SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("UPDATE library SET lastViewedAt = :timestamp WHERE id = :id")
    suspend fun touchViewed(id: String, timestamp: Long)

    @Query("UPDATE library SET isDownloaded = 1, localPath = :path, bytes = :bytes, savedAt = :timestamp WHERE id = :id")
    suspend fun markDownloaded(id: String, path: String, bytes: Long, timestamp: Long)

    @Query("DELETE FROM library WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM library WHERE isFavorite = 0 AND isDownloaded = 0")
    suspend fun purgeUnused()

    @Query("SELECT COUNT(*) FROM library")
    suspend fun count(): Int
}
