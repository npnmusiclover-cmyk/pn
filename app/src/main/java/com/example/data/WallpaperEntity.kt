package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val imageResName: String,
    val isLive: Boolean = false,
    val is4K: Boolean = false,
    val resolution: String = "3840 x 2160",
    val fileSize: String = "4.2 MB",
    val colors: String = "#FF007F,#00F0FF,#121212", // Comma-separated hex values
    val tags: String = "", // Comma-separated tags
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val lastViewedTime: Long = 0L,
    val downloadTime: Long = 0L,
    val isOffline: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = true,
    val useDynamicColors: Boolean = true,
    val language: String = "en", // "en", "hi", "es", "fr", "ja", "de"
    val isAutoChangerEnabled: Boolean = false,
    val autoChangerIntervalMinutes: Int = 60, // 15, 30, 60, 180, 360, 720, 1440, 10080
    val autoChangerShuffle: Boolean = true,
    val autoChangerWifiOnly: Boolean = false,
    val isNotificationsEnabled: Boolean = true
)
