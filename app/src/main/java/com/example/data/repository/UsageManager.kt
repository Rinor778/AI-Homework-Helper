package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class UsageManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("homework_usage_prefs", Context.MODE_PRIVATE)

    companion object {
        const val MAX_FREE_QUESTIONS_PER_WEEK = 100
        const val MAX_FREE_IMAGES_PER_WEEK = 10
    }

    private val _questionsUsed = MutableStateFlow(getQuestionsUsed())
    val questionsUsed: StateFlow<Int> = _questionsUsed

    private val _imagesUsed = MutableStateFlow(getImagesUsed())
    val imagesUsed: StateFlow<Int> = _imagesUsed

    private val _isPro = MutableStateFlow(prefs.getBoolean("is_pro_user", false))
    val isPro: StateFlow<Boolean> = _isPro

    init {
        checkAndResetWeeklyIfNeeded()
    }

    private fun checkAndResetWeeklyIfNeeded() {
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        val savedWeek = prefs.getInt("saved_week", -1)
        if (savedWeek != currentWeek) {
            prefs.edit()
                .putInt("saved_week", currentWeek)
                .putInt("questions_count", 0)
                .putInt("images_count", 0)
                .apply()
            _questionsUsed.value = 0
            _imagesUsed.value = 0
        }
    }

    fun getQuestionsUsed(): Int {
        checkAndResetWeeklyIfNeeded()
        return prefs.getInt("questions_count", 0)
    }

    fun getImagesUsed(): Int {
        checkAndResetWeeklyIfNeeded()
        return prefs.getInt("images_count", 0)
    }

    fun canAskQuestion(): Boolean {
        if (_isPro.value) return true
        return getQuestionsUsed() < MAX_FREE_QUESTIONS_PER_WEEK
    }

    fun canSolveImage(): Boolean {
        if (_isPro.value) return true
        return getImagesUsed() < MAX_FREE_IMAGES_PER_WEEK
    }

    fun incrementQuestions() {
        if (_isPro.value) return
        val current = getQuestionsUsed() + 1
        prefs.edit().putInt("questions_count", current).apply()
        _questionsUsed.value = current
    }

    fun incrementImages() {
        if (_isPro.value) return
        val current = getImagesUsed() + 1
        prefs.edit().putInt("images_count", current).apply()
        _imagesUsed.value = current
    }

    fun setProUser(enabled: Boolean) {
        prefs.edit().putBoolean("is_pro_user", enabled).apply()
        _isPro.value = enabled
    }

    fun resetFreeQuota() {
        prefs.edit()
            .putInt("questions_count", 0)
            .putInt("images_count", 0)
            .apply()
        _questionsUsed.value = 0
        _imagesUsed.value = 0
    }
}
