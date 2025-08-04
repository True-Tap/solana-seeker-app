package com.truetap.solana.seeker.presentation.screens.bluetooth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.data.bluetooth.*
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.presentation.components.*

/**
 * BluetoothPaymentScreen - TrueTap
 * Comprehensive Bluetooth payment screen with device discovery, connection, and payment flow
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothPaymentScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BluetoothViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var paymentAmount by remember { mutableStateOf("") }
    var selectedToken by remember { mutableStateOf("SOL") }
    var paymentMemo by remember { mutableStateOf("") }
    var showPaymentInput by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    
    // Permission Dialog
    if (uiState.showPermissionDialog) {
        BluetoothPermissionDialog(
            onGrantPermission = viewModel::requestBluetoothPermission,
            onDismiss = viewModel::hidePermissionDialog
        )
    }
    
    // Error Dialog
    uiState.errorMessage?.let { error ->
        ErrorDialog(
            errorMessage = error,
            onDismiss = viewModel::clearError
        )
    }
    
    // Connection Dialog
    if (uiState.showConnectionDialog) {
        ConnectionDialog(
            connectionState = uiState.connectionState,
            device = selectedDevice,
            onDismiss = viewModel::hideConnectionDialog
        )
    }
    
    // Payment Dialog
    if (uiState.showPaymentDialog) {
        PaymentDialog(
            paymentRequest = uiState.currentPaymentRequest,
            paymentStatus = uiState.paymentStatus,
            onCancel = viewModel::cancelPayment,
            onDismiss = viewModel::hidePaymentDialog
        )
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
        BluetoothHeader(
            title = "Bluetooth Payment",
            onNavigateBack = onNavigateBack,
            connectedDevice = uiState.connectedDevice,
            onDisconnect = viewModel::disconnectDevice
        )
        
        // Main Content
        when {
            !uiState.hasBluetoothPermission -> {
                PermissionRequiredContent(
                    onRequestPermission = viewModel::showPermissionDialog
                )
            }
            !uiState.isBluetoothEnabled -> {
                BluetoothDisabledContent(
                    onEnableBluetooth = viewModel::enableBluetooth
                )
            }
            showPaymentInput && selectedDevice != null -> {
                PaymentInputContent(
                    device = selectedDevice!!,
                    amount = paymentAmount,
                    onAmountChange = { paymentAmount = it },
                    selectedToken = selectedToken,
                    onTokenChange = { selectedToken = it },
                    memo = paymentMemo,
                    onMemoChange = { paymentMemo = it },
                    onSendPayment = { device, amount, token, memo ->
                        viewModel.initiatePayment(device, amount.toDoubleOrNull() ?: 0.0, token, memo)
                    },
                    onCancel = { 
                        showPaymentInput = false
                        selectedDevice = null
                    }
                )
            }
            else -> {
                BluetoothDiscoveryContent(
                    uiState = uiState,
                    onStartScan = viewModel::startScanning,
                    onStopScan = viewModel::stopScanning,
                    onConnectDevice = { device ->
                        selectedDevice = device
                        viewModel.connectToDevice(device)
                    },
                    onInitiatePayment = { device ->
                        selectedDevice = device
                        showPaymentInput = true
                    },
                    onRefresh = viewModel::refreshDeviceList
                )
            }
        }
    }
}

@Composable
private fun BluetoothHeader(
    title: String,
    onNavigateBack: () -> Unit,
    connectedDevice: BluetoothDevice?,
    onDisconnect: () -> Unit
) {
    Surface(
        color = TrueTapContainer,
        shadowElevation = 2.dp
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
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                connectedDevice?.let { device ->
                    Text(
                        text = "Connected to ${device.name}",
                        fontSize = 12.sp,
                        color = TrueTapSuccess,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            if (connectedDevice != null) {
                TextButton(
                    onClick = onDisconnect,
                    modifier = Modifier.size(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Disconnect",
                        fontSize = 10.sp,
                        color = TrueTapError
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(32.dp))
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
            text = "To discover and connect to nearby TrueTap devices for payments, we need Bluetooth permission.",
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
            text = "Please enable Bluetooth to discover nearby TrueTap devices for seamless payments.",
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
private fun BluetoothDiscoveryContent(
    uiState: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    onInitiatePayment: (BluetoothDevice) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Scanning Header
            ScanningHeaderSection(
                isScanning = uiState.isScanning,
                scanDuration = uiState.scanDuration,
                maxScanDuration = uiState.maxScanDuration,
                onStartScan = onStartScan,
                onStopScan = onStopScan,
                onRefresh = onRefresh
            )
        }
        
        // Connected Device Section
        uiState.connectedDevice?.let { device ->
            item {
                ConnectedDeviceSection(
                    device = device,
                    onInitiatePayment = { onInitiatePayment(device) }
                )
            }
        }
        
        // TrueTap Users Section
        if (uiState.nearbyTrueTapUsers.isNotEmpty()) {
            item {
                Text(
                    text = "Nearby TrueTap Users",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
            }
            
            items(uiState.nearbyTrueTapUsers) { device ->
                TrueTapUserCard(
                    device = device,
                    onConnect = { onConnectDevice(device) }
                )
            }
        }
        
        // All Devices Section
        if (uiState.discoveredDevices.isNotEmpty()) {
            item {
                Text(
                    text = "All Bluetooth Devices",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
            }
            
            items(uiState.discoveredDevices) { device ->
                if (!device.isTrueTapUser) {
                    BluetoothDeviceCard(
                        device = device,
                        onConnect = { onConnectDevice(device) }
                    )
                }
            }
        }
        
        // Empty State
        if (uiState.discoveredDevices.isEmpty() && !uiState.isScanning) {
            item {
                EmptyStateContent(onStartScan = onStartScan)
            }
        }
    }
}

@Composable
private fun PaymentInputContent(
    device: BluetoothDevice,
    amount: String,
    onAmountChange: (String) -> Unit,
    selectedToken: String,
    onTokenChange: (String) -> Unit,
    memo: String,
    onMemoChange: (String) -> Unit,
    onSendPayment: (BluetoothDevice, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Recipient Info
        Card(
            colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(TrueTapPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (device.isTrueTapUser) {
                        Text(
                            text = device.userName?.take(2)?.uppercase() ?: device.name.take(2),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = TrueTapPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = device.userName ?: device.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    Text(
                        text = device.walletAddress?.take(8) ?: "No wallet",
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )
                }
            }
        }
        
        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Text(
                    text = selectedToken,
                    color = TrueTapTextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        )
        
        // Token Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            device.supportedTokens.forEach { token ->
                FilterChip(
                    onClick = { onTokenChange(token) },
                    label = { Text(token) },
                    selected = selectedToken == token,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Memo Input
        OutlinedTextField(
            value = memo,
            onValueChange = onMemoChange,
            label = { Text("Memo (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = { onSendPayment(device, amount, selectedToken, memo) },
                enabled = amount.toDoubleOrNull() != null && amount.toDouble() > 0,
                colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Send Payment")
            }
        }
    }
}