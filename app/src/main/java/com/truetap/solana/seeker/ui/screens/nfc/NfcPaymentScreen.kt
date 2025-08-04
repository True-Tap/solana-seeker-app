package com.truetap.solana.seeker.ui.screens.nfc

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.data.models.*
import com.truetap.solana.seeker.ui.components.*
import com.truetap.solana.seeker.ui.theme.*

/**
 * NFC Payment Screen - Jetpack Compose
 * Main screen for NFC Tap to Pay functionality
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcPaymentScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NfcViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // NFC reader mode setup
    DisposableEffect(uiState.role) {
        // TODO: Set up NFC reader mode based on role
        /*
        val activity = context as? Activity
        activity?.let {
            when (uiState.role) {
                NfcRole.SENDER -> {
                    // Enable NFC writer mode
                    NfcAdapter.getDefaultAdapter(context)?.let { nfcAdapter ->
                        if (nfcAdapter.isEnabled) {
                            // Set up write mode
                        }
                    }
                }
                NfcRole.RECEIVER -> {
                    // Enable NFC reader mode
                    NfcAdapter.getDefaultAdapter(context)?.let { nfcAdapter ->
                        if (nfcAdapter.isEnabled) {
                            // Set up read mode
                        }
                    }
                }
            }
        }
        */
        
        onDispose {
            // Clean up NFC mode
            /*
            val activity = context as? Activity
            activity?.let {
                NfcAdapter.getDefaultAdapter(context)?.let { nfcAdapter ->
                    // Disable NFC modes
                }
            }
            */
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NFC Pay",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TrueTapTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                // Settings/Info button could go here
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Role Toggle
            RoleToggle(
                currentRole = uiState.role,
                onRoleChanged = { viewModel.toggleRole() },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !uiState.isNfcEnabled -> {
                        NfcDisabledContent(
                            onEnableNfc = { viewModel.enableNfc() }
                        )
                    }
                    uiState.showSuccessAnimation -> {
                        TappyCharacter(
                            role = uiState.role,
                            state = TappyState.SUCCESS,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    uiState.showErrorAnimation -> {
                        TappyCharacter(
                            role = uiState.role,
                            state = TappyState.ERROR,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    uiState.isProcessing -> {
                        TappyCharacter(
                            role = uiState.role,
                            state = TappyState.PROCESSING,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        when (uiState.role) {
                            NfcRole.SENDER -> {
                                SenderContent(
                                    amount = uiState.amount,
                                    onAmountChanged = viewModel::updateAmount,
                                    onTapToPay = { viewModel.processNfcTag() }
                                )
                            }
                            NfcRole.RECEIVER -> {
                                ReceiverContent(
                                    receivedAmount = uiState.amount,
                                    senderAddress = uiState.recipient,
                                    onReadyToReceive = { viewModel.processNfcTag() }
                                )
                            }
                        }
                    }
                }
            }
            
            // Error Message
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapError.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TrueTapError,
                            modifier = Modifier.weight(1f)
                        )
                        
                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text("Dismiss", color = TrueTapError)
                        }
                    }
                }
            }
            
            // Reset Button (when transaction is complete)
            AnimatedVisibility(
                visible = uiState.transactionResult is TransactionResult.Success || 
                         uiState.transactionResult is TransactionResult.Error
            ) {
                Button(
                    onClick = { viewModel.resetTransaction() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "New Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleToggle(
    currentRole: NfcRole,
    onRoleChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Sender Button
            Button(
                onClick = { if (currentRole != NfcRole.SENDER) onRoleChanged() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentRole == NfcRole.SENDER) TrueTapPrimary else Color.Transparent,
                    contentColor = if (currentRole == NfcRole.SENDER) Color.White else TrueTapTextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (currentRole == NfcRole.SENDER) 2.dp else 0.dp
                )
            ) {
                Text(
                    text = "I'm Sending",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Receiver Button
            Button(
                onClick = { if (currentRole != NfcRole.RECEIVER) onRoleChanged() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentRole == NfcRole.RECEIVER) TrueTapPrimary else Color.Transparent,
                    contentColor = if (currentRole == NfcRole.RECEIVER) Color.White else TrueTapTextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (currentRole == NfcRole.RECEIVER) 2.dp else 0.dp
                )
            ) {
                Text(
                    text = "I'm Receiving",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SenderContent(
    amount: String,
    onAmountChanged: (String) -> Unit,
    onTapToPay: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tappy Character
        TappyCharacter(
            role = NfcRole.SENDER,
            state = TappyState.IDLE,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Amount Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Amount to Send",
                    style = MaterialTheme.typography.titleMedium,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    suffix = { Text("SOL", color = TrueTapTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrueTapPrimary,
                        unfocusedBorderColor = TrueTapTextInactive
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onTapToPay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDouble() > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Tap to Pay",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiverContent(
    receivedAmount: String,
    senderAddress: String,
    onReadyToReceive: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tappy Character
        TappyCharacter(
            role = NfcRole.RECEIVER,
            state = TappyState.IDLE,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Received Payment Display
        if (receivedAmount.isNotEmpty() && senderAddress.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TrueTapSuccess.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Payment Received!",
                        style = MaterialTheme.typography.titleMedium,
                        color = TrueTapSuccess,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "$receivedAmount SOL",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TrueTapTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "From: ${senderAddress.take(8)}...${senderAddress.takeLast(8)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrueTapTextSecondary
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ready to Receive",
                        style = MaterialTheme.typography.titleMedium,
                        color = TrueTapTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Hold your device near someone who wants to send you SOL",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrueTapTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onReadyToReceive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "I'm Ready",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NfcDisabledContent(
    onEnableNfc: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TrueTapError.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NFC Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = TrueTapError,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "NFC is required for Tap to Pay. Please enable NFC in your device settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onEnableNfc,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Open NFC Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}