package br.com.aranoua.app_quiz.ui.theme.model

data class Question(
    val pergunta: String,
    val opcoes: List<String>,
    val correta: String,
    val categoria: String,
    val curiosidade: String
)