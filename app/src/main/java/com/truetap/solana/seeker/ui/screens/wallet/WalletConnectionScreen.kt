package com.truetap.solana.seeker.ui.screens.wallet

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import kotlinx.coroutines.delay

data class WalletCategory(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: Int,
    val gradient: List<Color>
)

sealed class ConnectionState {
    object Selection : ConnectionState()
    object Success : ConnectionState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletConnectionScreen(
    onNavigateToNext: () -> Unit,
    onNavigateToPairing: (String) -> Unit,
    activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var currentState by remember { mutableStateOf<ConnectionState>(ConnectionState.Selection) }
    var selectedWallet by remember { mutableStateOf<String?>(null) }
    var lastChoiceId by remember { mutableStateOf<String?>(null) }
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "wallet_connection")
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    // Load last choice for badge/preselection hint
    LaunchedEffect(Unit) {
        lastChoiceId = viewModel.getLastWalletChoice()
    }
    
    // Detect installed wallets
    val packageManager = context.packageManager
    val phantomInstalled = remember {
        try { packageManager.getPackageInfo("app.phantom.mobile", 0); true } catch (_: Exception) { false }
    }
    val solflareInstalled = remember {
        try { packageManager.getPackageInfo("com.solflare.mobile", 0); true } catch (_: Exception) { false }
    }

    // Wallet categories with availability flags
    val walletCategories = listOf(
        WalletCategory(
            id = "phantom",
            name = "Phantom",
            description = if (phantomInstalled) "Mobile Wallet Adapter" else "Install from Play Store",
            iconRes = R.drawable.phantom,
            gradient = listOf(Color(0xFF9945FF), Color(0xFF14F195))
        ),
        WalletCategory(
            id = "solflare",
            name = "Solflare",
            description = if (solflareInstalled) "Mobile Wallet Adapter" else "Install from Play Store",
            iconRes = R.drawable.solflare,
            gradient = listOf(Color(0xFFFFC107), Color(0xFFFF6B00))
        ),
        WalletCategory(
            id = "solana",
            name = "Seed Vault (Hardware)",
            description = "Hardware wallet on Seeker",
            iconRes = R.drawable.skr,
            gradient = listOf(Color(0xFF00D4FF), Color(0xFF00FFA3))
        )
    )
    
    // Handle wallet category selection
    val handleWalletCategorySelect: (String) -> Unit = { categoryId ->
        android.util.Log.d("WalletConnectionScreen", "handleWalletCategorySelect called with categoryId: '$categoryId'")
        selectedWallet = categoryId
        
        // All wallet types go through pairing screen - let pairing screen handle the specific logic
        android.util.Log.d("WalletConnectionScreen", "About to call onNavigateToPairing with categoryId: '$categoryId'")
        onNavigateToPairing(categoryId)
    }

