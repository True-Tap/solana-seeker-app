package com.truetap.solana.seeker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.ui.components.*
import com.truetap.solana.seeker.ui.components.layouts.*
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import java.text.NumberFormat
import java.util.*

/**
 * Refactored HomeScreen using TrueTap Design System
 * 
 * This demonstrates how to use the standardized components and layouts
 * for consistent UI/UX across the application.
 */

// Quick Action data class
data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreenRefactored(
    onNavigateToNFTCheck: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSend: () -> Unit = {},
    onNavigateToReceive: () -> Unit = {},
    walletViewModel: WalletViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val walletState by walletViewModel.walletState.collectAsStateWithLifecycle()
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    // Quick Actions
    val quickActions = remember {
        listOf(
            QuickAction("Send", Icons.AutoMirrored.Filled.Send, onNavigateToSend),
            QuickAction("Receive", Icons.Default.QrCode, onNavigateToReceive),
            QuickAction("Swap", Icons.Default.SwapHoriz, onNavigateToSwap),
            QuickAction("NFTs", Icons.Default.Image, onNavigateToNFTs)
        )
    }
    
    // Use standardized screen template
    TrueTapScreenTemplate(
        title = "Good ${getGreeting()}",
        subtitle = getWalletSubtitle(walletState),
        currentTab = BottomNavItem.HOME,
        onNavigateToHome = { /* Already on home */ },
        onNavigateToSwap = onNavigateToSwap,
        onNavigateToNFTs = onNavigateToNFTs,
        onNavigateToContacts = onNavigateToContacts,
        onNavigateToSettings = onNavigateToSettings,
        actions = {
            // Genesis NFT check button
            if (!walletState.hasGenesisNFT) {
                TrueTapButton(
                    text = "Check Genesis",
                    onClick = onNavigateToNFTCheck,
                    style = TrueTapButtonStyle.OUTLINE
                )
            }
            
            // Settings icon
            TrueTapButton(
                text = "Settings",
                onClick = onNavigateToSettings,
                style = TrueTapButtonStyle.ICON,
                icon = Icons.Default.Settings
            )
        },
        modifier = modifier
    ) {
        // Wallet Balance Card
        item {
            WalletBalanceCard(
                walletState = walletState,
                onConnectWallet = { walletViewModel.connectWallet() }
            )
        }
        
        // Quick Actions Section
        item {
            TrueTapSectionHeader(
                title = "Quick Actions",
                subtitle = "Common wallet operations"
            )
        }
        
        item {
            QuickActionsGrid(actions = quickActions)
        }
        
        // Genesis NFT Status (if applicable)
        if (walletState.hasGenesisNFT) {
            item {
                TrueTapSectionHeader(
                    title = "Genesis NFT",
                    subtitle = "Your exclusive benefits"
                )
            }
            
            item {
                GenesisNFTCard(
                    tier = walletState.genesisNFTTier,
                    onViewDetails = onNavigateToNFTs
                )
            }
        }
        
        // Recent Transactions Section
        item {
            TrueTapSectionHeader(
                title = "Recent Activity",
                subtitle = "Latest transactions",
                action = {
                    TrueTapButton(
                        text = "View All",
                        onClick = { /* Navigate to full transaction history */ },
                        style = TrueTapButtonStyle.TEXT
                    )
                }
            )
        }
        
        // Transaction List
        if (walletState.recentTransactions.isNotEmpty()) {
            items(walletState.recentTransactions.take(5)) { transaction ->
                TransactionListItem(transaction = transaction)
            }
        } else {
            item {
                TrueTapEmptyState(
                    title = "No Recent Activity",
                    description = "Your transactions will appear here",
                    icon = Icons.Default.History
                )
            }
        }
        
        // Portfolio Summary (if wallet connected)
        if (walletState.account != null) {
            item {
                TrueTapSectionHeader(
                    title = "Portfolio",
                    subtitle = "Token balances"
                )
            }
            
            item {
                PortfolioSummaryCard(walletState = walletState)
            }
        }
        
        // Network Status Card
        item {
            NetworkStatusCard(
                isConnected = walletState.isConnected,
                networkName = "Solana Mainnet"
            )
        }
    }
}

