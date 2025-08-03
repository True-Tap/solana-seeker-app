package com.truetap.solana.seeker.di

import android.content.Context
import com.truetap.solana.seeker.repositories.WalletRepository
import com.truetap.solana.seeker.services.SeedVaultService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSeedVaultService(
        @ApplicationContext context: Context
    ): SeedVaultService {
        return SeedVaultService(context)
    }

    @Provides
    @Singleton
    fun provideWalletRepository(
        @ApplicationContext context: Context,
        seedVaultService: SeedVaultService
    ): WalletRepository {
        return WalletRepository(context, seedVaultService)
    }
} 