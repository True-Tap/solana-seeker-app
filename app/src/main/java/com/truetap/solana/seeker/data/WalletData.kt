package com.truetap.solana.seeker.data

import java.math.BigDecimal

/**
 * Extended wallet data models for comprehensive wallet state management
 */

data class WalletBalance(
    val publicKey: String,
    val solBalance: BigDecimal,
    val usdValue: BigDecimal? = null,
    val tokenBalances: List<TokenBalance> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class TokenBalance(
    val mint: String,
    val symbol: String,
    val name: String,
    val amount: BigDecimal,
    val decimals: Int,
    val uiAmount: Double,
    val logoUri: String? = null
)

data class WalletNFT(
    val mint: String,
    val name: String,
    val description: String? = null,
    val image: String? = null,
    val collection: NFTCollection? = null,
    val attributes: List<NFTAttribute> = emptyList(),
    val creators: List<NFTCreator> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class NFTCollection(
    val name: String,
    val family: String? = null,
    val verified: Boolean = false
)

data class NFTAttribute(
    val traitType: String,
    val value: String
)

data class NFTCreator(
    val address: String,
    val verified: Boolean = false,
    val share: Int = 0
)

data class WalletTransaction(
    val signature: String,
    val blockTime: Long,
    val slot: Long,
    val status: TransactionStatus,
    val type: TransactionType,
    val amount: BigDecimal? = null,
    val fromAddress: String? = null,
    val toAddress: String? = null,
    val tokenMint: String? = null,
    val fee: BigDecimal,
    val memo: String? = null,
    val programIds: List<String> = emptyList()
)

enum class TransactionStatus {
    SUCCESS,
    FAILED,
    PENDING
}

enum class TransactionType {
    TRANSFER,
    TOKEN_TRANSFER,
    NFT_TRANSFER,
    SWAP,
    UNKNOWN
}

/**
 * Comprehensive wallet state that includes all wallet data
 */
data class WalletState(
    val account: WalletAccount?,
    val balance: WalletBalance?,
    val nfts: List<WalletNFT> = emptyList(),
    val transactions: List<WalletTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val lastUpdated: Long? = null,
    val error: String? = null,
    val connectedWallets: List<WalletAccount> = emptyList() // For multi-wallet support
) {
    val isConnected: Boolean get() = account != null
    val hasBalance: Boolean get() = balance != null && balance.solBalance > BigDecimal.ZERO
    val hasNFTs: Boolean get() = nfts.isNotEmpty()
    val hasTransactions: Boolean get() = transactions.isNotEmpty()
    
    // Helper properties for EnhancedSwapScreen
    val solBalance: BigDecimal get() = balance?.solBalance ?: BigDecimal.ZERO
    val tokenBalances: Map<String, BigDecimal> get() = 
        balance?.tokenBalances?.associate { it.symbol to it.amount } ?: emptyMap()
    
    // Genesis NFT helper properties
    val hasGenesisNFT: Boolean get() = nfts.any { nft -> 
        nft.name?.contains("Genesis", ignoreCase = true) == true ||
        nft.name?.contains("Seeker", ignoreCase = true) == true ||
        nft.collection?.name?.contains("Genesis", ignoreCase = true) == true ||
        nft.mint == "46pcSL5gmjBrPqGKFaLbbCmR6iVuLJbnQy13hAe7s6CC" // Official Seeker Genesis NFT mint
    }
    
    val genesisNFTTier: String get() {
        val genesisNFT = nfts.find { nft ->
            nft.name?.contains("Genesis", ignoreCase = true) == true ||
            nft.name?.contains("Seeker", ignoreCase = true) == true ||
            nft.mint == "46pcSL5gmjBrPqGKFaLbbCmR6iVuLJbnQy13hAe7s6CC"
        }
        
        return when {
            genesisNFT?.name?.contains("Legendary", ignoreCase = true) == true ||
            genesisNFT?.attributes?.any { it.traitType?.contains("rarity", ignoreCase = true) == true && 
                                        it.value.contains("legendary", ignoreCase = true) } == true -> "LEGENDARY"
            genesisNFT?.name?.contains("Rare", ignoreCase = true) == true ||
            genesisNFT?.attributes?.any { it.traitType?.contains("rarity", ignoreCase = true) == true && 
                                        it.value.contains("rare", ignoreCase = true) } == true -> "RARE"
            hasGenesisNFT -> "COMMON"
            else -> "NONE"
        }
    }
}