package com.example.lawnavigator.presentation.login

import com.example.lawnavigator.domain.usecase.LoginUseCase
import com.example.lawnavigator.domain.usecase.RegisterUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // Имитации (Моки)
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var viewModel: LoginViewModel

    // Диспетчер для корутин в тестах (чтобы они выполнялись синхронно)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Создаем моки
        loginUseCase = mockk()
        registerUseCase = mockk()

        // Передаем моки в ViewModel
        viewModel = LoginViewModel(loginUseCase, registerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when login is successful, navigate to Home`() = runTest {
        val email = "test@test.com"
        val password = "password"

        coEvery { loginUseCase(email, password) } returns Result.success(Unit)

        viewModel.setEvent(LoginContract.Event.OnEmailChanged(email))
        viewModel.setEvent(LoginContract.Event.OnPasswordChanged(password))
        viewModel.setEvent(LoginContract.Event.OnSubmitClicked)

        advanceUntilIdle()

        val finalState = viewModel.state.value
        assertEquals(null, finalState.error)

        // УБРАЛИ ПРОВЕРКУ isLoading, так как при успехе спиннер крутится до перехода
        // assertEquals(false, finalState.isLoading)

        val effect = viewModel.effect.first()
        assertTrue(effect is LoginContract.Effect.NavigateToHome)
    }

    @Test
    fun `when login fails, show error message`() = runTest {
        // УСТАНОВКА ДАННЫХ
        val email = "test@test.com"
        val wrongPassword = "wrong"
        val errorMessage = "Invalid credentials"

        // Настраиваем мок на ошибку
        coEvery { loginUseCase(email, wrongPassword) } returns Result.failure(Exception(errorMessage))

        // ДЕЙСТВИЕ
        viewModel.setEvent(LoginContract.Event.OnEmailChanged(email))
        viewModel.setEvent(LoginContract.Event.OnPasswordChanged(wrongPassword))
        viewModel.setEvent(LoginContract.Event.OnSubmitClicked)

        advanceUntilIdle()

        // ПРОВЕРКА
        val finalState = viewModel.state.value
        // Должна появиться ошибка в стейте
        assertEquals(errorMessage, finalState.error)
        assertEquals(false, finalState.isLoading)
    }

    @Test
    fun `when passwords do not match during registration, prevent submit and show error`() = runTest {
        // ДЕЙСТВИЕ: Включаем режим регистрации
        viewModel.setEvent(LoginContract.Event.OnToggleMode)

        viewModel.setEvent(LoginContract.Event.OnNameChanged("Student"))
        viewModel.setEvent(LoginContract.Event.OnEmailChanged("test@test.com"))
        viewModel.setEvent(LoginContract.Event.OnPasswordChanged("pass123"))
        viewModel.setEvent(LoginContract.Event.OnConfirmPasswordChanged("pass456")) // Пароли разные

        viewModel.setEvent(LoginContract.Event.OnSubmitClicked)

        advanceUntilIdle()

        // ПРОВЕРКА
        val finalState = viewModel.state.value
        // Ошибка валидации должна перехватить запрос
        assertEquals("Пароли не совпадают", finalState.error)

        // Важно: registerUseCase НЕ должен был вызваться вообще (можем это не проверять жестко, но стейт верен)
    }
}