package com.truetap.solana.seeker.ui.screens.contacts

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay

data class MockBluetoothDevice(
    val name: String,
    val deviceType: String,
    val distance: String,
    val isConnecting: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothContactScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isScanning by remember { mutableStateOf(true) }
    var devices by remember { mutableStateOf(listOf<MockBluetoothDevice>()) }
    var scanningText by remember { mutableStateOf("Scanning for nearby devices...") }
    
    // Scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "bluetooth_scan")
    val scanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_rotation"
    )
    
    // Simulate device discovery
    LaunchedEffect(Unit) {
        delay(1000)
        devices = listOf(
            MockBluetoothDevice("Alex's iPhone", "iPhone", "2m away")
        )
        delay(2000)
        devices = devices + MockBluetoothDevice("Sarah's Pixel", "Android", "5m away")
        delay(1500)
        devices = devices + MockBluetoothDevice("John's MacBook", "Laptop", "8m away")
        delay(2000)
        devices = devices + MockBluetoothDevice("Emma's AirPods", "Headphones", "3m away")
        delay(1000)
        scanningText = "Found ${devices.filter { it.deviceType in listOf("iPhone", "Android") }.size} TrueTap users nearby"
        delay(3000)
        isScanning = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TrueTapTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Bluetooth Discovery",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                IconButton(
                    onClick = {
                        isScanning = true
                        scanningText = "Scanning for nearby devices..."
                        devices = listOf()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TrueTapPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Scanning indicator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier.size(80.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isScanning) TrueTapPrimary else TrueTapSuccess
                                    ),
                                    shape = CircleShape
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                                            contentDescription = "Bluetooth",
                                            tint = Color.White,
                                            modifier = if (isScanning) 
                                                Modifier.size(40.dp).rotate(scanRotation) 
                                            else 
                                                Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = scanningText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrueTapTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            
                            if (isScanning) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = TrueTapPrimary,
                                    trackColor = TrueTapBackground
                                )
                            }
                        }
                    }
                }
                
                if (devices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Nearby Devices",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                items(devices) { device ->
                    DeviceCard(
                        device = device,
                        onConnect = { /* Handle connection */ }
                    )
                }
                
                if (!isScanning && devices.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothSearching,
                                    contentDescription = "No devices",
                                    tint = TrueTapTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "No TrueTap users found nearby",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrueTapTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Make sure Bluetooth is enabled and other users are also in discovery mode.",
                                    fontSize = 14.sp,
                                    color = TrueTapTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: MockBluetoothDevice,
    onConnect: () -> Unit
) {
    val isTrueTapUser = device.deviceType in listOf("iPhone", "Android")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isTrueTapUser) { onConnect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isTrueTapUser) TrueTapContainer else TrueTapContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTrueTapUser) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTrueTapUser) TrueTapPrimary else TrueTapTextSecondary
                ),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (device.deviceType) {
                            "iPhone", "Android" -> Icons.Default.Person
                            "Laptop" -> Icons.Default.Computer
                            "Headphones" -> Icons.Default.Headphones
                            else -> Icons.Default.Phone
                        },
                        contentDescription = device.deviceType,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isTrueTapUser) TrueTapTextPrimary else TrueTapTextSecondary
                )
                
                Text(
                    text = if (isTrueTapUser) "TrueTap User • ${device.distance}" else "${device.deviceType} • ${device.distance}",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary
                )
            }
            
            if (isTrueTapUser) {
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Connect",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            } else {
                Text(
                    text = "Not compatible",
                    fontSize = 12.sp,
                    color = TrueTapTextSecondary
                )
            }
        }
    }
}