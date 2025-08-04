package com.truetap.solana.seeker.ui.truetap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.repositories.TrueTapContact
import com.truetap.solana.seeker.ui.theme.TrueTapPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapContainer
import com.truetap.solana.seeker.ui.theme.TrueTapTextPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapTextSecondary

@Composable
fun RecipientSelector(
    contacts: List<TrueTapContact>,
    onSelect: (TrueTapContact) -> Unit
) {
    Column {
        Text(
            "Send to",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TrueTapTextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(contacts) { contact ->
                ContactItem(
                    contact = contact,
                    onClick = { onSelect(contact) }
                )
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: TrueTapContact,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TrueTapPrimary)
            ) {
                Text(
                    contact.name.first().toString(),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapTextPrimary
                )
                Text(
                    "${contact.address.take(6)}...${contact.address.takeLast(4)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TrueTapTextSecondary
                )
            }
            
            if (contact.isMerchant) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = "Merchant",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (contact.isGroup) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Group",
                    tint = TrueTapPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}