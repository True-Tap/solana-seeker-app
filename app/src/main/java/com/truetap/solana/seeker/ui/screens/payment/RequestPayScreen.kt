package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
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
fun RequestPayScreen(
    onNavigateBack: () -> Unit,
    viewModel: RequestPayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Request Payment") }, navigationIcon = {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.info?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            OutlinedTextField(
                value = uiState.recipientAddress,
                onValueChange = viewModel::setRecipient,
                label = { Text("Recipient address") },
                trailingIcon = {
                    TextButton(onClick = { viewModel.useDemoContact() }) { Text("Pick") }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::setAmount,
                label = { Text("Amount (SOL)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.memo,
                onValueChange = viewModel::setMemo,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = uiState.isPrivate,
                    onClick = { viewModel.togglePrivate(true) },
                    label = { Text("Private") }
                )
                FilterChip(
                    selected = !uiState.isPrivate,
                    onClick = { viewModel.togglePrivate(false) },
                    label = { Text("Public (feed)") }
                )
            }
            Text("Fees", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(FeePreset.NORMAL, FeePreset.FAST, FeePreset.EXPRESS).forEach { preset ->
                    FilterChip(
                        selected = uiState.feePreset == preset,
                        onClick = { viewModel.setFeePreset(preset) },
                        label = { Text(when (preset) { FeePreset.NORMAL -> "Normal"; FeePreset.FAST -> "Fast"; FeePreset.EXPRESS -> "Express" }) }
                    )
                }
            }

            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = { viewModel.submitRequest() }, enabled = !uiState.isSubmitting) {
                Text("Send Request")
            }
            Text("Scam alert: Verify sender before accepting.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


