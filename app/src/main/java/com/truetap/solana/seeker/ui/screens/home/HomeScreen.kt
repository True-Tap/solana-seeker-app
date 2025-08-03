package com.truetap.solana.seeker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.truetap.solana.seeker.ui.theme.Spacing
import com.truetap.solana.seeker.ui.theme.screenPadding

@Composable
fun HomeScreen(
    onNavigateToWallet: () -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Solana Seeker",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(Spacing.xlarge))
        
        Button(
            onClick = onNavigateToWallet,
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Text("Open Wallet")
        }
        
        Button(
            onClick = onNavigateToAuth,
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Text("Authentication")
        }
    }
} 