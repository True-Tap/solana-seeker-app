package com.truetap.solana.seeker.ui.screens.contacts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
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
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendLinkContactScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val walletState by walletViewModel.walletState.collectAsState()
    var shareLink by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showQRCode by remember { mutableStateOf(false) }
    var copySuccess by remember { mutableStateOf(false) }
    
    // Generate share link
    LaunchedEffect(walletState.account?.publicKey) {
        val publicKey = walletState.account?.publicKey ?: "4xZvZKGs8KVZAKfjV3LmM4bUjpTZ1bBu9QvfyYjRsCCh"
        shareLink = "https://truetap.app/add-contact?address=${publicKey}&name=TrueTap%20User"
        
        // Generate QR code for the link
        withContext(Dispatchers.Default) {
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(
                    shareLink,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
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
            } catch (e: Exception) {
                // Handle QR generation error
            }
        }
    }
    
    // Reset copy success message
    LaunchedEffect(copySuccess) {
        if (copySuccess) {
            kotlinx.coroutines.delay(2000)
            copySuccess = false
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
                    text = "Send Link",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                IconButton(
                    onClick = { showQRCode = !showQRCode },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Toggle QR Code",
                        tint = if (showQRCode) TrueTapPrimary else TrueTapTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showQRCode && qrBitmap != null) {
                    // QR Code Display
                    Card(
                        modifier = Modifier
                            .size(280.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Share link QR code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Scan to add me as a contact",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Link Sharing Interface
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TrueTapContainer),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TrueTapPrimary,
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Share Your Contact Link",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrueTapTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Send this link to anyone and they can add you as a contact instantly",
                                fontSize = 14.sp,
                                color = TrueTapTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Link Display Card
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
                                text = "Your Contact Link",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TrueTapTextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TrueTapBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = shareLink,
                                        fontSize = 12.sp,
                                        color = TrueTapTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("TrueTap Contact Link", shareLink)
                                            clipboard.setPrimaryClip(clip)
                                            copySuccess = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy link",
                                            tint = TrueTapPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (copySuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Link copied to clipboard",
                            fontSize = 14.sp,
                            color = TrueTapSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("TrueTap Contact Link", shareLink)
                            clipboard.setPrimaryClip(clip)
                            copySuccess = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TrueTapContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = TrueTapPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Copy Link",
                            color = TrueTapPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Add me as a contact on TrueTap: $shareLink")
                                putExtra(Intent.EXTRA_SUBJECT, "Connect with me on TrueTap")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share contact link"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Link",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "💡 Tip: Toggle the QR code icon in the top right to switch between link and QR code sharing",
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}