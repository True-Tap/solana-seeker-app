package com.truetap.solana.seeker.seedvault

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.solanamobile.seedvault.WalletContractV1
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates if the device is a genuine Solana Seeker with working Seed Vault
 */
@Singleton
class SeekerDeviceValidator @Inject constructor() {
    
    companion object {
        private const val TAG = "SeekerDeviceValidator"
        
        // Official Solana Seeker device identifiers
        private val SEEKER_DEVICE_MODELS = setOf(
            "Seeker", // Official Solana Seeker
            "Solana Seeker", // Alternative naming
            "OSOM OV1" // Hardware manufacturer model
        )
        
        private val SEEKER_MANUFACTURERS = setOf(
            "Solana Mobile",
            "OSOM",
            "Solana Labs"
        )
        
        // Seed Vault package identifiers
        private const val SEED_VAULT_PACKAGE = "com.solanamobile.seedvault"
        private const val SEED_VAULT_SERVICE = "com.solanamobile.seedvault.WalletService"
        
        // Required Android API level for SMS features
        private const val MIN_SMS_API_LEVEL = 31 // Android 12
    }
    
    data class DeviceValidationResult(
        val isGenuineSeeker: Boolean,
        val hasSeedVault: Boolean,
        val seedVaultVersion: String?,
        val deviceInfo: DeviceInfo,
        val validationErrors: List<String>
    )
    
    data class DeviceInfo(
        val manufacturer: String,
        val model: String,
        val device: String,
        val androidVersion: String,
        val apiLevel: Int,
        val buildId: String
    )
    
    /**
     * Comprehensive validation of Solana Seeker device and Seed Vault capability
     */
    fun validateDevice(context: Context): DeviceValidationResult {
        Log.d(TAG, "Starting Solana Seeker device validation...")
        
        val deviceInfo = getDeviceInfo()
        val errors = mutableListOf<String>()
        
        // 1. Check hardware identifiers
        val isGenuineSeeker = validateHardwareIdentifiers(deviceInfo, errors)
        
        // 2. Check Seed Vault package installation
        val seedVaultInfo = validateSeedVaultPackage(context, errors)
        
        // 3. Check Seed Vault service availability
        val hasWorkingService = validateSeedVaultService(context, errors)
        
        // 4. Check SMS/Android version compatibility
        validateSMSCompatibility(deviceInfo, errors)
        
        // 5. Check Intent resolution
        val hasIntentSupport = validateIntentSupport(context, errors)
        
        val result = DeviceValidationResult(
            isGenuineSeeker = isGenuineSeeker,
            hasSeedVault = seedVaultInfo != null && hasWorkingService && hasIntentSupport,
            seedVaultVersion = seedVaultInfo,
            deviceInfo = deviceInfo,
            validationErrors = errors.toList()
        )
        
        logValidationResult(result)
        return result
    }
    
