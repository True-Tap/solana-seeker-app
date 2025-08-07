package com.truetap.solana.seeker.ui.navigation

sealed class Screen(val route: String) {
    // Core Flow
    object Splash : Screen("splash")
    object Landing : Screen("landing")
    object Welcome : Screen("welcome")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    
    // Genesis NFT Flow
    object GenesisToken : Screen("genesis_token")
    object NFTCheck : Screen("nft_check")
    object NFTSuccess : Screen("nft_success")
    object NFTFailure : Screen("nft_failure")
    
    // Wallet Connection Flow
    object WalletConnection : Screen("wallet_connection")
    object WalletPairing : Screen("wallet_pairing/{walletId}") {
        fun createRoute(walletId: String) = "wallet_pairing/$walletId"
    }
    object WalletSuccess : Screen("wallet_success")
    object WalletDetails : Screen("wallet_details")
    object WalletFailure : Screen("wallet_failure/{walletId}?errorMessage={errorMessage}") {
        fun createRoute(walletId: String, errorMessage: String? = null) = 
            "wallet_failure/$walletId" + if (errorMessage != null) "?errorMessage=${java.net.URLEncoder.encode(errorMessage, "UTF-8")}" else ""
    }
    
    // Main App Screens
    object Dashboard : Screen("dashboard")
    object Contacts : Screen("contacts")
    object AddContact : Screen("add_contact")
    object QRContact : Screen("qr_contact")
    object NFCContact : Screen("nfc_contact")
    object BluetoothContact : Screen("bluetooth_contact")
    object SendLinkContact : Screen("send_link_contact")
    object ContactDetails : Screen("contact_details/{contactId}") {
        fun createRoute(contactId: String) = "contact_details/$contactId"
    }
    object Payment : Screen("payment")
    object Swap : Screen("swap")
    object SendPayment : Screen("send_payment?recipientAddress={recipientAddress}&amount={amount}&token={token}") {
        fun createRoute(recipientAddress: String? = null, amount: String? = null, token: String? = null): String {
            return "send_payment" + 
                (if (recipientAddress != null || amount != null || token != null) "?" else "") +
                listOfNotNull(
                    recipientAddress?.let { "recipientAddress=$it" },
                    amount?.let { "amount=$it" },
                    token?.let { "token=$it" }
                ).joinToString("&")
        }
    }
    object SchedulePayment : Screen("schedule_payment")
    object ScheduledPayments : Screen("scheduled_payments")
    object TransactionHistory : Screen("transaction_history")
    object NFTs : Screen("nfts")
    object Settings : Screen("settings")
    
    // Additional Features
    object BluetoothDiscovery : Screen("bluetooth_discovery")
    object BluetoothPayment : Screen("bluetooth_payment")
    object SeedVault : Screen("seed_vault")
    object NfcPayment : Screen("nfc_payment")
} 