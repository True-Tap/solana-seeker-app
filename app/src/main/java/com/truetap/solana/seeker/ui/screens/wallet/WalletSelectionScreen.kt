package com.truetap.solana.seeker.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*

enum class WalletType {
    PHANTOM, SOLFLARE, SOLANA_SEEKER
}

@Composable
fun WalletSelectionScreen(
    onWalletSelected: (WalletType) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .padding(screenPadding)
            .navigationBarsPadding()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TrueTapTextPrimary
                )
            }
            
            // Title
            Text(
                text = "Select Wallet",
                style = MaterialTheme.typography.headlineMedium,
                color = TrueTapTextPrimary
            )
            
            // Placeholder for symmetry
            Box(modifier = Modifier.size(48.dp))
        }
        
        Spacer(modifier = Modifier.height(Spacing.large))
        
        // Subtitle
        Text(
            text = "Choose your preferred Solana wallet",
            style = MaterialTheme.typography.bodyLarge,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.xlarge))
        
        // Wallet Options
        WalletOptionCard(
            walletType = WalletType.PHANTOM,
            icon = Icons.Filled.AccountBalanceWallet,
            iconBackground = Color(0xFFAB47BC), // Purple
            title = "Phantom",
            description = "Most popular Solana wallet",
            onClick = { onWalletSelected(WalletType.PHANTOM) }
        )
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        WalletOptionCard(
            walletType = WalletType.SOLFLARE,
            icon = Icons.Filled.AccountBalanceWallet,
            iconBackground = Color(0xFFF39C12), // Yellow
            title = "Solflare",
            description = "Feature-rich Solana wallet",
            onClick = { onWalletSelected(WalletType.SOLFLARE) }
        )
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        WalletOptionCard(
            walletType = WalletType.SOLANA_SEEKER,
            icon = Icons.Filled.AccountBalanceWallet,
            iconBackground = Color(0xFF16A085), // Teal
            title = "Solana Seeker",
            description = "Hardware wallet integration",
            onClick = { onWalletSelected(WalletType.SOLANA_SEEKER) }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Open wallet help */ }
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wallet Icon
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.AccountBalanceWallet,
                contentDescription = null,
                tint = TrueTapTextInactive,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(Spacing.small))
            
            Text(
                text = "Don't have a wallet?",
                style = MaterialTheme.typography.bodyMedium,
                color = TrueTapTextInactive
            )
            
            Spacer(modifier = Modifier.width(Spacing.small))
            
            Text(
                text = "Learn more",
                style = MaterialTheme.typography.bodyMedium,
                color = TrueTapPrimary
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.OpenInNew,
                contentDescription = null,
                tint = TrueTapPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun WalletOptionCard(
    walletType: WalletType,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = Spacing.cardElevation
        ),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = TrueTapContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.medium))
            
            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TrueTapTextSecondary
                )
            }
            
            // Arrow Icon
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TrueTapPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
} 