    /**
     * Quick check if device appears to be a Solana Seeker
     */
    fun isLikelySeeker(): Boolean {
        val deviceInfo = getDeviceInfo()
        return SEEKER_DEVICE_MODELS.any { model ->
            deviceInfo.model.contains(model, ignoreCase = true)
        } || SEEKER_MANUFACTURERS.any { manufacturer ->
            deviceInfo.manufacturer.contains(manufacturer, ignoreCase = true)
        }
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            buildId = Build.ID
        )
    }
    
    private fun validateHardwareIdentifiers(deviceInfo: DeviceInfo, errors: MutableList<String>): Boolean {
        Log.d(TAG, "Validating hardware: ${deviceInfo.manufacturer} ${deviceInfo.model}")
        
        val isKnownSeeker = SEEKER_DEVICE_MODELS.any { model ->
            deviceInfo.model.contains(model, ignoreCase = true)
        } || SEEKER_MANUFACTURERS.any { manufacturer ->
            deviceInfo.manufacturer.contains(manufacturer, ignoreCase = true)
        }
        
        if (!isKnownSeeker) {
            errors.add("Device '${deviceInfo.manufacturer} ${deviceInfo.model}' is not a recognized Solana Seeker")
            Log.w(TAG, "Unknown device hardware identifiers")
        }
        
        return isKnownSeeker
    }
    
    private fun validateSeedVaultPackage(context: Context, errors: MutableList<String>): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(SEED_VAULT_PACKAGE, 0)
            val versionName = packageInfo.versionName
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            
            Log.d(TAG, "Seed Vault found: v$versionName ($versionCode)")
            versionName
        } catch (e: PackageManager.NameNotFoundException) {
            val error = "Seed Vault package '$SEED_VAULT_PACKAGE' not installed"
            errors.add(error)
            Log.e(TAG, error, e)
            null
        }
    }
    
    private fun validateSeedVaultService(context: Context, errors: MutableList<String>): Boolean {
        return try {
            val serviceIntent = Intent().apply {
                setClassName(SEED_VAULT_PACKAGE, SEED_VAULT_SERVICE)
            }
            
            val resolveInfo = context.packageManager.resolveService(serviceIntent, 0)
            val isAvailable = resolveInfo != null
            
            if (!isAvailable) {
                errors.add("Seed Vault service '$SEED_VAULT_SERVICE' not available")
                Log.e(TAG, "Seed Vault service not found")
            } else {
                Log.d(TAG, "Seed Vault service is available")
            }
            
            isAvailable
        } catch (e: Exception) {
            val error = "Failed to check Seed Vault service: ${e.message}"
            errors.add(error)
            Log.e(TAG, error, e)
            false
        }
    }
    
    private fun validateSMSCompatibility(deviceInfo: DeviceInfo, errors: MutableList<String>) {
        if (deviceInfo.apiLevel < MIN_SMS_API_LEVEL) {
            val error = "Android API level ${deviceInfo.apiLevel} < $MIN_SMS_API_LEVEL. SMS features require Android 12+"
            errors.add(error)
            Log.w(TAG, error)
        } else {
            Log.d(TAG, "SMS compatibility: Android ${deviceInfo.androidVersion} (API ${deviceInfo.apiLevel}) ✓")
        }
    }
    
    private fun validateIntentSupport(context: Context, errors: MutableList<String>): Boolean {
        val testIntents = listOf(
            WalletContractV1.ACTION_AUTHORIZE_SEED_ACCESS,
            WalletContractV1.ACTION_GET_PUBLIC_KEY,
            WalletContractV1.ACTION_SIGN_TRANSACTION,
            WalletContractV1.ACTION_SIGN_MESSAGE
        )
        
        var supportedCount = 0
        
        testIntents.forEach { action ->
            try {
                val intent = Intent(action)
                val resolveInfo = context.packageManager.resolveActivity(intent, 0)
                
                if (resolveInfo != null) {
                    supportedCount++
                    Log.d(TAG, "Intent support: $action ✓")
                } else {
                    Log.w(TAG, "Intent support: $action ✗")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check intent: $action", e)
            }
        }
        
        val hasFullSupport = supportedCount == testIntents.size
        
        if (!hasFullSupport) {
            errors.add("Seed Vault Intent support incomplete: $supportedCount/${testIntents.size} actions available")
        }
        
        return hasFullSupport
    }
    
    private fun logValidationResult(result: DeviceValidationResult) {
        Log.i(TAG, """
            |=== SOLANA SEEKER VALIDATION RESULTS ===
            |Device: ${result.deviceInfo.manufacturer} ${result.deviceInfo.model}
            |Android: ${result.deviceInfo.androidVersion} (API ${result.deviceInfo.apiLevel})
            |Build: ${result.deviceInfo.buildId}
            |Genuine Seeker: ${result.isGenuineSeeker}
            |Seed Vault Available: ${result.hasSeedVault}
            |Seed Vault Version: ${result.seedVaultVersion ?: "N/A"}
            |Validation Errors: ${result.validationErrors.size}
            |${result.validationErrors.joinToString("\n") { "  - $it" }}
            |========================================
        """.trimMargin())
    }
    
    /**
     * Get human-readable validation summary
     */
    fun getValidationSummary(result: DeviceValidationResult): String {
        return when {
            result.isGenuineSeeker && result.hasSeedVault -> 
                "✅ Genuine Solana Seeker with working Seed Vault v${result.seedVaultVersion}"
            
            result.isGenuineSeeker && !result.hasSeedVault -> 
                "⚠️ Genuine Solana Seeker but Seed Vault unavailable"
            
            !result.isGenuineSeeker && result.hasSeedVault -> 
                "⚠️ Seed Vault detected on non-Seeker device (${result.deviceInfo.manufacturer} ${result.deviceInfo.model})"
            
            else -> 
                "❌ Not a Solana Seeker device (${result.deviceInfo.manufacturer} ${result.deviceInfo.model})"
        }
    }
}