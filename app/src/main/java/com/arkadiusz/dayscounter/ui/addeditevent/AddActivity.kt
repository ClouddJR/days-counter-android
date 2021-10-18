package com.arkadiusz.dayscounter.ui.addeditevent

import android.os.Bundle
import com.arkadiusz.dayscounter.util.PreferenceUtils.defaultPrefs
import com.arkadiusz.dayscounter.util.PreferenceUtils.get
import com.arkadiusz.dayscounter.util.PreferenceUtils.set
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AddActivity : BaseAddEditActivity() {

    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init(intent.getStringExtra("Event Type")!!)
        setUpAd()
    }

    override fun handleSaveClick() {
        showAd()
        val event = viewModel.saveEvent()
        addReminder(event)
        finish()
    }

    private fun setUpAd() {
        val prefs = defaultPrefs(this)
        val areAdsRemoved: Boolean = prefs["ads", false] ?: false
        val wasShown: Boolean = prefs["wasAdShown", true] ?: true

        val adUnitId = "ca-app-pub-4098342918729972/3144606816"
        if (!areAdsRemoved && !wasShown) {
            InterstitialAd.load(
                this,
                adUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }
                }
            )
        }
    }

    private fun showAd() {
        interstitialAd?.show(this)
        defaultPrefs(this)["wasAdShown"] = interstitialAd != null
    }
}