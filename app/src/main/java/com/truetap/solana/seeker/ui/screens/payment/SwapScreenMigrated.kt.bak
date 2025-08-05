package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.ui.components.*
import com.truetap.solana.seeker.ui.components.layouts.*
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.viewmodels.SwapViewModel
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.truetap.solana.seeker.viewmodels.SwapState

/**
 * SwapScreen migrated to use TrueTap Design System
 * 
 * Clean, intuitive swap interface with proper state management and consistent styling
 */

data class TokenOption(
    val symbol: String,
    val name: String,
    val balance: Double,
    val icon: ImageVector,
    val price: Double
)

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
    val swapUiState by swapViewModel.uiState.collectAsStateWithLifecycle()
    val walletState by walletViewModel.walletState.collectAsStateWithLifecycle()
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    // Available tokens
    val availableTokens = remember {
        listOf(
            TokenOption("SOL", "Solana", 2.5, Icons.Default.Circle, 103.37),
            TokenOption("USDC", "USD Coin", 1000.0, Icons.Default.AttachMoney, 1.0),
            TokenOption("RAY", "Raydium", 50.0, Icons.Default.Circle, 0.23),
            TokenOption("SRM", "Serum", 25.0, Icons.Default.Circle, 0.15)
        )
    }
    
    var inputAmount by remember { mutableStateOf("") }
    var outputAmount by remember { mutableStateOf("") }
    var selectedInputToken by remember { mutableStateOf(availableTokens[0]) }
    var selectedOutputToken by remember { mutableStateOf(availableTokens[1]) }
    var showTokenSelector by remember { mutableStateOf<String?>(null) }
    var showSlippageSettings by remember { mutableStateOf(false) }
    var slippageTolerance by remember { mutableStateOf(0.5) }
    
    // Calculate output amount when input changes
    LaunchedEffect(inputAmount, selectedInputToken, selectedOutputToken) {
        if (inputAmount.isNotEmpty()) {
            val input = inputAmount.toDoubleOrNull() ?: 0.0
            val rate = selectedInputToken.price / selectedOutputToken.price
            outputAmount = "%.6f".format(input * rate)
        } else {
            outputAmount = ""
        }
    }
    
    // Sync wallet address with SwapViewModel
    LaunchedEffect(walletState.account?.publicKey) {
        walletState.account?.publicKey?.let { address ->
            swapViewModel.updateWalletAddress(address)
        }
    }
    
    // Use standardized screen template
    TrueTapScreenTemplate(
        title = "Swap",
        subtitle = if (walletState.account != null) "Exchange your tokens" else "Connect wallet to swap",
        currentTab = BottomNavItem.SWAP,
        onNavigateToHome = onNavigateToHome,
        onNavigateToSwap = { /* Already on swap */ },
        onNavigateToNFTs = onNavigateToNFTs,
        onNavigateToContacts = onNavigateToContacts,
        onNavigateToSettings = onNavigateToSettings,
        actions = {
            // Slippage settings
            TrueTapButton(
                text = "Settings",
                onClick = { showSlippageSettings = true },
                style = TrueTapButtonStyle.ICON,
                icon = Icons.Default.Settings
            )
        },
        modifier = modifier
    ) {
        if (walletState.account == null) {
            // No wallet connected state
            item {
                NoWalletSwapState(
                    onConnectWallet = { walletViewModel.connectWallet() }
                )
            }
        } else {
            // Main swap interface
            item {
                SwapCard(
                    inputToken = selectedInputToken,
                    outputToken = selectedOutputToken,
                    inputAmount = inputAmount,
                    outputAmount = outputAmount,
                    onInputAmountChange = { inputAmount = it },
                    onSelectInputToken = { showTokenSelector = "input" },
                    onSelectOutputToken = { showTokenSelector = "output" },
                    onSwapTokens = {
                        val temp = selectedInputToken
                        selectedInputToken = selectedOutputToken
                        selectedOutputToken = temp
                        val tempAmount = inputAmount
                        inputAmount = outputAmount
                        outputAmount = tempAmount
                    }
                )
            }
            
            // Swap details
            item {
                SwapDetailsCard(
                    inputToken = selectedInputToken,
                    outputToken = selectedOutputToken,
                    inputAmount = inputAmount.toDoubleOrNull() ?: 0.0,
                    outputAmount = outputAmount.toDoubleOrNull() ?: 0.0,
                    slippageTolerance = slippageTolerance,
                    estimatedFee = 0.000025 // SOL
                )
            }
            
            // Recent swaps section
            item {
                TrueTapSectionHeader(
                    title = "Recent Swaps",
                    subtitle = "Your swap history"
                )
            }
            
            // Mock recent swaps
            val recentSwaps = remember {
                listOf(
                    Triple("SOL", "USDC", "2.5 → 258.43"),
                    Triple("USDC", "RAY", "100 → 434.78"),
                    Triple("RAY", "SOL", "50 → 0.115")
                )
            }
            
            if (recentSwaps.isNotEmpty()) {
                items(recentSwaps) { (from, to, amounts) ->
                    RecentSwapItem(
                        fromToken = from,
                        toToken = to,
                        amounts = amounts,
                        timestamp = "2 hours ago"
                    )
                }
            } else {
                item {
                    TrueTapEmptyState(
                        title = "No Recent Swaps",
                        description = "Your swap history will appear here",
                        icon = Icons.Default.SwapHoriz
                    )
                }
            }
            
            // Swap button
            item {
                SwapActionButton(
                    enabled = inputAmount.isNotEmpty() && inputAmount.toDoubleOrNull() != null && inputAmount.toDoubleOrNull()!! > 0,
                    isLoading = swapUiState.swapState == SwapState.PROCESSING,
                    onSwap = {
                        // Trigger swap
                        swapViewModel.executeSwap()
                    }
                )
            }
        }
    }
    
    // Token Selector Modal
    showTokenSelector?.let { selectorType ->
        TokenSelectorModal(
            tokens = availableTokens,
            selectedToken = if (selectorType == "input") selectedInputToken else selectedOutputToken,
            onTokenSelected = { token ->
                if (selectorType == "input") {
                    selectedInputToken = token
                } else {
                    selectedOutputToken = token
                }
                showTokenSelector = null
            },
            onDismiss = { showTokenSelector = null }
        )
    }
    
    // Slippage Settings Modal
    if (showSlippageSettings) {
        SlippageSettingsModal(
            currentSlippage = slippageTolerance,
            onSlippageChanged = { slippageTolerance = it },
            onDismiss = { showSlippageSettings = false }
        )
    }
    
    // Swap Confirmation Modal
    if (swapUiState.showSwapConfirmation) {
        SwapConfirmationModal(
            inputToken = selectedInputToken,
            outputToken = selectedOutputToken,
            inputAmount = inputAmount,
            outputAmount = outputAmount,
            slippageTolerance = slippageTolerance,
            swapState = swapUiState.swapState,
            onConfirmSwap = { swapViewModel.executeSwap() },
            onDismiss = { swapViewModel.showSwapConfirmation(false) }
        )
    }
}

