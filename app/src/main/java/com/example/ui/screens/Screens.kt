package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WallpaperEntity
import com.example.ui.components.TranslationHelper
import com.example.ui.components.WallpaperImage
import com.example.ui.viewmodel.WallsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: WallsViewModel, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLogoVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isLogoVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1115),
                        Color(0xFF16181C),
                        Color(0xFF0F1115)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative pulsing abstract ambient circles
        val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowScale"
        )

        Box(
            modifier = Modifier
                .size(400.dp)
                .scale(glowScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isLogoVisible,
                enter = fadeIn(tween(1000)) + expandVertically(tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Elevated glassmorphic logo container
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_app_icon_1783227864457),
                            contentDescription = "App Icon",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Walls Engine",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "UNLIMITED FREE 4K & LIVE WALLPAPERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA),
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Login buttons
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.loginWithGoogle("pawan.walls@gmail.com")
                        Toast.makeText(context, "Google Sync Enabled: pawan.walls@gmail.com", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("google_login_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = TranslationHelper.translate("google_login", "en"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLoginSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("guest_login_button"),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
            ) {
                Text(
                    text = TranslationHelper.translate("guest_login", "en"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun LiveScreen(viewModel: WallsViewModel, onWallpaperClick: (String) -> Unit) {
    val wallpapers by viewModel.allWallpapers.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val liveWalls = remember(wallpapers) { wallpapers.filter { it.isLive } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = TranslationHelper.translate("live", settings.language),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (liveWalls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No Live Wallpapers available.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(liveWalls) { wall ->
                    WallpaperGridCard(wall = wall, onClick = { onWallpaperClick(wall.id) })
                }
            }
        }
    }
}

@Composable
fun FourKScreen(viewModel: WallsViewModel, onWallpaperClick: (String) -> Unit) {
    val wallpapers by viewModel.allWallpapers.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val fourKWalls = remember(wallpapers) { wallpapers.filter { it.is4K } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = TranslationHelper.translate("4k", settings.language),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (fourKWalls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No 4K Wallpapers available.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(fourKWalls) { wall ->
                    WallpaperGridCard(wall = wall, onClick = { onWallpaperClick(wall.id) })
                }
            }
        }
    }
}

@Composable
fun CategoriesScreen(viewModel: WallsViewModel, onCategoryClick: (String) -> Unit) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val categoriesList = listOf(
        "Nature" to "#2E7D32",
        "Space" to "#311B92",
        "Cars" to "#C62828",
        "Gaming" to "#AD1457",
        "Anime" to "#EF6C00",
        "Sports" to "#1565C0",
        "Technology" to "#00838F",
        "Cyberpunk" to "#4A148C",
        "Abstract" to "#283593",
        "Animals" to "#4E342E",
        "Minimal" to "#37474F",
        "Neon" to "#00E5FF",
        "Dark" to "#000000",
        "Quotes" to "#558B2F"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = TranslationHelper.translate("categories", settings.language),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categoriesList) { (name, colorHex) ->
                val color = remember(colorHex) { Color(android.graphics.Color.parseColor(colorHex)) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(color, color.copy(alpha = 0.5f))
                            )
                        )
                        .clickable { onCategoryClick(name) }
                        .testTag("category_card_${name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Go",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(viewModel: WallsViewModel, onWallpaperClick: (String) -> Unit) {
    val favorites by viewModel.favoriteWallpapers.collectAsStateWithLifecycle()
    val downloads by viewModel.downloadHistory.collectAsStateWithLifecycle()
    val history by viewModel.viewHistory.collectAsStateWithLifecycle()
    val offlineWalls by viewModel.offlineWallpapers.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TranslationHelper.translate("favorites", settings.language),
        TranslationHelper.translate("downloads_history", settings.language),
        TranslationHelper.translate("view_history", settings.language),
        TranslationHelper.translate("offline_walls", settings.language)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        val activeList = when (selectedTab) {
            0 -> favorites
            1 -> downloads
            2 -> history
            else -> offlineWalls
        }

        if (activeList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        imageVector = when (selectedTab) {
                            0 -> Icons.Outlined.FavoriteBorder
                            1 -> Icons.Outlined.FileDownload
                            2 -> Icons.Outlined.History
                            else -> Icons.Outlined.PhotoLibrary
                        },
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTab == 0) TranslationHelper.translate("no_favorites", settings.language) else "No items found in this collection.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activeList) { wall ->
                    WallpaperGridCard(wall = wall, onClick = { onWallpaperClick(wall.id) })
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: WallsViewModel) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isClearingCache by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // User Profile Block
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "User",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = userEmail ?: "Guest Session",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (userEmail != null) TranslationHelper.translate("cloud_sync", settings.language) else "Local only. Tap Google login to sync.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Dark Mode
        ListItem(
            headlineContent = { Text(TranslationHelper.translate("dark_mode", settings.language)) },
            trailingContent = {
                Switch(
                    checked = settings.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() },
                    modifier = Modifier.testTag("dark_mode_switch")
                )
            }
        )

        // Dynamic Colors
        ListItem(
            headlineContent = { Text(TranslationHelper.translate("dynamic_color", settings.language)) },
            trailingContent = {
                Switch(
                    checked = settings.useDynamicColors,
                    onCheckedChange = { viewModel.toggleDynamicColors() }
                )
            }
        )

        // Cache Cleaner
        ListItem(
            headlineContent = { Text(TranslationHelper.translate("cache_cleaner", settings.language)) },
            supportingContent = { Text("Used space: 128 MB") },
            trailingContent = {
                Button(
                    onClick = {
                        isClearingCache = true
                        // Simulate clearing
                    },
                    enabled = !isClearingCache
                ) {
                    Text(if (isClearingCache) "Clearing..." else TranslationHelper.translate("clear_cache", settings.language))
                }
            }
        )

        LaunchedEffect(isClearingCache) {
            if (isClearingCache) {
                delay(1500)
                isClearingCache = false
                Toast.makeText(context, "Cleared 128 MB cache successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Languages
        Text(
            text = TranslationHelper.translate("languages", settings.language),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )

        val languages = listOf(
            "en" to "English",
            "hi" to "हिंदी",
            "es" to "Español",
            "fr" to "Français",
            "ja" to "日本語",
            "de" to "Deutsch"
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { (code, label) ->
                FilterChip(
                    selected = settings.language == code,
                    onClick = { viewModel.changeLanguage(code) },
                    label = { Text(label) }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Auto Wallpaper Changer
        Text(
            text = TranslationHelper.translate("auto_changer", settings.language),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )

        ListItem(
            headlineContent = { Text(TranslationHelper.translate("enabled", settings.language)) },
            trailingContent = {
                Switch(
                    checked = settings.isAutoChangerEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setAutoChanger(
                            enabled = enabled,
                            interval = settings.autoChangerIntervalMinutes,
                            shuffle = settings.autoChangerShuffle,
                            wifiOnly = settings.autoChangerWifiOnly
                        )
                    }
                )
            }
        )

        if (settings.isAutoChangerEnabled) {
            var selectedInterval by remember { mutableStateOf(settings.autoChangerIntervalMinutes) }
            val intervals = listOf(
                15 to "15 Min",
                30 to "30 Min",
                60 to "1 Hour",
                180 to "3 Hours",
                360 to "6 Hours",
                720 to "12 Hours",
                1440 to "Daily",
                10080 to "Weekly"
            )

            Text("Change Interval", modifier = Modifier.padding(vertical = 8.dp), fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                intervals.forEach { (mins, label) ->
                    FilterChip(
                        selected = selectedInterval == mins,
                        onClick = {
                            selectedInterval = mins
                            viewModel.setAutoChanger(
                                enabled = true,
                                interval = mins,
                                shuffle = settings.autoChangerShuffle,
                                wifiOnly = settings.autoChangerWifiOnly
                            )
                        },
                        label = { Text(label) }
                    )
                }
            }

            ListItem(
                headlineContent = { Text(TranslationHelper.translate("shuffle", settings.language)) },
                trailingContent = {
                    Switch(
                        checked = settings.autoChangerShuffle,
                        onCheckedChange = { shuffle ->
                            viewModel.setAutoChanger(
                                enabled = true,
                                interval = settings.autoChangerIntervalMinutes,
                                shuffle = shuffle,
                                wifiOnly = settings.autoChangerWifiOnly
                            )
                        }
                    )
                }
            )

            ListItem(
                headlineContent = { Text(TranslationHelper.translate("wifi_only", settings.language)) },
                trailingContent = {
                    Switch(
                        checked = settings.autoChangerWifiOnly,
                        onCheckedChange = { wifiOnly ->
                            viewModel.setAutoChanger(
                                enabled = true,
                                interval = settings.autoChangerIntervalMinutes,
                                shuffle = settings.autoChangerShuffle,
                                wifiOnly = wifiOnly
                            )
                        }
                    )
                }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Notifications Setting
        ListItem(
            headlineContent = { Text("Daily Notification Reminder") },
            trailingContent = {
                Switch(
                    checked = settings.isNotificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications() }
                )
            }
        )

    }
}

@Composable
fun WallpaperGridCard(wall: WallpaperEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("wallpaper_card_${wall.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16181C))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperImage(
                imageResName = wall.imageResName,
                colors = wall.colors,
                isAnimatedLive = wall.isLive
            )

            // Info tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wall.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = wall.category,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (wall.isLive) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF3B82F6).copy(alpha = 0.3f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = "LIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (wall.is4K) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = "4K",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
