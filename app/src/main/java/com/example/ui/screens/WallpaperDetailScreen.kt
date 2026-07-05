package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    viewModel: WallsViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onRelatedClick: (String) -> Unit
) {
    val selectedWall by viewModel.selectedWallpaper.collectAsStateWithLifecycle()
    val allWallpapers by viewModel.allWallpapers.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Screen states
    var showSetSheet by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var previewModeActive by remember { mutableStateOf(false) }
    var previewIsHomeScreen by remember { mutableStateOf(true) } // true = home, false = lock

    // Double tap Zoom level
    var zoomScale by remember { mutableStateOf(1f) }

    if (selectedWall == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val wall = selectedWall!!
    val colorsList = remember(wall.colors) { wall.colors.split(",") }
    val tagsList = remember(wall.tags) { wall.tags.split(",").map { it.trim() } }

    val relatedWallpapers = remember(allWallpapers, wall) {
        allWallpapers.filter { it.category == wall.category && it.id != wall.id }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Wallpaper Rendering with double-tap zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            zoomScale = if (zoomScale == 1f) 1.5f else 1f
                        }
                    )
                }
                .graphicsLayer(
                    scaleX = zoomScale,
                    scaleY = zoomScale
                )
        ) {
            WallpaperImage(
                imageResName = wall.imageResName,
                colors = wall.colors,
                isAnimatedLive = wall.isLive
            )
        }

        // Overlay transparent home screen / lock screen shortcuts to preview wallpaper!
        AnimatedVisibility(
            visible = previewModeActive,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { previewModeActive = false } // Tap anywhere to exit
            ) {
                if (previewIsHomeScreen) {
                    // Home Screen Preview Overlays
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Date Widget
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 48.dp)
                        ) {
                            Text(
                                text = "10:30 AM",
                                color = Color.White,
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                text = "Saturday, July 4",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // App Grid Shortcut Mockups
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ShortcutIcon(icon = Icons.Filled.Mail, label = "Gmail")
                                ShortcutIcon(icon = Icons.Filled.CameraAlt, label = "Camera")
                                ShortcutIcon(icon = Icons.Filled.Map, label = "Maps")
                                ShortcutIcon(icon = Icons.Filled.Settings, label = "Settings")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ShortcutIcon(icon = Icons.Filled.Phone, label = "Phone")
                                ShortcutIcon(icon = Icons.Filled.Chat, label = "Messages")
                                ShortcutIcon(icon = Icons.Filled.Language, label = "Chrome")
                                ShortcutIcon(icon = Icons.Filled.PlayArrow, label = "YouTube")
                            }
                        }
                    }
                } else {
                    // Lock Screen Preview Overlays
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Large Centered Clock
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 80.dp)
                        ) {
                            Text(
                                text = "10:30",
                                color = Color.White,
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Thin,
                                letterSpacing = (-2).sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Swipe up to unlock",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Quick Lock-screen Shortcuts (Flashlight, Camera)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.FlashlightOn, contentDescription = "Flashlight", tint = Color.White)
                            }

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Camera", tint = Color.White)
                            }
                        }
                    }
                }

                // Selector Switcher at bottom center
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { previewIsHomeScreen = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (previewIsHomeScreen) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Home Screen")
                    }

                    Button(
                        onClick = { previewIsHomeScreen = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!previewIsHomeScreen) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Lock Screen")
                    }
                }

                Text(
                    text = "TAP ANYWHERE TO EXIT PREVIEW",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }

        // Action Top Bar (Floating)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Interactive Preview Toggle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { previewModeActive = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Visibility, contentDescription = "Preview", tint = Color.White)
                }

                // Edit Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onEditClick() }
                        .testTag("detail_edit_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White)
                }
            }
        }

        // Details Bottom Sheet Style Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title & Favorite button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = wall.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = wall.category,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Interactive Heart Icon
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(wall.id)
                            Toast.makeText(
                                context,
                                if (wall.isFavorite) "Removed from favorites" else "Added to favorites!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (wall.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (wall.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Set Wallpaper Action Call to Action
                Button(
                    onClick = { showSetSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("apply_wallpaper_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Wallpaper, contentDescription = "Apply")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = TranslationHelper.translate("set_wallpaper", settings.language),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Action Grid: Download, Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isDownloading = true
                                downloadProgress = 0f
                                while (downloadProgress < 1f) {
                                    delay(100)
                                    downloadProgress += 0.1f
                                }
                                viewModel.downloadWallpaper(wall.id)
                                isDownloading = false
                                Toast.makeText(context, "Wallpaper downloaded to Album!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_download_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isDownloading
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Download")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(TranslationHelper.translate("download", settings.language))
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Check out this breathtaking 4K wallpaper ' ${wall.title} ' on Walls Engine! 100% Free!")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Wallpaper"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(TranslationHelper.translate("share", settings.language))
                    }
                }

                // Downloading Progress Bar
                if (isDownloading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column {
                        LinearProgressIndicator(
                            progress = downloadProgress,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Downloading...", fontSize = 11.sp, color = Color.Gray)
                            Text("${(downloadProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Stats Section
                Text(
                    text = TranslationHelper.translate("wallpaper_details", settings.language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailStat(label = "Resolution", value = wall.resolution)
                    DetailStat(label = "File Size", value = wall.fileSize)
                    DetailStat(label = "Type", value = if (wall.isLive) "Live/GIF" else "Static PNG")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color Palette Searchable pills
                Text(
                    text = TranslationHelper.translate("color_palette", settings.language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorsList.forEach { colorHex ->
                        val color = remember(colorHex) { Color(android.graphics.Color.parseColor(colorHex.trim())) }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable {
                                    viewModel.selectColorFilter(colorHex)
                                    viewModel.search("")
                                    Toast
                                        .makeText(context, "Filtered by $colorHex palette!", Toast.LENGTH_SHORT)
                                        .show()
                                    onBackClick()
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tag Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tagsList.forEach { tag ->
                        SuggestionChip(
                            onClick = {
                                viewModel.selectTag(tag)
                                Toast.makeText(context, "Search filtered to: #$tag", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            },
                            label = { Text("#$tag") }
                        )
                    }
                }

                // Related Wallpapers Scroll
                if (relatedWallpapers.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        text = TranslationHelper.translate("related_wallpapers", settings.language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(relatedWallpapers) { rw ->
                            Card(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onRelatedClick(rw.id) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                WallpaperImage(
                                    imageResName = rw.imageResName,
                                    colors = rw.colors,
                                    isAnimatedLive = rw.isLive
                                )
                            }
                        }
                    }
                }
            }
        }

        // Applying Wallpaper loading overlay
        if (isApplying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Configuring rendering engine...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Setting wallpaper surface...",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    // Modal Set Wallpaper Sheet Option dialog
    if (showSetSheet) {
        AlertDialog(
            onDismissRequest = { showSetSheet = false },
            title = { Text(TranslationHelper.translate("set_wallpaper", settings.language), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Where would you like to set this wallpaper?", modifier = Modifier.padding(bottom = 16.dp))
                    SetOptionItem(
                        icon = Icons.Filled.Home,
                        title = TranslationHelper.translate("set_home", settings.language),
                        onClick = {
                            showSetSheet = false
                            coroutineScope.launch {
                                isApplying = true
                                try {
                                    applyWallpaperToDevice(context, wall, WallpaperManager.FLAG_SYSTEM)
                                    Toast.makeText(context, "Home Screen Wallpaper set successfully!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to apply wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isApplying = false
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SetOptionItem(
                        icon = Icons.Filled.Lock,
                        title = TranslationHelper.translate("set_lock", settings.language),
                        onClick = {
                            showSetSheet = false
                            coroutineScope.launch {
                                isApplying = true
                                try {
                                    applyWallpaperToDevice(context, wall, WallpaperManager.FLAG_LOCK)
                                    Toast.makeText(context, "Lock Screen Wallpaper set successfully!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to apply lock wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isApplying = false
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SetOptionItem(
                        icon = Icons.Filled.Smartphone,
                        title = TranslationHelper.translate("set_both", settings.language),
                        onClick = {
                            showSetSheet = false
                            coroutineScope.launch {
                                isApplying = true
                                try {
                                    applyWallpaperToDevice(context, wall, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                                    Toast.makeText(context, "Home & Lock Screen Wallpapers applied!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to apply wallpapers: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isApplying = false
                                }
                            }
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSetSheet = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SetOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ShortcutIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailStat(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

suspend fun applyWallpaperToDevice(
    context: android.content.Context,
    wallpaper: WallpaperEntity,
    flag: Int
) {
    try {
        val wallpaperManager = WallpaperManager.getInstance(context)
        var bitmap: Bitmap? = null

        val imageResName = wallpaper.imageResName
        if (imageResName.startsWith("http://") || imageResName.startsWith("https://")) {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageResName)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                if (drawable is android.graphics.drawable.BitmapDrawable) {
                    bitmap = drawable.bitmap
                }
            }
        } else {
            val drawableResId = when (imageResName) {
                "img_app_icon_1783227864457" -> R.drawable.img_app_icon_1783227864457
                "img_wallpaper_ai_art_1783227889331" -> R.drawable.img_wallpaper_ai_art_1783227889331
                "img_wallpaper_cyberpunk_1783227903579" -> R.drawable.img_wallpaper_cyberpunk_1783227903579
                "img_wallpaper_space_1783227916508" -> R.drawable.img_wallpaper_space_1783227916508
                else -> null
            }

            if (drawableResId != null) {
                bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
            } else {
                val colorsList = try {
                    wallpaper.colors.split(",").map { android.graphics.Color.parseColor(it.trim()) }
                } catch (e: Exception) {
                    listOf(android.graphics.Color.BLUE, android.graphics.Color.CYAN, android.graphics.Color.BLACK)
                }

                val width = 1440
                val height = 2560
                val generatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(generatedBitmap)
                val paint = Paint().apply { isAntiAlias = true }

                val shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    colorsList.toIntArray(),
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.shader = shader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                bitmap = generatedBitmap
            }
        }

        if (bitmap != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, flag)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
        } else {
            throw Exception("Failed to decode or generate wallpaper image")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
