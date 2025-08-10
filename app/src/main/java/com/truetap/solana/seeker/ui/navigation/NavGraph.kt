package com.truetap.solana.seeker.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import androidx.compose.foundation.layout.fillMaxSize
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
import com.truetap.solana.seeker.ui.screens.wallet.WalletDetailsScreen
import com.truetap.solana.seeker.ui.screens.wallet.WalletFailureScreen
import com.truetap.solana.seeker.ui.screens.welcome.WelcomeScreen
import com.truetap.solana.seeker.ui.screens.genesis.GenesisTokenScreen
import com.truetap.solana.seeker.ui.screens.onboarding.OnboardingScreen
import com.truetap.solana.seeker.ui.screens.dashboard.DashboardScreen
import com.truetap.solana.seeker.ui.screens.contacts.ContactsScreen
import com.truetap.solana.seeker.ui.screens.contacts.ContactDetailsScreen
import com.truetap.solana.seeker.ui.screens.contacts.AddContactScreen
import com.truetap.solana.seeker.ui.screens.contacts.QRContactScreen
import com.truetap.solana.seeker.ui.screens.contacts.NFCContactScreen
import com.truetap.solana.seeker.ui.screens.contacts.BluetoothContactScreen
import com.truetap.solana.seeker.ui.screens.contacts.SendLinkContactScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.ui.screens.payment.PaymentScreen
import com.truetap.solana.seeker.ui.components.MainNavItem
import com.truetap.solana.seeker.ui.components.MainScreenScaffold
import com.truetap.solana.seeker.ui.navigation.rememberMainNavigationHandler
import com.truetap.solana.seeker.ui.screens.EnhancedSwapScreenContent
import com.truetap.solana.seeker.ui.screens.payment.SendPaymentScreen
import com.truetap.solana.seeker.ui.screens.schedule.CreateScheduledPaymentScreen
import com.truetap.solana.seeker.ui.screens.schedule.ScheduleScreen
import com.truetap.solana.seeker.ui.screens.nft.NFTsScreen
import com.truetap.solana.seeker.ui.screens.settings.SettingsScreen
import com.truetap.solana.seeker.ui.screens.bluetooth.BluetoothDiscoveryScreen
import com.truetap.solana.seeker.presentation.screens.bluetooth.BluetoothPaymentScreen
import com.truetap.solana.seeker.ui.screens.transaction.TransactionHistoryScreen
import com.truetap.solana.seeker.ui.screens.SeedVaultScreen
import com.truetap.solana.seeker.ui.screens.nfc.NfcPaymentScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    activityResultSender: ActivityResultSender,
    pendingWalletConnection: android.net.Uri? = null,
    onWalletConnectionHandled: () -> Unit = {},
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
            
            // Debug logging
            android.util.Log.d("NavGraph", "WalletPairing route with walletId: '$walletId'")
            android.util.Log.d("NavGraph", "walletId comparison: '$walletId' == 'solana' = ${walletId == "solana"}")
            
            // If walletId is "solana" (Use Hardware Wallet), navigate to SeedVault
            if (walletId == "solana") {
                android.util.Log.d("NavGraph", "✓ CONFIRMED: Routing to SeedVaultScreen for hardware wallet")
                SeedVaultScreen(
                    activityResultLauncher = activityResultLauncher,
                    onNavigateToSuccess = { 
                        navController.navigate(Screen.WalletSuccess.route) {
                            popUpTo(Screen.WalletPairing.route) { inclusive = true }
                        }
                    },
                    onNavigateToFailure = { errorMessage, _ ->
                        navController.navigate(Screen.WalletFailure.createRoute("solana", errorMessage)) {
                            popUpTo(Screen.WalletPairing.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                android.util.Log.d("NavGraph", "Routing to PairingScreen for walletId: '$walletId'")
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
                    onNavigateBack = { navController.popBackStack() },
                    activityResultLauncher = activityResultLauncher,
                    activityResultSender = activityResultSender,
                    pendingWalletConnection = pendingWalletConnection,
                    onWalletConnectionHandled = onWalletConnectionHandled
                )
            }
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
                    navController.navigate(Screen.WalletDetails.route)
                }
            )
        }
        
        // 5B. WALLET DETAILS SCREEN
        composable(Screen.WalletDetails.route) {
            WalletDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
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
            val errorMessage = backStackEntry.arguments?.getString("errorMessage")?.let { 
                java.net.URLDecoder.decode(it, "UTF-8") 
            }
            WalletFailureScreen(
                walletId = walletId,
                errorMessage = errorMessage,
                onTryAgain = { 
                    navController.navigate(Screen.WalletPairing.createRoute(walletId)) {
                        popUpTo(Screen.WalletFailure.route) { inclusive = true }
                    }
                },
                onTryDifferentWallet = { 
                    // Route directly to Seed Vault flow as a safe fallback
                    navController.navigate(Screen.WalletPairing.createRoute("solana")) {
                        popUpTo(Screen.WalletFailure.route) { inclusive = true }
                    }
                },
                onGetHelp = { 
                    // Navigate to Settings screen which should contain help and support information
                    navController.navigate(Screen.Settings.route)
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
                onNavigateToTransactionHistory = { navController.navigate(Screen.TransactionHistory.route) },
                onNavigateToSchedule = { navController.navigate(Screen.ScheduledPayments.route) }
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
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAddContact = { navController.navigate(Screen.AddContact.route) }
            )
        }
        
        composable(Screen.AddContact.route) {
            AddContactScreen(
                onNavigateBack = { navController.popBackStack() },
                onContactAdded = { navController.popBackStack() },
                onNavigateToQRContact = { navController.navigate(Screen.QRContact.route) },
                onNavigateToNFCContact = { navController.navigate(Screen.NFCContact.route) },
                onNavigateToBluetoothContact = { navController.navigate(Screen.BluetoothContact.route) },
                onNavigateToSendLinkContact = { navController.navigate(Screen.SendLinkContact.route) }
            )
        }
        
        composable(Screen.QRContact.route) {
            QRContactScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.NFCContact.route) {
            NFCContactScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.BluetoothContact.route) {
            BluetoothContactScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.SendLinkContact.route) {
            SendLinkContactScreen(
                onNavigateBack = { navController.popBackStack() }
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

        // Request Payment flow
        composable(Screen.RequestPay.route) {
            com.truetap.solana.seeker.ui.screens.payment.RequestPayScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Swap.route) {
            val onNavigate = rememberMainNavigationHandler(navController)
            MainScreenScaffold(
                currentScreen = MainNavItem.SWAP,
                onNavigate = onNavigate
            ) { paddingValues ->
                EnhancedSwapScreenContent(
                    paddingValues = paddingValues
                )
            }
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
                recipientAddress = recipientAddress,
                activityResultSender = activityResultSender
            )
        }
        
        composable(Screen.SchedulePayment.route) {
            CreateScheduledPaymentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ScheduledPayments.route) {
            ScheduleScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreatePayment = { navController.navigate(Screen.SchedulePayment.route) }
            )
        }
        
        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { 
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

        // Phase 3: Split Pay & Request Payment
        composable(Screen.SplitPay.route) {
            com.truetap.solana.seeker.ui.screens.payment.SplitPayScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.RequestPay.route) {
            com.truetap.solana.seeker.ui.screens.payment.SplitPayScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.BluetoothDiscovery.route) {
            BluetoothDiscoveryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.BluetoothPayment.route) {
            BluetoothPaymentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.SeedVault.route) {
            SeedVaultScreen(
                activityResultLauncher = activityResultLauncher
            )
        }
        
        composable(Screen.NfcPayment.route) {
            NfcPaymentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Merchant checkout via Solana Pay
        composable(Screen.SolanaPay.route) {
            com.truetap.solana.seeker.ui.screens.payment.SolanaPayScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenScanner = { /* TODO: integrate scanner */ },
                onPasteLink = { link ->
                    // TODO: parse Solana Pay link and route into SendPayment flow
                    navController.navigate(Screen.SendPayment.route)
                }
            )
        }

    }
} 