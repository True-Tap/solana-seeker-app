package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Composable
private fun rememberSolanaPayViewModel(): SolanaPayViewModel = hiltViewModel()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolanaPayScreen(
    onNavigateBack: () -> Unit,
    onOpenScanner: () -> Unit,
    onConfirm: (recipient: String?, amount: String?, memo: String?) -> Unit,
    onAfterSuccessNavigate: () -> Unit = {}
) {
    val viewModel = rememberSolanaPayViewModel()
    var pending by remember { mutableStateOf<SolanaPayFields?>(null) }
    var txPending by remember { mutableStateOf<TxUi.Tx?>(null) }
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }
    val sender = remember(activity) { activity?.let { ActivityResultSender(it) } }
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
            Button(onClick = {
                if (link.isNotBlank()) {
                    val parsed = parseSolanaPay(link)
                    if (parsed.recipient == null && (link.startsWith("http", true) || (link.startsWith("solana:", true) && link.contains("link=")))) {
                        // Transaction-request flow
                        viewModel.fetchTx(link) { result ->
                            when (result) {
                                is TxUi.Tx -> txPending = result
                                is TxUi.Error -> viewModel.showError(result.message)
                            }
                        }
                    } else {
                        pending = parsed
                    }
                }
            }, enabled = link.isNotBlank()) { Text("Continue") }
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

    // Confirmation for transaction-request (prebuilt transaction)
    val tx = txPending
    if (tx != null) {
        AlertDialog(
            onDismissRequest = { txPending = null },
            confirmButton = {
                Button(onClick = {
                    val act = activity
                    val s = sender
                    if (act != null && s != null) {
                        viewModel.signAndSend(tx, act, launcher, s) { ok, signature, errMsg ->
                            txPending = null
                            if (ok) {
                                val sig = signature
                                // Success toast
                                if (sig != null) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Paid: ${sig.take(8)}...${sig.takeLast(8)}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Payment sent", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                // Handle redirect if present
                                val redirect = tx.redirect
                                if (!redirect.isNullOrBlank() && sig != null) {
                                    try {
                                        val uri = android.net.Uri.parse(redirect).buildUpon()
                                            .appendQueryParameter("signature", sig)
                                            .build()
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        // Fallback to in-app navigation when redirect fails
                                        onAfterSuccessNavigate()
                                    }
                                } else {
                                    onAfterSuccessNavigate()
                                }
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    "Payment failed: ${errMsg ?: "Unknown error"}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                if (errMsg != null) viewModel.showError(errMsg)
                            }
                        }
                    } else {
                        txPending = null
                        viewModel.showError("Missing activity context for signing")
                    }
                }) { Text("Confirm & Pay") }
            },
            dismissButton = { OutlinedButton(onClick = { txPending = null }) { Text("Cancel") } },
            title = { Text("Confirm Checkout") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tx.label?.let { Text("Merchant: $it") }
                    tx.message?.let { Text("Message: $it") }
                    Text("You will review and approve in your wallet.")
                }
            }
        )
    }
}

// Simple UI VM glue
sealed class TxUi {
    data class Tx(val base64: String, val message: String?, val label: String?, val redirect: String?) : TxUi()
    data class Error(val message: String) : TxUi()
}

class SolanaPayViewModel @javax.inject.Inject constructor(
    private val service: com.truetap.solana.seeker.services.SolanaPayService,
    private val walletRepository: com.truetap.solana.seeker.repositories.WalletRepository
) : androidx.lifecycle.ViewModel() {
    var txUi by mutableStateOf<TxUi?>(null)
        private set

    fun fetchTx(link: String, onDone: (TxUi) -> Unit) {
        try {
            val req = service.fetchTransactionRequest(link)
            val ui = TxUi.Tx(req.transactionBase64, req.message, req.label, req.redirect)
            txUi = ui
            onDone(ui)
        } catch (e: Exception) {
            val err = TxUi.Error("Failed to fetch transaction: ${e.message}")
            txUi = err
            onDone(err)
        }
    }

    fun showConfirmForTx(tx: TxUi.Tx) {
        // For now, decode is deferred to signing screen; we just signal success
    }

    fun showError(msg: String) { txUi = TxUi.Error(msg) }

    fun signAndSend(
        tx: TxUi.Tx,
        activity: androidx.activity.ComponentActivity,
        launcher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>,
        sender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender,
        onDone: (Boolean, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = walletRepository.signAndSendPreparedTransaction(
                transactionBase64 = tx.base64,
                message = tx.message,
                activity = activity,
                activityResultLauncher = launcher,
                activityResultSender = sender
            )
            if (result.isSuccess) onDone(true, (result.getOrNull()?.txId), null) else onDone(false, null, result.exceptionOrNull()?.message)
        }
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


