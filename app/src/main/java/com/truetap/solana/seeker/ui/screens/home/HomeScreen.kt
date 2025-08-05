package com.truetap.solana.seeker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.ui.components.*
import com.truetap.solana.seeker.ui.components.layouts.*
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.truetap.solana.seeker.data.models.TransactionType
import com.truetap.solana.seeker.data.models.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeScreen migrated to use TrueTap Design System
 * 
 * This replaces the original HomeScreen with a standardized, consistent implementation
 * using the design system components and layouts.
 */

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onNavigateToNFTCheck: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTransactionHistory: () -> Unit = {},
    onNavigateToNfcPayment: () -> Unit = {},
    onNavigateToBluetoothPayment: () -> Unit = {},
    onNavigateToScheduledPayments: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val walletState by viewModel.walletState.collectAsStateWithLifecycle()
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    // Quick Actions
    val quickActions = remember {
        listOf(
            QuickAction("Send", Icons.AutoMirrored.Filled.Send, onNavigateToNfcPayment),
            QuickAction("Receive", Icons.Default.QrCode) { /* TODO: Navigate to receive */ },
            QuickAction("Swap", Icons.Default.SwapHoriz, onNavigateToSwap),
            QuickAction("NFTs", Icons.Default.Image, onNavigateToNFTs)
        )
    }
    
    // Get greeting based on time of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
    
    // Get wallet subtitle
    val walletSubtitle = if (walletState.account != null) {
        "Your crypto portfolio"
    } else {
        "Connect your wallet to get started"
    }
    
    // Convert wallet transactions to display format
    val displayTransactions = remember(walletState.transactions) {
        walletState.transactions.map { walletTransaction ->
            val transactionType = when (walletTransaction.type) {
                com.truetap.solana.seeker.data.TransactionType.TRANSFER -> {
                    if (walletTransaction.amount?.compareTo(java.math.BigDecimal.ZERO) == -1) {
                        TransactionType.SENT
                    } else {
                        TransactionType.RECEIVED
                    }
                }
                com.truetap.solana.seeker.data.TransactionType.SWAP -> TransactionType.SWAPPED
                com.truetap.solana.seeker.data.TransactionType.UNKNOWN -> TransactionType.RECEIVED
                else -> TransactionType.RECEIVED
            }
            
            val otherParty = when (transactionType) {
                TransactionType.SENT -> walletTransaction.toAddress?.take(8) ?: "Unknown"
                TransactionType.RECEIVED -> walletTransaction.fromAddress?.take(8) ?: "Unknown"
                TransactionType.SWAPPED -> "Jupiter"
            }
            
            Transaction(
                id = walletTransaction.signature,
                type = transactionType,
                amount = walletTransaction.amount?.toDouble() ?: 0.0,
                currency = "SOL",
                timestamp = (walletTransaction.blockTime ?: (System.currentTimeMillis() / 1000)) * 1000
            )
        }
    }
    
    // Use standardized screen template
    TrueTapScreenTemplate(
        title = greeting,
        subtitle = walletSubtitle,
        currentTab = BottomNavItem.HOME,
        onNavigateToHome = { /* Already on home */ },
        onNavigateToSwap = onNavigateToSwap,
        onNavigateToNFTs = onNavigateToNFTs,
        onNavigateToContacts = onNavigateToContacts,
        onNavigateToSettings = onNavigateToSettings,
        actions = {
            // Genesis NFT check button (if needed)
            if (walletState.account != null && !walletState.hasGenesisNFT) {
                TrueTapButton(
                    text = "Check Genesis",
                    onClick = onNavigateToNFTCheck,
                    style = TrueTapButtonStyle.OUTLINE
                )
            }
            
            // Settings button
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
                onConnectWallet = { viewModel.connectWallet() },
                onRefreshWallet = { viewModel.refreshWalletData() }
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
        
        // Genesis NFT Status (if holder)
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
        if (walletState.account != null) {
            item {
                TrueTapSectionHeader(
                    title = "Recent Activity",
                    subtitle = "Latest transactions",
                    action = {
                        TrueTapButton(
                            text = "View All",
                            onClick = onNavigateToTransactionHistory,
                            style = TrueTapButtonStyle.TEXT
                        )
                    }
                )
            }
            
            // Transaction List
            if (displayTransactions.isNotEmpty()) {
                items(displayTransactions.take(5)) { transaction ->
                    TransactionListItem(transaction = transaction)
                }
            } else {
                item {
                    TrueTapEmptyState(
                        title = "No Recent Activity",
                        description = "Your transactions will appear here once you start using your wallet",
                        icon = Icons.Default.History
                    )
                }
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
        
        // Scheduled Payments (if any)
        item {
            TrueTapSectionHeader(
                title = "Scheduled",
                subtitle = "Upcoming payments",
                action = {
                    TrueTapButton(
                        text = "Manage",
                        onClick = onNavigateToScheduledPayments,
                        style = TrueTapButtonStyle.TEXT
                    )
                }
            )
        }
        
        item {
            TrueTapEmptyState(
                title = "No Scheduled Payments",
                description = "Set up recurring payments to automate your transactions",
                icon = Icons.Default.Schedule,
                action = {
                    TrueTapButton(
                        text = "Schedule Payment",
                        onClick = onNavigateToScheduledPayments
                    )
                }
            )
        }
    }
}

@Composable
private fun WalletBalanceCard(
    walletState: com.truetap.solana.seeker.data.WalletState,
    onConnectWallet: () -> Unit,
    onRefreshWallet: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    TrueTapCard(style = TrueTapCardStyle.ELEVATED) {
        if (walletState.account != null) {
            // Connected wallet view
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Balance",
                        style = getDynamicTypography(accessibility.largeButtonMode).labelLarge,
                        color = dynamicColors.textSecondary
                    )
                    
                    TrueTapButton(
                        text = "Refresh",
                        onClick = onRefreshWallet,
                        style = TrueTapButtonStyle.ICON,
                        icon = Icons.Default.Refresh
                    )
                }
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
                
                Text(
                    text = formatBalance(walletState.balance?.solBalance?.toDouble() ?: 0.0),
                    style = getDynamicTypography(accessibility.largeButtonMode).displaySmall,
                    color = dynamicColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.xs))
                
                Text(
                    text = "≈ ${formatCurrency(calculateUSDValue(walletState.balance?.solBalance?.toDouble() ?: 0.0))}",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyLarge,
                    color = dynamicColors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.md))
                
                // Wallet address (shortened)
                Text(
                    text = "${walletState.account!!.publicKey.take(8)}...${walletState.account!!.publicKey.takeLast(8)}",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                    color = dynamicColors.textInactive,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                
                // Connection status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = TrueTapSpacing.sm)
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (walletState.isConnected) dynamicColors.success else dynamicColors.error
                    ) {}
                    
                    Spacer(modifier = Modifier.width(TrueTapSpacing.xs))
                    
                    Text(
                        text = if (walletState.isConnected) "Connected" else "Disconnected",
                        style = getDynamicTypography(accessibility.largeButtonMode).bodySmall,
                        color = if (walletState.isConnected) dynamicColors.success else dynamicColors.error
                    )
                }
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
                    textAlign = TextAlign.Center
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
    val dynamicColors = LocalDynamicColors.current
    
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(TrueTapSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(if (accessibility.largeButtonMode) 56.dp else 48.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = dynamicColors.primary.copy(alpha = 0.1f)
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
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GenesisNFTCard(
    tier: String?,
    onViewDetails: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    TrueTapCard(
        style = TrueTapCardStyle.FILLED,
        onClick = onViewDetails
    ) {
        TrueTapListItem(
            title = "Genesis NFT Holder",
            subtitle = tier?.let { "Tier: $it" } ?: "Exclusive benefits unlocked",
            leadingIcon = Icons.Default.Diamond,
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = dynamicColors.textSecondary
                )
            }
        )
    }
}

