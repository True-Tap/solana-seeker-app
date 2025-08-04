package com.truetap.solana.seeker.domain.model

/**
 * Cryptocurrency tokens supported by TrueTap
 */
enum class CryptoToken(val symbol: String, val displayName: String, val decimals: Int = 9) {
    SOL("SOL", "Solana", 9),
    USDC("USDC", "USD Coin", 6),
    USDT("USDT", "Tether", 6),
    RAY("RAY", "Raydium", 6),
    SRM("SRM", "Serum", 6),
    ORCA("ORCA", "Orca", 6),
    BONK("BONK", "Bonk", 5),
    JUP("JUP", "Jupiter", 6);

    companion object {
        fun fromSymbol(symbol: String): CryptoToken? {
            return values().find { it.symbol.equals(symbol, ignoreCase = true) }
        }
    }
}

/**
 * Transaction types supported across the app
 */
enum class TransactionType {
    SENT, 
    RECEIVED, 
    SWAP, 
    SCHEDULED,
    NFC_PAYMENT,
    BLUETOOTH_PAYMENT
}


/**
 * Swap quote information
 */
data class SwapQuote(
    val inputAmount: java.math.BigDecimal,
    val outputAmount: java.math.BigDecimal,
    val inputToken: CryptoToken,
    val outputToken: CryptoToken,
    val rate: Double,
    val slippage: Double,
    val networkFee: java.math.BigDecimal,
    val exchangeFee: java.math.BigDecimal,
    val route: List<String>,
    val estimatedTime: Long, // in seconds
    val priceImpact: Double,
    val isGenesisHolder: Boolean = false,
    val platformFeeBps: Int = 50,
    val jupiterQuoteResponse: String = ""
)

/**
 * Transaction result sealed class
 */
sealed class TransactionResult {
    data class Success(val signature: String) : TransactionResult()
    data class Error(val message: String) : TransactionResult()
    object Processing : TransactionResult()
}