package com.truetap.solana.seeker.ui.screens.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.screens.home.Transaction
import com.truetap.solana.seeker.ui.screens.home.TransactionType
import com.truetap.solana.seeker.viewmodels.TransactionHistoryViewModel
import com.truetap.solana.seeker.viewmodels.SortOption
import com.truetap.solana.seeker.viewmodels.TransactionTypeFilter
import com.truetap.solana.seeker.viewmodels.TimePeriodFilter
import java.text.SimpleDateFormat
import java.util.*

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit = {},
    viewModel: TransactionHistoryViewModel = hiltViewModel()
) {
    // Get state from TransactionHistoryViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val transactionStats by viewModel.transactionStats.collectAsStateWithLifecycle()
    val hasActiveFilters by viewModel.hasActiveFilters.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Transaction History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TrueTapTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = TrueTapBackground
            )
        )

        // Summary Stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Received",
                    value = "${String.format("%.2f", transactionStats.totalReceived)} SOL",
                    color = Color(0xFF4CAF50)
                )
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = TrueTapTextSecondary.copy(alpha = 0.3f)
                )
                
                StatItem(
                    label = "Total Sent",
                    value = "${String.format("%.2f", transactionStats.totalSent)} SOL",
                    color = Color(0xFFF44336)
                )
            }
        }

        // Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.searchTransactions(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text(
                        text = "Search transactions...",
                        color = TrueTapTextSecondary,
                        fontSize = 16.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TrueTapTextSecondary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.searchTransactions("") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TrueTapTextSecondary
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrueTapPrimary,
                    unfocusedBorderColor = TrueTapTextSecondary.copy(alpha = 0.3f),
                    focusedTextColor = TrueTapTextPrimary,
                    unfocusedTextColor = TrueTapTextPrimary,
                    cursorColor = TrueTapPrimary
                )
            )
        }

        // Enhanced Filter and Sort Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Active Filters Summary
                if (hasActiveFilters || uiState.searchQuery.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active filters:",
                            fontSize = 12.sp,
                            color = TrueTapTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (uiState.transactionTypeFilter != TransactionTypeFilter.ALL) {
                            FilterChip(
                                text = uiState.transactionTypeFilter.displayName,
                                onRemove = { viewModel.setTransactionTypeFilter(TransactionTypeFilter.ALL) }
                            )
                        }
                        
                        if (uiState.timePeriodFilter != TimePeriodFilter.ALL_TIME) {
                            FilterChip(
                                text = uiState.timePeriodFilter.displayName,
                                onRemove = { viewModel.setTimePeriodFilter(TimePeriodFilter.ALL_TIME) }
                            )
                        }
                        
                        if (uiState.searchQuery.isNotEmpty()) {
                            FilterChip(
                                text = "\"${uiState.searchQuery.take(10)}${if (uiState.searchQuery.length > 10) "..." else ""}\"",
                                onRemove = { viewModel.searchTransactions("") }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Filter Pills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionTypeFilter.values().forEach { filter ->
                        EnhancedFilterPill(
                            text = filter.displayName,
                            isSelected = uiState.transactionTypeFilter == filter,
                            onClick = { viewModel.setTransactionTypeFilter(filter) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Time Period and Sort Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time Period Dropdown
                    FilterDropdown(
                        label = "Period: ${uiState.timePeriodFilter.displayName}",
                        isExpanded = uiState.isFilterExpanded,
                        onToggle = { viewModel.toggleFilterExpanded() },
                        options = TimePeriodFilter.values().toList(),
                        onOptionSelected = { viewModel.setTimePeriodFilter(it) }
                    )
                    
                    // Sort Dropdown
                    FilterDropdown(
                        label = "Sort: ${uiState.sortOption.displayName}",
                        isExpanded = uiState.isSortExpanded,
                        onToggle = { viewModel.toggleSortExpanded() },
                        options = SortOption.values().toList(),
                        onOptionSelected = { viewModel.setSortOption(it) }
                    )
                }
                
                // Clear Filters Button
                if (hasActiveFilters) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Clear Filters",
                            modifier = Modifier.clickable { viewModel.clearAllFilters() },
                            color = TrueTapPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Results Count
        if (filteredTransactions.isNotEmpty() || hasActiveFilters || uiState.searchQuery.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTransactions.size} transaction${if (filteredTransactions.size != 1) "s" else ""} found",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                
                if (uiState.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Updating...",
                            fontSize = 12.sp,
                            color = TrueTapTextSecondary
                        )
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.dp,
                            color = TrueTapPrimary
                        )
                    }
                }
            }
        }

        // Transaction List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyTransactionsState(
                        hasActiveFilters = hasActiveFilters || uiState.searchQuery.isNotEmpty(),
                        onClearFilters = { viewModel.clearAllFilters(); viewModel.searchTransactions("") },
                        onRefresh = { viewModel.refreshTransactions() }
                    )
                }
            } else {
                items(filteredTransactions) { transaction ->
                    TransactionHistoryItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        }
        
        // Floating Action Button for refresh positioned at bottom end
        FloatingActionButton(
            onClick = { viewModel.refreshTransactions() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = TrueTapPrimary,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh transactions"
            )
        }
        
        // Click outside to dismiss dropdowns
        if (uiState.isFilterExpanded || uiState.isSortExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.collapseAll() }
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = TrueTapPrimary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, TrueTapPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = TrueTapPrimary,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter",
                tint = TrueTapPrimary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun EnhancedFilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) TrueTapPrimary else TrueTapContainer,
        border = if (isSelected) null else BorderStroke(1.dp, TrueTapTextSecondary.copy(alpha = 0.3f)),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else TrueTapTextSecondary
            )
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) TrueTapPrimary else TrueTapContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else TrueTapTextSecondary
        )
    }
}

