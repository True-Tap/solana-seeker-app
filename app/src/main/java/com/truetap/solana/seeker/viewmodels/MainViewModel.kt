package com.truetap.solana.seeker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.data.AuthState
import com.truetap.solana.seeker.repositories.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _startDestination = MutableStateFlow("splash")
    val startDestination: StateFlow<String> = _startDestination.asStateFlow()

    val authState: StateFlow<AuthState> = walletRepository.authState

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            // Attempt to restore session
            val sessionResult = walletRepository.restoreSession()
            
            when (sessionResult) {
                is com.truetap.solana.seeker.data.WalletResult.Success -> {
                    // User has an active session, go directly to home
                    _startDestination.value = "home"
                }
                is com.truetap.solana.seeker.data.WalletResult.Error -> {
                    // No session found, start from splash/onboarding
                    _startDestination.value = "splash"
                }
            }
            
            _isInitialized.value = true
        }
    }
    
    fun forceOnboarding() {
        // Force user through onboarding flow (useful for testing or logout)
        _startDestination.value = "splash"
        _isInitialized.value = true
    }
}