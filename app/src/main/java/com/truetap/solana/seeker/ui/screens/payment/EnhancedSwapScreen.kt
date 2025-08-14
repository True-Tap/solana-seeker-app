/**
 * Enhanced SwapScreen - TrueTap
 * Beautiful, primo swap interface with Jupiter V6 integration
 * Real-time pricing, route optimization, and comprehensive error handling
 */

package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.components.BottomNavItem
import com.truetap.solana.seeker.viewmodels.SwapViewModel
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.truetap.solana.seeker.viewmodels.SwapState
import com.truetap.solana.seeker.domain.model.CryptoToken
import com.truetap.solana.seeker.data.WalletState
import com.truetap.solana.seeker.presentation.components.*
import com.truetap.solana.seeker.services.FeePreset
import kotlinx.coroutines.delay
import java.math.BigDecimal
import android.content.Context
import android.content.ContextWrapper

/**
 * Extension function to find the Activity from any Context
 */
fun Context.findActivity(): android.app.Activity? {
    var context = this
    while (context !is android.app.Activity) {
        context = (context as? ContextWrapper)?.baseContext ?: return null
    }
    return context
}

@Composable
fun EnhancedSwapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    swapViewModel: SwapViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context.findActivity()
    
    // Create ActivityResultLauncher for Seed Vault operations
    val activityResultLauncher = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult().let { contract ->
        androidx.activity.compose.rememberLauncherForActivityResult(contract) { result ->
            // Handle results if needed
            android.util.Log.d("EnhancedSwapScreen", "Seed Vault operation result: ${result.resultCode}")
        }
    }
    // Get state from ViewModels
    val swapUiState by swapViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.walletState.collectAsStateWithLifecycle()
    
    // Modal states
    var showTokenSelectorModal by remember { mutableStateOf<TokenSelectorType?>(null) }
    var showWalletSelectorModal by remember { mutableStateOf(false) }
    var showSpeedSelectorModal by remember { mutableStateOf(false) }
    var showRouteSelectorModal by remember { mutableStateOf(false) }
    
    // UI states
    var showBalanceInUSD by remember { mutableStateOf(false) }
    
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "swap_animations")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Initialize wallet address and Genesis NFT status when connected
    LaunchedEffect(walletState.account, walletState.hasGenesisNFT) {
        walletState.account?.publicKey?.let { address ->
            swapViewModel.updateWalletAddress(address)
            swapViewModel.updateGenesisNFTStatus(walletState.hasGenesisNFT, walletState.genesisNFTTier)
        }
    }
    
    // Auto-refresh quotes every 10 seconds
    LaunchedEffect(swapUiState.inputAmount, swapUiState.inputToken, swapUiState.outputToken) {
        if (swapUiState.inputAmount.isNotEmpty() && 
            swapUiState.inputAmount != "0" && 
            swapUiState.inputAmount.toBigDecimalOrNull() != null) {
            while (true) {
                swapViewModel.getSwapQuote()
                delay(10000) // Refresh every 10 seconds
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TrueTapBackground,
                        TrueTapBackground.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        // Main content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // Header with wallet selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Swap",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary
                        )
                        Text(
                            text = "Best rates across all DEXs",
                            fontSize = 16.sp,
                            color = TrueTapPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Wallet selector
                    val account = walletState.account
                    if (account != null) {
                        WalletSelectorButton(
                            walletAddress = account.publicKey,
                            isGenesisHolder = swapUiState.isGenesisHolder,
                            onClick = { showWalletSelectorModal = true }
                        )
                    }
                }
            }
            
            // Network status warning
            if (swapUiState.networkCongestionMessage != null) {
                item {
                    NetworkStatusCard(
                        message = swapUiState.networkCongestionMessage!!,
                        status = swapUiState.networkCongestionStatus,
                        recommendedSpeed = swapUiState.recommendedSpeed
                    )
                }
            }
            
            // Route and Speed controls + Fee preset selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RouteCard(
                        modifier = Modifier.weight(1f),
                        route = swapUiState.route,
                        priceImpact = swapUiState.priceImpact,
                        onClick = { showRouteSelectorModal = true }
                    )
                    
                    SpeedCard(
                        modifier = Modifier.weight(1f),
                        speed = swapUiState.transactionSpeed,
                        slippage = swapUiState.slippagePercent,
                        onClick = { showSpeedSelectorModal = true },
                        isRecommended = swapUiState.transactionSpeed == swapUiState.recommendedSpeed
                    )
                }
            }

            // Fee preset selector with tooltip
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FeePresetSelector(
                        selected = swapUiState.feePreset,
                        onSelected = { preset ->
                            when (preset) {
                                FeePreset.NORMAL -> swapViewModel.setTransactionSpeed("Normal")
                                FeePreset.FAST -> swapViewModel.setTransactionSpeed("Fast")
                                FeePreset.EXPRESS -> swapViewModel.setTransactionSpeed("Express")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Normal (0), Fast (500), Express (5000) microLamports per compute unit. Fees may increase on retry during congestion.",
                        color = TrueTapTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            
            // From Token Section
            item {
                SwapTokenCard(
                    token = swapUiState.inputToken,
                    amount = swapUiState.inputAmount,
                    balance = getTokenBalance(swapUiState.inputToken, walletState),
                    price = swapUiState.tokenPrices[swapUiState.inputToken.symbol],
                    showBalanceInUSD = showBalanceInUSD,
                    onAmountChange = { swapViewModel.updateInputAmount(it) },
                    onTokenClick = { showTokenSelectorModal = TokenSelectorType.INPUT },
                    onBalanceClick = { showBalanceInUSD = !showBalanceInUSD },
                    onMaxClick = { 
                        val maxAmount = getTokenBalance(swapUiState.inputToken, walletState)
                        swapViewModel.updateInputAmount(maxAmount.toString())
                    },
                    isInput = true
                )
            }
            
            // Swap Button with animation
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = { swapViewModel.swapTokens() },
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                            .semantics { 
                                contentDescription = "Swap tokens button. Tap to exchange your selected tokens at the current market rate"
                            },
                        containerColor = TrueTapPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Swap tokens",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            // To Token Section
            item {
                SwapTokenCard(
                    token = swapUiState.outputToken,
                    amount = swapUiState.outputAmount,
                    balance = getTokenBalance(swapUiState.outputToken, walletState),
                    price = swapUiState.tokenPrices[swapUiState.outputToken.symbol],
                    showBalanceInUSD = showBalanceInUSD,
                    onAmountChange = { swapViewModel.updateOutputAmount(it) },
                    onTokenClick = { showTokenSelectorModal = TokenSelectorType.OUTPUT },
                    onBalanceClick = { showBalanceInUSD = !showBalanceInUSD },
                    onMaxClick = { },
                    isInput = false,
                    isLoading = swapUiState.isLoadingQuote
                )
            }
            
            // Quote details (only show when we have a quote)
            if (swapUiState.currentQuote != null) {
                item {
                    QuoteDetailsCard(
                        quote = swapUiState.currentQuote!!,
                        isGenesisHolder = swapUiState.isGenesisHolder
                    )
                }
            }
            
            // Swap execution button
            item {
                SwapExecutionButton(
                    swapState = swapUiState.swapState,
                    hasValidQuote = swapUiState.currentQuote != null,
                    hasValidAmount = swapUiState.inputAmount.isNotEmpty() && 
                                   swapUiState.inputAmount != "0",
                    onExecuteSwap = { 
                        if (activity != null) {
                            swapViewModel.executeSwap(activity, activityResultLauncher)
                        } else {
                            swapViewModel.clearError() // Clear any existing errors first
                            swapViewModel.setError("Unable to access wallet signing interface. Please try restarting the app.")
                        }
                    },
                    onShowConfirmation = { swapViewModel.showSwapConfirmation(true) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fees may change while queued.",
                    color = TrueTapTextSecondary,
                    fontSize = 12.sp
                )
            }
            
            // Error message
            if (swapUiState.errorMessage != null) {
                item {
                    ErrorCard(
                        message = swapUiState.errorMessage!!,
                        onDismiss = { swapViewModel.clearError() }
                    )
                }
            }
        }
    }
    
    // Modals
    if (showTokenSelectorModal != null) {
        TokenSelectorModal(
            type = showTokenSelectorModal!!,
            currentToken = if (showTokenSelectorModal == TokenSelectorType.INPUT) 
                swapUiState.inputToken else swapUiState.outputToken,
            onTokenSelected = { token ->
                when (showTokenSelectorModal) {
                    TokenSelectorType.INPUT -> swapViewModel.selectInputToken(token)
                    TokenSelectorType.OUTPUT -> swapViewModel.selectOutputToken(token)
                    null -> {}
                }
                showTokenSelectorModal = null
            },
            onDismiss = { showTokenSelectorModal = null }
        )
    }
    
    if (showWalletSelectorModal) {
        WalletSelectorModal(
            wallets = walletState.connectedWallets,
            currentWallet = walletState.account,
            onWalletSelected = { /* Handle wallet switch */ },
            onDismiss = { showWalletSelectorModal = false }
        )
    }
    
    if (showSpeedSelectorModal) {
        SpeedSelectorModal(
            currentSpeed = swapUiState.transactionSpeed,
            currentSlippage = swapUiState.slippagePercent,
            onSpeedSelected = { speed ->
                swapViewModel.setTransactionSpeed(speed)
                showSpeedSelectorModal = false
            },
            onSlippageChanged = { slippage ->
                swapViewModel.updateSlippage(slippage)
            },
            onDismiss = { showSpeedSelectorModal = false }
        )
    }
    
    if (showRouteSelectorModal) {
        RouteSelectorModal(
            availableRoutes = swapUiState.route,
            onRouteSelected = { /* Handle route selection */ },
            onDismiss = { showRouteSelectorModal = false }
        )
    }
    
    // Swap confirmation modal
    if (swapUiState.showSwapConfirmation) {
        SwapConfirmationDialog(
            quote = swapUiState.currentQuote!!,
            isGenesisHolder = swapUiState.isGenesisHolder,
            onConfirm = { 
                if (activity != null) {
                    swapViewModel.executeSwap(activity, activityResultLauncher)
                } else {
                    swapViewModel.clearError()
                    swapViewModel.setError("Unable to access wallet signing interface. Please try restarting the app.")
                }
            },
            onDismiss = { swapViewModel.showSwapConfirmation(false) }
        )
    }
}

@Composable
private fun WalletSelectorButton(
    walletAddress: String,
    isGenesisHolder: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGenesisHolder) 
                TrueTapSuccess.copy(alpha = 0.1f) else TrueTapContainer
        ),
        border = if (isGenesisHolder) BorderStroke(1.dp, TrueTapSuccess) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "Wallet",
                tint = if (isGenesisHolder) TrueTapSuccess else TrueTapPrimary,
                modifier = Modifier.size(20.dp)
            )
            
            Column {
                Text(
                    text = "${walletAddress.take(4)}...${walletAddress.takeLast(4)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextPrimary
                )
                if (isGenesisHolder) {
                    Text(
                        text = "Genesis • 0.25%",
                        fontSize = 12.sp,
                        color = TrueTapSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand",
                tint = TrueTapTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun RouteCard(
    modifier: Modifier = Modifier,
    route: List<String>,
    priceImpact: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = "Route",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Best Route",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
            }
            
            Text(
                text = if (route.isNotEmpty()) 
                    route.joinToString(" → ") else "Jupiter DEX",
                fontSize = 12.sp,
                color = TrueTapTextSecondary
            )
            
            PriceImpactIndicator(
                priceImpact = priceImpact,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SpeedCard(
    modifier: Modifier = Modifier,
    speed: String,
    slippage: Double,
    onClick: () -> Unit,
    isRecommended: Boolean = false
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) 
                TrueTapSuccess.copy(alpha = 0.1f) else TrueTapContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        border = if (isRecommended) BorderStroke(1.dp, TrueTapSuccess) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when (speed) {
                        "Fast" -> Icons.Default.Speed
                        "Normal" -> Icons.Default.Schedule
                        else -> Icons.Default.Eco
                    },
                    contentDescription = "Speed",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = speed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
            }
            
            Text(
                text = "${slippage}% slippage",
                fontSize = 12.sp,
                color = TrueTapTextSecondary
            )
            
            if (isRecommended) {
                Text(
                    text = "✓ Recommended",
                    fontSize = 10.sp,
                    color = TrueTapSuccess,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SwapTokenCard(
    token: CryptoToken,
    amount: String,
    balance: BigDecimal,
    price: BigDecimal?,
    showBalanceInUSD: Boolean,
    onAmountChange: (String) -> Unit,
    onTokenClick: () -> Unit,
    onBalanceClick: () -> Unit,
    onMaxClick: () -> Unit,
    isInput: Boolean,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInput) 
                TrueTapPrimary.copy(alpha = 0.05f) else TrueTapSuccess.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Token selector and amount input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Token selector
                TokenSelectorChip(
                    token = token,
                    isSelected = true,
                    onClick = onTokenClick
                )
                
                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.width(140.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary,
                        textAlign = TextAlign.End
                    ),
                    placeholder = {
                        Text(
                            text = "0.00",
                            color = TrueTapTextSecondary,
                            fontSize = 24.sp,
                            textAlign = TextAlign.End
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TrueTapTextPrimary,
                        unfocusedTextColor = TrueTapTextPrimary,
                        cursorColor = TrueTapPrimary
                    ),
                    enabled = isInput,
                    trailingIcon = {
                        if (isLoading && !isInput) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = TrueTapPrimary
                            )
                        }
                    }
                )
            }
            
            // Balance and price info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price info
                val usdValue = price?.multiply(balance) ?: BigDecimal.ZERO
                Column(
                    modifier = Modifier.clickable { onBalanceClick() }
                ) {
                    if (showBalanceInUSD) {
                        Text(
                            text = "$${"%.2f".format(usdValue.toDouble())}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary
                        )
                        Text(
                            text = "$balance ${token.symbol}",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary
                        )
                    } else {
                        Text(
                            text = "$balance ${token.symbol}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary
                        )
                        Text(
                            text = "$${"%.2f".format(usdValue.toDouble())}",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary
                        )
                    }
                }
                
                // MAX button for input
                if (isInput) {
                    Button(
                        onClick = onMaxClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TrueTapPrimary.copy(alpha = 0.1f),
                            contentColor = TrueTapPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MAX",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteDetailsCard(
    quote: com.truetap.solana.seeker.domain.model.SwapQuote,
    isGenesisHolder: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Quote Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary
            )
            
            DetailRow("Rate", "1 ${quote.inputToken.symbol} = ${quote.rate} ${quote.outputToken.symbol}")
            DetailRow("Network Fee", "${quote.networkFee} SOL")
            DetailRow("Platform Fee", "${quote.exchangeFee} ${quote.inputToken.symbol} (${if (isGenesisHolder) "0.25%" else "0.5%"})")
            DetailRow("Price Impact", "${String.format("%.3f", quote.priceImpact)}%")
            DetailRow("Minimum Received", "${quote.outputAmount.multiply(BigDecimal(1 - quote.slippage))} ${quote.outputToken.symbol}")
            
            if (quote.route.isNotEmpty()) {
                DetailRow("Route", quote.route.joinToString(" → "))
            }
        }
    }
}

