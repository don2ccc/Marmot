package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.InlineData
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.api.StoryAnalysisResponse
import com.example.data.DataRepository
import com.example.data.ExtractedPhraseJson
import com.example.data.PassageEntity
import com.example.data.PhraseEntity
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale

sealed interface AnalysisState {
    object Idle : AnalysisState
    data class Loading(val message: String) : AnalysisState
    data class Success(val passageId: Long, val title: String) : AnalysisState
    data class Error(val message: String) : AnalysisState
}

// Interactive Quiz States
sealed interface QuizState {
    object Setup : QuizState
    data class Active(
        val questions: List<QuizQuestion>,
        val currentIndex: Int,
        val score: Int,
        val phraseBeingTested: PhraseEntity,
        val wordBank: List<String>, // Words for Sentence Reconstruction
        val currentBuild: List<String> // Current words taped by child
    ) : QuizState
    data class Finished(
        val score: Int,
        val totalQuestions: Int,
        val starsEarned: Int
    ) : QuizState
}

data class QuizQuestion(
    val phrase: PhraseEntity,
    val type: QuizType,
    val prompt: String,
    val correctAnswer: String,
    val options: List<String> // Choice options or shuffled words
)

enum class QuizType {
    MEANING_MATCH,     // Pick the correct Chinese meaning
    SENTENCE_BUILDER   // Tap words in order to form the example sentence
}

