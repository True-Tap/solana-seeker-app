package com.truetap.solana.seeker.ui.screens.nft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for NFTs Screen
 * Manages state for NFT collection display and management
 */

enum class NFTView {
    COLLECTIONS,
    GALLERY
}

data class NFTsUiState(
    val currentView: NFTView = NFTView.COLLECTIONS,
    val collections: List<Collection> = emptyList(),
    val selectedCollection: Collection? = null,
    val selectedNFT: NFT? = null,
    val cardStack: Int = 0,
    val showTutorial: Boolean = true,
    val walletAddress: String = "",
    val sendingNFT: Boolean = false,
    val sendStatus: SendStatus? = null,
    val canUndo: Boolean = false
)

@HiltViewModel
class NFTsViewModel @Inject constructor(
    // TODO: Inject actual services when available
    // private val nftService: NFTService,
    // private val walletService: WalletService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NFTsUiState())
    val uiState: StateFlow<NFTsUiState> = _uiState.asStateFlow()
    
    init {
        loadCollections()
    }
    
    fun setCurrentView(view: NFTView) {
        _uiState.value = _uiState.value.copy(currentView = view)
    }
    
    fun setCardStack(index: Int) {
        _uiState.value = _uiState.value.copy(cardStack = index)
    }
    
    fun enterGallery(collection: Collection) {
        _uiState.value = _uiState.value.copy(
            selectedCollection = collection,
            currentView = NFTView.GALLERY,
            showTutorial = false
        )
    }
    
    fun setSelectedNFT(nft: NFT?) {
        _uiState.value = _uiState.value.copy(
            selectedNFT = nft,
            walletAddress = if (nft == null) "" else _uiState.value.walletAddress,
            sendStatus = if (nft == null) null else _uiState.value.sendStatus,
            canUndo = if (nft == null) false else _uiState.value.canUndo
        )
    }
    
    fun dismissTutorial() {
        _uiState.value = _uiState.value.copy(showTutorial = false)
    }
    
    fun setWalletAddress(address: String) {
        _uiState.value = _uiState.value.copy(walletAddress = address)
    }
    
    fun sendNFT() {
        val currentState = _uiState.value
        val isValidWallet = currentState.walletAddress.length in 32..44
        
        if (!isValidWallet) {
            _uiState.value = currentState.copy(sendStatus = SendStatus.ERROR)
            return
        }
        
        _uiState.value = currentState.copy(
            sendingNFT = true,
            sendStatus = null
        )
        
        viewModelScope.launch {
            try {
                // Simulate sending NFT
                delay(2000)
                
                _uiState.value = _uiState.value.copy(
                    sendingNFT = false,
                    sendStatus = SendStatus.SUCCESS,
                    canUndo = true
                )
                
                // Start undo timeout (30 seconds)
                delay(30000)
                
                // Check if still in success state and undo is still available
                if (_uiState.value.sendStatus == SendStatus.SUCCESS && _uiState.value.canUndo) {
                    _uiState.value = _uiState.value.copy(
                        canUndo = false,
                        selectedNFT = null,
                        sendStatus = null,
                        walletAddress = ""
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    sendingNFT = false,
                    sendStatus = SendStatus.ERROR
                )
            }
        }
    }
    
    fun undoSend() {
        _uiState.value = _uiState.value.copy(
            canUndo = false,
            sendStatus = SendStatus.UNDONE
        )
        
        viewModelScope.launch {
            delay(2000)
            _uiState.value = _uiState.value.copy(sendStatus = null)
        }
    }
    
    private fun loadCollections() {
        viewModelScope.launch {
            try {
                // TODO: Load from actual NFT service
                /*
                val collections = nftService.getUserCollections()
                _uiState.value = _uiState.value.copy(collections = collections)
                */
                
                // Sample data for now - commenting out to show empty state
                val sampleCollections = emptyList<Collection>()
                /*
                listOf(
                    Collection(
                        id = 1,
                        name = "Solana Seekers",
                        count = 12,
                        floorPrice = "12.5 SOL",
                        coverImage = "https://picsum.photos/400/400?random=1",
                        nfts = listOf(
                            NFT(
                                id = "1",
                                name = "Seeker #001",
                                image = "https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?w=600&h=600&fit=crop",
                                creator = "ArtistDAO",
                                value = "45 SOL",
                                rarity = "Legendary",
                                traits = listOf("Golden", "Animated", "Genesis", "Rare Eyes"),
                                description = "The first legendary Seeker, forged in the depths of Solana."
                            ),
                            NFT(
                                id = "2",
                                name = "Seeker #002",
                                image = "https://images.unsplash.com/photo-1635322966219-b75ed372eb01?w=600&h=600&fit=crop",
                                creator = "ArtistDAO",
                                value = "18 SOL",
                                rarity = "Rare",
                                traits = listOf("Silver", "Static", "Early Bird"),
                                description = "An early edition Seeker with unique attributes."
                            ),
                            NFT(
                                id = "3",
                                name = "Seeker #003",
                                image = "https://images.unsplash.com/photo-1637739242353-ae1c4e6c2e?w=600&h=600&fit=crop",
                                creator = "ArtistDAO",
                                value = "12 SOL",
                                rarity = "Common",
                                traits = listOf("Bronze", "Standard"),
                                description = "A standard Seeker ready for adventure."
                            )
                        )
                    ),
                    Collection(
                        id = 2,
                        name = "TrueTap Genesis",
                        count = 8,
                        floorPrice = "8.2 SOL",
                        coverImage = "https://picsum.photos/400/400?random=2",
                        nfts = listOf(
                            NFT(
                                id = "4",
                                name = "Genesis #001",
                                image = "https://images.unsplash.com/photo-1646463910913-e2d1b9f928d5?w=600&h=600&fit=crop",
                                creator = "TrueTap",
                                value = "150 SOL",
                                rarity = "Legendary",
                                traits = listOf("Founder", "1/1", "Animated"),
                                description = "The genesis of TrueTap. The one that started it all."
                            )
                        )
                    ),
                    Collection(
                        id = 3,
                        name = "Pixel Punks",
                        count = 20,
                        floorPrice = "5.5 SOL",
                        coverImage = "https://picsum.photos/400/400?random=3",
                        nfts = listOf(
                            NFT(
                                id = "5",
                                name = "Pixel Punk #777",
                                image = "https://images.unsplash.com/photo-1618172193763-c511deb635ca?w=600&h=600&fit=crop",
                                creator = "PixelLabs",
                                value = "25 SOL",
                                rarity = "Ultra Rare",
                                traits = listOf("Laser Eyes", "Gold Chain", "Mohawk"),
                                description = "Lucky number 777 with the rarest traits."
                            )
                        )
                    )
                )
                */
                
                _uiState.value = _uiState.value.copy(collections = sampleCollections)
                
            } catch (e: Exception) {
                // Handle error silently for now
                _uiState.value = _uiState.value.copy(collections = emptyList())
            }
        }
    }
}