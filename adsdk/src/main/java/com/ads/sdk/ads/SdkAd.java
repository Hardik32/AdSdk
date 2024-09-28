package com.ads.sdk.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.ads.sdk.adapternative.SdkAdAdapter;
import com.ads.sdk.adapternative.SdkAdPlacer;
import com.ads.sdk.admob.Admob;
import com.ads.sdk.admob.AppOpenManager;
import com.ads.sdk.ads.wrapper.ApInterstitialAd;
import com.ads.sdk.ads.wrapper.ApNativeAd;
import com.ads.sdk.billing.AppPurchase;
import com.ads.sdk.config.SdkAdConfig;
import com.ads.sdk.event.SdkAdjust;
import com.ads.sdk.funtion.AdCallback;
import com.ads.sdk.funtion.RewardCallback;
import com.ads.sdk.helper.banner.param.BannerSize;
import com.ads.sdk.util.AppUtil;
import com.ads.sdk.util.SharePreferenceUtils;
//import com.facebook.FacebookSdk;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.common.IntentSenderForResultStarter;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;


public class SdkAd {
    public static final String TAG_ADJUST = "SDKAdjust";
    public static final String TAG = "SdkAd";
    private static volatile SdkAd INSTANCE;
    private SdkAdConfig adConfig;
    private SdkInitCallback initCallback;
    private Boolean initAdSuccess = false;

    Application context;

