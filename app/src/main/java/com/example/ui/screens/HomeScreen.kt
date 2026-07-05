package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.TranslationHelper
import com.example.ui.components.WallpaperImage
import com.example.ui.viewmodel.WallsViewModel
import com.example.data.WallpaperEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WallsViewModel,
    onWallpaperClick: (String) -> Unit
) {
    val wallpapers by viewModel.allWallpapers.collectAsStateWithLifecycle()
    val filteredWalls by viewModel.filteredWallpapers.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val colorFilter by viewModel.selectedColorFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val activeTag by viewModel.selectedTag.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }
    var voiceSearchListening by remember { mutableStateOf(false) }

    // Carousel state
    val carouselItems = remember(wallpapers) {
        wallpapers.filter { it.is4K || it.isLive }.take(4)
    }

    val categories = listOf(
        "All", "AI Art", "Cyberpunk", "Space", "Nature", "Cars", "Gaming", "Anime", "Minimal", "Neon", "Animals", "Technology", "Abstract"
    )

    val colorPills = listOf(
        "#FF007F" to "Pink",
        "#00FFFF" to "Cyan",
        "#FF8C00" to "Orange",
        "#00FF66" to "Green",
        "#8A2BE2" to "Purple",
        "#000000" to "Black",
        "#FFFFFF" to "White",
        "#FF0000" to "Red"
    )

    val trendingTags = listOf("cosmic", "neon", "future", "minimal", "speed", "live", "aesthetic")

    // Handle voice search simulation
    LaunchedEffect(voiceSearchListening) {
        if (voiceSearchListening) {
            delay(2500)
            voiceSearchListening = false
            // Simulate voice picking "Cyberpunk"
            viewModel.search("Cyberpunk")
            Toast.makeText(context, "Voice Search captured: \"Cyberpunk\"", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern Premium Header with Search & Voice Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = TranslationHelper.translate("app_name", settings.language),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "100% Free • All Assets Unlocked",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FFCC)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Manual trigger refresh simulation
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isRefreshing = true
                                delay(1200)
                                isRefreshing = false
                                Toast.makeText(context, "Feeds Refreshed Successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (query.isNotEmpty() || colorFilter != null || categoryFilter != null || activeTag != null) {
                        TextButton(
                            onClick = {
                                viewModel.search("")
                                viewModel.selectColorFilter(null)
                                viewModel.selectCategoryFilter(null)
                                viewModel.selectTag(null)
                            }
                        ) {
                            Text("Reset", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Elegant Search Bar with Voice Input
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text(TranslationHelper.translate("search_placeholder", settings.language), fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF60A5FA)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { voiceSearchListening = true },
                        modifier = Modifier.testTag("voice_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Voice Search",
                            tint = if (voiceSearchListening) Color.Red else Color(0xFF60A5FA)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedTextColor = Color(0xFFF1F5F9),
                    unfocusedTextColor = Color(0xFFF1F5F9).copy(alpha = 0.8f),
                    focusedLabelColor = Color.White.copy(alpha = 0.5f),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                    focusedLeadingIconColor = Color(0xFF60A5FA),
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.5f),
                    focusedTrailingIconColor = Color(0xFF60A5FA),
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.5f)
                )
            )
        }

        // Voice Search Listening Overlay
        AnimatedVisibility(
            visible = voiceSearchListening,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = TranslationHelper.translate("voice_listening", settings.language),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Scrollable content area
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            
            // Render category tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = if (cat == "All") categoryFilter == null else categoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (cat == "All") viewModel.selectCategoryFilter(null)
                            else viewModel.selectCategoryFilter(cat)
                        },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (query.isEmpty() && colorFilter == null && categoryFilter == null && activeTag == null) {
                // Featured Carousel
                if (carouselItems.isNotEmpty()) {
                    Text(
                        text = "Featured Masterpieces",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(carouselItems) { wall ->
                            FeaturedCarouselCard(wall = wall, onClick = { onWallpaperClick(wall.id) })
                        }
                    }
                }

                // Color Palette quick Search Filters
                Text(
                    text = "Filter by Color Theme",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(colorPills) { (hex, name) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    2.dp,
                                    if (colorFilter == hex) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .clickable { viewModel.selectColorFilter(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorFilter == hex) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Trending Search Tags
                Text(
                    text = "Trending Tags",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trendingTags) { tag ->
                        SuggestionChip(
                            onClick = { viewModel.selectTag(tag) },
                            label = { Text("#$tag") }
                        )
                    }
                }
            }

            // Results / Feed grid title
            Text(
                text = when {
                    query.isNotEmpty() -> "Search Results for \"$query\""
                    categoryFilter != null -> "Collection: $categoryFilter"
                    colorFilter != null -> "Matching Color Schemes"
                    activeTag != null -> "Tag: #$activeTag"
                    else -> "Explore Wallpapers"
                },
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // Dynamic grid list with scrollable contents
            if (filteredWalls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No Wallpapers match your filters.", color = Color.Gray)
                }
            } else {
                // Since nesting scrollables (Vertical LazyGrid inside Column scroll) is unsupported, 
                // we can render elements via manual Row chunks to provide dynamic, seamless layout and infinite feeling!
                val chunkedWalls = remember(filteredWalls) { filteredWalls.chunked(2) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    chunkedWalls.forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            chunk.forEach { wall ->
                                Box(modifier = Modifier.weight(1f)) {
                                    WallpaperGridCard(wall = wall, onClick = { onWallpaperClick(wall.id) })
                                }
                            }
                            if (chunk.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Sponsored Google AdMob Ad simulation banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "SPONSORED AD",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "Enjoying the engine? Support us with a simple click!",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Ad Link",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedCarouselCard(wall: WallpaperEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperImage(
                imageResName = wall.imageResName,
                colors = wall.colors,
                isAnimatedLive = wall.isLive
            )

            // Info glassmorphic banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = wall.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Featured ${wall.category}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    // Luxury Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "PREMIUM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
