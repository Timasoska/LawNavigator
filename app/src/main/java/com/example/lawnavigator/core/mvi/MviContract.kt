package com.example.lawnavigator.core.mvi

// Маркерные интерфейсы
interface ViewState // Состояние экрана (данные)
interface ViewIntent // Действие пользователя (нажатие кнопки)
interface ViewSideEffect // Разовое событие (навигация, ошибка)