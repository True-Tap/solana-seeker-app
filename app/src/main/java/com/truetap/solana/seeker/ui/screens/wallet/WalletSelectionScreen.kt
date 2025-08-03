package com.truetap.solana.seeker.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*

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
        // Top Section - Branding
        Spacer(modifier = Modifier.height(Spacing.large))
        
        // TrueTap Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = Spacing.cardElevation,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = TrueTapShadow.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "TrueTap Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        // Title
        Text(
            text = "Choose Your Wallet",
            style = MaterialTheme.typography.headlineMedium,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        // Subtitle
        Text(
            text = "Connect to start using TrueTap",
            style = MaterialTheme.typography.bodyLarge,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.xlarge))
        
        // Wallet Options
        WalletOptionCard(
            walletType = WalletType.PHANTOM,
            icon = R.drawable.icons.svgs.regular.ghost,
            iconBackground = Color(0xFFAB47BC), // Purple
            title = "Phantom",
            description = "Most popular Solana wallet",
            onClick = { onWalletSelected(WalletType.PHANTOM) }
        )
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        WalletOptionCard(
            walletType = WalletType.SOLFLARE,
            icon = R.drawable.icons.svgs.regular.sun,
            iconBackground = Color(0xFFF39C12), // Yellow
            title = "Solflare",
            description = "Feature-rich Solana wallet",
            onClick = { onWalletSelected(WalletType.SOLFLARE) }
        )
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        WalletOptionCard(
            walletType = WalletType.SOLANA_SEEKER,
            icon = R.drawable.icons.svgs.regular.stack,
            iconBackground = Color(0xFF16A085), // Teal
            title = "Solana Seeker",
            description = "Hardware wallet integration",
            onClick = { onWalletSelected(WalletType.SOLANA_SEEKER) }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Help Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "New to Solana?",
                style = MaterialTheme.typography.bodyLarge,
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Get Started with Phantom",
                    style = MaterialTheme.typography.bodySmall,
                    color = TrueTapPrimary,
                    modifier = Modifier.clickable { /* TODO: Open Phantom guide */ }
                )
                Spacer(modifier = Modifier.width(4.dp))
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.icons.svgs.regular.arrow_up_right),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(TrueTapPrimary)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
    }
}

@Composable
private fun WalletOptionCard(
    walletType: WalletType,
    icon: Int,
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
            // Wallet Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
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
            
            // Wallet Icon
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.icons.svgs.regular.wallet),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(TrueTapTextInactive)
            )
        }
    }
}

enum class WalletType {
    PHANTOM,
    SOLFLARE,
    SOLANA_SEEKER
} 