    public static synchronized SdkAd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SdkAd();
        }
        return INSTANCE;
    }

    public SdkAdConfig getAdConfig() {
        return adConfig;
    }

    public void setCountClickToShowAds(int countClickToShowAds) {
        Admob.getInstance().setNumToShowAds(countClickToShowAds);
    }

    public void setCountClickToShowAds(int countClickToShowAds, int currentClicked) {
        Admob.getInstance().setNumToShowAds(countClickToShowAds, currentClicked);
    }

    public void setIncreseClickCount() {
        Admob.getInstance().currentClicked++;
    }

    public void init(Application context, SdkAdConfig adConfig) {
        if (adConfig == null) {
            throw new RuntimeException("Cant not set GamAdConfig null");
        }
        this.context = context;
        this.adConfig = adConfig;
        AppUtil.VARIANT_DEV = adConfig.isVariantDev();
        if (adConfig.isEnableAdjust()) {
            SdkAdjust.enableAdjust = true;
            setupAdjust(adConfig.isVariantDev(), adConfig.getAdjustConfig().getAdjustToken());
        }
        Admob.getInstance().init(context, adConfig.getListDeviceTest(), adConfig.getAdjustTokenTiktok());
        if (adConfig.isEnableAdResume()) {
//            AppOpenManager.getInstance().setAppResumeAdId(adConfig.getIdAdResume());
            AppOpenManager.getInstance().init(adConfig.getApplication(), adConfig.getIdAdResume());
        }

//        FacebookSdk.setClientToken(adConfig.getFacebookClientToken());
//        FacebookSdk.sdkInitialize(context);
    }

    public void initAdsNetwork() {
        Admob.getInstance().init(context, adConfig.getListDeviceTest(), adConfig.getAdjustTokenTiktok());
        if (adConfig.isEnableAdResume()) {
//            AppOpenManager.getInstance().setAppResumeAdId(adConfig.getIdAdResume());
            AppOpenManager.getInstance().init(adConfig.getApplication(), adConfig.getIdAdResume());
        }

    }

    public void setInitCallback(SdkInitCallback initCallback) {
        this.initCallback = initCallback;
        if (initAdSuccess)
            initCallback.initAdSuccess();
    }

    private void setupAdjust(Boolean buildDebug, String adjustToken) {
        String environment = buildDebug ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(adConfig.getApplication(), adjustToken, environment);

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE);
        config.setPreinstallTrackingEnabled(true);
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Log.d(TAG_ADJUST, "Attribution callback called!");
                Log.d(TAG_ADJUST, "Attribution: " + attribution.toString());
            }
        });

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                Log.d(TAG_ADJUST, "Event success callback called!");
                Log.d(TAG_ADJUST, "Event success data: " + eventSuccessResponseData.toString());
            }
        });
        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                Log.d(TAG_ADJUST, "Event failure callback called!");
                Log.d(TAG_ADJUST, "Event failure data: " + eventFailureResponseData.toString());
            }
        });

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                Log.d(TAG_ADJUST, "Session success callback called!");
                Log.d(TAG_ADJUST, "Session success data: " + sessionSuccessResponseData.toString());
            }
        });

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                Log.d(TAG_ADJUST, "Session failure callback called!");
                Log.d(TAG_ADJUST, "Session failure data: " + sessionFailureResponseData.toString());
            }
        });


        config.setSendInBackground(true);
        Adjust.onCreate(config);
        adConfig.getApplication().registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    }

    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }
    }

    public void requestLoadBanner(Activity activity, String id, String gravity, BannerSize bannerSize, AdCallback adCallback) {
//        if (this.f490a.getMediationProvider() == 0) {

        Log.e("requestLoadBanner", " =>");
        Admob.getInstance().requestLoadBanner(activity, id, gravity, adCallback, Boolean.FALSE, Admob.BANNER_INLINE_LARGE_STYLE, bannerSize);
//        } else {
//            AppLovin.getInstance().requestLoadBanner(activity, str, adCallback);
//        }
    }


    public void loadBanner(Activity mActivity, String id) {
        Admob.getInstance().loadBanner(mActivity, id);
    }

    public void loadBanner(Activity mActivity, String id, AdCallback adCallback) {
        Admob.getInstance().loadBanner(mActivity, id, adCallback);
    }

    public void loadCollapsibleBanner(Activity activity, String id, String gravity, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBanner(activity, id, gravity, adCallback);
    }

    public void loadCollapsibleBannerFragment(Activity activity, String id, View rootView, String gravity, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBannerFragment(activity, id, rootView, gravity, adCallback);
    }

    public void loadCollapsibleBannerSizeMedium(Activity activity, String id, String gravity, AdSize sizeBanner, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBannerSizeMedium(activity, id, gravity, sizeBanner, adCallback);
    }

    public void loadBannerFragment(Activity mActivity, String id, View rootView) {
        Admob.getInstance().loadBannerFragment(mActivity, id, rootView);
    }

    public void loadBannerFragment(Activity mActivity, String id, View rootView, AdCallback adCallback) {
        Admob.getInstance().loadBannerFragment(mActivity, id, rootView, adCallback);
    }

    public void loadInlineBanner(Activity mActivity, String idBanner, String inlineStyle) {
        Admob.getInstance().loadInlineBanner(mActivity, idBanner, inlineStyle);
    }

    public void loadInlineBanner(Activity mActivity, String idBanner, String inlineStyle, AdCallback adCallback) {
        Admob.getInstance().loadInlineBanner(mActivity, idBanner, inlineStyle, adCallback);
    }

    public void loadBannerInlineFragment(Activity mActivity, String idBanner, View rootView, String inlineStyle) {
        Admob.getInstance().loadInlineBannerFragment(mActivity, idBanner, rootView, inlineStyle);
    }

    public void loadBannerInlineFragment(Activity mActivity, String idBanner, View rootView, String inlineStyle, AdCallback adCallback) {
        Admob.getInstance().loadInlineBannerFragment(mActivity, idBanner, rootView, inlineStyle, adCallback);
    }

    public void loadSplashInterstitialAds(Context context, String id, long timeOut, long timeDelay, AdCallback adListener) {
        Admob.getInstance().loadSplashInterstitialAds(context, id, timeOut, timeDelay, true, adListener);
    }

    public void onCheckShowSplashWhenFail(AppCompatActivity activity, AdCallback callback, int timeDelay) {
        Admob.getInstance().onCheckShowSplashWhenFail(activity, callback, timeDelay);
    }


    public ApInterstitialAd getInterstitialAds(Context context, String id, AdCallback adListener) {
        ApInterstitialAd apInterstitialAd = new ApInterstitialAd();
        Admob.getInstance().getInterstitialAds(context, id, new AdCallback() {
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                apInterstitialAd.setInterstitialAd(interstitialAd);
                adListener.onApInterstitialLoad(apInterstitialAd);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "Admob onAdFailedToLoad");
                adListener.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                Log.d(TAG, "Admob onAdFailedToShow");
                adListener.onAdFailedToShow(adError);
            }

        });
        return apInterstitialAd;
    }

