package com.vlaados.freeze.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlaados.freeze.data.local.TokenStorage
import com.vlaados.freeze.data.remote.AiRequest
import com.vlaados.freeze.data.remote.ApiService
import com.vlaados.freeze.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val role: String,
    val content: String,
    val isTyping: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userRepository: UserRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastDebugResponse = MutableStateFlow<String?>(null)
    val lastDebugResponse: StateFlow<String?> = _lastDebugResponse.asStateFlow()

    fun clearDebug() { _lastDebugResponse.value = null }

    private var productContext: String = ""
    private var userContext: String = ""

    fun initChat(productName: String, price: String, emotions: String, starter: String?) {
        if (_messages.value.isNotEmpty()) return

        productContext = "Товар: $productName, Цена: $price, Мотив: $emotions"
        
        if (!starter.isNullOrBlank()) {
            _messages.value = listOf(ChatMessage("assistant", starter))
        }
        
        viewModelScope.launch {
            val token = tokenStorage.getToken().first()
            if (token != null) {
                userRepository.getMe(token).getOrNull()?.let { profile ->
                    userContext = """
                        Имя: ${profile.name}
                        Доход: ${profile.income}
                        Цель: ${profile.goal_name}
                        Слабость: ${profile.weakness}
                        САМОЗАПРЕТЫ (ТО ЧТО НЕЛЬЗЯ ПОКУПАТЬ): ${profile.self_ban ?: "Нет"}
                        ${if (!profile.user_prompt.isNullOrBlank()) "ВАЖНО - ЛИЧНЫЙ ПРОМПТ ПОЛЬЗОВАТЕЛЯ: ${profile.user_prompt}" else ""}
                    """.trimIndent()
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val currentList = _messages.value.toMutableList()
        currentList.add(ChatMessage("user", text))
        _messages.value = currentList
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val history = _messages.value.joinToString("\n") { msg ->
                    "${if (msg.role == "user") "Пользователь" else "Фризи"}: ${msg.content}"
                }

                val prompt = """
                    Контекст диалога:
                    Ты - Жаба Фризи, жадный, саркастичный и строгий финансовый помощник.
                    Ты разговариваешь с пользователем (инфо: $userContext) о его попытке купить: $productContext.
                    Твоя задача - отговорить его тратить деньги. Используй юмор, сарказм и давление на совесть.
                    
                    История диалога:
                    $history
                    
                    Ответь на последнюю реплику пользователя. 
                    ОТВЕТЬ ТОЛЬКО ТЕКСТОМ ОТВЕТА (без "Фризи:" и т.д.).
                    Максимум 2-3 предложения.
                """.trimIndent()

                val systemPrompt = "Ты - Жаба Фризи. Ты жадный, вредный, но заботливый. Ты ненавидишь траты."
                
                val response = apiService.askAi(AiRequest(prompt, systemPrompt))
                
                val output = response.output
                val replyRaw = if (output is List<*>) output.joinToString("") else output.toString()
                val reply = replyRaw.replace(Regex("\\s+"), " ").trim()
                
                _lastDebugResponse.value = "Raw Output Type: ${output?.javaClass?.simpleName ?: "Null"}\n\nJoined: $replyRaw\n\nFinal: $reply"
                
                val updatedList = _messages.value.toMutableList()
                updatedList.add(ChatMessage("assistant", reply))
                _messages.value = updatedList

            } catch (e: Exception) {
                 val updatedList = _messages.value.toMutableList()
                updatedList.add(ChatMessage("assistant", "Ква... Интернет пропал. Но деньги я тебе не дам потратить!"))
                _messages.value = updatedList
                _lastDebugResponse.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