@Composable
private fun NoWalletSwapState(onConnectWallet: () -> Unit) {
    TrueTapEmptyState(
        title = "Connect Your Wallet",
        description = "Connect a Solana wallet to start swapping tokens with the best rates.",
        icon = Icons.Default.AccountBalanceWallet,
        action = {
            TrueTapButton(
                text = "Connect Wallet",
                onClick = onConnectWallet,
                icon = Icons.Default.AccountBalanceWallet
            )
        }
    )
}

@Composable
private fun SwapCard(
    inputToken: TokenOption,
    outputToken: TokenOption,
    inputAmount: String,
    outputAmount: String,
    onInputAmountChange: (String) -> Unit,
    onSelectInputToken: () -> Unit,
    onSelectOutputToken: () -> Unit,
    onSwapTokens: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    TrueTapCard(style = TrueTapCardStyle.ELEVATED) {
        Column {
            // Input section
            SwapTokenSection(
                label = "From",
                token = inputToken,
                amount = inputAmount,
                onAmountChange = onInputAmountChange,
                onSelectToken = onSelectInputToken,
                isInput = true
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            
            // Swap button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TrueTapButton(
                    text = "Swap",
                    onClick = onSwapTokens,
                    style = TrueTapButtonStyle.ICON,
                    icon = Icons.Default.SwapVert
                )
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            
            // Output section
            SwapTokenSection(
                label = "To",
                token = outputToken,
                amount = outputAmount,
                onAmountChange = { /* Read-only */ },
                onSelectToken = onSelectOutputToken,
                isInput = false
            )
        }
    }
}

