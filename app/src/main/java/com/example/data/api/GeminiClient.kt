package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Compress bitmap if large to ensure fast network request
        val maxDimension = 1024
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = Math.min(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height
            )
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun generateHomeworkExplanation(
        prompt: String,
        subject: String,
        imageBitmap: Bitmap? = null,
        language: String = "English",
        useProModel: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing or default. Please configure GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        // Model selection according to gemini-api skill rules
        val model = if (useProModel || subject.equals("Math", ignoreCase = true) || subject.equals("Physics", ignoreCase = true) || subject.equals("Programming", ignoreCase = true)) {
            "gemini-3.1-pro-preview"
        } else {
            "gemini-3.5-flash"
        }

        val url = "$BASE_URL$model:generateContent?key=$apiKey"

        val systemInstructionText = """
            You are an expert AI Homework Assistant & Step-by-Step Educational Tutor.
            Target Subject: $subject
            Preferred Response Language: $language

            Please structure your response cleanly into clear markdown sections:
            ### 💡 Key Concept
            Explain the core concept or formula in 1-2 friendly sentences.

            ### 📝 Step-by-Step Explanation
            Provide clear, logically numbered steps showing how to solve or understand the problem completely.

            ### ✅ Final Answer / Key Takeaway
            Summarize the exact final answer or main result clearly in bold.

            ### 🎯 Practice Check
            Include one quick, related practice question or tip for the student to solidify their understanding.
        """.trimIndent()

        try {
            val rootJson = JSONObject()

            // System Instruction
            val systemInstruction = JSONObject()
            val systemPart = JSONObject().put("text", systemInstructionText)
            systemInstruction.put("parts", JSONArray().put(systemPart))
            rootJson.put("systemInstruction", systemInstruction)

            // User Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Prompt part
            val textPart = JSONObject().put("text", "Subject: $subject\nQuestion/Task: $prompt\nProvide step-by-step guidance in $language.")
            partsArray.put(textPart)

            // Image part if available
            if (imageBitmap != null) {
                val base64Data = convertBitmapToBase64(imageBitmap)
                val inlineDataObj = JSONObject()
                    .put("mimeType", "image/jpeg")
                    .put("data", base64Data)
                val imagePart = JSONObject().put("inlineData", inlineDataObj)
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = rootJson.toString().toRequestBody(mediaType)

            val httpRequest = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val httpResponse = okHttpClient.newCall(httpRequest).execute()
            val responseString = httpResponse.body?.string() ?: ""

            if (!httpResponse.isSuccessful) {
                return@withContext "Error: Request failed (${httpResponse.code}). $responseString"
            }

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val candidateContent = firstCandidate.optJSONObject("content")
                if (candidateContent != null) {
                    val parts = candidateContent.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No solution text generated.")
                    }
                }
            }
            "No response candidates returned."
        } catch (e: Exception) {
            "Network error: ${e.message}"
        }
    }

    suspend fun generateEssayAssistance(
        topic: String,
        mode: String, // "Draft", "Proofread", "Outline", "Tone Polish"
        language: String = "English"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing. Please set GEMINI_API_KEY in AI Studio Secrets."
        }

        val url = "$BASE_URL" + "gemini-3.5-flash:generateContent?key=$apiKey"

        val promptText = when (mode) {
            "Proofread" -> "Proofread and correct grammar, syntax, and vocabulary in the following text. Provide the polished version and bulleted key improvements made:\n\n$topic"
            "Outline" -> "Create a well-structured essay outline (Title, Introduction, Thesis statement, 3 Body Paragraph points with evidence, and Conclusion) for the topic:\n\n$topic"
            "Tone Polish" -> "Polish the tone of the following essay text to make it academic, engaging, clear, and articulate:\n\n$topic"
            else -> "Write a structured essay draft on the topic: $topic. Include Introduction, Body Paragraphs, and Conclusion in $language."
        }

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray().put(JSONObject().put("text", promptText))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val httpRequest = Request.Builder().url(url).post(body).build()
            val httpResponse = okHttpClient.newCall(httpRequest).execute()
            val responseString = httpResponse.body?.string() ?: ""

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                }
            }
            "No result returned."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun generateQuizAndFlashcards(
        subject: String,
        topic: String,
        language: String = "English"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing."
        }

        val url = "$BASE_URL" + "gemini-3.5-flash:generateContent?key=$apiKey"
        val promptText = """
            Generate 3 multiple-choice practice questions and 3 flashcard study summaries for students studying $subject on the topic: "$topic".
            Respond in $language.
            Format clearly with markdown headings:
            ### 🎴 Flashcards (Key Term & Definition)
            1. **Term**: ...
            2. **Term**: ...
            3. **Term**: ...

            ### ❓ Practice Quiz Questions
            1. Question?
               A) Option
               B) Option
               C) Option
               D) Option
               **Correct Answer**: ...
               **Explanation**: ...
        """.trimIndent()

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray().put(JSONObject().put("text", promptText))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val httpRequest = Request.Builder().url(url).post(body).build()
            val httpResponse = okHttpClient.newCall(httpRequest).execute()
            val responseString = httpResponse.body?.string() ?: ""

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "No quiz content generated.")
                }
            }
            "No content generated."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun generatePersonalizedStudyPlan(
        recentTopics: List<String>,
        language: String = "English"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing."
        }

        val url = "$BASE_URL" + "gemini-3.5-flash:generateContent?key=$apiKey"
        val topicsText = if (recentTopics.isEmpty()) "General STEM & Humanities" else recentTopics.joinToString(", ")
        val promptText = """
            Act as a personalized AI Academic Advisor. The student has recently studied or asked questions about:
            $topicsText.

            Provide a tailored study recommendation report in $language with markdown sections:
            ### 📈 Focus Areas & Strengths Analysis
            Analyze what key competencies these topics reflect.

            ### 🎯 Recommended 7-Day Study Strategy
            Give a day-by-day quick study schedule (15-20 mins/day).

            ### 🚀 Smart Memory Tips
            Give 2 actionable memory techniques (e.g. active recall, Feynman technique) applied to these subjects.
        """.trimIndent()

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray().put(JSONObject().put("text", promptText))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val httpRequest = Request.Builder().url(url).post(body).build()
            val httpResponse = okHttpClient.newCall(httpRequest).execute()
            val responseString = httpResponse.body?.string() ?: ""

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "No advice generated.")
                }
            }
            "No recommendations generated."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
