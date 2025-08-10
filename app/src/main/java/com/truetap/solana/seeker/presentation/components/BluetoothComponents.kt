package com.truetap.solana.seeker.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.truetap.solana.seeker.data.bluetooth.*
import com.truetap.solana.seeker.ui.theme.TrueTapPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapTextPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapTextSecondary
import com.truetap.solana.seeker.ui.theme.TrueTapContainer
import com.truetap.solana.seeker.ui.theme.TrueTapSuccess
import com.truetap.solana.seeker.ui.theme.TrueTapError
import com.truetap.solana.seeker.ui.theme.TrueTapTextInactive
import com.truetap.solana.seeker.ui.theme.TrueTapBackground
import kotlin.math.cos
import kotlin.math.sin

/**
 * BluetoothComponents - TrueTap
 * Reusable Bluetooth UI components with Material 3 design and TrueTap theming
 */

@Composable
fun ScanningHeaderSection(
    isScanning: Boolean,
    scanDuration: Int,
    maxScanDuration: Int,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onRefresh: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Nearby Devices",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                if (isScanning) {
                    Text(
                        text = "Scanning... (${maxScanDuration - scanDuration}s)",
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )
                } else {
                    Text(
                        text = "Tap scan to find TrueTap users nearby",
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )
                }
            }
            
            if (isScanning) {
                ScanningIndicator(onStopScan = onStopScan)
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TrueTapTextSecondary
                        )
                    }
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
        }
        
        // Scanning Progress Bar
        if (isScanning) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { scanDuration.toFloat() / maxScanDuration },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = TrueTapPrimary,
                trackColor = TrueTapTextInactive.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ScanningIndicator(
    onStopScan: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    
    // Radar sweep animation
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )
    
    // Pulsing scale animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Button(
        onClick = onStopScan,
        colors = ButtonDefaults.buttonColors(
            containerColor = TrueTapError
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radar background circles
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .scale(pulseScale)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        CircleShape
                    )
            )
            
            // Rotating radar sweep
            Icon(
                imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(12.dp)
                    .scale(pulseScale)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text("Stop", fontSize = 14.sp)
    }
}

