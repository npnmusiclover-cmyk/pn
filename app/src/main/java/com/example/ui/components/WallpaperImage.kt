package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R

@Composable
fun WallpaperImage(
    imageResName: String,
    modifier: Modifier = Modifier,
    colors: String = "#3F51B5,#00E5FF,#212121",
    blurRadius: Float = 0f,
    brightness: Float = 0f, // -100 to 100
    contrast: Float = 1f, // 0.5 to 2.0
    saturation: Float = 1f, // 0.0 to 2.0
    activeFilter: String = "None", // "None", "Sepia", "Noir", "Vintage", "Neon", "Cold", "Warm"
    overlayText: String = "",
    textColorHex: String = "#FFFFFF",
    textSizeSp: Float = 24f,
    activeSticker: String = "", // "None", "Star", "Heart", "Flame", "Crown", "Sparkles"
    isAnimatedLive: Boolean = false
) {
    val context = LocalContext.current

    // Resolve physical drawable resource
    val drawableResId = when (imageResName) {
        "img_app_icon_1783227864457" -> R.drawable.img_app_icon_1783227864457
        "img_wallpaper_ai_art_1783227889331" -> R.drawable.img_wallpaper_ai_art_1783227889331
        "img_wallpaper_cyberpunk_1783227903579" -> R.drawable.img_wallpaper_cyberpunk_1783227903579
        "img_wallpaper_space_1783227916508" -> R.drawable.img_wallpaper_space_1783227916508
        else -> null
    }

    // Apply color matrices
    val finalColorFilter = remember(brightness, contrast, saturation, activeFilter) {
        // Brightness matrix
        val bOffset = brightness // Add directly to channels
        val bMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, bOffset,
            0f, 1f, 0f, 0f, bOffset,
            0f, 0f, 1f, 0f, bOffset,
            0f, 0f, 0f, 1f, 0f
        ))

        // Contrast matrix
        val cScale = contrast
        val translate = (-0.5f * cScale + 0.5f) * 255f
        val cMatrix = ColorMatrix(floatArrayOf(
            cScale, 0f, 0f, 0f, translate,
            0f, cScale, 0f, 0f, translate,
            0f, 0f, cScale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))

        // Saturation matrix
        val sMatrix = ColorMatrix().apply { setToSaturation(saturation) }

        // Filter matrix
        val fMatrix = when (activeFilter) {
            "Sepia" -> ColorMatrix(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            "Noir" -> ColorMatrix().apply { setToSaturation(0f) }
            "Vintage" -> ColorMatrix(floatArrayOf(
                0.9f, 0.1f, 0f, 0f, 10f,
                0f, 0.8f, 0.2f, 0f, 5f,
                0f, 0f, 0.6f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            "Neon" -> ColorMatrix(floatArrayOf(
                1.3f, 0f, 0.5f, 0f, -10f,
                0f, 1.3f, 0.5f, 0f, -10f,
                0.5f, 0f, 2.0f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            ))
            "Cold" -> ColorMatrix(floatArrayOf(
                0.8f, 0f, 0f, 0f, 0f,
                0f, 0.9f, 0f, 0f, 0f,
                0f, 0f, 1.3f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            ))
            "Warm" -> ColorMatrix(floatArrayOf(
                1.3f, 0f, 0f, 0f, 15f,
                0f, 1.0f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            ))
            else -> ColorMatrix()
        }

        // Combine matrices
        val combined = ColorMatrix()
        combined.timesAssign(bMatrix)
        combined.timesAssign(cMatrix)
        combined.timesAssign(sMatrix)
        combined.timesAssign(fMatrix)

        ColorFilter.colorMatrix(combined)
    }

    // Live animation parameters
    val infiniteTransition = rememberInfiniteTransition(label = "LiveWallpaper")
    val liveOffset by if (isAnimatedLive) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "LiveOffset"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val liveScale by if (isAnimatedLive) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "LiveScale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (blurRadius > 0) Modifier.blur(blurRadius.dp) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (imageResName.startsWith("http://") || imageResName.startsWith("https://")) {
            // Render remote URL image from Firebase
            AsyncImage(
                model = imageResName,
                contentDescription = "Wallpaper background",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = liveScale,
                        scaleY = liveScale,
                        rotationZ = if (isAnimatedLive) (liveOffset / 30f) else 0f
                    ),
                contentScale = ContentScale.Crop,
                colorFilter = finalColorFilter
            )
        } else if (drawableResId != null) {
            // Render physical high-res image
            androidx.compose.foundation.Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Wallpaper background",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = liveScale,
                        scaleY = liveScale,
                        rotationZ = if (isAnimatedLive) (liveOffset / 30f) else 0f
                    ),
                contentScale = ContentScale.Crop,
                colorFilter = finalColorFilter
            )
        } else {
            // Parse list of hex colors
            val parsedColors = remember(colors) {
                try {
                    colors.split(",")
                        .map { Color(android.graphics.Color.parseColor(it.trim())) }
                } catch (e: Exception) {
                    listOf(Color(0xFF3F51B5), Color(0xFF00E5FF), Color(0xFF212121))
                }
            }

            // Procedural luxury background canvas with animation support
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = liveScale,
                        scaleY = liveScale
                    )
            ) {
                val brush = Brush.sweepGradient(
                    colors = parsedColors + parsedColors.first(),
                    center = if (isAnimatedLive) {
                        val angleRad = Math.toRadians(liveOffset.toDouble())
                        androidx.compose.ui.geometry.Offset(
                            x = (size.width / 2) + (size.width * 0.15f * Math.cos(angleRad)).toFloat(),
                            y = (size.height / 2) + (size.height * 0.15f * Math.sin(angleRad)).toFloat()
                        )
                    } else {
                        androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    }
                )

                // Draw base gradient
                drawRect(
                    brush = brush,
                    colorFilter = finalColorFilter
                )

                // Add deep noise and layered geometric glows
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.2f),
                        radius = size.width * 0.6f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.2f),
                    radius = size.width * 0.6f,
                    colorFilter = finalColorFilter
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parsedColors.first().copy(alpha = 0.3f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.8f),
                        radius = size.width * 0.8f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.8f),
                    radius = size.width * 0.8f,
                    colorFilter = finalColorFilter
                )
            }
        }

        // Overlay sticker if selected
        if (activeSticker != "None" && activeSticker.isNotEmpty()) {
            val stickerEmoji = when (activeSticker) {
                "Star" -> "⭐️"
                "Heart" -> "❤️"
                "Flame" -> "🔥"
                "Crown" -> "👑"
                "Sparkles" -> "✨"
                "Cat" -> "🐱"
                "Rocket" -> "🚀"
                "Music" -> "🎵"
                else -> ""
            }
            if (stickerEmoji.isNotEmpty()) {
                Text(
                    text = stickerEmoji,
                    fontSize = 60.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Overlay text if specified
        if (overlayText.isNotEmpty()) {
            val parsedTextColor = remember(textColorHex) {
                try {
                    Color(android.graphics.Color.parseColor(textColorHex))
                } catch (e: Exception) {
                    Color.White
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = overlayText,
                    color = parsedTextColor,
                    fontSize = textSizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
