package com.truetap.solana.seeker.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.screens.home.Transaction
import com.truetap.solana.seeker.ui.screens.home.TransactionType
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.truetap.solana.seeker.data.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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
                    containerColor = Color.Transparent
                )
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Wallet Connection Status
                item {
                    WalletConnectionCard(authState = authState)
                }
                
                // Wallet Information
                item {
                    val currentAuthState = authState
                    when (currentAuthState) {
                        is AuthState.Connected -> {
                            WalletInfoCard(
                                walletAddress = currentAuthState.account.publicKey,
                                walletLabel = currentAuthState.account.accountLabel ?: "Connected Wallet",
                                walletType = "Seed Vault"
                            )
                        }
                        else -> {
                            WalletInfoCard(
                                walletAddress = "Not connected",
                                walletLabel = "No wallet connected",
                                walletType = "Unknown"
                            )
                        }
                    }
                }
                
                // Connection Support
                item {
                    ConnectionSupportCard()
                }
            }
        }
    }
}

@Composable
private fun WalletConnectionCard(authState: AuthState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (authState) {
                        is AuthState.Connected -> Icons.Default.CheckCircle
                        is AuthState.Connecting -> Icons.Default.Refresh
                        else -> Icons.Default.Error
                    },
                    contentDescription = "Connection Status",
                    tint = when (authState) {
                        is AuthState.Connected -> Color(0xFF4CAF50)
                        is AuthState.Connecting -> TrueTapPrimary
                        else -> Color(0xFFF44336)
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Connection Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = when (authState) {
                    is AuthState.Connected -> "Connected to Solana Seed Vault"
                    is AuthState.Connecting -> "Connecting to wallet..."
                    else -> "No wallet connected"
                },
                fontSize = 16.sp,
                color = TrueTapTextSecondary
            )
        }
    }
}

@Composable
private fun WalletInfoCard(
    walletAddress: String,
    walletLabel: String,
    walletType: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet Info",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Wallet Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wallet Label
            DetailRow(label = "Name", value = walletLabel)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Wallet Type
            DetailRow(label = "Type", value = walletType)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Wallet Address
            DetailRow(
                label = "Address", 
                value = if (walletAddress != "Not connected") {
                    "${walletAddress.take(6)}...${walletAddress.takeLast(4)}"
                } else {
                    walletAddress
                }
            )
        }
    }
}

@Composable
private fun ConnectionSupportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = "Support",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Connection Support",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "If you're experiencing connection issues, try disconnecting and reconnecting your wallet through the Settings menu.",
                fontSize = 14.sp,
                color = TrueTapTextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TrueTapTextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
