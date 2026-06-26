package com.example.data

import com.example.BuildConfig
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
import java.util.concurrent.TimeUnit

// --- Request/Response Data Classes for Gemini REST API ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<ContentPart>,
    val systemInstruction: ContentPart? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class ContentPart(
    val parts: List<PartText>
)

@JsonClass(generateAdapter = true)
data class PartText(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int? = 2048
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<CandidatePart>?
)

@JsonClass(generateAdapter = true)
data class CandidatePart(
    val content: ContentPart?
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit Client Holder ---

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- High Level AI Client Interface ---

class GeminiAiTutor {
    suspend fun askTutor(prompt: String, systemPrompt: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "AI Tutor is in simulation mode because the Gemini API Key is missing. Set your GEMINI_API_KEY in the Secrets Panel to activate live tutoring!\n\nSimulated Answer: That is an excellent question about '$prompt'. In a live connection, I will provide step-by-step explanations, custom quizzes, coding walkthroughs, and flashcards!"
        }

        val request = GenerateContentRequest(
            contents = listOf(ContentPart(parts = listOf(PartText(text = prompt)))),
            systemInstruction = systemPrompt?.let { ContentPart(parts = listOf(PartText(text = it))) }
        )

        return try {
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            replyText ?: "The AI tutor responded with an empty answer. Please try rephrasing your prompt!"
        } catch (e: Exception) {
            "Error connecting to AI Tutor: ${e.localizedMessage ?: "Unknown connection failure"}. Please check your internet connection or API Key configuration."
        }
    }
}
