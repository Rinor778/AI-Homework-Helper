package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.api.GeminiClient
import com.example.data.local.HomeworkDao
import com.example.data.local.HomeworkItem
import kotlinx.coroutines.flow.Flow

class HomeworkRepository(
    private val homeworkDao: HomeworkDao,
    val usageManager: UsageManager
) {
    val allHomework: Flow<List<HomeworkItem>> = homeworkDao.getAllHomework()
    val favoriteHomework: Flow<List<HomeworkItem>> = homeworkDao.getFavoriteHomework()

    fun searchHomework(query: String): Flow<List<HomeworkItem>> = homeworkDao.searchHomework(query)

    suspend fun solveHomework(
        question: String,
        subject: String,
        imageBitmap: Bitmap? = null,
        language: String = "English",
        useProModel: Boolean = false
    ): Result<HomeworkItem> {
        if (imageBitmap != null && !usageManager.canSolveImage()) {
            return Result.failure(Exception("Free limit reached: You have used your 10 free images for this week. Upgrade to Pro for unlimited access!"))
        }
        if (!usageManager.canAskQuestion()) {
            return Result.failure(Exception("Free limit reached: You have reached your 100 free questions for this week. Upgrade to Pro for unlimited access!"))
        }

        val explanation = GeminiClient.generateHomeworkExplanation(
            prompt = question,
            subject = subject,
            imageBitmap = imageBitmap,
            language = language,
            useProModel = useProModel
        )

        if (explanation.startsWith("Error:") || explanation.startsWith("API Key is missing")) {
            return Result.failure(Exception(explanation))
        }

        if (imageBitmap != null) {
            usageManager.incrementImages()
        }
        usageManager.incrementQuestions()

        val item = HomeworkItem(
            subject = subject,
            question = question,
            explanation = explanation,
            language = language
        )

        val insertedId = homeworkDao.insertHomework(item)
        val savedItem = item.copy(id = insertedId)

        return Result.success(savedItem)
    }

    suspend fun generateEssay(
        topic: String,
        mode: String,
        language: String
    ): Result<String> {
        if (!usageManager.canAskQuestion()) {
            return Result.failure(Exception("Free question limit reached for this week."))
        }
        val result = GeminiClient.generateEssayAssistance(topic, mode, language)
        usageManager.incrementQuestions()
        return Result.success(result)
    }

    suspend fun generateQuiz(
        subject: String,
        topic: String,
        language: String
    ): Result<String> {
        if (!usageManager.canAskQuestion()) {
            return Result.failure(Exception("Free question limit reached for this week."))
        }
        val result = GeminiClient.generateQuizAndFlashcards(subject, topic, language)
        usageManager.incrementQuestions()
        return Result.success(result)
    }

    suspend fun generateStudyPlan(
        recentTopics: List<String>,
        language: String
    ): Result<String> {
        val result = GeminiClient.generatePersonalizedStudyPlan(recentTopics, language)
        return Result.success(result)
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        homeworkDao.updateFavoriteStatus(id, !currentStatus)
    }

    suspend fun deleteHomework(item: HomeworkItem) {
        homeworkDao.deleteHomework(item)
    }

    suspend fun clearHistory() {
        homeworkDao.clearAll()
    }
}
