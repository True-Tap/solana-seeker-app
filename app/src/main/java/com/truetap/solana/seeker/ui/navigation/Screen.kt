package com.truetap.solana.seeker.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Landing : Screen("landing")
    object WalletSelection : Screen("wallet_selection")
    object Home : Screen("home")
    object Wallet : Screen("wallet")
    object Auth : Screen("auth")
} 