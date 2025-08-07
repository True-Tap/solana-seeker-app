package com.truetap.solana.seeker.data

import com.truetap.solana.seeker.data.models.Contact as DataContact
import com.truetap.solana.seeker.data.models.Transaction as DataTransaction
import com.truetap.solana.seeker.data.models.TransactionType as DataTransactionType
import com.truetap.solana.seeker.data.models.TransactionStatus as DataTransactionStatus
import com.truetap.solana.seeker.domain.model.Contact as DomainContact
import com.truetap.solana.seeker.domain.model.ScheduledPayment
import com.truetap.solana.seeker.domain.model.RepeatInterval
import com.truetap.solana.seeker.domain.model.PaymentStatus
import com.truetap.solana.seeker.ui.screens.contacts.ModernContact
import com.truetap.solana.seeker.ui.screens.contacts.ContactWallet
import com.truetap.solana.seeker.data.models.WalletType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Centralized Mock Data Provider for TrueTap
 * Provides consistent mock data across all screens including contacts, transactions, and scheduled payments
 */
@Singleton
class MockData @Inject constructor() {
    
    companion object {
        // Stable contact data - these should remain consistent across app sessions
        private val baseContacts = listOf(
            ContactData(
                id = "contact_001",
                name = "John Doe",
                initials = "JD",
                walletAddress = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
                isFavorite = true,
                preferredToken = "SOL"
            ),
            ContactData(
                id = "contact_002", 
                name = "Alice Johnson",
                initials = "AJ",
                walletAddress = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                isFavorite = true,
                preferredToken = "USDC"
            ),
            ContactData(
                id = "contact_003",
                name = "Bob Smith",
                initials = "BS", 
                walletAddress = "5dSHdvJBQ38YuuHdKHDHFLhMhLCvdV7xB5QH5Y8z9CXD",
                isFavorite = false,
                preferredToken = "SOL"
            ),
            ContactData(
                id = "contact_004",
                name = "Diana Prince",
                initials = "DP",
                walletAddress = "3mKQrv8fHpPQHhcQdAKYzuG8SN7eCPThjGhNkEKvXX5C",
                isFavorite = false,
                preferredToken = "SOL"
            ),
            ContactData(
                id = "contact_005",
                name = "Charlie Brown",
                initials = "CB",
                walletAddress = "8VzCXwBbmkg9ZTbNMqUxvQRAyrZzDsGYdLVL9zYtBXXN",
                isFavorite = true,
                preferredToken = "BONK"
            ),
            ContactData(
                id = "contact_006",
                name = "Eva Martinez",
                initials = "EM",
                walletAddress = "4nGTfQm1CK8BvMNQ2X7YgKFaLbbCmR6iVuLJbnQy13hA",
                isFavorite = false,
                preferredToken = "RAY"
            ),
            ContactData(
                id = "contact_007",
                name = "Frank Wilson",
                initials = "FW",
                walletAddress = "6pzSL5gmjBrPqGKFaLbbCmR6iVuLJbnQy13hAe7s6DD",
                isFavorite = false,
                preferredToken = "SOL"
            ),
            ContactData(
                id = "contact_008",
                name = "Grace Lee",
                initials = "GL",
                walletAddress = "2mKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgBsV",
                isFavorite = true,
                preferredToken = "USDC"
            )
        )
        
        // Generate stable transaction IDs using hashing (without timestamp for stability)
        private fun generateStableTransactionId(fromAddress: String, toAddress: String, amount: String): String {
            val data = "$fromAddress-$toAddress-$amount"
            val baseId = "tx_${data.hashCode().toString().replace("-", "")}"
            return baseId
        }
        
        
        // Current user wallet address for transaction direction determination
        private const val USER_WALLET_ADDRESS = "YourWalletAddressHere123456789ABCDEF" // Placeholder - could be injected
        
        // Base recent transactions - will be combined with scheduled payments
        private fun generateBaseTransactions(): List<TransactionData> {
            val now = System.currentTimeMillis()
            return listOf(
                TransactionData(
                    id = generateStableTransactionId(baseContacts[0].walletAddress, USER_WALLET_ADDRESS, "2.5") + "_base1",
                    type = DataTransactionType.RECEIVED,
                    amount = 2.5,
                    currency = "SOL", 
                    timestamp = now - 3600000, // 1 hour ago
                    otherPartyName = baseContacts[0].name,
                    otherPartyAddress = baseContacts[0].walletAddress,
                    memo = "Payment for services",
                    status = DataTransactionStatus.COMPLETED
                ),
                TransactionData(
                    id = generateStableTransactionId(USER_WALLET_ADDRESS, baseContacts[1].walletAddress, "50.0") + "_base2",
                    type = DataTransactionType.SENT,
                    amount = 50.0,
                    currency = "USDC",
                    timestamp = now - 7200000, // 2 hours ago
                    otherPartyName = baseContacts[1].name,
                    otherPartyAddress = baseContacts[1].walletAddress,
                    memo = "Lunch money",
                    status = DataTransactionStatus.COMPLETED
                ),
                TransactionData(
                    id = generateStableTransactionId(baseContacts[2].walletAddress, USER_WALLET_ADDRESS, "1.25") + "_base3",
                    type = DataTransactionType.RECEIVED,
                    amount = 1.25,
                    currency = "SOL",
                    timestamp = now - 86400000, // 1 day ago
                    otherPartyName = baseContacts[2].name,
                    otherPartyAddress = baseContacts[2].walletAddress,
                    memo = null,
                    status = DataTransactionStatus.COMPLETED
                ),
                TransactionData(
                    id = generateStableTransactionId(USER_WALLET_ADDRESS, baseContacts[4].walletAddress, "1000000.0") + "_base4",
                    type = DataTransactionType.SENT,
                    amount = 1000000.0,
                    currency = "BONK",
                    timestamp = now - 172800000, // 2 days ago
                    otherPartyName = baseContacts[4].name,
                    otherPartyAddress = baseContacts[4].walletAddress,
                    memo = "BONK transfer test",
                    status = DataTransactionStatus.COMPLETED
                ),
                TransactionData(
                    id = generateStableTransactionId(baseContacts[7].walletAddress, USER_WALLET_ADDRESS, "75.5") + "_base5",
                    type = DataTransactionType.RECEIVED,
                    amount = 75.5,
                    currency = "USDC",
                    timestamp = now - 259200000, // 3 days ago
                    otherPartyName = baseContacts[7].name,
                    otherPartyAddress = baseContacts[7].walletAddress,
                    memo = "Freelance work payment",
                    status = DataTransactionStatus.COMPLETED
                ),
                TransactionData(
                    id = generateStableTransactionId(USER_WALLET_ADDRESS, baseContacts[3].walletAddress, "0.75") + "_base6",
                    type = DataTransactionType.SENT,
                    amount = 0.75,
                    currency = "SOL",
                    timestamp = now - 432000000, // 5 days ago
                    otherPartyName = baseContacts[3].name,
                    otherPartyAddress = baseContacts[3].walletAddress,
                    memo = "Coffee money",
                    status = DataTransactionStatus.COMPLETED
                )
            )
        }
    }
    
