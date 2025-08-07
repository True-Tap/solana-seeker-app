package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.data.models.Contact
import com.truetap.solana.seeker.data.models.Wallet
import com.truetap.solana.seeker.data.models.WalletType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.truetap.solana.seeker.domain.model.RepeatInterval
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPaymentScreen(
    onNavigateBack: () -> Unit,
    recipientAddress: String? = null,
    modifier: Modifier = Modifier,
    viewModel: SendPaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    
    val haptic = LocalHapticFeedback.current
    
    // Initialize with passed recipient address
    LaunchedEffect(recipientAddress) {
        recipientAddress?.let { viewModel.updateRecipientAddress(it) }
    }
    
    
    // Validation from ViewModel
    val canSend = uiState.isFormValid && !uiState.isLoading
    
    // Send function
    val sendPayment = {
        showConfirmDialog = false
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.sendPayment()
    }
    
    // Handle payment result
    LaunchedEffect(uiState.paymentResult) {
        uiState.paymentResult?.let { result ->
            if (result.success) {
                delay(2000)
                onNavigateBack() // Navigate back after success
            }
        }
    }
    
    // Handle schedule result
    LaunchedEffect(uiState.scheduleResult) {
        uiState.scheduleResult?.let { result ->
            if (result.success) {
                delay(2000)
                onNavigateBack() // Navigate back after successful scheduling
            }
        }
    }
    
    // Confirm Dialog
    if (showConfirmDialog) {
        ConfirmPaymentDialog(
            amount = uiState.amount,
            token = uiState.selectedToken,
            recipient = uiState.selectedContact?.name ?: "${uiState.recipientAddress.take(8)}...${uiState.recipientAddress.takeLast(4)}",
            message = uiState.memo,
            onConfirm = sendPayment,
            onDismiss = { showConfirmDialog = false }
        )
    }
    
    // Schedule Dialog
    if (showScheduleDialog) {
        SchedulePaymentDialog(
            amount = uiState.amount,
            token = uiState.selectedToken,
            recipient = uiState.selectedContact?.name ?: "${uiState.recipientAddress.take(8)}...${uiState.recipientAddress.takeLast(4)}",
            recipientAddress = uiState.recipientAddress,
            message = uiState.memo,
            onSchedule = { startDate, recurrence, maxExecutions ->
                viewModel.schedulePayment(startDate, recurrence, maxExecutions)
                showScheduleDialog = false
            },
            onDismiss = { showScheduleDialog = false }
        )
    }
    
    // Success Dialogs
    uiState.paymentResult?.let { result ->
        if (result.success) {
            PaymentSuccessDialog(
                amount = uiState.amount,
                token = uiState.selectedToken,
                recipient = uiState.selectedContact?.name ?: "${uiState.recipientAddress.take(8)}...${uiState.recipientAddress.takeLast(4)}"
            )
        }
    }
    
    uiState.scheduleResult?.let { result ->
        if (result.success) {
            ScheduleSuccessDialog(
                amount = uiState.amount,
                token = uiState.selectedToken,
                recipient = uiState.selectedContact?.name ?: "${uiState.recipientAddress.take(8)}...${uiState.recipientAddress.takeLast(4)}"
            )
        }
    }
    
    // Error Dialog
    uiState.errorMessage?.let { error ->
        PaymentErrorDialog(
            error = error,
            onDismiss = { viewModel.clearError() },
            onRetry = { viewModel.clearError() }
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TrueTapTextPrimary
                    )
                }
                
                Text(
                    text = "Send Payment",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount Section
            item {
                AmountSection(
                    amount = uiState.amount,
                    onAmountChange = viewModel::updateAmount,
                    selectedToken = uiState.selectedToken,
                    availableTokens = uiState.availableTokens.map { tokenInfo ->
                        Token(tokenInfo.symbol, tokenInfo.name, tokenInfo.balance, 
                              "$${String.format("%.2f", tokenInfo.balance * 100)}")
                    },
                    onTokenSelect = { token -> viewModel.selectToken(token.symbol) }
                )
            }
            
            // Recent Contacts
            if (uiState.recentContacts.isNotEmpty()) {
                item {
                    RecentContactsSection(
                        contacts = uiState.recentContacts,
                        onContactSelect = { contact ->
                            viewModel.selectContact(contact)
                        }
                    )
                }
            }
            
            // Recipient Section
            item {
                RecipientSection(
                    recipientWallet = uiState.recipientAddress,
                    onRecipientChange = viewModel::updateRecipientAddress,
                    selectedContact = uiState.selectedContact,
                    onContactPickerOpen = { showContactPicker = true }
                )
            }
            
            // Message Section
            item {
                MessageSection(
                    message = uiState.memo,
                    onMessageChange = viewModel::updateMemo
                )
            }
            
            // Send and Schedule Buttons
            item {
                SendAndScheduleButtons(
                    canSend = canSend,
                    isSending = uiState.isLoading,
                    onSend = { showConfirmDialog = true },
                    onSchedule = { showScheduleDialog = true }
                )
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

data class Token(
    val symbol: String,
    val name: String,
    val balance: Double,
    val usdValue: String
)

@Composable
private fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    selectedToken: String,
    availableTokens: List<Token>,
    onTokenSelect: (Token) -> Unit
) {
    Column {
        Text(
            text = "Amount",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapTextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            placeholder = { Text("0.00") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TrueTapPrimary,
                unfocusedBorderColor = Color(0xFFE5E5E5),
                focusedContainerColor = TrueTapContainer,
                unfocusedContainerColor = TrueTapContainer
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Token Selection
        Text(
            text = "Select Token",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TrueTapTextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableTokens) { token ->
                TokenChip(
                    token = token,
                    isSelected = token.symbol == selectedToken,
                    onSelect = { onTokenSelect(token) }
                )
            }
        }
    }
}

@Composable
private fun TokenChip(
    token: Token,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TrueTapPrimary else TrueTapContainer
        ),
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = token.symbol,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TrueTapTextPrimary
            )
            Text(
                text = String.format("%.4f", token.balance),
                fontSize = 12.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TrueTapTextSecondary
            )
            Text(
                text = token.usdValue,
                fontSize = 10.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.6f) else TrueTapTextInactive
            )
        }
    }
}

