package com.truetap.solana.seeker.ui.screens.transaction

import androidx.compose.foundation.background
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
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.screens.home.Transaction
import com.truetap.solana.seeker.ui.screens.home.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    // Extended sample transaction data
    val allTransactions = remember {
        listOf(
            Transaction("1", TransactionType.RECEIVED, "25.5", "Les Grossman", "2 min ago"),
            Transaction("2", TransactionType.SENT, "12", "Sarah Kim", "1 hour ago"),
            Transaction("3", TransactionType.RECEIVED, "8.75", "Mike Johnson", "3 hours ago"),
            Transaction("4", TransactionType.SENT, "45.0", "Alex Thompson", "1 day ago"),
            Transaction("5", TransactionType.RECEIVED, "33.25", "Jennifer Lee", "2 days ago"),
            Transaction("6", TransactionType.SENT, "18.5", "David Wilson", "3 days ago"),
            Transaction("7", TransactionType.RECEIVED, "67.8", "Maria Garcia", "4 days ago"),
            Transaction("8", TransactionType.SENT, "22.15", "John Smith", "5 days ago"),
            Transaction("9", TransactionType.RECEIVED, "91.0", "Emily Brown", "1 week ago"),
            Transaction("10", TransactionType.SENT, "15.75", "Robert Davis", "1 week ago"),
            Transaction("11", TransactionType.RECEIVED, "42.5", "Lisa Anderson", "2 weeks ago"),
            Transaction("12", TransactionType.SENT, "38.25", "Thomas Miller", "2 weeks ago"),
            Transaction("13", TransactionType.RECEIVED, "156.75", "Michelle White", "3 weeks ago"),
            Transaction("14", TransactionType.SENT, "29.5", "Kevin Martinez", "3 weeks ago"),
            Transaction("15", TransactionType.RECEIVED, "73.25", "Jessica Taylor", "1 month ago")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueTapBackground)
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
                    value = "${allTransactions.filter { it.type == TransactionType.RECEIVED }.sumOf { it.amount.toDouble() }} SOL",
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
                    value = "${allTransactions.filter { it.type == TransactionType.SENT }.sumOf { it.amount.toDouble() }} SOL",
                    color = Color(0xFFF44336)
                )
            }
        }

        // Filter Pills (Future enhancement)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill("All", true)
            FilterPill("Received", false)
            FilterPill("Sent", false)
        }

        // Transaction List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(allTransactions) { transaction ->
                TransactionHistoryItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction) }
                )
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
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
private fun FilterPill(
    text: String,
    isSelected: Boolean
) {
    Surface(
        modifier = Modifier.clickable { /* Handle filter */ },
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
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.type) {
                        TransactionType.RECEIVED -> Icons.AutoMirrored.Filled.TrendingUp
                        TransactionType.SENT -> Icons.AutoMirrored.Filled.TrendingDown
                    },
                    contentDescription = transaction.type.name,
                    tint = when (transaction.type) {
                        TransactionType.RECEIVED -> Color(0xFF4CAF50) // Green
                        TransactionType.SENT -> Color(0xFFF44336) // Red
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
                text = "${if (transaction.type == TransactionType.RECEIVED) "+" else "-"}${transaction.amount} ${transaction.token}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (transaction.type) {
                    TransactionType.RECEIVED -> Color(0xFF4CAF50) // Green
                    TransactionType.SENT -> Color(0xFFF44336) // Red
                }
            )
        }
    }
}