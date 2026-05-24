package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhraseEntity
import com.example.ui.viewmodel.AnalysisState
import com.example.ui.viewmodel.PhraseBuddyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    viewModel: PhraseBuddyViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.analysisState.collectAsState()
    val selectedPassage by viewModel.selectedPassage.collectAsState()
    val phrases by viewModel.activePassagePhrases.collectAsState()
    val isTtsReady by viewModel.isTtsReady.collectAsState()

    // Screen BG gradient
    val brushBg = Brush.verticalGradient(
        colors = listOf(GeoBg, Color.White)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("巴迪魔法阅读镜 🧐", fontWeight = FontWeight.Black, fontSize = 20.sp, color = GeoTextDark) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cleanAnalysisState()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back", tint = GeoPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = GeoBg
                )
            )
        },
        containerColor = GeoBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(brushBg)
        ) {
            when (val currState = state) {
                is AnalysisState.Idle -> {
                    // Show current selected passage results if any
                    if (selectedPassage != null) {
                        AnalysisResultSection(
                            title = selectedPassage!!.title,
                            content = selectedPassage!!.content,
                            phrases = phrases,
                            viewModel = viewModel
                        )
                    } else {
                        // Empty
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无正在分析的短文，请回主页挑选一篇文章或拍照哦！", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }

                is AnalysisState.Loading -> {
                    // Fun, animated, kid-friendly Loader!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CuteMascotCanvas(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color(0xFFFFECB3), CircleShape)
                                .border(3.dp, Color(0xFFFFB300), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(color = Color(0xFFFF9800), strokeWidth = 5.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            currState.message,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7D5260),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "巴迪正在把难懂的句子化作香甜的糖果 🍬...",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.Gray
                        )
                    }
                }

                is AnalysisState.Success -> {
                    // Success state, select and render
                    LaunchedEffect(currState.passageId) {
                        viewModel.cleanAnalysisState()
                    }
                }

                is AnalysisState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFFFFEBEE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error Logo",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "呜呜，巴迪迷路了...",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                currState.message,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 13.sp,
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.cleanAnalysisState() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                        ) {
                            Text("返回重试", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisResultSection(
    title: String,
    content: String,
    phrases: List<PhraseEntity>,
    viewModel: PhraseBuddyViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // Book Text Card with Brutalist Geometric offset shadow
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                // Physical balanced offset shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 0.dp, y = 8.dp)
                        .background(GeoTertiary, RoundedCornerShape(32.dp))
                )

                // Front Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, GeoTertiary, RoundedCornerShape(32.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "📖 智能分析课文: $title",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = GeoPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            content,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Serif,
                            color = GeoTextDark,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GeoSecondary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .border(1.dp, GeoSecondary, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "extracted count",
                                tint = GeoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AI 为你挖掘出 ${phrases.size} 个重点词组宝贝！🐾",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoPrimary
                            )
                        }
                    }
                }
            }
        }

        // Phrase Worksheet Card Header
        item {
            Text(
                "✨ 词组放大镜工作表",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF333333)
            )
        }

        // Expanded/Expandable cards
        items(phrases) { phrase ->
            PhraseInteractiveCard(
                phrase = phrase,
                onVoiceClick = { text -> viewModel.speak(text) },
                onLearnedToggle = { isLearned -> viewModel.markPhraseLearned(phrase, isLearned) },
                onRate = { stars -> viewModel.updatePhraseRating(phrase, stars) }
            )
        }
    }
}

@Composable
fun PhraseInteractiveCard(
    phrase: PhraseEntity,
    onVoiceClick: (String) -> Unit,
    onLearnedToggle: (Boolean) -> Unit,
    onRate: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Alternating card styles matching Geometric Balance guidelines
    val isYellow = phrase.phrase.hashCode() % 2 == 0
    val cardBg = if (phrase.isLearned) Color(0xFFF1F8E9) else (if (isYellow) GeoCardYellowBg else GeoCardBlueBg)
    val cardBorder = if (phrase.isLearned) Color(0xFF81C784) else (if (isYellow) GeoCardYellowBorder else GeoCardBlueBorder)
    val labelColor = if (phrase.isLearned) Color(0xFF4CAF50) else (if (isYellow) Color(0xFFF57C00) else Color(0xFF1976D2))
    val phraseTextColor = if (phrase.isLearned) Color(0xFF2E7D32) else (if (isYellow) Color(0xFF795548) else Color(0xFF0D47A1))
    val meaningTextColor = if (phrase.isLearned) Color(0xFF5D4037) else (if (isYellow) Color(0xFF8D6E63) else Color(0xFF1565C0))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (phrase.isLearned) 2.5.dp else 2.dp,
                color = cardBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Phrase title, learn check, expandable caret
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cute status circle indicator
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(labelColor, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (phrase.isLearned) Icons.Outlined.CheckCircle else Icons.Default.Star,
                        contentDescription = "Status icon",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Core phrase text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        phrase.phrase,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = phraseTextColor
                    )
                    Text(
                        phrase.meaning,
                        fontSize = 13.sp,
                        color = meaningTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Audio TTS Speak Button
                IconButton(
                    onClick = { onVoiceClick(phrase.phrase) },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.6f), CircleShape)
                        .border(1.5.dp, cardBorder, CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.VolumeUp,
                        contentDescription = "Pronounce Word",
                        tint = labelColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Checked status
                IconButton(
                    onClick = { onLearnedToggle(!phrase.isLearned) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (phrase.isLearned) Icons.Filled.CheckCircle else Icons.Default.Circle,
                        contentDescription = "learned status indicator",
                        tint = if (phrase.isLearned) Color(0xFF4CAF50) else cardBorder,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Expanded Detail View
            AnimatedVisibility(
                visible = expanded || phrase.isLearned, // Keep visible if learned or expanded
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .border(1.5.dp, cardBorder, RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Kid Example
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "🍭 少儿糖果例句:",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = labelColor
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { onVoiceClick(phrase.cuteExample) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.VolumeUp,
                                    contentDescription = "Speak Example",
                                    tint = labelColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            phrase.cuteExample,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = phraseTextColor,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            phrase.exampleTranslation,
                            fontSize = 12.sp,
                            color = GeoTextGray,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Context Bubble
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBg.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ChatBubble,
                                contentDescription = "Tips",
                                tint = labelColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "什么时候用呢？",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = labelColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            phrase.cuteContext,
                            fontSize = 11.sp,
                            color = meaningTextColor,
                            lineHeight = 15.sp
                        )
                    }

                    // Rating stars setup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "给这个词组点亮星星 ⭐",
                            fontSize = 11.sp,
                            color = GeoTextGray,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..3).forEach { index ->
                                Icon(
                                    imageVector = if (phrase.starRating >= index) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "Star $index",
                                    tint = if (phrase.starRating >= index) GeoAccentYellow else Color.LightGray,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onRate(index) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
