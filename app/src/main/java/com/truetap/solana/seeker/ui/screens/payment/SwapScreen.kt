/**
 * SwapScreen - TrueTap
 * Advanced DeFi Trading interface
 * Kotlin Compose implementation
 */

package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.truetap.solana.seeker.ui.screens.home.BottomNavItem
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

enum class SwapState {
    None,
    Processing,
    Success,
    Failed
}

@Composable
fun SwapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTokenFrom by remember { mutableStateOf("SOL") }
    var selectedTokenTo by remember { mutableStateOf("USDC") }
    var fromAmount by remember { mutableStateOf("0.00") }
    var toAmount by remember { mutableStateOf("0.0000") }
    var isDetailsExpanded by remember { mutableStateOf(false) }
    var showSolBalanceInUSD by remember { mutableStateOf(false) }
    var showUsdcBalanceInSOL by remember { mutableStateOf(false) }
    
    // New modal states
    var showCurrencyFromModal by remember { mutableStateOf(false) }
    var showCurrencyToModal by remember { mutableStateOf(false) }
    var showTransactionSpeedModal by remember { mutableStateOf(false) }
    var showOptimalRouteModal by remember { mutableStateOf(false) }
    var showSwapConfirmationModal by remember { mutableStateOf(false) }
    var swapState by remember { mutableStateOf<SwapState>(SwapState.None) }
    var transactionSpeed by remember { mutableStateOf("Eco") } // Default to Eco
    var currentSlippage by remember { mutableStateOf("0.1%") }
    
    // Coroutine scope for async operations
    val coroutineScope = rememberCoroutineScope()
    
    // Sample wallet balances and prices
    val solBalance = 12.45f
    val usdcBalance = 1234.56f
    val solPrice = 103.37f
    val usdcPrice = 1.0f
    
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
    
    // Update conversion when amounts change
    LaunchedEffect(fromAmount, selectedTokenFrom, selectedTokenTo) {
        if (fromAmount.isNotEmpty() && fromAmount != "0.00") {
            toAmount = calculateConversion(fromAmount, selectedTokenFrom, selectedTokenTo)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        // Main content area
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header Section
            Column {
                Text(
                    text = "Swap",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = "Advanced DeFi Trading",
                    fontSize = 14.sp,
                    color = Color(0xFFEB5017),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        
            // Genesis Holder Badge - Smaller
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Genesis Holder • Premium benefits",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        
            // Route and Speed Section - Combined
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = "Route",
                                tint = TrueTapPrimary,
                                modifier = Modifier.size(6.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Best Route",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TrueTapTextPrimary
                            )
                        }
                        Text(
                            text = "98.7% efficiency",
                            fontSize = 10.sp,
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
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Speed",
                                tint = TrueTapPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = transactionSpeed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TrueTapTextPrimary
                            )
                        }
                        Text(
                            text = currentSlippage,
                            fontSize = 10.sp,
                            color = TrueTapTextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        
            // Selling Section
            Column {
                Text(
                    text = "Selling",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
            
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
                            painter = painterResource(id = when (selectedTokenFrom) {
                                "SOL" -> R.drawable.solana
                                "USDC" -> R.drawable.usdc
                                "ORCA" -> R.drawable.orca
                                "BONK" -> R.drawable.bonk
                                "JUP" -> R.drawable.jupiter
                                "RAY" -> R.drawable.raydium
                                else -> R.drawable.solana
                            }),
                            contentDescription = "$selectedTokenFrom icon",
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
                                text = selectedTokenFrom,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrueTapTextPrimary
                            )
                            Text(
                                text = "+5.2%",
                                fontSize = 12.sp,
                                color = Color(0xFF22C55E)
                            )
                        }
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedTextField(
                            value = fromAmount,
                            onValueChange = { fromAmount = it },
                            modifier = Modifier.widthIn(min = 140.dp, max = 160.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 18.sp,
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
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary
                                )
                                Text(
                                    text = "$solBalance SOL",
                                    fontSize = 12.sp,
                                    color = TrueTapTextSecondary
                                )
                            } else {
                                Text(
                                    text = "$solBalance SOL",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary
                                )
                                Text(
                                    text = "$${"%.2f".format(solBalance * solPrice)}",
                                    fontSize = 12.sp,
                                    color = TrueTapTextSecondary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1 SOL = $$solPrice",
                                    fontSize = 10.sp,
                                    color = TrueTapTextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MAX",
                                    fontSize = 10.sp,
                                    color = TrueTapPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Swap Icon
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(TrueTapPrimary, CircleShape)
                        .clickable { 
                            // Swap tokens but keep the same input amount
                            val tempToken = selectedTokenFrom
                            selectedTokenFrom = selectedTokenTo
                            selectedTokenTo = tempToken
                            
                            // Recalculate the "to" amount based on new conversion
                            toAmount = calculateConversion(fromAmount, selectedTokenFrom, selectedTokenTo)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        
            // Buying Section
            Column {
                Text(
                    text = "Buying",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
            
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
                            painter = painterResource(id = when (selectedTokenTo) {
                                "SOL" -> R.drawable.solana
                                "USDC" -> R.drawable.usdc
                                "ORCA" -> R.drawable.orca
                                "BONK" -> R.drawable.bonk
                                "JUP" -> R.drawable.jupiter
                                "RAY" -> R.drawable.raydium
                                else -> R.drawable.usdc
                            }),
                            contentDescription = "$selectedTokenTo icon",
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
                                text = selectedTokenTo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrueTapTextPrimary
                            )
                            Text(
                                text = "+0.1%",
                                fontSize = 12.sp,
                                color = Color(0xFF22C55E)
                            )
                        }
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedTextField(
                            value = toAmount,
                            onValueChange = { 
                                toAmount = it
                                // Calculate reverse conversion for "from" amount
                                if (it.isNotEmpty() && it != "0.00") {
                                    fromAmount = calculateConversion(it, selectedTokenTo, selectedTokenFrom)
                                }
                            },
                            modifier = Modifier.widthIn(min = 140.dp, max = 160.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 18.sp,
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
                            if (showUsdcBalanceInSOL) {
                                Text(
                                    text = "${"%.3f".format(usdcBalance / solPrice)} SOL",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary
                                )
                                Text(
                                    text = "$${"%.2f".format(usdcBalance)}",
                                    fontSize = 12.sp,
                                    color = TrueTapTextSecondary
                                )
                            } else {
                                Text(
                                    text = "$${"%.2f".format(usdcBalance)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary
                                )
                                Text(
                                    text = "${"%.3f".format(usdcBalance / solPrice)} SOL",
                                    fontSize = 12.sp,
                                    color = TrueTapTextSecondary
                                )
                            }
                            Text(
                                text = "1 USDC = $1",
                                fontSize = 10.sp,
                                color = TrueTapTextSecondary
                            )
                        }
                    }
                }
            }
        }
        
            Spacer(modifier = Modifier.height(12.dp))
            
            // Details Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDetailsExpanded = !isDetailsExpanded },
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                    Icon(
                        imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Details",
                        tint = TrueTapTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                if (isDetailsExpanded) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        DetailRow("Rate", "1 SOL = $103.37")
                        DetailRow("Network Fee", "0.00025 SOL")
                        DetailRow("Route", "Raydium → Orca")
                        DetailRow("Slippage", currentSlippage)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Swap Button
            Button(
                onClick = { 
                    showSwapConfirmationModal = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = fromAmount.isNotEmpty() && fromAmount != "0.00" && toAmount.isNotEmpty() && toAmount != "0.00"
            ) {
                Text(
                    text = "Review Swap",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Bottom Navigation Bar
        BottomNavigationBar(
            selectedTab = BottomNavItem.SWAP,
            onTabSelected = { tab ->
                when (tab) {
                    BottomNavItem.HOME -> onNavigateToHome()
                    BottomNavItem.SWAP -> { /* Already on swap */ }
                    BottomNavItem.NFTS -> onNavigateToNFTs()
                    BottomNavItem.CONTACTS -> onNavigateToContacts()
                    BottomNavItem.SETTINGS -> onNavigateToSettings()
                }
            }
        )
    }
    
    // Transaction Speed Modal
    if (showTransactionSpeedModal) {
        TransactionSpeedModal(
            currentSpeed = transactionSpeed,
            onSpeedSelected = { speed ->
                transactionSpeed = speed
                currentSlippage = when (speed) {
                    "Fast" -> "0.5%"
                    "Moderate" -> "0.3%"
                    "Eco" -> "0.1%"
                    else -> "0.1%"
                }
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
            currentCurrency = selectedTokenFrom,
            onCurrencySelected = { currency ->
                selectedTokenFrom = currency
                showCurrencyFromModal = false
            },
            onDismiss = { showCurrencyFromModal = false }
        )
    }
    
    // Currency To Modal
    if (showCurrencyToModal) {
        CurrencySelectionModal(
            currentCurrency = selectedTokenTo,
            onCurrencySelected = { currency ->
                selectedTokenTo = currency
                showCurrencyToModal = false
            },
            onDismiss = { showCurrencyToModal = false }
        )
    }
    
    // Swap Confirmation Modal
    if (showSwapConfirmationModal) {
        SwapConfirmationModal(
            fromToken = selectedTokenFrom,
            toToken = selectedTokenTo,
            fromAmount = fromAmount,
            toAmount = toAmount,
            transactionSpeed = transactionSpeed,
            currentSlippage = currentSlippage,
            swapState = swapState,
            onConfirmSwap = {
                // Start swap processing
                swapState = SwapState.Processing
                // Simulate swap process (in real app this would be blockchain transaction)
                coroutineScope.launch {
                    delay(3000) // Simulate processing time
                    swapState = if (kotlin.random.Random.nextBoolean()) {
                        SwapState.Success
                    } else {
                        SwapState.Failed
                    }
                    delay(2000) // Show result
                    swapState = SwapState.None
                    showSwapConfirmationModal = false
                }
            },
            onDismiss = { 
                if (swapState == SwapState.None || swapState == SwapState.Success || swapState == SwapState.Failed) {
                    showSwapConfirmationModal = false
                    swapState = SwapState.None
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
            
            // Currency Options (as requested by user)
            val currencies = listOf(
                Pair("SOL", "Solana"),
                Pair("USDC", "USD Coin"),
                Pair("ORCA", "Orca"),
                Pair("BONK", "Bonk"),
                Pair("JUP", "Jupiter"),
                Pair("RAY", "Raydium")
            )
            
            currencies.forEach { (symbol, name) ->
                CurrencyOptionCard(
                    symbol = symbol,
                    name = name,
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
                Text(
                    text = name,
                    fontSize = 14.sp,
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

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TrueTapTextSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
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
                SwapState.None -> SwapConfirmationContent(
                    fromToken, toToken, fromAmount, toAmount, transactionSpeed, currentSlippage, onConfirmSwap
                )
                SwapState.Processing -> SwapProcessingContent()
                SwapState.Success -> SwapSuccessContent(fromToken, toToken, fromAmount, toAmount)
                SwapState.Failed -> SwapFailureContent(onRetry = onConfirmSwap)
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
            
            DetailRow("Rate", "1 SOL = $103.37")
            DetailRow("Network Fee", "0.00025 SOL")
            DetailRow("Speed", transactionSpeed)
            DetailRow("Slippage", currentSlippage)
            DetailRow("Route", "Raydium → Orca")
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

@Composable
private fun BottomNavigationBar(
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TrueTapContainer,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem.values().forEach { item ->
                BottomNavButton(
                    item = item,
                    isSelected = selectedTab == item,
                    onClick = { onTabSelected(item) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavButton(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with filled orange when selected
        Icon(
            imageVector = when (item) {
                BottomNavItem.HOME -> Icons.Default.Home
                BottomNavItem.SWAP -> Icons.Default.SwapHoriz
                BottomNavItem.NFTS -> Icons.Default.Image
                BottomNavItem.CONTACTS -> Icons.Default.People
                BottomNavItem.SETTINGS -> Icons.Default.Settings
            },
            contentDescription = item.title,
            tint = if (isSelected) TrueTapPrimary else TrueTapTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = item.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) TrueTapPrimary else TrueTapTextSecondary
        )
    }
}