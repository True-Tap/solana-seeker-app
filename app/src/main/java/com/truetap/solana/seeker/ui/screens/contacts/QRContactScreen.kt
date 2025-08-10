package com.truetap.solana.seeker.ui.screens.contacts

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRContactScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val walletState by walletViewModel.walletState.collectAsState()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(walletState.account?.publicKey) {
        // Generate QR code from wallet account
        walletState.account?.publicKey?.let { publicKey ->
            // Using wallet public key for QR generation
            withContext(Dispatchers.Default) {
                try {
                    val contactData = buildString {
                        append("solana:")
                        append(publicKey)
                        append("?label=TrueTap User")
                    }
                    // Contact data prepared for QR encoding
                    
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(
                        contactData,
                        BarcodeFormat.QR_CODE,
                        400,
                        400
                    )
                    // QR code matrix generated
                    
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    // QR code bitmap generated successfully
                    
                    qrBitmap = bitmap
                    isLoading = false
                } catch (e: Exception) {
                    // QR code generation failed
                    e.printStackTrace()
                    isLoading = false
                }
            }
        } ?: run {
            // No wallet connected - using mock data for demo
            // For testing, use a mock public key
            withContext(Dispatchers.Default) {
                try {
                    val mockPublicKey = "4xZvZKGs8KVZAKfjV3LmM4bUjpTZ1bBu9QvfyYjRsCCh" // Mock Solana address
                    val contactData = buildString {
                        append("solana:")
                        append(mockPublicKey)
                        append("?label=TrueTap User (Demo)")
                    }
                    // Using demo contact data for QR code
                    
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(
                        contactData,
                        BarcodeFormat.QR_CODE,
                        400,
                        400
                    )
                    
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    
                    qrBitmap = bitmap
                    isLoading = false
                } catch (e: Exception) {
                    // Mock QR code generation failed
                    e.printStackTrace()
                    isLoading = false
                }
            }
        }
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TrueTapTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Share Contact",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                IconButton(
                    onClick = {
                        // TODO: Implement sharing functionality
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TrueTapPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(320.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    color = TrueTapPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            qrBitmap != null -> {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "QR Code for contact",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Error generating QR code",
                                        fontSize = 16.sp,
                                        color = TrueTapError,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    TextButton(
                                        onClick = {
                                            isLoading = true
                                            // Retry QR generation
                                        }
                                    ) {
                                        Text("Retry", color = TrueTapPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Scan this QR code to add me as a contact",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Others can scan this code with their TrueTap app to quickly add your wallet information to their contacts.",
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (walletState.account?.publicKey != null) "Your Wallet Address" else "Demo Wallet Address",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TrueTapTextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = walletState.account?.publicKey ?: "4xZvZKGs8KVZAKfjV3LmM4bUjpTZ1bBu9QvfyYjRsCCh",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TrueTapTextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}