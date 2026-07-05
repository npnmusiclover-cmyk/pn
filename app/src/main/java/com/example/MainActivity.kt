package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TranslationHelper
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WallsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        setContent {
            val viewModel: WallsViewModel = viewModel()
            val settings by viewModel.appSettings.collectAsStateWithLifecycle()

            MyApplicationTheme(
                darkTheme = settings.isDarkMode,
                dynamicColor = settings.useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WallsAppOrchestrator(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun WallsAppOrchestrator(viewModel: WallsViewModel) {
    // Basic navigation state
    // "main" -> "detail" -> "editor"
    var currentScreenState by remember { mutableStateOf("main") }
    var bottomNavSelectedTab by remember { mutableStateOf("home") }

    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    Crossfade(targetState = currentScreenState, label = "ScreenTransition") { screen ->
        when (screen) {
            "main" -> {
                Scaffold(
                    bottomBar = {
                        Column {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            NavigationBar(
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("main_bottom_nav"),
                                containerColor = Color(0xFF16181C),
                                tonalElevation = 0.dp
                            ) {
                                val tabs = listOf(
                                    Triple("home", Icons.Filled.Home, Icons.Outlined.Home),
                                    Triple("live", Icons.Filled.Movie, Icons.Outlined.MovieFilter),
                                    Triple("4k", Icons.Filled.HighQuality, Icons.Outlined.HighQuality),
                                    Triple("categories", Icons.Filled.Category, Icons.Outlined.Category),
                                    Triple("favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
                                    Triple("settings", Icons.Filled.Settings, Icons.Outlined.Settings)
                                )

                                tabs.forEach { (tab, filledIcon, outlinedIcon) ->
                                    val isSelected = bottomNavSelectedTab == tab
                                    val label = TranslationHelper.translate(tab, settings.language)
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { bottomNavSelectedTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) filledIcon else outlinedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF60A5FA),
                                            selectedTextColor = Color(0xFF60A5FA),
                                            indicatorColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                            unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                            unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.testTag("nav_tab_$tab")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (bottomNavSelectedTab) {
                            "home" -> HomeScreen(
                                viewModel = viewModel,
                                onWallpaperClick = { id ->
                                    viewModel.setWallpaperId(id)
                                    currentScreenState = "detail"
                                }
                            )
                            "live" -> LiveScreen(
                                viewModel = viewModel,
                                onWallpaperClick = { id ->
                                    viewModel.setWallpaperId(id)
                                    currentScreenState = "detail"
                                }
                            )
                            "4k" -> FourKScreen(
                                viewModel = viewModel,
                                onWallpaperClick = { id ->
                                    viewModel.setWallpaperId(id)
                                    currentScreenState = "detail"
                                }
                            )
                            "categories" -> CategoriesScreen(
                                viewModel = viewModel,
                                onCategoryClick = { category ->
                                    viewModel.selectCategoryFilter(category)
                                    viewModel.search("")
                                    bottomNavSelectedTab = "home"
                                }
                            )
                            "favorites" -> FavoritesScreen(
                                viewModel = viewModel,
                                onWallpaperClick = { id ->
                                    viewModel.setWallpaperId(id)
                                    currentScreenState = "detail"
                                }
                            )
                            "settings" -> SettingsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
            "detail" -> {
                WallpaperDetailScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        currentScreenState = "main"
                        viewModel.setWallpaperId(null)
                    },
                    onEditClick = {
                        currentScreenState = "editor"
                    },
                    onRelatedClick = { id ->
                        viewModel.setWallpaperId(id)
                    }
                )
            }
            "editor" -> {
                EditorScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        currentScreenState = "detail"
                    }
                )
            }
        }
    }
}
