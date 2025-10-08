package br.com.aranoua.app_quiz.ui.theme.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTopBar(
    title: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        actions = {
            // Alternar tema
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme)
                        Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Alternar tema"
                )
            }

            // Logout (só aparece se a função for passada)
            onLogout?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
