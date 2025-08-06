package com.truetap.solana.seeker.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.accessibility.AccessibleButton
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.ui.theme.LocalDynamicColors
import com.truetap.solana.seeker.ui.components.BottomNavItem
import com.truetap.solana.seeker.ui.components.TrueTapBottomNavigationBar
import com.truetap.solana.seeker.ui.components.rememberBottomNavHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.truetap.TrueTapBottomSheet
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.truetap.solana.seeker.data.WalletState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

// Data classes for the home screen
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: String,
    val otherParty: String,
    val timeAgo: String,
    val token: String = "SOL"
)

data class Wallet(
    val id: String,
    val name: String,
    val balance: String,
    val address: String,
    val isConnected: Boolean = true
)

enum class TransactionType { SENT, RECEIVED, SWAPPED }

// Helper function to format timestamp to "time ago" format
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
    var isBalanceVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(BottomNavItem.HOME) }
    var showWalletRenameDialog by remember { mutableStateOf(false) }
    var walletToRename by remember { mutableStateOf<Wallet?>(null) }
    var showTransactionModal by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showTrueTapSheet by remember { mutableStateOf(false) }
    
    // Get wallet state from ViewModel
    val walletState by viewModel.walletState.collectAsStateWithLifecycle()
    
    // Convert wallet state to display wallets with enhanced information
    val wallets = remember(walletState) {
        val account = walletState.account
        if (walletState.isConnected && account != null) {
            listOf(
                Wallet(
                    id = "1",
                    name = account.accountLabel ?: "Connected Wallet",
                    balance = walletState.balance?.solBalance?.toPlainString() ?: "0.00",
                    address = account.publicKey,
                    isConnected = true
                )
            )
        } else {
            emptyList()
        }
    }
    
    // Create infinite pager with repeated wallets for seamless looping
    val infiniteWallets = remember(wallets) {
        if (wallets.size > 1) {
            // Add wallets at the beginning and end for infinite scrolling
            listOf(wallets.last()) + wallets + listOf(wallets.first())
        } else {
            wallets
        }
    }
    
    val pagerState = rememberPagerState(pageCount = { infiniteWallets.size })
    val actualCurrentIndex = if (wallets.size > 1) {
        (pagerState.currentPage - 1 + wallets.size) % wallets.size
    } else {
        pagerState.currentPage
    }
    val currentWallet = if (wallets.isNotEmpty()) wallets[actualCurrentIndex] else null
    
    // Handle infinite looping by jumping to the middle when reaching edges
    LaunchedEffect(pagerState.currentPage) {
        if (wallets.size > 1) {
            when (pagerState.currentPage) {
                0 -> {
                    // Jump to the last real wallet (second to last in infinite list)
                    pagerState.animateScrollToPage(wallets.size)
                }
                infiniteWallets.size - 1 -> {
                    // Jump to the first real wallet (second in infinite list)
                    pagerState.animateScrollToPage(1)
                }
            }
        }
    }
    
    // Sample data
    val userName = "Emory"
    val currentDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    
    // Convert wallet transactions to display transactions (limit to 3 most recent)
    val recentTransactions = remember(walletState) {
        walletState.transactions
            .sortedByDescending { it.blockTime } // Sort by most recent first
            .take(3) // Limit to 3 most recent
            .map { walletTransaction ->
                val transactionType = when (walletTransaction.type) {
                    com.truetap.solana.seeker.data.TransactionType.TRANSFER -> {
                        // Determine if sent or received based on addresses
                        if (walletTransaction.fromAddress == walletState.account?.publicKey) {
                            TransactionType.SENT
                        } else {
                            TransactionType.RECEIVED
                        }
                    }
                    com.truetap.solana.seeker.data.TransactionType.TOKEN_TRANSFER -> {
                        if (walletTransaction.fromAddress == walletState.account?.publicKey) {
                            TransactionType.SENT
                        } else {
                            TransactionType.RECEIVED
                        }
                    }
                    com.truetap.solana.seeker.data.TransactionType.NFT_TRANSFER -> {
                        if (walletTransaction.fromAddress == walletState.account?.publicKey) {
                            TransactionType.SENT
                        } else {
                            TransactionType.RECEIVED
                        }
                    }
                    com.truetap.solana.seeker.data.TransactionType.SWAP -> TransactionType.SWAPPED
                    com.truetap.solana.seeker.data.TransactionType.UNKNOWN -> TransactionType.RECEIVED
                }
                
                val otherParty = when (transactionType) {
                    TransactionType.SENT -> walletTransaction.toAddress?.take(8) ?: "Unknown"
                    TransactionType.RECEIVED -> walletTransaction.fromAddress?.take(8) ?: "Unknown"
                    TransactionType.SWAPPED -> "Jupiter"
                }
                
                Transaction(
                    id = walletTransaction.signature,
                    type = transactionType,
                    amount = walletTransaction.amount?.toPlainString() ?: "0.00", 
                    otherParty = otherParty,
                    timeAgo = formatTimeAgo(walletTransaction.blockTime * 1000),
                    token = "SOL"
                )
            }
    }
    
    // Function to refresh wallet data
    val refreshWalletData = {
        if (walletState.isConnected) {
            viewModel.refreshWalletData()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        // Main content area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Increased spacing for camera
                
                // Header Section
                HeaderSection(userName = userName, currentDate = currentDate)
            }
            
            item {
                // Wallet Count Indicator (if multiple wallets) - Only dots at bottom
                if (wallets.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Swipeable Wallet Balance Card
                SwipeableWalletCard(
                    wallets = infiniteWallets,
                    currentWallet = currentWallet ?: Wallet(
                        id = "default",
                        name = "No Wallet Connected",
                        balance = "0.0000",
                        address = "",
                        isConnected = false
                    ),
                    pagerState = pagerState,
                    isBalanceVisible = isBalanceVisible,
                    onToggleVisibility = { isBalanceVisible = !isBalanceVisible },
                    onWalletRename = { wallet ->
                        walletToRename = wallet
                        showWalletRenameDialog = true
                    },
                    walletState = walletState,
                    onRefresh = refreshWalletData
                )
                
                // Page indicators under wallet container (if multiple wallets)
                if (wallets.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(wallets.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (index == actualCurrentIndex) TrueTapPrimary else TrueTapTextSecondary.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                            if (index < wallets.size - 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }
            
            item {
                // Action Buttons Section
                ActionButtonsSection(
                    onTrueTap = { showTrueTapSheet = true },
                    onBluetooth = { onNavigateToBluetoothPayment() },
                    onContacts = { onNavigateToContacts() },
                    onSchedule = { onNavigateToScheduledPayments() }
                )
            }
            
            item {
                // Recent Activity Section
                RecentActivitySection(
                    transactions = recentTransactions,
                    isLoading = walletState.isLoading,
                    hasWallet = walletState.isConnected,
                    onViewAll = { onNavigateToTransactionHistory() },
                    onTransactionClick = { transaction ->
                        selectedTransaction = transaction
                        showTransactionModal = true
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Bottom Navigation Bar - Using shared component with system bar adaptation
        TrueTapBottomNavigationBar(
            selectedTab = BottomNavItem.HOME,
            onTabSelected = rememberBottomNavHandler(
                currentScreen = BottomNavItem.HOME,
                onNavigateToSwap = onNavigateToSwap,
                onNavigateToNFTs = onNavigateToNFTs,
                onNavigateToContacts = onNavigateToContacts,
                onNavigateToSettings = onNavigateToSettings
            )
        )
    }
    
    // Wallet Rename Dialog
    if (showWalletRenameDialog && walletToRename != null) {
        WalletRenameDialog(
            wallet = walletToRename!!,
            onDismiss = { showWalletRenameDialog = false },
            onRename = { newName ->
                // Here you would update the wallet name in your data source
                showWalletRenameDialog = false
            }
        )
    }
    
    // Transaction Detail Modal
    if (showTransactionModal && selectedTransaction != null) {
        TransactionDetailModal(
            transaction = selectedTransaction!!,
            onDismiss = { showTransactionModal = false }
        )
    }
    
    // TrueTap Bottom Sheet
    if (showTrueTapSheet) {
        TrueTapBottomSheet(
            viewModel = viewModel,
            onDismiss = { showTrueTapSheet = false }
        )
    }
}


@Composable
private fun HeaderSection(
    userName: String,
    currentDate: String
) {
    // Bouncing animation for Tappy
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    val currentHeight = bounceProgress
    val bounceHeight = 12.dp // Increased bounce height
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bouncing Tappy Logo with proper container height
        Box(
            modifier = Modifier
                .height(48.dp + bounceHeight + 8.dp) // Increased container height to prevent cutoff
                .width(48.dp)
                .offset(y = -(currentHeight * bounceHeight.value).dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "Tappy",
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = 1f - (1f - currentHeight) * 0.02f
                        scaleY = 1f + (1f - currentHeight) * 0.01f
                    },
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Greeting and Date
        Column {
            Text(
                text = "Ready to tap, $userName?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            Text(
                text = currentDate,
                fontSize = 14.sp,
                color = TrueTapTextSecondary
            )
        }
    }
}



@Composable
private fun SwipeableWalletCard(
    wallets: List<Wallet>,
    currentWallet: Wallet,
    pagerState: androidx.compose.foundation.pager.PagerState,
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onWalletRename: (Wallet) -> Unit,
    walletState: WalletState,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        if (wallets.size > 1) {
            // Swipeable content for multiple wallets
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                WalletCardContent(
                    wallet = wallets[page],
                    isBalanceVisible = isBalanceVisible,
                    onToggleVisibility = onToggleVisibility,
                    onWalletRename = onWalletRename,
                    showSwipeHint = true,
                    walletState = walletState,
                    onRefresh = onRefresh
                )
            }
        } else {
            // Single wallet content
            WalletCardContent(
                wallet = currentWallet,
                isBalanceVisible = isBalanceVisible,
                onToggleVisibility = onToggleVisibility,
                onWalletRename = onWalletRename,
                showSwipeHint = false,
                walletState = walletState,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
private fun WalletCardContent(
    wallet: Wallet,
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onWalletRename: (Wallet) -> Unit,
    showSwipeHint: Boolean,
    walletState: WalletState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggleVisibility() }
    ) {
        // Header with wallet name and icons
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top row: Wallet name with edit icon on left, refresh and visibility toggle on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Wallet name and edit icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = wallet.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onWalletRename(wallet) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename Wallet",
                        tint = TrueTapTextSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onWalletRename(wallet) }
                    )
                }
                
                // Right side: Refresh button and visibility toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (walletState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = TrueTapPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Wallet Data",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onRefresh() }
                        )
                    }
                    
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isBalanceVisible) "Hide Balance" else "Show Balance",
                        tint = TrueTapTextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onToggleVisibility() }
                    )
                }
            }
            
            // Connection status and wallet address
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (wallet.isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (wallet.isConnected) "Connected" else "Disconnected",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (wallet.isConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
                
                if (wallet.address.isNotEmpty()) {
                    Text(
                        text = "${wallet.address.take(8)}...${wallet.address.takeLast(8)}",
                        fontSize = 11.sp,
                        color = TrueTapTextSecondary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                Text(
                    text = "Wallet Balance",
                    fontSize = 12.sp,
                    color = TrueTapTextSecondary
                )
                
                // Show NFT count if Genesis NFT detected
                if (walletState.hasGenesisNFT) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Genesis NFT",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Genesis NFT Holder",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
                
                if (walletState.nfts.isNotEmpty()) {
                    Text(
                        text = "${walletState.nfts.size} NFTs",
                        fontSize = 10.sp,
                        color = TrueTapTextSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Balance Display - Centered with proper spacing
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isBalanceVisible) {
                    Text(
                        text = "${wallet.balance} SOL",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "••••••",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletRenameDialog(
    wallet: Wallet,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(wallet.name) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename Wallet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Wallet Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(newName) }
            ) {
                Text("Save", color = TrueTapPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TrueTapTextSecondary)
            }
        },
        containerColor = TrueTapContainer,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun BalanceCard(
    balance: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggleVisibility() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with wallet icon and visibility toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = TrueTapTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wallet Balance",
                        fontSize = 16.sp,
                        color = TrueTapTextSecondary
                    )
                }
                
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isVisible) "Hide Balance" else "Show Balance",
                    tint = TrueTapTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Balance Display
            if (isVisible) {
                Text(
                    text = "$balance SOL",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "••••••",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onTrueTap: () -> Unit,
    onBluetooth: () -> Unit,
    onContacts: () -> Unit,
    onSchedule: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "TrueTap",
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = onTrueTap,
                modifier = Modifier.weight(1f)
            )
            
            ActionButton(
                text = "Bluetooth",
                icon = Icons.Default.Bluetooth,
                onClick = onBluetooth,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Contacts",
                icon = Icons.Default.People,
                onClick = onContacts,
                modifier = Modifier.weight(1f)
            )
            
            ActionButton(
                text = "Schedule",
                icon = Icons.Default.Schedule,
                onClick = onSchedule,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    // Adjust sizes and colors based on accessibility settings
    val buttonHeight = if (accessibility.largeButtonMode) 80.dp else 64.dp
    val iconSize = if (accessibility.largeButtonMode) 32.dp else 24.dp
    val fontSize = if (accessibility.largeButtonMode) 16.sp else 14.sp
    val containerColor = dynamicColors.primary
    val contentColor = dynamicColors.onPrimary
    
    Button(
        onClick = {
            onClick()
            // Audio confirmation if enabled
            if (accessibility.audioConfirmations) {
                // TODO: Play confirmation sound
            }
        },
        modifier = modifier.semantics { 
            contentDescription = "$text button. Tap to open $text feature"
        },
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(if (accessibility.simplifiedUIMode) 8.dp else 12.dp),
        contentPadding = PaddingValues(vertical = if (accessibility.largeButtonMode) 20.dp else 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(if (accessibility.largeButtonMode) 12.dp else 4.dp))
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = if (accessibility.highContrastMode) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RecentActivitySection(
    transactions: List<Transaction>,
    isLoading: Boolean,
    hasWallet: Boolean,
    onViewAll: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    Column {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            if (hasWallet && transactions.isNotEmpty()) {
                Text(
                    text = "View All",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary,
                    modifier = Modifier.clickable { onViewAll() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on state
        when {
            !hasWallet -> {
                // No wallet connected state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "No Wallet",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Connect a wallet to see activity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TrueTapTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your transactions will appear here",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            isLoading -> {
                // Loading state
                repeat(3) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TrueTapTextSecondary.copy(alpha = 0.2f))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(16.dp)
                                        .background(
                                            TrueTapTextSecondary.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .height(12.dp)
                                        .background(
                                            TrueTapTextSecondary.copy(alpha = 0.2f),
                                            RoundedCornerShape(6.dp)
                                        )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(16.dp)
                                    .background(
                                        TrueTapTextSecondary.copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                    }
                    if (it < 2) Spacer(modifier = Modifier.height(12.dp))
                }
            }
            transactions.isEmpty() -> {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No Transactions",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent activity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TrueTapTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Make your first transaction to get started",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                // Transaction List
                transactions.forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (transaction.type) {
                            TransactionType.RECEIVED -> Color(0xFFE8F5E8) // Light green
                            TransactionType.SENT -> Color(0xFFFFEBEE) // Light red
                            TransactionType.SWAPPED -> Color(0xFFE3F2FD) // Light blue
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.type) {
                        TransactionType.RECEIVED -> Icons.AutoMirrored.Filled.TrendingUp
                        TransactionType.SENT -> Icons.AutoMirrored.Filled.TrendingDown
                        TransactionType.SWAPPED -> Icons.Filled.SwapHoriz
                    },
                    contentDescription = transaction.type.name,
                    tint = when (transaction.type) {
                        TransactionType.RECEIVED -> Color(0xFF4CAF50) // Green
                        TransactionType.SENT -> Color(0xFFF44336) // Red
                        TransactionType.SWAPPED -> Color(0xFF2196F3) // Blue
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.RECEIVED -> "From ${transaction.otherParty}"
                        TransactionType.SENT -> "To ${transaction.otherParty}"
                        TransactionType.SWAPPED -> "Swapped with ${transaction.otherParty}"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = transaction.timeAgo,
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
            }
            
            // Transaction Amount
            Text(
                text = when (transaction.type) {
                    TransactionType.RECEIVED -> "+${transaction.amount} ${transaction.token}"
                    TransactionType.SENT -> "-${transaction.amount} ${transaction.token}"
                    TransactionType.SWAPPED -> "${transaction.amount} ${transaction.token}"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (transaction.type) {
                    TransactionType.RECEIVED -> Color(0xFF4CAF50) // Green
                    TransactionType.SENT -> Color(0xFFF44336) // Red
                    TransactionType.SWAPPED -> Color(0xFF2196F3) // Blue
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailModal(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = TrueTapContainer,
        contentColor = TrueTapTextPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(TrueTapTextSecondary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TrueTapTextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Transaction Icon and Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            when (transaction.type) {
                                TransactionType.RECEIVED -> Color(0xFFE8F5E8)
                                TransactionType.SENT -> Color(0xFFFFEBEE)
                                TransactionType.SWAPPED -> Color(0xFFE3F2FD)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.type) {
                            TransactionType.RECEIVED -> Icons.AutoMirrored.Filled.TrendingUp
                            TransactionType.SENT -> Icons.AutoMirrored.Filled.TrendingDown
                            TransactionType.SWAPPED -> Icons.Filled.SwapHoriz
                        },
                        contentDescription = transaction.type.name,
                        tint = when (transaction.type) {
                            TransactionType.RECEIVED -> Color(0xFF4CAF50)
                            TransactionType.SENT -> Color(0xFFF44336)
                            TransactionType.SWAPPED -> Color(0xFF2196F3)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = when (transaction.type) {
                            TransactionType.RECEIVED -> "Received"
                            TransactionType.SENT -> "Sent"
                            TransactionType.SWAPPED -> "Swapped"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    Text(
                        text = transaction.timeAgo,
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Amount
            DetailRow(
                label = "Amount",
                value = when (transaction.type) {
                    TransactionType.RECEIVED -> "+${transaction.amount} ${transaction.token}"
                    TransactionType.SENT -> "-${transaction.amount} ${transaction.token}"
                    TransactionType.SWAPPED -> "${transaction.amount} ${transaction.token}"
                },
                valueColor = when (transaction.type) {
                    TransactionType.RECEIVED -> Color(0xFF4CAF50)
                    TransactionType.SENT -> Color(0xFFF44336)
                    TransactionType.SWAPPED -> Color(0xFF2196F3)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Other Party
            DetailRow(
                label = when (transaction.type) {
                    TransactionType.RECEIVED -> "From"
                    TransactionType.SENT -> "To"
                    TransactionType.SWAPPED -> "Counterparty"
                },
                value = transaction.otherParty
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Transaction ID
            DetailRow(
                label = "Transaction ID",
                value = transaction.id,
                isMonospace = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Network Fee (mock data)
            DetailRow(
                label = "Network Fee",
                value = "0.00025 SOL"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status
            DetailRow(
                label = "Status",
                value = "Confirmed",
                valueColor = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Copy transaction ID */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TrueTapPrimary
                    ),
                    border = BorderStroke(1.dp, TrueTapPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy ID")
                }
                
                Button(
                    onClick = { /* View on explorer */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrueTapPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Explorer",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Explorer")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TrueTapTextPrimary,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
            fontFamily = if (isMonospace) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default
        )
    }
} 