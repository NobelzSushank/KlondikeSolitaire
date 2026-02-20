package com.example.klondikesolitaire.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Initializes AdMob safely.
 *
 * - Will never crash the app if Play Services / Ads SDK has issues.
 * - Only initializes once.
 */
object AdsInitializer {
    private val initialized = AtomicBoolean(false)

    fun initializeSafely(context: Context) {
        if (initialized.get()) return

        try {
            // MobileAds.initialize is async; callback not required for basic usage.
            MobileAds.initialize(context)
            initialized.set(true)
        } catch (_: Throwable) {
            // Ignore all errors so the app still runs.
            // (Real apps may log this to Crashlytics or similar.)
        }
    }
}