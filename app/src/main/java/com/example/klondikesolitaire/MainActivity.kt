package com.example.klondikesolitaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.klondikesolitaire.ads.AdIds
import com.example.klondikesolitaire.ads.AdsController
import com.example.klondikesolitaire.ui.App
import com.example.klondikesolitaire.ui.AppRoot
import com.example.klondikesolitaire.ui.theme.KlondikeSolitaireTheme
import com.example.klondikesolitaire.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

//    private lateinit var adsController: AdsController
//    private val gameViewModel: GameViewModel by lazy { GameViewModel(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

//        adsController = AdsController(
//            activity = this,
//            interstitialAdUnitId = AdIds.TEST_INTERSTITIAL,
//            rewardedAdUnitId = AdIds.TEST_REWARDED
//        )
//        adsController.loadInterstitial()
//        adsController.loadRewarded()

        // Collect one-off events coming from the ViewModel (show ads, snackbars, etc.)
//        lifecycleScope.launch {
//            gameViewModel.uiEvents.collect { event ->
//                when (event) {
//                    GameViewModel.UiEvent.ShowInterstitial -> {
//                        adsController.showInterstitial(
//                            onClosed = { adsController.loadInterstitial() }
//                        )
//                    }
//                    GameViewModel.UiEvent.ShowRewarded -> {
//                        adsController.showRewarded(
//                            onReward = { gameViewModel.onRewardEarned(coinReward = 25) },
//                            onClosed = { adsController.loadRewarded() }
//                        )
//                    }
//                }
//            }
//        }

        setContent {
            KlondikeSolitaireTheme {
                App()
            }
        }

    }
}