package com.truetap.solana.seeker.ui.screens.nft

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.truetap.solana.seeker.ui.components.*
import com.truetap.solana.seeker.ui.components.layouts.*
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.viewmodels.WalletViewModel

/**
 * NFTsScreen migrated to use TrueTap Design System
 * 
 * Clean, organized NFT collection interface with proper state management
 */

@Composable
fun NFTsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: NFTsViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val walletState by walletViewModel.walletState.collectAsStateWithLifecycle()
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    // Load NFTs when wallet state changes
    LaunchedEffect(walletState.account?.publicKey) {
        viewModel.loadWalletNFTs(walletState.account?.publicKey)
    }
    
    // Use standardized screen template
    TrueTapScreenTemplate(
        title = "NFT Vault",
        subtitle = when {
            !uiState.hasWallet -> "Connect wallet to view NFTs"
            uiState.isLoading -> "Loading your collection..."
            uiState.collections.isNotEmpty() -> "${uiState.collections.size} collections found"
            else -> "Your NFT collection"
        },
        currentTab = BottomNavItem.NFTS,
        onNavigateToHome = onNavigateToHome,
        onNavigateToSwap = onNavigateToSwap,
        onNavigateToNFTs = { /* Already on NFTs */ },
        onNavigateToContacts = onNavigateToContacts,
        onNavigateToSettings = onNavigateToSettings,
        actions = {
            // Refresh button
            if (uiState.hasWallet) {
                TrueTapButton(
                    text = "Refresh",
                    onClick = { viewModel.loadWalletNFTs(walletState.account?.publicKey) },
                    style = TrueTapButtonStyle.ICON,
                    icon = Icons.Default.Refresh
                )
            }
        },
        modifier = modifier
    ) {
        when {
            uiState.isLoading -> {
                item {
                    TrueTapLoadingState(
                        message = "Searching for your NFTs and Genesis tokens..."
                    )
                }
            }
            
            !uiState.hasWallet -> {
                item {
                    NoWalletNFTState(
                        onConnectWallet = { walletViewModel.connectWallet() }
                    )
                }
            }
            
            uiState.error != null -> {
                item {
                    ErrorNFTState(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.loadWalletNFTs(walletState.account?.publicKey) }
                    )
                }
            }
            
            uiState.collections.isEmpty() -> {
                item {
                    EmptyNFTState(
                        onExploreNFTs = { /* Navigate to marketplace */ }
                    )
                }
            }
            
            else -> {
                // Collections view
                when (uiState.currentView) {
                    NFTView.COLLECTIONS -> {
                        // Genesis NFT Status (if holder)
                        if (walletState.hasGenesisNFT) {
                            item {
                                GenesisNFTStatusCard(
                                    tier = walletState.genesisNFTTier,
                                    onViewDetails = { /* Navigate to Genesis details */ }
                                )
                            }
                        }
                        
                        // Collections grid
                        item {
                            TrueTapSectionHeader(
                                title = "Collections",
                                subtitle = "${uiState.collections.size} collections"
                            )
                        }
                        
                        items(uiState.collections) { collection ->
                            CollectionCard(
                                collection = collection,
                                onClick = { viewModel.enterGallery(collection) }
                            )
                        }
                    }
                    
                    NFTView.GALLERY -> {
                        // Gallery view for specific collection
                        uiState.selectedCollection?.let { collection ->
                            item {
                                GalleryHeader(
                                    collection = collection,
                                    onNavigateBack = { viewModel.setCurrentView(NFTView.COLLECTIONS) }
                                )
                            }
                            
                            item {
                                NFTGrid(
                                    nfts = collection.nfts,
                                    onNFTSelect = viewModel::setSelectedNFT
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // NFT Detail Dialog
    uiState.selectedNFT?.let { nft ->
        NFTDetailDialog(
            nft = nft,
            onDismiss = { viewModel.setSelectedNFT(null) },
            onSendNFT = { /* Handle NFT sending */ }
        )
    }
}

@Composable
private fun NoWalletNFTState(onConnectWallet: () -> Unit) {
    TrueTapEmptyState(
        title = "No Wallet Connected",
        description = "Connect your wallet to view your NFT collection and Genesis tokens.",
        icon = Icons.Default.AccountBalanceWallet,
        action = {
            TrueTapButton(
                text = "Connect Wallet",
                onClick = onConnectWallet,
                icon = Icons.Default.AccountBalanceWallet
            )
        }
    )
}

@Composable
private fun ErrorNFTState(
    error: String,
    onRetry: () -> Unit
) {
    TrueTapEmptyState(
        title = "Failed to Load NFTs",
        description = error,
        icon = Icons.Default.Warning,
        action = {
            TrueTapButton(
                text = "Try Again",
                onClick = onRetry,
                icon = Icons.Default.Refresh
            )
        }
    )
}

@Composable
private fun EmptyNFTState(onExploreNFTs: () -> Unit) {
    TrueTapEmptyState(
        title = "No NFTs Found",
        description = "No NFTs were found in your connected wallet. Purchase or mint NFTs to get started.",
        icon = Icons.Default.Image,
        action = {
            TrueTapButton(
                text = "Explore NFTs",
                onClick = onExploreNFTs,
                icon = Icons.Default.Explore
            )
        }
    )
}

@Composable
private fun GenesisNFTStatusCard(
    tier: String?,
    onViewDetails: () -> Unit
) {
    TrueTapCard(
        style = TrueTapCardStyle.FILLED,
        onClick = onViewDetails
    ) {
        TrueTapListItem(
            title = "Genesis NFT Holder",
            subtitle = tier?.let { "Tier: $it • Exclusive benefits active" } ?: "Exclusive benefits unlocked",
            leadingIcon = Icons.Default.Diamond,
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.sm)
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = getDynamicColors().success
                    ) {}
                    
                    Text(
                        text = "Active",
                        style = getDynamicTypography().labelSmall,
                        color = getDynamicColors().success
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Details",
                        tint = getDynamicColors().textSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    TrueTapCard(
        style = TrueTapCardStyle.ELEVATED,
        onClick = onClick
    ) {
        Column {
            // Collection cover image
            AsyncImage(
                model = collection.coverImage,
                contentDescription = collection.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            
            // Collection info
            Text(
                text = collection.name,
                style = getDynamicTypography(accessibility.largeButtonMode).headlineSmall,
                color = dynamicColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Items",
                        style = getDynamicTypography(accessibility.largeButtonMode).bodySmall,
                        color = dynamicColors.textSecondary
                    )
                    Text(
                        text = "${collection.count}",
                        style = getDynamicTypography(accessibility.largeButtonMode).bodyLarge,
                        color = dynamicColors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Floor Price",
                        style = getDynamicTypography(accessibility.largeButtonMode).bodySmall,
                        color = dynamicColors.textSecondary
                    )
                    Text(
                        text = collection.floorPrice,
                        style = getDynamicTypography(accessibility.largeButtonMode).bodyLarge,
                        color = dynamicColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            
            TrueTapButton(
                text = "View Collection",
                onClick = onClick,
                style = TrueTapButtonStyle.OUTLINE,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GalleryHeader(
    collection: Collection,
    onNavigateBack: () -> Unit
) {
    TrueTapCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrueTapButton(
                    text = "Back",
                    onClick = onNavigateBack,
                    style = TrueTapButtonStyle.ICON,
                    icon = Icons.Default.ArrowBack
                )
                
                Spacer(modifier = Modifier.width(TrueTapSpacing.md))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.name,
                        style = getDynamicTypography().headlineSmall,
                        color = getDynamicColors().textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${collection.nfts.size} items",
                        style = getDynamicTypography().bodyMedium,
                        color = getDynamicColors().textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun NFTGrid(
    nfts: List<NFT>,
    onNFTSelect: (NFT) -> Unit
) {
    TrueTapCard {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(TrueTapSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.md),
            verticalArrangement = Arrangement.spacedBy(TrueTapSpacing.md),
            modifier = Modifier.height(400.dp) // Fixed height for proper scrolling
        ) {
            items(nfts) { nft ->
                NFTCard(
                    nft = nft,
                    onClick = { onNFTSelect(nft) }
                )
            }
        }
    }
}

@Composable
private fun NFTCard(
    nft: NFT,
    onClick: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    TrueTapCard(
        style = TrueTapCardStyle.DEFAULT,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // NFT Image
            AsyncImage(
                model = nft.image,
                contentDescription = nft.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
            
            // NFT Info
            Text(
                text = nft.name,
                style = getDynamicTypography(accessibility.largeButtonMode).titleSmall,
                color = dynamicColors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            Text(
                text = nft.value,
                style = getDynamicTypography(accessibility.largeButtonMode).bodySmall,
                color = dynamicColors.primary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NFTDetailDialog(
    nft: NFT,
    onDismiss: () -> Unit,
    onSendNFT: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
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
                Text(
                    text = nft.name,
                    style = getDynamicTypography(accessibility.largeButtonMode).headlineSmall,
                    color = dynamicColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = dynamicColors.textSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            
            // NFT Image
            AsyncImage(
                model = nft.image,
                contentDescription = nft.name,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
            
            // NFT Details
            TrueTapCard(style = TrueTapCardStyle.OUTLINED) {
                Column {
                    TrueTapListItem(
                        title = "Creator",
                        subtitle = nft.creator,
                        leadingIcon = Icons.Default.Person
                    )
                    
                    TrueTapListItem(
                        title = "Current Value",
                        subtitle = nft.value,
                        leadingIcon = Icons.Default.AttachMoney
                    )
                    
                    TrueTapListItem(
                        title = "Rarity",
                        subtitle = nft.rarity,
                        leadingIcon = Icons.Default.Star
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            
            // Description
            if (nft.description.isNotEmpty()) {
                TrueTapSectionHeader(title = "Description")
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
                
                Text(
                    text = nft.description,
                    style = getDynamicTypography(accessibility.largeButtonMode).bodyMedium,
                    color = dynamicColors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            }
            
            // Traits
            if (nft.traits.isNotEmpty()) {
                TrueTapSectionHeader(title = "Traits")
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
                
                // Display traits as chips
                // This would be implemented with a flow layout
                
                Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            }
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.md)
            ) {
                TrueTapButton(
                    text = "Send NFT",
                    onClick = onSendNFT,
                    icon = Icons.Default.Send,
                    modifier = Modifier.weight(1f)
                )
                
                TrueTapButton(
                    text = "View on Explorer",
                    onClick = { /* Navigate to blockchain explorer */ },
                    style = TrueTapButtonStyle.OUTLINE,
                    icon = Icons.Default.OpenInNew,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(TrueTapSpacing.xl))
        }
    }
}