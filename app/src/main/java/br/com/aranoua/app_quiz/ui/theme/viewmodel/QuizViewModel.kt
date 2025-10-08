package br.com.aranoua.app_quiz.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.aranoua.app_quiz.ui.theme.model.Question
import br.com.aranoua.app_quiz.ui.theme.repository.PerguntaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// -------------------------
// Estado da UI do Quiz
// -------------------------
data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val isLoading: Boolean = true,
    val selectedAnswer: String? = null,
    val isAnswerCorrect: Boolean? = null,
    val showCuriosity: Boolean = false,
    val currentScore: Int = 0,
    val selectedCategory: String? = null
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentQuestionIndex)

    val currentAnswers: List<String>
        get() = currentQuestion?.opcoes ?: emptyList()

    val correctAnswer: String?
        get() = currentQuestion?.correta

    val totalQuestions: Int
        get() = questions.size

    val isLastQuestion: Boolean
        get() = currentQuestionIndex == totalQuestions - 1

    val isQuizFinished: Boolean
        get() = currentQuestionIndex >= questions.size && questions.isNotEmpty() && !isLoading
}

// -------------------------
// ViewModel do Quiz
// -------------------------
class QuizViewModel(private val repository: PerguntaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Flows do DataStore
    val userName = repository.getUserName()
    val highScore = repository.getHighScore()

    // Lista de categorias
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private var allQuestions: List<Question> = emptyList()

    init {
        loadAllQuestionsAndCategories()
    }

    private fun loadAllQuestionsAndCategories() {
        viewModelScope.launch {
            allQuestions = repository.loadQuestions()
            _categories.value = allQuestions.map { it.categoria }.distinct()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun startQuiz(category: String) {
        val filteredQuestions = allQuestions.filter { it.categoria == category }.shuffled()
        _uiState.value = QuizUiState(
            questions = filteredQuestions,
            selectedCategory = category,
            isLoading = false,
            currentQuestionIndex = 0,
            currentScore = 0
        )
    }

    fun answerQuestion(selectedOption: String) {
        if (_uiState.value.selectedAnswer != null) return

        val question = _uiState.value.currentQuestion ?: return
        val isCorrect = selectedOption == question.correta

        val newScore = if (isCorrect) _uiState.value.currentScore + 1 else _uiState.value.currentScore

        _uiState.value = _uiState.value.copy(
            selectedAnswer = selectedOption,
            isAnswerCorrect = isCorrect,
            showCuriosity = true,
            currentScore = newScore
        )

        viewModelScope.launch {
            repository.updateHighScore(newScore)
        }
    }

    fun nextQuestion() {
        val nextIndex = _uiState.value.currentQuestionIndex + 1
        if (nextIndex < _uiState.value.questions.size) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswer = null,
                isAnswerCorrect = null,
                showCuriosity = false
            )
        } else {
            _uiState.value = _uiState.value.copy(selectedCategory = null)
        }
    }

    fun resetQuiz() {
        _uiState.value = QuizUiState(
            questions = emptyList(),
            selectedCategory = null,
            isLoading = false,
            currentScore = 0
        )
    }
}
