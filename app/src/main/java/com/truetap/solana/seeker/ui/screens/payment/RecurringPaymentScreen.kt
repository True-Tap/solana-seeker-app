package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.services.FeePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringPaymentScreen(onNavigateBack: () -> Unit, viewModel: RecurringPaymentViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Schedule Payment") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Schedule recurring payments. Fees may vary; transactions queue if offline.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = ui.recipient, onValueChange = viewModel::setRecipient, label = { Text("Recipient address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ui.amount, onValueChange = viewModel::setAmount, label = { Text("Amount (SOL)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ui.memo, onValueChange = viewModel::setMemo, label = { Text("Memo (optional)") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Every")
                var days by remember { mutableStateOf(ui.cadenceDays.toString()) }
                OutlinedTextField(value = days, onValueChange = { days = it.filter { c -> c.isDigit() } }, modifier = Modifier.width(80.dp))
                Text("days")
                Button(onClick = { viewModel.setCadence(days.toIntOrNull() ?: 7) }) { Text("Set") }
            }
            Text("Speed", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(FeePreset.NORMAL, FeePreset.FAST, FeePreset.EXPRESS).forEach { p ->
                    FilterChip(selected = ui.feePreset == p, onClick = { viewModel.setFeePreset(p) }, label = { Text(when (p) { FeePreset.NORMAL -> "Normal"; FeePreset.FAST -> "Fast"; FeePreset.EXPRESS -> "Express" }) })
                }
            }
            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { viewModel.schedule() }, enabled = !ui.isSubmitting) { Text("Schedule") }
        }
    }
}