@Composable
private fun RecentContactsSection(
    contacts: List<Contact>,
    onContactSelect: (Contact) -> Unit
) {
    Column {
        Text(
            text = "Recent",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapTextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(contacts) { contact ->
                ContactAvatar(
                    contact = contact,
                    onSelect = { onContactSelect(contact) }
                )
            }
        }
    }
}

@Composable
private fun ContactAvatar(
    contact: Contact,
    onSelect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onSelect() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(TrueTapPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.first().uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = contact.name,
            fontSize = 12.sp,
            color = TrueTapTextSecondary
        )
    }
}

@Composable
private fun RecipientSection(
    recipientWallet: String,
    onRecipientChange: (String) -> Unit,
    selectedContact: Contact?,
    onContactPickerOpen: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recipient",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            
            TextButton(onClick = onContactPickerOpen) {
                Text(
                    text = "Contacts",
                    color = TrueTapPrimary,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (selectedContact != null) {
            // Selected Contact Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .background(TrueTapPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedContact.name.first().uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedContact.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary
                        )
                        Text(
                            text = "${selectedContact.walletAddress.take(8)}...${selectedContact.walletAddress.takeLast(4)}",
                            fontSize = 12.sp,
                            color = TrueTapTextSecondary
                        )
                    }
                    
                    IconButton(
                        onClick = { onRecipientChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TrueTapTextSecondary
                        )
                    }
                }
            }
        } else {
            // Wallet Address Input
            OutlinedTextField(
                value = recipientWallet,
                onValueChange = onRecipientChange,
                placeholder = { Text("Enter wallet address...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrueTapPrimary,
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                    focusedContainerColor = TrueTapContainer,
                    unfocusedContainerColor = TrueTapContainer
                ),
                trailingIcon = {
                    IconButton(onClick = { /* QR Scanner */ }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = TrueTapPrimary
                        )
                    }
                },
                singleLine = true
            )
        }
    }
}

@Composable
private fun MessageSection(
    message: String,
    onMessageChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Message (Optional)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapTextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            placeholder = { Text("Add a note...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TrueTapPrimary,
                unfocusedBorderColor = Color(0xFFE5E5E5),
                focusedContainerColor = TrueTapContainer,
                unfocusedContainerColor = TrueTapContainer
            ),
            maxLines = 3
        )
    }
}

