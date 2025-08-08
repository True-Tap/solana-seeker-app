package com.truetap.solana.seeker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Unified Adaptive Bottom Navigation Component
 * Provides consistent navigation across all main screens with proper system navigation adaptation
 */

// Single source of truth for navigation items
enum class MainNavItem(
    val title: String, 
    val icon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Default.Home, "home"),
    SWAP("Swap", Icons.Default.SwapHoriz, "swap"),
    NFTS("NFTs", Icons.Default.Image, "nfts"),
    CONTACTS("Contacts", Icons.Default.People, "contacts"),
    SETTINGS("Settings", Icons.Default.Settings, "settings")
}


/**
 * Adaptive Bottom Navigation Bar
 * Automatically adjusts for gesture navigation, 3-button navigation, and visibility changes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveBottomNavigationBar(
    selectedTab: MainNavItem,
    onTabSelected: (MainNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    NavigationBar(
        modifier = modifier
            .fillMaxWidth(),
        containerColor = dynamicColors.surface,
        contentColor = dynamicColors.textPrimary,
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        MainNavItem.values().forEach { item ->
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
                        fontSize = if (accessibility.largeButtonMode) 13.sp else 11.sp,
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
                    indicatorColor = Color.Transparent  // Remove the orange circle indicator
                )
            )
        }
    }
}

/**
 * Main Screen Wrapper with Adaptive Navigation
 * Use this to wrap all main screens for consistent navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenScaffold(
    currentScreen: MainNavItem,
    onNavigate: (MainNavItem) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = {
            AdaptiveBottomNavigationBar(
                selectedTab = currentScreen,
                onTabSelected = onNavigate
            )
        },
        floatingActionButton = floatingActionButton,
        containerColor = Color.Transparent
    ) { paddingValues ->
        content(paddingValues)
    }
}

/**
 * Helper function to handle navigation consistently
 */
fun handleMainNavigation(
    item: MainNavItem,
    navController: androidx.navigation.NavController
) {
    navController.navigate(item.route) {
        // Pop up to the home destination to avoid building up a back stack
        popUpTo("home") {
            saveState = true
        }
        // Avoid multiple copies of the same destination
        launchSingleTop = true
        // Restore state when navigating back to a destination
        restoreState = true
    }
}