package com.vlaados.freeze.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlaados.freeze.data.local.TokenStorage
import com.vlaados.freeze.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val name: String = "",
    val level: Int = 1,
    val avatarInitials: String = "?",
    val goalProgress: Float = 0f,
    val goalTitle: String = "",
    val savedForGoal: Int = 0,
    val achievements: List<AchievementUiModel> = emptyList()
)

data class AchievementUiModel(
    val id: Int,
    val title: String,
    val description: String,
    val photoUrl: String?,
    val targetValue: Int,
    val currentValue: Int,
    val isUnlocked: Boolean
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savingsRepository: com.vlaados.freeze.data.repository.SavingsRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val token = tokenStorage.getToken().first()
            if (token != null) {
                launch { loadProfile(token) }
                launch { loadAchievementsLocally(token) }
            }
        }
    }

    private suspend fun loadProfile(token: String) {
        userRepository.getMe(token)
            .onSuccess { user ->
                val initial = user.name?.firstOrNull()?.toString()?.uppercase() ?: "?"
                val goalAmount = user.goal_amount?.toInt() ?: 1
                val saved = user.saved_for_goal?.toInt() ?: 0
                val progress = if (goalAmount > 0) saved.toFloat() / goalAmount.toFloat() else 0f
                val calculatedLevel = (saved / 5000) + 1

                _uiState.value = _uiState.value.copy(
                    name = user.name ?: "Пользователь",
                    avatarInitials = initial,
                    goalTitle = user.goal_name ?: "Нет цели",
                    savedForGoal = saved,
                    goalProgress = progress,
                    level = calculatedLevel
                )
            }
    }

    private suspend fun loadAchievementsLocally(token: String) {
        val savingsResult = savingsRepository.getSavings(token)
        
        if (savingsResult.isSuccess) {
            val savings = savingsResult.getOrNull() ?: emptyList()
            
            val totalSaved = savings.filter { !it.is_breakdown }.sumOf { it.amount }.toInt()
            val breakdownCount = savings.count { it.is_breakdown }
            val refusalCount = savings.count { !it.is_breakdown } // Successful refusals (savings)

            val localAchievements = listOf(
                AchievementUiModel(
                    id = 1,
                    title = "Бережливый",
                    description = "Сэкономить 10.000р",
                    photoUrl = "https://i.postimg.cc/k55hqsWV/Gemini_Generated_Image_jvf3ykjvf3ykjvf3_(2)_no_bg_preview_(carve_photos).png",
                    targetValue = 10000,
                    currentValue = totalSaved,
                    isUnlocked = totalSaved >= 10000
                ),
                AchievementUiModel(
                    id = 2,
                    title = "Миллионер",
                    description = "Сэкономить 24.000р",
                    photoUrl = "https://i.postimg.cc/QMMnhmcm/Gemini_Generated_Image_j65ow3j65ow3j65o_edited_free_(carve_photos).png",
                    targetValue = 24000,
                    currentValue = totalSaved,
                    isUnlocked = totalSaved >= 24000
                ),
                AchievementUiModel(
                    id = 3,
                    title = "Новичок",
                    description = "Сорваться 10 раз",
                    photoUrl = "https://i.postimg.cc/HLPvxMtR/Gemini_Generated_Image_vupqk7vupqk7vupq_edited_free_(carve_photos).png",
                    targetValue = 10,
                    currentValue = breakdownCount,
                    isUnlocked = breakdownCount >= 10
                ),
                AchievementUiModel(
                    id = 4,
                    title = "Экспериментатор",
                    description = "Сорваться 25 раз",
                    photoUrl = "https://i.postimg.cc/cHg98J9x/Gemini_Generated_Image_2go1582go1582go1_edited_free_(carve_photos).png",
                    targetValue = 25,
                    currentValue = breakdownCount,
                    isUnlocked = breakdownCount >= 25
                ),
                AchievementUiModel(
                    id = 7,
                    title = "Аскет",
                    description = "Отказаться от 10 покупок",
                    photoUrl = "https://i.postimg.cc/NjDP3S2s/Gemini_Generated_Image_392h5o392h5o392h_edited_free_(carve_photos).png",
                    targetValue = 10,
                    currentValue = refusalCount,
                    isUnlocked = refusalCount >= 10
                ),
                AchievementUiModel(
                    id = 8,
                    title = "Мастер",
                    description = "Отказаться от 25 покупок",
                    photoUrl = "https://i.postimg.cc/JhTvkvRc/Gemini_Generated_Image_mrh63ymrh63ymrh6_no_bg_preview_(carve_photos).png",
                    targetValue = 25,
                    currentValue = refusalCount,
                    isUnlocked = refusalCount >= 25
                )
            )
            
            _uiState.value = _uiState.value.copy(achievements = localAchievements)
        }
    }
}
