package com.truetap.solana.seeker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.ui.components.*
import com.truetap.solana.seeker.ui.components.layouts.*
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings

/**
 * SettingsScreen migrated to use TrueTap Design System
 * 
 * Provides a clean, organized settings interface using standardized components
 */

data class SettingsGroup(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val items: List<SettingsItemData>
)

data class SettingsItemData(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val type: SettingsItemType,
    val value: Any? = null,
    val enabled: Boolean = true,
    val onClick: (() -> Unit)? = null
)

enum class SettingsItemType {
    TOGGLE,
    NAVIGATION,
    ACTION,
    SELECTION
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToWalletSuccess: () -> Unit = {},
    onNavigateToWalletFailure: () -> Unit = {},
    onNavigateToNFTSuccess: () -> Unit = {},
    onNavigateToNFTFailure: () -> Unit = {},
    onNavigateToGenesisToken: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    // Settings groups organized by category
    val settingsGroups = remember(uiState) {
        listOf(
            // Security Settings
            SettingsGroup(
                title = "Security & Privacy",
                subtitle = "Protect your wallet and data",
                icon = Icons.Default.Security,
                items = listOf(
                    SettingsItemData(
                        title = "Biometric Authentication",
                        subtitle = if (uiState.biometricEnabled) "Enabled" else "Disabled",
                        icon = Icons.Default.Fingerprint,
                        type = SettingsItemType.TOGGLE,
                        value = uiState.biometricEnabled,
                        onClick = { viewModel.toggleBiometric() }
                    ),
                    SettingsItemData(
                        title = "PIN Code",
                        subtitle = if (uiState.pinCode.isNotEmpty()) "Set" else "Not set",
                        icon = Icons.Default.Lock,
                        type = SettingsItemType.ACTION,
                        onClick = { viewModel.showPinModal() }
                    ),
                    SettingsItemData(
                        title = "Auto-Lock",
                        subtitle = "Lock app after inactivity",
                        icon = Icons.Default.Timer,
                        type = SettingsItemType.SELECTION,
                        value = "5 minutes"
                    )
                )
            ),
            
            // Wallet Settings
            SettingsGroup(
                title = "Wallet & Network",
                subtitle = "Manage your wallet connections",
                icon = Icons.Default.AccountBalanceWallet,
                items = listOf(
                    SettingsItemData(
                        title = "Connected Wallets",
                        subtitle = "${uiState.linkedWallets.count { it.connected }} connected",
                        icon = Icons.Default.AccountBalanceWallet,
                        type = SettingsItemType.NAVIGATION,
                        onClick = { viewModel.showWalletDialog() }
                    ),
                    SettingsItemData(
                        title = "Default Currency",
                        subtitle = uiState.defaultCurrency,
                        icon = Icons.Default.AttachMoney,
                        type = SettingsItemType.SELECTION,
                        value = uiState.defaultCurrency
                    ),
                    SettingsItemData(
                        title = "Network",
                        subtitle = "Solana Mainnet",
                        icon = Icons.Default.Language,
                        type = SettingsItemType.NAVIGATION
                    )
                )
            ),
            
            // Appearance Settings
            SettingsGroup(
                title = "Appearance",
                subtitle = "Customize your experience",
                icon = Icons.Default.Palette,
                items = listOf(
                    SettingsItemData(
                        title = "Theme",
                        subtitle = uiState.themeMode.replaceFirstChar { it.uppercase() },
                        icon = Icons.Default.Brightness6,
                        type = SettingsItemType.SELECTION,
                        value = uiState.themeMode
                    ),
                    SettingsItemData(
                        title = "Language",
                        subtitle = uiState.language,
                        icon = Icons.Default.Language,
                        type = SettingsItemType.SELECTION,
                        value = uiState.language
                    ),
                    SettingsItemData(
                        title = "Large Text",
                        subtitle = "Increase text size",
                        icon = Icons.Default.FormatSize,
                        type = SettingsItemType.TOGGLE,
                        value = uiState.largeButtonMode,
                        onClick = { viewModel.toggleLargeButtons() }
                    )
                )
            ),
            
            // Accessibility Settings
            SettingsGroup(
                title = "Accessibility",
                subtitle = "Improve usability",
                icon = Icons.Default.Accessibility,
                items = listOf(
                    SettingsItemData(
                        title = "High Contrast",
                        subtitle = "Enhance visibility",
                        icon = Icons.Default.Visibility,
                        type = SettingsItemType.TOGGLE,
                        value = uiState.highContrastMode,
                        onClick = { viewModel.toggleHighContrast() }
                    ),
                    SettingsItemData(
                        title = "Audio Feedback",
                        subtitle = "Sound confirmations",
                        icon = Icons.Default.VolumeUp,
                        type = SettingsItemType.TOGGLE,
                        value = uiState.audioConfirmations,
                        onClick = { viewModel.toggleAudioConfirmations() }
                    ),
                    SettingsItemData(
                        title = "Simplified Interface",
                        subtitle = "Reduce visual complexity",
                        icon = Icons.Default.ViewList,
                        type = SettingsItemType.TOGGLE,
                        value = uiState.simplifiedUIMode,
                        onClick = { viewModel.toggleSimplifiedUI() }
                    )
                )
            ),
            
            // Data & Privacy Settings
            SettingsGroup(
                title = "Data & Privacy",
                subtitle = "Control your information",
                icon = Icons.Default.PrivacyTip,
                items = listOf(
                    SettingsItemData(
                        title = "Privacy Policy",
                        subtitle = "View our privacy policy",
                        icon = Icons.Default.Description,
                        type = SettingsItemType.ACTION,
                        onClick = { viewModel.showPrivacyPolicy() }
                    ),
                    SettingsItemData(
                        title = "Export Data",
                        subtitle = "Download your activity",
                        icon = Icons.Default.Download,
                        type = SettingsItemType.ACTION,
                        onClick = { viewModel.exportData() }
                    ),
                    SettingsItemData(
                        title = "Delete Data",
                        subtitle = "Remove all data",
                        icon = Icons.Default.Delete,
                        type = SettingsItemType.ACTION,
                        onClick = { viewModel.confirmDeleteData() }
                    )
                )
            ),
            
            // Developer Settings (if enabled)
            SettingsGroup(
                title = "Developer",
                subtitle = "Testing and debugging",
                icon = Icons.Default.BugReport,
                items = listOf(
                    SettingsItemData(
                        title = "Wallet Success Test",
                        subtitle = "Test success flow",
                        icon = Icons.Default.CheckCircle,
                        type = SettingsItemType.ACTION,
                        onClick = onNavigateToWalletSuccess
                    ),
                    SettingsItemData(
                        title = "Wallet Failure Test",
                        subtitle = "Test error flow",
                        icon = Icons.Default.Error,
                        type = SettingsItemType.ACTION,
                        onClick = onNavigateToWalletFailure
                    ),
                    SettingsItemData(
                        title = "NFT Success Test",
                        subtitle = "Test NFT success",
                        icon = Icons.Default.Image,
                        type = SettingsItemType.ACTION,
                        onClick = onNavigateToNFTSuccess
                    ),
                    SettingsItemData(
                        title = "Genesis Token Test",
                        subtitle = "Test Genesis flow",
                        icon = Icons.Default.Token,
                        type = SettingsItemType.ACTION,
                        onClick = onNavigateToGenesisToken
                    )
                )
            )
        )
    }
    