//    public ApInterstitialAd getInterstitialAds(Context context, String id, AdCallback adListener) {
//        ApInterstitialAd apInterstitialAd = new ApInterstitialAd();
//        Admob.getInstance().getInterstitialAds(context, id, new AdCallback() {
//            @Override
//            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
//                super.onInterstitialLoad(interstitialAd);
//                apInterstitialAd.setInterstitialAd(interstitialAd);
//                adListener.onApInterstitialLoad(apInterstitialAd);
//
//            }
//
//
//            @Override
//            public void onAdFailedToLoad(@Nullable LoadAdError i) {
//                super.onAdFailedToLoad(i);
//                Log.d(TAG, "Admob onAdFailedToLoad");
//                adListener.onAdFailedToLoad(i);
//                if (!Admob.getInstance().openActivityAfterShowInterAds) {
//                    adListener.onNextAction();
//                }
//            }
//
//            @Override
//            public void onAdFailedToShow(@Nullable AdError adError) {
//                super.onAdFailedToShow(adError);
//                Log.d(TAG, "Admob onAdFailedToShow");
//                adListener.onAdFailedToShow(adError);
//                if (!Admob.getInstance().openActivityAfterShowInterAds) {
//                    adListener.onNextAction();
//                }
//
//            }
//
//            @Override
//            public void onAdClosed() {
//                super.onAdClosed();
//                Log.d(TAG, "Admob onAdClosed");
//                adListener.onAdClosed();
//                apInterstitialAd.setInterstitialAd(null);
//
//            }
//        });
//
//        return apInterstitialAd;
//    }

    public void forceShowInterstitial(@NonNull Context context, ApInterstitialAd mInterstitialAd,
                                      @NonNull final AdCallback callback, boolean shouldReloadAds) {
        if (System.currentTimeMillis() - SharePreferenceUtils.getLastImpressionInterstitialTime(context)
                < SdkAd.getInstance().adConfig.getIntervalInterstitialAd() * 1000L
        ) {
            callback.onNextAction();
            return;
        }
        if (AppPurchase.getInstance().isPurchased(context)) {
            Log.e(TAG, "forceShowInterstitial ==> isPurchased ");
            callback.onNextAction();
            return;
        }

        if (mInterstitialAd == null || mInterstitialAd.isNotReady()) {
            callback.onNextAction();
            return;
        }
        AdCallback adCallback = new AdCallback() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                callback.onAdClosed();
                if (shouldReloadAds) {
                    Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                        @Override
                        public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                            super.onInterstitialLoad(interstitialAd);
                            mInterstitialAd.setInterstitialAd(interstitialAd);
                            callback.onInterstitialLoad(mInterstitialAd.getInterstitialAd());
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            mInterstitialAd.setInterstitialAd(null);
                            callback.onAdFailedToLoad(i);
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(adError);
                        }

                    });
                } else {
                    mInterstitialAd.setInterstitialAd(null);
                }
            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                callback.onNextAction();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
                if (shouldReloadAds) {
                    Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                        @Override
                        public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                            super.onInterstitialLoad(interstitialAd);
                            mInterstitialAd.setInterstitialAd(interstitialAd);
                            callback.onInterstitialLoad(mInterstitialAd.getInterstitialAd());
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            callback.onAdFailedToLoad(i);
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(adError);
                        }

                    });
                } else {
                    mInterstitialAd.setInterstitialAd(null);
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
            }

            @Override
            public void onInterstitialShow() {
                super.onInterstitialShow();
                callback.onInterstitialShow();
            }
        };
        Admob.getInstance().forceShowInterstitial(context, mInterstitialAd.getInterstitialAd(), adCallback);
    }

    public void showintersialClickCount(Context context, ApInterstitialAd mInterstitialAd, AdCallback callback) {

        AdCallback adCallback = new AdCallback() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.e("showintersialClickCount", "onAdClosed");
                callback.onAdClosed();
                Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        mInterstitialAd.setInterstitialAd(interstitialAd);
                        callback.onInterstitialLoad(mInterstitialAd.getInterstitialAd());
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        mInterstitialAd.setInterstitialAd(null);
                        callback.onAdFailedToLoad(i);
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(adError);
                    }

                });

            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                Log.e("showintersialClickCount", "onNextAction");
                callback.onNextAction();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
                Log.e("showintersialClickCount", "onAdFailedToShow");

                Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                    @Override
                    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                        super.onInterstitialLoad(interstitialAd);
                        mInterstitialAd.setInterstitialAd(interstitialAd);
                        callback.onInterstitialLoad(mInterstitialAd.getInterstitialAd());
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        callback.onAdFailedToLoad(i);
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        callback.onAdFailedToShow(adError);
                    }

                });

            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
                Log.e("showintersialClickCount", "onAdClicked");
            }

            @Override
            public void onInterstitialShow() {
                super.onInterstitialShow();
                callback.onInterstitialShow();
                Log.e("showintersialClickCount", "onInterstitialShow");
            }
        };


        Admob.getInstance().showInterstitialAdByTimesClick(context, mInterstitialAd.getInterstitialAd(), adCallback);
    }


    public void loadAndShowIntersialAd(Context context, String id, AdCallback callback) {
        Log.e("intersial", "==> a");
        Admob.getInstance().directLoadAndInterstitial(context, id, callback);
    }


    public void loadNativeAdResultCallback(final Activity activity, String id,
                                           int layoutCustomNative, AdCallback callback) {
        Admob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                callback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
            }
        });
    }


    public void loadNativeAdsFullScreen(final Activity activity, String id, int layoutCustomNative, AdCallback callback) {
        Admob.getInstance().loadNativeAdsFullScreen(((Context) activity), id, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                callback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
            }
        });
    }


    public void loadNativeAd(final Activity activity, String id, int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout containerShimmerLoading, AdCallback callback) {
        Admob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
                populateNativeAdView(activity, new ApNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                callback.onAdImpression();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                callback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
            }
        });
    }


    public void loadNativePrioritySameTime(Activity activity, String ID_NATIVE_PRIORITY, String ID_NATIVE_NORMAL, int layout, final AdCallback adCallback) {
        Admob.getInstance().loadNativePrioritySameTime(activity, ID_NATIVE_PRIORITY, ID_NATIVE_NORMAL, layout, adCallback);
    }

    public void loadNative3Alternate(Activity activity, String ID_NATIVE_PRIORITY, String ID_NATIVE_MEDIUM, String ID_NATIVE_NORMAL, int layout, AdCallback callback) {
        Admob.getInstance().loadNative3Alternate(activity, ID_NATIVE_PRIORITY, ID_NATIVE_MEDIUM, ID_NATIVE_NORMAL, layout, callback);
    }

    public void loadNativePriorityAlternate(final Activity activity, String ID_NATIVE_PRIORITY, final String ID_NATIVE_NORMAL, final int layout, final AdCallback adCallback) {
        Admob.getInstance().loadNativePriorityAlternate(activity, ID_NATIVE_PRIORITY, ID_NATIVE_NORMAL, layout, adCallback);
    }

    public void loadNative3SameTime(Activity activity, String ID_NATIVE_PRIORITY, String ID_NATIVE_MEDIUM, String ID_NATIVE_NORMAL, int layout, final AdCallback adCallback) {
        Admob.getInstance().loadNative3SameTime(activity, ID_NATIVE_PRIORITY, ID_NATIVE_MEDIUM, ID_NATIVE_NORMAL, layout, adCallback);
    }

    public SdkAdAdapter getNativeRepeatAdapter(final Activity activity, String str, int i, int i2, RecyclerView.Adapter adapter, final SdkAdPlacer.Listener listener, int i3, boolean isRepeting) {
        return Admob.getInstance().getNativeRepeatAdapter(activity, str, i, i2, adapter, listener, i3, isRepeting);
    }


    public void populateNativeAdView(Activity activity, ApNativeAd apNativeAd, FrameLayout adPlaceHolder, ShimmerFrameLayout containerShimmerLoading) {
        try {
            if (apNativeAd.getAdmobNativeAd() == null && apNativeAd.getNativeView() == null) {
                containerShimmerLoading.setVisibility(View.GONE);
                return;
            }
            @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(activity).inflate(apNativeAd.getLayoutCustomNative(), null);
            containerShimmerLoading.stopShimmer();
            containerShimmerLoading.setVisibility(View.GONE);
            adPlaceHolder.setVisibility(View.VISIBLE);
            Admob.getInstance().populateUnifiedNativeAdView(apNativeAd.getAdmobNativeAd(), adView);
            adPlaceHolder.removeAllViews();
            adPlaceHolder.addView(adView);
        } catch (Exception e) {

        }
    }

    public void initRewardAds(Context context, String id) {
        Admob.getInstance().initRewardAds(context, id);
    }

    public void initRewardAds(Context context, String id, AdCallback callback) {
        Admob.getInstance().initRewardAds(context, id, callback);
    }

    public void getRewardInterstitial(Context context, String id, AdCallback callback) {
        Admob.getInstance().getRewardInterstitial(context, id, callback);
    }

    public void showRewardInterstitial(Activity activity, RewardedInterstitialAd rewardedInterstitialAd, RewardCallback adCallback) {
        Admob.getInstance().showRewardInterstitial(activity, rewardedInterstitialAd, adCallback);
    }

    public void showRewardAds(Activity context, RewardCallback adCallback) {
        Admob.getInstance().showRewardAds(context, adCallback);
    }

    public void showRewardAds(Activity context, RewardedAd rewardedAd, RewardCallback adCallback) {
        Admob.getInstance().showRewardAds(context, rewardedAd, adCallback);
    }

    public void initLoadAndShowRewardAds(Context context, String id, RewardCallback callback) {
        Admob.getInstance().initLoadAndShowRewardAds(context, id, callback);
    }


    AppCompatActivity activity;
    private AppUpdateManager appUpdateManager;
    boolean flexiblecheck = false;
    ActivityResultLauncher<IntentSenderRequest> updateLauncher;

    public void appUpdateDilog(AppCompatActivity context, boolean flexiblecheck, ActivityResultLauncher<IntentSenderRequest> updateLauncher) {
        this.activity = context;
        this.flexiblecheck = flexiblecheck;
        this.updateLauncher = updateLauncher;
        appUpdateManager = AppUpdateManagerFactory.create(activity);
        checkUpdate();
    }

    private final IntentSenderForResultStarter updateResultStarter =
            new IntentSenderForResultStarter() {
                @Override
                public void startIntentSenderForResult(@NonNull IntentSender intent, int i, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int i3, @Nullable Bundle bundle) throws IntentSender.SendIntentException {
                    IntentSenderRequest request = new IntentSenderRequest.Builder(intent)
                            .setFillInIntent(fillInIntent)
                            .setFlags(flagsValues, flagsMask)
                            .build();
                    // launch updateLauncher
                    updateLauncher.launch(request);
                }


            };

