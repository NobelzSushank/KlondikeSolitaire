package com.example.klondikesolitaire.ads

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * A tiny wrapper around AdMob objects to keep Activity/UI code clean.
 *
 * Note: This uses test IDs by default (see AdIds).
 */
class AdsController(
    private val activity: Activity,
    private val interstitialAdUnitId: String,
    private val rewardedAdUnitId: String,
) {
    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null

    fun loadInterstitial() {
        InterstitialAd.load(
            activity,
            interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            }
        )
    }

    fun showInterstitial(onClosed: () -> Unit) {
        val ad = interstitial ?: return onClosed()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                onClosed()
            }
        }
        ad.show(activity)
    }

    fun loadRewarded() {
        RewardedAd.load(
            activity,
            rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                }
            }
        )
    }

    fun showRewarded(
        onReward: (RewardItem) -> Unit,
        onClosed: () -> Unit
    ) {
        val ad = rewarded ?: return onClosed()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                onClosed()
            }
        }
        ad.show(activity) { rewardItem -> onReward(rewardItem) }
    }
}