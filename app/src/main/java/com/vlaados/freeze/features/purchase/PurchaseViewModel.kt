package com.vlaados.freeze.features.purchase

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.vlaados.freeze.data.local.TokenStorage
import com.vlaados.freeze.data.remote.AiRequest
import com.vlaados.freeze.data.remote.ApiService
import com.vlaados.freeze.data.remote.LinkRequest
import com.vlaados.freeze.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FreezeOption(
    val label: String,
    val duration_seconds: Int
)

data class AiVerdict(
    val verdict_type: String?, 
    val comment: String?,
    val chat_starter: String? = null,
    val text_purchased: String? = null,
    val text_rejected: String? = null,
    val freeze_options: List<FreezeOption>? = null,
    val extracted_name: String? = null,
    val extracted_price: Any? = null
)

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userRepository: UserRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _verdict = MutableStateFlow<AiVerdict?>(null)
    val verdict: StateFlow<AiVerdict?> = _verdict

    data class PurchaseContext(
        val name: String,
        val price: String,
        val emotions: String
    )

    private val _currentPurchase = MutableStateFlow<PurchaseContext?>(null)
    val currentPurchase: StateFlow<PurchaseContext?> = _currentPurchase

    private val _costInTime = MutableStateFlow<String?>(null)
    val costInTime: StateFlow<String?> = _costInTime

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    fun clearError() { _validationError.value = null }

    fun analyzePurchase(
        productName: String,
        price: String,
        emotions: String,
        isLinkMode: Boolean = false,
        onSuccess: () -> Unit
    ) {
        _currentPurchase.value = PurchaseContext(productName, price, emotions)
        
        if (isLinkMode) {
             if (productName.isBlank() || emotions.isBlank()) {
                 _validationError.value = "Заполните ссылку и комментарий"
                 return
             }
        } else {
             if (productName.isBlank() || price.isBlank() || emotions.isBlank()) {
                 _validationError.value = "Заполните все поля:\nНазвание, цену и эмоции"
                 return
             }
        }
        
        viewModelScope.launch {
            val token = tokenStorage.getToken().first()
            if (token != null) {
                val userProfile = userRepository.getMe(token).getOrNull()
                val income = userProfile?.income ?: 0.0
                val priceValue = price.toDoubleOrNull() ?: 0.0

                if (income > 0 && priceValue > 0) {
                    val hourlyRate = income / 165.0
                    val hoursNeeded = priceValue / hourlyRate
                    
                    _costInTime.value = when {
                        hoursNeeded < 1.0 / 60.0 -> {
                            val seconds = (hoursNeeded * 3600).toInt()
                            "$seconds ${getPlural(seconds, "секунда", "секунды", "секунд")}"
                        }
                        hoursNeeded < 1 -> {
                            val minutes = (hoursNeeded * 60).toInt()
                            "$minutes ${getPlural(minutes, "минута", "минуты", "минут")}"
                        }
                        hoursNeeded < 8 -> {
                             val hoursRounded = kotlin.math.round(hoursNeeded * 10) / 10.0
                             if (hoursRounded % 1.0 == 0.0) {
                                 val h = hoursRounded.toInt()
                                 "$h ${getPlural(h, "час", "часа", "часов")}"
                             } else {
                                 "$hoursRounded часа"
                             }
                        }
                        else -> {
                            val days = kotlin.math.round(hoursNeeded / 8.0).toInt()
                            "почти $days ${getPlural(days, "день", "дня", "дней")}"                            
                        }
                    }
                } else {
                    _costInTime.value = null
                }
            }
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenStorage.getToken().first()

                if (token == null) {
                    _validationError.value = "Ошибка авторизации 🔒"
                    _isLoading.value = false
                    return@launch
                }

                val userProfile = userRepository.getMe(token).getOrNull()
                
                if (isLinkMode) {
                    var productInfo = "Информация не получена"
                    try {
                        val linkResponse = apiService.ask(com.vlaados.freeze.data.remote.AskRequest(message = productName))
                        if (linkResponse.isSuccessful) {
                            productInfo = linkResponse.body()?.response ?: "Пустой ответ"
                        } else {
                            productInfo = "Ошибка получения данных: ${linkResponse.code()}"
                        }
                    } catch (e: Exception) {
                        Log.e("PurchaseViewModel", "Error parsing link", e)
                        productInfo = "Ошибка загрузки: ${e.message}"
                    }

                    val prompt = """
                        Проанализируй этот товар по ссылке.
                        Ссылка: $productName
                        Информация, полученная с сайта по ссылке:
                        $productInfo
                        
                        Комментарий пользователя: $emotions
                        
                        Информация о пользователе:
                        Имя: ${userProfile?.name ?: "Н/Д"}
                        Доход: ${ if ((userProfile?.income ?: 0.0) > 0) userProfile?.income else "Не указан" }
                        Цель: ${userProfile?.goal_name ?: "Нет"}
                        Слабость: ${userProfile?.weakness ?: "Нет"}
                        
                        ТВОЯ ЗАДАЧА:
                        1. Попытайся понять, что это за товар, используя "Информация, полученная с сайта по ссылке".
                        2. Оцени его примерную стоимость, если она есть в информации.
                        3. АНАЛИЗ СОДЕРЖИМОГО:
                           - Если ссылка битая или информация пустая -> verdict_type="unclear".
                           - Если это ТОВАР (любой) -> ВЕРДИКТ ВСЕГДА: "impulsive" (Импульсивная покупка).
                             
                             ИНСТРУКЦИЯ ДЛЯ КОММЕНТАРИЯ (если impulsive):
                             ТЫ - ЖАДНАЯ ЖАБА ПИВНОГО ЛАРЬКА. ТЫ НЕНАВИДИШЬ, КОГДА ТРАТЯТ ДЕНЬГИ.
                             Твоя задача - ЖЕСТКО ОТГОВОРИТЬ и высмеять желание купить.
                             Никаких "ну вещь прикольная". ТОЛЬКО КРИТИКА. Найди минусы: "тряпка", "безделушка", "пылесборник", "деньги на ветер".
                             В КОНЦЕ ОБЯЗАТЕЛЬНО: Прямой призыв не покупать (например: "Это полный бред, не покупай!").
                        
                        Ответь СТРОГО JSON:
                        {
                            "verdict_type": "impulsive" | "unclear",
                            "comment": "Твой ЕДКИЙ и ПЛОХОЙ комментарий",
                            "chat_starter": "Сообщение от лица Жабы Фризи (или null если unclear)",
                            "text_purchased": "Текст разочарования (или null если unclear)",
                            "text_rejected": "Текст похвалы (или null если unclear)",
                            "extracted_name": "Название товара (коротко) или null",
                            "extracted_price": "Примерная цена (числом или строкой) или null",
                             "freeze_options": [
                                {"label": "10 минут", "duration_seconds": 600},
                                {"label": "1 час", "duration_seconds": 3600},
                                {"label": "24 часа", "duration_seconds": 86400},
                                {"label": "1 неделя", "duration_seconds": 604800}
                            ]
                        }
                    """.trimIndent()
                    
                    val systemPrompt = "Ты - строгий, но справедливый финансовый помощник. Твоя задача - уберечь пользователя от ненужных трат."
                    
                    val response = apiService.askAi(AiRequest(prompt, systemPrompt))
                    
                    val output = response.output
                    val jsonString = when (output) {
                        is List<*> -> output.joinToString("")
                        is String -> output
                        // Handle potential map if the old API also does weird things, though askAi usually returns string/list
                        is Map<*, *> -> {
                             if (output.containsKey("output")) {
                                 val inner = output["output"]
                                 if (inner is List<*>) inner.joinToString("") else inner.toString()
                             } else {
                                 Gson().toJson(output)
                             }
                        }
                        else -> output.toString()
                    }

                    val cleanJson = jsonString.replace("```json", "").replace("```", "").trim()
                    val verdictObj = Gson().fromJson(cleanJson, AiVerdict::class.java)
                    
                    if (verdictObj == null || (verdictObj.verdict_type == null && verdictObj.comment == null)) {
                        _validationError.value = "Ошибка: некорректный ответ от AI"
                        _isLoading.value = false
                        return@launch
                    }
                    
                    _verdict.value = verdictObj
                    
                    if (!verdictObj.extracted_name.isNullOrBlank()) {
                         val priceStr = when(val p = verdictObj.extracted_price) {
                             is String -> p
                             is Number -> p.toString()
                             else -> p?.toString() ?: ""
                         }
                         
                         _currentPurchase.value = _currentPurchase.value?.copy(
                             name = verdictObj.extracted_name,
                             price = if(priceStr.isNotBlank()) priceStr else (_currentPurchase.value?.price ?: "")
                         )
                    }

                    onSuccess()
                    return@launch
                }

                val prompt = """
                    Пользователь *хочет* купить (еще не купил) следующий товар. Проанализируй это намерение.
                    
                    Пользователь:
                    Имя: ${userProfile?.name ?: "Н/Д"}
                    Доход: ${ if ((userProfile?.income ?: 0.0) > 0) userProfile?.income else "Не указан" }
                    Цель: ${userProfile?.goal_name ?: "Нет"} (накоплено ${ if ((userProfile?.saved_for_goal ?: 0.0) >= 0 && (userProfile?.monthly_savings ?: -1.0) > 0) userProfile?.saved_for_goal else "Не указано (или 0)" } из ${userProfile?.goal_amount})
                    Слабость: ${userProfile?.weakness ?: "Нет"}
                    СПИСОК ЗАПРЕЩЕННЫХ ТОВАРОВ (SELF_BAN): ${userProfile?.self_ban ?: "Нет"}
                    
                    ВАЖНО: Пользователь указал свой личный промпт: "${userProfile?.user_prompt ?: "Нет"}". Обязательно учитывай его пожелания к стилю или содержанию ответа.
                    
                    Товар: $productName
                    Цена: ${if (price.isBlank()) "Не указана (это ссылка, оцени примерную стоимость или сам факт желания)" else price}
                    Мотив: $emotions
                    
                    Ответь СТРОГО JSON:
                    {
                        "verdict_type": "impulsive" | "rational" | "unclear",
                        "comment": "Текст комментария",
                        "chat_starter": "Сообщение для чата или null",
                        "text_purchased": "Текст если купил (стыд) или null",
                        "text_rejected": "Текст если отказался (похвала) или null",
                        "freeze_options": [
                            {"label": "10 минут", "duration_seconds": 600},
                            {"label": "1 час", "duration_seconds": 3600},
                            {"label": "24 часа", "duration_seconds": 86400},
                            {"label": "1 неделя", "duration_seconds": 604800}
                        ]
                    }
                    
                    Дополнительно для "freeze_options":
                    Предложи 4 варианта времени "заморозки" (откладывания покупки), чтобы пользователь остыл.
                    - Если товар дорогой или импульсивный -> предлагай длительное время (часы, дни).
                    - Если мелочь -> минуты, часы.
                    
                    Правила анализа:
                    1. verdict_type="unclear": 
                       - Если название товара или мотив выглядят как бред, тест, набор букв (например "ыва", "тест", "asdf", "ляяя").
                       - Если цена абсолютно нереалистична (слишком огромная или отрицательная).
                       - Если похоже, что пользователь просто тыкает кнопки.
                       Комментарий: Иронично подмети, что пользователь пишет ерунду.
                       chat_starter: null
                       text_purchased: null
                       text_rejected: null
                       
                    2. ПРОВЕРКА САМОЗАПРЕТА (ПРИОРИТЕТ 1): 
                       Проверь, входит ли "$productName" или его категория в "СПИСОК ЗАПРЕЩЕННЫХ ТОВАРОВ".
                       ЕСЛИ ДА (Товар запрещен):
                       - verdict_type ОБЯЗАТЕЛЬНО "impulsive".
                       - Комментарий: Начни с крика "ТЫ ЖЕ ЭТО ЗАБАНИЛ!". Ты обязан ЗАПРЕТИТЬ покупку.
                       - chat_starter: "Ты просил меня ни в коем случае не дать купить тебе это! Вспомни! Откажись от покупки!"
                       - text_purchased: "Ты предал сам себя... Зачем тогда список запретов писал? Разочарование..."
                       - text_rejected: "Фух! Молодец, что одумался. Самозапрет сработал!"

                    3. В остальных случаях (если данные похожи на правду и товар НЕ запрещен):
                       - verdict_type="impulsive": 
                         Комментарий: Если покупка выглядит эмоциональной, ненужной или вредной для бюджета/цели, будь РЕЗКИМ, жестким, используй сарказм. Твоя цель - ОТГОВОРИТЬ. В конце обязательно призови отказаться от покупки.
                         chat_starter: Напиши короткое (1-2 предложения) сообщение от лица Жабы Фризи, чтобы начать спор. Обязательно закончи ВОПРОСОМ, который заставит пользователя оправдываться.
                         - text_purchased: Грустный, стыдящий текст (1-2 предл). Пример: "Ну вот... Опять деньги на ветер. А ведь могли стать богаче."
                         - text_rejected: Радостный, хвалебный текст (1-2 предл). Пример: "Ура! Победа над маркетингом! Горжусь тобой, эти деньги пойдут на Цель!"
                       
                       - verdict_type="rational": 
                         Комментарий: Если это базовая потребность или оправданная трата, коротко поддержи.
                         chat_starter: null
                         text_purchased: "Правильный выбор. Это полезная покупка."
                         text_rejected: "Ну, хозяин - барин. Сэкономил - считай заработал."
                    
                    4. Общее:
                       - БЕЗ приветствий.
                       - НЕ используй длинные тире (—) и двоеточия (:).
                       - Максимум 3 предложения в комментарии.
                """.trimIndent()

                val systemPrompt = "Ты - строгий, но справедливый финансовый помощник. Твоя задача - уберечь пользователя от ненужных трат."

                val response = apiService.askAi(AiRequest(prompt, systemPrompt))
                
                val output = response.output
                val jsonString = when (output) {
                    is List<*> -> output.joinToString("")
                    is String -> output
                    else -> output.toString()
                }

                val cleanJson = jsonString.replace("```json", "").replace("```", "").trim()
                
                val verdictObj = Gson().fromJson(cleanJson, AiVerdict::class.java)
                _verdict.value = verdictObj
                onSuccess()

            } catch (e: Exception) {
                Log.e("PurchaseViewModel", "Error analyzing purchase", e)
                _validationError.value = "Ошибка анализа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun confirmRefusal(onSuccess: () -> Unit) {
        val current = _currentPurchase.value ?: return
        val currentVerdict = _verdict.value
        
        if (currentVerdict?.verdict_type != "impulsive") {
            onSuccess()
            return
        }

        val price = current.price.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        if (price == 0.0) { onSuccess(); return }

        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                
                val saving = com.vlaados.freeze.data.model.Saving(
                    id = 0,
                    user_id = 0,
                    item_name = current.name,
                    date = java.time.LocalDateTime.now().toString(),
                    amount = price,
                    is_breakdown = false
                )
                apiService.addSaving("Bearer $token", saving)


                userRepository.addSavingsToGoal(token, price)
r
                try {
                    val groupsResponse = apiService.getMyGroups("Bearer $token")
                    if (groupsResponse.isSuccessful) {
                        val groups = groupsResponse.body()
                        groups?.forEach { group ->
                            apiService.saveForGroupGoal("Bearer $token", group.id, price)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PurchaseViewModel", "Error updating group savings", e)
                    // Non-critical failure, don't block success
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                 Log.e("PurchaseViewModel", "Error saving refusal", e)
                 kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess() }
            }
        }
    }

    fun freezeItem(durationSeconds: Int, onSuccess: () -> Unit) {
        val current = _currentPurchase.value ?: return
        val price = current.price.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val freeze = com.vlaados.freeze.data.model.FreezeItem(
                    item_name = current.name,
                    start_time = java.time.LocalDateTime.now().toString(),
                    duration_seconds = durationSeconds,
                    is_frozen = true,
                    amount = price
                )
                apiService.addFreeze("Bearer $token", freeze)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                 Log.e("PurchaseViewModel", "Error freezing item", e)
                 kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess() }
            }
        }
    }

    fun confirmBreakdown(onSuccess: () -> Unit) {
        val current = _currentPurchase.value ?: return
        val price = current.price.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        if (price == 0.0) { onSuccess(); return }

        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val saving = com.vlaados.freeze.data.model.Saving(
                    id = 0,
                    user_id = 0,
                    item_name = current.name,
                    date = java.time.LocalDateTime.now().toString(),
                    amount = price,
                    is_breakdown = true
                )
                apiService.addSaving("Bearer $token", saving)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                 Log.e("PurchaseViewModel", "Error saving breakdown", e)
                 kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess() }
            }
        }
    }

    fun resetVerdict() {
        _verdict.value = null
        _costInTime.value = null
    }

    private fun getPlural(n: Int, form1: String, form2: String, form5: String): String {
        val n10 = n % 10
        val n100 = n % 100
        return when {
            n100 in 11..19 -> form5
            n10 == 1 -> form1
            n10 in 2..4 -> form2
            else -> form5
        }
    }
}
