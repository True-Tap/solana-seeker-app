package com.truetap.solana.seeker.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.truetap.solana.seeker.ui.theme.*

@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .padding(screenPadding)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TrueTapTextPrimary
                )
            }
            
            Text(
                text = "Wallet",
                style = MaterialTheme.typography.headlineMedium,
                color = TrueTapTextPrimary
            )
            
            Box(modifier = Modifier.size(48.dp))
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Content
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wallet Screen",
                style = MaterialTheme.typography.headlineLarge,
                color = TrueTapTextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.large))
            
            Text(
                text = "Wallet functionality coming soon...",
                style = MaterialTheme.typography.bodyLarge,
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
} 