    // Mutable list for scheduled payments that can be added to at runtime
    private val scheduledPayments = mutableListOf<ScheduledPayment>()
    
    // Mutable list for pending transactions (scheduled transactions become pending when "executed")
    private val pendingTransactions = mutableListOf<TransactionData>()
    
    // Mutable list for runtime transactions (new sends, receives)
    private val runtimeTransactions = mutableListOf<TransactionData>()
    
    // Reactive flow for transaction updates - emits whenever transactions change
    private val _transactionsFlow = MutableStateFlow<List<DataTransaction>>(emptyList())
    val transactionsFlow: StateFlow<List<DataTransaction>> = _transactionsFlow.asStateFlow()
    
    init {
        // Initialize the flow with base transactions
        _transactionsFlow.value = getAllTransactions()
    }
    
    /**
     * Get all contacts in ModernContact format (for ContactsRepository)
     */
    fun getModernContacts(): List<ModernContact> {
        return baseContacts.map { contact ->
            ModernContact(
                id = contact.id,
                name = contact.name,
                initials = contact.initials,
                wallets = listOf(
                    ContactWallet(
                        id = "${contact.id}_wallet_main",
                        name = "Main Wallet",
                        address = contact.walletAddress,
                        type = WalletType.PERSONAL
                    )
                ),
                isFavorite = contact.isFavorite
            )
        }
    }
    
