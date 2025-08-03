package com.truetap.solana.seeker.ui.screens.nft

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.truetap.solana.seeker.R

// Bottom Navigation Components
enum class BottomNavItem(val title: String, val icon: String) {
    HOME("Home", "house"),
    SWAP("Swap", "arrows-clockwise"),
    NFTS("NFTs", "image"),
    CONTACTS("Contacts", "users"),
    SETTINGS("Settings", "gear")
}

/**
 * NFTs Screen - Compose screen for displaying NFT collection
 * Converted from React Native TSX to Kotlin Compose
 */

data class NFT(
    val id: String,
    val name: String,
    val image: String,
    val creator: String,
    val value: String,
    val rarity: String,
    val traits: List<String>,
    val description: String
)

data class Collection(
    val id: Int,
    val name: String,
    val count: Int,
    val floorPrice: String,
    val coverImage: String,
    val nfts: List<NFT>
)

enum class SendStatus {
    SUCCESS,
    ERROR,
    UNDONE
}

// Removed duplicate NFTView enum - defined in NFTsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NFTsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: NFTsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    

    
    // NFT Detail Dialog
    uiState.selectedNFT?.let { nft ->
        NFTDetailDialog(
            nft = nft,
            walletAddress = uiState.walletAddress,
            onWalletAddressChange = viewModel::setWalletAddress,
            sendingNFT = uiState.sendingNFT,
            sendStatus = uiState.sendStatus,
            canUndo = uiState.canUndo,
            onSendNFT = viewModel::sendNFT,
            onUndo = viewModel::undoSend,
            onDismiss = { viewModel.setSelectedNFT(null) }
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E8))
    ) {
        // Main content area
        Box(
            modifier = Modifier.weight(1f)
        ) {
            when (uiState.currentView) {
                NFTView.COLLECTIONS -> {
                    if (uiState.collections.isEmpty()) {
                        EmptyNFTState()
                    } else {
                        CollectionsView(
                            collections = uiState.collections,
                            currentIndex = uiState.cardStack,
                            onCollectionSelect = viewModel::enterGallery,
                            onCardChange = viewModel::setCardStack,
                            screenWidth = screenWidth
                        )
                    }
                }
                NFTView.GALLERY -> {
                    uiState.selectedCollection?.let { collection ->
                        GalleryView(
                            collection = collection,
                            onNavigateBack = { viewModel.setCurrentView(NFTView.COLLECTIONS) },
                            onNFTSelect = viewModel::setSelectedNFT
                        )
                    }
                }
            }
        }
        
        // Bottom Navigation Bar
        BottomNavigationBar(
            selectedTab = BottomNavItem.NFTS,
            onTabSelected = { tab ->
                when (tab) {
                    BottomNavItem.HOME -> onNavigateToHome()
                    BottomNavItem.SWAP -> onNavigateToSwap()
                    BottomNavItem.NFTS -> { /* Already on NFTs */ }
                    BottomNavItem.CONTACTS -> onNavigateToContacts()
                    BottomNavItem.SETTINGS -> onNavigateToSettings()
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollectionsView(
    collections: List<Collection>,
    currentIndex: Int,
    onCollectionSelect: (Collection) -> Unit,
    onCardChange: (Int) -> Unit,
    screenWidth: Dp
) {
    val pagerState = rememberPagerState(
        initialPage = currentIndex,
        pageCount = { collections.size }
    )
    
    LaunchedEffect(pagerState.currentPage) {
        onCardChange(pagerState.currentPage)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Text(
                text = "NFT Vault",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "Swipe or tap cards to browse",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Card Pager
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                pageSpacing = 16.dp
            ) { page ->
                val collection = collections[page]
                CollectionCard(
                    collection = collection,
                    onClick = { onCollectionSelect(collection) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newIndex = (currentIndex - 1 + collections.size) % collections.size
                    onCardChange(newIndex)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous",
                    tint = Color(0xFF1A1A1A)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(collections.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(
                                width = if (index == currentIndex) 32.dp else 8.dp,
                                height = 8.dp
                            )
                            .background(
                                color = if (index == currentIndex) 
                                    Color(0xFFFF8B3D) 
                                else 
                                    Color(0xFFE5E5E5),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onCardChange(index) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(
                onClick = {
                    val newIndex = (currentIndex + 1) % collections.size
                    onCardChange(newIndex)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next",
                    tint = Color(0xFF1A1A1A)
                )
            }
        }
    }
}

@Composable
private fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(400.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // Collection Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = collection.coverImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
            }
            
            // Collection Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
            ) {
                Text(
                    text = collection.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Collection size",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${collection.count} NFTs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF8B3D)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Floor price",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = collection.floorPrice,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
                
                Text(
                    text = "Tap to enter gallery →",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun GalleryView(
    collection: Collection,
    onNavigateBack: () -> Unit,
    onNFTSelect: (NFT) -> Unit
) {
    LazyColumn {
        item {
            // Gallery Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onNavigateBack() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = Color(0xFF666666)
                    )
                    Text(
                        text = "Back",
                        fontSize = 16.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = collection.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
        }
        
        item {
            // NFT Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(600.dp) // Fixed height to work inside LazyColumn
            ) {
                items(collection.nfts) { nft ->
                    NFTCard(
                        nft = nft,
                        onClick = { onNFTSelect(nft) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NFTCard(
    nft: NFT,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Polaroid Frame
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 48.dp)
            ) {
                // NFT Image
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1A1A1A))
                ) {
                    AsyncImage(
                        model = nft.image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // NFT Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = nft.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = nft.value,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF8B3D),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        // Shadow
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                )
                .graphicsLayer(scaleY = 0.3f)
        )
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NFTDetailDialog(
    nft: NFT,
    walletAddress: String,
    onWalletAddressChange: (String) -> Unit,
    sendingNFT: Boolean,
    sendStatus: SendStatus?,
    canUndo: Boolean,
    onSendNFT: () -> Unit,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp)
            ) {
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nft.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF666666)
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // NFT Display
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .size(200.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFF9B4E),
                                            Color(0xFFFF6B29)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = nft.image,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // NFT Info
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = nft.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = "by ${nft.creator}",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        
                        Surface(
                            color = Color(0xFFFFF4E6),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current Value",
                                    fontSize = 16.sp,
                                    color = Color(0xFF666666)
                                )
                                Text(
                                    text = nft.value,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8B3D)
                                )
                            }
                        }
                        
                        Column {
                            Text(
                                text = "Description",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = nft.description,
                                fontSize = 16.sp,
                                color = Color(0xFF1A1A1A),
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Traits",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                items(nft.traits) { trait ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFFF8B3D)
                                    ) {
                                        Text(
                                            text = trait,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Send NFT Section
                        when {
                            sendingNFT -> {
                                SendingStatus()
                            }
                            sendStatus == SendStatus.SUCCESS -> {
                                SuccessStatus(canUndo = canUndo, onUndo = onUndo)
                            }
                            sendStatus == SendStatus.UNDONE -> {
                                UndoStatus()
                            }
                            sendStatus == SendStatus.ERROR -> {
                                ErrorStatus()
                            }
                            else -> {
                                SendSection(
                                    walletAddress = walletAddress,
                                    onWalletAddressChange = onWalletAddressChange,
                                    onSendNFT = {
                                        keyboardController?.hide()
                                        onSendNFT()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SendSection(
    walletAddress: String,
    onWalletAddressChange: (String) -> Unit,
    onSendNFT: () -> Unit
) {
    val isValidWallet = walletAddress.length in 32..44
    val walletError = if (walletAddress.isNotEmpty() && !isValidWallet) {
        "Wallet address should be 32-44 characters (currently ${walletAddress.length})"
    } else null
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = walletAddress,
            onValueChange = onWalletAddressChange,
            placeholder = { Text("Enter wallet address...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = walletError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isValidWallet) Color(0xFF4CAF50) else TrueTapPrimary,
                unfocusedBorderColor = if (walletError != null) Color(0xFFF44336) else Color(0xFFE5E5E5),
                errorBorderColor = Color(0xFFF44336)
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        walletError?.let { error ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = Color(0xFFF44336)
                )
            }
        }
        
        Button(
            onClick = onSendNFT,
            enabled = isValidWallet,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValidWallet) TrueTapPrimary else Color(0xFFE5E5E5),
                contentColor = if (isValidWallet) Color.White else Color(0xFF999999)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Send NFT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SendingStatus() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        CircularProgressIndicator(
            color = TrueTapPrimary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Sending NFT...",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = "Please wait",
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun SuccessStatus(
    canUndo: Boolean,
    onUndo: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF4CAF50),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "NFT Sent Successfully!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
        
        if (canUndo) {
            TextButton(
                onClick = onUndo,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Undo (30s remaining)",
                    fontSize = 14.sp,
                    color = TrueTapPrimary,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
private fun UndoStatus() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = TrueTapPrimary,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Transaction Cancelled",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapPrimary
        )
    }
}

@Composable
private fun ErrorStatus() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF44336),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Invalid wallet address",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF44336)
        )
        Text(
            text = "Please check and try again",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyNFTState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // No Picasso Image
        Image(
            painter = painterResource(id = R.drawable.nopicasso),
            contentDescription = "No NFTs",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp),
            contentScale = ContentScale.Fit
        )
        
        // Main message
        Text(
            text = "No Picasso's Here",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Subtitle
        Text(
            text = "Your NFT vault is empty. Connect your wallet or purchase NFTs to get started.",
            fontSize = 18.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Call to action
        Button(
            onClick = { /* TODO: Add action to connect wallet or browse marketplace */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TrueTapPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Explore NFTs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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