@Composable
private fun <T> FilterDropdown(
    label: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    options: List<T>,
    onOptionSelected: (T) -> Unit
) where T : Enum<T> {
    Box {
        Surface(
            modifier = Modifier.clickable { onToggle() },
            shape = RoundedCornerShape(8.dp),
            color = TrueTapContainer,
            border = BorderStroke(1.dp, TrueTapTextSecondary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = TrueTapTextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                val rotationAngle by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(200, easing = EaseOutCubic),
                    label = "arrow_rotation"
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TrueTapTextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = rotationAngle }
                )
            }
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(200)) + 
                   expandVertically(animationSpec = tween(200, easing = EaseOutCubic)),
            exit = fadeOut(animationSpec = tween(150)) + 
                  shrinkVertically(animationSpec = tween(150, easing = EaseInCubic))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
            ) {
                Column {
                    options.forEach { option ->
                        Text(
                            text = when (option) {
                                is SortOption -> option.displayName
                                is TimePeriodFilter -> option.displayName
                                is TransactionTypeFilter -> option.displayName
                                else -> option.name
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(option) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            fontSize = 14.sp,
                            color = TrueTapTextPrimary
                        )
                        if (option != options.last()) {
                            Divider(
                                color = TrueTapTextSecondary.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionHistoryItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (transaction.type) {
                            TransactionType.RECEIVED -> Color(0xFFE8F5E8) // Light green
                            TransactionType.SENT -> Color(0xFFFFEBEE) // Light red
                            TransactionType.SWAPPED -> Color(0xFFE3F2FD) // Light blue
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.type) {
                        TransactionType.RECEIVED -> Icons.AutoMirrored.Filled.TrendingUp
                        TransactionType.SENT -> Icons.AutoMirrored.Filled.TrendingDown
                        TransactionType.SWAPPED -> Icons.Filled.SwapHoriz
                    },
                    contentDescription = transaction.type.name,
                    tint = when (transaction.type) {
                        TransactionType.RECEIVED -> Color(0xFF4CAF50) // Green
                        TransactionType.SENT -> Color(0xFFF44336) // Red
                        TransactionType.SWAPPED -> Color(0xFF2196F3) // Blue
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.RECEIVED -> "From ${transaction.otherParty}"
                        TransactionType.SENT -> "To ${transaction.otherParty}"
                        TransactionType.SWAPPED -> "Swapped with ${transaction.otherParty}"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = transaction.timeAgo,
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
            }
            
            // Transaction Amount
            Text(
                text = when (transaction.type) {
                    TransactionType.RECEIVED -> "+${transaction.amount} ${transaction.token}"
                    TransactionType.SENT -> "-${transaction.amount} ${transaction.token}"
                    TransactionType.SWAPPED -> "${transaction.amount} ${transaction.token}"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (transaction.type) {
                    TransactionType.RECEIVED -> Color(0xFF4CAF50) // Green
                    TransactionType.SENT -> Color(0xFFF44336) // Red
                    TransactionType.SWAPPED -> Color(0xFF2196F3) // Blue
                }
            )
        }
    }
}

@Composable
private fun EmptyTransactionsState(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = if (hasActiveFilters) Icons.Default.FilterList else Icons.Default.Receipt,
            contentDescription = null,
            tint = TrueTapTextSecondary,
            modifier = Modifier.size(64.dp)
        )
        
        Text(
            text = if (hasActiveFilters) {
                "No transactions match your filters"
            } else {
                "No transactions found"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = if (hasActiveFilters) {
                "Try adjusting your filters or search terms to see more results."
            } else {
                "Your transaction history will appear here once you start using TrueTap."
            },
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (hasActiveFilters) {
                OutlinedButton(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TrueTapPrimary
                    ),
                    border = BorderStroke(1.dp, TrueTapPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Clear Filters")
                }
            }
            
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrueTapPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Refresh")
            }
        }
    }
}