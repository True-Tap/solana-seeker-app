package com.truetap.solana.seeker.presentation.screens.scheduled

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.domain.model.Contact
import com.truetap.solana.seeker.domain.model.RepeatInterval
import com.truetap.solana.seeker.domain.model.ScheduledPayment
import com.truetap.solana.seeker.presentation.components.ScheduledPaymentComponents
import com.truetap.solana.seeker.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Scheduled Payment Screen - Main composable for scheduling payments
 * Provides interface for creating, viewing and managing scheduled payments
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledPaymentScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduledPaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Success Dialog
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = {
                Text(
                    text = "Payment Scheduled!",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Text(
                    text = "Your scheduled payment has been created successfully.",
                    color = TrueTapTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSuccessDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = TrueTapContainer
        )
    }

    // Error Dialog
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = {
                Text(
                    text = "Error",
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
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapError)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = TrueTapContainer
        )
    }

    // Payment Schedule Confirmation Dialog
    if (uiState.showPaymentScheduleDialog) {
        ScheduledPaymentComponents.PaymentScheduleDialog(
            uiState = uiState,
            onConfirm = { viewModel.schedulePayment() },
            onDismiss = { viewModel.hidePaymentScheduleDialog() }
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
                    text = "Scheduled Payments",
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
            // Create New Payment Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Create New Scheduled Payment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary
                        )

                        // Contact Selector
                        ScheduledPaymentComponents.ContactSelector(
                            selectedContact = uiState.selectedContact,
                            recipientAddress = uiState.recipientAddress,
                            onContactClick = { viewModel.showContactSelector() },
                            onAddressChange = viewModel::setRecipientAddress
                        )

                        // Amount Input
                        ScheduledPaymentComponents.AmountInput(
                            amount = uiState.amount,
                            selectedToken = uiState.selectedToken,
                            tokenBalances = uiState.tokenBalances,
                            onAmountChange = viewModel::setAmount,
                            onTokenChange = viewModel::setSelectedToken
                        )

                        // Date and Time Pickers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ScheduledPaymentComponents.DateTimePicker(
                                label = "Start Date",
                                selectedDateTime = uiState.selectedDate,
                                onClick = { viewModel.showDatePicker() },
                                modifier = Modifier.weight(1f),
                                isDatePicker = true
                            )

                            ScheduledPaymentComponents.DateTimePicker(
                                label = "Start Time",
                                selectedDateTime = uiState.selectedTime,
                                onClick = { viewModel.showTimePicker() },
                                modifier = Modifier.weight(1f),
                                isDatePicker = false
                            )
                        }

                        // Repeat Options
                        ScheduledPaymentComponents.RepeatOptions(
                            selectedInterval = uiState.repeatInterval,
                            maxExecutions = uiState.maxExecutions,
                            onIntervalChange = viewModel::setRepeatInterval,
                            onMaxExecutionsChange = viewModel::setMaxExecutions
                        )

                        // Memo Input
                        OutlinedTextField(
                            value = uiState.memo,
                            onValueChange = viewModel::setMemo,
                            label = { Text("Memo (Optional)") },
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

                        // Schedule Button
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.showPaymentScheduleDialog()
                            },
                            enabled = !uiState.isLoading && 
                                     uiState.recipientAddress.isNotEmpty() && 
                                     uiState.amount.isNotEmpty() &&
                                     uiState.selectedDate != null &&
                                     uiState.selectedTime != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TrueTapPrimary,
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
                                text = if (uiState.isLoading) "Scheduling..." else "Review & Schedule",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Active Scheduled Payments Section
            if (uiState.activeScheduledPayments.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Scheduled Payments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.activeScheduledPayments) { payment ->
                    ScheduledPaymentComponents.ScheduledPaymentCard(
                        payment = payment,
                        onCancel = { viewModel.cancelScheduledPayment(payment.id) },
                        onExecuteNow = { viewModel.executeScheduledPayment(payment.id) }
                    )
                }
            }
        }
    }

    // Contact Selector Modal
    if (uiState.showContactSelector) {
        ScheduledPaymentComponents.ContactSelectorModal(
            contacts = uiState.filteredContacts,
            searchQuery = uiState.searchQuery,
            onSearchChange = viewModel::setSearchQuery,
            onContactSelect = viewModel::selectContact,
            onDismiss = { viewModel.hideContactSelector() }
        )
    }

    // Date Picker
    if (uiState.showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.setSelectedDate(date)
            },
            onDismiss = { viewModel.hideDatePicker() }
        )
    }

    // Time Picker
    if (uiState.showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                viewModel.setSelectedTime(time)
            },
            onDismiss = { viewModel.hideTimePicker() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = java.time.Instant.ofEpochMilli(millis)
                        val date = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK", color = TrueTapPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TrueTapTextSecondary)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = TrueTapContainer,
                selectedDayContainerColor = TrueTapPrimary,
                todayContentColor = TrueTapPrimary,
                todayDateBorderColor = TrueTapPrimary
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Time",
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    containerColor = TrueTapContainer,
                    selectorColor = TrueTapPrimary,
                    periodSelectorSelectedContainerColor = TrueTapPrimary,
                    timeSelectorSelectedContainerColor = TrueTapPrimary
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val now = LocalDateTime.now()
                    val selectedTime = now
                        .withHour(timePickerState.hour)
                        .withMinute(timePickerState.minute)
                    onTimeSelected(selectedTime)
                }
            ) {
                Text("OK", color = TrueTapPrimary)
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