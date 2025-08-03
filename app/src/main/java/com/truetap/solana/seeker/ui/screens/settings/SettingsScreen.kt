package com.truetap.solana.seeker.ui.screens.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

// Bottom Navigation Components
enum class BottomNavItem(val title: String, val icon: String) {
    HOME("Home", "house"),
    SWAP("Swap", "arrows-clockwise"),
    NFTS("NFTs", "image"),
    CONTACTS("Contacts", "users"),
    SETTINGS("Settings", "gear")
}

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
                        containerColor = if (message.isError) TrueTapError else TrueTapSuccess
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
                        Text("Cancel", color = TrueTapTextSecondary)
                    }
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TrueTapBackground, Color(0xFFF0ECE4))
                )
            )
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
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            Text(
                text = "Customize your TrueTap experience",
                fontSize = 16.sp,
                color = TrueTapTextSecondary,
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
        BottomNavigationBar(
            selectedTab = BottomNavItem.SETTINGS,
            onTabSelected = { tab ->
                when (tab) {
                    BottomNavItem.HOME -> onNavigateToHome()
                    BottomNavItem.SWAP -> onNavigateToSwap()
                    BottomNavItem.NFTS -> onNavigateToNFTs()
                    BottomNavItem.CONTACTS -> onNavigateToContacts()
                    BottomNavItem.SETTINGS -> { /* Already on Settings */ }
                }
            }
        )
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
                .padding(16.dp),
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
                    tint = TrueTapTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                
                Column {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextPrimary
                    )
                    item.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary,
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
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TrueTapPrimary,
                            uncheckedThumbColor = TrueTapTextInactive,
                            uncheckedTrackColor = Color(0xFFE5E5E5)
                        )
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TrueTapTextInactive,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        if (showDivider) {
            HorizontalDivider(
                color = Color(0xFFE5E5E5),
                modifier = Modifier.padding(horizontal = 16.dp)
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
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TrueTapContainer
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TrueTapTextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (step == PinStep.INPUT) 
                        "Enter a 6-digit PIN code" 
                    else 
                        "Re-enter your PIN to confirm",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary,
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TrueTapPrimary,
                        unfocusedBorderColor = Color(0xFFE5E5E5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrueTapPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (step == PinStep.INPUT) "Continue" else "Confirm",
                        color = Color.White,
                        fontSize = 16.sp,
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
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TrueTapContainer,
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TrueTapTextSecondary
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
                                        TrueTapPrimary.copy(alpha = 0.1f) 
                                    else 
                                        Color.Transparent
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                color = if (selectedValue == option) TrueTapPrimary else TrueTapTextPrimary,
                                fontWeight = if (selectedValue == option) FontWeight.Medium else FontWeight.Normal,
                                modifier = Modifier.padding(8.dp)
                            )
                            if (selectedValue == option) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = TrueTapPrimary,
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