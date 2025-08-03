package com.truetap.solana.seeker.ui.screens.bluetooth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.data.models.*
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf

/**
 * Bluetooth Discovery Screen - Compose screen for discovering nearby Solana Seeker devices
 * Converted from React Native TSX to Kotlin Compose
 */

// BluetoothUiState is now imported from shared models

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDiscoveryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State management without ViewModel
    var isBluetoothEnabled by remember { mutableStateOf(true) }
    var hasBluetoothPermission by remember { mutableStateOf(true) }
    var isScanning by remember { mutableStateOf(false) }
    var connectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Sample discovered devices
    val discoveredDevices = remember {
        mutableStateListOf(
            BluetoothDevice(
                id = "1",
                name = "TrueTap Seeker #1",
                address = "00:1A:7D:DA:71:13",
                rssi = -45,
                isConnecting = false,
                walletAddress = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM"
            ),
            BluetoothDevice(
                id = "2",
                name = "TrueTap Seeker #2",
                address = "00:1A:7D:DA:71:14",
                rssi = -65,
                isConnecting = false,
                walletAddress = null
            ),
            BluetoothDevice(
                id = "3",
                name = "Solana Pay Device",
                address = "00:1A:7D:DA:71:15",
                rssi = -78,
                isConnecting = false,
                walletAddress = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU"
            )
        )
    }
    
    val uiState = BluetoothUiState(
        isBluetoothEnabled = isBluetoothEnabled,
        hasBluetoothPermission = hasBluetoothPermission,
        isScanning = isScanning,
        discoveredDevices = discoveredDevices,
        connectedDevice = connectedDevice,
        errorMessage = errorMessage
    )
    
    // Functions
    val requestPermission = {
        // Simulate permission request
        showPermissionDialog = false
        hasBluetoothPermission = true
    }
    
    val enableBluetooth = {
        isBluetoothEnabled = true
    }
    
    val startScan = {
        if (hasBluetoothPermission && isBluetoothEnabled) {
            isScanning = true
        } else if (!hasBluetoothPermission) {
            showPermissionDialog = true
        }
    }
    
    val stopScan = {
        isScanning = false
    }
    
    val connectDevice = { device: BluetoothDevice ->
        val index = discoveredDevices.indexOfFirst { it.id == device.id }
        if (index != -1) {
            discoveredDevices[index] = device.copy(isConnecting = true)
        }
    }
    
    val disconnectDevice = { device: BluetoothDevice ->
        connectedDevice = null
        discoveredDevices.add(device)
        Unit
    }
    
    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = "Bluetooth Permission Required",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            },
            text = {
                Text(
                    text = "TrueTap needs Bluetooth permission to discover nearby devices for payments. Please grant permission in settings.",
                    color = TrueTapTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPermissionDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrueTapPrimary
                    )
                ) {
                    Text("Grant Permission", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel", color = TrueTapTextSecondary)
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    // Error Dialog
    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = {
                Text(
                    text = "Bluetooth Error",
                    fontWeight = FontWeight.Bold,
                    color = TrueTapError
                )
            },
            text = {
                Text(
                    text = error,
                    color = TrueTapTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrueTapError
                    )
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = TrueTapContainer
        )
    }
    
    // Handle scan timeout
    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(10000) // Scan for 10 seconds
            isScanning = false
        }
    }
    
    // Handle device connection
    LaunchedEffect(discoveredDevices.find { it.isConnecting }) {
        val connectingDevice = discoveredDevices.find { it.isConnecting }
        if (connectingDevice != null) {
            delay(3000)
            val index = discoveredDevices.indexOfFirst { it.id == connectingDevice.id }
            if (index != -1 && discoveredDevices[index].isConnecting) {
                connectedDevice = connectingDevice.copy(isConnecting = false)
                discoveredDevices.removeAll { it.id == connectingDevice.id }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TrueTapBackground, Color(0xFFF0ECE4))
                )
            )
    ) {
        // Header
        Surface(
            color = TrueTapContainer,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TrueTapTextPrimary
                    )
                }
                
                Text(
                    text = "Bluetooth Discovery",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                
                Spacer(modifier = Modifier.size(32.dp))
            }
        }
        
        // Content based on state
        when {
            !uiState.hasBluetoothPermission -> {
                PermissionRequiredContent(
                    onRequestPermission = requestPermission
                )
            }
            !uiState.isBluetoothEnabled -> {
                BluetoothDisabledContent(
                    onEnableBluetooth = enableBluetooth
                )
            }
            else -> {
                DiscoveryContent(
                    uiState = uiState,
                    onStartScan = startScan,
                    onStopScan = stopScan,
                    onConnectDevice = connectDevice,
                    onDisconnectDevice = disconnectDevice
                )
            }
        }
    }
}

