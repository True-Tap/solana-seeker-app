package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.truetap.solana.seeker.repositories.PaymentRequest
import com.truetap.solana.seeker.repositories.RequestStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsInboxScreen(
    onNavigateBack: () -> Unit,
    viewModel: RequestsInboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Requests") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (uiState.isEmpty()) Text("No requests")
            uiState.forEach { req ->
                RequestRow(req,
                    onAccept = { viewModel.accept(req) },
                    onDecline = { viewModel.decline(req) }
                )
            }
        }
    }
}

@Composable
private fun RequestRow(req: PaymentRequest, onAccept: () -> Unit, onDecline: () -> Unit) {
    Card { 
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("From: ${req.fromAddress.take(6)}...${req.fromAddress.takeLast(4)}")
            Text("Amount: ${req.amount} SOL")
            req.memo?.let { Text("Note: $it") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, enabled = req.status == RequestStatus.PENDING) { Text("Accept") }
                OutlinedButton(onClick = onDecline, enabled = req.status == RequestStatus.PENDING) { Text("Decline") }
            }
        }
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class RequestsInboxViewModel @javax.inject.Inject constructor(
    private val requestsRepository: com.truetap.solana.seeker.repositories.RequestsRepository,
    private val walletRepository: com.truetap.solana.seeker.repositories.WalletRepository
) : androidx.lifecycle.ViewModel() {
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow<List<PaymentRequest>>(emptyList())
    val uiState: kotlinx.coroutines.flow.StateFlow<List<PaymentRequest>> = _uiState

    init {
        viewModelScope.launch {
            requestsRepository.flow.collect { _uiState.value = it }
        }
    }

    fun accept(req: PaymentRequest) {
        viewModelScope.launch {
            try {
                // Attempt to send payment
                walletRepository.sendTransaction(
                    toAddress = req.toAddress,
                    amount = req.amount,
                    message = req.memo,
                    activityResultSender = null
                ).onSuccess {
                    requestsRepository.updateStatus(req.id, RequestStatus.ACCEPTED)
                }.onFailure {
                    // keep pending; optionally surface error
                }
            } catch (_: Exception) { }
        }
    }

    fun decline(req: PaymentRequest) {
        viewModelScope.launch {
            requestsRepository.updateStatus(req.id, RequestStatus.DECLINED)
        }
    }
}