    // Use standardized screen template
    TrueTapScreenTemplate(
        title = "Settings",
        subtitle = "Customize your TrueTap experience",
        currentTab = BottomNavItem.SETTINGS,
        onNavigateToHome = onNavigateToHome,
        onNavigateToSwap = onNavigateToSwap,
        onNavigateToNFTs = onNavigateToNFTs,
        onNavigateToContacts = onNavigateToContacts,
        onNavigateToSettings = { /* Already on settings */ },
        modifier = modifier
    ) {
        // Settings groups
        items(settingsGroups) { group ->
            SettingsGroupCard(
                group = group,
                onItemClick = { item ->
                    item.onClick?.invoke()
                }
            )
        }
        
        // App Info Section
        item {
            AppInfoCard()
        }
    }
    
    // Show PIN modal if needed
    if (uiState.showPinModal) {
        PinDialog(
            step = uiState.pinStep,
            pinInput = uiState.pinInput,
            confirmPin = uiState.confirmPin,
            onPinChange = viewModel::setPinInput,
            onConfirmPinChange = viewModel::setConfirmPin,
            onSubmit = viewModel::submitPin,
            onDismiss = viewModel::dismissPinModal
        )
    }
    
    // Show selection modals if needed
    uiState.showSelectionModal?.let { modalType ->
        SelectionDialog(
            title = getSelectionTitle(modalType),
            options = getSelectionOptions(modalType, uiState),
            selectedValue = getSelectedValue(modalType, uiState),
            onSelect = { value -> viewModel.selectOption(modalType, value) },
            onDismiss = viewModel::dismissSelectionModal
        )
    }
    
