package br.com.aranoua.app_quiz.ui.theme.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.aranoua.app_quiz.ui.theme.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel, onBackToCategories: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val question = uiState.currentQuestion ?: return
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.selectedCategory ?: "Quiz") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetQuiz()
                        onBackToCategories()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Pergunta e progresso
                Text(
                    "Pergunta ${uiState.currentQuestionIndex + 1} de ${uiState.questions.size}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(question.pergunta, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(question.opcoes) { option ->
                val isCorrect = option == question.correta
                val isSelected = option == uiState.selectedAnswer

                val bgColor = when {
                    !uiState.showCuriosity -> MaterialTheme.colorScheme.surfaceVariant
                    isSelected && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                    isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.3f)
                    isCorrect && uiState.showCuriosity -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Button(
                    onClick = { viewModel.answerQuestion(option) },
                    enabled = !uiState.showCuriosity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor, shape = MaterialTheme.shapes.medium)
                ) {
                    Text(option)
                }
            }

            if (uiState.showCuriosity) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Resultado da resposta
                    Text(
                        if (uiState.isAnswerCorrect == true) "✅ Correto!" else "❌ Errado!",
                        color = if (uiState.isAnswerCorrect == true) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "💡 Curiosidade: ${question.curiosidade}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.nextQuestion() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (uiState.currentQuestionIndex == uiState.questions.size - 1) "Finalizar Quiz"
                            else "Próxima Pergunta"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Pontuação: ${uiState.currentScore} | Recorde: ${viewModel.highScore.collectAsState(initial = 0).value}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
