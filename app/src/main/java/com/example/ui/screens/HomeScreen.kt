package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PassageEntity
import com.example.ui.viewmodel.PhraseBuddyViewModel
import com.example.ui.theme.*
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PhraseBuddyViewModel,
    onNavigateToAnalyze: () -> Unit,
    onNavigateToPhraseBook: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val passages by viewModel.passages.collectAsState()
    var showTypeDialog by remember { mutableStateOf(false) }
    var typedText by remember { mutableStateOf("") }
    var typedTitle by remember { mutableStateOf("") }

    // Camera Result Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.analyzePassage(textInput = null, imageBitmap = bitmap, savedImagePath = null)
            onNavigateToAnalyze()
        }
    }

    // Gallery Result Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.analyzePassage(textInput = null, imageBitmap = bitmap, savedImagePath = uri.toString())
                    onNavigateToAnalyze()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Playful colors matching Geometric Balance
    val brushBg = Brush.verticalGradient(
        colors = listOf(GeoBg, Color.White)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Logo Box (Yellow circle matching active logo in geometry theme)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(GeoAccentYellow, RoundedCornerShape(12.dp))
                                .border(1.5.dp, GeoTextDark, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🦁", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "小艾英语助手",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = GeoTextDark
                            )
                            Text(
                                "发现短文中的秘密",
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = GeoTextGray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "API设置",
                            tint = GeoPrimary
                        )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Interactive Cute Mascot section
                item {
                    MascotGreetingCard()
                }

                // Core Scan/Input Controls
                item {
                    ControlCenterPanel(
                        onTakeClick = { cameraLauncher.launch(null) },
                        onGalleryClick = { galleryLauncher.launch("image/*") },
                        onTypeClick = { showTypeDialog = true },
                        onQuizClick = onNavigateToQuiz,
                        onPhraseBookClick = onNavigateToPhraseBook
                    )
                }

                // Try quick textbook stories
                item {
                    Text(
                        "👇 零等待极速分析！选择小学课文体验：",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GeoPrimary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }

                item {
                    QuickDemoSection { title, text ->
                        viewModel.analyzePassage(textInput = text, imageBitmap = null)
                        onNavigateToAnalyze()
                    }
                }

                // Historical Scans
                item {
                    val heading = if (passages.isNotEmpty()) "📜 生词分析档案 (${passages.size})" else "📜 暂无分析记录，快开始吧！"
                    Text(
                        heading,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GeoTextDark,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (passages.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(3.dp, GeoTertiary, RoundedCornerShape(32.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Outlined.PhotoCamera,
                                    contentDescription = "Empty",
                                    tint = GeoTertiary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "给你的英文小课文拍个照，或者点击上方“体验故事”，巴迪会立马为你分析出所有常用短语哦！🐾",
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp,
                                    color = GeoTextMuted,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    items(passages) { passage ->
                        HistoryPassageItem(
                            passage = passage,
                            onClick = {
                                viewModel.selectPassage(passage)
                                onNavigateToAnalyze()
                            },
                            onDelete = {
                                viewModel.deletePassage(passage)
                            }
                        )
                    }
                }
            }
        }

        // Custom Manual Type Dialog
        if (showTypeDialog) {
            Dialog(onDismissRequest = { showTypeDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, Color(0xFFFFCC80), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "🖊️ 读文章记短语",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFE65100),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = typedTitle,
                            onValueChange = { typedTitle = it },
                            placeholder = { Text("故事或课文标题 (选填)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFA726),
                                unfocusedBorderColor = Color(0xFFFFCC80)
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = typedText,
                            onValueChange = { typedText = it },
                            placeholder = { Text("请在此输入或粘贴你想要分析的英语故事、段落或句子...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 8,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFA726),
                                unfocusedBorderColor = Color(0xFFFFCC80)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { showTypeDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("取消", color = Color.Gray)
                            }

                            Button(
                                onClick = {
                                    if (typedText.isNotBlank()) {
                                        showTypeDialog = false
                                        val combinedText = if (typedTitle.isNotBlank()) {
                                            "Title: $typedTitle\nContent: $typedText"
                                        } else {
                                            typedText
                                        }
                                        viewModel.analyzePassage(textInput = combinedText, imageBitmap = null)
                                        typedText = ""
                                        typedTitle = ""
                                        onNavigateToAnalyze()
                                    }
                                },
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                enabled = typedText.isNotBlank()
                            ) {
                                Text("开始分析", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Vector Mascot dog drawn in Canvas!
@Composable
fun CuteMascotCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f

        // Draw ears
        val leftEar = Path().apply {
            moveTo(centerX - 42.dp.toPx(), centerY - 35.dp.toPx())
            quadraticTo(centerX - 70.dp.toPx(), centerY - 70.dp.toPx(), centerX - 65.dp.toPx(), centerY - 10.dp.toPx())
            quadraticTo(centerX - 55.dp.toPx(), centerY + 15.dp.toPx(), centerX - 38.dp.toPx(), centerY - 15.dp.toPx())
            close()
        }
        val rightEar = Path().apply {
            moveTo(centerX + 42.dp.toPx(), centerY - 35.dp.toPx())
            quadraticTo(centerX + 70.dp.toPx(), centerY - 70.dp.toPx(), centerX + 65.dp.toPx(), centerY - 10.dp.toPx())
            quadraticTo(centerX + 55.dp.toPx(), centerY + 15.dp.toPx(), centerX + 38.dp.toPx(), centerY - 15.dp.toPx())
            close()
        }
        drawPath(leftEar, Color(0xFF8D6E63))
        drawPath(rightEar, Color(0xFF8D6E63))

        // Draw main head base (large ellipse)
        drawCircle(
            color = Color(0xFFE4D5C1),
            radius = 45.dp.toPx(),
            center = Offset(centerX, centerY)
        )

        // Cute eye spot (brown patch around one eye)
        drawCircle(
            color = Color(0xFF8D6E63),
            radius = 20.dp.toPx(),
            center = Offset(centerX - 18.dp.toPx(), centerY - 10.dp.toPx())
        )

        // Draw Left Eye
        drawCircle(
            color = Color(0xFF1E2F2F),
            radius = 7.dp.toPx(),
            center = Offset(centerX - 18.dp.toPx(), centerY - 8.dp.toPx())
        )
        // Pupil shine
        drawCircle(
            color = Color.White,
            radius = 2.5f.dp.toPx(),
            center = Offset(centerX - 21.dp.toPx(), centerY - 11.dp.toPx())
        )

        // Draw Right Eye
        drawCircle(
            color = Color(0xFF1E2F2F),
            radius = 7.dp.toPx(),
            center = Offset(centerX + 18.dp.toPx(), centerY - 8.dp.toPx())
        )
        // Pupil shine right
        drawCircle(
            color = Color.White,
            radius = 2.5f.dp.toPx(),
            center = Offset(centerX + 15.dp.toPx(), centerY - 11.dp.toPx())
        )

        // Draw cheeks peach blush
        drawCircle(
            color = Color(0xFFFFB74D).copy(alpha = 0.6f),
            radius = 11.dp.toPx(),
            center = Offset(centerX - 30.dp.toPx(), centerY + 12.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFFFB74D).copy(alpha = 0.6f),
            radius = 11.dp.toPx(),
            center = Offset(centerX + 30.dp.toPx(), centerY + 12.dp.toPx())
        )

        // Cute snout (ellipse)
        drawCircle(
            color = Color(0xFFFBFBFB),
            radius = 15.dp.toPx(),
            center = Offset(centerX, centerY + 15.dp.toPx())
        )

        // Nose (upside down triangle/rounded ellipse)
        drawCircle(
            color = Color(0xFF333333),
            radius = 5.5f.dp.toPx(),
            center = Offset(centerX, centerY + 10.dp.toPx())
        )

        // Tiny smiling mouth
        val mouthPath = Path().apply {
            moveTo(centerX - 8.dp.toPx(), centerY + 18.dp.toPx())
            quadraticTo(centerX - 4.dp.toPx(), centerY + 24.dp.toPx(), centerX, centerY + 18.dp.toPx())
            quadraticTo(centerX + 4.dp.toPx(), centerY + 24.dp.toPx(), centerX + 8.dp.toPx(), centerY + 18.dp.toPx())
        }
        drawPath(
            path = mouthPath,
            color = Color(0xFF333333),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun MascotGreetingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        // Physical Offset Shadow (Geometric Balance style)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 8.dp)
                .background(GeoTertiary, RoundedCornerShape(32.dp))
        )
        // Foreground Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, GeoTertiary, RoundedCornerShape(32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CuteMascotCanvas(
                    modifier = Modifier
                        .size(90.dp)
                        .background(GeoSecondary, CircleShape)
                        .border(2.dp, GeoPrimary.copy(alpha = 0.2f), CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .background(GeoCardYellowBg, RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                            .border(1.5.dp, GeoCardYellowBorder, RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "哈啰，我是巴迪 (Buddy)！汪汪！🐶 我有神妙的“词组识别魔法”哦，快带我读读英文故事吧！✨",
                            fontSize = 13.sp,
                            color = GeoTextDark,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlCenterPanel(
    onTakeClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onTypeClick: () -> Unit,
    onQuizClick: () -> Unit,
    onPhraseBookClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Double main capture buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Camera
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp)
                    .border(3.dp, GeoPrimary, RoundedCornerShape(24.dp))
                    .clickable { onTakeClick() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Camera",
                        tint = GeoPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "拍照读课文",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = GeoPrimary
                    )
                }
            }

            // Gallery
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp)
                    .border(3.dp, GeoPrimary, RoundedCornerShape(24.dp))
                    .clickable { onGalleryClick() },
                colors = CardDefaults.cardColors(containerColor = GeoSecondary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Collections,
                        contentDescription = "Gallery",
                        tint = GeoPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "相册选照片",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = GeoPrimary
                    )
                }
            }
        }

        // Action support rows (Manual entry, PhraseBook, Quiz)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Manual Enter
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .border(2.dp, GeoTertiary, RoundedCornerShape(16.dp))
                    .clickable { onTypeClick() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Keyboard,
                        contentDescription = "Type",
                        tint = GeoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("手动输入", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GeoPrimary)
                }
            }

            // Book review
            Card(
                modifier = Modifier
                    .weight(1.5f)
                    .border(2.dp, GeoCardYellowBorder, RoundedCornerShape(16.dp))
                    .clickable { onPhraseBookClick() },
                colors = CardDefaults.cardColors(containerColor = GeoCardYellowBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Book,
                        contentDescription = "Cards",
                        tint = GeoTextDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("翻翻卡单词本", fontWeight = FontWeight.Black, fontSize = 13.sp, color = GeoTextDark)
                }
            }

            // Quiz game
            Card(
                modifier = Modifier
                    .weight(1.4f)
                    .border(2.dp, GeoCardBlueBorder, RoundedCornerShape(16.dp))
                    .clickable { onQuizClick() },
                colors = CardDefaults.cardColors(containerColor = GeoCardBlueBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.SportsEsports,
                        contentDescription = "Quiz",
                        tint = GeoTextDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("闯关大冒险", fontWeight = FontWeight.Black, fontSize = 13.sp, color = GeoTextDark)
                }
            }
        }
    }
}

@Composable
fun QuickDemoSection(onSelectStory: (String, String) -> Unit) {
    val demos = listOf(
        Triple(
            "🦊 狐狸与葡萄",
            "Color(0xFFFFF3E0)",
            "A hungry fox walked in the garden. He looked at the high tree and saw beautiful purple grapes. He wanted to eat them. He jumped and jumped, but he could not reach for them. At last, he had to give up and walk away. He said, 'Those grapes are sour!'."
        ),
        Triple(
            "🐦 聪明的小乌鸦",
            "Color(0xFFE0F7FA)",
            "A thirsty crow was looking for water. He flew to a small garden and found a big bottle on the table. But there was only a little water inside. He thought of a clever way. He began to pick up small stones and put them in the bottle. More and more stones made the water rise! At last, he drank it and happily flew away."
        ),
        Triple(
            "👦 互助的好朋友",
            "Color(0xFFE8F5E9)",
            "Sam and Tim are best friends. Tim felt very ill today and did not get up early. Sam ran to Tim's house and wanted to cheer up his pal. He sat by Tim and began to look after him with a warm smile, sharing funny comic books. Tim said, 'Thank you! You are the best friend in the world!'."
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        demos.forEach { (title, colorHex, content) ->
            // Parse color safely
            val finalColor = when (title) {
                "🦊 狐狸与葡萄" -> Color(0xFFFFE0B2)
                "🐦 聪明的小乌鸦" -> Color(0xFFB2EBF2)
                else -> Color(0xFFC8E6C9)
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(95.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .clickable { onSelectStory(title, content) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = finalColor)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242),
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.5f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = "Go",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        "立刻试玩",
                        fontSize = 9.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryPassageItem(
    passage: PassageEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, GeoSecondary, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in clean geometric circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GeoSecondary, CircleShape)
                    .border(1.5.dp, GeoPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.MenuBook,
                    contentDescription = "Book",
                    tint = GeoPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    passage.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = GeoTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    passage.content,
                    fontSize = 12.sp,
                    color = GeoTextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = GeoTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Custom extension wrapper/overload to represent icons easily
@Composable
private fun Icon(
    imageIcons: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    modifier: Modifier
) {
    Icon(
        imageVector = imageIcons,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