@Composable
private fun SwapExecutionButton(
    swapState: SwapState,
    hasValidQuote: Boolean,
    hasValidAmount: Boolean,
    onExecuteSwap: () -> Unit,
    onShowConfirmation: () -> Unit
) {
    when (swapState) {
        SwapState.IDLE -> {
            Button(
                onClick = { 
                    if (hasValidQuote && hasValidAmount) {
                        onShowConfirmation()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasValidQuote && hasValidAmount) 
                        TrueTapPrimary else TrueTapTextSecondary,
                    disabledContainerColor = TrueTapTextSecondary
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = hasValidQuote && hasValidAmount
            ) {
                Text(
                    text = when {
                        !hasValidAmount -> "Enter Amount"
                        !hasValidQuote -> "Getting Quote..."
                        else -> "Review Swap"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        SwapState.PROCESSING -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrueTapPrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TrueTapPrimary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Processing Swap...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapPrimary
                    )
                }
            }
        }
        
        SwapState.SUCCESS -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapSuccess.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = TrueTapSuccess,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Swap Successful!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrueTapSuccess
                            )
                        }
                    }
                }
            }
        }
        
        SwapState.ERROR -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrueTapError.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = TrueTapError,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Swap Failed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TrueTapError
                        )
                    }
                    
                    TextButton(
                        onClick = onExecuteSwap,
                        colors = ButtonDefaults.textButtonColors(contentColor = TrueTapError)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapError.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, TrueTapError.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = TrueTapError,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = TrueTapError,
                    fontWeight = FontWeight.Medium
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TrueTapError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TrueTapTextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TrueTapTextPrimary
        )
    }
}

