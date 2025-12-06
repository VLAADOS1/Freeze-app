package com.vlaados.freeze.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlaados.freeze.data.local.TokenStorage
import com.vlaados.freeze.data.model.User
import com.vlaados.freeze.data.repository.AuthRepository
import com.vlaados.freeze.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState = _updateProfileState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val token = tokenStorage.getToken().first()
            if (!token.isNullOrBlank()) {
                loadUserProfile(token)
            }
            _isLoading.value = false
        }
    }

    private fun getUserFriendlyErrorMessage(throwable: Throwable): String {
        val message = throwable.message ?: return "Произошла неизвестная ошибка"
        
{
            message.contains("401") -> "Неверный логин или пароль"
            message.contains("400") -> "Неверные данные или пользователь уже существует"
            message.contains("404") -> "Сервер не найден"
            message.contains("500") -> "Внутренняя ошибка сервера"
            message.contains("Connect") || message.contains("Socket") || message.contains("UnknownHost") -> "Ошибка подключения. Проверьте интернет."
            message.contains("timeout", ignoreCase = true) -> "Превышено время ожидания ответа"
 {
                 try {
                     val regex = "\"detail\"\\s*:\\s*\"(.*?)\"".toRegex()
                     val match = regex.find(message)
                     match?.groupValues?.get(1) ?: "Ошибка: $message"
                 } catch (e: Exception) {
                     "Ошибка: $message"
                 }
            }
            else -> "Ошибка: $message"
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authRepository.login(username, password)
                .onSuccess { loginResponse ->
                    tokenStorage.saveToken(loginResponse.access_token)
                    loadUserProfile(loginResponse.access_token)
                }
                .onFailure { throwable ->
                    val friendlyMessage = getUserFriendlyErrorMessage(throwable)
                    _loginState.value = LoginState.Error(friendlyMessage)
                }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            authRepository.register(username, password)
                .onSuccess {
                    authRepository.login(username, password)
                        .onSuccess { loginResponse ->
                            tokenStorage.saveToken(loginResponse.access_token)
                            userRepository.getMe(loginResponse.access_token)
                                .onSuccess { user ->
                                    _userProfile.value = user
                                    _registrationState.value = RegistrationState.Success
                                }
                                .onFailure { throwable ->
                                     val friendlyMessage = getUserFriendlyErrorMessage(throwable)
                                     _registrationState.value = RegistrationState.Error("Регистрация успешна, но вход не удался: $friendlyMessage")
                                }
                        }
                        .onFailure { throwable ->
                             val friendlyMessage = getUserFriendlyErrorMessage(throwable)
                             _registrationState.value = RegistrationState.Error("Регистрация успешна, но вход не удался: $friendlyMessage")
                        }
                }
                .onFailure { throwable ->
                    val friendlyMessage = getUserFriendlyErrorMessage(throwable)
                    _registrationState.value = RegistrationState.Error(friendlyMessage)
                }
        }
    }

    fun loginAnonymously() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            val randomHash = java.util.UUID.randomUUID().toString().filter { it.isLetterOrDigit() }.take(7)
            
            authRepository.register(randomHash, randomHash)
                .onSuccess {
                    login(randomHash, randomHash)
                }
                .onFailure { throwable ->
                     val sw = StringWriter()
                     throwable.printStackTrace(PrintWriter(sw))
                     _loginState.value = LoginState.Error("Anonymous login failed: ${sw.toString()}")
                }
        }
    }

    private suspend fun loadUserProfile(token: String) {
        userRepository.getMe(token)
            .onSuccess {
                _userProfile.value = it
                _loginState.value = LoginState.Success
            }
            .onFailure { throwable ->
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                _loginState.value = LoginState.Error("Failed to load user profile: ${sw.toString()}")
            }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            _updateProfileState.value = UpdateProfileState.Loading
            val token = tokenStorage.getToken().first()
            if (token != null && user.id != null) {
                userRepository.updateUser(token, user.id, user)
                    .onSuccess { updatedUser ->
                        _userProfile.value = updatedUser
                        _updateProfileState.value = UpdateProfileState.Success
                    }
                    .onFailure { throwable ->
                        val sw = StringWriter()
                        throwable.printStackTrace(PrintWriter(sw))
                        _updateProfileState.value = UpdateProfileState.Error(sw.toString())
                    }
            } else {
                _updateProfileState.value = UpdateProfileState.Error("Token or User ID is null")
            }
        }
    }

    fun dismissError() {
        _loginState.value = LoginState.Idle
        _registrationState.value = RegistrationState.Idle
    }

    fun dismissUpdateProfileError() {
        _updateProfileState.value = UpdateProfileState.Idle
    }

    fun resetSavedForGoal() {
        val currentUser = _userProfile.value
        if (currentUser != null) {
            updateUserProfile(currentUser.copy(saved_for_goal = 0.0))
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenStorage.clearToken()
            _userProfile.value = null
            _loginState.value = LoginState.Idle
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}
