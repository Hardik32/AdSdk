//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.ads.sdk.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ads.sdk.ads.SdkAd;
import com.ads.sdk.event.FirebaseAnalyticsUtil;
import com.ads.sdk.funtion.UMPResultListener;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;

public class AdsConsentManager {
    private static String TAG = "AdsConsentManager";
    private ConsentInformation consentInformation;
    private Activity activity;
    private AtomicBoolean atomicBoolean;
    FirebaseAnalyticsUtil mFirebaseAnalytics;

    public AdsConsentManager(Activity activity) {
        this.atomicBoolean = new AtomicBoolean(false);
        this.activity = activity;
    }

    public static boolean getConsentResult(Context context) {

        String strr= context.getSharedPreferences(context.getPackageName() + "_preferences", 0).getString("IABTCF_PurposeConsents", "");
        Log.d(TAG, "consentResult: " + strr);
        return !strr.isEmpty() ? String.valueOf(strr.charAt(0)).equals("1") : true;
    }

    public void requestUMP(UMPResultListener umpResultListener) {
        Boolean var10001 = Boolean.FALSE;
        this.requestUMP(var10001, "", var10001, umpResultListener);
    }

    public void requestUMP(Boolean enableDebug, String testDevice, Boolean resetData, UMPResultListener umpResultListener) {

        Boolean var10000 = enableDebug;
        ConsentRequestParameters.Builder enableDebug1;
        enableDebug1 = new ConsentRequestParameters.Builder();
        if (var10000) {
            enableDebug1.setConsentDebugSettings((new ConsentDebugSettings.Builder(this.activity)).setDebugGeography(1).addTestDeviceHashedId(testDevice).build());
        }

        ConsentRequestParameters enableDebug2 = enableDebug1.setTagForUnderAgeOfConsent(false).build();
        this.consentInformation = UserMessagingPlatform.getConsentInformation(this.activity);
        if (resetData) {
            this.consentInformation.reset();
        }

        ConsentInformation var10001 = this.consentInformation;
        Activity testDevice1 = this.activity;
        ConsentInformation.OnConsentInfoUpdateSuccessListener resetData1 = () -> {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this.activity, (loadAndShowError) -> {
                if (loadAndShowError != null) {

                    Log.w(TAG, loadAndShowError.getMessage());

                    Bundle var6 = new Bundle();
                    var6.putInt("error_code", loadAndShowError.getErrorCode());
                    var6.putString("error_msg", loadAndShowError.getMessage());
                    mFirebaseAnalytics.logEventTracking(this.activity, "ump_consent_failed", var6);
                }


                Bundle var7 = new Bundle();

                var7.putBoolean("consent", getConsentResult(this.activity));
                mFirebaseAnalytics.logEventTracking(this.activity, "ump_consent_result", var7);
                if (!this.atomicBoolean.getAndSet(true)) {
                    umpResultListener.onCheckUMPSuccess(getConsentResult(this.activity));
                    SdkAd.getInstance().initAdsNetwork();
                }

            });
        };
        ConsentInformation.OnConsentInfoUpdateFailureListener var5 = (requestConsentError) -> {

            Log.w(TAG, requestConsentError.getMessage());
            Bundle var4 = new Bundle();
            var4.putInt("error_code", requestConsentError.getErrorCode());
            var4.putString("error_msg", requestConsentError.getMessage());
            FirebaseAnalyticsUtil.logEventTracking(this.activity, "ump_request_failed", var4);
            if (!this.atomicBoolean.getAndSet(true)) {
                umpResultListener.onCheckUMPSuccess(getConsentResult(this.activity));
                SdkAd.getInstance().initAdsNetwork();
            }

        };
        var10001.requestConsentInfoUpdate(testDevice1, enableDebug2, resetData1, var5);
        if (this.consentInformation.canRequestAds() && !this.atomicBoolean.getAndSet(true)) {
            umpResultListener.onCheckUMPSuccess(getConsentResult(this.activity));
            SdkAd.getInstance().initAdsNetwork();
        }

    }

    public void showPrivacyOption(Activity activity, UMPResultListener umpResultListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, (formError) -> {
            if (formError != null) {
                Log.w(TAG, formError.getMessage());
                Bundle formError1;
                formError1 = new Bundle();
                FirebaseAnalyticsUtil.logEventTracking(activity, "ump_consent_failed", formError1);
            }

            if (getConsentResult(activity)) {
                SdkAd.getInstance().initAdsNetwork();
            }

            UMPResultListener var10000 = umpResultListener;

            Bundle umpResultListener1 = new Bundle();
            umpResultListener1.putBoolean("consent", getConsentResult(activity));
            FirebaseAnalyticsUtil.logEventTracking(activity, "ump_consent_result", umpResultListener1);
            var10000.onCheckUMPSuccess(getConsentResult(activity));
        });
    }
}