// Helper functions
private fun getTokenBalance(token: CryptoToken, walletState: WalletState): BigDecimal {
    return when (token.symbol) {
        "SOL" -> walletState.solBalance
        else -> walletState.tokenBalances[token.symbol] ?: BigDecimal.ZERO
    }
}

// Enum for token selector modal
enum class TokenSelectorType {
    INPUT, OUTPUT
}

// TODO: Implement the modal composables
@Composable
private fun TokenSelectorModal(
    type: TokenSelectorType,
    currentToken: CryptoToken,
    onTokenSelected: (CryptoToken) -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation will use existing TokenSelectorChip and modal logic
}

@Composable
private fun WalletSelectorModal(
    wallets: List<Any>, // Define proper wallet type
    currentWallet: Any?,
    onWalletSelected: (Any) -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation for wallet switching
}

@Composable
private fun SpeedSelectorModal(
    currentSpeed: String,
    currentSlippage: Double,
    onSpeedSelected: (String) -> Unit,
    onSlippageChanged: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation for speed/slippage selection
}

@Composable
private fun RouteSelectorModal(
    availableRoutes: List<String>,
    onRouteSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation for route selection
}


@Composable
private fun NetworkStatusCard(
    message: String,
    status: com.truetap.solana.seeker.services.NetworkCongestionStatus,
    recommendedSpeed: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                com.truetap.solana.seeker.services.NetworkCongestionStatus.HIGH -> 
                    TrueTapError.copy(alpha = 0.1f)
                com.truetap.solana.seeker.services.NetworkCongestionStatus.MEDIUM -> 
                    Color(0xFFFF9800).copy(alpha = 0.1f)
                com.truetap.solana.seeker.services.NetworkCongestionStatus.LOW -> 
                    TrueTapSuccess.copy(alpha = 0.1f)
                else -> TrueTapContainer
            }
        ),
        border = BorderStroke(
            1.dp, 
            when (status) {
                com.truetap.solana.seeker.services.NetworkCongestionStatus.HIGH -> 
                    TrueTapError.copy(alpha = 0.3f)
                com.truetap.solana.seeker.services.NetworkCongestionStatus.MEDIUM -> 
                    Color(0xFFFF9800).copy(alpha = 0.3f)
                com.truetap.solana.seeker.services.NetworkCongestionStatus.LOW -> 
                    TrueTapSuccess.copy(alpha = 0.3f)
                else -> Color.Transparent
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (status) {
                    com.truetap.solana.seeker.services.NetworkCongestionStatus.HIGH -> 
                        Icons.Default.Warning
                    com.truetap.solana.seeker.services.NetworkCongestionStatus.MEDIUM -> 
                        Icons.Default.Info
                    com.truetap.solana.seeker.services.NetworkCongestionStatus.LOW -> 
                        Icons.Default.CheckCircle
                    else -> Icons.Default.Info
                },
                contentDescription = "Network Status",
                tint = when (status) {
                    com.truetap.solana.seeker.services.NetworkCongestionStatus.HIGH -> TrueTapError
                    com.truetap.solana.seeker.services.NetworkCongestionStatus.MEDIUM -> Color(0xFFFF9800)
                    com.truetap.solana.seeker.services.NetworkCongestionStatus.LOW -> TrueTapSuccess
                    else -> TrueTapTextSecondary
                },
                modifier = Modifier.size(20.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = TrueTapTextPrimary,
                    fontWeight = FontWeight.Medium
                )
                
                if (recommendedSpeed.isNotEmpty()) {
                    Text(
                        text = "Recommended: $recommendedSpeed speed",
                        fontSize = 12.sp,
                        color = TrueTapTextSecondary
                    )
                }
            }
        }
    }
}