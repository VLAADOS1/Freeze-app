package com.vlaados.freeze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlaados.freeze.data.local.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    init {
        viewModelScope.launch {
            _isLoggedIn.value = tokenStorage.getToken().first() != null
        }
    }

    fun onLoginSuccess() {
        _isLoggedIn.value = true
    }

    fun logout() {
        viewModelScope.launch {
            tokenStorage.clearToken()
            _isLoggedIn.value = false
        }
    }
}
