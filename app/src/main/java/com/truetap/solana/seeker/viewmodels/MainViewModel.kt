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
            // Always start from splash screen which shows "TrueTap Payments. Reimagined." for 2.5s
            // then navigates to landing screen with terms & conditions and wallet connection
            _startDestination.value = "splash"
            _isInitialized.value = true
        }
    }
    
    fun forceOnboarding() {
        // Force user through onboarding flow (useful for testing or logout)
        _startDestination.value = "splash"
        _isInitialized.value = true
    }
}