@Composable
fun ConnectedDeviceSection(
    device: BluetoothDevice,
    onInitiatePayment: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, TrueTapSuccess)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(TrueTapSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BluetoothConnected,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.userName ?: device.name,
                        fontSize = 18.sp,
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
                            color = TrueTapTextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onInitiatePayment,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrueTapPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send Payment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TrueTapUserCard(
    device: BluetoothDevice,
    onConnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.clickable { onConnect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(TrueTapPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = device.userName?.take(2)?.uppercase() ?: device.name.take(2),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = device.userName ?: device.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(TrueTapSuccess, CircleShape)
                    )
                }
                
                Text(
                    text = "${getSignalStrength(device.rssi)} • ${device.distance}",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
                
                // Supported tokens
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    device.supportedTokens.take(3).forEach { token ->
                        Text(
                            text = token,
                            fontSize = 10.sp,
                            color = TrueTapTextSecondary,
                            modifier = Modifier
                                .background(
                                    TrueTapTextInactive.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
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
fun BluetoothDeviceCard(
    device: BluetoothDevice,
    onConnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.clickable { onConnect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TrueTapBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (device.deviceType) {
                        BluetoothDeviceType.SOLANA_PAY_DEVICE -> Icons.Default.Payment
                        BluetoothDeviceType.SMARTPHONE -> Icons.Default.Phone
                        BluetoothDeviceType.TABLET -> Icons.Default.Tablet
                        else -> Icons.Default.Bluetooth
                    },
                    contentDescription = null,
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextPrimary
                )
                Text(
                    text = "${getSignalStrength(device.rssi)} • ${device.deviceType.name.replace("_", " ")}",
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
                    modifier = Modifier.size(20.dp),
                    color = TrueTapPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TrueTapTextInactive,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateContent(
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
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
            text = "Make sure nearby devices have Bluetooth enabled and are discoverable",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
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

@Composable
fun ConnectionDialog(
    connectionState: BluetoothConnectionState,
    device: BluetoothDevice?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (connectionState) {
                    BluetoothConnectionState.Connecting -> "Connecting..."
                    BluetoothConnectionState.Pairing -> "Pairing..."
                    BluetoothConnectionState.Connected -> "Connected!"
                    is BluetoothConnectionState.Error -> "Connection Failed"
                    else -> "Connection"
                },
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (connectionState) {
                    BluetoothConnectionState.Connecting -> {
                        CircularProgressIndicator(
                            color = TrueTapPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connecting to ${device?.name ?: "device"}...",
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    BluetoothConnectionState.Pairing -> {
                        CircularProgressIndicator(
                            color = TrueTapPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pairing with ${device?.name ?: "device"}...\nPlease confirm pairing on both devices.",
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    BluetoothConnectionState.Connected -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TrueTapSuccess,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Successfully connected to ${device?.name ?: "device"}!",
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    is BluetoothConnectionState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = TrueTapError,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = connectionState.message,
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            when (connectionState) {
                BluetoothConnectionState.Connected,
                is BluetoothConnectionState.Error -> {
                    TextButton(onClick = onDismiss) {
                        Text("OK", color = TrueTapPrimary)
                    }
                }
                else -> {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TrueTapTextSecondary)
                    }
                }
            }
        },
        containerColor = TrueTapContainer
    )
}

@Composable
fun PaymentDialog(
    paymentRequest: BluetoothPaymentRequest?,
    paymentStatus: BluetoothPaymentStatus,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    if (paymentRequest == null) return
    
    AlertDialog(
        onDismissRequest = { if (paymentStatus == BluetoothPaymentStatus.COMPLETED) onDismiss() },
        title = {
            Text(
                text = when (paymentStatus) {
                    BluetoothPaymentStatus.INITIATING -> "Initiating Payment..."
                    BluetoothPaymentStatus.WAITING_FOR_CONFIRMATION -> "Waiting for Confirmation"
                    BluetoothPaymentStatus.PROCESSING -> "Processing Payment..."
                    BluetoothPaymentStatus.COMPLETED -> "Payment Sent!"
                    BluetoothPaymentStatus.FAILED -> "Payment Failed"
                    BluetoothPaymentStatus.CANCELLED -> "Payment Cancelled"
                    else -> "Payment"
                },
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (paymentStatus) {
                    BluetoothPaymentStatus.INITIATING,
                    BluetoothPaymentStatus.PROCESSING -> {
                        CircularProgressIndicator(
                            color = TrueTapPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    BluetoothPaymentStatus.WAITING_FOR_CONFIRMATION -> {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TrueTapPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    BluetoothPaymentStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TrueTapSuccess,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    BluetoothPaymentStatus.FAILED -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = TrueTapError,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    else -> {}
                }
                
                Text(
                    text = "${paymentRequest.amount} ${paymentRequest.token}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "to ${paymentRequest.recipientDevice.userName ?: paymentRequest.recipientDevice.name}",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
                
                paymentRequest.memo?.let { memo ->
                    if (memo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"$memo\"",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(
                                    TrueTapTextInactive.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = when (paymentStatus) {
                        BluetoothPaymentStatus.INITIATING -> "Preparing payment request..."
                        BluetoothPaymentStatus.WAITING_FOR_CONFIRMATION -> "Waiting for recipient to accept payment..."
                        BluetoothPaymentStatus.PROCESSING -> "Processing transaction on Solana..."
                        BluetoothPaymentStatus.COMPLETED -> "Payment sent successfully!"
                        BluetoothPaymentStatus.FAILED -> "Payment was declined or failed to process"
                        BluetoothPaymentStatus.CANCELLED -> "Payment was cancelled"
                        else -> ""
                    },
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            when (paymentStatus) {
                BluetoothPaymentStatus.COMPLETED,
                BluetoothPaymentStatus.FAILED,
                BluetoothPaymentStatus.CANCELLED -> {
                    TextButton(onClick = onDismiss) {
                        Text("OK", color = TrueTapPrimary)
                    }
                }
                else -> {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = TrueTapError)
                    }
                }
            }
        },
        containerColor = TrueTapContainer
    )
}

@Composable
fun BluetoothPermissionDialog(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bluetooth Permission Required",
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
        },
        text = {
            Text(
                text = "TrueTap needs Bluetooth permission to discover nearby devices for payments. Please grant permission to continue.",
                color = TrueTapTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onGrantPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrueTapPrimary
                )
            ) {
                Text("Grant Permission", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TrueTapTextSecondary)
            }
        },
        containerColor = TrueTapContainer
    )
}

@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Error",
                fontWeight = FontWeight.Bold,
                color = TrueTapError
            )
        },
        text = {
            Text(
                text = errorMessage,
                color = TrueTapTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
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

// Utility function
private fun getSignalStrength(rssi: Int): String {
    return when {
        rssi >= -50 -> "Excellent"
        rssi >= -60 -> "Good"
        rssi >= -70 -> "Fair"
        else -> "Weak"
    }
}