package com.truetap.solana.seeker.ui.screens.landing

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*

@Composable
fun LandingScreen(
    onConnectWallet: () -> Unit,
    onTryDemo: () -> Unit,
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
        Spacer(modifier = Modifier.height(Spacing.xlarge))
        
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
        
        // Welcome Text
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.bodyLarge,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        // Brand Name
        Text(
            text = "TrueTap",
            style = MaterialTheme.typography.displayLarge,
            color = TrueTapPrimary,
            textAlign = TextAlign.Center
        )
        
        // Tagline
        Text(
            text = "Payments. Reimagined.",
            style = MaterialTheme.typography.bodyLarge,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.xlarge))
        
        // Action Cards
        ActionCard(
            icon = R.drawable.wallet,
            title = "Connect Wallet",
            description = "Use your Solana wallet for real payments",
            onClick = onConnectWallet
        )
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        ActionCard(
            icon = R.drawable.code,
            title = "Try Demo Mode",
            description = "Explore features with mock transactions",
            onClick = onTryDemo
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom Features
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeatureItem(R.drawable.shield, "Secure")
            Spacer(modifier = Modifier.width(Spacing.medium))
            Text("•", color = TrueTapTextSecondary)
            Spacer(modifier = Modifier.width(Spacing.medium))
            FeatureItem(R.drawable.lightning, "Instant")
            Spacer(modifier = Modifier.width(Spacing.medium))
            Text("•", color = TrueTapTextSecondary)
            Spacer(modifier = Modifier.width(Spacing.medium))
            FeatureItem(R.drawable.globe, "Decentralized")
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        // Footer
        Text(
            text = "Powered by Solana • Built for Seeker",
            style = MaterialTheme.typography.labelMedium,
            color = TrueTapTextInactive,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.medium))
    }
}

@Composable
private fun ActionCard(
    icon: Int,
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
                    .background(TrueTapPrimary),
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
            
            // Arrow Icon
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.caret_right),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(TrueTapPrimary)
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: Int,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(TrueTapTextSecondary)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = TrueTapTextSecondary
        )
    }
} 