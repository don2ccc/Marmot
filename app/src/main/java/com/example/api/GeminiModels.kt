package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import retrofit2.http.Header
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response Models ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String // Base64 raw bytes
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

// --- Custom Analyzed Output parsed from Gemini ---

@JsonClass(generateAdapter = true)
data class StoryAnalysisResponse(
    @Json(name = "storyTitle") val storyTitle: String,
    @Json(name = "passageContent") val passageContent: String,
    @Json(name = "phrases") val phrases: List<PhraseDetail>
)

@JsonClass(generateAdapter = true)
data class PhraseDetail(
    @Json(name = "phrase") val phrase: String,
    @Json(name = "meaning") val meaning: String,
    @Json(name = "cuteExample") val cuteExample: String,
    @Json(name = "exampleTranslation") val exampleTranslation: String,
    @Json(name = "cuteContext") val cuteContext: String
)

// --- Retrofit Setup ---

interface GeminiApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    @Json(name = "choices") val choices: List<ChatChoice>? = null
)

@JsonClass(generateAdapter = true)
data class ChatChoice(
    @Json(name = "message") val message: ChatChoiceMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatChoiceMessage(
    @Json(name = "role") val role: String? = null,
    @Json(name = "content") val content: String? = null
)

interface OpenAiApiService {
    @POST
    suspend fun chatCompletions(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): ChatCompletionResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    val openAiService: OpenAiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(OpenAiApiService::class.java)
    }

    val moshiInstance: Moshi get() = moshi
}
