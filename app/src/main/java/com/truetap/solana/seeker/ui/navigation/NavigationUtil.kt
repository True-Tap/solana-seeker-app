package com.truetap.solana.seeker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.truetap.solana.seeker.ui.components.MainNavItem

/**
 * Navigation utility functions for handling main screen navigation
 */

/**
 * Creates a navigation handler for main screens
 */
@Composable
fun rememberMainNavigationHandler(
    navController: NavController
): (MainNavItem) -> Unit {
    return remember(navController) { { item ->
        when (item) {
            MainNavItem.HOME -> {
                // Only navigate if we're not already on home
                if (navController.currentDestination?.route != "home") {
                    navController.navigate("home") {
                        popUpTo("home") {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            MainNavItem.SWAP -> navController.navigate("swap") {
                popUpTo("home") {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            MainNavItem.NFTS -> navController.navigate("nfts") {
                popUpTo("home") {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            MainNavItem.CONTACTS -> navController.navigate("contacts") {
                popUpTo("home") {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            MainNavItem.SETTINGS -> navController.navigate("settings") {
                popUpTo("home") {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }}
}

/**
 * Extension function to convert route to MainNavItem
 */
fun String.toMainNavItem(): MainNavItem? {
    return when (this) {
        "home", "dashboard" -> MainNavItem.HOME
        "swap", "payment", "enhanced_swap" -> MainNavItem.SWAP
        "nfts", "nft" -> MainNavItem.NFTS
        "contacts", "contact" -> MainNavItem.CONTACTS
        "settings", "setting" -> MainNavItem.SETTINGS
        else -> null
    }
}