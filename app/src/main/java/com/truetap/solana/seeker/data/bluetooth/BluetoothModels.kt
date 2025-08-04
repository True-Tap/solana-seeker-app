package com.truetap.solana.seeker.data.bluetooth

/**
 * Bluetooth Payment Models - TrueTap
 * Data classes and enums for Bluetooth payment functionality
 */

// Enhanced Bluetooth Device representation
data class BluetoothDevice(
    val id: String,
    val name: String,
    val address: String,
    val rssi: Int = -50, // Signal strength
    val distance: String = "Nearby",
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isPairing: Boolean = false,
    val walletAddress: String? = null,
    val lastSeen: Long = System.currentTimeMillis(),
    val deviceType: BluetoothDeviceType = BluetoothDeviceType.TRUETAP_SEEKER,
    val batteryLevel: Int? = null,
    val isTrueTapUser: Boolean = false,
    val userAvatar: String? = null,
    val userName: String? = null,
    val supportedTokens: List<String> = listOf("SOL", "USDC"),
    val connectionHistory: List<Long> = emptyList()
)

// Bluetooth Device Types
enum class BluetoothDeviceType {
    TRUETAP_SEEKER,
    SOLANA_PAY_DEVICE,
    GENERIC_BLUETOOTH,
    SMARTPHONE,
    TABLET
}

// Bluetooth Payment Status
enum class BluetoothPaymentStatus {
    IDLE,
    INITIATING,
    WAITING_FOR_CONFIRMATION,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

// Device Discovery State
sealed class DeviceDiscoveryState {
    object Idle : DeviceDiscoveryState()
    object Scanning : DeviceDiscoveryState()
    data class DeviceFound(val device: BluetoothDevice) : DeviceDiscoveryState()
    data class ScanComplete(val devicesFound: Int) : DeviceDiscoveryState()
    data class Error(val message: String) : DeviceDiscoveryState()
}

// Bluetooth Connection State
sealed class BluetoothConnectionState {
    object Disconnected : BluetoothConnectionState()
    object Connecting : BluetoothConnectionState()
    object Connected : BluetoothConnectionState()
    object Pairing : BluetoothConnectionState()
    object Paired : BluetoothConnectionState()
    data class Error(val message: String) : BluetoothConnectionState()
}

// Payment Request
data class BluetoothPaymentRequest(
    val recipientDevice: BluetoothDevice,
    val amount: Double,
    val token: String = "SOL",
    val memo: String? = null,
    val requestId: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 300_000, // 5 minutes
    val requesterWalletAddress: String
)

// Payment Response
data class BluetoothPaymentResponse(
    val requestId: String,
    val accepted: Boolean,
    val transactionSignature: String? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val responderWalletAddress: String
)

// Enhanced Bluetooth UI State
data class BluetoothUiState(
    val isScanning: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val hasBluetoothPermission: Boolean = true,
    val discoveredDevices: List<BluetoothDevice> = emptyList(),
    val connectedDevice: BluetoothDevice? = null,
    val errorMessage: String? = null,
    val showPermissionDialog: Boolean = false,
    val discoveryState: DeviceDiscoveryState = DeviceDiscoveryState.Idle,
    val connectionState: BluetoothConnectionState = BluetoothConnectionState.Disconnected,
    val paymentStatus: BluetoothPaymentStatus = BluetoothPaymentStatus.IDLE,
    val currentPaymentRequest: BluetoothPaymentRequest? = null,
    val showPaymentDialog: Boolean = false,
    val showConnectionDialog: Boolean = false,
    val showPairingDialog: Boolean = false,
    val isPaymentInProgress: Boolean = false,
    val lastScanTime: Long? = null,
    val scanDuration: Int = 0, // in seconds
    val maxScanDuration: Int = 30,
    val nearbyTrueTapUsers: List<BluetoothDevice> = emptyList(),
    val favoriteDevices: List<BluetoothDevice> = emptyList()
)

// Bluetooth Permission State
enum class BluetoothPermissionState {
    GRANTED,
    DENIED,
    DENIED_PERMANENTLY,
    NOT_REQUESTED
}

// Bluetooth Adapter State
enum class BluetoothAdapterState {
    ON,
    OFF,
    TURNING_ON,
    TURNING_OFF,
    UNKNOWN
}

// Payment Transaction
data class BluetoothPaymentTransaction(
    val id: String,
    val requestId: String,
    val senderDevice: BluetoothDevice,
    val recipientDevice: BluetoothDevice,
    val amount: Double,
    val token: String,
    val memo: String?,
    val status: BluetoothPaymentStatus,
    val transactionSignature: String?,
    val createdAt: Long,
    val completedAt: Long?,
    val errorMessage: String?
)

// Device Capability
data class DeviceCapability(
    val supportsPayments: Boolean = true,
    val supportsFileTransfer: Boolean = false,
    val supportsChat: Boolean = false,
    val maxTransactionAmount: Double = 1000.0,
    val supportedTokens: List<String> = listOf("SOL", "USDC"),
    val requiresConfirmation: Boolean = true
)

// Bluetooth Service State
data class BluetoothServiceState(
    val isServiceRunning: Boolean = false,
    val isAdvertising: Boolean = false,
    val isScanning: Boolean = false,
    val connectedDevices: List<BluetoothDevice> = emptyList(),
    val lastError: String? = null,
    val serviceStartTime: Long? = null
)