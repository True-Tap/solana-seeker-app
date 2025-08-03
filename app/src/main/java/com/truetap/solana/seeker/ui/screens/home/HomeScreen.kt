package com.truetap.solana.seeker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.truetap.solana.seeker.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToWallet: () -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .padding(screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Home Screen",
            style = MaterialTheme.typography.headlineLarge,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.large))
        
        Button(
            onClick = onNavigateToWallet,
            colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
        ) {
            Text("Go to Wallet")
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        Button(
            onClick = onNavigateToAuth,
            colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
        ) {
            Text("Go to Auth")
        }
    }
} 