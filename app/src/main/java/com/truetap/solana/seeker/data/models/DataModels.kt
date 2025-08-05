/**
 * Shared Data Models - TrueTap
 * Common data classes used across the application
 */

package com.truetap.solana.seeker.data.models

// Contact related models
data class Contact(
    val id: String,
    val name: String,
    val initials: String,
    val avatar: String? = null,
    val seekerActive: Boolean,
    val wallets: List<Wallet>,
    val favorite: Boolean,
    val walletAddress: String,
    val nickname: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val preferredCurrency: String = "SOL",
    val createdAt: Long = System.currentTimeMillis(),
    val lastTransactionAt: Long? = null
)

data class Wallet(
    val id: Int,
    val name: String,
    val address: String,
    val type: WalletType
)

enum class WalletType { PERSONAL, BUSINESS }

// Bluetooth related models
data class BluetoothDevice(
    val id: String,
    val name: String,
    val address: String,
    val rssi: Int = -50, // Signal strength
    val distance: String = "Nearby",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val walletAddress: String? = null,
    val lastSeen: Long = System.currentTimeMillis()
)

data class BluetoothUiState(
    val isScanning: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val hasBluetoothPermission: Boolean = true,
    val discoveredDevices: List<BluetoothDevice> = emptyList(),
    val connectedDevice: BluetoothDevice? = null,
    val errorMessage: String? = null,
    val showPermissionDialog: Boolean = false
)

// Transaction related models
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val memo: String? = null,
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

enum class TransactionType {
    SENT, RECEIVED, SWAPPED
}

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED
}

// Contact management enums
enum class AddContactMethod {
    NFC, BLUETOOTH, QR, SHARE, MANUAL
}

enum class QRMode { SCAN, SHOW }

// Contact Details Screen state
data class ContactDetailsUiState(
    val contact: Contact? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showEditDialog: Boolean = false,
    val editedContact: Contact? = null,
    val showDeleteDialog: Boolean = false
)

// NFC related models
enum class NfcRole {
    SENDER, RECEIVER
}

sealed class TransactionResult {
    object Success : TransactionResult()
    data class Error(val message: String) : TransactionResult()
    object Processing : TransactionResult()
}

data class NfcUiState(
    val role: NfcRole = NfcRole.SENDER,
    val amount: String = "",
    val recipient: String = "",
    val isProcessing: Boolean = false,
    val transactionResult: TransactionResult? = null,
    val errorMessage: String? = null,
    val isNfcEnabled: Boolean = true,
    val showSuccessAnimation: Boolean = false,
    val showErrorAnimation: Boolean = false
)

// Blockchain/Solana related models
data class TokenAccountInfo(
    val mint: String,
    val amount: String,
    val decimals: Int
)