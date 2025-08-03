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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPaymentScreen(
    onNavigateBack: () -> Unit,
    recipientAddress: String? = null,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    var recipientWallet by remember { mutableStateOf(recipientAddress ?: "") }
    var selectedToken by remember { mutableStateOf("SOL") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var sendSuccess by remember { mutableStateOf(false) }
    var sendError by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var showContactPicker by remember { mutableStateOf(false) }
    
    val haptic = LocalHapticFeedback.current
    
    // Sample recent contacts
    val recentContacts = remember {
        listOf(
            Contact(
                id = "1",
                name = "Alice",
                initials = "A",
                avatar = null,
                seekerActive = true,
                wallets = listOf(
                    Wallet(
                        id = 1,
                        name = "Main",
                        address = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
                        type = WalletType.PERSONAL
                    )
                ),
                favorite = true,
                walletAddress = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
                lastTransactionAt = System.currentTimeMillis() - 86400000
            ),
            Contact(
                id = "2",
                name = "Bob",
                initials = "B",
                avatar = null,
                seekerActive = true,
                wallets = listOf(
                    Wallet(
                        id = 2,
                        name = "Personal",
                        address = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                        type = WalletType.PERSONAL
                    )
                ),
                favorite = true,
                walletAddress = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                lastTransactionAt = System.currentTimeMillis() - 259200000
            ),
            Contact(
                id = "3",
                name = "Charlie",
                initials = "C",
                avatar = null,
                seekerActive = false,
                wallets = listOf(
                    Wallet(
                        id = 3,
                        name = "Wallet",
                        address = "5dSHdvJBQ38YuuHdKHDHFLhMhLCvdV7xB5QH5Y8z9CXD",
                        type = WalletType.PERSONAL
                    )
                ),
                favorite = false,
                walletAddress = "5dSHdvJBQ38YuuHdKHDHFLhMhLCvdV7xB5QH5Y8z9CXD",
                lastTransactionAt = System.currentTimeMillis() - 604800000
            )
        )
    }
    
    // Available tokens
    val availableTokens = remember {
        listOf(
            Token("SOL", "Solana", 12.4567, "$2,456.78"),
            Token("USDC", "USD Coin", 250.50, "$250.50"),
            Token("BONK", "Bonk", 1000000.0, "$1,000.00"),
            Token("RAY", "Raydium", 45.75, "$915.00")
        )
    }
    
    // Validation
    val isValidAmount = amount.toDoubleOrNull()?.let { it > 0 } ?: false
    val isValidWallet = recipientWallet.length in 32..44
    val canSend = isValidAmount && isValidWallet && !isSending
    
    // Send function
    val sendPayment = {
        isSending = true
        showConfirmDialog = false
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    // Handle send simulation
    LaunchedEffect(isSending) {
        if (isSending) {
            delay(3000) // Simulate transaction time
            isSending = false
            if (Math.random() > 0.1) { // 90% success rate
                sendSuccess = true
                delay(2000)
                sendSuccess = false
                onNavigateBack() // Navigate back after success
            } else {
                sendError = "Transaction failed. Please try again."
                delay(3000)
                sendError = null
            }
        }
    }
    
    // Confirm Dialog
    if (showConfirmDialog) {
        ConfirmPaymentDialog(
            amount = amount,
            token = selectedToken,
            recipient = selectedContact?.name ?: "${recipientWallet.take(8)}...${recipientWallet.takeLast(4)}",
            message = message,
            onConfirm = sendPayment,
            onDismiss = { showConfirmDialog = false }
        )
    }
    
    // Success Dialog
    if (sendSuccess) {
        PaymentSuccessDialog(
            amount = amount,
            token = selectedToken,
            recipient = selectedContact?.name ?: "${recipientWallet.take(8)}...${recipientWallet.takeLast(4)}"
        )
    }
    
    // Error Dialog
    sendError?.let { error ->
        PaymentErrorDialog(
            error = error,
            onDismiss = { sendError = null },
            onRetry = { sendError = null }
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
                    amount = amount,
                    onAmountChange = { amount = it },
                    selectedToken = selectedToken,
                    availableTokens = availableTokens,
                    onTokenSelect = { selectedToken = it.symbol }
                )
            }
            
            // Recent Contacts
            if (recentContacts.isNotEmpty()) {
                item {
                    RecentContactsSection(
                        contacts = recentContacts,
                        onContactSelect = { contact ->
                            selectedContact = contact
                            recipientWallet = contact.walletAddress
                        }
                    )
                }
            }
            
            // Recipient Section
            item {
                RecipientSection(
                    recipientWallet = recipientWallet,
                    onRecipientChange = { 
                        recipientWallet = it
                        selectedContact = null
                    },
                    selectedContact = selectedContact,
                    onContactPickerOpen = { showContactPicker = true }
                )
            }
            
            // Message Section
            item {
                MessageSection(
                    message = message,
                    onMessageChange = { message = it }
                )
            }
            
            // Send Button
            item {
                SendButton(
                    canSend = canSend,
                    isSending = isSending,
                    onSend = { showConfirmDialog = true }
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
private fun SendButton(
    canSend: Boolean,
    isSending: Boolean,
    onSend: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (canSend) 1f else 0.95f,
        animationSpec = tween(150),
        label = "sendButtonScale"
    )
    
    Button(
        onClick = onSend,
        enabled = canSend,
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