@Composable
private fun SendAndScheduleButtons(
    canSend: Boolean,
    isSending: Boolean,
    onSend: () -> Unit,
    onSchedule: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (canSend) 1f else 0.95f,
        animationSpec = tween(150),
        label = "buttonScale"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Send Payment Button (Primary)
        Button(
            onClick = onSend,
            enabled = canSend && !isSending,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(scale),
            colors = ButtonDefaults.buttonColors(
                containerColor = TrueTapPrimary,
                disabledContainerColor = Color(0xFFE5E5E5)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sending...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send Payment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Schedule Button (Secondary)
        OutlinedButton(
            onClick = onSchedule,
            enabled = canSend && !isSending,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(scale),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TrueTapPrimary,
                disabledContentColor = Color(0xFFBBBBBB)
            ),
            border = BorderStroke(
                width = 2.dp,
                color = if (canSend && !isSending) TrueTapPrimary else Color(0xFFE5E5E5)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Schedule Payment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ConfirmPaymentDialog(
    amount: String,
    token: String,
    recipient: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirm Payment",
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "You are about to send:",
                    color = TrueTapTextSecondary,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "$amount $token",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "To: $recipient",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
                
                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Message: $message",
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TrueTapTextSecondary)
            }
        },
        containerColor = TrueTapContainer
    )
}

@Composable
private fun PaymentSuccessDialog(
    amount: String,
    token: String,
    recipient: String
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Payment Sent!",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$amount $token",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = "sent to $recipient",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = TrueTapContainer
    )
}

@Composable
private fun ScheduleSuccessDialog(
    amount: String,
    token: String,
    recipient: String
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Payment Scheduled!",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$amount $token",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = "scheduled for $recipient",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = TrueTapContainer
    )
}

@Composable
private fun PaymentErrorDialog(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment Failed",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
        },
        text = {
            Text(
                text = error,
                color = TrueTapTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
            ) {
                Text("Try Again")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TrueTapTextSecondary)
            }
        },
        containerColor = TrueTapContainer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulePaymentDialog(
    amount: String,
    token: String,
    recipient: String,
    recipientAddress: String,
    message: String,
    onSchedule: (startDate: LocalDateTime, recurrence: RepeatInterval, maxExecutions: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var selectedRecurrence by remember { mutableStateOf(RepeatInterval.NONE) }
    var maxExecutions by remember { mutableStateOf("1") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Date picker state
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000 // Today or future
            }
        }
    )
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = TrueTapPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TrueTapTextSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = TrueTapPrimary,
                    todayDateBorderColor = TrueTapPrimary
                )
            )
        }
    }
    
    // Main dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Schedule Payment",
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Payment summary
                Text(
                    text = "Scheduling payment of $amount $token to $recipient",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary
                )
                
                HorizontalDivider(color = TrueTapTextSecondary.copy(alpha = 0.2f))
                
                // Start Date selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Start Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedDate?.let { 
                                    "${it.monthValue}/${it.dayOfMonth}/${it.year}" 
                                } ?: "Select date",
                                fontSize = 16.sp,
                                color = if (selectedDate != null) TrueTapTextPrimary else TrueTapTextSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date",
                                tint = TrueTapPrimary
                            )
                        }
                    }
                }
                
                // Recurrence selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Repeat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RepeatInterval.values().forEach { interval ->
                            FilterChip(
                                selected = selectedRecurrence == interval,
                                onClick = { selectedRecurrence = interval },
                                label = {
                                    Text(
                                        text = interval.displayName,
                                        fontSize = 14.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TrueTapPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
                
                // Max executions (only for recurring payments)
                if (selectedRecurrence != RepeatInterval.NONE) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Number of payments",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary
                        )
                        
                        OutlinedTextField(
                            value = maxExecutions,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() }) {
                                    maxExecutions = it
                                }
                            },
                            placeholder = { Text("Enter number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TrueTapPrimary,
                                unfocusedBorderColor = TrueTapTextSecondary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canSchedule = selectedDate != null && 
                            (selectedRecurrence == RepeatInterval.NONE || maxExecutions.toIntOrNull()?.let { it > 0 } == true)
            
            Button(
                onClick = {
                    selectedDate?.let { startDate ->
                        val executions = if (selectedRecurrence == RepeatInterval.NONE) null 
                                       else maxExecutions.toIntOrNull()
                        onSchedule(startDate, selectedRecurrence, executions)
                    }
                },
                enabled = canSchedule,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrueTapPrimary,
                    disabledContainerColor = Color(0xFFE5E5E5)
                )
            ) {
                Text("Schedule Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TrueTapTextSecondary)
            }
        },
        containerColor = TrueTapContainer
    )
}