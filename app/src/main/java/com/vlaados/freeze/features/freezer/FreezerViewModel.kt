package com.vlaados.freeze.features.freezer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlaados.freeze.core.FrozenPurchase
import com.vlaados.freeze.core.Purchase
import com.vlaados.freeze.data.local.TokenStorage
import com.vlaados.freeze.data.remote.ApiService
import com.vlaados.freeze.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class FreezerViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _frozenPurchases = MutableStateFlow<List<FrozenPurchase>>(emptyList())
    val frozenPurchases = _frozenPurchases.asStateFlow()

    sealed class FreezeEvent {
        data class ShowSuccess(val message: String) : FreezeEvent()
        data class ShowFailure(val message: String) : FreezeEvent()
    }

    private val _events = kotlinx.coroutines.channels.Channel<FreezeEvent>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadFrozenItems() {
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val items = apiService.getFreezes("Bearer $token")
                val mapped = items.map { item ->
                    val startTimeMillis = try {
                        LocalDateTime.parse(item.start_time)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    
                    val durationMillis = item.duration_seconds * 1000L
                    
                    FrozenPurchase(
                        purchase = Purchase(item.item_name, item.amount?.toInt() ?: 0),
                        freezeUntil = startTimeMillis + durationMillis,
                        freezeStartedTimestamp = startTimeMillis,
                        id = item.id.toString()
                    )
                }.sortedBy { it.freezeUntil }
                _frozenPurchases.value = mapped
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onPurchaseAborted(purchaseId: String) {
        processPurchaseDecision(purchaseId, isBreakdown = false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onPurchaseConfirmed(purchaseId: String) {
        processPurchaseDecision(purchaseId, isBreakdown = true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processPurchaseDecision(purchaseId: String, isBreakdown: Boolean) {
        val item = _frozenPurchases.value.find { it.id == purchaseId } ?: return
        
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                
                val saving = com.vlaados.freeze.data.model.Saving(
                    id = 0,
                    user_id = 0,
                    item_name = item.purchase.name,
                    date = LocalDateTime.now().toString(),
                    amount = item.purchase.price.toDouble(),
                    is_breakdown = isBreakdown
                )
                apiService.addSaving("Bearer $token", saving)

                if (!isBreakdown) {
                    userRepository.addSavingsToGoal(token, item.purchase.price.toDouble())
                }

                val idInt = try { purchaseId.toInt() } catch(e: Exception) { 0 }
                if (idInt != 0) {
                    apiService.deleteFreeze("Bearer $token", idInt)
                }

                _frozenPurchases.update { list -> list.filterNot { it.id == purchaseId } }
                
                if (!isBreakdown) {
                    _events.send(FreezeEvent.ShowSuccess("Молодец! Ты передумал и сэкономил деньги."))
                } else {
                    _events.send(FreezeEvent.ShowFailure("Эх... Срыв засчитан. Попробуй в следующий раз продержаться дольше."))
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
