/**
 * SwapScreen - TrueTap
 * Advanced DeFi Trading interface
 * Kotlin Compose implementation
 */

package com.truetap.solana.seeker.ui.screens.payment

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.components.BottomNavItem
import com.truetap.solana.seeker.ui.components.TrueTapBottomNavigationBar
import com.truetap.solana.seeker.ui.components.rememberBottomNavHandler
import com.truetap.solana.seeker.viewmodels.SwapViewModel
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.truetap.solana.seeker.viewmodels.SwapState
import com.truetap.solana.seeker.domain.model.CryptoToken
import com.truetap.solana.seeker.domain.model.SwapQuote
import com.truetap.solana.seeker.data.WalletState
import com.truetap.solana.seeker.presentation.components.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal


@Composable
fun SwapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    swapViewModel: SwapViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    // Get state from ViewModels
    val swapUiState by swapViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.walletState.collectAsStateWithLifecycle()
    
    // Local state for UI interactions
    var showSolBalanceInUSD by remember { mutableStateOf(false) }
    var showUsdcBalanceInSOL by remember { mutableStateOf(false) }
    var isDetailsExpanded by remember { mutableStateOf(false) }
    
    // Modal states
    var showCurrencyFromModal by remember { mutableStateOf(false) }
    var showCurrencyToModal by remember { mutableStateOf(false) }
    var showTransactionSpeedModal by remember { mutableStateOf(false) }
    var showOptimalRouteModal by remember { mutableStateOf(false) }
    var showWalletSelectorModal by remember { mutableStateOf(false) }
    
    // Coroutine scope for async operations
    val coroutineScope = rememberCoroutineScope()
    
    // Get real wallet balances or use defaults
    val solBalance = walletState.solBalance.toFloat()
    val usdcBalance = walletState.tokenBalances["USDC"]?.toFloat() ?: 0f
    val solPrice = swapUiState.tokenPrices["SOL"]?.toFloat() ?: 103.37f
    val usdcPrice = swapUiState.tokenPrices["USDC"]?.toFloat() ?: 1.0f
    
    // Sync wallet address with SwapViewModel
    LaunchedEffect(walletState.account?.publicKey) {
        walletState.account?.publicKey?.let { address ->
            swapViewModel.updateWalletAddress(address)
            swapViewModel.updateGenesisNFTStatus(walletState.hasGenesisNFT, walletState.genesisNFTTier)
        }
    }
    
    // Calculate conversion rates
    fun calculateConversion(amount: String, fromToken: String, toToken: String): String {
        val numAmount = amount.toFloatOrNull() ?: 0f
        return when {
            fromToken == "SOL" && toToken == "USDC" -> (numAmount * solPrice).toString()
            fromToken == "USDC" && toToken == "SOL" -> (numAmount / solPrice).let { "%.6f".format(it) }
            fromToken == toToken -> amount
            else -> (numAmount * 1.0f).toString() // Default conversion
        }
    }
    
    // Update conversion when amounts change using SwapViewModel
    LaunchedEffect(swapUiState.inputAmount, swapUiState.inputToken, swapUiState.outputToken) {
        if (swapUiState.inputAmount.isNotEmpty() && swapUiState.inputAmount != "0.00") {
            swapViewModel.getSwapQuote()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        // Main content area with scrolling
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Header Section
            item {
                Column {
                    Text(
                        text = "Swap",
                        fontSize = 28.sp, // Increased from 24sp
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary
                    )
                    Text(
                        text = "Advanced DeFi Trading",
                        fontSize = 16.sp, // Increased from 14sp
                        color = Color(0xFFEB5017),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Genesis Holder Badge - Smaller
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Genesis",
                            tint = TrueTapPrimary,
                            modifier = Modifier.size(18.dp) // Increased from 16dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Genesis Holder • Premium benefits",
                            fontSize = 14.sp, // Increased from 12sp
                            fontWeight = FontWeight.Medium,
                            color = TrueTapTextPrimary
                        )
                    }
                }
            }
            
            // Route and Speed Section - Combined
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
                ) {
                    // Optimal Route - Compact
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showOptimalRouteModal = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp) // Increased padding
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Circle,
                                    contentDescription = "Route",
                                    tint = TrueTapPrimary,
                                    modifier = Modifier.size(8.dp) // Increased from 6dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Best Route",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TrueTapTextPrimary
                                )
                            }
                            Text(
                                text = if (swapUiState.route.isNotEmpty()) 
                                    swapUiState.route.joinToString(" → ") else "Jupiter DEX",
                                fontSize = 12.sp,
                                color = TrueTapTextSecondary
                            )
                        }
                    }
                    
                    // Transaction Speed - Compact
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTransactionSpeedModal = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp) // Increased padding
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Speed",
                                    tint = TrueTapPrimary,
                                    modifier = Modifier.size(14.dp) // Increased from 12dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = swapUiState.transactionSpeed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TrueTapTextPrimary
                                )
                            }
                            Text(
                                text = "${swapUiState.slippagePercent}% slippage",
                                fontSize = 12.sp,
                                color = TrueTapTextSecondary
                            )
                        }
                    }
                }
            }
            
            // From Token Section (removed "Selling" label)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSolBalanceInUSD = !showSolBalanceInUSD },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF3E7))
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = when (swapUiState.inputToken.symbol) {
                                "SOL" -> R.drawable.solana
                                "USDC" -> R.drawable.usdc
                                "ORCA" -> R.drawable.orca
                                "BONK" -> R.drawable.bonk
                                "JUP" -> R.drawable.jupiter
                                "RAY" -> R.drawable.raydium
                                else -> R.drawable.solana
                            }),
                            contentDescription = "${swapUiState.inputToken.symbol} icon",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.clickable { showCurrencyFromModal = true }
                        ) {
                            Text(
                                text = swapUiState.inputToken.symbol,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrueTapTextPrimary
                            )
                            Text(
                                text = "+5.2%",
                                fontSize = 14.sp, // Increased from 12sp
                                color = Color(0xFF22C55E)
                            )
                        }
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        // Get current token balance and price info
                        val inputAmount = swapUiState.inputAmount.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                        val currentTokenBalance = when (swapUiState.inputToken.symbol) {
                            "SOL" -> walletState.solBalance
                            else -> walletState.tokenBalances[swapUiState.inputToken.symbol] ?: java.math.BigDecimal.ZERO
                        }
                        val hasInsufficientBalance = inputAmount > currentTokenBalance
                        val currentTokenPrice = swapUiState.tokenPrices[swapUiState.inputToken.symbol]?.toFloat() ?: when (swapUiState.inputToken.symbol) {
                            "SOL" -> solPrice
                            "USDC" -> usdcPrice
                            else -> 1.0f
                        }
                        
                        OutlinedTextField(
                            value = swapUiState.inputAmount,
                            onValueChange = { swapViewModel.updateInputAmount(it) },
                            modifier = Modifier.width(130.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp, // Increased from 18sp
                                fontWeight = FontWeight.Bold,
                                color = TrueTapTextPrimary,
                                textAlign = TextAlign.End
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TrueTapPrimary,
                                unfocusedBorderColor = TrueTapTextSecondary.copy(alpha = 0.3f),
                                focusedTextColor = TrueTapTextPrimary,
                                unfocusedTextColor = TrueTapTextPrimary,
                                cursorColor = TrueTapPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            if (showSolBalanceInUSD) {
                                Text(
                                    text = "$${"%.2f".format(solBalance * solPrice)}",
                                    fontSize = 16.sp, // Increased from 14sp
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary
                                )
                                Text(
                                    text = "$solBalance SOL",
                                    fontSize = 14.sp, // Increased from 12sp
                                    color = TrueTapTextSecondary
                                )
                            } else {
                                Text(
                                    text = "$solBalance SOL",
                                    fontSize = 16.sp, // Increased from 14sp
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary
                                )
                                Text(
                                    text = "$${"%.2f".format(solBalance * solPrice)}",
                                    fontSize = 14.sp, // Increased from 12sp
                                    color = TrueTapTextSecondary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1 ${swapUiState.inputToken.symbol} = $${String.format("%.2f", currentTokenPrice)}",
                                    fontSize = 12.sp,
                                    color = TrueTapTextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MAX",
                                    fontSize = 12.sp, // Increased from 10sp
                                    color = TrueTapPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                }
            }
            
            // Swap Icon
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp) // Increased from 40dp
                            .background(TrueTapPrimary, CircleShape)
                            .clickable { 
                                swapViewModel.swapTokens()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Swap",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp) // Increased from 20dp
                        )
                    }
                }
            }
            
            // To Token Section (removed "Buying" label)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUsdcBalanceInSOL = !showUsdcBalanceInSOL },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = when (swapUiState.outputToken.symbol) {
                                "SOL" -> R.drawable.solana
                                "USDC" -> R.drawable.usdc
                                "ORCA" -> R.drawable.orca
                                "BONK" -> R.drawable.bonk
                                "JUP" -> R.drawable.jupiter
                                "RAY" -> R.drawable.raydium
                                else -> R.drawable.usdc
                            }),
                            contentDescription = "${swapUiState.outputToken.symbol} icon",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.clickable { showCurrencyToModal = true }
                        ) {
                            Text(
                                text = swapUiState.outputToken.symbol,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrueTapTextPrimary
                            )
                            Text(
                                text = "+0.1%",
                                fontSize = 14.sp, // Increased from 12sp
                                color = Color(0xFF22C55E)
                            )
                        }
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        // Get output token balance and price info
                        val outputTokenBalance = when (swapUiState.outputToken.symbol) {
                            "SOL" -> walletState.solBalance
                            else -> walletState.tokenBalances[swapUiState.outputToken.symbol] ?: java.math.BigDecimal.ZERO
                        }
                        val outputTokenPrice = swapUiState.tokenPrices[swapUiState.outputToken.symbol]?.toFloat() ?: when (swapUiState.outputToken.symbol) {
                            "SOL" -> solPrice
                            "USDC" -> usdcPrice
                            else -> 1.0f
                        }
                        
                        OutlinedTextField(
                            value = swapUiState.outputAmount,
                            onValueChange = { 
                                swapViewModel.updateOutputAmount(it)
                            },
                            modifier = Modifier.width(130.dp), // Increased width
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp, // Increased from 18sp
                                fontWeight = FontWeight.Bold,
                                color = TrueTapTextPrimary,
                                textAlign = TextAlign.End
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TrueTapPrimary,
                                unfocusedBorderColor = TrueTapTextSecondary.copy(alpha = 0.3f),
                                focusedTextColor = TrueTapTextPrimary,
                                unfocusedTextColor = TrueTapTextPrimary,
                                cursorColor = TrueTapPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            val outputBalanceFloat = outputTokenBalance.toFloat()
                            val outputBalanceText = "${String.format("%.4f", outputBalanceFloat)} ${swapUiState.outputToken.symbol}"
                            val outputUsdValueText = "$${String.format("%.2f", outputBalanceFloat * outputTokenPrice)}"
                            
                            if (showUsdcBalanceInSOL && swapUiState.outputToken.symbol == "USDC") {
                                Text(
                                    text = "${String.format("%.3f", outputBalanceFloat / solPrice)} SOL",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary
                                )
                                Text(
                                    text = outputUsdValueText,
                                    fontSize = 14.sp,
                                    color = TrueTapTextSecondary
                                )
                            } else {
                                Text(
                                    text = outputBalanceText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary
                                )
                                Text(
                                    text = outputUsdValueText,
                                    fontSize = 14.sp,
                                    color = TrueTapTextSecondary
                                )
                            }
                            Text(
                                text = "1 ${swapUiState.outputToken.symbol} = $${String.format("%.2f", outputTokenPrice)}",
                                fontSize = 12.sp,
                                color = TrueTapTextSecondary
                            )
                        }
                    }
                }
                }
            }
            
            // Details Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { swapViewModel.toggleDetails() },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Details",
                            fontSize = 14.sp, // Increased from 12sp
                            fontWeight = FontWeight.Medium,
                            color = TrueTapTextPrimary
                        )
                        Icon(
                            imageVector = if (swapUiState.isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Details",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(18.dp) // Increased from 16dp
                        )
                    }
                    
                    if (swapUiState.isDetailsExpanded) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            val quote = swapUiState.currentQuote
                            DetailRow("Rate", quote?.let { "1 ${it.inputToken.symbol} = ${String.format("%.4f", it.rate)} ${it.outputToken.symbol}" } ?: "1 SOL = $103.37")
                            DetailRow("Network Fee", quote?.networkFee?.toPlainString()?.let { "$it SOL" } ?: "0.00025 SOL")
                            DetailRow("Route", quote?.route?.joinToString(" → ") ?: "Jupiter DEX")
                            DetailRow("Slippage", "${swapUiState.slippagePercent}%")
                            quote?.let {
                                DetailRow("Price Impact", "${String.format("%.2f", it.priceImpact)}%")
                                DetailRow("Estimated Time", "${it.estimatedTime}s")
                            }
                        }
                    }
                }
            }
            
            // Swap Button
            item {
                Button(
                    onClick = { 
                        swapViewModel.showSwapConfirmation(true)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp), // Increased from 48dp
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = swapUiState.inputAmount.isNotEmpty() && 
                             swapUiState.inputAmount != "0.00" && 
                             swapUiState.outputAmount.isNotEmpty() && 
                             swapUiState.outputAmount != "0.00" &&
                             walletState.isConnected &&
                             !swapUiState.isLoadingQuote
                ) {
                    Text(
                        text = "Review Swap",
                        fontSize = 18.sp, // Increased from 16sp
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
        
        // Bottom Navigation Bar
        TrueTapBottomNavigationBar(
            selectedTab = BottomNavItem.SWAP,
            onTabSelected = rememberBottomNavHandler(
                currentScreen = BottomNavItem.SWAP,
                onNavigateToHome = onNavigateToHome,
                onNavigateToContacts = onNavigateToContacts,
                onNavigateToNFTs = onNavigateToNFTs,
                onNavigateToSettings = onNavigateToSettings
            )
        )
    }
    
    // Transaction Speed Modal
    if (showTransactionSpeedModal) {
        TransactionSpeedModal(
            currentSpeed = swapUiState.transactionSpeed,
            onSpeedSelected = { speed ->
                swapViewModel.setTransactionSpeed(speed)
                val slippagePercent = when (speed) {
                    "Fast" -> 0.5
                    "Normal" -> 0.3
                    "Eco" -> 0.1
                    else -> 0.1
                }
                swapViewModel.updateSlippage(slippagePercent)
                showTransactionSpeedModal = false
            },
            onDismiss = { showTransactionSpeedModal = false }
        )
    }
    
    // Optimal Route Modal
    if (showOptimalRouteModal) {
        OptimalRouteModal(
            onDismiss = { showOptimalRouteModal = false }
        )
    }
    
    // Currency From Modal
    if (showCurrencyFromModal) {
        CurrencySelectionModal(
            currentCurrency = swapUiState.inputToken.symbol,
            availableTokens = (walletState.tokenBalances.keys.toList() + listOf("SOL")).distinct(),
            showBalances = true,
            walletState = walletState,
            onCurrencySelected = { currency ->
                val token = CryptoToken.fromSymbol(currency)
                token?.let { swapViewModel.selectInputToken(it) }
                showCurrencyFromModal = false
            },
            onDismiss = { showCurrencyFromModal = false }
        )
    }
    
    // Currency To Modal
    if (showCurrencyToModal) {
        CurrencySelectionModal(
            currentCurrency = swapUiState.outputToken.symbol,
            availableTokens = CryptoToken.values().map { it.symbol },
            showBalances = false,
            walletState = walletState,
            onCurrencySelected = { currency ->
                val token = CryptoToken.fromSymbol(currency)
                token?.let { swapViewModel.selectOutputToken(it) }
                showCurrencyToModal = false
            },
            onDismiss = { showCurrencyToModal = false }
        )
    }
    
    // Swap Confirmation Modal
    if (swapUiState.showSwapConfirmation) {
        SwapConfirmationModal(
            fromToken = swapUiState.inputToken.symbol,
            toToken = swapUiState.outputToken.symbol,
            fromAmount = swapUiState.inputAmount,
            toAmount = swapUiState.outputAmount,
            transactionSpeed = swapUiState.transactionSpeed,
            currentSlippage = "${swapUiState.slippagePercent}%",
            swapState = swapUiState.swapState,
            quote = swapUiState.currentQuote,
            onConfirmSwap = {
                swapViewModel.executeSwap()
            },
            onDismiss = { 
                when (swapUiState.swapState) {
                    SwapState.IDLE, SwapState.SUCCESS, SwapState.ERROR -> {
                        swapViewModel.showSwapConfirmation(false)
                        if (swapUiState.swapState != SwapState.IDLE) {
                            swapViewModel.resetSwap()
                        }
                    }
                    SwapState.PROCESSING -> {
                        // Don't allow dismissal during processing
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionSpeedModal(
    currentSpeed: String,
    onSpeedSelected: (String) -> Unit,
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
            Text(
                text = "Transaction Speed",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Speed Options with icons
            val speedOptions = listOf(
                Triple("Fast", "0.5% slippage", "Higher fees, faster execution"),
                Triple("Moderate", "0.3% slippage", "Balanced fees and speed"),
                Triple("Eco", "0.1% slippage", "Lower fees, slower execution")
            )
            
            speedOptions.forEach { (speed, slippage, description) ->
                SpeedOptionCard(
                    speed = speed,
                    slippage = slippage,
                    description = description,
                    isSelected = speed == currentSpeed,
                    onClick = { onSpeedSelected(speed) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SpeedOptionCard(
    speed: String,
    slippage: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TrueTapPrimary.copy(alpha = 0.1f) else TrueTapContainer
        ),
        border = if (isSelected) BorderStroke(2.dp, TrueTapPrimary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Icon
            Icon(
                imageVector = when (speed) {
                    "Fast" -> Icons.Outlined.Bolt
                    "Moderate" -> Icons.Outlined.Balance
                    "Eco" -> Icons.Outlined.Eco
                    else -> Icons.Outlined.Speed
                },
                contentDescription = speed,
                tint = if (isSelected) TrueTapPrimary else TrueTapTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = speed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = slippage,
                    fontSize = 14.sp,
                    color = TrueTapPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TrueTapTextPrimary
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptimalRouteModal(
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
            Text(
                text = "Available Routes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Route Options (mock data)
            val routes = listOf(
                Triple("Raydium → Orca", "98.7% efficiency", true),
                Triple("Jupiter → Serum", "97.2% efficiency", false),
                Triple("Orca → Raydium", "96.8% efficiency", false)
            )
            
            routes.forEach { (route, efficiency, isBest) ->
                RouteOptionCard(
                    route = route,
                    efficiency = efficiency,
                    isBest = isBest
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RouteOptionCard(
    route: String,
    efficiency: String,
    isBest: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBest) TrueTapPrimary.copy(alpha = 0.1f) else TrueTapContainer
        ),
        border = if (isBest) BorderStroke(2.dp, TrueTapPrimary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = route,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    if (isBest) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BEST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .background(TrueTapPrimary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = efficiency,
                    fontSize = 14.sp,
                    color = TrueTapPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (isBest) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Best Route",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelectionModal(
    currentCurrency: String,
    availableTokens: List<String> = CryptoToken.values().map { it.symbol },
    showBalances: Boolean = true,
    walletState: WalletState? = null,
    onCurrencySelected: (String) -> Unit,
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
            Text(
                text = "Select Currency",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Show available tokens with balances
            val availableCurrencies = if (showBalances && walletState != null) {
                // For input tokens, only show tokens with balances
                availableTokens.filter { tokenSymbol ->
                    when (tokenSymbol) {
                        "SOL" -> walletState.solBalance > java.math.BigDecimal.ZERO
                        else -> walletState.tokenBalances[tokenSymbol]?.let { it > java.math.BigDecimal.ZERO } ?: false
                    }
                }.map { symbol ->
                    val token = CryptoToken.fromSymbol(symbol)
                    Triple(symbol, token?.displayName ?: symbol, 
                        when (symbol) {
                            "SOL" -> walletState.solBalance
                            else -> walletState.tokenBalances[symbol] ?: java.math.BigDecimal.ZERO
                        }
                    )
                }
            } else {
                // For output tokens, show all available tokens
                availableTokens.map { symbol ->
                    val token = CryptoToken.fromSymbol(symbol)
                    Triple(symbol, token?.displayName ?: symbol, 
                        walletState?.let { state ->
                            when (symbol) {
                                "SOL" -> state.solBalance
                                else -> state.tokenBalances[symbol] ?: java.math.BigDecimal.ZERO
                            }
                        } ?: java.math.BigDecimal.ZERO
                    )
                }
            }
            
            availableCurrencies.forEach { (symbol, name, balance) ->
                CurrencyOptionCard(
                    symbol = symbol,
                    name = name,
                    balance = balance,
                    showBalance = showBalances,
                    isSelected = symbol == currentCurrency,
                    onClick = { onCurrencySelected(symbol) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CurrencyOptionCard(
    symbol: String,
    name: String,
    balance: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    showBalance: Boolean = false,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TrueTapPrimary.copy(alpha = 0.1f) else TrueTapContainer
        ),
        border = if (isSelected) BorderStroke(2.dp, TrueTapPrimary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Currency Icon from crypto_icons directory
            val iconResource = when (symbol) {
                "SOL" -> R.drawable.solana
                "USDC" -> R.drawable.usdc
                "ORCA" -> R.drawable.orca
                "BONK" -> R.drawable.bonk
                "JUP" -> R.drawable.jupiter
                "RAY" -> R.drawable.raydium
                else -> R.drawable.solana
            }
            
            Image(
                painter = painterResource(id = iconResource),
                contentDescription = "$symbol icon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = symbol,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )
                    if (showBalance) {
                        Text(
                            text = "${String.format("%.4f", balance.toFloat())} $symbol",
                            fontSize = 12.sp,
                            color = TrueTapTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(24.dp)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp), // Increased padding
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp, // Increased from 11sp
            color = TrueTapTextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp, // Increased from 11sp
            fontWeight = FontWeight.Medium,
            color = TrueTapTextPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwapConfirmationModal(
    fromToken: String,
    toToken: String,
    fromAmount: String,
    toAmount: String,
    transactionSpeed: String,
    currentSlippage: String,
    swapState: SwapState,
    quote: SwapQuote? = null,
    onConfirmSwap: () -> Unit,
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
            when (swapState) {
                SwapState.IDLE -> SwapConfirmationContent(
                    fromToken, toToken, fromAmount, toAmount, transactionSpeed, currentSlippage, quote, onConfirmSwap
                )
                SwapState.PROCESSING -> SwapProcessingContent()
                SwapState.SUCCESS -> SwapSuccessContent(fromToken, toToken, fromAmount, toAmount)
                SwapState.ERROR -> SwapFailureContent(onRetry = onConfirmSwap)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SwapConfirmationContent(
    fromToken: String,
    toToken: String,
    fromAmount: String,
    toAmount: String,
    transactionSpeed: String,
    currentSlippage: String,
    quote: SwapQuote? = null,
    onConfirmSwap: () -> Unit
) {
    Text(
        text = "Confirm Swap",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = TrueTapTextPrimary
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Swap Summary Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // From Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = when (fromToken) {
                            "SOL" -> R.drawable.solana
                            "USDC" -> R.drawable.usdc
                            "ORCA" -> R.drawable.orca
                            "BONK" -> R.drawable.bonk
                            "JUP" -> R.drawable.jupiter
                            "RAY" -> R.drawable.raydium
                            else -> R.drawable.solana
                        }),
                        contentDescription = "$fromToken icon",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fromToken,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                }
                Text(
                    text = fromAmount,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Swap Arrow
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Swap to",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // To Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = when (toToken) {
                            "SOL" -> R.drawable.solana
                            "USDC" -> R.drawable.usdc
                            "ORCA" -> R.drawable.orca
                            "BONK" -> R.drawable.bonk
                            "JUP" -> R.drawable.jupiter
                            "RAY" -> R.drawable.raydium
                            else -> R.drawable.usdc
                        }),
                        contentDescription = "$toToken icon",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = toToken,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                }
                Text(
                    text = toAmount,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Transaction Details
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Transaction Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val rateText = quote?.let { "1 ${it.inputToken.symbol} = ${String.format("%.4f", it.rate)} ${it.outputToken.symbol}" } ?: "1 SOL = $103.37"
            val feeText = quote?.networkFee?.toPlainString()?.let { "$it SOL" } ?: "0.00025 SOL"
            val routeText = quote?.route?.joinToString(" → ") ?: "Jupiter DEX"
            
            DetailRow("Rate", rateText)
            DetailRow("Network Fee", feeText)
            DetailRow("Speed", transactionSpeed)
            DetailRow("Slippage", currentSlippage)
            DetailRow("Route", routeText)
            quote?.let {
                DetailRow("Price Impact", "${String.format("%.2f", it.priceImpact)}%")
                DetailRow("Platform Fee", "${it.exchangeFee.toPlainString()} ${it.inputToken.symbol}")
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Confirm Button
    Button(
        onClick = onConfirmSwap,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Confirm Swap",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun SwapProcessingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Processing Swap",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapTextPrimary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Loading Animation
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = TrueTapPrimary,
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your swap is being processed on the blockchain...",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SwapSuccessContent(
    fromToken: String,
    toToken: String,
    fromAmount: String,
    toAmount: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Swap Successful!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapSuccess
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Success Icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = TrueTapSuccess,
            modifier = Modifier.size(60.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "You successfully swapped $fromAmount $fromToken for $toAmount $toToken",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SwapFailureContent(
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Swap Failed",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF4757)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Error Icon
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = Color(0xFFFF4757),
            modifier = Modifier.size(60.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Transaction failed due to insufficient funds or network issues. Please try again.",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Retry Button
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Try Again",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

