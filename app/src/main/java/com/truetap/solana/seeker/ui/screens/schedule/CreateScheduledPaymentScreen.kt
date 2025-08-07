/**
 * Create Scheduled Payment Screen - TrueTap
 * Full-featured screen for creating new scheduled payments
 */

package com.truetap.solana.seeker.ui.screens.schedule

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.data.models.Contact
import com.truetap.solana.seeker.domain.model.RepeatInterval
import com.truetap.solana.seeker.ui.screens.payment.SendPaymentViewModel
import com.truetap.solana.seeker.ui.screens.payment.SchedulePaymentDialog
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduledPaymentScreen(
    onNavigateBack: () -> Unit,
    recipientAddress: String? = null,
    modifier: Modifier = Modifier,
    viewModel: SendPaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }
    
    // Initialize with passed recipient address
    LaunchedEffect(recipientAddress) {
        recipientAddress?.let { viewModel.updateRecipientAddress(it) }
    }
    
    // Handle schedule result with success feedback
    LaunchedEffect(uiState.scheduleResult) {
        uiState.scheduleResult?.let { result ->
            if (result.success) {
                // Show success for 3 seconds then navigate back
                delay(3000)
                onNavigateBack()
            }
        }
    }
    
    // Validation - form needs to be valid to schedule
    val canSchedule = uiState.isFormValid && !uiState.isLoading
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        // Add status bar spacing
        Spacer(modifier = Modifier.height(28.dp))
        // Header
        CreateScheduleHeader(
            onNavigateBack = onNavigateBack
        )
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Instructions
                InstructionsCard()
            }
            
            item {
                // Amount Section (reused from SendPaymentScreen)
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
            
            item {
                // Recipient Section (reused from SendPaymentScreen)
                RecipientSection(
                    recipientWallet = uiState.recipientAddress,
                    onRecipientChange = viewModel::updateRecipientAddress,
                    selectedContact = uiState.selectedContact,
                    onContactPickerOpen = { showContactPicker = true }
                )
            }
            
            item {
                // Message Section (reused from SendPaymentScreen)
                MessageSection(
                    message = uiState.memo,
                    onMessageChange = viewModel::updateMemo
                )
            }
            
            item {
                // Schedule Configuration Preview
                SchedulePreviewCard()
            }
            
            item {
                // Schedule Button
                ScheduleButton(
                    enabled = canSchedule,
                    isLoading = uiState.isLoading,
                    onClick = { showScheduleDialog = true }
                )
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Schedule Dialog (reused enhanced version from SendPaymentScreen)
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
    
    // Success Dialog
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
        ScheduleErrorDialog(
            error = error,
            onDismiss = { viewModel.clearError() }
        )
    }
}

@Composable
private fun CreateScheduleHeader(
    onNavigateBack: () -> Unit
) {
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
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
}

@Composable
private fun InstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapPrimary.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = TrueTapPrimary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Schedule Automatic Payments",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Set up payments to be sent automatically at specific times and intervals. Perfect for rent, subscriptions, or regular transfers.",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
            }
        }
    }
}

@Composable
private fun SchedulePreviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Schedule Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "After filling in the payment details above, tap the Schedule button to configure when and how often this payment should be sent.",
                fontSize = 14.sp,
                color = TrueTapTextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScheduleFeatureChip(
                    icon = Icons.Default.Today,
                    text = "Choose Date"
                )
                ScheduleFeatureChip(
                    icon = Icons.Default.Repeat,
                    text = "Set Frequency"
                )
                ScheduleFeatureChip(
                    icon = Icons.Default.Numbers,
                    text = "Repeat Count"
                )
            }
        }
    }
}

@Composable
private fun ScheduleFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TrueTapPrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TrueTapPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TrueTapPrimary
            )
        }
    }
}

@Composable
private fun ScheduleButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TrueTapPrimary,
            disabledContainerColor = Color(0xFFE5E5E5)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Scheduling...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (enabled) "Configure Schedule" else "Fill in payment details first",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✅ Payment Scheduled!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Your payment is set and ready to go",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TrueTapPrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$amount $token",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapPrimary
                        )
                        Text(
                            text = "will be sent to",
                            fontSize = 12.sp,
                            color = TrueTapTextSecondary
                        )
                        Text(
                            text = recipient,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "You can view and manage this scheduled payment in the Schedule screen.",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Returning to schedule list...",
                    fontSize = 12.sp,
                    color = TrueTapTextSecondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = TrueTapContainer
    )
}

@Composable
private fun ScheduleErrorDialog(
    error: String,
    onDismiss: () -> Unit
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
                    text = "Schedule Failed",
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
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
            ) {
                Text("Try Again")
            }
        },
        containerColor = TrueTapContainer
    )
}

// Reuse components from SendPaymentScreen
// These would typically be moved to shared components, but for now we'll import them

// Token data class (should be shared)
data class Token(
    val symbol: String,
    val name: String,
    val balance: Double,
    val usdValue: String
)

// Reused components from SendPaymentScreen (extracted for scheduled payments)

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