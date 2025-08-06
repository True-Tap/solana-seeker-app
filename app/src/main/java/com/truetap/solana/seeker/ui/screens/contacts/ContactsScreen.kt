package com.truetap.solana.seeker.ui.screens.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.ui.components.*
import com.truetap.solana.seeker.ui.components.layouts.*
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.data.models.WalletType

/**
 * ContactsScreen migrated to use TrueTap Design System
 * 
 * Clean, searchable contacts interface with consistent styling
 */

@Composable
fun ContactsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddContact: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<ModernContact?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<ModernContact?>(null) }
    var contactToEdit by remember { mutableStateOf<ModernContact?>(null) }
    var walletToEdit by remember { mutableStateOf<Pair<ModernContact, ContactWallet>?>(null) }
    var walletToDelete by remember { mutableStateOf<Pair<ModernContact, ContactWallet>?>(null) }
    var contactToAddWallet by remember { mutableStateOf<ModernContact?>(null) }
    
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    // Sample contacts data
    val contacts = remember {
        mutableStateListOf(
            ModernContact(
                id = "1",
                name = "Charlie Brown",
                initials = "CB",
                wallets = listOf(
                    ContactWallet("1", "Main Wallet", "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM", WalletType.PERSONAL),
                    ContactWallet("2", "Savings", "8VzCXwBbmkg9ZTbNMqUxvQRAyrZzDsGYdLVL9zYtBXXN", WalletType.PERSONAL)
                ),
                isFavorite = true
            ),
            ModernContact(
                id = "2",
                name = "Diana Prince",
                initials = "DP",
                wallets = listOf(
                    ContactWallet("3", "Business", "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU", WalletType.BUSINESS)
                ),
                isFavorite = true
            ),
            ModernContact(
                id = "3",
                name = "Alice Johnson",
                initials = "AJ",
                wallets = listOf(
                    ContactWallet("4", "Personal", "5dSHdvJBQ38YuuHdKHDHFLhMhLCvdV7xB5QH5Y8z9CXD", WalletType.PERSONAL)
                ),
                isFavorite = false
            ),
            ModernContact(
                id = "4",
                name = "Bob Smith",
                initials = "BS",
                wallets = listOf(
                    ContactWallet("5", "Trading", "3mKQrv8fHpPQHhcQdAKYzuG8SN7eCPThjGhNkEKvXX5C", WalletType.PERSONAL)
                ),
                isFavorite = false
            )
        )
    }
    
    // Filter and sort contacts
    val filteredContacts = contacts.filter { contact ->
        if (searchQuery.isEmpty()) true
        else contact.name.contains(searchQuery, ignoreCase = true) ||
             contact.wallets.any { it.address.contains(searchQuery, ignoreCase = true) }
    }.sortedBy { it.name }
    
    val favoriteContacts = filteredContacts.filter { it.isFavorite }
    val regularContacts = filteredContacts
    
    // Use searchable screen template
    TrueTapSearchableScreenTemplate(
        title = "Contacts",
        currentTab = BottomNavItem.CONTACTS,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        searchPlaceholder = "Search contacts...",
        onNavigateToHome = onNavigateToHome,
        onNavigateToSwap = onNavigateToSwap,
        onNavigateToNFTs = onNavigateToNFTs,
        onNavigateToContacts = { /* Already on contacts */ },
        onNavigateToSettings = onNavigateToSettings,
        floatingActionButton = {
            TrueTapButton(
                text = "Add Contact",
                onClick = onNavigateToAddContact,
                icon = Icons.Default.Add
            )
        },
        modifier = modifier
    ) {
        // Favorites Section
        if (favoriteContacts.isNotEmpty()) {
            item {
                TrueTapSectionHeader(
                    title = "Favorites",
                    subtitle = "${favoriteContacts.size} favorite contacts"
                )
            }
            
            item {
                FavoritesRow(
                    favorites = favoriteContacts,
                    onContactClick = { selectedContact = it }
                )
            }
        }
        
        // All Contacts Section
        if (regularContacts.isNotEmpty()) {
            item {
                TrueTapSectionHeader(
                    title = "All Contacts",
                    subtitle = "${regularContacts.size} contacts",
                    modifier = Modifier.padding(
                        top = if (favoriteContacts.isNotEmpty()) TrueTapSpacing.lg else 0.dp
                    )
                )
            }
            
            // Contact list
            items(regularContacts) { contact ->
                ContactListItem(
                    contact = contact,
                    onContactClick = { selectedContact = contact },
                    onFavoriteClick = { 
                        val index = contacts.indexOfFirst { it.id == contact.id }
                        if (index != -1) {
                            contacts[index] = contact.copy(isFavorite = !contact.isFavorite)
                        }
                    }
                )
            }
        } else {
            // Empty state
            item {
                TrueTapEmptyState(
                    title = if (searchQuery.isNotEmpty()) "No contacts found" else "No contacts yet",
                    description = if (searchQuery.isNotEmpty()) 
                        "Try a different search term" 
                    else 
                        "Add contacts to make payments easier",
                    icon = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.Groups,
                    action = {
                        if (searchQuery.isEmpty()) {
                            TrueTapButton(
                                text = "Add Contact",
                                onClick = onNavigateToAddContact,
                                icon = Icons.Default.Add
                            )
                        }
                    }
                )
            }
        }
    }
    
    // Contact Details Modal
    selectedContact?.let { contact ->
        ContactDetailModal(
            contact = contact,
            onDismiss = { selectedContact = null },
            onToggleFavorite = { 
                val index = contacts.indexOfFirst { it.id == contact.id }
                if (index != -1) {
                    contacts[index] = contact.copy(isFavorite = !contact.isFavorite)
                }
                selectedContact = null
            },
            onDeleteContact = { 
                showDeleteConfirmation = contact
                selectedContact = null
            },
            onEditContact = {
                contactToEdit = contact
                selectedContact = null
            },
            onEditWallet = { wallet ->
                walletToEdit = Pair(contact, wallet)
                selectedContact = null
            },
            onDeleteWallet = { wallet ->
                walletToDelete = Pair(contact, wallet)
                selectedContact = null
            },
            onAddWallet = {
                contactToAddWallet = contact
                selectedContact = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteConfirmation?.let { contact ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = {
                Text(
                    text = "Delete Contact",
                    fontWeight = FontWeight.Bold,
                    color = dynamicColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${contact.name}? This action cannot be undone.",
                    color = dynamicColors.textSecondary
                )
            },
            confirmButton = {
                TrueTapButton(
                    text = "Delete",
                    onClick = {
                        contacts.removeIf { it.id == contact.id }
                        showDeleteConfirmation = null
                    },
                    style = TrueTapButtonStyle.SECONDARY // Using secondary for destructive action
                )
            },
            dismissButton = {
                TrueTapButton(
                    text = "Cancel",
                    onClick = { showDeleteConfirmation = null },
                    style = TrueTapButtonStyle.TEXT
                )
            }
        )
    }
    
    // Edit Contact Dialog
    contactToEdit?.let { contact ->
        var editedName by remember { mutableStateOf(contact.name) }
        
        AlertDialog(
            onDismissRequest = { contactToEdit = null },
            title = {
                Text(
                    text = "Edit Contact",
                    fontWeight = FontWeight.Bold,
                    color = dynamicColors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Change contact name:",
                        color = dynamicColors.textSecondary,
                        style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium
                    )
                    Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dynamicColors.primary,
                            focusedLabelColor = dynamicColors.primary,
                            cursorColor = dynamicColors.primary
                        )
                    )
                }
            },
            confirmButton = {
                TrueTapButton(
                    text = "Save",
                    onClick = {
                        if (editedName.isNotBlank()) {
                            val index = contacts.indexOfFirst { it.id == contact.id }
                            if (index >= 0) {
                                contacts[index] = contact.copy(
                                    name = editedName,
                                    initials = editedName.split(' ')
                                        .mapNotNull { it.firstOrNull()?.uppercase() }
                                        .take(2)
                                        .joinToString("")
                                )
                            }
                        }
                        contactToEdit = null
                    },
                    enabled = editedName.isNotBlank()
                )
            },
            dismissButton = {
                TrueTapButton(
                    text = "Cancel",
                    onClick = { contactToEdit = null },
                    style = TrueTapButtonStyle.TEXT
                )
            }
        )
    }
    
    // Additional dialogs would be implemented here similar to the original screen
    // (Edit Wallet, Delete Wallet, Add Wallet dialogs)
}

