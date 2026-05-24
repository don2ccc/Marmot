package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PhraseBuddyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PhraseBuddyViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Key configuration inputs
    val initialUseCustom by viewModel.useCustomApi.collectAsState()
    val initialProvider by viewModel.apiProvider.collectAsState()
    val initialApiKey by viewModel.customApiKey.collectAsState()
    val initialBaseUrl by viewModel.customBaseUrl.collectAsState()
    val initialModelName by viewModel.customModelName.collectAsState()

    var useCustomApi by remember { mutableStateOf(false) }
    var apiProvider by remember { mutableStateOf("Gemini") }
    var apiKey by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }

    // Sync input states once when loaded
    LaunchedEffect(initialUseCustom, initialProvider, initialApiKey, initialBaseUrl, initialModelName) {
        useCustomApi = initialUseCustom
        apiProvider = initialProvider
        apiKey = initialApiKey
        baseUrl = initialBaseUrl
        modelName = initialModelName
    }

    var isKeyVisible by remember { mutableStateOf(false) }

    val brushBg = Brush.verticalGradient(
        colors = listOf(GeoBg, Color.White)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "AI 助手服务配置 ⚙️",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = GeoTextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, GeoSecondary, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🧙‍♂️ 魔法词典加油站",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = GeoPrimary
                        )
                        Text(
                            "默认情况下，小艾英语助手使用内置的 Gemini 服务。为了保障您随时能畅快使用，您可以配置您自己购买的 API 密钥，支持 DeepSeek、Siliconflow 或任何兼容 OpenAI 格式的高速 API！",
                            fontSize = 13.sp,
                            color = GeoTextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Use Custom API Toggle Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, GeoCardYellowBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = GeoCardYellowBg),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "启用自定义 API 密钥",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = GeoTextDark
                            )
                            Text(
                                "开启后将优先使用您设置的 API",
                                fontSize = 12.sp,
                                color = GeoTextMuted
                            )
                        }
                        Switch(
                            checked = useCustomApi,
                            onCheckedChange = { useCustomApi = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GeoPrimary,
                                checkedTrackColor = GeoSecondary
                            )
                        )
                    }
                }

                // Custom API Config Area
                AnimatedVisibility(
                    visible = useCustomApi,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.5.dp, GeoCardBlueBorder, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "🔑 API 参数配置",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = GeoTextDark
                            )

                            // Provider Selector Row
                            Text(
                                "选择服务商:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GeoTextDark
                            )

                            // Quick Selector Buttons
                            val providers = listOf(
                                "Gemini" to "♊ Gemini",
                                "DeepSeek" to "🐋 DeepSeek",
                                "Siliconflow" to "⚡ 硅基流动",
                                "Custom" to "⚙️ 自定义"
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    providers.take(2).forEach { (id, label) ->
                                        val isSelected = apiProvider == id
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) GeoSecondary else GeoBg)
                                                .border(
                                                    if (isSelected) 2.dp else 1.dp,
                                                    if (isSelected) GeoPrimary else GeoTextGray.copy(alpha = 0.3f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    apiProvider = id
                                                    // Set defaults instantly
                                                    when (id) {
                                                        "Gemini" -> {
                                                            baseUrl = ""
                                                            modelName = "gemini-3.5-flash"
                                                        }
                                                        "DeepSeek" -> {
                                                            baseUrl = "https://api.deepseek.com/v1/"
                                                            modelName = "deepseek-chat"
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                label,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) GeoPrimary else GeoTextMuted
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    providers.drop(2).forEach { (id, label) ->
                                        val isSelected = apiProvider == id
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) GeoSecondary else GeoBg)
                                                .border(
                                                    if (isSelected) 2.dp else 1.dp,
                                                    if (isSelected) GeoPrimary else GeoTextGray.copy(alpha = 0.3f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    apiProvider = id
                                                    // Set defaults instantly
                                                    when (id) {
                                                        "Siliconflow" -> {
                                                            baseUrl = "https://api.siliconflow.cn/v1/"
                                                            modelName = "deepseek-ai/DeepSeek-V3"
                                                        }
                                                        "Custom" -> {
                                                            baseUrl = ""
                                                            modelName = ""
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                label,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) GeoPrimary else GeoTextMuted
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // API Key Field
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "API 密钥密钥 (API Key):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GeoTextDark
                                )
                                OutlinedTextField(
                                    value = apiKey,
                                    onValueChange = { apiKey = it },
                                    placeholder = { Text("请输入 API Key (如 sk-...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                            Icon(
                                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isKeyVisible) "隐藏密码" else "显示密码"
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GeoPrimary,
                                        unfocusedBorderColor = GeoTextGray.copy(alpha = 0.4f)
                                    )
                                )
                            }

                            // Base URL Field
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "接口地址 (API Base URL):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GeoTextDark
                                )
                                OutlinedTextField(
                                    value = baseUrl,
                                    onValueChange = { baseUrl = it },
                                    placeholder = {
                                        Text(
                                            when (apiProvider) {
                                                "Gemini" -> "空 (默认官方: https://generativelanguage...)"
                                                "DeepSeek" -> "https://api.deepseek.com/v1/"
                                                "Siliconflow" -> "https://api.siliconflow.cn/v1/"
                                                else -> "https://代理网址/v1/"
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GeoPrimary,
                                        unfocusedBorderColor = GeoTextGray.copy(alpha = 0.4f)
                                    )
                                )
                                if (apiProvider == "Gemini" && baseUrl.isNotBlank()) {
                                    Text(
                                        "提示: 官方 Gemini 默认不需要填写代理，仅在配置反代时填写。",
                                        fontSize = 10.sp,
                                        color = GeoPrimary
                                    )
                                }
                            }

                            // Model Name Field
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "模型名称 (Model Name):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GeoTextDark
                                )
                                OutlinedTextField(
                                    value = modelName,
                                    onValueChange = { modelName = it },
                                    placeholder = {
                                        Text(
                                            when (apiProvider) {
                                                "Gemini" -> "gemini-3.5-flash"
                                                "DeepSeek" -> "deepseek-chat"
                                                "Siliconflow" -> "deepseek-ai/DeepSeek-V3"
                                                else -> "gpt-4o / deepseek-chat"
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GeoPrimary,
                                        unfocusedBorderColor = GeoTextGray.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = {
                        // Validator check
                        if (useCustomApi && apiKey.isBlank()) {
                            Toast.makeText(context, "请填入您的 API 密钥再保存哦！🎒", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // For standard selections, set pre-filled values if empty
                        var savedBaseUrl = baseUrl.trim()
                        var savedModelName = modelName.trim()

                        if (useCustomApi) {
                            if (apiProvider == "Gemini") {
                                if (savedModelName.isEmpty()) savedModelName = "gemini-3.5-flash"
                            } else if (apiProvider == "DeepSeek") {
                                if (savedBaseUrl.isEmpty()) savedBaseUrl = "https://api.deepseek.com/v1/"
                                if (savedModelName.isEmpty()) savedModelName = "deepseek-chat"
                            } else if (apiProvider == "Siliconflow") {
                                if (savedBaseUrl.isEmpty()) savedBaseUrl = "https://api.siliconflow.cn/v1/"
                                if (savedModelName.isEmpty()) savedModelName = "deepseek-ai/DeepSeek-V3"
                            }
                        }

                        viewModel.saveApiConfig(
                            useCustom = useCustomApi,
                            provider = apiProvider,
                            apiKey = apiKey.trim(),
                            baseUrl = savedBaseUrl,
                            modelName = savedModelName
                        )

                        Toast.makeText(context, "巴迪已经为您存好了 AI 秘钥！✨ Let's Go!", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                ) {
                    Text(
                        "保存魔法配置 ✨",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    "巴迪已为所有的密码进行本地安全存储 🔐",
                    fontSize = 11.sp,
                    color = GeoTextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
