package com.vlaados.freeze.features.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlaados.freeze.data.local.TokenStorage
import com.vlaados.freeze.data.repository.AuthRepository
import com.vlaados.freeze.data.repository.SavingsRepository
import com.vlaados.freeze.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savingsRepository: SavingsRepository,
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _savedAmount = MutableStateFlow(0.0)
    val savedAmount: StateFlow<Double> = _savedAmount

    private val _goalProgress = MutableStateFlow<Pair<Float, Int>?>(null)
    val goalProgress: StateFlow<Pair<Float, Int>?> = _goalProgress

    private val _daysLeft = MutableStateFlow<Int?>(null)
    val daysLeft: StateFlow<Int?> = _daysLeft
    
    private val _goalName = MutableStateFlow<String?>(null)
    val goalName: StateFlow<String?> = _goalName

    private var allSavings: List<com.vlaados.freeze.data.model.Saving> = emptyList()
    private var currentPeriod = "Месяц"

    init {
        loadData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadData() {
        viewModelScope.launch {
            val token = tokenStorage.getToken().first() ?: return@launch
            
            userRepository.getMe(token).onSuccess { user ->
                 _goalName.value = user.goal_name
                 val goalAmount = user.goal_amount ?: 0.0
                 val savedForGoal = user.saved_for_goal ?: 0.0
                 
                 if (goalAmount > 0) {
                     val progress = (savedForGoal / goalAmount).toFloat().coerceIn(0f, 1f)
                     _goalProgress.value = progress to savedForGoal.toInt()
                 } else {
                     _goalProgress.value = null
                 }

                 user.goal_date?.let { dateStr ->
                     try {
                         val goalDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME)
                         val days = ChronoUnit.DAYS.between(LocalDateTime.now(), goalDate).toInt()
                         _daysLeft.value = if (days > 0) days else 0
                     } catch (e: Exception) {
                         Log.e("HomeViewModel", "Error parsing date: ${e.message}")
                         _daysLeft.value = null
                     }
                 }
            }

            savingsRepository.getSavings(token).onSuccess { savings ->
                allSavings = savings
                recalculateSavedAmount()
            }
        }
    }

    fun updatePeriod(period: String) {
        currentPeriod = period
        recalculateSavedAmount()
    }

    private fun recalculateSavedAmount() {
        val now = LocalDate.now()
        val filteredSavings = when (currentPeriod) {
            "День" -> allSavings.filter { 
                try {
                     LocalDateTime.parse(it.date, DateTimeFormatter.ISO_DATE_TIME).toLocalDate().isEqual(now)
                } catch(e: Exception) { false }
            }
            "Месяц" -> allSavings.filter {
                try {
                    val date = LocalDateTime.parse(it.date, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
                    date.month == now.month && date.year == now.year
                } catch(e: Exception) { false }
            }
            "Год" -> allSavings.filter {
                try {
                    LocalDateTime.parse(it.date, DateTimeFormatter.ISO_DATE_TIME).toLocalDate().year == now.year
                } catch(e: Exception) { false }
            }
            "Все время", "Всё время" -> allSavings
            else -> allSavings
        }
        
        _savedAmount.value = filteredSavings
            .filter { !it.is_breakdown }
            .sumOf { it.amount }
    }
}
