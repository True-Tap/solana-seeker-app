/**
 * Payment Screen - TrueTap
 * NFC payment interface placeholder
 * Kotlin Compose implementation
 */

package com.truetap.solana.seeker.ui.screens.payment

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*

// Bottom Navigation Components
enum class BottomNavItem(val title: String, val icon: String) {
    HOME("Home", "house"),
    SWAP("Swap", "arrows-clockwise"),
    NFTS("NFTs", "image"),
    CONTACTS("Contacts", "users"),
    SETTINGS("Settings", "gear")
}

data class SwapToken(
    val id: String,
    val name: String,
    val symbol: String,
    val price: String,
    val change24h: String,
    val isPositive: Boolean,
    val icon: String = "🪙"
)

@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BottomNavItem.SWAP) }
    
    // Sample swap tokens
    val swapTokens = remember {
        listOf(
            SwapToken("sol", "Solana", "SOL", "$98.45", "+5.2%", true, "🟣"),
            SwapToken("usdc", "USD Coin", "USDC", "$1.00", "0.0%", true, "🔵"),
            SwapToken("usdt", "Tether", "USDT", "$1.00", "0.0%", true, "🟢"),
            SwapToken("bonk", "Bonk", "BONK", "$0.000023", "+12.8%", true, "🟡"),
            SwapToken("jup", "Jupiter", "JUP", "$0.85", "-2.1%", false, "🟠")
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        // Main content area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                
                // Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Swap",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary
                        )
                        Text(
                            text = "Trade tokens instantly",
                            fontSize = 16.sp,
                            color = TrueTapTextSecondary
                        )
                    }
                    
                    IconButton(
                        onClick = { /* Settings or refresh */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(TrueTapContainer, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TrueTapTextSecondary
                        )
                    }
                }
            }
            
            item {
                // Quick Swap Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Quick Swap",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // From Token
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🟣",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "SOL",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TrueTapTextPrimary
                                    )
                                    Text(
                                        text = "Solana",
                                        fontSize = 14.sp,
                                        color = TrueTapTextSecondary
                                    )
                                }
                            }
                            
                            Text(
                                text = "1.0",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrueTapTextPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Swap Arrow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap",
                                tint = TrueTapPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // To Token
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔵",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "USDC",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TrueTapTextPrimary
                                    )
                                    Text(
                                        text = "USD Coin",
                                        fontSize = 14.sp,
                                        color = TrueTapTextSecondary
                                    )
                                }
                            }
                            
                            Text(
                                text = "98.45",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrueTapTextPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Swap Button
                        Button(
                            onClick = { /* Execute swap */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Swap SOL → USDC",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
            
            item {
                // Popular Tokens Section
                Column {
                    Text(
                        text = "Popular Tokens",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(swapTokens) { token ->
                            TokenCard(token = token)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Bottom Navigation Bar
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
                when (tab) {
                    BottomNavItem.HOME -> onNavigateToHome()
                    BottomNavItem.SWAP -> { /* Already on swap */ }
                    BottomNavItem.NFTS -> onNavigateToNFTs()
                    BottomNavItem.CONTACTS -> onNavigateToContacts()
                    BottomNavItem.SETTINGS -> onNavigateToSettings()
                }
            }
        )
    }
}

@Composable
private fun TokenCard(token: SwapToken) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = token.icon,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = token.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = token.symbol,
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = token.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = token.change24h,
                    fontSize = 14.sp,
                    color = if (token.isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TrueTapContainer,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem.values().forEach { item ->
                BottomNavButton(
                    item = item,
                    isSelected = selectedTab == item,
                    onClick = { onTabSelected(item) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavButton(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with filled orange when selected
        Icon(
            imageVector = when (item) {
                BottomNavItem.HOME -> Icons.Default.Home
                BottomNavItem.SWAP -> Icons.Default.SwapHoriz
                BottomNavItem.NFTS -> Icons.Default.Image
                BottomNavItem.CONTACTS -> Icons.Default.People
                BottomNavItem.SETTINGS -> Icons.Default.Settings
            },
            contentDescription = item.title,
            tint = if (isSelected) TrueTapPrimary else TrueTapTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = item.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) TrueTapPrimary else TrueTapTextSecondary
        )
    }
}