@Composable
private fun WalletBalanceCard(
    walletState: WalletState,
    onConnectWallet: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    TrueTapCard(style = TrueTapCardStyle.ELEVATED) {
        if (walletState.account != null) {
            // Connected wallet view
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total Balance",
                    style = getDynamicTypography(accessibility.largeButtonMode).labelLarge,
                    color = dynamicColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
                
                Text(
                    text = formatCurrency(walletState.totalBalance),
                    style = getDynamicTypography(accessibility.largeButtonMode).displaySmall,
                    color = dynamicColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.xs))
                
                Text(
                    text = "≈ ${formatCurrency(walletState.totalBalanceUSD, "USD")}",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyLarge,
                    color = dynamicColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.md))
                
                // Wallet address (shortened)
                Text(
                    text = "${walletState.account!!.publicKey.take(8)}...${walletState.account!!.publicKey.takeLast(8)}",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                    color = dynamicColors.textTertiary,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        } else {
            // Disconnected wallet view
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "No Wallet",
                    tint = dynamicColors.textSecondary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.md))
                
                Text(
                    text = "Connect Your Wallet",
                    style = getDynamicTypography(accessibility.largeButtonMode).headlineSmall,
                    color = dynamicColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
                
                Text(
                    text = "Connect a Solana wallet to view your balance and start using TrueTap",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                    color = dynamicColors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
                
                TrueTapButton(
                    text = "Connect Wallet",
                    onClick = onConnectWallet,
                    icon = Icons.Default.AccountBalanceWallet
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(actions: List<QuickAction>) {
    TrueTapCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            actions.forEach { action ->
                QuickActionButton(
                    title = action.title,
                    icon = action.icon,
                    onClick = action.onClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(TrueTapSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(if (accessibility.largeButtonMode) 56.dp else 48.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = dynamicColors.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = dynamicColors.primary,
                    modifier = Modifier.size(if (accessibility.largeButtonMode) 28.dp else 24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
        
        Text(
            text = title,
            style = getDynamicTypography(accessibility.largeButtonMode).labelMedium,
            color = dynamicColors.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun GenesisNFTCard(
    tier: String?,
    onViewDetails: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    TrueTapCard(
        style = TrueTapCardStyle.FILLED,
        onClick = onViewDetails
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = "Genesis NFT",
                tint = dynamicColors.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(TrueTapSpacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Genesis NFT Holder",
                    style = getDynamicTypography(accessibility.largeButtonMode).titleMedium,
                    color = dynamicColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = tier?.let { "Tier: $it" } ?: "Exclusive benefits unlocked",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                    color = dynamicColors.textSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = dynamicColors.textSecondary
            )
        }
    }
}

@Composable
private fun TransactionListItem(transaction: Any) {
    // Simplified transaction item using TrueTapListItem
    TrueTapListItem(
        title = "Transaction", // This would come from transaction data
        subtitle = "Recent activity", // This would come from transaction data
        leadingIcon = Icons.AutoMirrored.Filled.Send, // This would be based on transaction type
        trailingContent = {
            Text(
                text = "0.5 SOL", // This would come from transaction data
                style = getDynamicTypography().bodyMedium,
                color = getDynamicColors().textPrimary
            )
        }
    )
}

@Composable
private fun PortfolioSummaryCard(walletState: WalletState) {
    TrueTapCard {
        Column {
            TrueTapListItem(
                title = "SOL",
                subtitle = "Solana",
                leadingIcon = Icons.Default.Circle,
                trailingContent = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatCurrency(walletState.solBalance),
                            style = getDynamicTypography().bodyMedium,
                            color = getDynamicColors().textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatCurrency(walletState.solBalance * 100, "USD"), // Example price
                            style = getDynamicTypography().bodySmall,
                            color = getDynamicColors().textSecondary
                        )
                    }
                }
            )
            
            // Add other tokens...
        }
    }
}

@Composable
private fun NetworkStatusCard(
    isConnected: Boolean,
    networkName: String
) {
    val dynamicColors = getDynamicColors()
    val accessibility = LocalAccessibilitySettings.current
    
    TrueTapCard(style = TrueTapCardStyle.OUTLINED) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Surface(
                modifier = Modifier.size(12.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isConnected) dynamicColors.success else dynamicColors.error
            ) {}
            
            Spacer(modifier = Modifier.width(TrueTapSpacing.sm))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = networkName,
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                    color = dynamicColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = if (isConnected) "Connected" else "Disconnected",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodySmall,
                    color = if (isConnected) dynamicColors.success else dynamicColors.error
                )
            }
        }
    }
}

// Helper functions
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Morning"
        in 12..17 -> "Afternoon"
        else -> "Evening"
    }
}

private fun getWalletSubtitle(walletState: WalletState): String {
    return if (walletState.account != null) {
        "Your crypto portfolio"
    } else {
        "Connect your wallet to get started"
    }
}

private fun formatCurrency(amount: Double, currency: String = "SOL"): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = if (currency == "USD") 2 else 6
    return "${formatter.format(amount)} $currency"
}