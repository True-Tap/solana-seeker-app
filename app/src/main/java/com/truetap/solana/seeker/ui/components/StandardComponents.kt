package com.truetap.solana.seeker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.truetap.solana.seeker.ui.components.layouts.TrueTapSpacing
import com.truetap.solana.seeker.ui.components.layouts.TrueTapTypography
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings

/**
 * TrueTap Design System - Standard UI Components
 * 
 * Standardized components that ensure consistency across all screens.
 * Use these instead of creating custom implementations.
 */

// Card Types
enum class TrueTapCardStyle {
    DEFAULT,        // Standard card with subtle elevation
    ELEVATED,       // Higher elevation for important content
    OUTLINED,       // Border instead of shadow
    FILLED          // Colored background
}

enum class TrueTapButtonStyle {
    PRIMARY,        // Main action button
    SECONDARY,      // Secondary action
    OUTLINE,        // Outlined button
    TEXT,           // Text-only button
    ICON            // Icon button
}

/**
 * Standard TrueTap Card Component
 */
@Composable
fun TrueTapCard(
    modifier: Modifier = Modifier,
    style: TrueTapCardStyle = TrueTapCardStyle.DEFAULT,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    val cardModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else modifier
    
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(TrueTapSpacing.md),
        colors = CardDefaults.cardColors(
            containerColor = when (style) {
                TrueTapCardStyle.DEFAULT -> dynamicColors.surface
                TrueTapCardStyle.ELEVATED -> dynamicColors.surface
                TrueTapCardStyle.OUTLINED -> dynamicColors.surface
                TrueTapCardStyle.FILLED -> dynamicColors.primary.copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when (style) {
                TrueTapCardStyle.DEFAULT -> 2.dp
                TrueTapCardStyle.ELEVATED -> 8.dp
                TrueTapCardStyle.OUTLINED -> 0.dp
                TrueTapCardStyle.FILLED -> 2.dp
            }
        ),
        border = if (style == TrueTapCardStyle.OUTLINED) {
            BorderStroke(1.dp, dynamicColors.outline)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(TrueTapSpacing.md),
            content = content
        )
    }
}

/**
 * Standard TrueTap Button Component
 */
@Composable
fun TrueTapButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TrueTapButtonStyle = TrueTapButtonStyle.PRIMARY,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    loading: Boolean = false
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    val buttonHeight = if (accessibility.largeButtonMode) 56.dp else 48.dp
    
    when (style) {
        TrueTapButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier.height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = dynamicColors.primary,
                    contentColor = dynamicColors.onPrimary,
                    disabledContainerColor = dynamicColors.outline,
                    disabledContentColor = dynamicColors.onSurface.copy(alpha = 0.38f)
                ),
                shape = RoundedCornerShape(TrueTapSpacing.sm)
            ) {
                TrueTapButtonContent(text, icon, loading)
            }
        }
        
        TrueTapButtonStyle.SECONDARY -> {
            Button(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier.height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = dynamicColors.surface,
                    contentColor = dynamicColors.primary,
                    disabledContainerColor = dynamicColors.outline,
                    disabledContentColor = dynamicColors.onSurface.copy(alpha = 0.38f)
                ),
                shape = RoundedCornerShape(TrueTapSpacing.sm)
            ) {
                TrueTapButtonContent(text, icon, loading)
            }
        }
        
        TrueTapButtonStyle.OUTLINE -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier.height(buttonHeight),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = dynamicColors.primary,
                    disabledContentColor = dynamicColors.onSurface.copy(alpha = 0.38f)
                ),
                border = BorderStroke(1.dp, dynamicColors.primary),
                shape = RoundedCornerShape(TrueTapSpacing.sm)
            ) {
                TrueTapButtonContent(text, icon, loading)
            }
        }
        
        TrueTapButtonStyle.TEXT -> {
            TextButton(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier.height(buttonHeight),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = dynamicColors.primary,
                    disabledContentColor = dynamicColors.onSurface.copy(alpha = 0.38f)
                )
            ) {
                TrueTapButtonContent(text, icon, loading)
            }
        }
        
        TrueTapButtonStyle.ICON -> {
            IconButton(
                onClick = onClick,
                enabled = enabled && !loading,
                modifier = modifier.size(buttonHeight)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = dynamicColors.primary,
                        strokeWidth = 2.dp
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = dynamicColors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TrueTapButtonContent(
    text: String,
    icon: ImageVector?,
    loading: Boolean
) {
    val accessibility = LocalAccessibilitySettings.current
    
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = text,
                fontSize = if (accessibility.largeButtonMode) TrueTapTypography.titleMedium else TrueTapTypography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Standard Section Header
 */
@Composable
fun TrueTapSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: @Composable () -> Unit = {}
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = if (accessibility.largeButtonMode) TrueTapTypography.headlineMedium else TrueTapTypography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = dynamicColors.textPrimary
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = TrueTapTypography.bodyMedium,
                    color = dynamicColors.textSecondary,
                    modifier = Modifier.padding(top = TrueTapSpacing.xs)
                )
            }
        }
        
        action()
    }
}

/**
 * Standard List Item Component
 */
@Composable
fun TrueTapListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    val itemModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else modifier
    
    Row(
        modifier = itemModifier
            .fillMaxWidth()
            .padding(vertical = TrueTapSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon
        leadingIcon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = dynamicColors.textSecondary,
                modifier = Modifier
                    .size(if (accessibility.largeButtonMode) 28.dp else 24.dp)
                    .padding(end = TrueTapSpacing.md)
            )
        }
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = if (accessibility.largeButtonMode) TrueTapTypography.titleLarge else TrueTapTypography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = dynamicColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = TrueTapTypography.bodyMedium,
                    color = dynamicColors.textSecondary,
                    modifier = Modifier.padding(top = TrueTapSpacing.xs),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Trailing Content
        trailingContent()
    }
}

/**
 * Standard Avatar Component
 */
@Composable
fun TrueTapAvatar(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 48,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    val avatarSize = if (accessibility.largeButtonMode) (size * 1.2f).dp else size.dp
    val bgColor = backgroundColor ?: dynamicColors.primary
    
    val avatarModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else modifier
    
    Box(
        modifier = avatarModifier
            .size(avatarSize)
            .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.take(2).uppercase(),
            fontSize = (size * 0.4).sp,
            fontWeight = FontWeight.Bold,
            color = dynamicColors.onPrimary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Standard Empty State Component
 */
@Composable
fun TrueTapEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox,
    action: @Composable () -> Unit = {}
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(TrueTapSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = dynamicColors.textSecondary,
            modifier = Modifier.size(if (accessibility.largeButtonMode) 80.dp else 64.dp)
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
        
        Text(
            text = title,
            fontSize = if (accessibility.largeButtonMode) TrueTapTypography.headlineMedium else TrueTapTypography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = dynamicColors.textPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.sm))
        
        Text(
            text = description,
            fontSize = TrueTapTypography.bodyLarge,
            color = dynamicColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = TrueTapTypography.bodyLarge * 1.4
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.xl))
        
        action()
    }
}

/**
 * Standard Loading State Component
 */
@Composable
fun TrueTapLoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(TrueTapSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(if (accessibility.largeButtonMode) 48.dp else 40.dp),
            color = dynamicColors.primary,
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(TrueTapSpacing.lg))
        
        Text(
            text = message,
            fontSize = TrueTapTypography.bodyLarge,
            color = dynamicColors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}