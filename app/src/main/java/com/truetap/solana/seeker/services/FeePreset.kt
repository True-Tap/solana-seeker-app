package com.truetap.solana.seeker.services

enum class FeePreset(val microLamportsPerCU: Long, val computeUnits: Int) {
    NORMAL(0L, 200_000),
    FAST(500L, 250_000),
    EXPRESS(5_000L, 300_000)
}