@Composable
private fun TransactionListItem(transaction: Transaction) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    val (icon, description, amountColor) = when (transaction.type) {
        TransactionType.SENT -> Triple(
            Icons.AutoMirrored.Filled.Send,
            "Sent",
            dynamicColors.error
        )
        TransactionType.RECEIVED -> Triple(
            Icons.Default.CallReceived,
            "Received",
            dynamicColors.success
        )
        TransactionType.SWAPPED -> Triple(
            Icons.Default.SwapHoriz,
            "Swapped",
            dynamicColors.textPrimary
        )
    }
    
    TrueTapListItem(
        title = description,
        subtitle = formatTimeAgo(transaction.timestamp),
        leadingIcon = icon,
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == TransactionType.SENT) "-" else "+"}${transaction.amount} ${transaction.currency}",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                    color = amountColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
private fun PortfolioSummaryCard(walletState: com.truetap.solana.seeker.data.WalletState) {
    TrueTapCard {
        Column {
            // SOL Balance
            TrueTapListItem(
                title = "SOL",
                subtitle = "Solana",
                leadingIcon = Icons.Default.Circle,
                trailingContent = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatBalance(walletState.balance?.solBalance?.toDouble() ?: 0.0),
                            style = getDynamicTypography().bodyMedium,
                            color = LocalDynamicColors.current.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatCurrency(calculateUSDValue(walletState.balance?.solBalance?.toDouble() ?: 0.0)),
                            style = getDynamicTypography().bodySmall,
                            color = LocalDynamicColors.current.textSecondary
                        )
                    }
                }
            )
            
            // Add other token balances if available
            walletState.balance?.tokenBalances?.forEach { (symbol, balance) ->
                TrueTapListItem(
                    title = symbol,
                    subtitle = getTokenName(symbol),
                    leadingIcon = Icons.Default.Circle,
                    trailingContent = {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatBalance(balance.toDouble()),
                                style = getDynamicTypography().bodyMedium,
                                color = LocalDynamicColors.current.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                )
            }
        }
    }
}

// Helper functions
private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatBalance(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 6
    return "${formatter.format(amount)} SOL"
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

private fun calculateUSDValue(solAmount: Double): Double {
    // This should come from a price service, using placeholder value
    return solAmount * 103.37
}

private fun getTokenName(symbol: String): String {
    return when (symbol) {
        "USDC" -> "USD Coin"
        "USDT" -> "Tether"
        "RAY" -> "Raydium"
        "SRM" -> "Serum"
        else -> symbol
    }
}