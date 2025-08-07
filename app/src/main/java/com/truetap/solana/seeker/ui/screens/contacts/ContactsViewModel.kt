/**
 * Contacts ViewModel - TrueTap
 * Manages state and business logic for the Contacts screen
 */

package com.truetap.solana.seeker.ui.screens.contacts

import com.truetap.solana.seeker.data.models.*
import com.truetap.solana.seeker.repositories.ContactsRepository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManualContact(
    val name: String = "",
    val address: String = ""
)

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

data class ContactsUiState(
    val contacts: List<ModernContact> = emptyList(),
    val filteredContacts: List<ModernContact> = emptyList(),
    val groupedContacts: Map<String, List<ModernContact>> = emptyMap(),
    val searchQuery: String = "",
    val selectedContact: ModernContact? = null,
    val showAddModal: Boolean = false,
    val selectedAddMethod: AddContactMethod? = null,
    val showMoreOptions: Boolean = false,
    val nfcScanning: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val bluetoothDevices: List<BluetoothDevice> = emptyList(),
    val qrMode: QRMode? = null,
    val shareLink: String = "https://truetap.app/contact/invite/abc123",
    val manualContact: ManualContact = ManualContact(),
    val copiedAddress: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactsRepository: ContactsRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()
    
    init {
        initializeData()
        loadContacts()
    }
    
    private fun initializeData() {
        viewModelScope.launch {
            try {
                contactsRepository.initializeSampleDataIfEmpty()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    private fun loadContacts() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                
                // Combine contacts flow with search query
                combine(
                    contactsRepository.getContactsFlow(),
                    _searchQuery
                ) { contacts, query ->
                    val filtered = filterContacts(contacts, query)
                    ContactsUiState(
                        contacts = contacts,
                        filteredContacts = filtered,
                        groupedContacts = groupContacts(filtered),
                        searchQuery = query,
                        isLoading = false
                    )
                }.collect { newState ->
                    _uiState.update { currentState ->
                        newState.copy(
                            selectedContact = currentState.selectedContact,
                            showAddModal = currentState.showAddModal,
                            selectedAddMethod = currentState.selectedAddMethod,
                            showMoreOptions = currentState.showMoreOptions,
                            nfcScanning = currentState.nfcScanning,
                            bluetoothEnabled = currentState.bluetoothEnabled,
                            bluetoothDevices = currentState.bluetoothDevices,
                            qrMode = currentState.qrMode,
                            shareLink = currentState.shareLink,
                            manualContact = currentState.manualContact,
                            copiedAddress = currentState.copiedAddress,
                            errorMessage = currentState.errorMessage
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.update { query }
    }
    
    fun selectContact(contact: ModernContact?) {
        _uiState.update { it.copy(selectedContact = contact) }
    }
    
    fun toggleFavorite(contactId: String) {
        viewModelScope.launch {
            try {
                val currentContacts = contactsRepository.getContacts()
                val contactToUpdate = currentContacts.find { it.id == contactId }
                contactToUpdate?.let { contact ->
                    val updatedContact = contact.copy(isFavorite = !contact.isFavorite)
                    contactsRepository.updateContact(updatedContact)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun addContact(contact: ModernContact) {
        viewModelScope.launch {
            try {
                contactsRepository.addContact(contact)
                hideAddContactModal()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun updateContact(contact: ModernContact) {
        viewModelScope.launch {
            try {
                contactsRepository.updateContact(contact)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            try {
                contactsRepository.deleteContact(contactId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun addWalletToContact(contactId: String, wallet: ContactWallet) {
        viewModelScope.launch {
            try {
                contactsRepository.addWalletToContact(contactId, wallet)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun updateWalletInContact(contactId: String, wallet: ContactWallet) {
        viewModelScope.launch {
            try {
                contactsRepository.updateWalletInContact(contactId, wallet)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun deleteWalletFromContact(contactId: String, walletId: String) {
        viewModelScope.launch {
            try {
                contactsRepository.deleteWalletFromContact(contactId, walletId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun showAddContactModal() {
        _uiState.update { it.copy(showAddModal = true) }
    }
    
    fun hideAddContactModal() {
        _uiState.update { 
            it.copy(
                showAddModal = false,
                selectedAddMethod = null,
                nfcScanning = false,
                showMoreOptions = false,
                qrMode = null
            )
        }
    }
    
    fun selectAddMethod(method: AddContactMethod) {
        _uiState.update { 
            it.copy(
                selectedAddMethod = method,
                showMoreOptions = false
            )
        }
        
        when (method) {
            AddContactMethod.NFC -> startNFCScanning()
            AddContactMethod.BLUETOOTH -> checkBluetoothStatus()
            AddContactMethod.QR_CODE -> _uiState.update { it.copy(qrMode = QRMode.SCAN) }
            else -> {}
        }
    }
    
    fun backToMethods() {
        _uiState.update { 
            it.copy(
                selectedAddMethod = null,
                nfcScanning = false,
                qrMode = null
            )
        }
    }
    
    fun toggleBluetooth() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val newEnabled = !currentState.bluetoothEnabled
            
            _uiState.update { it.copy(bluetoothEnabled = newEnabled) }
            
            if (newEnabled) {
                // Simulate finding devices
                delay(1000)
                _uiState.update {
                    it.copy(
                        bluetoothDevices = listOf(
                            BluetoothDevice("1", "Sarah's iPhone", "Nearby"),
                            BluetoothDevice("2", "Mike's Android", "Nearby"),
                            BluetoothDevice("3", "Unknown Device", "Far")
                        )
                    )
                }
            } else {
                _uiState.update { it.copy(bluetoothDevices = emptyList()) }
            }
        }
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            // Simulate connection process
            // Connecting to device via Bluetooth
            
            // TODO: Implement actual Bluetooth connection
            delay(2000)
            
            // Simulate successful connection and contact addition
            val newContact = ModernContact(
                id = java.util.UUID.randomUUID().toString(),
                name = device.name.replace("'s iPhone", "").replace("'s Android", ""),
                initials = device.name.take(2).uppercase(),
                wallets = listOf(
                    ContactWallet(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Main Wallet",
                        address = "9WzDXwBbkk...9zYtAWWM", // Mock address
                        type = WalletType.PERSONAL
                    )
                ),
                isFavorite = false
            )
            addContact(newContact)
            
            hideAddContactModal()
        }
    }
    
    fun setQRMode(mode: QRMode) {
        _uiState.update { it.copy(qrMode = mode) }
    }
    
    fun updateManualContact(name: String, address: String) {
        _uiState.update { 
            it.copy(manualContact = ManualContact(name, address))
        }
    }
    
    fun addManualContact() {
        val currentState = _uiState.value
        val contact = currentState.manualContact
        
        if (contact.name.isNotBlank() && contact.address.isNotBlank()) {
            val newContact = ModernContact(
                id = java.util.UUID.randomUUID().toString(),
                name = contact.name,
                initials = contact.name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString(""),
                wallets = listOf(
                    ContactWallet(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Main Wallet",
                        address = contact.address,
                        type = WalletType.PERSONAL
                    )
                ),
                isFavorite = false
            )
            addContact(newContact)
        }
    }
    
    fun updateShareLink(link: String) {
        _uiState.update { it.copy(shareLink = link) }
    }
    
    fun copyShareLink() {
        viewModelScope.launch {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Share Link", _uiState.value.shareLink)
                clipboard.setPrimaryClip(clip)
                
                // TODO: Show toast confirmation
                // Share link copied successfully
            } catch (error: Exception) {
                // Failed to copy share link
            }
        }
    }
    
    fun sendShareLink() {
        // TODO: Implement share link via SMS/messaging
        // Sending share link via SMS/messaging
    }
    
    fun copyWalletAddress(address: String) {
        viewModelScope.launch {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Wallet Address", address)
                clipboard.setPrimaryClip(clip)
                
                _uiState.update { it.copy(copiedAddress = address) }
                
                // Clear copied state after 2 seconds
                delay(2000)
                _uiState.update { it.copy(copiedAddress = null) }
                
                // Wallet address copied successfully
            } catch (error: Exception) {
                // Failed to copy wallet address
            }
        }
    }
    
    private fun startNFCScanning() {
        _uiState.update { it.copy(nfcScanning = true) }
        
        viewModelScope.launch {
            // Simulate NFC scanning
            delay(3000)
            
            // TODO: Show dialog for contact found
            // NFC Contact Found - showing dialog for user confirmation
            
            // Simulate adding contact
            val newContact = ModernContact(
                id = java.util.UUID.randomUUID().toString(),
                name = "Alex Chen",
                initials = "AC",
                wallets = listOf(
                    ContactWallet(
                        id = java.util.UUID.randomUUID().toString(),
                        name = "Main Wallet",
                        address = "9WzDXwBbkk...9zYtAWWM",
                        type = WalletType.PERSONAL
                    )
                ),
                isFavorite = false
            )
            addContact(newContact)
            
            hideAddContactModal()
        }
    }
    
    private fun checkBluetoothStatus() {
        // Check if Bluetooth is enabled and request permission if needed
        if (!_uiState.value.bluetoothEnabled) {
            // TODO: Show dialog to enable Bluetooth
            // Bluetooth is required to scan for nearby devices
        }
    }
    
    
    private fun filterContacts(contacts: List<ModernContact>, query: String): List<ModernContact> {
        return if (query.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(query, ignoreCase = true) ||
                contact.wallets.any { it.address.contains(query, ignoreCase = true) }
            }
        }
    }
    
    private fun groupContacts(contacts: List<ModernContact>): Map<String, List<ModernContact>> {
        return contacts.groupBy { contact ->
            contact.name.firstOrNull()?.uppercase() ?: "#"
        }.toSortedMap()
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}