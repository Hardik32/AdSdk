package com.sdk.module;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ads.sdk.admob.AdsConsentManager;
import com.ads.sdk.admob.AppOpenManager;
import com.ads.sdk.ads.SdkAd;
import com.ads.sdk.ads.wrapper.ApNativeAd;
import com.ads.sdk.funtion.AdCallback;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    AdsConsentManager adsConsentManager;

    boolean isEnableUMP = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();

        if (isEnableUMP) {

//            adsConsentManager = new AdsConsentManager(this);
//
//            adsConsentManager.requestUMP(
//
//                    true,
//
//                    deviceId,
//
//                    false,
//
//                    canRequestAds -> runOnUiThread(this::loadSplash)
//
//            );

//            adsConsentManager.requestUMP(
//                    canRequestAds -> runOnUiThread(this::loadSplash));

            loadSplash();

        } else {

            SdkAd.getInstance().initAdsNetwork();

            loadSplash();

        }


    }


    @Override
    protected void onStart() {
        super.onStart();


    }

    public String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    private void loadSplash() {

//        AppPurchase.getInstance().setPurchase(true);

        preLoadNativeAd();

        SdkAd.getInstance().loadSplashInterstitialAds(SplashActivity.this, BuildConfig.ad_interstitial_splash, 25000, 5000, new AdCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                Log.e("DDDDDDDDDDDDD", "onNextAction");
                startActivity(new Intent(SplashActivity.this, MbitActivity.class));
                finish();
            }
        });

    }


    private void navigateToNextScreen() {

        if (isDestroyed() || isFinishing()) {
            return;
        }
        startActivity(new Intent(SplashActivity.this, LangugeActivity.class));
        finish();

    }

    public void preLoadNativeAd() {

        SdkAd.getInstance().loadNativeAdResultCallback(this, SDKApp.getApplication().nativeId, R.layout.native_large, new AdCallback() {
            @Override
            public void onNativeAdLoaded(ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);

                SDKApp.getApplication().nativeAdsLanguage.setValue(nativeAd);
            }

            @Override
            public void onAdFailedToLoad(@org.jetbrains.annotations.Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);

                SDKApp.getApplication().nativeAdsLanguage = null;
            }

            @Override
            public void onAdFailedToShow(@org.jetbrains.annotations.Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                SDKApp.getApplication().nativeAdsLanguage = null;
            }
        });


        SdkAd.getInstance().loadNativeAdResultCallback(this, SDKApp.getApplication().nativeId, R.layout.native_large, new AdCallback() {
            @Override
            public void onNativeAdLoaded(ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);

                SDKApp.getApplication().nativeAdsLanguage2.setValue(nativeAd);
            }

            @Override
            public void onAdFailedToLoad(@org.jetbrains.annotations.Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);

                SDKApp.getApplication().nativeAdsLanguage2 = null;
            }

            @Override
            public void onAdFailedToShow(@org.jetbrains.annotations.Nullable AdError adError) {
                super.onAdFailedToShow(adError);

                SDKApp.getApplication().nativeAdsLanguage2 = null;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        AppOpenManager.getInstance().disableAppResume();
    }
}
