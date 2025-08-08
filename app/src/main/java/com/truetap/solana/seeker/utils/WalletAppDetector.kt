package com.truetap.solana.seeker.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object WalletAppDetector {
    private val phantomPackageCandidates = listOf(
        "app.phantom.mobile",
        "app.phantom",
        "app.phantom.android"
    )

    private val solflarePackageCandidates = listOf(
        "com.solflare.mobile",
        "io.solflare.wallet"
    )

    fun isPhantomInstalled(context: Context): Boolean {
        return isAnyInstalled(context.packageManager, phantomPackageCandidates)
    }

    fun isSolflareInstalled(context: Context): Boolean {
        return isAnyInstalled(context.packageManager, solflarePackageCandidates)
    }

    fun openStoreForPhantom(context: Context) {
        openStore(context, preferredPackage = "app.phantom.mobile", fallbacks = phantomPackageCandidates)
    }

    fun openStoreForSolflare(context: Context) {
        openStore(context, preferredPackage = "com.solflare.mobile", fallbacks = solflarePackageCandidates)
    }

    private fun isAnyInstalled(pm: PackageManager, packages: List<String>): Boolean {
        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: Exception) {
                // continue
            }
        }
        return false
    }

    private fun openStore(context: Context, preferredPackage: String, fallbacks: List<String>) {
        val packageName = (listOf(preferredPackage) + fallbacks).first()
        val marketUri = Uri.parse("market://details?id=$packageName")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pm = context.packageManager
        if (marketIntent.resolveActivity(pm) != null) {
            context.startActivity(marketIntent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}


