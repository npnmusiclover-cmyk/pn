package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY id DESC")
    fun getAllWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    fun getWallpaperById(id: String): Flow<WallpaperEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<WallpaperEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: WallpaperEntity)

    @Update
    suspend fun updateWallpaper(wallpaper: WallpaperEntity)

    @Query("DELETE FROM wallpapers WHERE id = :id")
    suspend fun deleteWallpaperById(id: String)

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isDownloaded = 1 ORDER BY downloadTime DESC")
    fun getDownloadHistory(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE lastViewedTime > 0 ORDER BY lastViewedTime DESC")
    fun getViewHistory(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isOffline = 1 ORDER BY id DESC")
    fun getOfflineWallpapers(): Flow<List<WallpaperEntity>>

    // Settings queries
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(settings: AppSettingsEntity)

    @Update
    suspend fun updateAppSettings(settings: AppSettingsEntity)
}
