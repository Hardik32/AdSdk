package com.ads.sdk.helper.banner.param

import android.view.ViewGroup

sealed class AdBannerState {
    object Cancel : AdBannerState()
    object Fail : AdBannerState()
    data class Loaded(val adBanner: ViewGroup) : AdBannerState() {
        fun updateBanner(newAdBanner: ViewGroup): Loaded {
            return Loaded(newAdBanner)
        }
    }
    object Loading : AdBannerState()
    object None : AdBannerState()

}
