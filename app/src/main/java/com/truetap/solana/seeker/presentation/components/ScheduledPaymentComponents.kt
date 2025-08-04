package com.truetap.solana.seeker.presentation.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.truetap.solana.seeker.domain.model.*
import com.truetap.solana.seeker.ui.theme.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Supporting components for Scheduled Payment functionality
 * Reusable UI components with TrueTap design system
 */
object ScheduledPaymentComponents {

    @Composable
    fun ContactSelector(
        selectedContact: Contact?,
        recipientAddress: String,
        onContactClick: () -> Unit,
        onAddressChange: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier) {
            Text(
                text = "Recipient",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedContact != null) {
                // Selected Contact Display
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContactClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = TrueTapContainer,
                    border = BorderStroke(1.dp, TrueTapPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ContactAvatar(
                            contact = selectedContact,
                            size = 40.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedContact.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrueTapTextPrimary
                            )
                            Text(
                                text = "${selectedContact.walletAddress.take(8)}...",
                                fontSize = 14.sp,
                                color = TrueTapTextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Contact",
                            tint = TrueTapPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // Manual Address Input
                Column {
                    OutlinedTextField(
                        value = recipientAddress,
                        onValueChange = onAddressChange,
                        placeholder = { Text("Enter wallet address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrueTapPrimary,
                            unfocusedBorderColor = Color(0xFFE5E5E5),
                            focusedContainerColor = TrueTapContainer,
                            unfocusedContainerColor = TrueTapContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = onContactClick) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Select Contact",
                                    tint = TrueTapPrimary
                                )
                            }
                        }
                    )

                    TextButton(
                        onClick = onContactClick,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Select from contacts",
                            color = TrueTapPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ContactAvatar(
        contact: Contact,
        size: androidx.compose.ui.unit.Dp,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(TrueTapPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.initials,
                color = Color.White,
                fontSize = (size.value * 0.4).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ContactSelectorModal(
        contacts: List<Contact>,
        searchQuery: String,
        onSearchChange: (String) -> Unit,
        onContactSelect: (Contact) -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                color = TrueTapContainer
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
                        Text(
                            text = "Select Contact",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TrueTapTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search contacts...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TrueTapTextSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrueTapPrimary,
                            unfocusedBorderColor = Color(0xFFE5E5E5)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contacts List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(contacts) { contact ->
                            ContactItem(
                                contact = contact,
                                onClick = { onContactSelect(contact) }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ContactItem(
        contact: Contact,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            color = TrueTapBackground,
            border = BorderStroke(1.dp, Color(0xFFE5E5E5))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactAvatar(
                    contact = contact,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary
                        )
                        if (contact.isFavorite) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = TrueTapPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${contact.walletAddress.take(12)}...",
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )

                    if (contact.tags.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(contact.tags.take(2)) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TrueTapPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 10.sp,
                                        color = TrueTapPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select",
                    tint = TrueTapTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AmountInput(
        amount: String,
        selectedToken: String,
        tokenBalances: Map<String, BigDecimal>,
        onAmountChange: (String) -> Unit,
        onTokenChange: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier) {
            Text(
                text = "Amount",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Token Selection
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(listOf("SOL", "USDC", "BONK")) { token ->
                    TokenChip(
                        token = token,
                        isSelected = selectedToken == token,
                        balance = tokenBalances[token] ?: BigDecimal.ZERO,
                        onClick = { onTokenChange(token) }
                    )
                }
            }

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
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
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    Text(
                        text = selectedToken,
                        color = TrueTapPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )

            Text(
                text = "Available: ${tokenBalances[selectedToken]?.toPlainString() ?: "0"} $selectedToken",
                fontSize = 12.sp,
                color = TrueTapTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    @Composable
    private fun TokenChip(
        token: String,
        isSelected: Boolean,
        balance: BigDecimal,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) TrueTapPrimary else TrueTapContainer,
            border = BorderStroke(
                width = 1.dp,
                color = if (isSelected) TrueTapPrimary else Color(0xFFE5E5E5)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = token,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else TrueTapTextPrimary
                )
                Text(
                    text = balance.toPlainString(),
                    fontSize = 10.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TrueTapTextSecondary
                )
            }
        }
    }

    @Composable
    fun DateTimePicker(
        label: String,
        selectedDateTime: LocalDateTime?,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        isDatePicker: Boolean = true
    ) {
        Column(modifier = modifier) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                shape = RoundedCornerShape(12.dp),
                color = TrueTapContainer,
                border = BorderStroke(1.dp, Color(0xFFE5E5E5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (selectedDateTime != null) {
                                if (isDatePicker) {
                                    selectedDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                } else {
                                    selectedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                }
                            } else {
                                if (isDatePicker) "Select date" else "Select time"
                            },
                            fontSize = 16.sp,
                            color = if (selectedDateTime != null) TrueTapTextPrimary else TrueTapTextSecondary
                        )
                    }

                    Icon(
                        imageVector = if (isDatePicker) Icons.Default.CalendarToday else Icons.Default.AccessTime,
                        contentDescription = label,
                        tint = TrueTapPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun RepeatOptions(
        selectedInterval: RepeatInterval,
        maxExecutions: String,
        onIntervalChange: (RepeatInterval) -> Unit,
        onMaxExecutionsChange: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier) {
            Text(
                text = "Repeat",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Repeat Interval Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(RepeatInterval.values()) { interval ->
                    RepeatIntervalChip(
                        interval = interval,
                        isSelected = selectedInterval == interval,
                        onClick = { onIntervalChange(interval) }
                    )
                }
            }

            // Max Executions Input (only for repeating payments)
            if (selectedInterval != RepeatInterval.NONE) {
                OutlinedTextField(
                    value = maxExecutions,
                    onValueChange = onMaxExecutionsChange,
                    label = { Text("Number of payments") },
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
                    text = getRepeatDescription(selectedInterval, maxExecutions),
                    fontSize = 12.sp,
                    color = TrueTapTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    @Composable
    private fun RepeatIntervalChip(
        interval: RepeatInterval,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) TrueTapPrimary else TrueTapContainer,
            border = BorderStroke(
                width = 1.dp,
                color = if (isSelected) TrueTapPrimary else Color(0xFFE5E5E5)
            )
        ) {
            Text(
                text = interval.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else TrueTapTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    @Composable
    fun ScheduledPaymentCard(
        payment: ScheduledPayment,
        onCancel: () -> Unit,
        onExecuteNow: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header with status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = payment.recipientName ?: "${payment.recipientAddress.take(8)}...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary
                    )

                    PaymentStatusChip(status = payment.status)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Amount",
                            fontSize = 12.sp,
                            color = TrueTapTextSecondary
                        )
                        Text(
                            text = "${payment.amount} ${payment.token}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Next Payment",
                            fontSize = 12.sp,
                            color = TrueTapTextSecondary
                        )
                        Text(
                            text = payment.nextExecutionDate.format(
                                DateTimeFormatter.ofPattern("MMM dd, hh:mm a")
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TrueTapTextPrimary
                        )
                    }
                }

                if (payment.repeatInterval != RepeatInterval.NONE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${payment.repeatInterval.displayName} • ${payment.currentExecutions}/${payment.maxExecutions ?: "∞"}",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary
                        )
                    }
                }

                payment.memo?.let { memo ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = memo,
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TrueTapError
                        ),
                        border = BorderStroke(1.dp, TrueTapError)
                    ) {
                        Text("Cancel")
                    }

                    if (payment.status == PaymentStatus.PENDING) {
                        Button(
                            onClick = onExecuteNow,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TrueTapPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Execute Now")
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PaymentStatusChip(
        status: PaymentStatus,
        modifier: Modifier = Modifier
    ) {
        val (backgroundColor, textColor) = when (status) {
            PaymentStatus.PENDING -> Color(0xFFFFF3CD) to Color(0xFF856404)
            PaymentStatus.COMPLETED -> Color(0xFFD4EDDA) to Color(0xFF155724)
            PaymentStatus.FAILED -> Color(0xFFF8D7DA) to Color(0xFF721C24)
            PaymentStatus.CANCELLED -> Color(0xFFE2E3E5) to Color(0xFF383D41)
        }

        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor
        ) {
            Text(
                text = status.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    @Composable
    fun PaymentScheduleDialog(
        uiState: ScheduledPaymentUiState,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Confirm Scheduled Payment",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Please review your scheduled payment details:",
                        color = TrueTapTextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    PaymentSummaryRow("Recipient", uiState.selectedContact?.name ?: "${uiState.recipientAddress.take(8)}...")
                    PaymentSummaryRow("Amount", "${uiState.amount} ${uiState.selectedToken}")
                    
                    val startDateTime = combineDateTime(uiState.selectedDate, uiState.selectedTime)
                    PaymentSummaryRow(
                        "Start Date", 
                        startDateTime?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) ?: "Not set"
                    )
                    
                    PaymentSummaryRow("Frequency", uiState.repeatInterval.displayName)
                    
                    if (uiState.repeatInterval != RepeatInterval.NONE) {
                        PaymentSummaryRow("Payments", uiState.maxExecutions)
                    }
                    
                    if (uiState.memo.isNotEmpty()) {
                        PaymentSummaryRow("Memo", uiState.memo)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
                ) {
                    Text("Schedule Payment", color = Color.White)
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
    private fun PaymentSummaryRow(
        label: String,
        value: String,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label:",
                fontSize = 14.sp,
                color = TrueTapTextSecondary
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TrueTapTextPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }

    private fun combineDateTime(date: LocalDateTime?, time: LocalDateTime?): LocalDateTime? {
        return if (date != null && time != null) {
            date.withHour(time.hour).withMinute(time.minute)
        } else null
    }

    private fun getRepeatDescription(interval: RepeatInterval, maxExecutions: String): String {
        val maxExec = maxExecutions.toIntOrNull() ?: 0
        return when (interval) {
            RepeatInterval.DAILY -> "$maxExec daily payments"
            RepeatInterval.WEEKLY -> "$maxExec weekly payments"
            RepeatInterval.MONTHLY -> "$maxExec monthly payments"
            RepeatInterval.NONE -> "One-time payment"
        }
    }
}