    // Show dialog messages if needed
    uiState.dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = {
                Text(
                    text = message.title,
                    color = if (message.isError) dynamicColors.error else dynamicColors.textPrimary
                )
            },
            text = message.content?.let { { Text(it) } },
            confirmButton = {
                TrueTapButton(
                    text = "OK",
                    onClick = {
                        viewModel.dismissDialog()
                        message.onConfirm?.invoke()
                    },
                    style = if (message.isError) TrueTapButtonStyle.SECONDARY else TrueTapButtonStyle.PRIMARY
                )
            },
            dismissButton = message.onCancel?.let { cancel ->
                {
                    TrueTapButton(
                        text = "Cancel",
                        onClick = {
                            viewModel.dismissDialog()
                            cancel()
                        },
                        style = TrueTapButtonStyle.TEXT
                    )
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupCard(
    group: SettingsGroup,
    onItemClick: (SettingsItemData) -> Unit
) {
    TrueTapCard {
        Column {
            // Group header
            TrueTapSectionHeader(
                title = group.title,
                subtitle = group.subtitle
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
            
            // Group items
            group.items.forEachIndexed { index, item ->
                SettingsItem(
                    item = item,
                    onClick = { onItemClick(item) }
                )
                
                // Add divider between items (except last)
                if (index < group.items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = TrueTapSpacing.md),
                        color = getDynamicColors().outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    item: SettingsItemData,
    onClick: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    TrueTapListItem(
        title = item.title,
        subtitle = item.subtitle,
        leadingIcon = item.icon,
        onClick = if (item.type != SettingsItemType.TOGGLE) onClick else null,
        trailingContent = {
            when (item.type) {
                SettingsItemType.TOGGLE -> {
                    Switch(
                        checked = item.value as? Boolean ?: false,
                        onCheckedChange = { onClick() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = dynamicColors.onPrimary,
                            checkedTrackColor = dynamicColors.primary,
                            uncheckedThumbColor = dynamicColors.textInactive,
                            uncheckedTrackColor = dynamicColors.outline
                        )
                    )
                }
                SettingsItemType.NAVIGATION -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = dynamicColors.textSecondary
                    )
                }
                SettingsItemType.SELECTION -> {
                    Row {
                        Text(
                            text = item.value?.toString() ?: "",
                            style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                            color = dynamicColors.textSecondary
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select",
                            tint = dynamicColors.textSecondary
                        )
                    }
                }
                SettingsItemType.ACTION -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Action",
                        tint = dynamicColors.textSecondary
                    )
                }
            }
        }
    )
}

@Composable
private fun AppInfoCard() {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    TrueTapCard(style = TrueTapCardStyle.OUTLINED) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "TrueTap",
                style = getDynamicTypography(accessibility.largeButtonMode).headlineSmall,
                color = dynamicColors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.xs))
            
            Text(
                text = "Version 1.0.0",
                style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                color = dynamicColors.textSecondary
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
            
            Text(
                text = "Made with ❤️ for Solana",
                style = getDynamicTypography(accessibility.largeButtonMode).bodySmall,
                color = dynamicColors.textTertiary
            )
        }
    }
}

// Placeholder functions for existing ViewModel methods
@Composable
private fun PinDialog(
    step: Any,
    pinInput: String,
    confirmPin: String,
    onPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation would use existing PinDialog from original SettingsScreen
    // This is a placeholder for the migration
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Implementation would use existing SelectionDialog from original SettingsScreen
    // This is a placeholder for the migration
}

private fun getSelectionTitle(modalType: Any): String {
    // Implementation from original SettingsScreen
    return "Selection"
}

private fun getSelectionOptions(modalType: Any, uiState: Any): List<String> {
    // Implementation from original SettingsScreen
    return listOf("Option 1", "Option 2")
}

private fun getSelectedValue(modalType: Any, uiState: Any): String {
    // Implementation from original SettingsScreen
    return "Option 1"
}