    /**
     * Get all contacts in DataContact format (for SendPaymentScreen compatibility)
     */
    fun getDataContacts(): List<DataContact> {
        return baseContacts.map { contact ->
            DataContact(
                id = contact.id,
                name = contact.name,
                initials = contact.initials,
                avatar = null,
                seekerActive = true, // Mock as all active
                wallets = listOf(
                    com.truetap.solana.seeker.data.models.Wallet(
                        id = 1,
                        name = "Main",
                        address = contact.walletAddress,
                        type = WalletType.PERSONAL
                    )
                ),
                favorite = contact.isFavorite,
                walletAddress = contact.walletAddress,
                nickname = null,
                notes = null,
                tags = emptyList(),
                preferredCurrency = contact.preferredToken,
                createdAt = System.currentTimeMillis() - 2592000000, // 30 days ago
                lastTransactionAt = if (hasRecentTransaction(contact.walletAddress)) 
                    System.currentTimeMillis() - 86400000 else null // 1 day ago
            )
        }
    }
    
    /**
     * Get all contacts in DomainContact format (for ScheduledPayment compatibility)
     */
    fun getDomainContacts(): List<DomainContact> {
        return baseContacts.map { contact ->
            DomainContact(
                id = contact.id,
                name = contact.name,
                walletAddress = contact.walletAddress,
                avatar = null,
                initials = contact.initials,
                isFavorite = contact.isFavorite,
                lastTransactionDate = if (hasRecentTransaction(contact.walletAddress)) 
                    LocalDateTime.now().minusDays(1) else null,
                totalTransactions = getTransactionCountForContact(contact.walletAddress),
                preferredToken = contact.preferredToken,
                tags = emptyList(),
                notes = null
            )
        }
    }
    
    /**
     * Get all transactions including base transactions, runtime transactions, and pending scheduled transactions
     */
    fun getAllTransactions(): List<DataTransaction> {
        val baseTransactions = generateBaseTransactions()
        val allTransactions = (baseTransactions + runtimeTransactions + pendingTransactions).map { transactionData ->
            DataTransaction(
                id = transactionData.id,
                type = transactionData.type,
                amount = transactionData.amount,
                currency = transactionData.currency,
                timestamp = transactionData.timestamp,
                memo = transactionData.memo,
                status = transactionData.status,
                otherPartyName = transactionData.otherPartyName,
                otherPartyAddress = transactionData.otherPartyAddress
            )
        }
        return allTransactions.sortedByDescending { it.timestamp }
    }
    
    /**
     * Get scheduled payments
     */
    fun getScheduledPayments(): List<ScheduledPayment> {
        return scheduledPayments.toList()
    }
    
    /**
     * Add a new scheduled payment
     */
    fun addScheduledPayment(scheduledPayment: ScheduledPayment) {
        scheduledPayments.add(scheduledPayment)
        
        // Emit updated transactions (including scheduled ones that become pending)
        _transactionsFlow.value = getAllTransactions()
        // Optionally add a pending transaction to show in recent activity
        addPendingScheduledTransaction(scheduledPayment)
    }
    
    /**
     * Remove a scheduled payment (for cancellation)
     */
    fun removeScheduledPayment(paymentId: String): Boolean {
        return scheduledPayments.removeAll { it.id == paymentId }
    }
    
    /**
     * Create a new scheduled payment from payment details
     */
    fun createScheduledPayment(
        recipientAddress: String,
        amount: BigDecimal,
        token: String,
        memo: String?,
        startDate: LocalDateTime,
        repeatInterval: RepeatInterval,
        maxExecutions: Int?
    ): ScheduledPayment {
        val recipientContact = baseContacts.find { it.walletAddress == recipientAddress }
        return ScheduledPayment(
            id = "scheduled_${UUID.randomUUID()}",
            recipientAddress = recipientAddress,
            recipientName = recipientContact?.name,
            amount = amount,
            token = token,
            memo = memo,
            startDate = startDate,
            nextExecutionDate = startDate,
            repeatInterval = repeatInterval,
            maxExecutions = maxExecutions,
            currentExecutions = 0,
            status = PaymentStatus.PENDING,
            createdAt = LocalDateTime.now(),
            lastExecutedAt = null,
            failureReason = null
        )
    }
    
