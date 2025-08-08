/**
 * Schedule Screen - TrueTap
 * Main screen for managing scheduled payments
 */

package com.truetap.solana.seeker.ui.screens.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.domain.model.PaymentStatus
import com.truetap.solana.seeker.domain.model.RepeatInterval
import com.truetap.solana.seeker.domain.model.ScheduledPayment
import com.truetap.solana.seeker.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreatePayment: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showFilterMenu by remember { mutableStateOf(false) }
    
    // Refresh scheduled payments when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshScheduledPayments()
    }
    
    // Error handling
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Here you could show a snackbar or other error UI
            // For now, just clear the error after showing it
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        // Add status bar spacing
        Spacer(modifier = Modifier.height(28.dp))
        // Header
        ScheduleHeader(
            onNavigateBack = onNavigateBack,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            selectedFilter = uiState.selectedFilter,
            onFilterClick = { showFilterMenu = true },
            onFilterSelect = { filter ->
                viewModel.updateStatusFilter(filter)
                showFilterMenu = false
            },
            showFilterMenu = showFilterMenu,
            onDismissFilterMenu = { showFilterMenu = false }
        )
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.filteredPayments.isEmpty() -> {
                    EmptyState(
                        hasScheduledPayments = uiState.scheduledPayments.isNotEmpty(),
                        searchQuery = uiState.searchQuery,
                        onCreatePayment = onNavigateToCreatePayment,
                        onClearSearch = { viewModel.updateSearchQuery("") }
                    )
                }
                else -> {
                    ScheduledPaymentsList(
                        payments = uiState.filteredPayments,
                        onEditPayment = viewModel::showEditDialog,
                        onDeletePayment = viewModel::showDeleteConfirmation,
                        onCancelPayment = viewModel::cancelScheduledPayment
                    )
                }
            }
            
            // Floating Action Button
            if (!uiState.isLoading) {
                FloatingActionButton(
                    onClick = onNavigateToCreatePayment,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                    containerColor = TrueTapPrimary,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Scheduled Payment"
                    )
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmation && uiState.paymentToDelete != null) {
        DeleteConfirmationDialog(
            payment = uiState.paymentToDelete!!,
            onConfirm = viewModel::deleteScheduledPayment,
            onDismiss = viewModel::hideDeleteConfirmation
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleHeader(
    onNavigateBack: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: PaymentStatusFilter,
    onFilterClick: () -> Unit,
    onFilterSelect: (PaymentStatusFilter) -> Unit,
    showFilterMenu: Boolean,
    onDismissFilterMenu: () -> Unit
) {
    Surface(
        color = TrueTapContainer,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top row: Back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text = "Scheduled Payments",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                
                // Filter button
                Box {
                    IconButton(
                        onClick = onFilterClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (selectedFilter != PaymentStatusFilter.ALL) TrueTapPrimary else TrueTapTextSecondary
                        )
                    }
                    
                    // Filter dropdown menu
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = onDismissFilterMenu
                    ) {
                        PaymentStatusFilter.values().forEach { filter ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = filter.displayName,
                                        color = if (filter == selectedFilter) TrueTapPrimary else TrueTapTextPrimary
                                    )
                                },
                                onClick = { onFilterSelect(filter) },
                                leadingIcon = if (filter == selectedFilter) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = TrueTapPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search payments...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TrueTapTextSecondary
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TrueTapTextSecondary
                            )
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrueTapPrimary,
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                    focusedContainerColor = TrueTapContainer,
                    unfocusedContainerColor = TrueTapContainer
                ),
                singleLine = true
            )
            
            // Filter chips
            if (selectedFilter != PaymentStatusFilter.ALL) {
                Spacer(modifier = Modifier.height(12.dp))
                FilterChip(
                    onClick = { onFilterSelect(PaymentStatusFilter.ALL) },
                    label = { 
                        Text(
                            text = "Filter: ${selectedFilter.displayName}",
                            fontSize = 12.sp
                        )
                    },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove filter",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TrueTapPrimary.copy(alpha = 0.1f),
                        selectedLabelColor = TrueTapPrimary,
                        selectedLeadingIconColor = TrueTapPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = TrueTapPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading scheduled payments...",
                fontSize = 16.sp,
                color = TrueTapTextSecondary
            )
        }
    }
}

