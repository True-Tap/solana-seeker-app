package com.truetap.solana.seeker.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.data.models.WalletType
import com.truetap.solana.seeker.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    onNavigateBack: () -> Unit,
    onContactAdded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    var contactName by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(AddContactMethod.MANUAL) }
    var wallets by remember { mutableStateOf(listOf<ContactWalletEntry>()) }
    var showValidationErrors by remember { mutableStateOf(false) }

    // Initialize with one wallet entry
    LaunchedEffect(Unit) {
        wallets = listOf(
            ContactWalletEntry(
                id = UUID.randomUUID().toString(),
                name = "",
                address = "",
                type = WalletType.PERSONAL
            )
        )
    }

    val isFormValid = contactName.isNotBlank() && 
                     wallets.isNotEmpty() && 
                     wallets.all { it.address.isNotBlank() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Status Bar Spacing
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TrueTapTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Add Contact",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                // Save button
                TextButton(
                    onClick = {
                        if (isFormValid) {
                            val newContact = ModernContact(
                                id = UUID.randomUUID().toString(),
                                name = contactName.trim(),
                                initials = contactName.trim().split(" ")
                                    .take(2)
                                    .map { it.first().uppercaseChar() }
                                    .joinToString(""),
                                wallets = wallets.map { walletEntry ->
                                    ContactWallet(
                                        id = walletEntry.id,
                                        name = walletEntry.name.ifBlank { "Main Wallet" },
                                        address = walletEntry.address.trim(),
                                        type = walletEntry.type
                                    )
                                },
                                isFavorite = false
                            )
                            viewModel.addContact(newContact)
                            onContactAdded()
                        } else {
                            showValidationErrors = true
                        }
                    },
                    enabled = isFormValid
                ) {
                    Text(
                        text = "Save",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isFormValid) TrueTapPrimary else TrueTapTextInactive
                    )
                }
            }
            
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Contact Method Selection
                item {
                    ContactMethodSection(
                        selectedMethod = selectedMethod,
                        onMethodSelected = { selectedMethod = it }
                    )
                }
                
                // Manual Entry Form (shown for all methods for now)
                if (selectedMethod == AddContactMethod.MANUAL) {
                    item {
                        ContactDetailsSection(
                            contactName = contactName,
                            onContactNameChange = { contactName = it },
                            showValidationErrors = showValidationErrors
                        )
                    }
                    
                    item {
                        WalletsSection(
                            wallets = wallets,
                            onWalletsChange = { wallets = it },
                            showValidationErrors = showValidationErrors
                        )
                    }
                }
                
                // Placeholder for other methods
                if (selectedMethod != AddContactMethod.MANUAL) {
                    item {
                        ComingSoonSection(method = selectedMethod)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactMethodSection(
    selectedMethod: AddContactMethod,
    onMethodSelected: (AddContactMethod) -> Unit
) {
    Column {
        Text(
            text = "How would you like to add this contact?",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TrueTapTextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Method Grid - 2x2 for first 4, then full width for manual
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddContactMethodCard(
                    method = AddContactMethod.NFC,
                    title = "NFC Tap",
                    subtitle = "Tap devices together",
                    icon = Icons.Default.Nfc,
                    isSelected = selectedMethod == AddContactMethod.NFC,
                    onClick = { onMethodSelected(AddContactMethod.NFC) },
                    modifier = Modifier.weight(1f)
                )
                
                AddContactMethodCard(
                    method = AddContactMethod.QR_CODE,
                    title = "QR Code",
                    subtitle = "Scan QR code",
                    icon = Icons.Default.QrCode,
                    isSelected = selectedMethod == AddContactMethod.QR_CODE,
                    onClick = { onMethodSelected(AddContactMethod.QR_CODE) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddContactMethodCard(
                    method = AddContactMethod.BLUETOOTH,
                    title = "Bluetooth",
                    subtitle = "Connect via Bluetooth",
                    icon = Icons.Default.Bluetooth,
                    isSelected = selectedMethod == AddContactMethod.BLUETOOTH,
                    onClick = { onMethodSelected(AddContactMethod.BLUETOOTH) },
                    modifier = Modifier.weight(1f)
                )
                
                AddContactMethodCard(
                    method = AddContactMethod.SEND_LINK,
                    title = "Send Link",
                    subtitle = "Share connection link",
                    icon = Icons.Default.Share,
                    isSelected = selectedMethod == AddContactMethod.SEND_LINK,
                    onClick = { onMethodSelected(AddContactMethod.SEND_LINK) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            AddContactMethodCard(
                method = AddContactMethod.MANUAL,
                title = "Manual Entry",
                subtitle = "Enter contact details manually",
                icon = Icons.Default.Edit,
                isSelected = selectedMethod == AddContactMethod.MANUAL,
                onClick = { onMethodSelected(AddContactMethod.MANUAL) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ContactDetailsSection(
    contactName: String,
    onContactNameChange: (String) -> Unit,
    showValidationErrors: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Contact Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = contactName,
                onValueChange = onContactNameChange,
                label = { Text("Contact Name *") },
                placeholder = { Text("Enter contact name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrueTapPrimary,
                    focusedLabelColor = TrueTapPrimary,
                    cursorColor = TrueTapPrimary,
                    errorBorderColor = TrueTapError,
                    errorLabelColor = TrueTapError
                ),
                shape = RoundedCornerShape(12.dp),
                isError = showValidationErrors && contactName.isBlank(),
                supportingText = if (showValidationErrors && contactName.isBlank()) {
                    { Text("Contact name is required", color = TrueTapError) }
                } else null
            )
        }
    }
}

@Composable
private fun WalletsSection(
    wallets: List<ContactWalletEntry>,
    onWalletsChange: (List<ContactWalletEntry>) -> Unit,
    showValidationErrors: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallets",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                
                if (wallets.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            val newWallet = ContactWalletEntry(
                                id = UUID.randomUUID().toString(),
                                name = "",
                                address = "",
                                type = WalletType.PERSONAL
                            )
                            onWalletsChange(wallets + newWallet)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add wallet",
                            tint = TrueTapPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Wallet",
                            color = TrueTapPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            wallets.forEachIndexed { index, wallet ->
                WalletEntryCard(
                    wallet = wallet,
                    onWalletChange = { updatedWallet ->
                        val updatedWallets = wallets.toMutableList()
                        updatedWallets[index] = updatedWallet
                        onWalletsChange(updatedWallets)
                    },
                    onRemoveWallet = if (wallets.size > 1) {
                        {
                            onWalletsChange(wallets.filterIndexed { i, _ -> i != index })
                        }
                    } else null,
                    showValidationErrors = showValidationErrors,
                    modifier = Modifier.padding(bottom = if (index < wallets.size - 1) 16.dp else 0.dp)
                )
            }
        }
    }
}

@Composable
private fun WalletEntryCard(
    wallet: ContactWalletEntry,
    onWalletChange: (ContactWalletEntry) -> Unit,
    onRemoveWallet: (() -> Unit)?,
    showValidationErrors: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TrueTapBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallet ${if (wallet.name.isNotBlank()) "- ${wallet.name}" else ""}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextPrimary
                )
                
                onRemoveWallet?.let {
                    IconButton(
                        onClick = it,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove wallet",
                            tint = TrueTapError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = wallet.name,
                onValueChange = { onWalletChange(wallet.copy(name = it)) },
                label = { Text("Wallet Name") },
                placeholder = { Text("Main Wallet") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrueTapPrimary,
                    focusedLabelColor = TrueTapPrimary,
                    cursorColor = TrueTapPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = wallet.address,
                onValueChange = { onWalletChange(wallet.copy(address = it)) },
                label = { Text("Wallet Address *") },
                placeholder = { Text("Enter Solana address...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TrueTapPrimary,
                    focusedLabelColor = TrueTapPrimary,
                    cursorColor = TrueTapPrimary,
                    errorBorderColor = TrueTapError,
                    errorLabelColor = TrueTapError
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 2,
                isError = showValidationErrors && wallet.address.isBlank(),
                supportingText = if (showValidationErrors && wallet.address.isBlank()) {
                    { Text("Wallet address is required", color = TrueTapError) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Wallet Type Selector
            Text(
                text = "Wallet Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TrueTapTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WalletType.values().forEach { type ->
                    FilterChip(
                        onClick = { onWalletChange(wallet.copy(type = type)) },
                        label = { 
                            Text(
                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp
                            )
                        },
                        selected = wallet.type == type,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TrueTapPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = TrueTapContainer,
                            labelColor = TrueTapTextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ComingSoonSection(method: AddContactMethod) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (method) {
                    AddContactMethod.NFC -> Icons.Default.Nfc
                    AddContactMethod.BLUETOOTH -> Icons.Default.Bluetooth
                    AddContactMethod.QR_CODE -> Icons.Default.QrCode
                    AddContactMethod.SEND_LINK -> Icons.Default.Share
                    else -> Icons.Default.Construction
                },
                contentDescription = null,
                tint = TrueTapTextSecondary,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Coming Soon",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${method.name.lowercase().replaceFirstChar { it.uppercase() }} contact adding will be available in a future update.",
                fontSize = 14.sp,
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddContactMethodCard(
    method: AddContactMethod,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TrueTapPrimary.copy(alpha = 0.1f) else TrueTapContainer
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TrueTapPrimary) else null,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) TrueTapPrimary else TrueTapTextSecondary,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) TrueTapPrimary else TrueTapTextPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// Data class for wallet entry in the form
data class ContactWalletEntry(
    val id: String,
    val name: String,
    val address: String,
    val type: WalletType
)