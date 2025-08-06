package com.truetap.solana.seeker.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.data.models.Contact
import com.truetap.solana.seeker.data.models.Wallet
import com.truetap.solana.seeker.data.models.WalletType
import com.truetap.solana.seeker.ui.components.BottomNavItem
import com.truetap.solana.seeker.ui.components.TrueTapBottomNavigationBar
import com.truetap.solana.seeker.ui.components.rememberBottomNavHandler
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

// Contact data with multiple wallets support
data class ModernContact(
    val id: String,
    val name: String,
    val initials: String,
    val wallets: List<ContactWallet>,
    val isFavorite: Boolean = false
)

data class ContactWallet(
    val id: String,
    val name: String,
    val address: String,
    val type: WalletType
)

enum class AddContactMethod {
    NFC, BLUETOOTH, QR_CODE, SEND_LINK, MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<ModernContact?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<ModernContact?>(null) }
    var contactToEdit by remember { mutableStateOf<ModernContact?>(null) }
    var walletToEdit by remember { mutableStateOf<Pair<ModernContact, ContactWallet>?>(null) }
    var walletToDelete by remember { mutableStateOf<Pair<ModernContact, ContactWallet>?>(null) }
    var contactToAddWallet by remember { mutableStateOf<ModernContact?>(null) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Sample contacts data (as requested: Charlie Brown and Diana Prince style)
    // Using rememberSaveable to persist across navigation
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
    val nonFavoriteContacts = filteredContacts // Show all contacts in main section
    
    
    
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
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${contact.name}? This action cannot be undone.",
                    color = TrueTapTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        contacts.removeIf { it.id == contact.id }
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapError)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel", color = TrueTapTextSecondary)
                }
            },
            containerColor = TrueTapContainer
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
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Change contact name:",
                        color = TrueTapTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrueTapPrimary,
                            focusedLabelColor = TrueTapPrimary,
                            cursorColor = TrueTapPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            val index = contacts.indexOfFirst { it.id == contact.id }
                            if (index >= 0) {
                                contacts[index] = contact.copy(
                                    name = editedName,
                                    initials = editedName.split(' ').mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                                )
                            }
                        }
                        contactToEdit = null
                    },
                    enabled = editedName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToEdit = null }) {
                    Text("Cancel", color = TrueTapTextSecondary)
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    // Edit Wallet Dialog
    walletToEdit?.let { (contact, wallet) ->
        var editedWalletName by remember { mutableStateOf(wallet.name) }
        
        AlertDialog(
            onDismissRequest = { walletToEdit = null },
            title = {
                Text(
                    text = "Edit Wallet Name",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Change wallet name for ${contact.name}:",
                        color = TrueTapTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedWalletName,
                        onValueChange = { editedWalletName = it },
                        label = { Text("Wallet Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrueTapPrimary,
                            focusedLabelColor = TrueTapPrimary,
                            cursorColor = TrueTapPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedWalletName.isNotBlank()) {
                            val contactIndex = contacts.indexOfFirst { it.id == contact.id }
                            if (contactIndex >= 0) {
                                val updatedWallets = contact.wallets.map { 
                                    if (it.id == wallet.id) it.copy(name = editedWalletName) else it 
                                }
                                contacts[contactIndex] = contact.copy(wallets = updatedWallets)
                            }
                        }
                        walletToEdit = null
                    },
                    enabled = editedWalletName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { walletToEdit = null }) {
                    Text("Cancel", color = TrueTapTextSecondary)
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    // Delete Wallet Confirmation Dialog
    walletToDelete?.let { (contact, wallet) ->
        AlertDialog(
            onDismissRequest = { walletToDelete = null },
            title = {
                Text(
                    text = "Delete Wallet",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete the wallet \"${wallet.name}\" from ${contact.name}?",
                    color = TrueTapTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val contactIndex = contacts.indexOfFirst { it.id == contact.id }
                        if (contactIndex >= 0) {
                            val updatedWallets = contact.wallets.filter { it.id != wallet.id }
                            if (updatedWallets.isNotEmpty()) {
                                contacts[contactIndex] = contact.copy(wallets = updatedWallets)
                            } else {
                                // If no wallets left, delete the entire contact
                                contacts.removeIf { it.id == contact.id }
                            }
                        }
                        walletToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapError)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { walletToDelete = null }) {
                    Text("Cancel", color = TrueTapTextSecondary)
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    // Add Wallet Dialog
    contactToAddWallet?.let { contact ->
        var newWalletName by remember { mutableStateOf("") }
        var newWalletAddress by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { contactToAddWallet = null },
            title = {
                Text(
                    text = "Add Wallet",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Add a new wallet to ${contact.name}:",
                        color = TrueTapTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newWalletName,
                        onValueChange = { newWalletName = it },
                        label = { Text("Wallet Name") },
                        placeholder = { Text("Main Wallet") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrueTapPrimary,
                            focusedLabelColor = TrueTapPrimary,
                            cursorColor = TrueTapPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newWalletAddress,
                        onValueChange = { newWalletAddress = it },
                        label = { Text("Wallet Address") },
                        placeholder = { Text("Enter Solana address...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrueTapPrimary,
                            focusedLabelColor = TrueTapPrimary,
                            cursorColor = TrueTapPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWalletAddress.isNotBlank()) {
                            val contactIndex = contacts.indexOfFirst { it.id == contact.id }
                            if (contactIndex >= 0) {
                                val newWallet = ContactWallet(
                                    id = java.util.UUID.randomUUID().toString(),
                                    name = newWalletName.ifBlank { "Main Wallet" },
                                    address = newWalletAddress.trim(),
                                    type = WalletType.PERSONAL
                                )
                                val updatedWallets = contact.wallets + newWallet
                                contacts[contactIndex] = contact.copy(wallets = updatedWallets)
                            }
                        }
                        contactToAddWallet = null
                    },
                    enabled = newWalletAddress.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
                ) {
                    Text("Add Wallet", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToAddWallet = null }) {
                    Text("Cancel", color = TrueTapTextSecondary)
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Header with Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSearchExpanded) {
                        Text(
                            text = "Contacts",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Icon Button
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TrueTapTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                    }
                }
                
                // Expandable Search Bar
                if (isSearchExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search contacts...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TrueTapTextSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TrueTapTextSecondary
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrueTapPrimary,
                            focusedLabelColor = TrueTapPrimary,
                            cursorColor = TrueTapPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        
        // Main Content
        Box(
            modifier = Modifier.weight(1f)
        ) {
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Favorites Section
                if (favoriteContacts.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "Favorites",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrueTapTextPrimary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // Horizontal scrolling favorite avatars
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(favoriteContacts) { contact ->
                                    FavoriteContactAvatar(
                                        contact = contact,
                                        onClick = { selectedContact = contact }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // All Contacts Section
                if (nonFavoriteContacts.isNotEmpty()) {
                    item {
                        Text(
                            text = "All Contacts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary,
                            modifier = Modifier.padding(top = if (favoriteContacts.isNotEmpty()) 24.dp else 0.dp)
                        )
                    }
                    
                    // All contacts list (without alphabet grouping)
                    items(nonFavoriteContacts) { contact ->
                        ModernContactCard(
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
                }
                
                // Empty State
                if (filteredContacts.isEmpty()) {
                    item {
                        EmptyContactsState(
                            hasSearchQuery = searchQuery.isNotEmpty(),
                            onAddContact = onNavigateToAddContact
                        )
                    }
                }
                
                // Bottom spacing for navigation
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
        
        // Bottom Navigation
        TrueTapBottomNavigationBar(
            selectedTab = BottomNavItem.CONTACTS,
            onTabSelected = rememberBottomNavHandler(
                currentScreen = BottomNavItem.CONTACTS,
                onNavigateToHome = onNavigateToHome,
                onNavigateToSwap = onNavigateToSwap,
                onNavigateToNFTs = onNavigateToNFTs,
                onNavigateToSettings = onNavigateToSettings
            )
        )
        }
        
        // Floating Action Button for Add Contact - Better proportional spacing
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 20.dp, 
                    bottom = 20.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
                .size(56.dp)
                .background(TrueTapPrimary, CircleShape)
                .clickable { onNavigateToAddContact() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Contact",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
private fun FavoriteContactAvatar(
    contact: ModernContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(TrueTapPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.initials,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = contact.name.split(" ").first(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp)
        )
    }
}

@Composable
private fun ModernContactCard(
    contact: ModernContact,
    onContactClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onContactClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(TrueTapPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.initials,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contact Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                
                Text(
                    text = "${contact.wallets.size} wallet${if (contact.wallets.size != 1) "s" else ""}",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Favorite Star
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (contact.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (contact.isFavorite) TrueTapPrimary else TrueTapTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


@Composable
private fun EmptyContactsState(
    hasSearchQuery: Boolean,
    onAddContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasSearchQuery) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = TrueTapTextSecondary,
                modifier = Modifier.size(64.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = "No contacts",
                tint = TrueTapTextSecondary,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (hasSearchQuery) "No contacts found" else "Looking through the YellowPages",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = if (hasSearchQuery) 
                "Try a different search term" 
            else 
                "Add contacts to make payments easier",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
        
        if (!hasSearchQuery) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAddContact,
                colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Contact",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// New Contact Detail Modal based on the wallet management interface
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
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TrueTapBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            TrueTapTextSecondary.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header with Contact Name and Edit Pencil
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = contact.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onEditContact,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Contact",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete Contact Button (Trash Icon)
                    IconButton(
                        onClick = onDeleteContact,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Contact",
                            tint = TrueTapError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Add Wallet Button
                    Button(
                        onClick = onAddWallet,
                        colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Wallet List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(contact.wallets) { wallet ->
                    WalletEntryCard(
                        wallet = wallet,
                        onSend = { /* Send functionality */ },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(wallet.address))
                        },
                        onEdit = { onEditWallet(wallet) },
                        onDelete = { onDeleteWallet(wallet) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Close Button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TrueTapContainer),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = "Close",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WalletEntryCard(
    wallet: ContactWallet,
    onSend: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Wallet Name with Edit Pencil and Copy Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = wallet.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Wallet Name",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                // Copy and Delete Icons on the right
                Row {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Address",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Wallet",
                            tint = TrueTapError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Wallet Address
            Text(
                text = "${wallet.address.take(8)}...${wallet.address.takeLast(8)}",
                fontSize = 14.sp,
                color = TrueTapTextSecondary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Send Button
            Button(
                onClick = onSend,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
        }
    }
}


