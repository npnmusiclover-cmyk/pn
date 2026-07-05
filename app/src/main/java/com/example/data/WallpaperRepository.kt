package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class WallpaperRepository(private val wallpaperDao: WallpaperDao) {

    val allWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getAllWallpapers()
    val favoriteWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getFavoriteWallpapers()
    val downloadHistory: Flow<List<WallpaperEntity>> = wallpaperDao.getDownloadHistory()
    val viewHistory: Flow<List<WallpaperEntity>> = wallpaperDao.getViewHistory()
    val offlineWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getOfflineWallpapers()
    val appSettings: Flow<AppSettingsEntity> = wallpaperDao.getAppSettings().map { 
        it ?: AppSettingsEntity() 
    }

    fun getWallpaperById(id: String): Flow<WallpaperEntity?> {
        return wallpaperDao.getWallpaperById(id)
    }

    suspend fun insertWallpaper(wallpaper: WallpaperEntity) {
        wallpaperDao.insertWallpaper(wallpaper)
    }

    suspend fun updateWallpaper(wallpaper: WallpaperEntity) {
        wallpaperDao.updateWallpaper(wallpaper)
    }

    suspend fun deleteWallpaperById(id: String) {
        wallpaperDao.deleteWallpaperById(id)
    }

    suspend fun updateAppSettings(settings: AppSettingsEntity) {
        wallpaperDao.updateAppSettings(settings)
    }

    suspend fun toggleFavorite(wallpaperId: String) {
        val list = allWallpapers.first()
        val item = list.find { it.id == wallpaperId }
        if (item != null) {
            wallpaperDao.updateWallpaper(item.copy(isFavorite = !item.isFavorite))
        }
    }

    suspend fun markAsViewed(wallpaperId: String) {
        val list = allWallpapers.first()
        val item = list.find { it.id == wallpaperId }
        if (item != null) {
            wallpaperDao.updateWallpaper(item.copy(lastViewedTime = System.currentTimeMillis()))
        }
    }

    suspend fun markAsDownloaded(wallpaperId: String) {
        val list = allWallpapers.first()
        val item = list.find { it.id == wallpaperId }
        if (item != null) {
            wallpaperDao.updateWallpaper(item.copy(
                isDownloaded = true,
                downloadTime = System.currentTimeMillis()
            ))
        }
    }

    suspend fun prepopulateDatabaseIfEmpty() {
        val currentSettings = wallpaperDao.getAppSettings().first()
        if (currentSettings == null) {
            wallpaperDao.insertAppSettings(AppSettingsEntity())
        }

        val existing = wallpaperDao.getAllWallpapers().first()
        if (existing.isEmpty()) {
            val list = listOf(
                WallpaperEntity(
                    id = "wall_ai_art",
                    title = "Cosmic Swirls",
                    category = "AI Art",
                    imageResName = "img_wallpaper_ai_art_1783227889331",
                    is4K = true,
                    resolution = "3840 x 2160",
                    fileSize = "4.8 MB",
                    colors = "#FF007F,#7F00FF,#000000",
                    tags = "cosmic, swirls, nebula, abstract, colorful"
                ),
                WallpaperEntity(
                    id = "wall_cyberpunk",
                    title = "Neon Tokyo 2099",
                    category = "Cyberpunk",
                    imageResName = "img_wallpaper_cyberpunk_1783227903579",
                    is4K = true,
                    resolution = "3840 x 2160",
                    fileSize = "5.2 MB",
                    colors = "#00FFFF,#FF00FF,#050515",
                    tags = "cyberpunk, city, neon, rain, future, holographic"
                ),
                WallpaperEntity(
                    id = "wall_space",
                    title = "Lonely Astronaut",
                    category = "Space",
                    imageResName = "img_wallpaper_space_1783227916508",
                    is4K = true,
                    resolution = "3840 x 2160",
                    fileSize = "3.9 MB",
                    colors = "#8A2BE2,#FF8C00,#0F0F1A",
                    tags = "astronaut, minimal, orange, planet, purple, desert"
                ),
                WallpaperEntity(
                    id = "wall_nature_1",
                    title = "Misty Forest Peaks",
                    category = "Nature",
                    imageResName = "img_nature_misty_forest",
                    is4K = false,
                    resolution = "1920 x 1080",
                    fileSize = "1.8 MB",
                    colors = "#2D5A27,#1A331E,#E0F0E0",
                    tags = "nature, mountains, misty, green, pines"
                ),
                WallpaperEntity(
                    id = "wall_cars_1",
                    title = "Hypercar Red Eclipse",
                    category = "Cars",
                    imageResName = "img_cars_hypercar",
                    is4K = true,
                    resolution = "3840 x 2160",
                    fileSize = "4.5 MB",
                    colors = "#FF0000,#222222,#111111",
                    tags = "cars, sports, red, hypercar, speed"
                ),
                WallpaperEntity(
                    id = "wall_gaming_1",
                    title = "Cyber Katana",
                    category = "Gaming",
                    imageResName = "img_gaming_katana",
                    isLive = true,
                    is4K = true,
                    resolution = "3840 x 2160",
                    fileSize = "12.4 MB",
                    colors = "#00FF66,#FF0055,#000000",
                    tags = "gaming, cyberpunk, sword, glowing, live"
                ),
                WallpaperEntity(
                    id = "wall_anime_1",
                    title = "Sunset Rooftop Anime",
                    category = "Anime",
                    imageResName = "img_anime_sunset",
                    is4K = false,
                    resolution = "2048 x 1536",
                    fileSize = "2.2 MB",
                    colors = "#FF8000,#E040FB,#1D1B26",
                    tags = "anime, sunset, skyline, cozy, aesthetic"
                ),
                WallpaperEntity(
                    id = "wall_amoled_1",
                    title = "Deep Dark Minimal",
                    category = "Minimal",
                    imageResName = "img_amoled_minimal",
                    is4K = true,
                    resolution = "3840 x 2160",
                    fileSize = "0.8 MB",
                    colors = "#000000,#2C2C2C,#888888",
                    tags = "amoled, black, dark, minimal, grey"
                ),
                WallpaperEntity(
                    id = "wall_technology_1",
                    title = "Neural Web Grid",
                    category = "Technology",
                    imageResName = "img_tech_neural_web",
                    is4K = true,
                    resolution = "3840 x 2160",
                    fileSize = "3.1 MB",
                    colors = "#1A237E,#00E5FF,#000000",
                    tags = "technology, grid, network, ai, matrix"
                ),
                WallpaperEntity(
                    id = "wall_abstract_1",
                    title = "Liquid Marbled Dream",
                    category = "Abstract",
                    imageResName = "img_abstract_marble",
                    isLive = true,
                    is4K = false,
                    resolution = "1920 x 1080",
                    fileSize = "8.9 MB",
                    colors = "#FFEB3B,#9C27B0,#E91E63",
                    tags = "abstract, liquid, fluid, rainbow, trippy, live"
                ),
                WallpaperEntity(
                    id = "wall_neon_1",
                    title = "Neon Retro Wave",
                    category = "Neon",
                    imageResName = "img_neon_retrowave",
                    is4K = true,
                    resolution = "3840 x 2160",
                    fileSize = "4.0 MB",
                    colors = "#E91E63,#3F51B5,#212121",
                    tags = "neon, retrowave, synthwave, grid, sunset"
                ),
                WallpaperEntity(
                    id = "wall_animals_1",
                    title = "Majestic Golden Lion",
                    category = "Animals",
                    imageResName = "img_animals_lion",
                    is4K = false,
                    resolution = "1920 x 1200",
                    fileSize = "2.4 MB",
                    colors = "#FFB300,#795548,#212121",
                    tags = "animals, lion, majestic, wild, golden"
                )
            )
            wallpaperDao.insertWallpapers(list)
        }
    }
}
