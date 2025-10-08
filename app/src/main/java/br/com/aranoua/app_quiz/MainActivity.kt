package br.com.aranoua.app_quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App_quizTheme {
                val context = LocalContext.current
                val dataStoreManager = DataStoreManager(context)
                val repository = PerguntaRepository(context, dataStoreManager)

                // Obtém o ViewModel usando a Factory
                val quizViewModel: QuizViewModel = viewModel(
                    factory = QuizViewModelFactory(repository)
                )

                QuizAppNavHost(quizViewModel)
            }
        }
    }
}


@Composable
fun QuizAppNavHost(viewModel: QuizViewModel) {
    val userName by viewModel.userName.collectAsState(initial = null)

    val uiState by viewModel.uiState.collectAsState()


    when {
        // 1. Tela de Login: Usuário ainda não se identificou
        userName.isNullOrBlank() -> {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { /* Não é necessário fazer nada aqui, o 'userName' atualiza automaticamente */ }
            )
        }

        // 2. Tela de Categoria: Usuário logado, mas quiz não iniciado
        uiState.selectedCategory == null -> {
            CategoriaScreen(
                viewModel = viewModel,
                onCategorySelected = { category ->
                    // O startQuiz() no ViewModel já define o 'selectedCategory' e inicia o Quiz.
                    // A UI reage automaticamente.
                }
            )
        }

        // 3. Tela do Quiz: Usuário logado e quiz em andamento
        else -> {
            QuizScreen(
                viewModel = viewModel,
                onBackToCategories = {
                    // Após o quiz, volta para a tela de categorias
                    viewModel.resetQuiz()
                }
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