    // Handle creating wallet with Solflare
    val handleCreateWalletWithSolflare: () -> Unit = {
        try {
            // Check if Solflare app is installed
            val packageManager = context.packageManager
            val solflarePackageName = "com.solflare.mobile"
            
            try {
                // Try to get Solflare app info
                packageManager.getPackageInfo(solflarePackageName, 0)
                
                // Solflare is installed - open Solflare app directly for wallet creation
                val solflareIntent = packageManager.getLaunchIntentForPackage(solflarePackageName)
                if (solflareIntent != null) {
                    solflareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(solflareIntent)
                } else {
                    // If we can't launch it, fall back to pairing screen
                    onNavigateToPairing("solflare")
                }
                
            } catch (e: Exception) {
                // Solflare is not installed - open Play Store
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("market://details?id=$solflarePackageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                // Fallback to web Play Store if app store not available
                if (playStoreIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(playStoreIntent)
                } else {
                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=$solflarePackageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                }
            }
        } catch (e: Exception) {
            // If there's any unexpected error, open Play Store as fallback
            android.util.Log.e("WalletConnectionScreen", "Error handling create wallet", e)
            try {
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.solflare.mobile")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(playStoreIntent)
            } catch (ex: Exception) {
                // Last resort - show error or do nothing
                android.util.Log.e("WalletConnectionScreen", "Could not open Play Store", ex)
            }
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
    ) {
        when (currentState) {
            ConnectionState.Selection -> WalletSelectionContent(
                walletCategories = walletCategories,
                onWalletCategorySelect = handleWalletCategorySelect,
                onCreateWalletWithSolflare = handleCreateWalletWithSolflare,
                phantomInstalled = phantomInstalled,
                solflareInstalled = solflareInstalled,
                context = context,
                lastChoiceId = lastChoiceId
            )
            ConnectionState.Success -> WalletSuccessContent()
        }
    }
}

@Composable
private fun WalletSelectionContent(
    walletCategories: List<WalletCategory>,
    onWalletCategorySelect: (String) -> Unit,
    onCreateWalletWithSolflare: () -> Unit,
    phantomInstalled: Boolean,
    solflareInstalled: Boolean,
    context: android.content.Context,
    lastChoiceId: String?
) {
    // Bounce animation for Tappy
    val infiniteTransition = rememberInfiniteTransition(label = "TappyBounce")
    val bounceProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val currentHeight = bounceProgress
    val bounceHeight = 16.dp // Reduced bounce to prevent cutoff
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(WindowInsets.navigationBars.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Bouncing Tappy Mascot
        Box(
            modifier = Modifier
                .height(120.dp + bounceHeight + 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "TrueTap Logo",
                modifier = Modifier
                    .size(90.dp)
                    .offset(y = -(currentHeight * bounceHeight.value).dp)
                    .graphicsLayer {
                        scaleX = 1f - (1f - currentHeight) * 0.01f
                        scaleY = 1f + (1f - currentHeight) * 0.005f
                    },
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header Text
        Text(
            text = "Choose Your Wallet",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TrueTapPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Connect to start using TrueTap",
            fontSize = 18.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Wallet Categories
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(walletCategories) { category ->
                val isAvailable = when (category.id) {
                    "phantom" -> phantomInstalled
                    "solflare" -> solflareInstalled
                    else -> true
                }
                WalletCategoryCard(
                    category = category,
                    onClick = {
                        if (isAvailable) onWalletCategorySelect(category.id) else {
                            // Open Play Store for install
                            val pkg = if (category.id == "phantom") "app.phantom.mobile" else "com.solflare.mobile"
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$pkg"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent) else {
                                val web = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
                                web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(web)
                            }
                        }
                    },
                    enabled = isAvailable || category.id == "solana",
                    ctaText = if (!isAvailable && category.id != "solana") "Install" else null,
                    badgeText = when {
                        category.id == "solana" -> "hardware"
                        lastChoiceId != null && category.id == lastChoiceId -> "last used"
                        else -> null
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = "New to Solana?",
                fontSize = 16.sp,
                color = TrueTapTextInactive,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Create Wallet Button
            TextButton(
                onClick = onCreateWalletWithSolflare,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(TrueTapPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))  // Increased from 8dp
                
                Text(
                    text = "Create a new Solflare wallet",
                    fontSize = 18.sp,  // Increased from 16sp
                    fontWeight = FontWeight.Bold,  // Changed from SemiBold
                    color = TrueTapPrimary
                )
            }
        }
    }
}

@Composable
private fun WalletCategoryCard(
    category: WalletCategory,
    onClick: () -> Unit,
    enabled: Boolean = true,
    ctaText: String? = null,
    badgeText: String? = null
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .semantics { 
                contentDescription = "Connect with ${category.name}. ${category.description}"
                role = androidx.compose.ui.semantics.Role.Button
            },
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color.White else Color.LightGray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon - Full image filling the circle
            Image(
                painter = painterResource(id = category.iconRes),
                contentDescription = "${category.name} wallet icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Category Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) TrueTapTextPrimary else TrueTapTextInactive
                )
                
                Text(
                    text = category.description,
                    fontSize = 14.sp,
                    color = if (enabled) TrueTapTextSecondary else TrueTapTextInactive,
                    lineHeight = 20.sp
                )
            }
            
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TrueTapPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText.uppercase(),
                        fontSize = 12.sp,
                        color = TrueTapPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (ctaText != null) {
                Text(
                    text = ctaText,
                    fontSize = 14.sp,
                    color = TrueTapPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = TrueTapTextInactive
                )
            }
        }
    }
}



@Composable
private fun WalletSuccessContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(TrueTapSuccess, TrueTapSuccess.copy(alpha = 0.8f))
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✅",
                fontSize = 48.sp,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Wallet Connected!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Ready to make payments",
            fontSize = 18.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}