@Composable
private fun EmptyState(
    hasScheduledPayments: Boolean,
    searchQuery: String,
    onCreatePayment: () -> Unit,
    onClearSearch: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            // Icon
            Icon(
                imageVector = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.Schedule,
                contentDescription = null,
                tint = TrueTapTextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title and description
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "No matching payments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No scheduled payments match \"$searchQuery\"",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onClearSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Search")
                }
            } else if (hasScheduledPayments) {
                Text(
                    text = "All payments filtered out",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try adjusting your filter settings",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "No scheduled payments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Schedule payments to send automatically on specific dates and intervals",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onCreatePayment,
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Schedule Your First Payment")
                }
            }
        }
    }
}

@Composable
private fun ScheduledPaymentsList(
    payments: List<ScheduledPayment>,
    onEditPayment: (ScheduledPayment) -> Unit,
    onDeletePayment: (ScheduledPayment) -> Unit,
    onCancelPayment: (ScheduledPayment) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(payments, key = { it.id }) { payment ->
            ScheduledPaymentCard(
                payment = payment,
                onEdit = { onEditPayment(payment) },
                onDelete = { onDeletePayment(payment) },
                onCancel = { onCancelPayment(payment) }
            )
        }
        
        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ScheduledPaymentCard(
    payment: ScheduledPayment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showActions = !showActions },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row: Status and amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                PaymentStatusBadge(status = payment.status)
                
                // Amount
                Text(
                    text = "${payment.amount} ${payment.token}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Recipient info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar or initial
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TrueTapPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = payment.recipientName?.first()?.uppercase() ?: "?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = payment.recipientName ?: "Unknown Recipient",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${payment.recipientAddress.take(8)}...${payment.recipientAddress.takeLast(4)}",
                        fontSize = 12.sp,
                        color = TrueTapTextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Schedule info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Next payment",
                        fontSize = 12.sp,
                        color = TrueTapTextSecondary
                    )
                    Text(
                        text = formatDateTime(payment.nextExecutionDate),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Frequency",
                        fontSize = 12.sp,
                        color = TrueTapTextSecondary
                    )
                    Text(
                        text = payment.repeatInterval.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                }
            }
            
            // Memo if present
            payment.memo?.let { memo ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: $memo",
                    fontSize = 12.sp,
                    color = TrueTapTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Execution count if applicable
            if (payment.maxExecutions != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${payment.currentExecutions}/${payment.maxExecutions} payments sent",
                    fontSize = 12.sp,
                    color = TrueTapTextSecondary
                )
            }
            
            // Action buttons (expandable)
            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = TrueTapTextSecondary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (payment.status == PaymentStatus.PENDING) {
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF44336)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFF44336))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel", fontSize = 14.sp)
                            }
                        }
                        
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TrueTapPrimary
                            ),
                            border = BorderStroke(1.dp, TrueTapPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 14.sp)
                        }
                        
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TrueTapTextSecondary
                            ),
                            border = BorderStroke(1.dp, TrueTapTextSecondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusBadge(status: PaymentStatus) {
    val (backgroundColor, textColor, icon) = when (status) {
        PaymentStatus.PENDING -> Triple(
            Color(0xFFFFF3CD),
            Color(0xFF856404),
            Icons.Default.Schedule
        )
        PaymentStatus.COMPLETED -> Triple(
            Color(0xFFD4EDDA),
            Color(0xFF155724),
            Icons.Default.CheckCircle
        )
        PaymentStatus.FAILED -> Triple(
            Color(0xFFF8D7DA),
            Color(0xFF721C24),
            Icons.Default.Error
        )
        PaymentStatus.CANCELLED -> Triple(
            Color(0xFFE2E3E5),
            Color(0xFF383D41),
            Icons.Default.Cancel
        )
    }
    
    Row(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    payment: ScheduledPayment,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Scheduled Payment",
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete this scheduled payment?",
                    color = TrueTapTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${payment.amount} ${payment.token} to ${payment.recipientName ?: "Unknown"}",
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = "This action cannot be undone.",
                    color = Color(0xFFF44336),
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Text("Delete")
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

private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return dateTime.format(formatter)
}