package com.truetap.solana.seeker.ui.screens.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.ui.theme.LocalDynamicColors
import com.truetap.solana.seeker.ui.components.BottomNavItem
import com.truetap.solana.seeker.ui.components.TrueTapBottomNavigationBar
import com.truetap.solana.seeker.ui.components.rememberBottomNavHandler

/**
 * Settings Screen - Compose screen for app settings
 * Converted from React Native TSX to Kotlin Compose
 */

data class SettingsItem(
    val id: String,
    val title: String,
    val subtitle: String?,
    val type: SettingsItemType,
    val value: Any? = null,
    val options: List<String>? = null,
    val icon: ImageVector,
    val onPress: (() -> Unit)? = null,
    val onToggle: ((Boolean) -> Unit)? = null,
    val onSelect: ((String) -> Unit)? = null
)

data class SettingsSection(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val items: List<SettingsItem>
)

enum class SettingsItemType {
    TOGGLE,
    SELECT,
    NAVIGATE,
    ACTION
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Get accessibility settings and dynamic colors
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    // PIN Modal
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
    
    // Selection Modals
    uiState.showSelectionModal?.let { modalType ->
        SelectionDialog(
            title = getSelectionTitle(modalType),
            options = getSelectionOptions(modalType, uiState),
            selectedValue = getSelectedValue(modalType, uiState),
            onSelect = { value -> viewModel.selectOption(modalType, value) },
            onDismiss = viewModel::dismissSelectionModal
        )
    }
    
    // Error/Success dialogs
    uiState.dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = {
                Text(
                    text = message.title,
                    fontWeight = FontWeight.Bold,
                    color = if (message.isError) TrueTapError else TrueTapSuccess
                )
            },
            text = message.content?.let { { Text(it, color = TrueTapTextSecondary) } },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissDialog()
                        message.onConfirm?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (message.isError) dynamicColors.error else dynamicColors.success
                    )
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = message.onCancel?.let { cancel ->
                {
                    TextButton(onClick = {
                        viewModel.dismissDialog()
                        cancel()
                    }) {
                        Text("Cancel", color = dynamicColors.textSecondary)
                    }
                }
            },
            containerColor = dynamicColors.surface
        )
    }
    
    // Apply dynamic background color
    val backgroundModifier = if (dynamicColors.backgroundBrush != null) {
        modifier.fillMaxSize().background(dynamicColors.backgroundBrush)
    } else {
        modifier.fillMaxSize().background(dynamicColors.background)
    }
    
    Box(
        modifier = backgroundModifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Header
        Column(
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 16.dp
            )
        ) {
            Text(
                text = "Settings",
                fontSize = if (accessibility.largeButtonMode) 36.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                color = dynamicColors.textPrimary
            )
            Text(
                text = "Customize your TrueTap experience",
                fontSize = if (accessibility.largeButtonMode) 18.sp else 16.sp,
                color = dynamicColors.textSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Main content area
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(getSettingsSections(uiState, viewModel, onNavigateToWalletSuccess, onNavigateToWalletFailure, onNavigateToNFTSuccess, onNavigateToNFTFailure, onNavigateToGenesisToken, onNavigateToOnboarding)) { section ->
                SettingsSection(section = section)
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Bottom Navigation Bar
        TrueTapBottomNavigationBar(
            selectedTab = BottomNavItem.SETTINGS,
            onTabSelected = rememberBottomNavHandler(
                currentScreen = BottomNavItem.SETTINGS,
                onNavigateToHome = onNavigateToHome,
                onNavigateToSwap = onNavigateToSwap,
                onNavigateToNFTs = onNavigateToNFTs,
                onNavigateToContacts = onNavigateToContacts
            )
        )
        }
    }
}

