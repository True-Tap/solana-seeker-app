package com.truetap.solana.seeker.ui.screens.contacts

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.data.models.Contact
import com.truetap.solana.seeker.data.models.Wallet
import com.truetap.solana.seeker.data.models.WalletType
import com.truetap.solana.seeker.ui.screens.home.BottomNavItem

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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showAddContactModal by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<ModernContact?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<ModernContact?>(null) }
    var contactToEdit by remember { mutableStateOf<ModernContact?>(null) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var showLetterPreview by remember { mutableStateOf(false) }
    
    // Sample contacts data (as requested: Charlie Brown and Diana Prince style)
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
    
    // Group contacts by first letter for alphabet navigation
    val groupedContacts = nonFavoriteContacts.groupBy { it.name.first().uppercaseChar() }
    val alphabet = ('A'..'Z').toList()
    
    // Add Contact Modal
    if (showAddContactModal) {
        AddContactModal(
            onDismiss = { showAddContactModal = false },
            onContactAdded = { newContact ->
                contacts.add(newContact)
                showAddContactModal = false
            }
        )
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
                        // Expandable Search
                        AnimatedSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            isExpanded = isSearchExpanded,
                            onExpandedChange = { isSearchExpanded = it }
                        )
                        
                    }
                }
            }
        
        // Main Content with Alphabet Navigation
        Box(
            modifier = Modifier.weight(1f)
        ) {
            // Alphabet Sidebar on LEFT
            if (nonFavoriteContacts.isNotEmpty() && searchQuery.isEmpty()) {
                AlphabetSidebar(
                    alphabet = alphabet,
                    selectedLetter = selectedLetter,
                    onLetterSelected = { letter ->
                        selectedLetter = letter
                        showLetterPreview = true
                    },
                    onLetterReleased = {
                        showLetterPreview = false
                        selectedLetter = null
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                )
            }
            
            // Letter Preview Overlay
            if (showLetterPreview && selectedLetter != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(80.dp)
                        .background(
                            TrueTapPrimary.copy(alpha = 0.9f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedLetter.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = if (nonFavoriteContacts.isNotEmpty() && searchQuery.isEmpty()) 60.dp else 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 16.dp
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
                    
                    // Grouped contacts by alphabet
                    alphabet.forEach { letter ->
                        val contactsForLetter = groupedContacts[letter] ?: emptyList()
                        if (contactsForLetter.isNotEmpty()) {
                            item {
                                Text(
                                    text = letter.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapPrimary,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            
                            items(contactsForLetter) { contact ->
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
                    }
                }
                
                // Empty State
                if (filteredContacts.isEmpty()) {
                    item {
                        EmptyContactsState(
                            hasSearchQuery = searchQuery.isNotEmpty(),
                            onAddContact = { showAddContactModal = true }
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
        BottomNavigationBar(
            selectedTab = BottomNavItem.CONTACTS,
            onTabSelected = { item ->
                when (item) {
                    BottomNavItem.HOME -> onNavigateToHome()
                    BottomNavItem.SWAP -> onNavigateToSwap()
                    BottomNavItem.NFTS -> onNavigateToNFTs()
                    BottomNavItem.SETTINGS -> onNavigateToSettings()
                    BottomNavItem.CONTACTS -> { /* Already on contacts */ }
                }
            }
        )
        }
        
        // Floating Action Button for Add Contact - Circular and positioned higher
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp) // Move higher to avoid navbar
                .size(56.dp)
                .background(TrueTapPrimary, CircleShape)
                .clickable { showAddContactModal = true },
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
private fun AnimatedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchWidth by animateFloatAsState(
        targetValue = if (isExpanded) 240f else 48f,
        label = "searchWidth"
    )
    
    Box(
        modifier = modifier
            .width(searchWidth.dp)
            .height(48.dp)
            .background(
                if (isExpanded) TrueTapContainer else Color.Transparent,
                RoundedCornerShape(24.dp)
            )
            .border(
                if (isExpanded) 1.dp else 0.dp,
                TrueTapTextSecondary.copy(alpha = 0.3f),
                RoundedCornerShape(24.dp)
            )
            .clickable { 
                if (!isExpanded) onExpandedChange(true)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (isExpanded) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = TrueTapTextPrimary
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search contacts...",
                                color = TrueTapTextSecondary,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { onExpandedChange(false) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Search",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TrueTapTextSecondary,
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
private fun AlphabetSidebar(
    alphabet: List<Char>,
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit,
    onLetterReleased: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sidebarSize by remember { mutableStateOf(IntSize.Zero) }
    
    Column(
        modifier = modifier
            .background(
                TrueTapContainer.copy(alpha = 0.9f),
                RoundedCornerShape(16.dp)
            )
            .padding(vertical = 8.dp, horizontal = 6.dp)
            .onGloballyPositioned { coordinates ->
                sidebarSize = coordinates.size
            }
            .pointerInput(Unit) {
                var currentY = 0f
                detectDragGestures(
                    onDragStart = { offset ->
                        currentY = offset.y
                        val letterIndex = (currentY / (sidebarSize.height / alphabet.size)).toInt()
                            .coerceIn(0, alphabet.size - 1)
                        onLetterSelected(alphabet[letterIndex])
                    },
                    onDrag = { _, dragAmount ->
                        currentY += dragAmount.y
                        currentY = currentY.coerceIn(0f, sidebarSize.height.toFloat())
                        val letterIndex = (currentY / (sidebarSize.height / alphabet.size)).toInt()
                            .coerceIn(0, alphabet.size - 1)
                        onLetterSelected(alphabet[letterIndex])
                    },
                    onDragEnd = {
                        onLetterReleased()
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        alphabet.forEach { letter ->
            val isSelected = letter == selectedLetter
            Text(
                text = letter.toString(),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TrueTapPrimary else TrueTapTextPrimary,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clickable { 
                        onLetterSelected(letter)
                        onLetterReleased()
                    }
            )
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
        Icon(
            imageVector = if (hasSearchQuery) Icons.Default.SearchOff else Icons.Default.PersonAdd,
            contentDescription = null,
            tint = TrueTapTextSecondary,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (hasSearchQuery) "No contacts found" else "No contacts yet",
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

// New Contact Detail Modal based on the wallet management interface
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactDetailModal(
    contact: ModernContact,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteContact: () -> Unit,
    onEditContact: () -> Unit
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
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Contact",
                            tint = TrueTapError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // Add Wallet Button
                    Button(
                        onClick = { /* Add wallet functionality */ },
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
                        onDelete = { /* Delete wallet functionality */ }
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
                        onClick = { /* Edit wallet name functionality */ },
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
                
                // Copy Icon on the right
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Delete Wallet Icon (positioned to not interfere)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
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
    }
}

// Placeholder for AddContactModal (to be implemented)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContactModal(
    onDismiss: () -> Unit,
    onContactAdded: (ModernContact) -> Unit
) {
    // Placeholder implementation
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TrueTapBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Add Contact",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Add contact functionality coming soon...",
                color = TrueTapTextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

