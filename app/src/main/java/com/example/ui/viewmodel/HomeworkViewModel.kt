package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.HomeworkItem
import com.example.data.repository.HomeworkRepository
import com.example.data.repository.UsageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SolveUiState(
    val question: String = "",
    val subject: String = "Math",
    val selectedImage: Bitmap? = null,
    val selectedLanguage: String = "English",
    val isLoading: Boolean = false,
    val solutionResult: String? = null,
    val currentSavedItem: HomeworkItem? = null,
    val errorMessage: String? = null
)

data class EssayUiState(
    val prompt: String = "",
    val mode: String = "Draft", // "Draft", "Proofread", "Outline", "Tone Polish"
    val language: String = "English",
    val isLoading: Boolean = false,
    val result: String? = null,
    val errorMessage: String? = null
)

data class QuizUiState(
    val subject: String = "Math",
    val topic: String = "Quadratic Equations",
    val language: String = "English",
    val isLoading: Boolean = false,
    val result: String? = null,
    val errorMessage: String? = null
)

data class AdvisorUiState(
    val isLoading: Boolean = false,
    val studyPlan: String? = null,
    val errorMessage: String? = null
)

class HomeworkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val usageManager = UsageManager(application)
    val repository = HomeworkRepository(db.homeworkDao(), usageManager)

    // Usage State
    val questionsUsed: StateFlow<Int> = usageManager.questionsUsed
    val imagesUsed: StateFlow<Int> = usageManager.imagesUsed
    val isPro: StateFlow<Boolean> = usageManager.isPro

    // Solve State
    private val _solveState = MutableStateFlow(SolveUiState())
    val solveState: StateFlow<SolveUiState> = _solveState.asStateFlow()

    // Essay State
    private val _essayState = MutableStateFlow(EssayUiState())
    val essayState: StateFlow<EssayUiState> = _essayState.asStateFlow()

    // Quiz State
    private val _quizState = MutableStateFlow(QuizUiState())
    val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()

    // Advisor State
    private val _advisorState = MutableStateFlow(AdvisorUiState())
    val advisorState: StateFlow<AdvisorUiState> = _advisorState.asStateFlow()

    // Search Query & History State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val historyList: StateFlow<List<HomeworkItem>> = repository.allHomework
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteList: StateFlow<List<HomeworkItem>> = repository.favoriteHomework
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<HomeworkItem>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allHomework
            else repository.searchHomework(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Show Pro Modal Sheet
    private val _showProDialog = MutableStateFlow(false)
    val showProDialog: StateFlow<Boolean> = _showProDialog.asStateFlow()

    fun updateQuestion(question: String) {
        _solveState.value = _solveState.value.copy(question = question)
    }

    fun updateSubject(subject: String) {
        _solveState.value = _solveState.value.copy(subject = subject)
    }

    fun updateSelectedImage(bitmap: Bitmap?) {
        _solveState.value = _solveState.value.copy(selectedImage = bitmap)
    }

    fun updateLanguage(language: String) {
        _solveState.value = _solveState.value.copy(selectedLanguage = language)
    }

    fun clearSolveState() {
        _solveState.value = SolveUiState()
    }

    fun solveQuestion() {
        val state = _solveState.value
        if (state.question.isBlank() && state.selectedImage == null) {
            _solveState.value = state.copy(errorMessage = "Please enter a question or upload a photo of your homework.")
            return
        }

        _solveState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.solveHomework(
                question = state.question.ifBlank { "Solve and explain the question in the attached image." },
                subject = state.subject,
                imageBitmap = state.selectedImage,
                language = state.selectedLanguage,
                useProModel = isPro.value
            )

            result.onSuccess { item ->
                _solveState.value = _solveState.value.copy(
                    isLoading = false,
                    solutionResult = item.explanation,
                    currentSavedItem = item,
                    errorMessage = null
                )
            }.onFailure { exception ->
                _solveState.value = _solveState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to generate solution."
                )
                if (exception.message?.contains("Free limit reached") == true) {
                    _showProDialog.value = true
                }
            }
        }
    }

    // Essay actions
    fun updateEssayPrompt(prompt: String) {
        _essayState.value = _essayState.value.copy(prompt = prompt)
    }

    fun updateEssayMode(mode: String) {
        _essayState.value = _essayState.value.copy(mode = mode)
    }

    fun updateEssayLanguage(lang: String) {
        _essayState.value = _essayState.value.copy(language = lang)
    }

    fun generateEssay() {
        val state = _essayState.value
        if (state.prompt.isBlank()) {
            _essayState.value = state.copy(errorMessage = "Please enter your essay topic or text.")
            return
        }
        _essayState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.generateEssay(state.prompt, state.mode, state.language)
            result.onSuccess { res ->
                _essayState.value = _essayState.value.copy(isLoading = false, result = res)
            }.onFailure { err ->
                _essayState.value = _essayState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    // Quiz actions
    fun updateQuizSubject(subject: String) {
        _quizState.value = _quizState.value.copy(subject = subject)
    }

    fun updateQuizTopic(topic: String) {
        _quizState.value = _quizState.value.copy(topic = topic)
    }

    fun generateQuiz() {
        val state = _quizState.value
        if (state.topic.isBlank()) {
            _quizState.value = state.copy(errorMessage = "Please enter a study topic.")
            return
        }
        _quizState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.generateQuiz(state.subject, state.topic, state.language)
            result.onSuccess { res ->
                _quizState.value = _quizState.value.copy(isLoading = false, result = res)
            }.onFailure { err ->
                _quizState.value = _quizState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    // Advisor actions
    fun generateStudyAdvisor() {
        _advisorState.value = _advisorState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val recentTopics = historyList.value.take(5).map { "${it.subject}: ${it.question.take(30)}" }
            val result = repository.generateStudyPlan(recentTopics, _solveState.value.selectedLanguage)
            result.onSuccess { plan ->
                _advisorState.value = _advisorState.value.copy(isLoading = false, studyPlan = plan)
            }.onFailure { err ->
                _advisorState.value = _advisorState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(item: HomeworkItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun deleteItem(item: HomeworkItem) {
        viewModelScope.launch {
            repository.deleteHomework(item)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun openProDialog() {
        _showProDialog.value = true
    }

    fun closeProDialog() {
        _showProDialog.value = false
    }

    fun setProUser(enabled: Boolean) {
        usageManager.setProUser(enabled)
        _showProDialog.value = false
    }

    fun resetFreeQuota() {
        usageManager.resetFreeQuota()
    }
}
