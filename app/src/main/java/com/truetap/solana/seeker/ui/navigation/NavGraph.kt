package com.truetap.solana.seeker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.truetap.solana.seeker.ui.screens.splash.SplashScreen
import com.truetap.solana.seeker.ui.screens.landing.LandingScreen
import com.truetap.solana.seeker.ui.screens.wallet.WalletSelectionScreen
import com.truetap.solana.seeker.ui.screens.home.HomeScreen
import com.truetap.solana.seeker.ui.screens.auth.AuthScreen
import com.truetap.solana.seeker.ui.screens.wallet.WalletScreen

@Composable
fun SolanaSeekerNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = { navController.navigate(Screen.Landing.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }}
            )
        }
        
        composable(Screen.Landing.route) {
            LandingScreen(
                onConnectWallet = { navController.navigate(Screen.WalletSelection.route) },
                onTryDemo = { navController.navigate(Screen.Home.route) }
            )
        }
        
        composable(Screen.WalletSelection.route) {
            WalletSelectionScreen(
                onWalletSelected = { walletType ->
                    // TODO: Handle wallet selection
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToWallet = { navController.navigate(Screen.Wallet.route) },
                onNavigateToAuth = { navController.navigate(Screen.Auth.route) }
            )
        }
        
        composable(Screen.Wallet.route) {
            WalletScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 