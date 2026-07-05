package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WallsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = WallpaperRepository(database.wallpaperDao())

    init {
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
            startFirebaseSync()
        }
    }

    // Exposed flows
    val allWallpapers: StateFlow<List<WallpaperEntity>> = repository.allWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteWallpapers: StateFlow<List<WallpaperEntity>> = repository.favoriteWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadHistory: StateFlow<List<WallpaperEntity>> = repository.downloadHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val viewHistory: StateFlow<List<WallpaperEntity>> = repository.viewHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineWallpapers: StateFlow<List<WallpaperEntity>> = repository.offlineWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettingsEntity> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    // UI state states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedColorFilter = MutableStateFlow<String?>(null)
    val selectedColorFilter: StateFlow<String?> = _selectedColorFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Editor configurations
    private val _editorBlur = MutableStateFlow(0f)
    val editorBlur = _editorBlur.asStateFlow()

    private val _editorBrightness = MutableStateFlow(0f)
    val editorBrightness = _editorBrightness.asStateFlow()

    private val _editorContrast = MutableStateFlow(1f)
    val editorContrast = _editorContrast.asStateFlow()

    private val _editorSaturation = MutableStateFlow(1f)
    val editorSaturation = _editorSaturation.asStateFlow()

    private val _editorFilter = MutableStateFlow("None")
    val editorFilter = _editorFilter.asStateFlow()

    private val _editorText = MutableStateFlow("")
    val editorText = _editorText.asStateFlow()

    private val _editorTextColor = MutableStateFlow("#FFFFFF")
    val editorTextColor = _editorTextColor.asStateFlow()

    private val _editorTextSize = MutableStateFlow(24f)
    val editorTextSize = _editorTextSize.asStateFlow()

    private val _editorSticker = MutableStateFlow("None")
    val editorSticker = _editorSticker.asStateFlow()

    // Analytics simulation state
    private val _analyticsDownloads = MutableStateFlow(1420)
    val analyticsDownloads = _analyticsDownloads.asStateFlow()

    private val _analyticsUploads = MutableStateFlow(154)
    val analyticsUploads = _analyticsUploads.asStateFlow()

    // Filtered lists
    val filteredWallpapers: StateFlow<List<WallpaperEntity>> = combine(
        allWallpapers,
        _searchQuery,
        _selectedColorFilter,
        _selectedCategoryFilter,
        _selectedTag
    ) { list, query, color, category, tag ->
        var result = list

        if (query.isNotEmpty()) {
            result = result.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.tags.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }

        if (color != null) {
            result = result.filter { it.colors.contains(color, ignoreCase = true) }
        }

        if (category != null) {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (tag != null) {
            result = result.filter { it.tags.contains(tag, ignoreCase = true) }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selection for detail view
    private val _selectedWallpaperId = MutableStateFlow<String?>(null)
    val selectedWallpaperId = _selectedWallpaperId.asStateFlow()

    val selectedWallpaper: StateFlow<WallpaperEntity?> = _selectedWallpaperId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getWallpaperById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User authentication status simulation
    private val _userEmail = MutableStateFlow<String?>(null) // null means guest
    val userEmail = _userEmail.asStateFlow()

    fun loginWithGoogle(email: String) {
        viewModelScope.launch {
            _userEmail.value = email
            // Auto sync favorites and settings on login
        }
    }

    fun logout() {
        _userEmail.value = null
    }

    // Actions
    fun setWallpaperId(id: String?) {
        _selectedWallpaperId.value = id
        // Reset editor on select
        if (id != null) {
            resetEditor()
            viewModelScope.launch {
                repository.markAsViewed(id)
            }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun downloadWallpaper(id: String) {
        viewModelScope.launch {
            repository.markAsDownloaded(id)
            _analyticsDownloads.value += 1
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        _selectedTag.value = null
    }

    fun selectColorFilter(colorHex: String?) {
        _selectedColorFilter.value = colorHex
    }

    fun selectCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = tag
        if (tag != null) {
            _searchQuery.value = ""
        }
    }

    // Editor Setters
    fun setEditorBlur(value: Float) { _editorBlur.value = value }
    fun setEditorBrightness(value: Float) { _editorBrightness.value = value }
    fun setEditorContrast(value: Float) { _editorContrast.value = value }
    fun setEditorSaturation(value: Float) { _editorSaturation.value = value }
    fun setEditorFilter(value: String) { _editorFilter.value = value }
    fun setEditorText(value: String) { _editorText.value = value }
    fun setEditorTextColor(value: String) { _editorTextColor.value = value }
    fun setEditorTextSize(value: Float) { _editorTextSize.value = value }
    fun setEditorSticker(value: String) { _editorSticker.value = value }

    fun resetEditor() {
        _editorBlur.value = 0f
        _editorBrightness.value = 0f
        _editorContrast.value = 1f
        _editorSaturation.value = 1f
        _editorFilter.value = "None"
        _editorText.value = ""
        _editorTextColor.value = "#FFFFFF"
        _editorTextSize.value = 24f
        _editorSticker.value = "None"
    }

    fun saveEditedWallpaper(title: String, category: String) {
        val current = selectedWallpaper.value ?: return
        val newId = "edited_${current.id}_${System.currentTimeMillis()}"
        viewModelScope.launch {
            val edited = WallpaperEntity(
                id = newId,
                title = title,
                category = category,
                imageResName = current.imageResName,
                isLive = current.isLive,
                is4K = current.is4K,
                resolution = current.resolution,
                fileSize = current.fileSize,
                colors = current.colors,
                tags = current.tags + ", edited, custom",
                isFavorite = false,
                isDownloaded = true,
                downloadTime = System.currentTimeMillis(),
                isOffline = true // Save as custom edited wall
            )
            repository.insertWallpaper(edited)
            _analyticsUploads.value += 1
        }
    }

    // Settings actions
    fun toggleDarkMode() {
        viewModelScope.launch {
            val settings = appSettings.value
            repository.updateAppSettings(settings.copy(isDarkMode = !settings.isDarkMode))
        }
    }

    fun toggleDynamicColors() {
        viewModelScope.launch {
            val settings = appSettings.value
            repository.updateAppSettings(settings.copy(useDynamicColors = !settings.useDynamicColors))
        }
    }

    fun changeLanguage(lang: String) {
        viewModelScope.launch {
            val settings = appSettings.value
            repository.updateAppSettings(settings.copy(language = lang))
        }
    }

    fun setAutoChanger(enabled: Boolean, interval: Int, shuffle: Boolean, wifiOnly: Boolean) {
        viewModelScope.launch {
            val settings = appSettings.value
            repository.updateAppSettings(settings.copy(
                isAutoChangerEnabled = enabled,
                autoChangerIntervalMinutes = interval,
                autoChangerShuffle = shuffle,
                autoChangerWifiOnly = wifiOnly
            ))
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            val settings = appSettings.value
            repository.updateAppSettings(settings.copy(isNotificationsEnabled = !settings.isNotificationsEnabled))
        }
    }

    private fun startFirebaseSync() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("wallpapers")
                    .addSnapshotListener { snapshots, error ->
                        if (error != null) {
                            Log.e("FirebaseSync", "Listen failed.", error)
                            return@addSnapshotListener
                        }

                        if (snapshots != null) {
                            viewModelScope.launch {
                                for (dc in snapshots.documentChanges) {
                                    val doc = dc.document
                                    val id = doc.id
                                    
                                    when (dc.type) {
                                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                            val title = doc.getString("title") ?: "Untitled"
                                            val category = doc.getString("category") ?: "Other"
                                            val imageResName = doc.getString("imageResName") ?: ""
                                            val isLive = doc.getBoolean("isLive") ?: false
                                            val is4K = doc.getBoolean("is4K") ?: false
                                            val resolution = doc.getString("resolution") ?: "3840 x 2160"
                                            val fileSize = doc.getString("fileSize") ?: "4.2 MB"
                                            val colors = doc.getString("colors") ?: "#FF007F,#00F0FF,#121212"
                                            val tags = doc.getString("tags") ?: ""
                                            val isOffline = doc.getBoolean("isOffline") ?: false
                                            
                                            val wall = WallpaperEntity(
                                                id = id,
                                                title = title,
                                                category = category,
                                                imageResName = imageResName,
                                                isLive = isLive,
                                                is4K = is4K,
                                                resolution = resolution,
                                                fileSize = fileSize,
                                                colors = colors,
                                                tags = tags,
                                                isOffline = isOffline
                                            )
                                            repository.insertWallpaper(wall)
                                            Log.d("FirebaseSync", "Synced wallpaper: $title ($id)")
                                        }
                                        DocumentChange.Type.REMOVED -> {
                                            repository.deleteWallpaperById(id)
                                            Log.d("FirebaseSync", "Deleted wallpaper from local DB: $id")
                                        }
                                    }
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.w("FirebaseSync", "Firebase Firestore is not initialized or configured: ${e.message}")
            }
        }
    }
}