@Composable
private fun SwapTokenSection(
    label: String,
    token: TokenOption,
    amount: String,
    onAmountChange: (String) -> Unit,
    onSelectToken: () -> Unit,
    isInput: Boolean
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = getDynamicTypography(accessibility.largeButtonMode).labelLarge,
                color = dynamicColors.textSecondary
            )
            
            if (isInput) {
                Text(
                    text = "Balance: ${token.balance} ${token.symbol}",
                    style = getDynamicTypography(accessibility.largeButtonMode).bodySmall,
                    color = dynamicColors.textTertiary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Token selector
            TrueTapCard(
                style = TrueTapCardStyle.OUTLINED,
                onClick = onSelectToken
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.sm)
                ) {
                    Icon(
                        imageVector = token.icon,
                        contentDescription = token.symbol,
                        tint = dynamicColors.primary
                    )
                    
                    Text(
                        text = token.symbol,
                        style = getDynamicTypography(accessibility.largeButtonMode).titleMedium,
                        color = dynamicColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select token",
                        tint = dynamicColors.textSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(TrueTapSpacing.md))
            
            // Amount input
            OutlinedTextField(
                value = amount,
                onValueChange = if (isInput) onAmountChange else { },
                modifier = Modifier.weight(1f),
                placeholder = { Text("0.0") },
                readOnly = !isInput,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dynamicColors.primary,
                    focusedLabelColor = dynamicColors.primary,
                    cursorColor = dynamicColors.primary
                ),
                textStyle = getDynamicTypography(accessibility.largeButtonMode).headlineSmall.copy(
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            )
        }
        
        // USD value
        if (amount.isNotEmpty() && amount.toDoubleOrNull() != null) {
            val usdValue = (amount.toDoubleOrNull() ?: 0.0) * token.price
            Text(
                text = "≈ $%.2f".format(usdValue),
                style = getDynamicTypography(accessibility.largeButtonMode).bodySmall,
                color = dynamicColors.textTertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TrueTapSpacing.xs),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
private fun SwapDetailsCard(
    inputToken: TokenOption,
    outputToken: TokenOption,
    inputAmount: Double,
    outputAmount: Double,
    slippageTolerance: Double,
    estimatedFee: Double
) {
    if (inputAmount > 0 && outputAmount > 0) {
        TrueTapCard {
            Column {
                TrueTapSectionHeader(title = "Swap Details")
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
                
                SwapDetailRow(
                    label = "Exchange Rate",
                    value = "1 ${inputToken.symbol} = ${String.format("%.6f", outputAmount / inputAmount)} ${outputToken.symbol}"
                )
                
                SwapDetailRow(
                    label = "Slippage Tolerance",
                    value = "${slippageTolerance}%"
                )
                
                SwapDetailRow(
                    label = "Estimated Fee",
                    value = "$estimatedFee SOL"
                )
                
                SwapDetailRow(
                    label = "Minimum Received",
                    value = "${String.format("%.6f", outputAmount * (1 - slippageTolerance / 100))} ${outputToken.symbol}"
                )
            }
        }
    }
}

@Composable
private fun SwapDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = getDynamicTypography().bodyMedium,
            color = getDynamicColors().textSecondary
        )
        
        Text(
            text = value,
            style = getDynamicTypography().bodyMedium,
            color = getDynamicColors().textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RecentSwapItem(
    fromToken: String,
    toToken: String,
    amounts: String,
    timestamp: String
) {
    TrueTapListItem(
        title = "$fromToken → $toToken",
        subtitle = "$amounts • $timestamp",
        leadingIcon = Icons.Default.SwapHoriz,
        trailingContent = {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "View transaction",
                tint = getDynamicColors().textSecondary
            )
        },
        onClick = { /* Navigate to transaction details */ }
    )
}

@Composable
private fun SwapActionButton(
    enabled: Boolean,
    isLoading: Boolean,
    onSwap: () -> Unit
) {
    TrueTapButton(
        text = when {
            isLoading -> "Swapping..."
            enabled -> "Review Swap"
            else -> "Enter Amount"
        },
        onClick = onSwap,
        enabled = enabled && !isLoading,
        loading = isLoading,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TokenSelectorModal(
    tokens: List<TokenOption>,
    selectedToken: TokenOption,
    onTokenSelected: (TokenOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = getDynamicColors().surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TrueTapSpacing.lg)
        ) {
            TrueTapSectionHeader(title = "Select Token")
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            
            tokens.forEach { token ->
                TokenOptionItem(
                    token = token,
                    isSelected = token == selectedToken,
                    onClick = { onTokenSelected(token) }
                )
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.xl))
        }
    }
}

