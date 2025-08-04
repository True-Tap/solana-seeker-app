package com.truetap.solana.seeker.ui.truetap.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.TrueTapPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapTextPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapTextSecondary

@Composable
fun AmountInput(
    balance: Double,
    onConfirm: (Double, String) -> Unit,
    onBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                "Balance: ${balance.format(2)} SOL",
                style = MaterialTheme.typography.bodyMedium,
                color = TrueTapTextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Amount input field
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Allow only numbers and one decimal point
                    if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = newValue
                    }
                },
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (amount.isEmpty()) {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = TrueTapTextSecondary
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            Text(
                "SOL",
                style = MaterialTheme.typography.bodyLarge,
                color = TrueTapTextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick amount buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(5, 10, 20, 50).forEach { quickAmount ->
                OutlinedButton(
                    onClick = { amount = quickAmount.toString() }
                ) {
                    Text("$quickAmount")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Emoji message
        OutlinedTextField(
            value = emoji,
            onValueChange = { emoji = it },
            label = { Text("add message here") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TrueTapTextPrimary,
                unfocusedTextColor = TrueTapTextPrimary,
                focusedLabelColor = TrueTapTextSecondary,
                unfocusedLabelColor = TrueTapTextSecondary,
                cursorColor = TrueTapPrimary,
                focusedBorderColor = TrueTapPrimary,
                unfocusedBorderColor = TrueTapTextSecondary
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Number pad
        NumberPad(
            onNumberClick = { digit ->
                amount = when (digit) {
                    "." -> if (!amount.contains(".")) amount + digit else amount
                    "⌫" -> if (amount.isNotEmpty()) amount.dropLast(1) else ""
                    else -> amount + digit
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                if (amountDouble > 0 && amountDouble <= balance) {
                    onConfirm(amountDouble, emoji)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = amount.toDoubleOrNull()?.let { it > 0 && it <= balance } ?: false,
            colors = ButtonDefaults.buttonColors(containerColor = TrueTapPrimary)
        ) {
            Text("Continue", color = Color.White)
        }
    }
}

@Composable
private fun NumberPad(onNumberClick: (String) -> Unit) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )
    
    Column {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    TextButton(
                        onClick = { onNumberClick(digit) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            digit,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TrueTapTextPrimary
                        )
                    }
                }
            }
        }
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)