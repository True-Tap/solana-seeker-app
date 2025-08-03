package com.truetap.solana.seeker.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.truetap.solana.seeker.ui.screens.splash.SplashScreen
import com.truetap.solana.seeker.ui.screens.landing.LandingScreen
import com.truetap.solana.seeker.ui.screens.home.HomeScreen
import com.truetap.solana.seeker.ui.screens.nft.NFTCheckScreen
import com.truetap.solana.seeker.ui.screens.nft.NFTSuccessScreen
import com.truetap.solana.seeker.ui.screens.nft.NFTFailureScreen
import com.truetap.solana.seeker.ui.screens.wallet.WalletConnectionScreen
import com.truetap.solana.seeker.ui.screens.wallet.PairingScreen
import com.truetap.solana.seeker.ui.screens.wallet.WalletSuccessScreen
import com.truetap.solana.seeker.ui.screens.wallet.WalletFailureScreen
import com.truetap.solana.seeker.ui.screens.welcome.WelcomeScreen
import com.truetap.solana.seeker.ui.screens.genesis.GenesisTokenScreen
import com.truetap.solana.seeker.ui.screens.onboarding.OnboardingScreen
import com.truetap.solana.seeker.ui.screens.dashboard.DashboardScreen
import com.truetap.solana.seeker.ui.screens.contacts.ContactsScreen
import com.truetap.solana.seeker.ui.screens.contacts.ContactDetailsScreen
import com.truetap.solana.seeker.ui.screens.payment.PaymentScreen
import com.truetap.solana.seeker.ui.screens.payment.SwapScreen
import com.truetap.solana.seeker.ui.screens.payment.SendPaymentScreen
import com.truetap.solana.seeker.ui.screens.payment.SchedulePaymentScreen
import com.truetap.solana.seeker.ui.screens.nft.NFTsScreen
import com.truetap.solana.seeker.ui.screens.settings.SettingsScreen
import com.truetap.solana.seeker.ui.screens.bluetooth.BluetoothDiscoveryScreen
import com.truetap.solana.seeker.ui.screens.transaction.TransactionHistoryScreen

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
        // 1. SPLASH SCREEN
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = { 
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 2. LANDING SCREEN
        composable(Screen.Landing.route) {
            LandingScreen(
                onNavigateToHome = { 
                    navController.navigate(Screen.WalletConnection.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 2. WELCOME SCREEN
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigate = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 3. WALLET CONNECTION SCREEN
        composable(Screen.WalletConnection.route) {
            WalletConnectionScreen(
                onNavigateToPairing = { walletId ->
                    navController.navigate(Screen.WalletPairing.createRoute(walletId))
                },
                onNavigateToNext = { 
                    // This should not be used in the main flow
                    navController.navigate(Screen.GenesisToken.route) {
                        popUpTo(Screen.WalletConnection.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 4. WALLET PAIRING SCREEN
        composable(
            route = Screen.WalletPairing.route,
            arguments = listOf(
                navArgument("walletId") { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            PairingScreen(
                walletId = walletId,
                onNavigateToSuccess = { 
                    navController.navigate(Screen.WalletSuccess.route) {
                        popUpTo(Screen.WalletPairing.route) { inclusive = true }
                    }
                },
                onNavigateToFailure = { walletId, errorMessage ->
                    navController.navigate(Screen.WalletFailure.createRoute(walletId, errorMessage)) {
                        popUpTo(Screen.WalletPairing.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 5. WALLET SUCCESS SCREEN
        composable(Screen.WalletSuccess.route) {
            WalletSuccessScreen(
                onNavigateToNext = { 
                    navController.navigate(Screen.GenesisToken.route) {
                        popUpTo(Screen.WalletSuccess.route) { inclusive = true }
                    }
                },
                onViewDetails = { 
                    navController.navigate(Screen.Home.route)
                }
            )
        }
        
        // 6. WALLET FAILURE SCREEN
        composable(
            route = Screen.WalletFailure.route,
            arguments = listOf(
                navArgument("walletId") { 
                    type = NavType.StringType 
                },
                navArgument("errorMessage") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            val errorMessage = backStackEntry.arguments?.getString("errorMessage")
            WalletFailureScreen(
                walletId = walletId,
                errorMessage = errorMessage,
                onTryAgain = { 
                    navController.navigate(Screen.WalletPairing.createRoute(walletId)) {
                        popUpTo(Screen.WalletFailure.route) { inclusive = true }
                    }
                },
                onTryDifferentWallet = { 
                    navController.navigate(Screen.WalletConnection.route) {
                        popUpTo(Screen.WalletFailure.route) { inclusive = true }
                    }
                },
                onGetHelp = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.WalletFailure.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 7. GENESIS TOKEN SCREEN
        composable(Screen.GenesisToken.route) {
            GenesisTokenScreen(
                onLinkGenesis = { 
                    navController.navigate(Screen.NFTCheck.route) {
                        popUpTo(Screen.GenesisToken.route) { inclusive = true }
                    }
                },
                onContinueWithout = { 
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.GenesisToken.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 8. NFT CHECK SCREEN
        composable(Screen.NFTCheck.route) {
            NFTCheckScreen(
                onNavigateToSuccess = { 
                    navController.navigate(Screen.NFTSuccess.route) {
                        popUpTo(Screen.NFTCheck.route) { inclusive = true }
                    }
                },
                onNavigateToFailure = { 
                    navController.navigate(Screen.NFTFailure.route) {
                        popUpTo(Screen.NFTCheck.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 9. NFT SUCCESS SCREEN
        composable(Screen.NFTSuccess.route) {
            NFTSuccessScreen(
                onContinue = { 
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.NFTSuccess.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 10. NFT FAILURE SCREEN
        composable(Screen.NFTFailure.route) {
            NFTFailureScreen(
                onRetry = { 
                    navController.navigate(Screen.NFTCheck.route) {
                        popUpTo(Screen.NFTFailure.route) { inclusive = true }
                    }
                },
                onContinueWithoutNFT = { 
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.NFTFailure.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 11. ONBOARDING SCREEN
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 12. HOME SCREEN (Main App Hub)
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToNFTCheck = { navController.navigate(Screen.NFTCheck.route) },
                onNavigateToSwap = { navController.navigate(Screen.Swap.route) },
                onNavigateToNFTs = { navController.navigate(Screen.NFTs.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToTransactionHistory = { navController.navigate(Screen.TransactionHistory.route) }
            )
        }
        
        // MAIN APP SCREENS (Accessible from Home)
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController
            )
        }
        
        composable(Screen.Contacts.route) {
            ContactsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSwap = { navController.navigate(Screen.Swap.route) },
                onNavigateToNFTs = { navController.navigate(Screen.NFTs.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(
            route = Screen.ContactDetails.route,
            arguments = listOf(
                navArgument("contactId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            ContactDetailsScreen(
                contactId = contactId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Payment.route) {
            PaymentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToNFTs = { navController.navigate(Screen.NFTs.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Swap.route) {
            SwapScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToNFTs = { navController.navigate(Screen.NFTs.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(
            route = Screen.SendPayment.route,
            arguments = listOf(
                navArgument("recipientAddress") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("amount") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("token") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val recipientAddress = backStackEntry.arguments?.getString("recipientAddress")
            val amount = backStackEntry.arguments?.getString("amount")
            val token = backStackEntry.arguments?.getString("token")
            
            SendPaymentScreen(
                onNavigateBack = { navController.popBackStack() },
                recipientAddress = recipientAddress
            )
        }
        
        composable(Screen.SchedulePayment.route) {
            SchedulePaymentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transaction ->
                    // Handle transaction detail modal or navigation if needed
                }
            )
        }
        
        composable(Screen.NFTs.route) {
            NFTsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSwap = { navController.navigate(Screen.Swap.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToSwap = { navController.navigate(Screen.Swap.route) },
                onNavigateToNFTs = { navController.navigate(Screen.NFTs.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToWalletSuccess = { navController.navigate(Screen.WalletSuccess.route) },
                onNavigateToWalletFailure = { navController.navigate(Screen.WalletFailure.route) },
                onNavigateToNFTSuccess = { navController.navigate(Screen.NFTSuccess.route) },
                onNavigateToNFTFailure = { navController.navigate(Screen.NFTFailure.route) },
                onNavigateToGenesisToken = { navController.navigate(Screen.GenesisToken.route) },
                onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) }
            )
        }
        
        composable(Screen.BluetoothDiscovery.route) {
            BluetoothDiscoveryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        

    }
} 