//    private final ActivityResultLauncher<IntentSenderRequest> updateLauncher =
//            activity.registerForActivityResult(
//                    new ActivityResultContracts.StartIntentSenderForResult(),
//                    result -> {
//                        if (result.getData() == null) return;
//                        if (result.getResultCode() == 65) {
//                            Toast.makeText(activity, "Downloading started", Toast.LENGTH_SHORT).show();
//                            if (result.getResultCode() != Activity.RESULT_OK) {
//                                Toast.makeText(activity, "Downloading failed", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//            );

    private final InstallStateUpdatedListener listener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            long bytesDownloaded = state.bytesDownloaded();
            long totalBytesToDownload = state.totalBytesToDownload();
            // Handle download progress if needed
        }

        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            if (appUpdateManager != null) {
                appUpdateManager.completeUpdate();
            }
        }
    };


    private void checkUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager != null ? appUpdateManager.getAppUpdateInfo() : null;

        int ischeck;
        if (flexiblecheck) {
            ischeck = AppUpdateType.FLEXIBLE;
        } else {
            ischeck = AppUpdateType.IMMEDIATE;
        }

        if (appUpdateInfoTask != null) {
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(ischeck)) {
                    Log.e("Update Dialog", " --> checkUpdate --> if");

                    try {
                        Log.d("Update", " --> No Update available 12 213 123");
//                        MyApplication.getInstance().setIsShowCheckForUpdateDialog(true);

                        if (appUpdateManager != null) {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    updateResultStarter,
                                    AppUpdateOptions.newBuilder(ischeck).build(),
                                    65
                            );
                        }
                    } catch (IntentSender.SendIntentException exception) {
                        Log.e("Update Dialog", " --> checkUpdate --> IntentSender.SendIntentException --> ERROR = " + exception);
                        Toast.makeText(
                                activity,
                                "Somthing Wrong",
                                Toast.LENGTH_SHORT
                        ).show();
                    } catch (Exception e) {
                        Log.e("Update Dialog", " --> checkUpdate --> ERROR = " + e);
                    }
                } else {
                    Log.e("Update", " --> No Update available");
                }
            }).addOnFailureListener(e ->
                    Log.e("Update Dialog,", " --> checkUpdate --> Failed = " + e.getMessage())
            );

            appUpdateManager.registerListener(listener);
        }
    }


}
