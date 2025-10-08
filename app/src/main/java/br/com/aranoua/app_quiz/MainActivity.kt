package br.com.aranoua.app_quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.aranoua.app_quiz.ui.theme.App_quizTheme
import br.com.aranoua.app_quiz.ui.theme.data.DataStoreManager
import br.com.aranoua.app_quiz.ui.theme.repository.PerguntaRepository
import br.com.aranoua.app_quiz.ui.theme.ui.screen.CategoriaScreen
import br.com.aranoua.app_quiz.ui.theme.ui.screen.LoginScreen
import br.com.aranoua.app_quiz.ui.theme.ui.screen.QuizScreen
import br.com.aranoua.app_quiz.ui.theme.viewmodel.QuizViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val dataStoreManager = DataStoreManager(context)
            val repository = PerguntaRepository(context, dataStoreManager)

            val quizViewModel: QuizViewModel = viewModel(
                factory = QuizViewModelFactory(repository)
            )

            // 🌙 Controle de tema global
            val isDarkThemeFlow = dataStoreManager.getTheme()
            val isDarkTheme by isDarkThemeFlow.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            App_quizTheme(darkTheme = isDarkTheme) {
                QuizAppNavHost(
                    viewModel = quizViewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        scope.launch {
                            dataStoreManager.setTheme(!isDarkTheme)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuizAppNavHost(
    viewModel: QuizViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val userName by viewModel.userName.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()

    when {
        userName.isNullOrBlank() -> {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {},
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        uiState.selectedCategory == null -> {
            CategoriaScreen(
                viewModel = viewModel,
                onCategorySelected = {},
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        else -> {
            QuizScreen(
                viewModel = viewModel,
                onBackToCategories = { viewModel.resetQuiz() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    }
}



class QuizViewModelFactory(private val repository: PerguntaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}