@Composable
private fun SettingsSection(
    section: SettingsSection
) {
    Column {
        // Section Header
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = TrueTapPrimary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = section.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary
            )
        }
        
        // Section Content
        Surface(
            color = TrueTapContainer,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp
        ) {
            Column {
                section.items.forEachIndexed { index, item ->
                    SettingsItemRow(
                        item = item,
                        showDivider = index < section.items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItem,
    showDivider: Boolean
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    val itemPadding = if (accessibility.largeButtonMode) 20.dp else 16.dp
    val iconSize = if (accessibility.largeButtonMode) 24.dp else 20.dp
    val titleFontSize = if (accessibility.largeButtonMode) 18.sp else 16.sp
    val subtitleFontSize = if (accessibility.largeButtonMode) 16.sp else 14.sp
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = item.type != SettingsItemType.TOGGLE) {
                    when (item.type) {
                        SettingsItemType.ACTION -> item.onPress?.invoke()
                        SettingsItemType.NAVIGATE -> item.onPress?.invoke()
                        SettingsItemType.SELECT -> item.onPress?.invoke()
                        SettingsItemType.TOGGLE -> { /* Handled by Switch */ }
                    }
                }
                .padding(itemPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon and Text
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = dynamicColors.textSecondary,
                    modifier = Modifier.size(iconSize)
                )
                
                Column {
                    Text(
                        text = item.title,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Medium,
                        color = dynamicColors.textPrimary
                    )
                    item.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            fontSize = subtitleFontSize,
                            color = dynamicColors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            
            // Action/Value
            when (item.type) {
                SettingsItemType.TOGGLE -> {
                    Switch(
                        checked = item.value as? Boolean ?: false,
                        onCheckedChange = item.onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = dynamicColors.onPrimary,
                            checkedTrackColor = dynamicColors.primary,
                            uncheckedThumbColor = dynamicColors.textInactive,
                            uncheckedTrackColor = Color(0xFFE5E5E5)
                        )
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = dynamicColors.textInactive,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
        
        if (showDivider && !accessibility.simplifiedUIMode) {
            HorizontalDivider(
                color = dynamicColors.textInactive.copy(alpha = 0.2f),
                modifier = Modifier.padding(horizontal = itemPadding)
            )
        }
    }
}

@Composable
private fun PinDialog(
    step: PinStep,
    pinInput: String,
    confirmPin: String,
    onPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = dynamicColors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (step == PinStep.INPUT) "Enter PIN" else "Confirm PIN",
                        fontSize = if (accessibility.largeButtonMode) 22.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicColors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = dynamicColors.textSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (step == PinStep.INPUT) 
                        "Enter a 6-digit PIN code" 
                    else 
                        "Re-enter your PIN to confirm",
                    fontSize = if (accessibility.largeButtonMode) 18.sp else 16.sp,
                    color = dynamicColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = if (step == PinStep.INPUT) pinInput else confirmPin,
                    onValueChange = if (step == PinStep.INPUT) onPinChange else onConfirmPinChange,
                    placeholder = { Text("000000") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(if (accessibility.largeButtonMode) 64.dp else 56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dynamicColors.primary,
                        unfocusedBorderColor = Color(0xFFE5E5E5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth().height(if (accessibility.largeButtonMode) 56.dp else 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = dynamicColors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (step == PinStep.INPUT) "Continue" else "Confirm",
                        color = dynamicColors.onPrimary,
                        fontSize = if (accessibility.largeButtonMode) 18.sp else 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = LocalDynamicColors.current
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = dynamicColors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = if (accessibility.largeButtonMode) 22.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicColors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = dynamicColors.textSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Options
                LazyColumn {
                    items(options) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedValue == option) 
                                        dynamicColors.primary.copy(alpha = 0.1f) 
                                    else 
                                        Color.Transparent
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                color = if (selectedValue == option) dynamicColors.primary else dynamicColors.textPrimary,
                                fontWeight = if (selectedValue == option) FontWeight.Medium else FontWeight.Normal,
                                modifier = Modifier.padding(8.dp)
                            )
                            if (selectedValue == option) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = dynamicColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getSettingsSections(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onNavigateToWalletSuccess: () -> Unit,
    onNavigateToWalletFailure: () -> Unit,
    onNavigateToNFTSuccess: () -> Unit,
    onNavigateToNFTFailure: () -> Unit,
    onNavigateToGenesisToken: () -> Unit,
    onNavigateToOnboarding: () -> Unit
): List<SettingsSection> {
    return listOf(
        SettingsSection(
            id = "security",
            title = "Security",
            icon = Icons.Default.Security,
            items = listOf(
                SettingsItem(
                    id = "biometric",
                    title = "Biometric Security",
                    subtitle = "Use Face ID or Fingerprint to unlock",
                    type = SettingsItemType.TOGGLE,
                    value = uiState.biometricEnabled,
                    icon = Icons.Default.Fingerprint,
                    onToggle = viewModel::toggleBiometric
                ),
                SettingsItem(
                    id = "preferredBiometric",
                    title = "Preferred Method",
                    subtitle = if (uiState.preferredBiometric == "face") "Face ID" else "Fingerprint",
                    type = SettingsItemType.SELECT,
                    value = uiState.preferredBiometric,
                    options = listOf("Face ID", "Fingerprint"),
                    icon = Icons.Default.Face,
                    onPress = { viewModel.showSelectionModal(SelectionModalType.BIOMETRIC_METHOD) }
                ),
                SettingsItem(
                    id = "pinCode",
                    title = "PIN Code",
                    subtitle = if (uiState.pinCode.isNotEmpty()) "Change PIN" else "Set PIN",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.Lock,
                    onPress = viewModel::showPinModal
                )
            )
        ),
        SettingsSection(
            id = "wallet",
            title = "Wallet & Payments",
            icon = Icons.Default.AccountBalanceWallet,
            items = listOf(
                SettingsItem(
                    id = "linkedWallets",
                    title = "Linked Wallets",
                    subtitle = "${uiState.linkedWallets.count { it.connected }} connected",
                    type = SettingsItemType.NAVIGATE,
                    icon = Icons.Default.AccountBalanceWallet,
                    onPress = { viewModel.showWalletDialog() }
                ),
                SettingsItem(
                    id = "defaultCurrency",
                    title = "Default Currency",
                    subtitle = uiState.defaultCurrency,
                    type = SettingsItemType.SELECT,
                    value = uiState.defaultCurrency,
                    options = listOf("USD", "SOL", "USDC", "EUR"),
                    icon = Icons.Default.AttachMoney,
                    onPress = { viewModel.showSelectionModal(SelectionModalType.DEFAULT_CURRENCY) }
                )
            )
        ),
        SettingsSection(
            id = "personalization",
            title = "Personalization",
            icon = Icons.Default.ColorLens,
            items = listOf(
                SettingsItem(
                    id = "theme",
                    title = "Appearance",
                    subtitle = uiState.themeMode.replaceFirstChar { it.uppercase() },
                    type = SettingsItemType.SELECT,
                    value = uiState.themeMode,
                    options = listOf("Light", "Dark", "System"),
                    icon = Icons.Default.Brightness6,
                    onPress = { viewModel.showSelectionModal(SelectionModalType.THEME) }
                ),
                SettingsItem(
                    id = "language",
                    title = "Language",
                    subtitle = uiState.language,
                    type = SettingsItemType.SELECT,
                    value = uiState.language,
                    options = listOf("English", "Spanish", "French", "German"),
                    icon = Icons.Default.Language,
                    onPress = { viewModel.showSelectionModal(SelectionModalType.LANGUAGE) }
                )
            )
        ),
        SettingsSection(
            id = "accessibility",
            title = "Accessibility",
            icon = Icons.Default.Accessibility,
            items = listOf(
                SettingsItem(
                    id = "highContrast",
                    title = "High Contrast Mode",
                    subtitle = "Enhance text and button visibility",
                    type = SettingsItemType.TOGGLE,
                    value = uiState.highContrastMode,
                    icon = Icons.Default.Visibility,
                    onToggle = viewModel::toggleHighContrast
                ),
                SettingsItem(
                    id = "largeButtons",
                    title = "Large Button Mode",
                    subtitle = "Increase button sizes for easier tapping",
                    type = SettingsItemType.TOGGLE,
                    value = uiState.largeButtonMode,
                    icon = Icons.Default.TouchApp,
                    onToggle = viewModel::toggleLargeButtons
                ),
                SettingsItem(
                    id = "audioConfirmations",
                    title = "Audio Confirmations",
                    subtitle = "Play sounds for important actions",
                    type = SettingsItemType.TOGGLE,
                    value = uiState.audioConfirmations,
                    icon = Icons.Default.VolumeUp,
                    onToggle = viewModel::toggleAudioConfirmations
                ),
                SettingsItem(
                    id = "simplifiedUI",
                    title = "Simplified Interface",
                    subtitle = "Reduce visual complexity and animations",
                    type = SettingsItemType.TOGGLE,
                    value = uiState.simplifiedUIMode,
                    icon = Icons.Default.ViewList,
                    onToggle = viewModel::toggleSimplifiedUI
                )
            )
        ),
        SettingsSection(
            id = "privacy",
            title = "Legal & Privacy",
            icon = Icons.Default.Description,
            items = listOf(
                SettingsItem(
                    id = "privacyPolicy",
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.PrivacyTip,
                    onPress = { viewModel.showPrivacyPolicy() }
                ),
                SettingsItem(
                    id = "exportData",
                    title = "Export Activity Log",
                    subtitle = "Download your data",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.Download,
                    onPress = { viewModel.exportData() }
                ),
                SettingsItem(
                    id = "deleteData",
                    title = "Delete My Data",
                    subtitle = "Permanently delete all data",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.Delete,
                    onPress = { viewModel.confirmDeleteData() }
                )
            )
        ),
        SettingsSection(
            id = "devMode",
            title = "Developer Mode",
            icon = Icons.Default.BugReport,
            items = listOf(
                SettingsItem(
                    id = "walletSuccess",
                    title = "Wallet Success Screen",
                    subtitle = "View wallet connection success",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.CheckCircle,
                    onPress = { onNavigateToWalletSuccess() }
                ),
                SettingsItem(
                    id = "walletFailure",
                    title = "Wallet Failure Screen",
                    subtitle = "View wallet connection failure",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.Error,
                    onPress = { onNavigateToWalletFailure() }
                ),
                SettingsItem(
                    id = "nftSuccess",
                    title = "NFT Success Screen",
                    subtitle = "View NFT found success",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.Image,
                    onPress = { onNavigateToNFTSuccess() }
                ),
                SettingsItem(
                    id = "nftFailure",
                    title = "NFT Failure Screen",
                    subtitle = "View NFT not found",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.BrokenImage,
                    onPress = { onNavigateToNFTFailure() }
                ),
                SettingsItem(
                    id = "genesisToken",
                    title = "Genesis Token Screen",
                    subtitle = "View Genesis Token flow",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.Token,
                    onPress = { onNavigateToGenesisToken() }
                ),
                SettingsItem(
                    id = "onboarding",
                    title = "Onboarding Screen",
                    subtitle = "View onboarding flow",
                    type = SettingsItemType.ACTION,
                    icon = Icons.Default.School,
                    onPress = { onNavigateToOnboarding() }
                )
            )
        )
    )
}

private fun getSelectionTitle(modalType: SelectionModalType): String {
    return when (modalType) {
        SelectionModalType.BIOMETRIC_METHOD -> "Preferred Biometric Method"
        SelectionModalType.DEFAULT_CURRENCY -> "Default Currency"
        SelectionModalType.THEME -> "Appearance"
        SelectionModalType.LANGUAGE -> "Language"
    }
}

private fun getSelectionOptions(modalType: SelectionModalType, uiState: SettingsUiState): List<String> {
    return when (modalType) {
        SelectionModalType.BIOMETRIC_METHOD -> listOf("Face ID", "Fingerprint")
        SelectionModalType.DEFAULT_CURRENCY -> listOf("USD", "SOL", "USDC", "EUR")
        SelectionModalType.THEME -> listOf("Light", "Dark", "System")
        SelectionModalType.LANGUAGE -> listOf("English", "Spanish", "French", "German")
    }
}

private fun getSelectedValue(modalType: SelectionModalType, uiState: SettingsUiState): String {
    return when (modalType) {
        SelectionModalType.BIOMETRIC_METHOD -> if (uiState.preferredBiometric == "face") "Face ID" else "Fingerprint"
        SelectionModalType.DEFAULT_CURRENCY -> uiState.defaultCurrency
        SelectionModalType.THEME -> uiState.themeMode.replaceFirstChar { it.uppercase() }
        SelectionModalType.LANGUAGE -> uiState.language
    }
}

