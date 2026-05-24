package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhraseEntity
import com.example.ui.viewmodel.PhraseBuddyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseBookScreen(
    viewModel: PhraseBuddyViewModel,
    onNavigateBack: () -> Unit
) {
    val allPhrases by viewModel.allPhrases.collectAsState()
    var filterLearnedOnly by remember { mutableStateOf(false) }

    val filteredList = if (filterLearnedOnly) {
        allPhrases.filter { !it.isLearned }
    } else {
        allPhrases
    }

    val brushBg = Brush.verticalGradient(
        colors = listOf(GeoBg, Color.White)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("翻翻卡单词本 📖", fontWeight = FontWeight.Black, fontSize = 20.sp, color = GeoTextDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GeoPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = GeoBg
                )
            )
        },
        containerColor = GeoBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(brushBg),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter Control Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .border(2.5.dp, GeoTertiary, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.FilterAlt,
                        contentDescription = "Filter",
                        tint = GeoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "筛选未学会词组:",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = GeoTextDark,
                        modifier = Modifier.weight(1f)
                    )

                    // Filter Switch
                    Switch(
                        checked = filterLearnedOnly,
                        onCheckedChange = { filterLearnedOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GeoPrimary,
                            checkedTrackColor = GeoSecondary
                        )
                    )
                }
            }

            // Word Grid Cards
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Inventory,
                            contentDescription = "Empty",
                            tint = Color.LightGray,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (filterLearnedOnly) "哇哦！你太厉害了，所有的词组你都已经掌握学成啦！🎂🐾"
                            else "你的魔法单词本现在还是空空的呢，快去拍照识别几篇短文吧！🌟",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                Text(
                    "💡 点击生词卡，看看巴迪为你设计的可爱少儿例句和中文释义吧：",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Grid Cells responsive (2 column on typical mobile portrait)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(filteredList) { phrase ->
                        FlippableFlashCard(
                            phrase = phrase,
                            onSpeakClick = { text -> viewModel.speak(text) },
                            onLearnedToggle = { isLearned -> viewModel.markPhraseLearned(phrase, isLearned) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlippableFlashCard(
    phrase: PhraseEntity,
    onSpeakClick: (String) -> Unit,
    onLearnedToggle: (Boolean) -> Unit
) {
    var rotated by remember { mutableStateOf(false) }

    // Floating 3D flip animation configuration
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    // Geometric Balance setup
    val isYellow = phrase.phrase.hashCode() % 2 == 0
    val cardBgFront = if (phrase.isLearned) Color(0xFFF1F8E9) else Color.White
    val cardBgBack = if (phrase.isLearned) Color(0xFFF1F8E9) else (if (isYellow) GeoCardYellowBg else GeoCardBlueBg)
    val cardBorder = if (phrase.isLearned) Color(0xFF81C784) else (if (isYellow) GeoCardYellowBorder else GeoCardBlueBorder)
    val labelColor = if (phrase.isLearned) Color(0xFF4CAF50) else (if (isYellow) Color(0xFFF57C00) else Color(0xFF1976D2))
    val phraseTextColor = if (phrase.isLearned) Color(0xFF2E7D32) else (if (isYellow) Color(0xFF795548) else Color(0xFF0D47A1))
    val meaningTextColor = if (phrase.isLearned) Color(0xFF5D4037) else (if (isYellow) Color(0xFF8D6E63) else Color(0xFF1565C0))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable { rotated = !rotated },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // Front Side Card
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        2.5.dp,
                        cardBorder,
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgFront)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top header block (master flags)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (phrase.isLearned) Color(0xFFE8F5E9) else cardBgBack,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                if (phrase.isLearned) "学会啦 🎓" else "学习中 🚀",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = labelColor
                            )
                        }

                        // Pronunciation click
                        IconButton(
                            onClick = {
                                onSpeakClick(phrase.phrase)
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.White.copy(alpha = 0.6f), CircleShape)
                                .border(1.dp, cardBorder, CircleShape)
                        ) {
                            Icon(
                                Icons.Outlined.VolumeUp,
                                contentDescription = "Speak phrase",
                                tint = labelColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Main extracted entry word
                    Text(
                        phrase.phrase,
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = phraseTextColor,
                        textAlign = TextAlign.Center
                    )

                    // Sparkle prompt tips (Geometric rounded outline)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBgBack.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.TouchApp,
                            contentDescription = "Interactive Hint",
                            tint = labelColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "点我翻面看答案 💫",
                            fontSize = 10.sp,
                            color = labelColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Back Side
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
                    .border(2.5.dp, cardBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgBack)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Definition
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "💎 释义:",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = labelColor
                        )
                        Text(
                            phrase.meaning,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = phraseTextColor,
                            maxLines = 1
                        )
                    }

                    HorizontalDivider(color = cardBorder.copy(alpha = 0.4f), thickness = 1.dp)

                    // Child sentence example
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "🌸 糖果例句 (点击发音):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = labelColor,
                            modifier = Modifier.clickable { onSpeakClick(phrase.cuteExample) }
                        )
                        Text(
                            phrase.cuteExample,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = phraseTextColor,
                            fontFamily = FontFamily.Serif,
                            maxLines = 2,
                            modifier = Modifier.clickable { onSpeakClick(phrase.cuteExample) }
                        )
                        Text(
                            phrase.exampleTranslation,
                            fontSize = 10.sp,
                            color = meaningTextColor,
                            maxLines = 2
                        )
                    }

                    // Button to toggle learned
                    Button(
                        onClick = {
                            onLearnedToggle(!phrase.isLearned)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (phrase.isLearned) GeoTertiary else Color(0xFF81C784)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            if (phrase.isLearned) "打回重修 🎒" else "标为学会啦 🎓",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
