package com.example.klondikesolitaire

import android.app.Application
import com.google.android.gms.ads.MobileAds

class SolitaireApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}