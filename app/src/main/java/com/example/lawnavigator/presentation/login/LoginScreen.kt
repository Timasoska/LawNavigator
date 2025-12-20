package com.example.lawnavigator.presentation.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LoginContract.Effect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    if (state.error != null) {
        LaunchedEffect(state.error) {
            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
            viewModel.setEvent(LoginContract.Event.OnErrorShown)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Юр-Навигатор", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.setEvent(LoginContract.Event.OnEmailChanged(it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            // ПОЛЕ ПАРОЛЯ С ГЛАЗОМ
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.setEvent(LoginContract.Event.OnPasswordChanged(it)) },
                label = { Text("Пароль") },
                modifier = Modifier.fillMaxWidth(),
                // Если true - текст виден, если false - точки
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.setEvent(LoginContract.Event.OnTogglePasswordVisibility) }) {
                        val icon = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        val description = if (state.isPasswordVisible) "Скрыть пароль" else "Показать пароль"
                        Icon(imageVector = icon, contentDescription = description)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Button(
                onClick = { viewModel.setEvent(LoginContract.Event.OnLoginClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Войти")
            }

            OutlinedButton(
                onClick = { viewModel.setEvent(LoginContract.Event.OnRegisterClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Зарегистрироваться")
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        }
    }
}