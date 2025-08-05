package com.truetap.solana.seeker.ui.components.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.ui.components.BottomNavItem
import com.truetap.solana.seeker.ui.components.TrueTapBottomNavigationBar
import com.truetap.solana.seeker.ui.components.rememberBottomNavHandler
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings

/**
 * TrueTap Design System - Screen Layout Templates
 * 
 * Provides standardized screen layouts for consistent UI/UX across the app.
 * All screens should use these templates to ensure uniformity.
 */

// Standard spacing system
object TrueTapSpacing {
    val xs = 4.dp      // Micro spacing
    val sm = 8.dp      // Small spacing  
    val md = 16.dp     // Medium spacing
    val lg = 24.dp     // Large spacing
    val xl = 32.dp     // Extra large spacing
    val xxl = 48.dp    // Screen margins
}

// Typography scale
object TrueTapTypography {
    val displayLarge = 32.sp
    val displayMedium = 28.sp
    val headlineLarge = 24.sp
    val headlineMedium = 20.sp
    val titleLarge = 18.sp
    val titleMedium = 16.sp
    val bodyLarge = 16.sp
    val bodyMedium = 14.sp
    val bodySmall = 12.sp
    val labelLarge = 14.sp
    val labelMedium = 12.sp
    val labelSmall = 10.sp
}

/**
 * Standard Screen Template
 * Use this for most screens with standard header + content + bottom nav
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueTapScreenTemplate(
    title: String,
    currentTab: BottomNavItem,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dynamicColors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Status bar spacer
            Spacer(modifier = Modifier.height(TrueTapSpacing.xxl))
            
            // Standard Header
            TrueTapHeader(
                title = title,
                subtitle = subtitle,
                actions = actions
            )
            
            // Content Area
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = TrueTapSpacing.lg,
                    vertical = TrueTapSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(TrueTapSpacing.md),
                content = content
            )
            
            // Bottom Navigation
            TrueTapBottomNavigationBar(
                selectedTab = currentTab,
                onTabSelected = rememberBottomNavHandler(
                    currentScreen = currentTab,
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToSwap = onNavigateToSwap,
                    onNavigateToNFTs = onNavigateToNFTs,
                    onNavigateToContacts = onNavigateToContacts,
                    onNavigateToSettings = onNavigateToSettings
                )
            )
        }
        
        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = TrueTapSpacing.lg,
                    bottom = TrueTapSpacing.lg + 80.dp // Account for bottom nav
                )
        ) {
            floatingActionButton()
        }
    }
}

/**
 * Search-enabled Screen Template
 * Use for screens that need search functionality (Contacts, NFTs, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueTapSearchableScreenTemplate(
    title: String,
    currentTab: BottomNavItem,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToNFTs: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    searchPlaceholder: String = "Search...",
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dynamicColors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Status bar spacer
            Spacer(modifier = Modifier.height(TrueTapSpacing.xxl))
            
            // Searchable Header
            TrueTapSearchableHeader(
                title = title,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                searchPlaceholder = searchPlaceholder,
                isSearchExpanded = isSearchExpanded,
                onSearchExpandedChange = { isSearchExpanded = it },
                actions = actions
            )
            
            // Content Area
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = TrueTapSpacing.lg,
                    vertical = TrueTapSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(TrueTapSpacing.md),
                content = content
            )
            
            // Bottom Navigation
            TrueTapBottomNavigationBar(
                selectedTab = currentTab,
                onTabSelected = rememberBottomNavHandler(
                    currentScreen = currentTab,
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToSwap = onNavigateToSwap,
                    onNavigateToNFTs = onNavigateToNFTs,
                    onNavigateToContacts = onNavigateToContacts,
                    onNavigateToSettings = onNavigateToSettings
                )
            )
        }
        
        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = TrueTapSpacing.lg,
                    bottom = TrueTapSpacing.lg + 80.dp
                )
        ) {
            floatingActionButton()
        }
    }
}

/**
 * Modal/Detail Screen Template
 * Use for screens that don't need bottom navigation (modals, detail views, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueTapModalScreenTemplate(
    title: String,
    onNavigateBack: () -> Unit,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(dynamicColors.background)
    ) {
        // Status bar spacer
        Spacer(modifier = Modifier.height(TrueTapSpacing.xxl))
        
        // Modal Header with back button
        TrueTapModalHeader(
            title = title,
            subtitle = subtitle,
            onNavigateBack = onNavigateBack,
            actions = actions
        )
        
        // Content Area
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = TrueTapSpacing.lg,
                vertical = TrueTapSpacing.md
            ),
            verticalArrangement = Arrangement.spacedBy(TrueTapSpacing.md),
            content = content
        )
    }
}

// Header Components
@Composable
private fun TrueTapHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TrueTapSpacing.lg, vertical = TrueTapSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = if (accessibility.largeButtonMode) TrueTapTypography.displayMedium else TrueTapTypography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = dynamicColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = if (accessibility.largeButtonMode) TrueTapTypography.bodyLarge else TrueTapTypography.bodyMedium,
                        color = dynamicColors.textSecondary,
                        modifier = Modifier.padding(top = TrueTapSpacing.xs)
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.sm)) {
                actions()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrueTapSearchableHeader(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchPlaceholder: String,
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TrueTapSpacing.lg, vertical = TrueTapSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSearchExpanded) {
                Text(
                    text = title,
                    fontSize = if (accessibility.largeButtonMode) TrueTapTypography.displayMedium else TrueTapTypography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = dynamicColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.sm)) {
                IconButton(onClick = { onSearchExpandedChange(!isSearchExpanded) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = dynamicColors.textPrimary
                    )
                }
                actions()
            }
        }
        
        // Expandable Search Bar
        if (isSearchExpanded) {
            Spacer(modifier = Modifier.height(TrueTapSpacing.md))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(searchPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = dynamicColors.textSecondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dynamicColors.primary,
                    focusedLabelColor = dynamicColors.primary,
                    cursorColor = dynamicColors.primary
                ),
                shape = RoundedCornerShape(TrueTapSpacing.sm),
                singleLine = true
            )
        }
    }
}

@Composable
private fun TrueTapModalHeader(
    title: String,
    subtitle: String? = null,
    onNavigateBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TrueTapSpacing.lg, vertical = TrueTapSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = dynamicColors.textPrimary
            )
        }
        
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
        
        Row(horizontalArrangement = Arrangement.spacedBy(TrueTapSpacing.sm)) {
            actions()
        }
    }
}