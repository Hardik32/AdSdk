package com.ads.sdk.helper.banner;


import com.ads.sdk.helper.IAdsConfig;
import com.ads.sdk.helper.banner.param.BannerSize;

import kotlin.jvm.internal.Intrinsics;


public class BannerAdConfig implements IAdsConfig {
    private long autoReloadTime;
    private final boolean canReloadAds;
    private final boolean canShowAds;
    private String collapsibleGravity = "bottom";
    private boolean enableAutoReload;
    private final String idAds;
    private boolean remoteAdWhenStop;
    private BannerSize size;
    private long timeDebounceResume;

    public BannerAdConfig(String idAds, boolean canShowAds, boolean canReloadAds) {
        this.idAds = idAds;
        this.canShowAds = canShowAds;
        this.canReloadAds = canReloadAds;
        this.autoReloadTime = 15000;
        this.timeDebounceResume = 500L;
        this.size = BannerSize.ADAPTIVE;
    }

    public final long getAutoReloadTime() {
        return this.autoReloadTime;
    }

    @Override
    public boolean getCanReloadAds() {
        return this.canReloadAds;
    }

    @Override
    public boolean getCanShowAds() {
        return this.canShowAds;
    }

    public final String getCollapsibleGravity() {
        return this.collapsibleGravity;
    }

    public final boolean getEnableAutoReload() {
        return this.enableAutoReload;
    }

    @Override
    public String getIdAds() {
        return this.idAds;
    }

    public final boolean getRemoteAdWhenStop() {
        return this.remoteAdWhenStop;
    }

    public final BannerSize getSize() {
        return this.size;
    }

    public final long getTimeDebounceResume() {
        return this.timeDebounceResume;
    }

    public final void setAutoReloadTime(long j) {
        if (j >= 1000) {
            this.autoReloadTime = j;
            return;
        }
        throw new IllegalArgumentException("Time can not < 1000ms");
    }

    public final void setCollapsibleGravity(String str) {
        this.collapsibleGravity = str;
    }

    public final void setEnableAutoReload(boolean z) {
        this.enableAutoReload = z;
    }

    public final void setRemoteAdWhenStop(boolean z) {
        this.remoteAdWhenStop = z;
    }

    public final void setSize(BannerSize bannerSize) {
        Intrinsics.checkNotNullParameter(bannerSize, "<set-?>");
        this.size = bannerSize;
    }

    public final void setTimeDebounceResume(long j) {
        this.timeDebounceResume = j;
    }
}