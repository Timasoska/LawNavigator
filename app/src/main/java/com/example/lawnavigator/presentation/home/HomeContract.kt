package com.example.lawnavigator.presentation.home

import com.example.lawnavigator.core.mvi.ViewIntent
import com.example.lawnavigator.core.mvi.ViewSideEffect
import com.example.lawnavigator.core.mvi.ViewState
import com.example.lawnavigator.domain.model.Discipline
import com.example.lawnavigator.domain.model.EngagementStatus

// Варианты сортировки
enum class SortType(val title: String) {
    NONE("По умолчанию"),
    A_Z("От А до Я"),
    Z_A("От Я до А")
}

class HomeContract {

    data class State(
        val originalDisciplines: List<Discipline> = emptyList(), // Сохраняем исходный список
        val disciplines: List<Discipline> = emptyList(),         // Список для отображения
        val isLoading: Boolean = false,
        val error: String? = null,
        val isTeacher: Boolean = false,
        val engagementStatus: EngagementStatus? = null,
        val userName: String = "User",

        // Поля фильтрации
        val isFilterExpanded: Boolean = false,
        val currentSort: SortType = SortType.NONE
    ) : ViewState

    sealed class Event : ViewIntent {
        data object OnRetryClicked : Event()
        data class OnDisciplineClicked(val disciplineId: Int) : Event()
        data object OnTeacherGroupsClicked : Event()
        data object OnRefresh : Event()

        // События фильтрации
        data class OnFilterClick(val isExpanded: Boolean) : Event()
        data class OnSortSelected(val sortType: SortType) : Event()
    }

    sealed class Effect : ViewSideEffect {
        data class NavigateToTopics(val disciplineId: Int) : Effect()
        data object NavigateToTeacherGroups : Effect()
    }
}