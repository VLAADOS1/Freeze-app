package com.vlaados.freeze.features.onboarding

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

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    fun updateUserName(name: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val token = tokenStorage.getToken().first()
            if (token == null) {
                _updateState.value = UpdateState.Error("Token not found")
                return@launch
            }

            userRepository.getMe(token).fold(
                onSuccess = { currentUser ->
                    currentUser.id?.let { userId ->
                        val userToUpdate = currentUser.copy(name = name)
                        userRepository.updateUser(token, userId, userToUpdate).fold(
                            onSuccess = {
                                _updateState.value = UpdateState.Success
                                onFinished()
                            },
                            onFailure = {
                                _updateState.value = UpdateState.Error(it.message ?: "Unknown error")
                            }
                        )
                    } ?: run {
                        _updateState.value = UpdateState.Error("User ID not found")
                    }
                },
                onFailure = {
                    _updateState.value = UpdateState.Error(it.message ?: "Failed to get user")
                }
            )
        }
    }
}
