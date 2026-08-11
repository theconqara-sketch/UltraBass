package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Song
import com.example.player.PlaybackState
import com.example.player.RepeatMode as AppRepeatMode
import com.example.ui.theme.MaterialYouSongPalette

@Composable
fun NowPlayingScreen(
    state: PlaybackState,
    palette: MaterialYouSongPalette,
    onCollapse: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatCycle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onEqualizerPresetChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val song = state.currentSong ?: return
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Background gradient blending the soft album colors
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            palette.secondaryBg,
            palette.dominantBg,
            palette.dominantBg
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(horizontal = 20.dp)
            .testTag("now_playing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Collapse Button, Title, Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceContainer)
                        .testTag("now_playing_collapse_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse Now Playing",
                        tint = palette.iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM ALBUM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = palette.textSecondary
                    )
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.titleSmall,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Song options",
                        tint = palette.iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Center Album Cover
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = palette.primaryAccent.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(palette.surfaceContainer)
            ) {
                if (!song.albumArtUri.isNull_or_empty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(song.albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_default_album_art),
                        contentDescription = "Default Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Song Info & Favorite Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceContainer)
                        .testTag("now_playing_fav_btn")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) palette.primaryAccent else palette.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Track Slider & Time Displays
            val currentPos = state.currentPositionMs.toFloat()
            val totalDur = state.durationMs.coerceAtLeast(1000L).toFloat()
            val progressFraction = (currentPos / totalDur).coerceIn(0f, 1f)

            Slider(
                value = progressFraction,
                onValueChange = { frac ->
                    onSeek((frac * totalDur).toLong())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("now_playing_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = palette.primaryAccent,
                    activeTrackColor = palette.primaryAccent,
                    inactiveTrackColor = palette.primaryAccent.copy(alpha = 0.25f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(state.currentPositionMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Text(
                    text = "-${formatMs((state.durationMs - state.currentPositionMs).coerceAtLeast(0L))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Controls Row (Shuffle, Prev, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(onClick = onShuffleToggle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (state.isShuffle) palette.primaryAccent else palette.textSecondary.copy(alpha = 0.6f)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = palette.iconTint,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Play / Pause Large Pill Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(palette.primaryAccent)
                        .clickable { onPlayPauseToggle() }
                        .testTag("now_playing_main_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = palette.onPrimaryAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = palette.iconTint,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Repeat Mode Button
                IconButton(onClick = onRepeatCycle) {
                    Icon(
                        imageVector = when (state.repeatMode) {
                            AppRepeatMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (state.repeatMode != AppRepeatMode.OFF) palette.primaryAccent else palette.textSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Utla Bass Equalizer & Bass Boost Control Module
            UtlaBassControlCard(
                bassBoostLevel = state.bassBoostLevel,
                selectedPreset = state.equalizerPreset,
                palette = palette,
                onBassBoostChange = onBassBoostChange,
                onEqualizerPresetChange = onEqualizerPresetChange
            )
        }
    }
}

@Composable
fun UtlaBassControlCard(
    bassBoostLevel: Int,
    selectedPreset: String,
    palette: MaterialYouSongPalette,
    onBassBoostChange: (Int) -> Unit,
    onEqualizerPresetChange: (String) -> Unit
) {
    val presets = listOf("Utla Sub", "Deep Bounce", "Vocal Clarity", "Party Bass", "Balanced")

    val pulseTransition = rememberInfiniteTransition(label = "bass_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
        color = palette.surfaceContainer,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(palette.primaryAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Bass Equalizer",
                            tint = palette.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "UTLA BASS BOOST",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Subwoofer Frequency Enhancer",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }

                // Bass Level Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.primaryAccent
                ) {
                    Text(
                        text = "$bassBoostLevel%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.onPrimaryAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subwoofer Bass Level Slider
            Slider(
                value = bassBoostLevel.toFloat(),
                onValueChange = { onBassBoostChange(it.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = palette.primaryAccent,
                    activeTrackColor = palette.primaryAccent,
                    inactiveTrackColor = palette.primaryAccent.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Presets Row
            Text(
                text = "Bass Equalizer Presets:",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(3).forEach { preset ->
                    val isSelected = preset == selectedPreset
                    FilterChip(
                        selected = isSelected,
                        onClick = { onEqualizerPresetChange(preset) },
                        label = {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.primaryAccent,
                            selectedLabelColor = palette.onPrimaryAccent,
                            containerColor = palette.dominantBg.copy(alpha = 0.5f),
                            labelColor = palette.textPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = palette.primaryAccent.copy(alpha = 0.4f),
                            selectedBorderColor = palette.primaryAccent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