@Composable
private fun TokenOptionItem(
    token: TokenOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TrueTapListItem(
        title = token.name,
        subtitle = "Balance: ${token.balance} ${token.symbol}",
        leadingIcon = token.icon,
        onClick = onClick,
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = getDynamicColors().primary
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlippageSettingsModal(
    currentSlippage: Double,
    onSlippageChanged: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val slippageOptions = listOf(0.1, 0.5, 1.0, 3.0)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = getDynamicColors().surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TrueTapSpacing.lg)
        ) {
            TrueTapSectionHeader(
                title = "Slippage Tolerance",
                subtitle = "Your transaction will revert if the price changes unfavorably by more than this percentage."
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.sm)
            ) {
                slippageOptions.forEach { slippage ->
                    TrueTapButton(
                        text = "${slippage}%",
                        onClick = { onSlippageChanged(slippage) },
                        style = if (currentSlippage == slippage) TrueTapButtonStyle.PRIMARY else TrueTapButtonStyle.OUTLINE,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.xl))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwapConfirmationModal(
    inputToken: TokenOption,
    outputToken: TokenOption,
    inputAmount: String,
    outputAmount: String,
    slippageTolerance: Double,
    swapState: SwapState,
    onConfirmSwap: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = if (swapState != SwapState.PROCESSING) onDismiss else { },
        containerColor = getDynamicColors().surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TrueTapSpacing.lg)
        ) {
            when (swapState) {
                SwapState.PROCESSING -> {
                    SwapProcessingState()
                }
                SwapState.SUCCESS -> {
                    SwapSuccessState(
                        inputToken = inputToken,
                        outputToken = outputToken,
                        inputAmount = inputAmount,
                        outputAmount = outputAmount,
                        onDone = onDismiss
                    )
                }
                SwapState.ERROR -> {
                    SwapErrorState(
                        onRetry = onConfirmSwap,
                        onDismiss = onDismiss
                    )
                }
                else -> {
                    SwapConfirmationContent(
                        inputToken = inputToken,
                        outputToken = outputToken,
                        inputAmount = inputAmount,
                        outputAmount = outputAmount,
                        slippageTolerance = slippageTolerance,
                        onConfirm = onConfirmSwap,
                        onCancel = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun SwapConfirmationContent(
    inputToken: TokenOption,
    outputToken: TokenOption,
    inputAmount: String,
    outputAmount: String,
    slippageTolerance: Double,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column {
        TrueTapSectionHeader(title = "Confirm Swap")
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
        
        TrueTapCard(style = TrueTapCardStyle.OUTLINED) {
            Column {
                Text(
                    text = "You're swapping",
                    style = getDynamicTypography().bodyMedium,
                    color = getDynamicColors().textSecondary
                )
                
                Text(
                    text = "$inputAmount ${inputToken.symbol}",
                    style = getDynamicTypography().headlineSmall,
                    color = getDynamicColors().textPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.md))
                
                Text(
                    text = "To receive",
                    style = getDynamicTypography().bodyMedium,
                    color = getDynamicColors().textSecondary
                )
                
                Text(
                    text = "$outputAmount ${outputToken.symbol}",
                    style = getDynamicTypography().headlineSmall,
                    color = getDynamicColors().primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.md)
        ) {
            TrueTapButton(
                text = "Cancel",
                onClick = onCancel,
                style = TrueTapButtonStyle.OUTLINE,
                modifier = Modifier.weight(1f)
            )
            
            TrueTapButton(
                text = "Confirm Swap",
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.xl))
    }
}

@Composable
private fun SwapProcessingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TrueTapLoadingState(message = "Processing your swap...")
        
        Text(
            text = "Please don't close this window",
            style = getDynamicTypography().bodySmall,
            color = getDynamicColors().textTertiary
        )
    }
}

@Composable
private fun SwapSuccessState(
    inputToken: TokenOption,
    outputToken: TokenOption,
    inputAmount: String,
    outputAmount: String,
    onDone: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = getDynamicColors().success,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
        
        Text(
            text = "Swap Successful!",
            style = getDynamicTypography().headlineSmall,
            color = getDynamicColors().textPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
        
        Text(
            text = "Successfully swapped $inputAmount ${inputToken.symbol} for $outputAmount ${outputToken.symbol}",
            style = getDynamicTypography().bodyMedium,
            color = getDynamicColors().textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.xl))
        
        TrueTapButton(
            text = "Done",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
    }
}

@Composable
private fun SwapErrorState(
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = getDynamicColors().error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
        
        Text(
            text = "Swap Failed",
            style = getDynamicTypography().headlineSmall,
            color = getDynamicColors().textPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
        
        Text(
            text = "Your transaction failed. Please try again.",
            style = getDynamicTypography().bodyMedium,
            color = getDynamicColors().textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.xl))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.md)
        ) {
            TrueTapButton(
                text = "Cancel",
                onClick = onDismiss,
                style = TrueTapButtonStyle.OUTLINE,
                modifier = Modifier.weight(1f)
            )
            
            TrueTapButton(
                text = "Try Again",
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
    }
}