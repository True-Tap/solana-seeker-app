package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.services.FeePreset

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
            item { uiState.info?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
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
                    // Contact picker shortcut (demo)
                    TextButton(onClick = { viewModel.loadContacts(); showContacts = true }) { Text("Add contacts") }
                }
            }
            itemsIndexed(uiState.participants) { _, p ->
                ListItem(
                    headlineContent = { Text(p.name) },
                    supportingContent = { Text("${p.address.take(6)}...${p.address.takeLast(4)}") },
                    trailingContent = { Text("${p.amount}") }
                )
                HorizontalDivider()
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
                    Button(onClick = { viewModel.submitSplitSendOrRequests(true) }, enabled = !uiState.isSubmitting) { Text("Send Now") }
                    OutlinedButton(onClick = { viewModel.submitSplitSendOrRequests(false) }, enabled = !uiState.isSubmitting) { Text("Request") }
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
                            Column { Text(c.name); Text("${c.address.take(6)}...${c.address.takeLast(4)}", style = MaterialTheme.typography.bodySmall) }
                            FilterChip(selected = selected, onClick = { viewModel.toggleContactSelection(c) }, label = { Text(if (selected) "Selected" else "Add") })
                        }
                    }
                }
            }
        )
    }
}


