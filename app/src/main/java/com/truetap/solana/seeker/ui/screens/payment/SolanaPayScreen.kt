package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolanaPayScreen(
    onNavigateBack: () -> Unit,
    onOpenScanner: () -> Unit,
    onConfirm: (recipient: String?, amount: String?, memo: String?) -> Unit
) {
    var pending by remember { mutableStateOf<SolanaPayFields?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Merchant Checkout") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Scan a Solana Pay QR or paste a payment link.", style = MaterialTheme.typography.bodyMedium)
            val scanner = rememberLauncherForActivityResult(ScanContract()) { result ->
                if (result != null && !result.contents.isNullOrBlank()) {
                    pending = parseSolanaPay(result.contents)
                }
            }
            Button(onClick = {
                val opts = ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt("Scan Solana Pay QR").setBeepEnabled(false)
                scanner.launch(opts)
            }) { Text("Scan QR") }
            var link by remember { mutableStateOf("") }
            OutlinedTextField(value = link, onValueChange = { link = it }, label = { Text("Solana Pay link") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (link.isNotBlank()) pending = parseSolanaPay(link) }, enabled = link.isNotBlank()) { Text("Continue") }
            Text("Tip: Confirm the merchant before sending.", style = MaterialTheme.typography.bodySmall)
        }
    }

    // Confirmation dialog, shown when pending is set
    pending?.let { fields ->
        AlertDialog(
            onDismissRequest = { pending = null },
            confirmButton = {
                Button(onClick = {
                    val r = fields.recipient
                    val a = fields.amount
                    val m = fields.memo
                    pending = null
                    onConfirm(r, a, m)
                }) { Text("Confirm") }
            },
            dismissButton = { OutlinedButton(onClick = { pending = null }) { Text("Cancel") } },
            title = { Text("Confirm Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Recipient: ${fields.recipient ?: "—"}")
                    Text("Amount: ${fields.amount ?: "—"}")
                    fields.label?.let { Text("Merchant: $it") }
                    fields.memo?.let { Text("Memo: $it") }
                }
            }
        )
    }
}

private data class SolanaPayFields(
    val recipient: String?,
    val amount: String?,
    val memo: String?,
    val label: String?
)

private fun parseSolanaPay(input: String): SolanaPayFields {
    return try {
        val uri = android.net.Uri.parse(input)
        if (uri.scheme.equals("solana", true)) {
            val ssp = uri.schemeSpecificPart ?: ""
            return if (ssp.startsWith("pay", true)) {
                val qIndex = ssp.indexOf('?')
                val query = if (qIndex >= 0) ssp.substring(qIndex) else ""
                val qp = android.net.Uri.parse("dummy://host$query")
                SolanaPayFields(
                    recipient = qp.getQueryParameter("recipient"),
                    amount = qp.getQueryParameter("amount"),
                    memo = qp.getQueryParameter("memo"),
                    label = qp.getQueryParameter("label")
                )
            } else {
                val address = ssp.substringBefore("?").removePrefix("//")
                val qp = android.net.Uri.parse("dummy://host?${ssp.substringAfter('?', "")}")
                SolanaPayFields(
                    recipient = address.ifBlank { null },
                    amount = qp.getQueryParameter("amount"),
                    memo = qp.getQueryParameter("memo"),
                    label = qp.getQueryParameter("label")
                )
            }
        } else {
            // Fallback: not a solana: URI, try as plain address
            SolanaPayFields(recipient = input, amount = null, memo = null, label = null)
        }
    } catch (_: Exception) {
        SolanaPayFields(null, null, null, null)
    }
}


