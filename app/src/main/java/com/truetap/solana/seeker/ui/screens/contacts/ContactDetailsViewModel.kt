package com.truetap.solana.seeker.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.truetap.solana.seeker.data.models.*

/**
 * ViewModel for Contact Details Screen
 * Manages contact information, editing, and transaction history
 */

@HiltViewModel
class ContactDetailsViewModel @Inject constructor(
    // TODO: Inject actual services when available
    // private val contactsService: ContactsService,
    // private val transactionService: TransactionService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ContactDetailsUiState())
    val uiState: StateFlow<ContactDetailsUiState> = _uiState.asStateFlow()
    
    // Sample data for demonstration
    private val sampleContacts = mapOf(
        "contact_1" to Contact(
            id = "contact_1",
            name = "John Doe",
            initials = "JD",
            seekerActive = true,
            wallets = listOf(
                Wallet(1, "Main Wallet", "7xKBvf6Z1nP2QqXhJgY8sH3fK2dG1mVc9tB4wR5uE8pN", WalletType.PERSONAL)
            ),
            favorite = true,
            walletAddress = "7xKBvf6Z1nP2QqXhJgY8sH3fK2dG1mVc9tB4wR5uE8pN",
            nickname = "Johnny",
            notes = "Met at Solana conference. Great developer and crypto enthusiast.",
            tags = listOf("Developer", "Conference", "Friend"),
            preferredCurrency = "SOL",
            createdAt = System.currentTimeMillis() - 2592000000L, // 30 days ago
            lastTransactionAt = System.currentTimeMillis() - 86400000L // 1 day ago
        ),
        "contact_2" to Contact(
            id = "contact_2",
            name = "Sarah Wilson",
            initials = "SW",
            seekerActive = false,
            wallets = listOf(
                Wallet(2, "Business Wallet", "9mKxA8rT1sE3pQ7vB2nF6jL4wG8cY5dH1xZ3kP9sM7tR", WalletType.BUSINESS)
            ),
            favorite = false,
            walletAddress = "9mKxA8rT1sE3pQ7vB2nF6jL4wG8cY5dH1xZ3kP9sM7tR",
            nickname = "Sar",
            notes = "Business partner for NFT project.",
            tags = listOf("Business", "NFT"),
            preferredCurrency = "USDC",
            createdAt = System.currentTimeMillis() - 1209600000L, // 14 days ago
            lastTransactionAt = System.currentTimeMillis() - 604800000L // 7 days ago
        ),
        "contact_3" to Contact(
            id = "contact_3",
            name = "Mike Chen",
            initials = "MC",
            seekerActive = true,
            wallets = listOf(
                Wallet(3, "Personal Wallet", "5qW2nE8pM7xA1sT6vB9cR4jL3wG0fY8dH5zK1nP4mQ9s", WalletType.PERSONAL)
            ),
            favorite = false,
            walletAddress = "5qW2nE8pM7xA1sT6vB9cR4jL3wG0fY8dH5zK1nP4mQ9s",
            notes = null,
            tags = emptyList(),
            preferredCurrency = "SOL",
            createdAt = System.currentTimeMillis() - 604800000L, // 7 days ago
            lastTransactionAt = null
        )
    )
    
    private val sampleTransactions = mapOf(
        "contact_1" to listOf(
            Transaction(
                id = "tx_1",
                type = TransactionType.SENT,
                amount = 2.5,
                currency = "SOL",
                timestamp = System.currentTimeMillis() - 86400000L, // 1 day ago
                memo = "Payment for consulting work",
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                id = "tx_2",
                type = TransactionType.RECEIVED,
                amount = 100.0,
                currency = "USDC",
                timestamp = System.currentTimeMillis() - 259200000L, // 3 days ago
                memo = "Refund for cancelled order",
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                id = "tx_3",
                type = TransactionType.SENT,
                amount = 0.1,
                currency = "SOL",
                timestamp = System.currentTimeMillis() - 604800000L, // 7 days ago
                memo = "Coffee money",
                status = TransactionStatus.COMPLETED
            )
        ),
        "contact_2" to listOf(
            Transaction(
                id = "tx_4",
                type = TransactionType.RECEIVED,
                amount = 500.0,
                currency = "USDC",
                timestamp = System.currentTimeMillis() - 604800000L, // 7 days ago
                memo = "NFT project milestone payment",
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                id = "tx_5",
                type = TransactionType.SENT,
                amount = 1.5,
                currency = "SOL",
                timestamp = System.currentTimeMillis() - 1209600000L, // 14 days ago
                memo = "Gas fee reimbursement",
                status = TransactionStatus.COMPLETED
            )
        ),
        "contact_3" to emptyList()
    )
    
    fun loadContact(contactId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // TODO: Load from actual contacts service
                /*
                val contact = contactsService.getContact(contactId)
                val transactions = transactionService.getTransactionsForContact(contactId)
                
                if (contact != null) {
                    _uiState.value = _uiState.value.copy(
                        contact = contact,
                        transactions = transactions,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        contact = null,
                        isLoading = false
                    )
                }
                */
                
                // Simulate loading delay
                delay(1500)
                
                val contact = sampleContacts[contactId]
                val transactions = sampleTransactions[contactId] ?: emptyList()
                
                _uiState.value = _uiState.value.copy(
                    contact = contact,
                    transactions = transactions,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load contact: ${e.message}"
                )
            }
        }
    }
    
    fun showEditDialog() {
        val contact = _uiState.value.contact
        if (contact != null) {
            _uiState.value = _uiState.value.copy(
                showEditDialog = true,
                editedContact = contact.copy()
            )
        }
    }
    
    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            editedContact = null
        )
    }
    
    fun updateEditedContact(contact: Contact) {
        _uiState.value = _uiState.value.copy(editedContact = contact)
    }
    
    fun saveContact() {
        val editedContact = _uiState.value.editedContact
        if (editedContact != null) {
            viewModelScope.launch {
                try {
                    // TODO: Save to actual contacts service
                    /*
                    contactsService.updateContact(editedContact)
                    */
                    
                    // Simulate save delay
                    delay(500)
                    
                    _uiState.value = _uiState.value.copy(
                        contact = editedContact,
                        showEditDialog = false,
                        editedContact = null
                    )
                    
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to save contact: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun toggleFavorite() {
        val contact = _uiState.value.contact
        if (contact != null) {
            val updatedContact = contact.copy(favorite = !contact.favorite)
            
            viewModelScope.launch {
                try {
                    // TODO: Update in actual contacts service
                    /*
                    contactsService.updateContact(updatedContact)
                    */
                    
                    _uiState.value = _uiState.value.copy(contact = updatedContact)
                    
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to update favorite status: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun showDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }
    
    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }
    
    fun deleteContact() {
        val contact = _uiState.value.contact
        if (contact != null) {
            viewModelScope.launch {
                try {
                    // TODO: Delete from actual contacts service
                    /*
                    contactsService.deleteContact(contact.id)
                    */
                    
                    // Simulate delete delay
                    delay(500)
                    
                    _uiState.value = _uiState.value.copy(
                        showDeleteDialog = false
                    )
                    
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        showDeleteDialog = false,
                        errorMessage = "Failed to delete contact: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}