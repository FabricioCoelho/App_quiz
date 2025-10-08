package br.com.aranoua.app_quiz.ui.theme.repository

import android.content.Context
import androidx.datastore.core.IOException
import br.com.aranoua.app_quiz.ui.theme.data.DataStoreManager
import br.com.aranoua.app_quiz.ui.theme.model.Question
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.google.gson.Gson

class PerguntaRepository(
    private val context: Context,
    private val dataStoreManager: DataStoreManager // Injetado
) {

    // --- JSON: Carrega Perguntas ---
    suspend fun loadQuestions(): List<Question> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("perguntas.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Question>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    // --- DataStore: Acesso ao Usuário ---
    fun getUserName(): Flow<String?> = dataStoreManager.userNameFlow

    suspend fun setUserName(name: String) = dataStoreManager.saveUserName(name)

    // --- DataStore: Acesso ao Histórico (Recorde) ---
    fun getHighScore(): Flow<Int> = dataStoreManager.highScoreFlow

    suspend fun updateHighScore(score: Int) = dataStoreManager.saveHighScore(score)

    // Simula a obtenção de categorias
    fun getCategories(questions: List<Question>): List<String> {
        return questions.map { it.categoria }.distinct()
    }

    // Simula o filtro de perguntas por categoria
    fun getQuestionsByCategory(questions: List<Question>, category: String): List<Question> {
        return questions.filter { it.categoria == category }
    }
}