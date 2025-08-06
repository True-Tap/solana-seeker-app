package com.truetap.solana.seeker.di

import android.content.Context
import com.truetap.solana.seeker.BuildConfig
import com.truetap.solana.seeker.seedvault.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifiers for different Seed Vault providers
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RealProvider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FakeProvider

/**
 * Hilt module for providing Seed Vault related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object SeedVaultModule {
    
    @Provides
    @Singleton
    fun provideSeekerDeviceValidator(): SeekerDeviceValidator {
        return SeekerDeviceValidator()
    }
    
    @Provides
    @Singleton
    fun provideFallbackWalletHandler(): FallbackWalletHandler {
        return FallbackWalletHandler()
    }
    
    @Provides
    @Singleton
    @RealProvider
    fun provideRealSeedVaultProvider(): SeedVaultProvider {
        return RealSeedVaultProvider()
    }
    
    @Provides
    @Singleton
    @FakeProvider
    fun provideFakeSeedVaultProvider(): SeedVaultProvider {
        return FakeSeedVaultProvider()
    }
    
    @Provides
    @Singleton
    fun provideSeedVaultProvider(
        @ApplicationContext context: Context,
        @RealProvider realProvider: SeedVaultProvider,
        @FakeProvider fakeProvider: SeedVaultProvider
    ): SeedVaultProvider {
        return when {
            // Use fake provider in dev builds or when explicitly configured
            BuildConfig.USE_FAKE_SEED_VAULT -> {
                android.util.Log.d("SeedVaultModule", "Using FakeSeedVaultProvider for development")
                fakeProvider
            }
            
            // For production, always try real provider first
            else -> {
                android.util.Log.d("SeedVaultModule", "Using RealSeedVaultProvider for production")
                realProvider
            }
        }
    }
    
    @Provides
    @Singleton
    fun provideSeedVaultManager(
        seedVaultProvider: SeedVaultProvider,
        deviceValidator: SeekerDeviceValidator
    ): SeedVaultManager {
        return SeedVaultManager(seedVaultProvider, deviceValidator)
    }
}