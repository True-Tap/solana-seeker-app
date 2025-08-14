package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.services.FeePreset
import com.truetap.solana.seeker.ui.theme.TrueTapTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitPayScreen(
    onNavigateBack: () -> Unit,
    viewModel: SplitPayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showContacts by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Split Pay") }, navigationIcon = {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { uiState.info?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TrueTapTextSecondary) } }
            
            // Validation error display
            item {
                uiState.validationError?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = uiState.totalAmount,
                    onValueChange = viewModel::setTotalAmount,
                    label = { Text("Total amount (SOL)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = uiState.splitEvenly,
                        onClick = { viewModel.toggleEvenSplit(true) },
                        label = { Text("Split evenly") }
                    )
                    FilterChip(
                        selected = !uiState.splitEvenly,
                        onClick = { viewModel.toggleEvenSplit(false) },
                        label = { Text("Custom shares") }
                    )
                    TextButton(onClick = { viewModel.loadContacts(); showContacts = true }) { Text("Add contacts") }
                }
            }
            
            // Participants list with enhanced info
            itemsIndexed(uiState.participants) { _, p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (p.riskLevel) {
                            RiskLevel.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            RiskLevel.LOW -> MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${p.address.take(6)}...${p.address.takeLast(4)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TrueTapTextSecondary
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                if (uiState.splitEvenly) {
                                    Text(
                                        text = "${p.amount} SOL",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = p.sharePercent.toPlainString(),
                                            onValueChange = { new ->
                                                val pct = new.filter { it.isDigit() || it == '.' }
                                                val updated = uiState.participants.map {
                                                    if (it.id == p.id) it.copy(sharePercent = pct.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO) else it
                                                }
                                                viewModel.setParticipants(updated)
                                            },
                                            label = { Text("%") },
                                            modifier = Modifier.width(88.dp)
                                        )
                                        Text("= ${p.amount} SOL", fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        
                        // First payment and risk warnings
                        if (p.isFirstPayment || p.riskLevel != RiskLevel.LOW) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (p.isFirstPayment) Icons.Default.Info else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (p.isFirstPayment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        p.isFirstPayment -> "First payment to this contact"
                                        p.riskLevel == RiskLevel.HIGH -> "High risk contact"
                                        p.riskLevel == RiskLevel.MEDIUM -> "Medium risk contact"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (p.isFirstPayment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Custom shares total percentage display
            if (!uiState.splitEvenly && uiState.totalPercent != java.math.BigDecimal.ZERO) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.totalPercent == java.math.BigDecimal(100)) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Percentage:")
                            Text(
                                text = "${uiState.totalPercent}%",
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.totalPercent == java.math.BigDecimal(100)) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            item {
                Text("Speed", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(FeePreset.NORMAL, FeePreset.FAST, FeePreset.EXPRESS).forEach { preset ->
                        FilterChip(
                            selected = uiState.feePreset == preset,
                            onClick = { viewModel.setFeePreset(preset) },
                            label = { Text(when (preset) { FeePreset.NORMAL -> "Normal"; FeePreset.FAST -> "Fast"; FeePreset.EXPRESS -> "Express" }) }
                        )
                    }
                }
            }
            
            item {
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.showSummary() },
                        enabled = uiState.isFormValid && !uiState.isSubmitting
                    ) { Text("Review & Send") }
                    OutlinedButton(
                        onClick = { viewModel.submitSplitSendOrRequests(false) },
                        enabled = uiState.isFormValid && !uiState.isSubmitting
                    ) { Text("Request") }
                }
            }
        }
    }
    
    // Contacts modal
    if (showContacts) {
        AlertDialog(
            onDismissRequest = { showContacts = false },
            confirmButton = {
                TextButton(onClick = { showContacts = false }) { Text("Done") }
            },
            title = { Text("Select Contacts") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.availableContacts.forEach { c ->
                        val selected = uiState.participants.any { it.id == c.id }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column { 
                                Text(c.name)
                                Text("${c.address.take(6)}...${c.address.takeLast(4)}", style = MaterialTheme.typography.bodySmall) 
                            }
                            FilterChip(
                                selected = selected, 
                                onClick = { viewModel.toggleContactSelection(c) }, 
                                label = { Text(if (selected) "Selected" else "Add") }
                            )
                        }
                    }
                }
            }
        )
    }
    
    // Summary dialog
    if (uiState.showSummary) {
        AlertDialog(
            onDismissRequest = { viewModel.hideSummary() },
            title = { Text("Split Summary") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Total: ${uiState.totalAmount} SOL")
                    Text("Participants: ${uiState.participants.size}")
                    Text("Fee: ${uiState.feePreset.name}")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Participants:", fontWeight = FontWeight.Medium)
                    uiState.participants.forEach { p ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(p.name)
                            Text("${p.amount} SOL")
                        }
                    }
                    
                    if (uiState.participants.any { it.isFirstPayment }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "First payment to ${uiState.participants.count { it.isFirstPayment }} contact(s)",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.submitSplitSendOrRequests(true)
                        viewModel.hideSummary()
                    }
                ) { Text("Confirm & Send") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideSummary() }) { Text("Cancel") }
            }
        )
    }
}