class PhraseBuddyViewModel(
    private val repository: DataRepository,
    context: Context
) : ViewModel() {

    // --- Core states ---
    val passages: StateFlow<List<PassageEntity>> = repository.allPassages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPhrases: StateFlow<List<PhraseEntity>> = repository.allPhrases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Setup)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _activePassagePhrases = MutableStateFlow<List<PhraseEntity>>(emptyList())
    val activePassagePhrases: StateFlow<List<PhraseEntity>> = _activePassagePhrases.asStateFlow()

    private val _selectedPassage = MutableStateFlow<PassageEntity?>(null)
    val selectedPassage: StateFlow<PassageEntity?> = _selectedPassage.asStateFlow()

    // Android Text To Speech
    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // --- API Custom Config Persistence Settings ---
    private val prefs = context.getSharedPreferences("api_settings", Context.MODE_PRIVATE)

    private val _useCustomApi = MutableStateFlow(false)
    val useCustomApi = _useCustomApi.asStateFlow()

    private val _apiProvider = MutableStateFlow("Gemini")
    val apiProvider = _apiProvider.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey = _customApiKey.asStateFlow()

    private val _customBaseUrl = MutableStateFlow("")
    val customBaseUrl = _customBaseUrl.asStateFlow()

    private val _customModelName = MutableStateFlow("")
    val customModelName = _customModelName.asStateFlow()

    init {
        // Load API configs
        _useCustomApi.value = prefs.getBoolean("use_custom_api", false)
        _apiProvider.value = prefs.getString("api_provider", "Gemini") ?: "Gemini"
        _customApiKey.value = prefs.getString("custom_api_key", "") ?: ""
        _customBaseUrl.value = prefs.getString("custom_base_url", "") ?: ""
        _customModelName.value = prefs.getString("custom_model_name", "") ?: ""

        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    _isTtsReady.value = true
                }
            }
        }
    }

    fun saveApiConfig(
        useCustom: Boolean,
        provider: String,
        apiKey: String,
        baseUrl: String,
        modelName: String
    ) {
        prefs.edit().apply {
            putBoolean("use_custom_api", useCustom)
            putString("api_provider", provider)
            putString("custom_api_key", apiKey)
            putString("custom_base_url", baseUrl)
            putString("custom_model_name", modelName)
            apply()
        }
        _useCustomApi.value = useCustom
        _apiProvider.value = provider
        _customApiKey.value = apiKey
        _customBaseUrl.value = baseUrl
        _customModelName.value = modelName
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    fun speak(text: String) {
        if (_isTtsReady.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "phrase_buddy_tts")
        }
    }

    // --- Operations ---

    fun cleanAnalysisState() {
        _analysisState.value = AnalysisState.Idle
    }

    fun selectPassage(passage: PassageEntity?) {
        _selectedPassage.value = passage
        if (passage != null) {
            viewModelScope.launch {
                repository.getPhrasesForPassage(passage.id).collect {
                    _activePassagePhrases.value = it
                }
            }
        } else {
            _activePassagePhrases.value = emptyList()
        }
    }

    fun markPhraseLearned(phrase: PhraseEntity, isLearned: Boolean) {
        viewModelScope.launch {
            repository.updatePhrase(phrase.copy(isLearned = isLearned))
        }
    }

    fun updatePhraseRating(phrase: PhraseEntity, rating: Int) {
        viewModelScope.launch {
            repository.updatePhrase(phrase.copy(starRating = rating))
        }
    }

    fun deletePassage(passage: PassageEntity) {
        viewModelScope.launch {
            repository.deletePassage(passage)
            if (_selectedPassage.value?.id == passage.id) {
                _selectedPassage.value = null
                _activePassagePhrases.value = emptyList()
            }
        }
    }

    // --- Gemini Analysis Handler ---

    fun analyzePassage(
        textInput: String?,
        imageBitmap: Bitmap?,
        savedImagePath: String? = null
    ) {
        viewModelScope.launch {
            _analysisState.value = AnalysisState.Loading("聪明的巴迪（Buddy）正在仔细阅读中... ✨")

            val provider = _apiProvider.value
            val useCustom = _useCustomApi.value
            val customKey = _customApiKey.value
            val customBase = _customBaseUrl.value
            val customModel = _customModelName.value

            val apiKey = if (useCustom) customKey else BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                val errorMsg = if (useCustom) {
                    "哦不！未配置自定义 API Key 噢！请在设置界面配置有效 API Key，让巴迪获得魔法力量吧！🐾"
                } else {
                    "哦不！未配置 Gemini API Key噢！请前往 AI Studio 的 Secrets 选项卡，添加名为 GEMINI_API_KEY 的密钥，让巴迪获得魔法力量吧！🐾"
                }
                _analysisState.value = AnalysisState.Error(errorMsg)
                return@launch
            }

            try {
                val hasImage = imageBitmap != null
                val userPrompt = if (hasImage) {
                    "Please read the English passage shown in this textbook picture, transcribe its full text, analyze its content, and extract crucial phrasal verbs, idioms, or collocations that are extremely beneficial for low-grade elementary school kids."
                } else {
                    "Please analyze this English passage: \"$textInput\" and extract the core phrasal verbs, idioms, or useful expressions for low-grade young children."
                }

                val systemPrompt = """
                    You are a lovely and super enthusiastic English learning helper avatar named 'Buddy (巴迪)'. 
                    Your job is to read English short stories or photos of textbook passages for low-grade primary school kids and analyze them.
                    You must extract all the useful collocations, phrasal verbs, idioms, or core compound structures (短语/词组) inside, and provide a cute, kid-friendly analysis for young learners (aged 6 to 10).
                    
                    For EACH extracted phrase, you MUST provide:
                    1. The phrase itself.
                    2. Clear, simple Chinese translation suitable for young kids. Be lively!
                    3. A cute, simple example sentence using this phrase, with vocabulary and stories suitable for a primary school student. Keep it clean and short.
                    4. The Chinese translation of the example sentence.
                    5. A warm "cute context explanation" written in the tone of a friendly talking companion, explaining exactly how kids can use this phrase (for example, "when you look after kittens", etc.).
                    
                    You MUST return a JSON block EXACTLY matching the following structure and NOTHING else. No markdown formatting, no code tags, just raw JSON:
                    {
                      "storyTitle": "The name of the scanned story (or generate a lovely kid-friendly title if missing)",
                      "passageContent": "The transcribed full English content of the textbook passage (if image) or the cleaned original passage",
                      "phrases": [
                        {
                          "phrase": "phrase word e.g. look after",
                          "meaning": "Chinese meaning e.g. 照顾，照看 🐱",
                          "cuteExample": "Cute short kids sentence e.g. My sister looks after our small white rabbit.",
                          "exampleTranslation": "Chinese example translation e.g. 姐姐正在照顾我们的小白兔。",
                          "cuteContext": "Kid-friendly context e.g. 当你想要表达去细心照顾心爱的小动物、小植物或好朋友时，就可以用到这个超棒的词组呀！🐾"
                        }
                      ]
                    }
                    
                    Do not add any reasoning, explanations, or backticks before or after the JSON. Provide only valid JSON.
                """.trimIndent()

                val rawJson = if (useCustom && provider != "Gemini") {
                    // --- Call OpenAI-compatible ChatCompletion endpoint (DeepSeek, Siliconflow, or Custom) ---
                    val targetUrl = if (customBase.isNotBlank()) {
                        if (customBase.endsWith("/")) "${customBase}chat/completions" else "$customBase/chat/completions"
                    } else {
                        when (provider) {
                            "DeepSeek" -> "https://api.deepseek.com/v1/chat/completions"
                            "Siliconflow" -> "https://api.siliconflow.cn/v1/chat/completions"
                            else -> throw Exception("未配置自定义 API 的 Base URL，请前往设置配置！")
                        }
                    }

                    val model = if (customModel.isNotBlank()) customModel else {
                        when (provider) {
                            "DeepSeek" -> "deepseek-chat"
                            "Siliconflow" -> "deepseek-ai/DeepSeek-V3"
                            else -> "gpt-4o"
                        }
                    }

                    val authHeader = "Bearer $apiKey"

                    // Build standard messages list using dynamic maps (safe for Moshi serialization)
                    val messagesList = mutableListOf<Map<String, Any>>()
                    messagesList.add(mapOf("role" to "system", "content" to systemPrompt))

                    if (imageBitmap == null) {
                        messagesList.add(mapOf("role" to "user", "content" to userPrompt))
                    } else {
                        // Image payload in standard OpenAI layout
                        val base64Image = withContext(Dispatchers.IO) { imageBitmap.toBase64() }
                        val contentParts = listOf(
                            mapOf("type" to "text", "text" to userPrompt),
                            mapOf("type" to "image_url", "image_url" to mapOf("url" to "data:image/jpeg;base64,$base64Image"))
                        )
                        messagesList.add(mapOf("role" to "user", "content" to contentParts))
                    }

                    val requestMap = mutableMapOf<String, Any>(
                        "model" to model,
                        "messages" to messagesList,
                        "temperature" to 0.4f
                    )

                    // Enable response format: json_object if supported by DeepSeek/OpenAI
                    if (provider == "DeepSeek" || provider == "Custom") {
                        requestMap["response_format"] = mapOf("type" to "json_object")
                    }

                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.openAiService.chatCompletions(targetUrl, authHeader, requestMap)
                    }

                    response.choices?.firstOrNull()?.message?.content
                        ?: throw Exception("巴迪在云端走神了（服务商没有返回任何消息），请再试一次吧！")

                } else {
                    // --- Call standard Gemini (default or custom key/credentials) ---
                    val model = if (useCustom && customModel.isNotBlank()) customModel else "gemini-3.5-flash"
                    val serviceUrl = if (useCustom && customBase.isNotBlank()) {
                        val cleanBase = if (customBase.endsWith("/")) customBase else "$customBase/"
                        "${cleanBase}v1beta/models/$model:generateContent"
                    } else {
                        "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
                    }

                    val parts = mutableListOf<Part>()
                    parts.add(Part(text = userPrompt))

                    if (imageBitmap != null) {
                        val base64 = withContext(Dispatchers.IO) { imageBitmap.toBase64() }
                        parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64)))
                    }

                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = parts)),
                        generationConfig = GenerationConfig(
                            responseMimeType = "application/json",
                            temperature = 0.4f
                        ),
                        systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                    )

                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.service.generateContent(serviceUrl, apiKey, request)
                    }

                    response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: throw Exception("巴迪在云端走神了，没能给您提炼出短语。请再试一次吧！")
                }

                Log.d("PhraseBuddy", "Raw AI response: $rawJson")

                // Clean json tags if the model returned them despite system prompt
                val cleanJson = rawJson
                    .trim()
                    .removePrefix("```json")
                    .removeSuffix("```")
                    .trim()

                val adapter: JsonAdapter<StoryAnalysisResponse> =
                    RetrofitClient.moshiInstance.adapter(StoryAnalysisResponse::class.java)

                val analyzedData = withContext(Dispatchers.Default) {
                    adapter.fromJson(cleanJson)
                } ?: throw Exception("JSON解析失败，请检查排版并重试！")

                if (analyzedData.phrases.isEmpty()) {
                    throw Exception("这篇短文里似乎没有识别出高价值的短语，试试选一篇更生动好玩的文章吧！")
                }

                // Saving to database in Repository pattern
                val savedId = repository.savePassageAndPhrases(
                    title = analyzedData.storyTitle,
                    content = analyzedData.passageContent,
                    imagePath = savedImagePath,
                    extractedPhrases = analyzedData.phrases.map {
                        ExtractedPhraseJson(
                            phrase = it.phrase,
                            meaning = it.meaning,
                            cuteExample = it.cuteExample,
                            exampleTranslation = it.exampleTranslation,
                            cuteContext = it.cuteContext
                        )
                    }
                )

                // Select this passage instantly to present
                val newlyCreated = repository.getPassageById(savedId)
                selectPassage(newlyCreated)

                _analysisState.value = AnalysisState.Success(savedId, analyzedData.storyTitle)

            } catch (e: Exception) {
                e.printStackTrace()
                _analysisState.value = AnalysisState.Error("扫描遇到了困难，巴迪对您说：" + (e.localizedMessage ?: "网络通道有点不听话哦，请再试试~ 🌈"))
            }
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // --- Interactive Sentence Builder & Phrase Quiz Game Engine ---
    // Focuses on context, re-building the cute examples, motivating the child!

    fun startNewQuiz() {
        viewModelScope.launch {
            _quizState.value = QuizState.Setup
            val dbPhrases = repository.getRandomPhrasesSync(5)
            
            // Fallback default dictionary if database is dry (brand new app launch!)
            val sourceList = if (dbPhrases.isNotEmpty()) {
                dbPhrases
            } else {
                getDemoPhrasesForFreshLaunch()
            }

            // Let's engineer 5 questions!
            val questions = sourceList.mapIndexed { index, phrase ->
                // Alternates between Meaning Matching & Sentence Construction
                val quizType = if (index % 2 == 0) QuizType.MEANING_MATCH else QuizType.SENTENCE_BUILDER
                if (quizType == QuizType.MEANING_MATCH) {
                    // Create 3 incorrect options from default bank or other phrases
                    val distractors = mutableListOf<String>()
                    distractors.add("开会睡觉 😴")
                    distractors.add("飞上蓝天 🦅")
                    distractors.add("高兴地跳舞 💃")
                    
                    // Shuffle distractors with correct answer
                    val options = (distractors.shuffled().take(2) + phrase.meaning).shuffled()

                    QuizQuestion(
                        phrase = phrase,
                        type = QuizType.MEANING_MATCH,
                        prompt = "词组 \"${phrase.phrase}\" 是什么意思呢？快帮巴迪选一选！🐾",
                        correctAnswer = phrase.meaning,
                        options = options
                    )
                } else {
                    // Shuffled words for Sentence Builder!
                    // Let's split sentence elements. We should trim punctuation.
                    val cleanedSentence = phrase.cuteExample
                        .replace(".", "")
                        .replace(",", "")
                        .replace("!", "")
                        .replace("?", "")
                    
                    val words = cleanedSentence.split(" ").filter { it.isNotBlank() }
                    val shuffledWords = words.shuffled()

                    QuizQuestion(
                        phrase = phrase,
                        type = QuizType.SENTENCE_BUILDER,
                        prompt = "这句话里用到了 \"${phrase.phrase}\"，快把单词排排队拼出句子吧！✨\n\"${phrase.exampleTranslation}\"",
                        correctAnswer = words.joinToString(" "),
                        options = shuffledWords
                    )
                }
            }

            if (questions.isNotEmpty()) {
                val firstQ = questions.first()
                _quizState.value = QuizState.Active(
                    questions = questions,
                    currentIndex = 0,
                    score = 0,
                    phraseBeingTested = firstQ.phrase,
                    wordBank = firstQ.options,
                    currentBuild = emptyList()
                )
            }
        }
    }

    fun selectMeaningOption(option: String) {
        val current = _quizState.value
        if (current !is QuizState.Active) return

        val question = current.questions[current.currentIndex]
        val isCorrect = option == question.correctAnswer
        val scoreDelta = if (isCorrect) 20 else 0

        // Play positive sound effect visually or TTS
        if (isCorrect) {
            speak("Wonderful!")
        } else {
            speak("Keep trying!")
        }

        progressQuiz(current, scoreDelta)
    }

    // Sentence builder interactions (Adding word to build)
    fun addWordToBuild(word: String) {
        val current = _quizState.value
        if (current !is QuizState.Active) return

        val updatedBuild = current.currentBuild + word
        val updatedBank = current.wordBank.toMutableList().apply { remove(word) }

        _quizState.value = current.copy(
            currentBuild = updatedBuild,
            wordBank = updatedBank
        )

        // Check if fully built
        if (updatedBank.isEmpty()) {
            val builtSentence = updatedBuild.joinToString(" ")
            val question = current.questions[current.currentIndex]
            val isCorrect = builtSentence.equals(question.correctAnswer, ignoreCase = true)
            val scoreDelta = if (isCorrect) 20 else 0

            if (isCorrect) {
                speak("Awesome sentence!")
            } else {
                speak("Nice try!")
            }

            progressQuiz(current.copy(currentBuild = updatedBuild, wordBank = updatedBank), scoreDelta)
        }
    }

    fun resetSentenceBuild() {
        val current = _quizState.value
        if (current !is QuizState.Active) return

        val question = current.questions[current.currentIndex]
        _quizState.value = current.copy(
            wordBank = question.options,
            currentBuild = emptyList()
        )
    }

    private fun progressQuiz(state: QuizState.Active, scoreDelta: Int) {
        viewModelScope.launch {
            val nextIndex = state.currentIndex + 1
            val newScore = state.score + scoreDelta

            if (nextIndex < state.questions.size) {
                val nextQ = state.questions[nextIndex]
                _quizState.value = QuizState.Active(
                    questions = state.questions,
                    currentIndex = nextIndex,
                    score = newScore,
                    phraseBeingTested = nextQ.phrase,
                    wordBank = nextQ.options,
                    currentBuild = emptyList()
                )
            } else {
                // Determine stars based on score (max 100)
                val stars = when {
                    newScore >= 100 -> 3
                    newScore >= 60 -> 2
                    newScore >= 20 -> 1
                    else -> 0
                }
                _quizState.value = QuizState.Finished(
                    score = newScore,
                    totalQuestions = state.questions.size,
                    starsEarned = stars
                )
            }
        }
    }

    private fun getDemoPhrasesForFreshLaunch(): List<PhraseEntity> {
        return listOf(
            PhraseEntity(
                id = 9001,
                passageId = -1,
                phrase = "look after",
                meaning = "照顾，照看 🐰",
                cuteExample = "I look after my small white rabbit every day",
                exampleTranslation = "我每天都在照顾我的小白兔。",
                cuteContext = "这个词组用来表达你心疼小动物、花花草草，或者弟弟妹妹时去细心关照他们哦！",
                isLearned = false
            ),
            PhraseEntity(
                id = 9002,
                passageId = -1,
                phrase = "get up",
                meaning = "起床，起来 ⏰",
                cuteExample = "My fluffy cat hates to get up early",
                exampleTranslation = "我那只胖乎乎的猫咪讨厌早起。",
                cuteContext = "早上太阳公公出来，你从香甜的梦里醒来伸个懒腰，就可以大声喊：It's time to get up!",
                isLearned = false
            ),
            PhraseEntity(
                id = 9003,
                passageId = -1,
                phrase = "run away",
                meaning = "跑掉，逃跑 🏃",
                cuteExample = "The little sneaky mouse runs away",
                exampleTranslation = "那只鬼鬼祟祟的小老鼠溜跑了。",
                cuteContext = "小动物遇到大怪兽，或者小偷由于害怕赶快溜走时，就会嗖的一下 run away！",
                isLearned = false
            ),
            PhraseEntity(
                id = 9004,
                passageId = -1,
                phrase = "pick up",
                meaning = "拾起，捡起 🍎",
                cuteExample = "Please pick up the red apple from the green grass",
                exampleTranslation = "请把绿草地上的红苹果捡起来吧。",
                cuteContext = "小玩具掉地上了，或者帮妈妈捡起地上的小东西，就要用到这个动作词组：pick up！",
                isLearned = false
            ),
            PhraseEntity(
                id = 9005,
                passageId = -1,
                phrase = "play with",
                meaning = "和...一起玩耍 ⚽",
                cuteExample = "I want to play with my clever puppy Buddy",
                exampleTranslation = "我想和聪明的狗宝宝巴迪一起玩游戏。",
                cuteContext = "当你和你的好朋友、心爱的小球或洋娃娃一起度过快乐的时光时，就用 play with 吧！",
                isLearned = false
            )
        )
    }
}

class PhraseBuddyViewModelFactory(
    private val repository: DataRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhraseBuddyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhraseBuddyViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
