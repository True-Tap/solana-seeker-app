package com.truetap.solana.seeker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.data.AuthState
import com.truetap.solana.seeker.ui.theme.SolanaseekerappTheme
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val walletViewModel: WalletViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolanaseekerappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WalletScreen(
                        walletViewModel = walletViewModel,
                        activity = this@MainActivity,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WalletScreen(
    walletViewModel: WalletViewModel,
    activity: ComponentActivity,
    modifier: Modifier = Modifier
) {
    val authState by walletViewModel.authState.collectAsStateWithLifecycle()
    val isConnected by walletViewModel.isConnected.collectAsStateWithLifecycle(initialValue = false)
    val currentAccount by walletViewModel.currentAccount.collectAsStateWithLifecycle(initialValue = null)
    val seedVaultInfo by walletViewModel.seedVaultInfo.collectAsStateWithLifecycle()
    val isLoading by walletViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by walletViewModel.errorMessage.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "True Tap - Solana Wallet Integration",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Environment: Devnet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Seed Vault Status
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Seed Vault Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Available: ${seedVaultInfo?.isAvailable ?: "Checking..."}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Biometric Auth: ${seedVaultInfo?.isBiometricAuthSupported ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Connection Status
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Connection Status",
                    style = MaterialTheme.typography.titleMedium
                )
                val statusText = when (val currentState = authState) {
                    is AuthState.Idle -> "Not connected"
                    is AuthState.Connecting -> "Connecting..."
                    is AuthState.Authenticating -> "Authenticating..."
                    is AuthState.Connected -> "Connected"
                    is AuthState.Error -> "Error: ${currentState.message}"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                currentAccount?.let { account ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Public Key: ${account.publicKey.take(20)}...",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Cluster: ${account.cluster}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    account.accountLabel?.let { label ->
                        Text(
                            text = "Label: $label",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        // Error Message
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { walletViewModel.clearError() }
                    ) {
                        Text("Clear Error")
                    }
                }
            }
        }
        
        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isConnected) {
                Button(
                    onClick = { walletViewModel.connectWallet(activity) },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Connect Wallet")
                    }
                }
            } else {
                Button(
                    onClick = { walletViewModel.disconnect() },
                    enabled = !isLoading
                ) {
                    Text("Disconnect")
                }
                
                Button(
                    onClick = { walletViewModel.signAuthMessage(activity) },
                    enabled = !isLoading
                ) {
                    Text("Sign Message")
                }
            }
        }
        
        if (authState is AuthState.Error) {
            Button(
                onClick = { walletViewModel.retry(activity) },
                enabled = !isLoading
            ) {
                Text("Retry")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WalletScreenPreview() {
    SolanaseekerappTheme {
        // Preview with mock data - would need to create mock ViewModel for full preview
        Text("Wallet Screen Preview")
    }
}