package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.TranslationHelper
import com.example.ui.components.WallpaperImage
import com.example.ui.viewmodel.WallsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: WallsViewModel,
    onBackClick: () -> Unit
) {
    val selectedWall by viewModel.selectedWallpaper.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Editor bindings from ViewModel
    val blur by viewModel.editorBlur.collectAsStateWithLifecycle()
    val brightness by viewModel.editorBrightness.collectAsStateWithLifecycle()
    val contrast by viewModel.editorContrast.collectAsStateWithLifecycle()
    val saturation by viewModel.editorSaturation.collectAsStateWithLifecycle()
    val activeFilter by viewModel.editorFilter.collectAsStateWithLifecycle()
    val textOverlay by viewModel.editorText.collectAsStateWithLifecycle()
    val textColor by viewModel.editorTextColor.collectAsStateWithLifecycle()
    val textSize by viewModel.editorTextSize.collectAsStateWithLifecycle()
    val sticker by viewModel.editorSticker.collectAsStateWithLifecycle()

    var customTitle by remember { mutableStateOf("Custom Wallpaper") }
    var isSavingDialogVisible by remember { mutableStateOf(false) }

    if (selectedWall == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(TranslationHelper.translate("editor", settings.language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isSavingDialogVisible = true },
                        modifier = Modifier.testTag("save_editor_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF121212))
        ) {
            // Live Preview Canvas with real-time filters
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                WallpaperImage(
                    imageResName = selectedWall!!.imageResName,
                    colors = selectedWall!!.colors,
                    blurRadius = blur,
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation,
                    activeFilter = activeFilter,
                    overlayText = textOverlay,
                    textColorHex = textColor,
                    textSizeSp = textSize,
                    activeSticker = sticker,
                    isAnimatedLive = selectedWall!!.isLive
                )

                // High Contrast badge for feedback
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "LIVE PREVIEW",
                        color = Color(0xFF00FFCC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Controls list
            Card(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // SLIDERS: Blur, Brightness, Contrast, Saturation
                    Text(
                        text = "Adjustments",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Blur Slider
                    AdjustmentSlider(
                        label = "Blur",
                        value = blur,
                        onValueChange = { viewModel.setEditorBlur(it) },
                        valueRange = 0f..25f,
                        valueLabel = "${blur.toInt()} dp"
                    )

                    // Brightness Slider
                    AdjustmentSlider(
                        label = "Brightness",
                        value = brightness,
                        onValueChange = { viewModel.setEditorBrightness(it) },
                        valueRange = -50f..50f,
                        valueLabel = "${brightness.toInt()}"
                    )

                    // Contrast Slider
                    AdjustmentSlider(
                        label = "Contrast",
                        value = contrast,
                        onValueChange = { viewModel.setEditorContrast(it) },
                        valueRange = 0.5f..2.0f,
                        valueLabel = String.format("%.1f", contrast)
                    )

                    // Saturation Slider
                    AdjustmentSlider(
                        label = "Saturation",
                        value = saturation,
                        onValueChange = { viewModel.setEditorSaturation(it) },
                        valueRange = 0.0f..2.0f,
                        valueLabel = String.format("%.1f", saturation)
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // FILTERS
                    Text(
                        text = TranslationHelper.translate("filters", settings.language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val filtersList = listOf("None", "Sepia", "Noir", "Vintage", "Neon", "Cold", "Warm")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filtersList.forEach { f ->
                            FilterChip(
                                selected = activeFilter == f,
                                onClick = { viewModel.setEditorFilter(f) },
                                label = { Text(f) }
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // TEXT OVERLAY
                    Text(
                        text = TranslationHelper.translate("text_overlay", settings.language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = textOverlay,
                        onValueChange = { viewModel.setEditorText(it) },
                        placeholder = { Text(TranslationHelper.translate("add_text", settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (textOverlay.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Text color chooser
                        Text("Text Color", fontSize = 13.sp, color = Color.Gray)
                        val colorsList = listOf(
                            "#FFFFFF" to Color.White,
                            "#00FFCC" to Color(0xFF00FFCC),
                            "#FF007F" to Color(0xFFFF007F),
                            "#FFFF00" to Color.Yellow,
                            "#00E5FF" to Color(0xFF00E5FF),
                            "#000000" to Color.Black
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            colorsList.forEach { (hex, color) ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            2.dp,
                                            if (textColor == hex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { viewModel.setEditorTextColor(hex) }
                                )
                            }
                        }

                        // Text Size Slider
                        AdjustmentSlider(
                            label = "Text Size",
                            value = textSize,
                            onValueChange = { viewModel.setEditorTextSize(it) },
                            valueRange = 16f..48f,
                            valueLabel = "${textSize.toInt()} sp"
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // STICKERS
                    Text(
                        text = TranslationHelper.translate("stickers", settings.language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val stickersList = listOf("None", "Star", "Heart", "Flame", "Crown", "Sparkles", "Cat", "Rocket", "Music")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stickersList.forEach { s ->
                            FilterChip(
                                selected = sticker == s,
                                onClick = { viewModel.setEditorSticker(s) },
                                label = { Text(s) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Save edited wallpaper dialog
    if (isSavingDialogVisible) {
        AlertDialog(
            onDismissRequest = { isSavingDialogVisible = false },
            title = { Text("Save Edited Wallpaper") },
            text = {
                Column {
                    Text("Choose a name for your custom styled masterpiece:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveEditedWallpaper(customTitle, "Custom")
                        isSavingDialogVisible = false
                        Toast.makeText(context, "Saved to Offline & Custom Wallpapers!", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    },
                    modifier = Modifier.testTag("confirm_save_button")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isSavingDialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp)
            Text(valueLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