@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = null,
            tint = TrueTapTextInactive,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Bluetooth Permission Required",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "To discover and connect to nearby Solana Seeker devices, TrueTap needs Bluetooth permission.",
            fontSize = 16.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = TrueTapPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Grant Permission",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BluetoothDisabledContent(
    onEnableBluetooth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BluetoothDisabled,
            contentDescription = null,
            tint = TrueTapError,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Bluetooth is Disabled",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Please enable Bluetooth to discover nearby Solana Seeker devices for seamless payments.",
            fontSize = 16.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onEnableBluetooth,
            colors = ButtonDefaults.buttonColors(
                containerColor = TrueTapPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Enable Bluetooth",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DiscoveryContent(
    uiState: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    onDisconnectDevice: (BluetoothDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Discovery Header
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nearby Devices",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary
                    )
                    
                    if (uiState.isScanning) {
                        ScanningIndicator(onStopScan = onStopScan)
                    } else {
                        Button(
                            onClick = onStartScan,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TrueTapPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan", fontSize = 14.sp)
                        }
                    }
                }
                
                Text(
                    text = "Discover nearby Solana Seeker devices for seamless payments",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Connected Device
        uiState.connectedDevice?.let { device ->
            item {
                ConnectedDeviceCard(
                    device = device,
                    onDisconnect = { onDisconnectDevice(device) }
                )
            }
        }
        
        // Discovered Devices
        if (uiState.discoveredDevices.isEmpty() && !uiState.isScanning) {
            item {
                EmptyStateContent(onStartScan = onStartScan)
            }
        } else {
            items(uiState.discoveredDevices) { device ->
                DeviceCard(
                    device = device,
                    onConnect = { onConnectDevice(device) }
                )
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ScanningIndicator(
    onStopScan: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Button(
        onClick = onStopScan,
        colors = ButtonDefaults.buttonColors(
            containerColor = TrueTapError
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.BluetoothSearching,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Stop", fontSize = 14.sp)
    }
}

@Composable
private fun ConnectedDeviceCard(
    device: BluetoothDevice,
    onDisconnect: () -> Unit
) {
    Surface(
        color = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, TrueTapSuccess)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = TrueTapSuccess
            ) {
                Icon(
                    imageVector = Icons.Default.BluetoothConnected,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = "Connected • ${getSignalStrength(device.rssi)}",
                    fontSize = 14.sp,
                    color = TrueTapSuccess
                )
                device.walletAddress?.let { address ->
                    Text(
                        text = "${address.take(8)}...${address.takeLast(4)}",
                        fontSize = 12.sp,
                        color = TrueTapTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            TextButton(onClick = onDisconnect) {
                Text(
                    text = "Disconnect",
                    color = TrueTapError,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: BluetoothDevice,
    onConnect: () -> Unit
) {
    Surface(
        color = TrueTapContainer,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.clickable { onConnect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = TrueTapBackground
            ) {
                Icon(
                    imageVector = if (device.isConnecting) 
                        Icons.Default.BluetoothSearching 
                    else 
                        Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = TrueTapPrimary,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = getSignalStrength(device.rssi),
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
                Text(
                    text = device.address,
                    fontSize = 12.sp,
                    color = TrueTapTextInactive,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            if (device.isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = TrueTapPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TrueTapTextInactive
                )
            }
        }
    }
}

@Composable
private fun EmptyStateContent(
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.DeviceUnknown,
            contentDescription = null,
            tint = TrueTapTextInactive,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No devices found",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Make sure nearby devices have Bluetooth enabled and are in pairing mode",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onStartScan,
            colors = ButtonDefaults.buttonColors(
                containerColor = TrueTapPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Scanning",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun getSignalStrength(rssi: Int): String {
    return when {
        rssi >= -50 -> "Excellent"
        rssi >= -60 -> "Good"
        rssi >= -70 -> "Fair"
        else -> "Weak"
    }
}