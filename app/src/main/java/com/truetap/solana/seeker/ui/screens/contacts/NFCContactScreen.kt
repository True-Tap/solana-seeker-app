package com.truetap.solana.seeker.ui.screens.contacts

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCContactScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isScanning by remember { mutableStateOf(true) }
    var scanningText by remember { mutableStateOf("Bring devices close together...") }
    var showSuccess by remember { mutableStateOf(false) }
    var contactName by remember { mutableStateOf("Alex Chen") }
    
    // Animation for NFC icon pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Ripple effect animation
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_scale"
    )
    
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_alpha"
    )
    
    // Simulate scanning states and success
    LaunchedEffect(Unit) {
        delay(2000)
        scanningText = "Keep devices close..."
        delay(2000)
        scanningText = "Detecting device..."
        delay(1000)
        scanningText = "Exchanging contact information..."
        delay(1000)
        isScanning = false
        showSuccess = true
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
                    text = "NFC Contact",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            if (showSuccess) {
                // Success Screen
                SuccessScreen(
                    contactName = contactName,
                    onNavigateBack = onNavigateBack
                )
            } else {
                // Scanning Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // NFC Animation Area
                    Box(
                        modifier = Modifier.size(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ripple effects
                        if (isScanning) {
                            repeat(3) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .scale(rippleScale + (index * 0.3f))
                                        .alpha(rippleAlpha * (1f - index * 0.3f))
                                        .background(
                                            TrueTapPrimary.copy(alpha = 0.1f),
                                            CircleShape
                                        )
                                )
                            }
                        }
                        
                        // Main NFC icon
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(if (isScanning) pulseScale else 1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isScanning) TrueTapPrimary else TrueTapSuccess
                            ),
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isScanning) Icons.Default.Nfc else Icons.Default.CheckCircle,
                                    contentDescription = if (isScanning) "NFC" else "Success",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = scanningText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Hold the back of your device close to another TrueTap user's device. The NFC will automatically detect and exchange contact information.",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "NFC Tips",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapTextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        listOf(
                            "Remove phone cases for better connection",
                            "Keep devices within 4cm of each other",
                            "Make sure NFC is enabled on both devices",
                            "Hold steady for 2-3 seconds"
                        ).forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    color = TrueTapPrimary,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = tip,
                                    fontSize = 14.sp,
                                    color = TrueTapTextSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                    if (isScanning) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = TrueTapPrimary,
                            trackColor = TrueTapContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessScreen(
    contactName: String,
    onNavigateBack: () -> Unit
) {
    // Success animation
    val infiniteTransition = rememberInfiniteTransition(label = "success_celebration")
    val celebrationScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "celebration_scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Animation
        Card(
            modifier = Modifier
                .size(140.dp)
                .scale(celebrationScale),
            colors = CardDefaults.cardColors(containerColor = TrueTapSuccess),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Contact Added Successfully!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "You and $contactName are now connected",
            fontSize = 16.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Contact Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(56.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapPrimary),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Contact",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = contactName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    
                    Text(
                        text = "Added via NFC",
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = TrueTapSuccess.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✓ Connected",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "🎉 You can now send payments and messages to $contactName",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}