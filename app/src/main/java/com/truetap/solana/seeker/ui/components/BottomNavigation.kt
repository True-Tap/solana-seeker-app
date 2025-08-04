package com.truetap.solana.seeker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.ui.theme.getDynamicColors

/**
 * Shared Bottom Navigation Component
 * Provides consistent navigation across all main screens with system navigation bar adaptation
 */

enum class BottomNavItem(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SWAP("Swap", Icons.Default.SwapHoriz),
    NFTS("NFTs", Icons.Default.Image),
    CONTACTS("Contacts", Icons.Default.People),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun TrueTapBottomNavigationBar(
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    // Adaptive padding for system navigation bars (back button, home button, etc.)
    val systemBarPadding = WindowInsets.navigationBars.asPaddingValues()
    
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(systemBarPadding), // Adaptive system bar padding
        containerColor = dynamicColors.surface,
        contentColor = dynamicColors.textPrimary,
        tonalElevation = 8.dp
    ) {
        BottomNavItem.values().forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = "${item.title} tab",
                        modifier = Modifier.size(
                            if (accessibility.largeButtonMode) 28.dp else 24.dp
                        )
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = if (accessibility.largeButtonMode) 14.sp else 12.sp,
                        fontWeight = if (selectedTab == item) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                },
                selected = selectedTab == item,
                onClick = { onTabSelected(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = dynamicColors.primary,
                    selectedTextColor = dynamicColors.primary,
                    unselectedIconColor = dynamicColors.textSecondary,
                    unselectedTextColor = dynamicColors.textSecondary,
                    indicatorColor = dynamicColors.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

/**
 * Legacy Bottom Navigation Bar (for backward compatibility during migration)
 * All screens have been migrated to use TrueTapBottomNavigationBar directly
 */
@Composable
@Deprecated("Use TrueTapBottomNavigationBar directly", ReplaceWith("TrueTapBottomNavigationBar(selectedTab, onTabSelected, modifier)"))
fun BottomNavigationBar(
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    TrueTapBottomNavigationBar(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        modifier = modifier
    )
}

/**
 * Extension function to get current screen's BottomNavItem
 */
fun String.toBottomNavItem(): BottomNavItem? {
    return when (this.lowercase()) {
        "home", "dashboard" -> BottomNavItem.HOME
        "swap", "payment", "enhancedswap" -> BottomNavItem.SWAP
        "nfts", "nft" -> BottomNavItem.NFTS
        "contacts", "contact" -> BottomNavItem.CONTACTS
        "settings", "setting" -> BottomNavItem.SETTINGS
        else -> null
    }
}

/**
 * Helper function to handle navigation logic consistently
 */
@Composable
fun rememberBottomNavHandler(
    currentScreen: BottomNavItem,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
): (BottomNavItem) -> Unit {
    return remember(currentScreen) { { tab ->
        if (tab != currentScreen) {
            when (tab) {
                BottomNavItem.HOME -> onNavigateToHome()
                BottomNavItem.SWAP -> onNavigateToSwap()
                BottomNavItem.NFTS -> onNavigateToNFTs()
                BottomNavItem.CONTACTS -> onNavigateToContacts()
                BottomNavItem.SETTINGS -> onNavigateToSettings()
            }
        }
    }}
}