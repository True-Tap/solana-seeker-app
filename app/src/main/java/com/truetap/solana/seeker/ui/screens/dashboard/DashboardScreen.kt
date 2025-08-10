/**
 * Dashboard Screen - TrueTap
 * Main home screen with balance, quick actions, and recent activity
 * Kotlin Compose implementation
 */

package com.truetap.solana.seeker.ui.screens.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.navigation.Screen
import com.truetap.solana.seeker.ui.components.TrueTapButton
import com.truetap.solana.seeker.ui.truetap.TrueTapBottomSheet
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Data Classes
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: String,
    val recipient: String,
    val time: String,
    val otherParty: String? = null,
    val timestamp: Long? = null,
    val tokenSymbol: String? = null,
    val transactionHash: String? = null,
    val fee: String? = null,
    val status: TransactionStatus = TransactionStatus.CONFIRMED
)

enum class TransactionType { SENT, RECEIVED }
enum class TransactionStatus { CONFIRMED, PENDING, FAILED }

data class TokenBalance(
    val symbol: String,
    val amount: Double,
    val uiAmount: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showTrueTapSheet by remember { mutableStateOf(false) }
    
    // Animation states
    val fadeAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "fadeAnimation"
    )
    
    val tappyFloat by animateFloatAsState(
        targetValue = if (uiState.animateTappy) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tappyFloat"
    )
    
    val balanceScale by animateFloatAsState(
        targetValue = if (uiState.isBalancePressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "balanceScale"
    )
    
    // Start animations
    LaunchedEffect(Unit) {
        viewModel.startAnimations()
        delay(600)
        viewModel.loadWalletData()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        // Pull to refresh would go here if needed
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.screenHorizontal),
                contentPadding = PaddingValues(
                    top = Spacing.large,
                    bottom = Spacing.large + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
            ) {
                // Header with Tappy and Greeting
                item {
                    DashboardHeader(
                        greeting = uiState.greeting,
                        subtitle = uiState.subtitle,
                        tappyOffset = tappyFloat * -20f,
                        fadeAnimation = fadeAnimation
                    )
                    Spacer(modifier = Modifier.height(Spacing.large))
                }
                
                // Balance Card
                item {
                    BalanceCard(
                        isVisible = uiState.isBalanceVisible,
                        balance = uiState.walletBalance,
                        isConnected = uiState.isWalletConnected,
                        tokenBalances = uiState.tokenBalances,
                        scale = balanceScale,
                        onToggleVisibility = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleBalanceVisibility()
                        }
                    )
                    Spacer(modifier = Modifier.height(Spacing.large))
                }
                
                // Quick Actions
                item {
                    QuickActions(
                        onTrueTapClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showTrueTapSheet = true
                        },
                        onContactsClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("contacts")
                        },
                        onScheduleClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("schedule_payment")
                        }
                    )
                    Spacer(modifier = Modifier.height(Spacing.large))
                }
                
                // Pending (Queued) Section (live)
                item {
                    val pending = uiState.pendingOutbox
                    if (pending.isNotEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = TrueTapContainer)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Queued—will send when online", fontWeight = FontWeight.Bold, color = TrueTapTextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                pending.take(3).forEach { pt ->
                                    val retryText = if (pt.retries > 0) "  (retries: ${pt.retries})" else ""
                                    Text("${pt.amount} SOL → ${pt.toAddress.take(6)}...${pt.toAddress.takeLast(4)}$retryText", color = TrueTapTextSecondary)
                                }
                                if (pending.size > 3) {
                                    Text("+${pending.size - 3} more queued", color = TrueTapTextInactive, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Fees may change while queued and on retry.",
                                    color = TrueTapTextInactive,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.medium))
                    }
                }

                // Recent Activity Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary
                        )
                        TextButton(
                            onClick = { /* Navigate to all transactions */ }
                        ) {
                            Text(
                                text = "View All",
                                fontSize = 14.sp,
                                color = TrueTapTextSecondary
                            )
                        }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { navController.navigate(com.truetap.solana.seeker.ui.navigation.Screen.SplitPay.route) }) {
                            Text(text = "Split Pay", fontSize = 14.sp, color = TrueTapTextSecondary)
                        }
                        TextButton(onClick = { navController.navigate(com.truetap.solana.seeker.ui.navigation.Screen.RequestPay.route) }) {
                            Text(text = "Request", fontSize = 14.sp, color = TrueTapTextSecondary)
                        }
                    }
                    }
                    Spacer(modifier = Modifier.height(Spacing.medium))
                }
                
                // Transaction List
                items(uiState.transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.selectTransaction(transaction)
                        }
                    )
                    // Social / Rewards row (likes, comments count, reward suggestion)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val likeCount = uiState.likesByTx[transaction.transactionHash ?: transaction.id] ?: 0
                        val comments = uiState.commentsByTx[transaction.transactionHash ?: transaction.id] ?: emptyList()
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = { viewModel.likeTransaction(transaction.transactionHash ?: transaction.id) }) {
                                Text("❤ $likeCount", color = TrueTapTextSecondary, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.addComment(transaction.transactionHash ?: transaction.id, text = "Nice!") }) {
                                Text("💬 ${comments.size}", color = TrueTapTextSecondary, fontSize = 12.sp)
                            }
                        }
                        viewModel.computeRewardSuggestion(transaction.amount)?.let { hint ->
                            Text(hint, color = TrueTapSuccess, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.medium))
                }
            }
        }
        
        // Transaction Detail Modal
        uiState.selectedTransaction?.let { transaction ->
            TransactionDetailModal(
                transaction = transaction,
                onDismiss = { viewModel.clearSelectedTransaction() },
                onCopyHash = { hash ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.copyToClipboard(hash)
                },
                onOpenExplorer = { hash ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.openExplorer(hash)
                },
                onAddNote = { txId, message, isPrivate -> viewModel.addNote(txId, message, isPrivate) }
            )
        }
        
        // TrueTap FAB Button
        TrueTapButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp),
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showTrueTapSheet = true 
            }
        )
    }
    
    // TrueTap Bottom Sheet
    if (showTrueTapSheet) {
        TrueTapBottomSheet(
            viewModel = walletViewModel,
            onDismiss = { showTrueTapSheet = false }
        )
    }
}

