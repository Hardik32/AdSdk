package com.ads.sdk.helper.banner

import android.view.ViewGroup
import android.widget.Toast
import com.ads.sdk.funtion.AdCallback
import com.ads.sdk.helper.banner.param.AdBannerState
import com.google.android.gms.ads.LoadAdError
import com.ads.sdk.helper.BannerAdHelper
import kotlinx.coroutines.*

class DefaultCallback(private val bannerAdHelper: BannerAdHelper) : AdCallback() {

    override fun onAdFailedToLoad(i: LoadAdError?) {
        super.onAdFailedToLoad(i)

        val isShowMessageTester = true
        if (isShowMessageTester) {
            bannerAdHelper.activity.runOnUiThread {
                Toast.makeText(
                    bannerAdHelper.activity,
                    "Load banner fail : ${bannerAdHelper.config.idAds}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (bannerAdHelper.isActiveState()) {
            GlobalScope.launch(Dispatchers.Main) {
                val adBannerState = bannerAdHelper._adBannerState
                adBannerState.emit(AdBannerState.Fail)
            }
            bannerAdHelper.logZ("onAdFailedToLoad()")
        } else {
            bannerAdHelper.logInterruptExecute("onAdFailedToLoad")
        }

        bannerAdHelper.requestAutoReloadAd()
    }

    override fun onAdImpression() {
        super.onAdImpression()

        val isShowMessageTester = true
        if (isShowMessageTester) {
            bannerAdHelper.activity.runOnUiThread {
                Toast.makeText(
                    bannerAdHelper.activity,
                    "Show banner : ${bannerAdHelper.config.idAds}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        bannerAdHelper.timeShowAdImpression = System.currentTimeMillis()
        bannerAdHelper.logZ("timeShowAdImpression: ${bannerAdHelper.timeShowAdImpression}")
        bannerAdHelper.requestAutoReloadAd()
    }

    override fun onBannerLoaded(viewGroup: ViewGroup?) {
        super.onBannerLoaded(viewGroup)

        if (bannerAdHelper.isActiveState()) {
            GlobalScope.launch(Dispatchers.Main) {
                bannerAdHelper.setBannerAdView(viewGroup)

                if (viewGroup != null) {
                    val adBannerState = bannerAdHelper._adBannerState
                    adBannerState.emit(AdBannerState.Loaded(viewGroup))
                }
            }
            bannerAdHelper.logZ("onBannerLoaded()")
        } else {
            bannerAdHelper.logInterruptExecute("onBannerLoaded")
        }
    }
}
