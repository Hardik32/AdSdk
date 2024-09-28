package com.ads.sdk.helper.banner.param

import com.google.android.gms.ads.AdView

//import com.facebook.ads.AdView

sealed class BannerAdParam : IAdsParam {
    data class Clickable(val minimumTimeKeepAdsDisplay: Long) : BannerAdParam()

    data class Ready(val bannerAds: AdView) : BannerAdParam()

    object Reload : BannerAdParam() {
        @JvmStatic
        fun create(): Reload = Reload
    }

    object Request : BannerAdParam() {
        @JvmStatic
        fun create(): Request = Request
    }
}