@Composable
private fun DashboardHeader(
    greeting: String,
    subtitle: String,
    tappyOffset: Float,
    fadeAnimation: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = fadeAnimation),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tappy Logo with animation
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer(translationY = tappyOffset)
                .clip(CircleShape)
                .background(TrueTapPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "TrueTap Logo",
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.medium))
        
        Column {
            Text(
                text = greeting,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = TrueTapTextSecondary
            )
        }
    }
}

@Composable
private fun BalanceCard(
    isVisible: Boolean,
    balance: Double,
    isConnected: Boolean,
    tokenBalances: List<TokenBalance>,
    scale: Float,
    onToggleVisibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onToggleVisibility() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing.cardElevation),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_wallet),
                        contentDescription = "Wallet",
                        tint = TrueTapPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total Balance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                }
                
                Icon(
                    painter = painterResource(
                        id = if (isVisible) R.drawable.ic_eye else R.drawable.ic_eye_slash
                    ),
                    contentDescription = if (isVisible) "Hide Balance" else "Show Balance",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Balance Content
            if (isVisible) {
                Text(
                    text = if (isConnected) "${String.format("%.4f", balance)} SOL" else "Not Connected",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                if (isConnected && tokenBalances.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    tokenBalances.take(3).forEach { token ->
                        Text(
                            text = "${String.format("%.2f", token.uiAmount)} ${token.symbol}",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary
                        )
                    }
                }
            } else {
                Text(
                    text = "••••••",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextInactive,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Tap to reveal",
                    fontSize = 14.sp,
                    color = TrueTapTextInactive
                )
            }
        }
    }
}

