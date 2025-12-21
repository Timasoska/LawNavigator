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
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.effect.collectLatest {
            if (it is LoginContract.Effect.NavigateToHome) onNavigateToHome()
        }
    }

    if (state.error != null) {
        LaunchedEffect(state.error) {
            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
            viewModel.setEvent(LoginContract.Event.OnErrorShown)
        }
    }

    // Основной контейнер (Темный фон как на макете)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Deep Charcoal
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Чтобы клавиатура не перекрывала
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- ЛОГОТИП И ЗАГОЛОВОК ---
            Icon(
                imageVector = Icons.Default.Balance, // Весы правосудия
                contentDescription = null,
                tint = Color(0xFF5C6BC0), // Мягкий синий
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (state.isRegisterMode) "Создание аккаунта" else "С возвращением!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = "Ваш путь в юриспруденцию начинается здесь.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // --- ПОЛЯ ВВОДА ---

            // ИМЯ (Только при регистрации)
            AnimatedVisibility(visible = state.isRegisterMode) {
                Column {
                    CustomTextField(
                        value = state.name,
                        onValueChange = { viewModel.setEvent(LoginContract.Event.OnNameChanged(it)) },
                        label = "Как к вам обращаться?",
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // EMAIL
            CustomTextField(
                value = state.email,
                onValueChange = { viewModel.setEvent(LoginContract.Event.OnEmailChanged(it)) },
                label = "Email адрес",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ПАРОЛЬ
            CustomTextField(
                value = state.password,
                onValueChange = { viewModel.setEvent(LoginContract.Event.OnPasswordChanged(it)) },
                label = "Пароль",
                icon = Icons.Default.Lock,
                isPassword = true,
                isVisible = state.isPasswordVisible,
                onToggleVisibility = { viewModel.setEvent(LoginContract.Event.OnTogglePasswordVisibility) }
            )

            // ПОДТВЕРЖДЕНИЕ ПАРОЛЯ (Только регистрация)
            AnimatedVisibility(visible = state.isRegisterMode) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.setEvent(LoginContract.Event.OnConfirmPasswordChanged(it)) },
                        label = "Повторите пароль",
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        isVisible = state.isPasswordVisible,
                        isError = state.password.isNotEmpty() && state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword
                    )
                    if (state.password.isNotEmpty() && state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword) {
                        Text("Пароли не совпадают", color = Color(0xFFEF5350), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp).align(Alignment.Start))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- КНОПКА ДЕЙСТВИЯ ---
            Button(
                onClick = { viewModel.setEvent(LoginContract.Event.OnSubmitClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5C6BC0), // Royal Blue
                    disabledContainerColor = Color(0xFF3949AB).copy(alpha = 0.5f)
                ),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (state.isRegisterMode) "Создать аккаунт" else "Войти",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ПЕРЕКЛЮЧЕНИЕ РЕЖИМА ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (state.isRegisterMode) "Уже есть аккаунт? " else "Нет аккаунта? ",
                    color = Color.Gray
                )
                Text(
                    text = if (state.isRegisterMode) "Войти" else "Создать аккаунт",
                    color = Color(0xFF5C6BC0),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.setEvent(LoginContract.Event.OnToggleMode) }
                )
            }

            // --- НИЖНЯЯ КНОПКА (INVITE CODE) ---
            // Показываем только при регистрации
            AnimatedVisibility(visible = state.isRegisterMode) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Кнопка-переключатель "Enter with Invite Code"
                    if (!state.isTeacherMode) {
                        TextButton(
                            onClick = { viewModel.setEvent(LoginContract.Event.OnToggleTeacherMode) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("У меня есть код приглашения (Преподаватель)")
                        }
                    } else {
                        // Поле ввода кода
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Код приглашения", color = Color.Gray, fontSize = 12.sp)
                                BasicTextField(
                                    value = state.inviteCode,
                                    onValueChange = { viewModel.setEvent(LoginContract.Event.OnInviteCodeChanged(it)) },
                                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            }
                        }
                        TextButton(onClick = { viewModel.setEvent(LoginContract.Event.OnToggleTeacherMode) }) {
                            Text("Отмена", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательный компонент для красивых полей
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onToggleVisibility?.invoke() }) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        } else null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Color(0xFFEF5350) else Color(0xFF5C6BC0),
            unfocusedBorderColor = if (isError) Color(0xFFEF5350) else Color(0xFF424242),
            focusedContainerColor = Color(0xFF1E1E1E),
            unfocusedContainerColor = Color(0xFF1E1E1E),
            focusedLabelColor = if (isError) Color(0xFFEF5350) else Color(0xFF5C6BC0),
            unfocusedLabelColor = Color.Gray,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        visualTransformation = if (isPassword && !isVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}