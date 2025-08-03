package com.truetap.solana.seeker.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*

@Composable
fun ContactDetailsScreen(
    contactId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Contact Details",
            style = MaterialTheme.typography.headlineLarge,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Contact ID: $contactId",
            fontSize = 16.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Contact details coming soon",
            fontSize = 16.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
        ) {
            Text("Go Back")
        }
    }
}