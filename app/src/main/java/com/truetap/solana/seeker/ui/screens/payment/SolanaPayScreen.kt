package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolanaPayScreen(
    onNavigateBack: () -> Unit,
    onOpenScanner: () -> Unit,
    onPasteLink: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Merchant Checkout") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Scan a Solana Pay QR or paste a payment link.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onOpenScanner) { Text("Scan QR") }
            var link by remember { mutableStateOf("") }
            OutlinedTextField(value = link, onValueChange = { link = it }, label = { Text("Solana Pay link") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (link.isNotBlank()) onPasteLink(link) }, enabled = link.isNotBlank()) { Text("Continue") }
            Text("Tip: Confirm the merchant before sending.", style = MaterialTheme.typography.bodySmall)
        }
    }
}