@Composable
private fun FavoritesRow(
    favorites: List<ModernContact>,
    onContactClick: (ModernContact) -> Unit
) {
    TrueTapCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TrueTapSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.md)
        ) {
            favorites.take(4).forEach { contact ->
                FavoriteContactItem(
                    contact = contact,
                    onClick = { onContactClick(contact) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Show more indicator if there are more favorites
            if (favorites.size > 4) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrueTapAvatar(
                        text = "+${favorites.size - 4}",
                        onClick = { /* Show all favorites */ }
                    )
                    Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
                    Text(
                        text = "More",
                        style = getDynamicTypography().labelSmall,
                        color = LocalDynamicColors.current.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteContactItem(
    contact: ModernContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrueTapAvatar(
            text = contact.initials,
            onClick = onClick
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
        
        Text(
            text = contact.name.split(" ").first(),
            style = getDynamicTypography().labelSmall,
            color = LocalDynamicColors.current.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun ContactListItem(
    contact: ModernContact,
    onContactClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val dynamicColors = LocalDynamicColors.current
    
    TrueTapCard(onClick = onContactClick) {
        TrueTapListItem(
            title = contact.name,
            subtitle = "${contact.wallets.size} wallet${if (contact.wallets.size != 1) "s" else ""}",
            leadingIcon = null, // We'll use custom avatar
            trailingContent = {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (contact.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (contact.isFavorite) dynamicColors.primary else dynamicColors.textSecondary
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactDetailModal(
    contact: ModernContact,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteContact: () -> Unit,
    onEditContact: () -> Unit,
    onEditWallet: (ContactWallet) -> Unit,
    onDeleteWallet: (ContactWallet) -> Unit,
    onAddWallet: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val dynamicColors = LocalDynamicColors.current
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = dynamicColors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TrueTapSpacing.lg)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TrueTapAvatar(text = contact.initials)
                    
                    Spacer(modifier = Modifier.width(TrueTapSpacing.md))
                    
                    Column {
                        Text(
                            text = contact.name,
                            style = getDynamicTypography().headlineSmall,
                            color = dynamicColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${contact.wallets.size} wallet${if (contact.wallets.size != 1) "s" else ""}",
                            style = getDynamicTypography().bodyMedium,
                            color = dynamicColors.textSecondary
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.sm)) {
                    TrueTapButton(
                        text = "Edit",
                        onClick = onEditContact,
                        style = TrueTapButtonStyle.ICON,
                        icon = Icons.Default.Edit
                    )
                    
                    TrueTapButton(
                        text = "Delete",
                        onClick = onDeleteContact,
                        style = TrueTapButtonStyle.ICON,
                        icon = Icons.Default.Delete
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
            
            // Wallets section
            TrueTapSectionHeader(
                title = "Wallets",
                action = {
                    TrueTapButton(
                        text = "Add Wallet",
                        onClick = onAddWallet,
                        style = TrueTapButtonStyle.OUTLINE,
                        icon = Icons.Default.Add
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
            
            // Wallet list
            contact.wallets.forEach { wallet ->
                WalletItem(
                    wallet = wallet,
                    onCopyAddress = {
                        clipboardManager.setText(AnnotatedString(wallet.address))
                    },
                    onEditWallet = { onEditWallet(wallet) },
                    onDeleteWallet = { onDeleteWallet(wallet) }
                )
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.xl))
            
            // Close button
            TrueTapButton(
                text = "Close",
                onClick = onDismiss,
                style = TrueTapButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
        }
    }
}

@Composable
private fun WalletItem(
    wallet: ContactWallet,
    onCopyAddress: () -> Unit,
    onEditWallet: () -> Unit,
    onDeleteWallet: () -> Unit
) {
    TrueTapCard {
        Column {
            TrueTapListItem(
                title = wallet.name,
                subtitle = "${wallet.address.take(8)}...${wallet.address.takeLast(8)}",
                leadingIcon = Icons.Default.AccountBalanceWallet,
                trailingContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.xs)) {
                        TrueTapButton(
                            text = "Copy",
                            onClick = onCopyAddress,
                            style = TrueTapButtonStyle.ICON,
                            icon = Icons.Default.ContentCopy
                        )
                        
                        TrueTapButton(
                            text = "Edit",
                            onClick = onEditWallet,
                            style = TrueTapButtonStyle.ICON,
                            icon = Icons.Default.Edit
                        )
                        
                        TrueTapButton(
                            text = "Delete",
                            onClick = onDeleteWallet,
                            style = TrueTapButtonStyle.ICON,
                            icon = Icons.Default.Delete
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
            
            TrueTapButton(
                text = "Send Payment",
                onClick = { /* Navigate to send payment */ },
                icon = Icons.Default.Send,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}