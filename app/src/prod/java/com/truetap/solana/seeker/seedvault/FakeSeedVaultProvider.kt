package com.truetap.solana.seeker.seedvault

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import javax.inject.Inject

/**
 * Production stub - FakeSeedVaultProvider is not available in production builds
 */
class FakeSeedVaultProvider @Inject constructor() : SeedVaultProvider {
    
    override fun isAvailable(activity: Activity): Boolean = false
    
    override fun getProviderInfo(): ProviderInfo {
        return ProviderInfo(
            name = "Fake Seed Vault (Disabled)",
            version = "Production",
            isFake = true,
            description = "Fake provider disabled in production builds"
        )
    }
    
    override suspend fun requestAuthorization(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<android.content.Intent>
    ): AuthResult = AuthResult.NotAvailable
    
    override suspend fun getPublicKey(
        activity: Activity,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<android.content.Intent>
    ): PublicKeyResult = PublicKeyResult.Error("Fake provider not available in production")
    
    override suspend fun signTransaction(
        activity: Activity,
        transactionBytes: ByteArray,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<android.content.Intent>
    ): SigningResult = SigningResult.Error("Fake provider not available in production")
    
    override suspend fun signMessage(
        activity: Activity,
        messageBytes: ByteArray,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<android.content.Intent>
    ): SigningResult = SigningResult.Error("Fake provider not available in production")
    
    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        // No-op in production
    }
}