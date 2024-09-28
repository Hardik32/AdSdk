package com.sdk.module;

import com.ads.sdk.admob.Admob;
import com.ads.sdk.admob.AppOpenManager;
import com.ads.sdk.ads.SdkAd;
import com.ads.sdk.ads.wrapper.ApNativeAd;
import com.ads.sdk.application.AdsMultiDexApplication;
import com.ads.sdk.config.AdjustConfig;
import com.ads.sdk.config.SdkAdConfig;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;

public class SDKApp extends AdsMultiDexApplication {

    public static ApNativeAd mApNativeAd;

    public MutableLiveData<ApNativeAd> nativeAdsLanguage = new MutableLiveData<>();
    public MutableLiveData<ApNativeAd> nativeAdsLanguage2 = new MutableLiveData<>();

    public String intterialID = "ca-app-pub-3940256099942544/1033173712";
    public String bannerId = "ca-app-pub-3940256099942544/9214589741";
    public String nativeId = "ca-app-pub-3940256099942544/2247696110";
    public String bannerCollapsingId = "ca-app-pub-3940256099942544/2014213617";
    public String rewardAdID = "ca-app-pub-3940256099942544/5224354917";
    public String appopenResumeId = "ca-app-pub-3940256099942544/9257395921";

    private static SDKApp context;
    public MutableLiveData<Boolean> isAdCloseSplash = new MutableLiveData<>();

    public static SDKApp getApplication() {
        return context;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initAds();
        initBilling();
    }

    private void initAds() {
//
        mSdkAdConfig = new SdkAdConfig(this, SdkAdConfig.ENVIRONMENT_DEVELOP);


        AdjustConfig adjustConfig = new AdjustConfig(true, getString(R.string.adjust_token));
        mSdkAdConfig.setAdjustConfig(adjustConfig);
//        mSdkAdConfig.setListDeviceTest(list);
        mSdkAdConfig.setFacebookClientToken(getString(R.string.facebook_client_token));
        mSdkAdConfig.setAdjustTokenTiktok(getString(R.string.tiktok_token));
        mSdkAdConfig.setIdAdResume(appopenResumeId);

        SdkAd.getInstance().init(this, mSdkAdConfig);
        Admob.getInstance().setDisableAdResumeWhenClickAds(false);
        Admob.getInstance().setOpenActivityAfterShowInterAds(false);
//        AppPurchase.getInstance().setPurchase(true);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);

    }

    private void initBilling() {
        List<String> listIAP = new ArrayList<>();
        listIAP.add("android.test.purchased");
        List<String> listSub = new ArrayList<>();
//        AppPurchase.getInstance().initBilling(this, listIAP, listSub);
    }
}
