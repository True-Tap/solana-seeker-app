package com.truetap.solana.seeker.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.truetap.solana.seeker.ui.screens.home.HomeScreen as OriginalHomeScreen
import com.truetap.solana.seeker.ui.screens.payment.EnhancedSwapScreen as OriginalEnhancedSwapScreen
import com.truetap.solana.seeker.ui.screens.payment.SwapScreen as NewSwapScreen
import com.truetap.solana.seeker.ui.screens.nft.NFTsScreen as OriginalNFTsScreen
import com.truetap.solana.seeker.ui.screens.contacts.ContactsScreen as OriginalContactsScreen
import com.truetap.solana.seeker.ui.screens.settings.SettingsScreen as OriginalSettingsScreen

/**
 * Content wrappers for existing screens
 * These adapt the existing screens to work with the new unified navigation
 */

@Composable
fun HomeScreenContent(
    paddingValues: PaddingValues,
    onNavigateToNFTCheck: () -> Unit = {},
    onNavigateToTransactionHistory: () -> Unit = {}
) {
    OriginalHomeScreen(
        onNavigateToNFTCheck = onNavigateToNFTCheck,
        onNavigateToSwap = {}, // Handled by unified nav
        onNavigateToNFTs = {}, // Handled by unified nav
        onNavigateToContacts = {}, // Handled by unified nav
        onNavigateToSettings = {}, // Handled by unified nav
        onNavigateToTransactionHistory = onNavigateToTransactionHistory,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
}

@Composable
fun SwapScreenContent(
    paddingValues: PaddingValues
) {
    NewSwapScreen(
        onNavigateBack = {}, // Not needed with bottom nav
        onNavigateToHome = {}, // Handled by unified nav
        onNavigateToContacts = {}, // Handled by unified nav
        onNavigateToNFTs = {}, // Handled by unified nav
        onNavigateToSettings = {}, // Handled by unified nav
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
}

@Composable
fun EnhancedSwapScreenContent(
    paddingValues: PaddingValues,
    onNavigateToSchedule: () -> Unit = {}
) {
    OriginalEnhancedSwapScreen(
        onNavigateBack = {}, // Not needed with bottom nav
        onNavigateToHome = {}, // Handled by unified nav
        onNavigateToContacts = {}, // Handled by unified nav
        onNavigateToNFTs = {}, // Handled by unified nav
        onNavigateToSettings = {}, // Handled by unified nav
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
}

@Composable
fun NFTsScreenContent(
    paddingValues: PaddingValues
) {
    OriginalNFTsScreen(
        onNavigateBack = {}, // Not needed with bottom nav
        onNavigateToHome = {}, // Handled by unified nav
        onNavigateToSwap = {}, // Handled by unified nav
        onNavigateToContacts = {}, // Handled by unified nav
        onNavigateToSettings = {}, // Handled by unified nav
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
}

@Composable
fun ContactsScreenContent(
    paddingValues: PaddingValues,
    onNavigateToAddContact: () -> Unit = {},
    onNavigateToContactDetails: (String) -> Unit = {},
    onNavigateToBluetooth: () -> Unit = {}
) {
    OriginalContactsScreen(
        onNavigateBack = {}, // Not needed with bottom nav
        onNavigateToHome = {}, // Handled by unified nav
        onNavigateToSwap = {}, // Handled by unified nav
        onNavigateToNFTs = {}, // Handled by unified nav
        onNavigateToSettings = {}, // Handled by unified nav
        onNavigateToAddContact = onNavigateToAddContact,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
}

@Composable
fun SettingsScreenContent(
    paddingValues: PaddingValues,
    onNavigateToWalletDetails: () -> Unit = {}
) {
    OriginalSettingsScreen(
        onNavigateBack = {}, // Not needed with bottom nav
        onNavigateToHome = {}, // Handled by unified nav
        onNavigateToSwap = {}, // Handled by unified nav
        onNavigateToNFTs = {}, // Handled by unified nav
        onNavigateToContacts = {}, // Handled by unified nav
        onNavigateToWalletSuccess = {}, // Handled separately
        onNavigateToWalletFailure = {}, // Handled separately
        onNavigateToNFTSuccess = {}, // Handled separately
        onNavigateToNFTFailure = {}, // Handled separately
        onNavigateToGenesisToken = {}, // Handled separately
        onNavigateToOnboarding = {}, // Handled separately
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
}