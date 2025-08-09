package com.truetap.solana.seeker.ui.screens.contacts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.data.models.WalletType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailModal(
    contact: ModernContact,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteContact: () -> Unit,
    onEditContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = TrueTapContainer,
        contentColor = TrueTapTextPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(TrueTapTextSecondary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header with contact info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(TrueTapPrimary, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.initials,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = contact.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onEditContact,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Contact",
                                tint = TrueTapTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${contact.wallets.size} wallet${if (contact.wallets.size != 1) "s" else ""}",
                        fontSize = 14.sp,
                        color = TrueTapTextSecondary
                    )
                }
                
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (contact.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (contact.isFavorite) TrueTapPrimary else TrueTapTextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Wallets Section
            Text(
                text = "Wallets",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TrueTapTextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            contact.wallets.forEach { wallet ->
                WalletCard(
                    wallet = wallet,
                    onSendClick = { /* Handle send to this wallet */ },
                    onEditClick = { /* Handle edit wallet */ },
                    onDeleteClick = { /* Handle delete wallet */ },
                    onCopyClick = { /* Handle copy wallet address */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEditContact,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TrueTapTextPrimary
                    ),
                    border = BorderStroke(1.dp, TrueTapTextSecondary.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
                
                Button(
                    onClick = onDeleteContact,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TrueTapError)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun WalletCard(
    wallet: ContactWallet,
    onSendClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallet.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${wallet.address.take(8)}...${wallet.address.takeLast(4)}",
                            fontSize = 12.sp,
                            color = TrueTapTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onCopyClick,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Address",
                                tint = TrueTapTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button on the left - less likely to be accidentally clicked
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Wallet",
                        tint = TrueTapError,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSendClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send", fontSize = 12.sp)
                    }
                    
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Wallet",
                            tint = TrueTapTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactModal(
    onDismiss: () -> Unit,
    onContactAdded: (ModernContact) -> Unit,
    modifier: Modifier = Modifier
) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var selectedMethod by remember { mutableStateOf(AddContactMethod.NFC) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = TrueTapContainer,
        contentColor = TrueTapTextPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(TrueTapTextSecondary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Add Contact",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Method Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AddContactMethod.values()) { method ->
                    AddMethodTab(
                        method = method,
                        isSelected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Content based on selected method
            when (selectedMethod) {
                AddContactMethod.NFC -> NFCAddContent()
                AddContactMethod.BLUETOOTH -> BluetoothAddContent()
                AddContactMethod.QR_CODE -> QRCodeAddContent()
                AddContactMethod.SEND_LINK -> SendLinkAddContent()
                AddContactMethod.MANUAL -> ManualAddContent(onContactAdded = onContactAdded)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AddMethodTab(
    method: AddContactMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (method) {
        AddContactMethod.NFC -> Icons.Default.Nfc
        AddContactMethod.BLUETOOTH -> Icons.Default.Bluetooth
        AddContactMethod.QR_CODE -> Icons.Default.QrCode
        AddContactMethod.SEND_LINK -> Icons.Default.Share
        AddContactMethod.MANUAL -> Icons.Default.Edit
    }
    
    val label = when (method) {
        AddContactMethod.NFC -> "NFC"
        AddContactMethod.BLUETOOTH -> "Bluetooth"
        AddContactMethod.QR_CODE -> "QR Code"
        AddContactMethod.SEND_LINK -> "Send Link"
        AddContactMethod.MANUAL -> "Manual"
    }
    
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TrueTapPrimary.copy(alpha = 0.1f) else Color(0xFFFFF8F5)
        ),
        border = if (isSelected) BorderStroke(2.dp, TrueTapPrimary) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) TrueTapPrimary else TrueTapTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) TrueTapPrimary else TrueTapTextSecondary
            )
        }
    }
}

@Composable
private fun NFCAddContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Nfc,
            contentDescription = "NFC",
            tint = TrueTapPrimary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap devices together",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TrueTapTextPrimary
        )
        Text(
            text = "Hold your device close to theirs to exchange contact information",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BluetoothAddContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = "Bluetooth",
            tint = TrueTapPrimary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Connect via Bluetooth",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TrueTapTextPrimary
        )
        Text(
            text = "Make sure Bluetooth is enabled and discover nearby devices",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QRCodeAddContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.QrCode,
            contentDescription = "QR Code",
            tint = TrueTapPrimary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Scan QR Code",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TrueTapTextPrimary
        )
        Text(
            text = "Scan their QR code or let them scan yours",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SendLinkAddContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Send Link",
            tint = TrueTapPrimary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Send invitation link",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TrueTapTextPrimary
        )
        Text(
            text = "Share a link via message, email, or social media",
            fontSize = 14.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ManualAddContent(
    onContactAdded: (ModernContact) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var walletName by remember { mutableStateOf("Main Wallet") }
    var walletAddress by remember { mutableStateOf("") }
    
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Contact Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TrueTapPrimary,
                unfocusedBorderColor = TrueTapTextSecondary.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = walletName,
            onValueChange = { walletName = it },
            label = { Text("Wallet Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TrueTapPrimary,
                unfocusedBorderColor = TrueTapTextSecondary.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = walletAddress,
            onValueChange = { walletAddress = it },
            label = { Text("Wallet Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TrueTapPrimary,
                unfocusedBorderColor = TrueTapTextSecondary.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (name.isNotBlank() && walletAddress.isNotBlank()) {
                    val newContact = ModernContact(
                        id = java.util.UUID.randomUUID().toString(),
                        name = name,
                        initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString(""),
                        wallets = listOf(
                            ContactWallet(
                                id = java.util.UUID.randomUUID().toString(),
                                name = walletName.ifBlank { "Main Wallet" },
                                address = walletAddress,
                                type = WalletType.PERSONAL
                            )
                        ),
                        isFavorite = false
                    )
                    onContactAdded(newContact)
                }
            },
            enabled = name.isNotBlank() && walletAddress.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Add Contact",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}