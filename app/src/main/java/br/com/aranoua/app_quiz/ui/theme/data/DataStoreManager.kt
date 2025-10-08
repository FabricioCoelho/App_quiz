package br.com.aranoua.app_quiz.ui.theme.data
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Cria o DataStore de preferências
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val HIGH_SCORE_KEY = intPreferencesKey("high_score")
    }

    val userNameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME_KEY]
        }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    // Pontuação Máxima (HistoricoResposta Simplificado)
    val highScoreFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[HIGH_SCORE_KEY] ?: 0
        }

    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { preferences ->
            // Apenas salva a pontuação se for maior que a atual (lógica de recorde)
            val currentScore = preferences[HIGH_SCORE_KEY] ?: 0
            if (score > currentScore) {
                preferences[HIGH_SCORE_KEY] = score
            }
        }
    }

    val THEME_KEY = booleanPreferencesKey("dark_theme")

    fun getTheme(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[THEME_KEY] ?: false // padrão: modo claro
        }
    }

    suspend fun setTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = isDark
        }
    }

}