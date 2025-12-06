package com.vlaados.freeze.features.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlaados.freeze.core.FrozenPurchase
import com.vlaados.freeze.core.Purchase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val savingsRepository: com.vlaados.freeze.data.repository.SavingsRepository,
    private val tokenStorage: com.vlaados.freeze.data.local.TokenStorage
) : ViewModel() {

    private val _pastPurchases = MutableStateFlow<List<FrozenPurchase>>(emptyList())
    val pastPurchases = _pastPurchases.asStateFlow()

    init {
        loadHistory()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadHistory() {
        viewModelScope.launch {
            val token = tokenStorage.getToken().first()
            if (!token.isNullOrBlank()) {
                savingsRepository.getSavings(token)
                    .onSuccess { savings ->
                        val mapped = savings.map { saving ->
                            val dateMillis = try {
                                java.time.LocalDateTime.parse(saving.date)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            
                            FrozenPurchase(
                                purchase = Purchase(saving.item_name, saving.amount.toInt()),
                                freezeUntil = dateMillis,
                                freezeStartedTimestamp = dateMillis,
                                id = saving.id.toString()
                            )
                        }.sortedByDescending { it.freezeUntil }
                        _pastPurchases.value = mapped
                    }
                    .onFailure {
                    }
            }
        }
    }
}
