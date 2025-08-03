/**
 * Contacts ViewModel - TrueTap
 * Manages state and business logic for the Contacts screen
 */

package com.truetap.solana.seeker.ui.screens.contacts

import com.truetap.solana.seeker.data.models.*

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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManualContact(
    val name: String = "",
    val address: String = ""
)

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val groupedContacts: Map<String, List<Contact>> = emptyMap(),
    val searchQuery: String = "",
    val selectedContact: Contact? = null,
    val showAddModal: Boolean = false,
    val selectedAddMethod: AddContactMethod? = null,
    val showMoreOptions: Boolean = false,
    val nfcScanning: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val bluetoothDevices: List<BluetoothDevice> = emptyList(),
    val qrMode: QRMode? = null,
    val shareLink: String = "https://truetap.app/contact/invite/abc123",
    val manualContact: ManualContact = ManualContact(),
    val copiedAddress: String? = null
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()
    
    // Sample contacts data
    private val sampleContacts = listOf(
        Contact(
            id = "1",
            name = "Alex Chen",
            initials = "AC",
            seekerActive = true,
            wallets = listOf(
                Wallet(1, "Personal", "9WzDXwBbkk...9zYtAWWM", WalletType.PERSONAL),
                Wallet(2, "Business", "7WzDXwBbkk...9zYtAWWM", WalletType.BUSINESS)
            ),
            favorite = false,
            walletAddress = "9WzDXwBbkk...9zYtAWWM"
        ),
        Contact(
            id = "2",
            name = "Sarah Johnson",
            initials = "SJ",
            seekerActive = false,
            wallets = listOf(
                Wallet(3, "Main Wallet", "8XyPQwBbkk...7yZtBXXN", WalletType.PERSONAL)
            ),
            favorite = true,
            walletAddress = "8XyPQwBbkk...7yZtBXXN"
        ),
        Contact(
            id = "3",
            name = "Mike Davis",
            initials = "MD",
            seekerActive = true,
            wallets = listOf(
                Wallet(4, "Work", "6ZaPQwBbkk...8yZtCYYO", WalletType.BUSINESS),
                Wallet(5, "Personal", "5YbPQwBbkk...9yZtDZZP", WalletType.PERSONAL)
            ),
            favorite = false,
            walletAddress = "6ZaPQwBbkk...8yZtCYYO"
        )
    )
    
    init {
        loadContacts()
    }
    
    private fun loadContacts() {
        _uiState.update { currentState ->
            val filtered = filterContacts(sampleContacts, currentState.searchQuery)
            currentState.copy(
                contacts = sampleContacts,
                filteredContacts = filtered,
                groupedContacts = groupContacts(filtered)
            )
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = filterContacts(currentState.contacts, query)
            currentState.copy(
                searchQuery = query,
                filteredContacts = filtered,
                groupedContacts = groupContacts(filtered)
            )
        }
    }
    
    fun selectContact(contact: Contact) {
        _uiState.update { it.copy(selectedContact = contact) }
    }
    
    fun toggleFavorite(contactId: String) {
        _uiState.update { currentState ->
            val updatedContacts = currentState.contacts.map { contact ->
                if (contact.id == contactId) {
                    contact.copy(favorite = !contact.favorite)
                } else {
                    contact
                }
            }
            val filtered = filterContacts(updatedContacts, currentState.searchQuery)
            currentState.copy(
                contacts = updatedContacts,
                filteredContacts = filtered,
                groupedContacts = groupContacts(filtered)
            )
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
            println("Connecting to device: ${device.name}")
            
            // TODO: Implement actual Bluetooth connection
            delay(2000)
            
            // Simulate successful connection and contact addition
            addContact(
                name = device.name.replace("'s iPhone", "").replace("'s Android", ""),
                address = "9WzDXwBbkk...9zYtAWWM" // Mock address
            )
            
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
            addContact(contact.name, contact.address)
            hideAddContactModal()
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
                println("Share link copied to clipboard")
            } catch (error: Exception) {
                println("Failed to copy share link: ${error.message}")
            }
        }
    }
    
    fun sendShareLink() {
        // TODO: Implement share link via SMS/messaging
        println("Sending share link: ${_uiState.value.shareLink}")
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
                
                println("Wallet address copied to clipboard")
            } catch (error: Exception) {
                println("Failed to copy wallet address: ${error.message}")
            }
        }
    }
    
    private fun startNFCScanning() {
        _uiState.update { it.copy(nfcScanning = true) }
        
        viewModelScope.launch {
            // Simulate NFC scanning
            delay(3000)
            
            // TODO: Show dialog for contact found
            println("NFC Contact Found: Alex Chen would like to share their contact info. Accept?")
            
            // Simulate adding contact
            addContact("Alex Chen", "9WzDXwBbkk...9zYtAWWM")
            
            hideAddContactModal()
        }
    }
    
    private fun checkBluetoothStatus() {
        // Check if Bluetooth is enabled and request permission if needed
        if (!_uiState.value.bluetoothEnabled) {
            // TODO: Show dialog to enable Bluetooth
            println("Bluetooth is required to scan for nearby devices.")
        }
    }
    
    private fun addContact(name: String, address: String) {
        val newContact = Contact(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString(""),
            seekerActive = true,
            wallets = listOf(
                Wallet(
                    id = 1,
                    name = "Main Wallet",
                    address = address,
                    type = WalletType.PERSONAL
                )
            ),
            favorite = false,
            walletAddress = address
        )
        
        _uiState.update { currentState ->
            val updatedContacts = currentState.contacts + newContact
            val filtered = filterContacts(updatedContacts, currentState.searchQuery)
            currentState.copy(
                contacts = updatedContacts,
                filteredContacts = filtered,
                groupedContacts = groupContacts(filtered),
                manualContact = ManualContact() // Reset manual contact form
            )
        }
    }
    
    private fun filterContacts(contacts: List<Contact>, query: String): List<Contact> {
        return if (query.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(query, ignoreCase = true)
            }
        }
    }
    
    private fun groupContacts(contacts: List<Contact>): Map<String, List<Contact>> {
        return contacts.groupBy { contact ->
            contact.name.firstOrNull()?.uppercase() ?: "#"
        }.toSortedMap()
    }
}