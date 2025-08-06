package com.truetap.solana.seeker.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.domain.model.CryptoToken
import com.truetap.solana.seeker.domain.model.SwapQuote
import com.truetap.solana.seeker.services.GenesisNFTTier
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.viewmodels.SwapState
import java.math.BigDecimal

/**
 * Enhanced Swap Components with Genesis NFT Benefits
 */

@Composable
fun GenesisNFTBenefitsBadge(
    isGenesisHolder: Boolean,
    tier: GenesisNFTTier,
    modifier: Modifier = Modifier
) {
    if (isGenesisHolder) {
        val (badgeColor, tierText) = when (tier) {
            GenesisNFTTier.LEGENDARY -> Pair(Color(0xFFFFD700), "LEGENDARY")
            GenesisNFTTier.RARE -> Pair(Color(0xFF9C27B0), "RARE")
            GenesisNFTTier.COMMON -> Pair(TrueTapPrimary, "GENESIS")
            GenesisNFTTier.NONE -> Pair(TrueTapPrimary, "GENESIS")
        }
        
        Row(
            modifier = modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(badgeColor.copy(alpha = 0.2f), badgeColor.copy(alpha = 0.1f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = "Genesis NFT",
                tint = badgeColor,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = tierText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
            
            Text(
                text = "0.25% FEES",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapSuccess
            )
        }
    }
}

@Composable
fun SwapFeeDisplay(
    quote: SwapQuote?,
    isGenesisHolder: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGenesisHolder) 
                TrueTapSuccess.copy(alpha = 0.05f) else TrueTapContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TrueTap Fee",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    val feePercent = if (isGenesisHolder) "0.25%" else "0.5%"
                    val feeAmount = quote?.exchangeFee?.toString() ?: "0.00"
                    
                    Text(
                        text = "$feePercent • $feeAmount ${quote?.inputToken?.symbol ?: ""}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                    
                    if (isGenesisHolder) {
                        Text(
                            text = "50% off standard rate!",
                            fontSize = 12.sp,
                            color = TrueTapSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "35% less than Phantom",
                            fontSize = 12.sp,
                            color = TrueTapSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            if (!isGenesisHolder) {
                HorizontalDivider(color = TrueTapTextInactive.copy(alpha = 0.2f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💎 Genesis NFT Holders",
                        fontSize = 12.sp,
                        color = TrueTapTextSecondary
                    )
                    
                    Text(
                        text = "Get 0.25% fees",
                        fontSize = 12.sp,
                        color = TrueTapPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SwapRouteDisplay(
    route: List<String>,
    modifier: Modifier = Modifier
) {
    if (route.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Route",
                        tint = TrueTapPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "Best Route Found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    route.forEachIndexed { index, dex ->
                        if (index > 0) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Arrow",
                                tint = TrueTapTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        
                        Surface(
                            color = TrueTapPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = dex,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TrueTapPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = "Jupiter found the best prices across ${route.size} DEX${if (route.size > 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = TrueTapTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun PriceImpactIndicator(
    priceImpact: Double,
    modifier: Modifier = Modifier
) {
    val (color, text, icon) = when {
        priceImpact < 0.1 -> Triple(TrueTapSuccess, "Excellent", Icons.AutoMirrored.Filled.TrendingUp)
        priceImpact < 0.5 -> Triple(Color(0xFFFFC107), "Good", Icons.AutoMirrored.Filled.TrendingFlat)
        priceImpact < 1.0 -> Triple(Color(0xFFFF9800), "Fair", Icons.Default.Warning)
        else -> Triple(TrueTapError, "High Impact", Icons.Default.Error)
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Price Impact",
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        
        Text(
            text = "$text (${String.format("%.2f", priceImpact)}%)",
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SwapStateIndicator(
    swapState: SwapState,
    modifier: Modifier = Modifier
) {
    when (swapState) {
        SwapState.PROCESSING -> {
            val infiniteTransition = rememberInfiniteTransition(label = "processing")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Processing",
                    tint = TrueTapPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
                
                Text(
                    text = "Processing swap...",
                    fontSize = 14.sp,
                    color = TrueTapPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        SwapState.SUCCESS -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = TrueTapSuccess,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = "Swap completed successfully!",
                    fontSize = 14.sp,
                    color = TrueTapSuccess,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        SwapState.ERROR -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = TrueTapError,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = "Swap failed",
                    fontSize = 14.sp,
                    color = TrueTapError,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        SwapState.IDLE -> {
            // No indicator when idle
        }
    }
}

@Composable
fun SwapConfirmationDialog(
    quote: SwapQuote,
    isGenesisHolder: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Confirm Swap",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                if (isGenesisHolder) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GenesisNFTBenefitsBadge(
                        isGenesisHolder = true,
                        tier = GenesisNFTTier.COMMON
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Swap details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("You pay:", color = TrueTapTextSecondary)
                    Text(
                        "${quote.inputAmount} ${quote.inputToken.symbol}",
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("You receive:", color = TrueTapTextSecondary)
                    Text(
                        "${quote.outputAmount} ${quote.outputToken.symbol}",
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                }
                
                HorizontalDivider()
                
                // Fee breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TrueTap Fee:", color = TrueTapTextSecondary)
                    Column(horizontalAlignment = Alignment.End) {
                        val feePercent = if (isGenesisHolder) "0.25%" else "0.5%"
                        Text(
                            "$feePercent (${quote.exchangeFee} ${quote.inputToken.symbol})",
                            fontWeight = FontWeight.Medium,
                            color = TrueTapTextPrimary,
                            fontSize = 14.sp
                        )
                        if (isGenesisHolder) {
                            Text(
                                "Genesis discount applied!",
                                fontSize = 12.sp,
                                color = TrueTapSuccess
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Network Fee:", color = TrueTapTextSecondary)
                    Text(
                        "~${quote.networkFee} SOL",
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Price Impact:", color = TrueTapTextSecondary)
                    PriceImpactIndicator(quote.priceImpact)
                }
                
                if (quote.route.isNotEmpty()) {
                    HorizontalDivider()
                    Column {
                        Text(
                            "Route:",
                            color = TrueTapTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            quote.route.joinToString(" → "),
                            fontSize = 12.sp,
                            color = TrueTapPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrueTapPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Confirm Swap",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = TrueTapTextSecondary
                )
            }
        },
        containerColor = TrueTapContainer,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun TokenSelectorChip(
    token: CryptoToken,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) TrueTapPrimary else TrueTapContainer
    val textColor = if (isSelected) Color.White else TrueTapTextPrimary
    
    Surface(
        modifier = modifier
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected) BorderStroke(1.dp, TrueTapTextInactive.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Token icon placeholder - you could add actual token icons here
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f) else TrueTapPrimary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = token.symbol.take(1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TrueTapPrimary
                )
            }
            
            Text(
                text = token.symbol,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}