package br.com.aranoua.app_quiz.ui.theme.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.aranoua.app_quiz.ui.theme.ui.components.QuizTopBar
import br.com.aranoua.app_quiz.ui.theme.viewmodel.QuizViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBackToCategories: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val highScore by viewModel.highScore.collectAsState(initial = 0)

    // Tela final
    if (uiState.isQuizFinished) {
        QuizEndScreen(
            score = uiState.currentScore,
            highScore = highScore,
            onPlayAgain = { viewModel.resetQuiz() },
        )
        return
    }

    var timeLeft by remember(uiState.currentQuestionIndex) { mutableStateOf(15) }

    // Temporizador
    LaunchedEffect(uiState.currentQuestionIndex) {
        timeLeft = 15
        while (timeLeft > 0 && uiState.selectedAnswer == null) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0 && uiState.selectedAnswer == null) {
            viewModel.nextQuestion()
        }
    }

    val animatedScore by animateFloatAsState(targetValue = uiState.currentScore.toFloat())

    Scaffold(
        topBar = {
            QuizTopBar(
                title = "Perguntas",
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onBack = onBackToCategories,
                onLogout = {
                    viewModel.setUserName("") // limpa o DataStore
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Barra de progresso
            if (uiState.totalQuestions > 0) {
                LinearProgressIndicator(
                    progress = (uiState.currentQuestionIndex + 1f) / uiState.totalQuestions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .padding(vertical = 8.dp)
                )
            }

            // Pontuação e tempo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pontuação: ${animatedScore.toInt()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tempo: ${timeLeft}s",
                    color = if (timeLeft < 5) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = uiState.currentQuestionIndex,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                }
            ) { index ->
                val question = uiState.questions.getOrNull(index)
                if (question != null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = question.pergunta,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Lista de opções
                        question.opcoes.forEach { option ->
                            AnswerOption(
                                answerText = option,
                                selectedAnswer = uiState.selectedAnswer,
                                correctAnswer = uiState.correctAnswer,
                                onSelect = { viewModel.answerQuestion(it) }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Feedback e curiosidade
                        AnimatedVisibility(visible = uiState.showCuriosity) {
                            val color = if (uiState.isAnswerCorrect == true)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (uiState.isAnswerCorrect == true) "Acertou! 🎉" else "Errou 😢",
                                    color = color,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                uiState.currentQuestion?.curiosidade?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Spacer(Modifier.height(12.dp))
                                Button(onClick = { viewModel.nextQuestion() }) {
                                    Text(if (uiState.isLastQuestion) "Finalizar" else "Próxima")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnswerOption(
    answerText: String,
    selectedAnswer: String?,
    correctAnswer: String?,
    onSelect: (String) -> Unit
) {
    val correctColor = Color(0xFF4CAF50)
    val incorrectColor = Color(0xFFF44336)

    val backgroundColor = when {
        selectedAnswer == null -> MaterialTheme.colorScheme.primaryContainer
        answerText == correctAnswer -> correctColor
        selectedAnswer == answerText && selectedAnswer != correctAnswer -> incorrectColor
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (backgroundColor == correctColor || backgroundColor == incorrectColor)
        Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    ElevatedButton  (
        onClick = { if (selectedAnswer == null) onSelect(answerText) },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(6.dp),
    ) {
        Text(
            text = answerText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
