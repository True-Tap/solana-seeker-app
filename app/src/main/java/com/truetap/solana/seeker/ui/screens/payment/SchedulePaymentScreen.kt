package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Schedule Payment Screen - Compose screen for scheduling recurring payments
 * Converted from React Native TSX to Kotlin Compose
 */

data class FrequencyOption(
    val key: String,
    val label: String,
    val icon: String
)

data class SchedulePaymentUiState(
    val recipientAddress: String = "",
    val amount: String = "",
    val memo: String = "",
    val selectedToken: String = "SOL",
    val selectedFrequency: String = "monthly",
    val maxExecutions: String = "12",
    val isLoading: Boolean = false,
    val walletConnected: Boolean = false,
    val tokenBalances: Map<String, Double> = mapOf(
        "SOL" to 0.0,
        "USDC" to 0.0,
        "BONK" to 0.0
    ),
    val showSuccessDialog: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePaymentScreen(
    onNavigateBack: () -> Unit,
    recipientAddress: String? = null,
    viewModel: SchedulePaymentViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val frequencyOptions = listOf(
        FrequencyOption("daily", "Daily", "📅"),
        FrequencyOption("weekly", "Weekly", "📅"),
        FrequencyOption("monthly", "Monthly", "🗓️")
    )
    
    // Initialize with passed recipient address
    LaunchedEffect(recipientAddress) {
        recipientAddress?.let { viewModel.setRecipientAddress(it) }
    }
    
    // Handle success dialog
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Schedule Created!",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Text(
                    text = "Successfully scheduled ${uiState.selectedFrequency} payments of ${uiState.amount} ${uiState.selectedToken} to ${uiState.recipientAddress.take(8)}... (${uiState.maxExecutions} payments)",
                    color = TrueTapTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrueTapPrimary
                    )
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    // Handle error dialog
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = {
                Text(
                    text = "Schedule Failed",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapError
                )
            },
            text = {
                Text(
                    text = error,
                    color = TrueTapTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearError() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrueTapError
                    )
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        // Header
        Surface(
            color = TrueTapContainer,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TrueTapTextPrimary
                    )
                }
                
                Text(
                    text = "Schedule Payment",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Recipient Address Input
            item {
                InputSection(
                    label = "Recipient Address",
                    content = {
                        OutlinedTextField(
                            value = uiState.recipientAddress,
                            onValueChange = viewModel::setRecipientAddress,
                            placeholder = { Text("Enter Solana wallet address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TrueTapPrimary,
                                unfocusedBorderColor = Color(0xFFE5E5E5),
                                focusedContainerColor = TrueTapContainer,
                                unfocusedContainerColor = TrueTapContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )
            }
            
            // Token Selection
            item {
                InputSection(
                    label = "Token",
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf("SOL", "USDC", "BONK").forEach { token ->
                                TokenButton(
                                    token = token,
                                    isSelected = uiState.selectedToken == token,
                                    balance = uiState.tokenBalances[token] ?: 0.0,
                                    onClick = { viewModel.setSelectedToken(token) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                )
            }
            
            // Amount Input
            item {
                InputSection(
                    label = "Amount per Payment (${uiState.selectedToken})",
                    content = {
                        Column {
                            OutlinedTextField(
                                value = uiState.amount,
                                onValueChange = viewModel::setAmount,
                                placeholder = { Text("0.00") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TrueTapPrimary,
                                    unfocusedBorderColor = Color(0xFFE5E5E5),
                                    focusedContainerColor = TrueTapContainer,
                                    unfocusedContainerColor = TrueTapContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Text(
                                text = "Available: ${String.format("%.4f", uiState.tokenBalances[uiState.selectedToken] ?: 0.0)} ${uiState.selectedToken}",
                                fontSize = 12.sp,
                                color = TrueTapTextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                )
            }
            
            // Frequency Selection
            item {
                InputSection(
                    label = "Payment Frequency",
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            frequencyOptions.forEach { freq ->
                                FrequencyButton(
                                    frequency = freq,
                                    isSelected = uiState.selectedFrequency == freq.key,
                                    onClick = { viewModel.setSelectedFrequency(freq.key) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                )
            }
            
            // Max Executions
            item {
                InputSection(
                    label = "Number of Payments",
                    content = {
                        Column {
                            OutlinedTextField(
                                value = uiState.maxExecutions,
                                onValueChange = viewModel::setMaxExecutions,
                                placeholder = { Text("12") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TrueTapPrimary,
                                    unfocusedBorderColor = Color(0xFFE5E5E5),
                                    focusedContainerColor = TrueTapContainer,
                                    unfocusedContainerColor = TrueTapContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Text(
                                text = getFrequencyDescription(uiState.selectedFrequency, uiState.maxExecutions),
                                fontSize = 12.sp,
                                color = TrueTapTextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                )
            }
            
            // Memo Input
            item {
                InputSection(
                    label = "Memo (Optional)",
                    content = {
                        OutlinedTextField(
                            value = uiState.memo,
                            onValueChange = viewModel::setMemo,
                            placeholder = { Text("Add a note...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TrueTapPrimary,
                                unfocusedBorderColor = Color(0xFFE5E5E5),
                                focusedContainerColor = TrueTapContainer,
                                unfocusedContainerColor = TrueTapContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )
            }
            
            // Schedule Button
            item {
                val isEnabled = uiState.recipientAddress.isNotEmpty() && 
                               uiState.amount.isNotEmpty() && 
                               !uiState.isLoading
                
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.schedulePayment()
                    },
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) TrueTapPrimary else TrueTapTextInactive,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = if (uiState.isLoading) 
                            "Creating Schedule..." 
                        else 
                            "Schedule ${uiState.selectedFrequency} payments",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InputSection(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TrueTapTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun TokenButton(
    token: String,
    isSelected: Boolean,
    balance: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) TrueTapPrimary else TrueTapContainer,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) TrueTapPrimary else Color(0xFFE5E5E5)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = token,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else TrueTapTextPrimary
            )
            Text(
                text = String.format("%.4f", balance),
                fontSize = 12.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TrueTapTextInactive,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun FrequencyButton(
    frequency: FrequencyOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) TrueTapPrimary else TrueTapContainer,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) TrueTapPrimary else Color(0xFFE5E5E5)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = frequency.icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = frequency.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else TrueTapTextPrimary
            )
        }
    }
}

private fun getFrequencyDescription(frequency: String, maxExecutions: String): String {
    val maxExec = maxExecutions.toIntOrNull() ?: 12
    return when (frequency) {
        "daily" -> "$maxExec daily payments"
        "weekly" -> "$maxExec weekly payments"
        "monthly" -> "$maxExec monthly payments"
        else -> "$maxExec payments"
    }
}