@Composable
private fun QuickActions(
    onTrueTapClick: () -> Unit,
    onContactsClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = R.drawable.ic_paper_plane,
            label = "TrueTap",
            onClick = onTrueTapClick
        )
        
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = R.drawable.ic_users,
            label = "Contacts",
            onClick = onContactsClick
        )
        
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = R.drawable.ic_clock,
            label = "Schedule",
            onClick = onScheduleClick
        )
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF7A45),
                            Color(0xFFFF5722)
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
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
                        if (transaction.type == TransactionType.RECEIVED) {
                            TrueTapSuccess.copy(alpha = 0.2f)
                        } else {
                            TrueTapError.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (transaction.type == TransactionType.RECEIVED) {
                            R.drawable.ic_arrow_down_left
                        } else {
                            R.drawable.ic_arrow_up_right
                        }
                    ),
                    contentDescription = transaction.type.name,
                    tint = if (transaction.type == TransactionType.RECEIVED) {
                        TrueTapSuccess
                    } else {
                        TrueTapError
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.otherParty?.let {
                        "${if (transaction.type == TransactionType.SENT) "To" else "From"} ${it.take(6)}...${it.takeLast(4)}"
                    } ?: transaction.recipient,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = transaction.timestamp?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: transaction.time,
                    fontSize = 13.sp,
                    color = TrueTapTextInactive
                )
            }
            
            // Amount
            Text(
                text = if (transaction.amount.startsWith("+") || transaction.amount.startsWith("-")) {
                    transaction.amount
                } else {
                    "${if (transaction.type == TransactionType.RECEIVED) "+" else "-"}${transaction.amount}"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.RECEIVED) {
                    TrueTapSuccess
                } else {
                    TrueTapError
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailModal(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onCopyHash: (String) -> Unit,
    onOpenExplorer: (String) -> Unit,
    onAddNote: (txId: String, message: String, isPrivate: Boolean) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.type == TransactionType.RECEIVED) {
                                TrueTapSuccess.copy(alpha = 0.2f)
                            } else {
                                TrueTapError.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (transaction.type == TransactionType.RECEIVED) {
                                R.drawable.ic_arrow_down_left
                            } else {
                                R.drawable.ic_arrow_up_right
                            }
                        ),
                        contentDescription = transaction.type.name,
                        tint = if (transaction.type == TransactionType.RECEIVED) {
                            TrueTapSuccess
                        } else {
                            TrueTapError
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (transaction.type == TransactionType.RECEIVED) "Received" else "Sent",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                
                Text(
                    text = transaction.amount,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Privacy and scam alerts
            Text(
                text = "Private by default — hide from feed. Verify sender before accepting.",
                fontSize = 12.sp,
                color = TrueTapTextInactive
            )
            Spacer(modifier = Modifier.height(Spacing.medium))

            // Details
            TransactionDetailRow(
                label = "Status",
                value = transaction.status.name.lowercase().replaceFirstChar { it.uppercase() }
            )
            
            TransactionDetailRow(
                label = "Time",
                value = transaction.timestamp?.let {
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(it))
                } ?: transaction.time
            )
            
            TransactionDetailRow(
                label = if (transaction.type == TransactionType.RECEIVED) "From" else "To",
                value = transaction.otherParty ?: transaction.recipient
            )
            
            transaction.fee?.let { fee ->
                TransactionDetailRow(
                    label = "Network Fee",
                    value = fee
                )
            }
            
            transaction.transactionHash?.let { hash ->
                TransactionDetailRow(
                    label = "Transaction Hash",
                    value = hash,
                    copyable = true,
                    onCopy = { onCopyHash(hash) },
                    onExternalLink = { onOpenExplorer(hash) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action Buttons and social interactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Share functionality */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Share")
                }
                
                Button(
                    onClick = { /* Repeat transaction */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
                ) {
                    Text("Repeat Transaction")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))
            Text("Add a note (private by default)", fontSize = 12.sp, color = TrueTapTextSecondary)
            var note by remember { mutableStateOf("") }
            OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val txId = transaction.transactionHash ?: transaction.id
                    if (note.isNotBlank()) onAddNote(txId, note, true)
                    note = ""
                }) { Text("Save Note") }
                OutlinedButton(onClick = {
                    val txId = transaction.transactionHash ?: transaction.id
                    if (note.isNotBlank()) onAddNote(txId, note, false)
                    note = ""
                }) { Text("Post Public") }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TransactionDetailRow(
    label: String,
    value: String,
    copyable: Boolean = false,
    onCopy: (() -> Unit)? = null,
    onExternalLink: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = TrueTapTextSecondary,
            fontWeight = FontWeight.Medium
        )
        
        if (copyable && onCopy != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value.take(8) + "..." + value.takeLast(8),
                    fontSize = 16.sp,
                    color = TrueTapTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, false)
                )
                
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy),
                        contentDescription = "Copy",
                        tint = TrueTapPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                onExternalLink?.let { onExternal ->
                    IconButton(
                        onClick = onExternal,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_external_link),
                            contentDescription = "Open Explorer",
                            tint = TrueTapPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                color = TrueTapTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}