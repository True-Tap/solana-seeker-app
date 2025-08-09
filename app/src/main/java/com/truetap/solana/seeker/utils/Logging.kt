package com.truetap.solana.seeker.utils

object Logx {
    private val isDebug = com.truetap.solana.seeker.BuildConfig.DEBUG
    fun d(tag: String, msg: () -> String) { if (isDebug) android.util.Log.d(tag, msg()) }
    fun i(tag: String, msg: () -> String) { if (isDebug) android.util.Log.i(tag, msg()) }
    fun w(tag: String, msg: () -> String) { android.util.Log.w(tag, msg()) }
    fun e(tag: String, msg: () -> String, t: Throwable? = null) { android.util.Log.e(tag, msg(), t) }
    fun redact(s: String?, max: Int = 12): String = s?.let { if (it.length <= max) it else it.take(6) + "..." + it.takeLast(4) } ?: ""
}


