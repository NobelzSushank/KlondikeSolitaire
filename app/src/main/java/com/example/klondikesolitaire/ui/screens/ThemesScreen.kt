package com.example.klondikesolitaire.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.klondikesolitaire.viewmodel.AppViewModel

private data class CosmeticOption(
    val id: String,
    val title: String,
    val isPremium: Boolean,
    val brush: Brush
)

private val backgroundOptions = listOf(
    CosmeticOption("default_gradient", "Emerald", false, Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)))),
    CosmeticOption("bg_ocean", "Ocean", false, Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))),
    CosmeticOption("bg_sunset", "Sunset", false, Brush.verticalGradient(listOf(Color(0xFFBF360C), Color(0xFFF57C00)))),
    CosmeticOption("bg_royal", "Royal", false, Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF6A1B9A)))),
    CosmeticOption("bg_aurora", "Aurora", true, Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF26A69A)))),
    CosmeticOption("bg_ruby", "Ruby", true, Brush.verticalGradient(listOf(Color(0xFF880E4F), Color(0xFFC2185B)))),
    CosmeticOption("bg_night", "Night", true, Brush.verticalGradient(listOf(Color(0xFF212121), Color(0xFF424242)))),
    CosmeticOption("bg_gold", "Gold", true, Brush.verticalGradient(listOf(Color(0xFF8D6E63), Color(0xFFD4AF37))))
)

private val backOptions = listOf(
    CosmeticOption("default_back", "Classic", false, Brush.linearGradient(listOf(Color(0xFF1E88E5), Color(0xFF1565C0)))),
    CosmeticOption("classic_back", "Velvet", false, Brush.linearGradient(listOf(Color(0xFF6A1B9A), Color(0xFF4A148C)))),
    CosmeticOption("modern_back", "Modern", false, Brush.linearGradient(listOf(Color(0xFF00897B), Color(0xFF00695C)))),
    CosmeticOption("back_rose", "Rose", false, Brush.linearGradient(listOf(Color(0xFFC2185B), Color(0xFFAD1457)))),
    CosmeticOption("back_carbon", "Carbon", true, Brush.linearGradient(listOf(Color(0xFF37474F), Color(0xFF263238)))),
    CosmeticOption("back_amber", "Amber", true, Brush.linearGradient(listOf(Color(0xFFFF8F00), Color(0xFFFF6F00)))),
    CosmeticOption("back_mint", "Mint", true, Brush.linearGradient(listOf(Color(0xFF26A69A), Color(0xFF00796B)))),
    CosmeticOption("back_luxe", "Luxe", true, Brush.linearGradient(listOf(Color(0xFF6D4C41), Color(0xFFD4AF37))))
)

@Composable
fun ThemesScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit
) {
    val settings by appViewModel.settings.collectAsState()
    val premiumActive = settings.isPremiumSessionActive

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Themes", style = MaterialTheme.typography.headlineSmall)

            PreviewPanel(
                backgroundId = settings.backgroundStyle,
                cardBackId = settings.cardBackStyle,
                faceStyle = settings.faceStyle
            )

            FaceStyleSelector(
                selected = settings.faceStyle,
                onSelect = { appViewModel.setFaceStyle(it) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBack) { Text("Back") }
                if (!premiumActive) {
                    Button(onClick = { appViewModel.startPremiumSession() }) {
                        Text("Start Premium Session")
                    }
                } else {
                    Text(
                        text = "Premium Session Active",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text("Backgrounds", style = MaterialTheme.typography.titleMedium)
            CosmeticsGrid(
                options = backgroundOptions,
                selectedId = settings.backgroundStyle,
                premiumActive = premiumActive,
                onSelect = { appViewModel.setBackgroundStyle(it) }
            )

            Text("Card Backs", style = MaterialTheme.typography.titleMedium)
            CosmeticsGrid(
                options = backOptions,
                selectedId = settings.cardBackStyle,
                premiumActive = premiumActive,
                onSelect = { appViewModel.setCardBackStyle(it) }
            )
        }
    }
}

@Composable
private fun PreviewPanel(
    backgroundId: String,
    cardBackId: String,
    faceStyle: String
) {
    val bg = backgroundOptions.firstOrNull { it.id == backgroundId } ?: backgroundOptions.first()
    val back = backOptions.firstOrNull { it.id == cardBackId } ?: backOptions.first()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg.brush)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 74.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(back.brush)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("◆", color = Color.White)
            }

            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 74.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A♠",
                    color = Color.Black,
                    style = if (faceStyle == "modern_face") {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    }
                )
            }
        }
    }
}

@Composable
private fun FaceStyleSelector(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FaceChip(label = "Classic", selected = selected == "classic_face", onClick = { onSelect("classic_face") }, modifier = Modifier.weight(1f))
        FaceChip(label = "Modern", selected = selected == "modern_face", onClick = { onSelect("modern_face") }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FaceChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CosmeticsGrid(
    options: List<CosmeticOption>,
    selectedId: String,
    premiumActive: Boolean,
    onSelect: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(190.dp)
    ) {
        items(options) { option ->
            val locked = option.isPremium && !premiumActive
            val selected = selectedId == option.id

            Box(
                modifier = Modifier
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(option.brush)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = !locked) { onSelect(option.id) }
                    .padding(6.dp)
            ) {
                Text(
                    text = option.title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.BottomStart)
                )

                if (locked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Premium", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
