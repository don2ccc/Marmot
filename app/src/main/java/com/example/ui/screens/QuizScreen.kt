package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.viewmodel.PhraseBuddyViewModel
import com.example.ui.viewmodel.QuizState
import com.example.ui.viewmodel.QuizType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: PhraseBuddyViewModel,
    onNavigateBack: () -> Unit
) {
    val quizState by viewModel.quizState.collectAsState()

    val brushBg = Brush.verticalGradient(
        colors = listOf(GeoBg, Color.White)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("句子大冒险闯关 🎮", fontWeight = FontWeight.Black, fontSize = 20.sp, color = GeoTextDark) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(brushBg)
        ) {
            when (val state = quizState) {
                is QuizState.Setup -> {
                    // Welcome screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CuteMascotCanvas(
                            modifier = Modifier
                                .size(130.dp)
                                .background(GeoSecondary, CircleShape)
                                .border(3.dp, GeoPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Buddy 句子积木大挑战!",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = GeoPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "巴迪会根据你分析记录过的词组，出一些有趣的题目哦！不管是选择中文释义，还是把英语单词拼排起名，只要闯关成功，就有闪闪发光的星星勋章等你拿！⭐✨",
                            fontSize = 14.sp,
                            color = GeoTextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.startNewQuiz() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("开启大冒险! 🐾", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }

                is QuizState.Active -> {
                    val currentQuestion = state.questions[state.currentIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Game scoreboard / Progress Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Question count
                            Text(
                                "关卡: ${state.currentIndex + 1} / ${state.questions.size}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = GeoPrimary
                            )

                            // Score Tracker
                            Box(
                                modifier = Modifier
                                    .background(GeoCardYellowBg, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, GeoCardYellowBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "积分: ${state.score} 🌟",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = GeoTextDark
                                )
                            }
                        }

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { (state.currentIndex.toFloat() / state.questions.size.toFloat()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .border(1.dp, GeoPrimary.copy(alpha = 0.3f), CircleShape),
                            color = GeoPrimary,
                            trackColor = Color.White
                        )

                        // Mascot Prompt Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.5.dp, GeoCardBlueBorder, RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = GeoCardBlueBg),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CuteMascotCanvas(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.5.dp, GeoCardBlueBorder, CircleShape)
                                )
                                Column {
                                    Text(
                                        currentQuestion.prompt,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = GeoTextDark,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Rendering Quest Types dynamically
                        if (currentQuestion.type == QuizType.MEANING_MATCH) {
                            // Multiple choice buttons list
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "✨ 选择最合适的一个中文含义吧：",
                                    fontSize = 13.sp,
                                    color = GeoTextDark,
                                    fontWeight = FontWeight.Black
                                )

                                currentQuestion.options.forEach { option ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(64.dp)
                                            .border(2.dp, GeoCardYellowBorder, RoundedCornerShape(20.dp))
                                            .clickable { viewModel.selectMeaningOption(option) },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = GeoCardYellowBg)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                option,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = GeoTextDark
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Sentence reconstruction layout!
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Row to build sentence slots
                                    Text(
                                        "🔨 句子拼拼看 (把地上的积木摆上来吧)：",
                                        fontSize = 12.sp,
                                        color = GeoTextMuted,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Slots visual card
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 90.dp)
                                            .border(2.dp, GeoPrimary, RoundedCornerShape(20.dp)),
                                        colors = CardDefaults.cardColors(containerColor = GeoSecondary),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        // Flow list of already placed words
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp)
                                        ) {
                                            if (state.currentBuild.isEmpty()) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().height(70.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "等待摆放单词积木...",
                                                        fontSize = 13.sp,
                                                        color = GeoTextMuted,
                                                        fontStyle = FontStyle.Italic
                                                    )
                                                }
                                            } else {
                                                // Words elements
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    state.currentBuild.forEach { word ->
                                                        Box(
                                                            modifier = Modifier
                                                                .background(GeoPrimary, RoundedCornerShape(8.dp))
                                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                                        ) {
                                                            Text(
                                                                word,
                                                                fontWeight = FontWeight.Black,
                                                                fontSize = 14.sp,
                                                                color = Color.White,
                                                                fontFamily = FontFamily.Serif
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Actions: Reset building, start over
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.resetSentenceBuild() },
                                            modifier = Modifier.height(34.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoPrimary),
                                            border = androidx.compose.foundation.BorderStroke(1.5.dp, GeoPrimary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("重新排列", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Word bank puzzles options (shuffled bubbles)
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        "🍬 地面上的积木 (点击积木填空)：",
                                        fontSize = 12.sp,
                                        color = GeoTextMuted,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Render options bank
                                        if (state.wordBank.isEmpty()) {
                                            Text(
                                                "全部拼好啦！正在核对中...",
                                                fontSize = 11.sp,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            // Split or flex line. We can draw them side by side
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                state.wordBank.forEach { word ->
                                                    Card(
                                                        modifier = Modifier
                                                            .border(1.5.dp, GeoCardYellowBorder, RoundedCornerShape(12.dp))
                                                            .clickable { viewModel.addWordToBuild(word) },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = CardDefaults.cardColors(containerColor = GeoCardYellowBg)
                                                    ) {
                                                        Text(
                                                            word,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 13.sp,
                                                            color = GeoTextDark,
                                                            fontFamily = FontFamily.Serif
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is QuizState.Finished -> {
                    // Celebration summary screen!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .background(GeoSecondary, CircleShape)
                                .border(3.dp, GeoPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CuteMascotCanvas(modifier = Modifier.size(110.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "恭喜你，大冒险通关成功！🎉",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = GeoPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "你的本次冒险最终得分：",
                            fontSize = 13.sp,
                            color = GeoTextMuted
                        )

                        Text(
                            "${state.score} 分",
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = Color(0xFFF57C00)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Draw golden award stars based on performance
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(1.5.dp, GeoCardYellowBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            (1..3).forEach { rating ->
                                Icon(
                                    imageVector = if (state.starsEarned >= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "Star Reward $rating",
                                    tint = if (state.starsEarned >= rating) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            when (state.starsEarned) {
                                3 -> "太完美了！你现在绝对是英语天才大明星啦！👑🐾🌟"
                                2 -> "非常棒！你和巴迪越来越有默契了，继续保持！🍰🌈"
                                else -> "很棒的尝试！多多拍照分析新文章，我们下次一定会取得3星徽章！👍💪"
                            },
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = GeoTextDark,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, GeoPrimary),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text("返回大厅", fontWeight = FontWeight.Black, color = GeoPrimary)
                            }

                            Button(
                                onClick = { viewModel.startNewQuiz() },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text("再玩一次 🎮", fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
