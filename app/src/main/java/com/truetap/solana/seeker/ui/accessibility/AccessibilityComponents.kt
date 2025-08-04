package com.truetap.solana.seeker.ui.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.theme.*

@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    enabled: Boolean = true
) {
    val accessibility = LocalAccessibilitySettings.current
    
    // Adjust size based on large button mode
    val buttonHeight = if (accessibility.largeButtonMode) 64.dp else 48.dp
    val fontSize = if (accessibility.largeButtonMode) 18.sp else 16.sp
    val iconSize = if (accessibility.largeButtonMode) 28.dp else 24.dp
    
    // Adjust colors based on high contrast mode
    val containerColor = if (accessibility.highContrastMode) {
        Color.Black
    } else {
        TrueTapPrimary
    }
    
    val contentColor = if (accessibility.highContrastMode) {
        Color.White
    } else {
        Color.White
    }
    
    Button(
        onClick = {
            onClick()
            // Play audio confirmation if enabled
            if (accessibility.audioConfirmations) {
                // TODO: Play confirmation sound
            }
        },
        modifier = modifier
            .size(height = buttonHeight, width = if (accessibility.largeButtonMode) 200.dp else 160.dp)
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (accessibility.highContrastMode) Color.DarkGray else ButtonDefaults.buttonColors().disabledContainerColor,
            disabledContentColor = if (accessibility.highContrastMode) Color.Gray else ButtonDefaults.buttonColors().disabledContentColor
        ),
        shape = RoundedCornerShape(if (accessibility.simplifiedUIMode) 4.dp else 12.dp),
        contentPadding = PaddingValues(
            horizontal = if (accessibility.largeButtonMode) 20.dp else 16.dp,
            vertical = if (accessibility.largeButtonMode) 16.dp else 12.dp
        ),
        enabled = enabled
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = contentColor
            )
        }
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = if (accessibility.highContrastMode) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun AccessibleCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    
    // Adjust elevation and colors based on accessibility settings
    val elevation = if (accessibility.simplifiedUIMode) 0.dp else 4.dp
    val containerColor = if (accessibility.highContrastMode) {
        Color.White
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val shape = if (accessibility.simplifiedUIMode) {
        RoundedCornerShape(4.dp)
    } else {
        RoundedCornerShape(16.dp)
    }
    
    Card(
        onClick = onClick ?: {},
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = shape
    ) {
        content()
    }
}

@Composable
fun AccessibleText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    contentDescription: String? = null
) {
    val accessibility = LocalAccessibilitySettings.current
    
    // Adjust text style based on accessibility settings
    val adjustedStyle = if (accessibility.highContrastMode) {
        style.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    } else {
        style
    }
    
    Text(
        text = text,
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        },
        style = adjustedStyle
    )
}