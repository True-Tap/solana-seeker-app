package com.truetap.solana.seeker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.truetap.solana.seeker.ui.components.MainNavItem
import com.truetap.solana.seeker.ui.components.MainScreenScaffold
import com.truetap.solana.seeker.ui.navigation.rememberMainNavigationHandler

/**
 * Wrapper screens with unified adaptive navigation
 * These wrappers provide consistent navigation across all main screens
 */

@Composable
fun HomeScreenWithNav(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val onNavigate = rememberMainNavigationHandler(navController)
    
    MainScreenScaffold(
        currentScreen = MainNavItem.HOME,
        onNavigate = onNavigate,
        modifier = modifier
    ) { paddingValues ->
        HomeScreenContent(
            paddingValues = paddingValues,
            onNavigateToNFTCheck = { navController.navigate("nft_check") },
            onNavigateToTransactionHistory = { navController.navigate("transaction_history") }
        )
    }
}

@Composable
fun SwapScreenWithNav(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val onNavigate = rememberMainNavigationHandler(navController)
    
    MainScreenScaffold(
        currentScreen = MainNavItem.SWAP,
        onNavigate = onNavigate,
        modifier = modifier
    ) { paddingValues ->
        SwapScreenContent(
            paddingValues = paddingValues
        )
    }
}

@Composable
fun NFTsScreenWithNav(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val onNavigate = rememberMainNavigationHandler(navController)
    
    MainScreenScaffold(
        currentScreen = MainNavItem.NFTS,
        onNavigate = onNavigate,
        modifier = modifier
    ) { paddingValues ->
        NFTsScreenContent(
            paddingValues = paddingValues
        )
    }
}

@Composable
fun ContactsScreenWithNav(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val onNavigate = rememberMainNavigationHandler(navController)
    
    MainScreenScaffold(
        currentScreen = MainNavItem.CONTACTS,
        onNavigate = onNavigate,
        modifier = modifier
    ) { paddingValues ->
        ContactsScreenContent(
            paddingValues = paddingValues,
            onNavigateToAddContact = { navController.navigate("add_contact") },
            onNavigateToContactDetails = { contactId ->
                navController.navigate("contact_details/$contactId")
            },
            onNavigateToBluetooth = { navController.navigate("bluetooth_discovery") }
        )
    }
}

@Composable
fun SettingsScreenWithNav(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val onNavigate = rememberMainNavigationHandler(navController)
    
    MainScreenScaffold(
        currentScreen = MainNavItem.SETTINGS,
        onNavigate = onNavigate,
        modifier = modifier
    ) { paddingValues ->
        SettingsScreenContent(
            paddingValues = paddingValues,
            onNavigateToWalletDetails = { navController.navigate("wallet_details") }
        )
    }
}