    /**
     * Add a pending transaction for a scheduled payment (to show in recent activity)
     */
    private fun addPendingScheduledTransaction(scheduledPayment: ScheduledPayment) {
        val scheduledTransaction = TransactionData(
            id = "scheduled_tx_${scheduledPayment.id}",
            type = DataTransactionType.SENT, // Scheduled sends are always outgoing
            amount = scheduledPayment.amount.toDouble(),
            currency = scheduledPayment.token,
            timestamp = scheduledPayment.startDate.toEpochSecond(ZoneOffset.UTC) * 1000,
            otherPartyName = scheduledPayment.recipientName ?: "Unknown",
            otherPartyAddress = scheduledPayment.recipientAddress,
            memo = scheduledPayment.memo,
            status = DataTransactionStatus.PENDING // Show as pending/scheduled
        )
        pendingTransactions.add(scheduledTransaction)
    }
    
    // Helper methods
    private fun hasRecentTransaction(walletAddress: String): Boolean {
        val baseTransactions = generateBaseTransactions()
        return baseTransactions.any { 
            it.otherPartyAddress == walletAddress && 
            (System.currentTimeMillis() - it.timestamp) < 604800000 // Within last 7 days
        }
    }
    
    private fun getTransactionCountForContact(walletAddress: String): Int {
        val baseTransactions = generateBaseTransactions()
        return baseTransactions.count { it.otherPartyAddress == walletAddress }
    }
    
    /**
     * Get recent contacts (those with recent transactions, for SendPaymentScreen)
     */
    fun getRecentContacts(): List<DataContact> {
        return getDataContacts().filter { contact ->
            hasRecentTransaction(contact.walletAddress)
        }.sortedByDescending { it.lastTransactionAt ?: 0L }
    }
    
    /**
     * Find contact by wallet address
     */
    fun findContactByAddress(walletAddress: String): DataContact? {
        return getDataContacts().find { it.walletAddress == walletAddress }
    }
    
    /**
     * Add a new transaction (for sent/received payments)
     */
    fun addTransaction(
        type: DataTransactionType,
        amount: Double,
        currency: String,
        otherPartyAddress: String,
        otherPartyName: String? = null,
        memo: String? = null
    ): String {
        val timestamp = System.currentTimeMillis()
        val fromAddress = if (type == DataTransactionType.SENT) USER_WALLET_ADDRESS else otherPartyAddress
        val toAddress = if (type == DataTransactionType.SENT) otherPartyAddress else USER_WALLET_ADDRESS
        val baseTransactionId = generateStableTransactionId(fromAddress, toAddress, amount.toString())
        
        // Handle duplicates by adding counter suffix if same ID exists
        val transactionId = generateUniqueTransactionId(baseTransactionId)
        
        val transaction = TransactionData(
            id = transactionId,
            type = type,
            amount = amount,
            currency = currency,
            timestamp = timestamp,
            otherPartyName = otherPartyName ?: findContactByAddress(otherPartyAddress)?.name ?: "Unknown",
            otherPartyAddress = otherPartyAddress,
            memo = memo,
            status = DataTransactionStatus.COMPLETED
        )
        
        runtimeTransactions.add(0, transaction) // Add to beginning for most recent first
        
        // Emit updated transactions to reactive flow
        _transactionsFlow.value = getAllTransactions()
        
        return transactionId
    }
    
    /**
     * Clear all scheduled payments (for testing/reset purposes)
     */
    fun clearScheduledPayments() {
        scheduledPayments.clear()
        pendingTransactions.clear()
    }
    
    /**
     * Ensure unique transaction IDs by adding counter suffix if needed
     */
    private fun generateUniqueTransactionId(baseId: String): String {
        val allExistingIds = (generateBaseTransactions() + runtimeTransactions + pendingTransactions).map { it.id }.toSet()
        
        if (!allExistingIds.contains(baseId)) {
            return baseId
        }
        
        // Add counter suffix for duplicates
        var counter = 1
        var uniqueId = "${baseId}_$counter"
        while (allExistingIds.contains(uniqueId)) {
            counter++
            uniqueId = "${baseId}_$counter"
        }
        return uniqueId
    }
    
    /**
     * Clear all runtime transactions (for testing/reset purposes)
     */
    fun clearRuntimeTransactions() {
        runtimeTransactions.clear()
    }
}

/**
 * Internal data structures for consistent mock data
 */
private data class ContactData(
    val id: String,
    val name: String,
    val initials: String,
    val walletAddress: String,
    val isFavorite: Boolean,
    val preferredToken: String
)

private data class TransactionData(
    val id: String,
    val type: DataTransactionType,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val otherPartyName: String,
    val otherPartyAddress: String,
    val memo: String?,
    val status: DataTransactionStatus
)