package com.example.lawnavigator.presentation.login

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.offset(x = (-50).dp, y = (-50).dp).size(200.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(20.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Balance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (state.isRegisterMode) "Создание аккаунта" else "С возвращением, Юрист",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            )

            Text(
                text = "Ваш путь к адвокатуре продолжается здесь.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            AnimatedVisibility(visible = state.isRegisterMode) {
                Column {
                    StitchTextField(
                        value = state.name,
                        onValueChange = { viewModel.setEvent(LoginContract.Event.OnNameChanged(it)) },
                        label = "Имя и Фамилия",
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            StitchTextField(
                value = state.email,
                onValueChange = { viewModel.setEvent(LoginContract.Event.OnEmailChanged(it)) },
                label = "Email Адрес",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            StitchTextField(
                value = state.password,
                onValueChange = { viewModel.setEvent(LoginContract.Event.OnPasswordChanged(it)) },
                label = "Пароль",
                icon = Icons.Default.Lock,
                isPassword = true,
                isVisible = state.isPasswordVisible,
                onToggleVisibility = { viewModel.setEvent(LoginContract.Event.OnTogglePasswordVisibility) }
            )

            AnimatedVisibility(visible = state.isRegisterMode) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    StitchTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.setEvent(LoginContract.Event.OnConfirmPasswordChanged(it)) },
                        label = "Повторите пароль",
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        isVisible = state.isPasswordVisible,
                        onToggleVisibility = { viewModel.setEvent(LoginContract.Event.OnTogglePasswordVisibility) },
                        isError = state.password.isNotEmpty() && state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword
                    )
                    if (state.password.isNotEmpty() && state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword) {
                        Text("Пароли не совпадают", color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp).align(Alignment.Start))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(16.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)), CircleShape)
                    .clickable(enabled = !state.isLoading) { viewModel.setEvent(LoginContract.Event.OnSubmitClicked) },
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.background, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (state.isRegisterMode) "Создать аккаунт" else "Войти",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (state.isRegisterMode) "Уже есть аккаунт? " else "Нет аккаунта? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (state.isRegisterMode) "Войти" else "Создать",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.setEvent(LoginContract.Event.OnToggleMode) }
                )
            }

            AnimatedVisibility(visible = state.isRegisterMode) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))
                    if (!state.isTeacherMode) {
                        TextButton(onClick = { viewModel.setEvent(LoginContract.Event.OnToggleTeacherMode) }) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ввести инвайт-код (Преподаватель)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Код приглашения", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                BasicTextField(
                                    value = state.inviteCode,
                                    onValueChange = { viewModel.setEvent(LoginContract.Event.OnInviteCodeChanged(it)) },
                                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            }
                        }
                        TextButton(onClick = { viewModel.setEvent(LoginContract.Event.OnToggleTeacherMode) }) {
                            Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StitchTextField(
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
    Column(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onToggleVisibility?.invoke() }) {
                        Icon(if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !isVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}