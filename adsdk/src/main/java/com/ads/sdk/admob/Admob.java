package com.ads.sdk.admob;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.ads.sdk.R;
import com.ads.sdk.adapternative.AdmobRecyclerAdapter;
import com.ads.sdk.adapternative.SdkAdAdapter;
import com.ads.sdk.adapternative.SdkAdPlacer;
import com.ads.sdk.adapternative.SdkAdPlacerSettings;
import com.ads.sdk.ads.wrapper.ApNativeAd;
import com.ads.sdk.billing.AppPurchase;
import com.ads.sdk.dialog.PrepareLoadingAdsDialog;
import com.ads.sdk.dialog.PrepareLoadingRewardAdsDialog;
import com.ads.sdk.event.SdkLogEventManager;
import com.ads.sdk.funtion.AdCallback;
import com.ads.sdk.funtion.AdType;
import com.ads.sdk.funtion.AdmobHelper;
import com.ads.sdk.funtion.RewardCallback;
import com.ads.sdk.helper.banner.param.BannerSize;
import com.ads.sdk.util.SharePreferenceUtils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MediaAspectRatio;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Admob {
    private static final String TAG = "SDK-Studio";
    private static Admob instance;
    public int currentClicked = 0;
    private String nativeId;
    private int numShowAds = 3;

    private int maxClickAds = 100;
    private Handler handlerTimeout;
    private Runnable rdTimeout;
    private PrepareLoadingAdsDialog dialog;
    private boolean isTimeout;
    private boolean disableAdResumeWhenClickAds = false;
    private boolean isShowLoadingSplash = false;
    boolean isTimeDelay = false;
    public boolean openActivityAfterShowInterAds = false;
    private Context context;

    public static final String BANNER_INLINE_SMALL_STYLE = "BANNER_INLINE_SMALL_STYLE";
    public static final String BANNER_INLINE_LARGE_STYLE = "BANNER_INLINE_LARGE_STYLE";
    private final int MAX_SMALL_INLINE_BANNER_HEIGHT = 50;

    InterstitialAd mInterstitialSplash;

    private String tokenAdjust;


    public void setMaxClickAdsPerDay(int maxClickAds) {
        this.maxClickAds = maxClickAds;
    }

    public static Admob getInstance() {
        if (instance == null) {
            instance = new Admob();
            instance.isShowLoadingSplash = false;
        }
        return instance;
    }

    private Admob() {

    }


    public void removeNavigation(Activity activity,boolean isLight) {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(activity.getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
                if (activity.getWindow().getDecorView().getRootWindowInsets() != null) {
                    activity.getWindow().getDecorView().getRootWindowInsets().getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars());
                }
                activity.getWindow().setDecorFitsSystemWindows(true);
            }
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(InputDeviceCompat.SOURCE_TOUCHSCREEN);
        }
        new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(isLight);
    }

    public void setNumToShowAds(int numShowAds) {
        this.numShowAds = numShowAds;
    }

    public void setNumToShowAds(int numShowAds, int currentClicked) {
        this.numShowAds = numShowAds;
        this.currentClicked = currentClicked;
    }

    public void setDisableAdResumeWhenClickAds(boolean disableAdResumeWhenClickAds) {
        this.disableAdResumeWhenClickAds = disableAdResumeWhenClickAds;
    }

    public void init(Context context, List<String> testDeviceList, String tokenAdjust) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        MobileAds.initialize(context, initializationStatus -> {
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                if (status != null) {
                    Log.d(TAG, String.format("Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }
            }
        });
        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(testDeviceList).build());

        this.tokenAdjust = tokenAdjust;
        this.context = context;
    }

    public void init(Context context, String tokenAdjust) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }

        MobileAds.initialize(context, initializationStatus -> {
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                if (status != null) {
                    Log.d(TAG, String.format("Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }
            }
        });

        this.tokenAdjust = tokenAdjust;
        this.context = context;
    }

    public boolean isShowLoadingSplash() {
        return isShowLoadingSplash;
    }

    private String getProcessName(Context context) {
        if (context == null) return null;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }

    public void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds) {
        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds;
    }

    @SuppressLint("VisibleForTests")
    public AdRequest getAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }

    public boolean interstitialSplashLoaded() {
        return mInterstitialSplash != null;
    }

    public InterstitialAd getInterstitialSplash() {
        return mInterstitialSplash;
    }

    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, AdCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;

        if (AppPurchase.getInstance().isPurchased(context)) {
            if (adListener != null) {
                adListener.onNextAction();
            }
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //check delay show ad splash
                if (mInterstitialSplash != null) {
                    onShowSplash((AppCompatActivity) context, adListener);
                    return;
                }
                isTimeDelay = true;
            }
        }, timeDelay);

        if (timeOut > 0) {
            handlerTimeout = new Handler();
            rdTimeout = new Runnable() {
                @Override
                public void run() {
                    isTimeout = true;
                    if (mInterstitialSplash != null) {
                        onShowSplash((AppCompatActivity) context, adListener);
                        return;
                    }
                    if (adListener != null) {
                        adListener.onNextAction();
                        isShowLoadingSplash = false;
                    }
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeOut);
        }


        isShowLoadingSplash = true;
        getInterstitialAds(context, id, new AdCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                if (isTimeout)
                    return;
                if (interstitialAd != null) {
                    mInterstitialSplash = interstitialAd;
                    if (isTimeDelay) {
                        onShowSplash((AppCompatActivity) context, adListener);
                    }
                }
            }


            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                isShowLoadingSplash = false;
                if (isTimeout)
                    return;
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout.removeCallbacks(rdTimeout);
                    }
                    adListener.onAdFailedToLoad(i);
                    adListener.onNextAction();
                }
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                if (adListener != null) {
                    adListener.onAdFailedToShow(adError);
                    adListener.onNextAction();
                }
            }
        });

    }

    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, boolean showSplashIfReady, AdCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        Log.e(TAG, " loadSplashInterstitialAds call");
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
        if (AppPurchase.getInstance().isPurchased(context)) {
            Log.e(TAG, "loadSplashInterstitialAds ==> 1");
            if (adListener != null) {
                Log.e(TAG, "loadSplashInterstitialAds ==> 2");
                adListener.onNextAction();
            }
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialSplash != null) {

                    if (showSplashIfReady) {
                        Log.e(TAG, "loadSplashInterstitialAds ==> 4");
                        onShowSplash((AppCompatActivity) context, adListener);
                    } else {
                        Log.e(TAG, "loadSplashInterstitialAds ==> 5");
                        adListener.onAdSplashReady();
                    }
                    return;
                }
                Log.e(TAG, "loadSplashInterstitialAds ==> 6");
                isTimeDelay = true;
            }
        }, timeDelay);

        if (timeOut > 0) {
            Log.e(TAG, "loadSplashInterstitialAds ==> 7");
            handlerTimeout = new Handler();
            rdTimeout = new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "loadSplashInterstitialAds ==> 8");
                    isTimeout = true;
                    if (mInterstitialSplash != null) {
                        if (showSplashIfReady)
                            onShowSplash((AppCompatActivity) context, adListener);
                        else
                            adListener.onAdSplashReady();
                        return;
                    }
                    if (adListener != null) {
                        adListener.onNextAction();
                        isShowLoadingSplash = false;
                    }
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeOut);
        }

        isShowLoadingSplash = true;
        getInterstitialAds(context, id, new AdCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.e(TAG, "onInterstitialLoad ==> 8");
                if (isTimeout)
                    return;
                if (interstitialAd != null) {
                    mInterstitialSplash = interstitialAd;
                    if (isTimeDelay) {
                        if (showSplashIfReady)
                            onShowSplash((AppCompatActivity) context, adListener);
                        else
                            adListener.onAdSplashReady();
                    }
                }
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                Log.e(TAG, "onInterstitialLoad ==> onAdFailedToShow");
                if (adListener != null) {
                    adListener.onNextAction();
                    adListener.onAdFailedToShow(adError);
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "onInterstitialLoad ==> onAdFailedToLoad");
                if (isTimeout)
                    return;
                if (adListener != null) {


                    adListener.onNextAction();
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout.removeCallbacks(rdTimeout);
                    }
                    adListener.onAdFailedToLoad(i);
                }
            }
        });
//            }
//        }, 3000);

    }

    public void onShowSplash(AppCompatActivity activity, AdCallback adListener) {
        isShowLoadingSplash = true;
        Log.e(TAG, "onShowSplash");

        if (mInterstitialSplash == null) {
            adListener.onNextAction();
            return;
        }

        mInterstitialSplash.setOnPaidEventListener(adValue -> {
            SdkLogEventManager.logPaidAdImpression(context,
                    adValue,
                    mInterstitialSplash.getAdUnitId(),
                    mInterstitialSplash.getResponseInfo()
                            .getMediationAdapterClassName(), AdType.INTERSTITIAL);
            if (tokenAdjust != null) {
                SdkLogEventManager.logPaidAdjustWithToken(adValue, mInterstitialSplash.getAdUnitId(), tokenAdjust);
            }
        });

        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout.removeCallbacks(rdTimeout);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }
        if (mInterstitialSplash != null) {
            Log.e(TAG, "onShowSplash  if");
        } else {
            Log.e(TAG, "onShowSplash  else");
        }


        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            Log.e(TAG, "==>" + "RESUMED");
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new PrepareLoadingAdsDialog(activity);
                try {
                    dialog.show();
                    AppOpenManager.getInstance().setInterstitialShowing(true);
                } catch (Exception e) {
                    assert adListener != null;
                    adListener.onNextAction();
                    return;
                }
            } catch (Exception e) {
                dialog = null;
                e.printStackTrace();
            }
            new Handler().postDelayed(() -> {
                if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    if (openActivityAfterShowInterAds && adListener != null) {
                        Log.e("PPPPPPPPPPPPPPP", "==>" + openActivityAfterShowInterAds);
                        adListener.onNextAction();
                        new Handler().postDelayed(() -> {
                            if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                                dialog.dismiss();
                        }, 1500);
                    }
                    Log.e("PPPPPPPPPPPPPPP", "==>" + openActivityAfterShowInterAds);
                    if (mInterstitialSplash != null) {
                        mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                Log.e(TAG, "==>" + "onAdShowedFullScreenContent");
                                AppOpenManager.getInstance().setInterstitialShowing(true);
                                isShowLoadingSplash = false;
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.e(TAG, "==>" + "onAdDismissedFullScreenContent");
                                AppOpenManager.getInstance().setInterstitialShowing(false);
                                mInterstitialSplash = null;
                                if (adListener != null) {
                                    if (!openActivityAfterShowInterAds) {
                                        adListener.onNextAction();
                                    }
                                    adListener.onAdClosed();
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                                isShowLoadingSplash = false;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                Log.e(TAG, "==>" + "onAdFailedToShowFullScreenContent");
                                mInterstitialSplash = null;
                                isShowLoadingSplash = false;
                                if (adListener != null) {
                                    adListener.onAdFailedToShow(adError);
                                    adListener.onNextAction();
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                Log.e(TAG, "==>" + "onAdClicked");
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                SdkLogEventManager.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                                Log.e(TAG, "==>" + "onAdImpression");
                                if (adListener != null) {
                                    adListener.onAdImpression();
                                }
                            }
                        });
                        mInterstitialSplash.show(activity);

                        isShowLoadingSplash = false;
                    } else if (adListener != null) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        adListener.onNextAction();
                        isShowLoadingSplash = false;
                    }

                } else {
                    if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                        dialog.dismiss();
                    isShowLoadingSplash = false;
                    assert adListener != null;
                    adListener.onAdFailedToShow(new AdError(0, "Show fail in background after show loading ad", "LuanDT"));
                }
            }, 800);

        } else {
            Log.e(TAG, "==>" + "RESUMED else");
            isShowLoadingSplash = false;
        }


    }

    public void onShowSplash(AppCompatActivity activity, AdCallback adListener, InterstitialAd mInter) {
        mInterstitialSplash = mInter;
        isShowLoadingSplash = true;

        if (mInter == null) {
            adListener.onNextAction();
            return;
        }

        mInterstitialSplash.setOnPaidEventListener(adValue -> {
            SdkLogEventManager.logPaidAdImpression(context,
                    adValue,
                    mInterstitialSplash.getAdUnitId(),
                    mInterstitialSplash.getResponseInfo()
                            .getMediationAdapterClassName(), AdType.INTERSTITIAL);

            if (tokenAdjust != null) {
                SdkLogEventManager.logPaidAdjustWithToken(adValue, mInterstitialSplash.getAdUnitId(), tokenAdjust);
            }
        });

        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout.removeCallbacks(rdTimeout);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }

        mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                AppOpenManager.getInstance().setInterstitialShowing(true);
                isShowLoadingSplash = false;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                AppOpenManager.getInstance().setInterstitialShowing(false);
                mInterstitialSplash = null;
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onNextAction();
                    }
                    adListener.onAdClosed();

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                isShowLoadingSplash = false;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                mInterstitialSplash = null;
                isShowLoadingSplash = false;
                if (adListener != null) {
                    adListener.onAdFailedToShow(adError);
                    if (!openActivityAfterShowInterAds) {
                        adListener.onNextAction();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                SdkLogEventManager.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                if (adListener != null) {
                    adListener.onAdImpression();
                }
            }
        });

        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new PrepareLoadingAdsDialog(activity);
                try {
                    dialog.show();
                    AppOpenManager.getInstance().setInterstitialShowing(true);
                } catch (Exception e) {
                    adListener.onNextAction();
                    return;
                }
            } catch (Exception e) {
                dialog = null;
                e.printStackTrace();
            }
            new Handler().postDelayed(() -> {
                if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    if (openActivityAfterShowInterAds && adListener != null) {
                        adListener.onNextAction();
                        new Handler().postDelayed(() -> {
                            if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                                dialog.dismiss();
                        }, 1500);
                    }
                    if (mInterstitialSplash != null) {
                        mInterstitialSplash.show(activity);
                        isShowLoadingSplash = false;
                    } else if (adListener != null) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        adListener.onNextAction();
                        isShowLoadingSplash = false;
                    }
                } else {
                    if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                        dialog.dismiss();
                    isShowLoadingSplash = false;
                    assert adListener != null;
                    adListener.onAdFailedToShow(new AdError(0, "Show fail in background after show loading ad", "LuanDT"));
                }
            }, 800);

        } else {
            isShowLoadingSplash = false;
        }
    }

    public void onCheckShowSplashWhenFail(AppCompatActivity activity, AdCallback callback, int timeDelay) {
        new Handler(activity.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (interstitialSplashLoaded() && !isShowLoadingSplash()) {
                    Admob.getInstance().onShowSplash(activity, callback);
                }
            }
        }, timeDelay);
    }

    public void getInterstitialAds(Context context, String id, AdCallback adCallback) {

        if (AppPurchase.getInstance().isPurchased(context) || AdmobHelper.getNumClickAdsPerDay(context, id) >= maxClickAds) {
            adCallback.onInterstitialLoad(null);
            return;
        }

        InterstitialAd.load(context, id, getAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        if (adCallback != null)
                            adCallback.onInterstitialLoad(interstitialAd);

                        interstitialAd.setOnPaidEventListener(adValue -> {
                            SdkLogEventManager.logPaidAdImpression(context,
                                    adValue,
                                    interstitialAd.getAdUnitId(),
                                    interstitialAd.getResponseInfo()
                                            .getMediationAdapterClassName(), AdType.INTERSTITIAL);
                            if (tokenAdjust != null) {
                                SdkLogEventManager.logPaidAdjustWithToken(adValue, interstitialAd.getAdUnitId(), tokenAdjust);
                            }
                        });


                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                AppOpenManager.getInstance().setInterstitialShowing(false);
                                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
                                if (adCallback != null) {
                                    if (!openActivityAfterShowInterAds) {
                                        adCallback.onNextAction();
                                    }
                                    adCallback.onAdClosed();
                                }
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                if (adCallback != null) {
                                    adCallback.onAdFailedToShow(adError);
                                    adCallback.onNextAction();

                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                                AppOpenManager.getInstance().setInterstitialShowing(true);
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                if (adCallback != null) {
                                    adCallback.onAdClicked();
                                }
                                SdkLogEventManager.logClickAdsEvent(context, interstitialAd.getAdUnitId());
                            }
                        });


                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i(TAG, loadAdError.getMessage());
                        if (adCallback != null) {
                            adCallback.onAdFailedToLoad(loadAdError);
                            adCallback.onNextAction();
                        }
                    }


                });

    }


//    public void getInterstitialAdsOther(Context context, String id, AdCallback adCallback, boolean shouldReloadAds) {
//        this.shouldReloadAds = shouldReloadAds;
//        this.adCallbacknew = adCallback;
//        if (AppPurchase.getInstance().isPurchased(context) || AdmobHelper.getNumClickAdsPerDay(context, id) >= maxClickAds) {
//            adCallback.onInterstitialLoad(null);
//            return;
//        }
//
//        InterstitialAd.load(context, id, getAdRequest(),
//                new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                        if (adCallback != null)
//                            adCallback.onInterstitialLoad(interstitialAd);
//
//                        interstitialAd.setOnPaidEventListener(adValue -> {
//                            SdkLogEventManager.logPaidAdImpression(context,
//                                    adValue,
//                                    interstitialAd.getAdUnitId(),
//                                    interstitialAd.getResponseInfo()
//                                            .getMediationAdapterClassName(), AdType.INTERSTITIAL);
//                            if (tokenAdjust != null) {
//                                SdkLogEventManager.logPaidAdjustWithToken(adValue, interstitialAd.getAdUnitId(), tokenAdjust);
//                            }
//                        });
//
//
//                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//
//                            @Override
//                            public void onAdDismissedFullScreenContent() {
//                                super.onAdDismissedFullScreenContent();
//                                AppOpenManager.getInstance().setInterstitialShowing(false);
//                                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
//                                if (adCallback != null) {
//                                    if (!openActivityAfterShowInterAds) {
//                                        adCallback.onNextAction();
//                                    }
//                                    adCallback.onAdClosed();
//                                }
//                                if (dialog != null) {
//                                    dialog.dismiss();
//                                }
//                            }
//
//                            @Override
//                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                                super.onAdFailedToShowFullScreenContent(adError);
//                                if (adCallback != null) {
//                                    adCallback.onAdFailedToShow(adError);
//                                    if (!openActivityAfterShowInterAds) {
//                                        adCallback.onNextAction();
//                                    }
//
//                                    if (dialog != null) {
//                                        dialog.dismiss();
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onAdShowedFullScreenContent() {
//                                super.onAdShowedFullScreenContent();
//                                AppOpenManager.getInstance().setInterstitialShowing(true);
//                            }
//
//                            @Override
//                            public void onAdClicked() {
//                                super.onAdClicked();
//                                if (disableAdResumeWhenClickAds)
//                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
//                                if (adCallback != null) {
//                                    adCallback.onAdClicked();
//                                }
//                                SdkLogEventManager.logClickAdsEvent(context, interstitialAd.getAdUnitId());
//                            }
//                        });
//
//
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        Log.i(TAG, loadAdError.getMessage());
//                        if (adCallback != null)
//                            adCallback.onAdFailedToLoad(loadAdError);
//                    }
//
//
//                });
//
//    }

    public void showInterstitialAdByTimes(final Context context, final InterstitialAd mInterstitialAd, final AdCallback callback, long timeDelay) {
        if (timeDelay > 0) {
            handlerTimeout = new Handler();
            rdTimeout = new Runnable() {
                @Override
                public void run() {
                    forceShowInterstitial(context, mInterstitialAd, callback);
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeDelay);
        } else {
            forceShowInterstitial(context, mInterstitialAd, callback);
        }
    }


    public void showInterstitialAdByTimes(final Context context, InterstitialAd mInterstitialAd, final AdCallback callback) {
        AdmobHelper.setupAdmobData(context);
        if (AppPurchase.getInstance().isPurchased(context)) {
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null) {
            if (callback != null) {
                callback.onNextAction();
            }
            return;
        }

        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                AppOpenManager.getInstance().setInterstitialShowing(false);
                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onNextAction();
                    }
                    callback.onAdClosed();
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                if (callback != null) {
                    callback.onAdFailedToShow(adError);
                    if (!openActivityAfterShowInterAds) {
                        callback.onNextAction();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                AppOpenManager.getInstance().setInterstitialShowing(true);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                if (callback != null) {
                    callback.onAdClicked();
                }
                SdkLogEventManager.logClickAdsEvent(context, mInterstitialAd.getAdUnitId());
            }
        });

        if (AdmobHelper.getNumClickAdsPerDay(context, mInterstitialAd.getAdUnitId()) < maxClickAds) {
            showInterstitialAd(context, mInterstitialAd, callback);
            return;
        }
        if (callback != null) {
            callback.onNextAction();
        }
    }


    public void showInterstitialAdByTimesClick(final Context context, InterstitialAd mInterstitialAd, final AdCallback callback) {


        if (AppPurchase.getInstance().isPurchased(context)) {
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null) {
            if (callback != null) {
                callback.onNextAction();
            }
            return;
        }
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                AppOpenManager.getInstance().setInterstitialShowing(false);
                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onNextAction();
                    }
                    callback.onAdClosed();
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                if (callback != null) {
                    callback.onAdFailedToShow(adError);
                    if (!openActivityAfterShowInterAds) {
                        callback.onNextAction();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                AppOpenManager.getInstance().setInterstitialShowing(true);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                if (callback != null) {
                    callback.onAdClicked();
                }
                SdkLogEventManager.logClickAdsEvent(context, mInterstitialAd.getAdUnitId());
            }
        });

        showInterstitialAd(context, mInterstitialAd, callback);

    }


    public void forceShowInterstitial(Context context, InterstitialAd mInterstitialAd, final AdCallback callback) {
        currentClicked = numShowAds;
        showInterstitialAdByTimes(context, mInterstitialAd, callback);
    }


    public void showInterstitialAd(Context context, InterstitialAd mInterstitialAd, AdCallback adCallback) {

        currentClicked++;
        if (currentClicked >= numShowAds && mInterstitialAd != null) {
            Log.e("showInterstitialAd", " 1");
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                Log.e("showInterstitialAd", " 2");

                try {
                    Log.e("showInterstitialAd", " 3");
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new PrepareLoadingAdsDialog(context);
                    dialog.setCancelable(false);
                    try {
                        Log.e("showInterstitialAd", " 4");
                        adCallback.onInterstitialShow();
                        dialog.show();
                        AppOpenManager.getInstance().setInterstitialShowing(true);
                    } catch (Exception e) {
                        Log.e("showInterstitialAd", " 5");
                        adCallback.onNextAction();
                        return;
                    }
                } catch (Exception e) {
                    dialog = null;
                    Log.e("showInterstitialAd", " 6");
                    e.printStackTrace();
                }
                new Handler().postDelayed(() -> {
                    if (((AppCompatActivity) context).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.e("showInterstitialAd", " 7" + openActivityAfterShowInterAds);
                        if (openActivityAfterShowInterAds && adCallback != null) {
                            Log.e("showInterstitialAd", " 8");
                            adCallback.onNextAction();
                            new Handler().postDelayed(() -> {
                                if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                                    dialog.dismiss();
                            }, 1500);
                        }

                        mInterstitialAd.show((Activity) context);


                    } else {
                        Log.e("showInterstitialAd", " 9");
                        if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                            dialog.dismiss();
                        adCallback.onAdFailedToShow(new AdError(0, "Show fail in background after show loading ad", "LuanDT"));
                    }
                }, 800);

            }
            currentClicked = 0;
        } else if (adCallback != null) {
            Log.e("showInterstitialAd", " 9");
            if (dialog != null) {
                dialog.dismiss();
            }
            adCallback.onNextAction();
//            adCallback.onAdClosed();
        }
    }


    public void directLoadAndInterstitial(Context context, String id, final AdCallback callback) {
        AdmobHelper.setupAdmobData(context);
        Log.e("intersial", "==>");
        if (AppPurchase.getInstance().isPurchased(context)) {
            callback.onNextAction();
            return;
        }

        try {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            dialog = new PrepareLoadingAdsDialog(context);
            try {
                dialog.show();
                AppOpenManager.getInstance().setInterstitialShowing(true);
            } catch (Exception e) {
                assert callback != null;
                callback.onNextAction();
                return;
            }
        } catch (Exception e) {
            dialog = null;
            e.printStackTrace();
        }

        InterstitialAd.load(context, id, getAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd mInterstitialAd) {
//                        if (adCallback != null)
//                            adCallback.onInterstitialLoad(interstitialAd);
                        Log.e("intersial", "==> onAdLoaded");

                        mInterstitialAd.setOnPaidEventListener(adValue -> {
                            SdkLogEventManager.logPaidAdImpression(context,
                                    adValue,
                                    mInterstitialAd.getAdUnitId(),
                                    mInterstitialAd.getResponseInfo()
                                            .getMediationAdapterClassName(), AdType.INTERSTITIAL);
                            if (tokenAdjust != null) {
                                SdkLogEventManager.logPaidAdjustWithToken(adValue, mInterstitialAd.getAdUnitId(), tokenAdjust);
                            }
                        });

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Log.e("intersial", "==> onAdDismissedFullScreenContent");
                                AppOpenManager.getInstance().setInterstitialShowing(false);
                                SharePreferenceUtils.setLastImpressionInterstitialTime(context);
                                if (callback != null) {
                                    if (!openActivityAfterShowInterAds) {
                                        callback.onNextAction();
                                    }
                                    callback.onAdClosed();
                                }
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Log.e("intersial", "==> onAdFailedToShowFullScreenContent");
                                if (callback != null) {
                                    callback.onAdFailedToShow(adError);
                                    if (!openActivityAfterShowInterAds) {
                                        callback.onNextAction();
                                    }

                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                                Log.e("intersial", "==> onAdShowedFullScreenContent");
                                AppOpenManager.getInstance().setInterstitialShowing(true);
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                Log.e("intersial", "==> onAdClicked");
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                if (callback != null) {
                                    callback.onAdClicked();
                                }
                                SdkLogEventManager.logClickAdsEvent(context, mInterstitialAd.getAdUnitId());
                            }
                        });

                        new Handler().postDelayed(() -> {
                            if (((AppCompatActivity) context).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                                if (openActivityAfterShowInterAds && callback != null) {
                                    callback.onNextAction();
                                    new Handler().postDelayed(() -> {
                                        if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                                            dialog.dismiss();
                                    }, 1500);
                                }
                                mInterstitialAd.show((Activity) context);
                            } else {
                                if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                                    dialog.dismiss();
                                callback.onAdFailedToShow(new AdError(0, "Show fail in background after show loading ad", "LuanDT"));
                            }
                        }, 800);


                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i(TAG, loadAdError.getMessage());
                        Log.e("intersial", "==> onAdFailedToLoad");
                        if (callback != null)
                            callback.onAdFailedToLoad(loadAdError);
                    }

                });
    }


    public void loadBanner(final Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
    }

    public void loadBanner(final Activity mActivity, String id, AdCallback callback) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
    }

    @Deprecated
    public void loadBanner(final Activity mActivity, String id, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
    }

    public void loadInlineBanner(final Activity activity, String id, String inlineStyle) {
        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
        loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
    }

    @Deprecated
    public void loadBanner(final Activity mActivity, String id, final AdCallback callback, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
    }

    public void loadInlineBanner(final Activity activity, String id, String inlineStyle, final AdCallback callback) {
        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
        loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
    }

    public void loadCollapsibleBanner(final Activity mActivity, String ids, String gravity, final AdCallback callback) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadCollapsibleBanner(mActivity, ids, gravity, adContainer, containerShimmer, callback);
    }

    public void loadCollapsibleBannerSizeMedium(final Activity mActivity, String ids, String gravity, AdSize sizeBanner, final AdCallback callback) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadCollapsibleAutoSizeMedium(mActivity, ids, gravity, sizeBanner, adContainer, containerShimmer, callback);
    }

    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
    }

    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final AdCallback callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
    }

    @Deprecated
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
    }

    public void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
    }

    @Deprecated
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final AdCallback callback, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
    }

    public void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle, final AdCallback callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
    }

    public void loadCollapsibleBannerFragment(final Activity mActivity, String id, final View rootView, String gravity, final AdCallback callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadCollapsibleBanner(mActivity, id, gravity, adContainer, containerShimmer, callback);
    }


    private void loadBanner(final Activity mActivity, String id,
                            final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer,
                            final AdCallback callback, Boolean useInlineAdaptive, String inlineStyle) {
        try {

            if (AppPurchase.getInstance().isPurchased(mActivity)) {
                containerShimmer.setVisibility(View.GONE);
                return;
            }

            containerShimmer.setVisibility(View.VISIBLE);
            containerShimmer.startShimmer();

            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, useInlineAdaptive, inlineStyle);
            int adHeight;
            if (useInlineAdaptive && inlineStyle.equalsIgnoreCase(BANNER_INLINE_SMALL_STYLE)) {
                adHeight = MAX_SMALL_INLINE_BANNER_HEIGHT;
            } else {
                adHeight = adSize.getHeight();
            }
            containerShimmer.getLayoutParams().height = (int) (adHeight * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);

                    if (callback != null) {
                        callback.onAdFailedToLoad(loadAdError);
                    }
                }


                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    if (adView != null) {
                        adView.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                            SdkLogEventManager.logPaidAdImpression(context,
                                    adValue,
                                    adView.getAdUnitId(),
                                    adView.getResponseInfo()
                                            .getMediationAdapterClassName(), AdType.BANNER);
                            if (tokenAdjust != null) {
                                SdkLogEventManager.logPaidAdjustWithToken(adValue, adView.getAdUnitId(), tokenAdjust);
                            }
                        });
                    }

                    if (callback != null) {
                        callback.onAdLoaded();
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    if (callback != null) {
                        callback.onAdClicked();
                        Log.d(TAG, "onAdClicked");
                    }
                    SdkLogEventManager.logClickAdsEvent(context, id);
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    if (callback != null) {
                        callback.onAdImpression();
                    }
                }
            });

            adView.loadAd(getAdRequest());
        } catch (Exception e) {
        }
    }

    public void loadCollapsibleBanner(final Activity mActivity, String id, String gravity, final FrameLayout adContainer,
                                      final ShimmerFrameLayout containerShimmer, final AdCallback callback) {
        try {
            if (AppPurchase.getInstance().isPurchased(mActivity)) {
                containerShimmer.setVisibility(View.GONE);
                return;
            }

            containerShimmer.setVisibility(View.VISIBLE);
            containerShimmer.startShimmer();

            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));

            Bundle bundle = new Bundle();
            bundle.putString("collapsible", gravity);

            AdRequest.Builder builder = new AdRequest.Builder();
//            AdRequest adRequest = new AdRequest.Builder()
            builder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);
            adView.loadAd(builder.build());

            adView.setAdListener(new AdListener() {

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.onAdFailedToLoad(loadAdError);
                    }
                }

                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    Log.d(TAG, "Banner adapter class name: " + "onAdLoaded");
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        SdkLogEventManager.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(),
                                adView.getResponseInfo()
                                        .getMediationAdapterClassName(), AdType.BANNER);
                        if (tokenAdjust != null) {
                            SdkLogEventManager.logPaidAdjustWithToken(adValue, adView.getAdUnitId(), tokenAdjust);
                        }
                    });
                    if (callback != null) {
                        callback.onAdLoaded();
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    SdkLogEventManager.logClickAdsEvent(context, id);
                    if (callback != null) {
                        callback.onAdClicked();
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    private void loadCollapsibleAutoSizeMedium(final Activity mActivity, String id, String gravity, AdSize sizeBanner, final FrameLayout adContainer,
                                               final ShimmerFrameLayout containerShimmer, final AdCallback callback) {
        try {
            if (AppPurchase.getInstance().isPurchased(mActivity)) {
                containerShimmer.setVisibility(View.GONE);
                return;
            }

            containerShimmer.setVisibility(View.VISIBLE);
            containerShimmer.startShimmer();

            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = sizeBanner;
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.onAdFailedToLoad(loadAdError);
                    }
                }

                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        SdkLogEventManager.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(),
                                adView.getResponseInfo()
                                        .getMediationAdapterClassName(), AdType.BANNER);
                        if (tokenAdjust != null) {
                            SdkLogEventManager.logPaidAdjustWithToken(adValue, adView.getAdUnitId(), tokenAdjust);
                        }
                    });
                    if (callback != null) {
                        callback.onAdLoaded();
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    SdkLogEventManager.logClickAdsEvent(context, id);
                    if (callback != null) {
                        callback.onAdClicked();
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    private void loadInlineAdaptiveBanner(final Activity mActivity, String id, AdCallback adCallback) {
        @SuppressLint("VisibleForTests") AdSize adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(mActivity, 320);
        AdView bannerView = new AdView(mActivity);
        bannerView.setAdUnitId(id);
        bannerView.setAdSize(adSize);
        bannerView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adCallback.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                adCallback.onAdFailedToLoad(loadAdError);
            }
        });
        bannerView.loadAd(getAdRequest());
    }

    @SuppressLint("VisibleForTests")
    private AdSize getAdSize(Activity mActivity, Boolean useInlineAdaptive, String inlineStyle) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        if (useInlineAdaptive) {
            if (inlineStyle.equalsIgnoreCase(BANNER_INLINE_LARGE_STYLE)) {
                return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(mActivity, adWidth);
            } else {
                return AdSize.getInlineAdaptiveBannerAdSize(adWidth, MAX_SMALL_INLINE_BANNER_HEIGHT);
            }
        }

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);

    }


    private AdRequest getAdRequestForCollapsibleBanner(String gravity) {
//        AdRequest.Builder builder = new AdRequest.Builder();
        Bundle bundle = new Bundle();
        bundle.putString("collapsible", gravity);
//        builder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);

        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, bundle)
                .build();
        return adRequest;
    }

    public void loadNative(final Activity mActivity, String id) {
        final FrameLayout frameLayout = mActivity.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_native);
        loadNative(mActivity, containerShimmer, frameLayout, id, R.layout.custom_native_admob_free_size);
    }

    public void loadNativeFragment(final Activity mActivity, String id, View parent) {
        final FrameLayout frameLayout = parent.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = parent.findViewById(R.id.shimmer_container_native);
        loadNative(mActivity, containerShimmer, frameLayout, id, R.layout.custom_native_admob_free_size);
    }

    public void loadSmallNative(final Activity mActivity, String adUnitId) {
        final FrameLayout frameLayout = mActivity.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_native);
        loadNative(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_admob_medium);
    }

    public void loadSmallNativeFragment(final Activity mActivity, String adUnitId, View parent) {
        final FrameLayout frameLayout = parent.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = parent.findViewById(R.id.shimmer_container_native);
        loadNative(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_admob_medium);
    }

    public void loadNativeAd(Context context, String id, final AdCallback callback) {

        if (AppPurchase.getInstance().isPurchased(context)) {
            if (callback != null) {
                callback.onAdFailedToLoad(new LoadAdError(101, "isPremumUser", null, null, null));
            }
            return;
        }

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
//
        AdLoader adLoader = new AdLoader.Builder(context, id)
                .forNativeAd(nativeAd -> {
                    callback.onUnifiedNativeAdLoaded(nativeAd);
                    nativeAd.setOnPaidEventListener(adValue -> {
                        SdkLogEventManager.logPaidAdImpression(context,
                                adValue,
                                id,
                                nativeAd.getResponseInfo().getMediationAdapterClassName(), AdType.NATIVE);
                        if (tokenAdjust != null) {
                            SdkLogEventManager.logPaidAdjustWithToken(adValue, id, tokenAdjust);
                        }
                    });
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        callback.onAdFailedToLoad(error);
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        if (callback != null) {
                            callback.onAdImpression();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                        SdkLogEventManager.logClickAdsEvent(context, id);
                    }
                })
                .withNativeAdOptions(adOptions)
                .build();
        adLoader.loadAd(getAdRequest());
    }

    public void loadNativeAds(Context context, String id, final AdCallback callback, int countAd) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            callback.onAdClosed();
            return;
        }
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        AdLoader adLoader = new AdLoader.Builder(context, id)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        callback.onUnifiedNativeAdLoaded(nativeAd);
                        nativeAd.setOnPaidEventListener(adValue -> {
                            SdkLogEventManager.logPaidAdImpression(context,
                                    adValue,
                                    id,
                                    nativeAd.getResponseInfo().getMediationAdapterClassName(), AdType.NATIVE);
                            if (tokenAdjust != null) {
                                SdkLogEventManager.logPaidAdjustWithToken(adValue, id, tokenAdjust);
                            }
                        });
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        callback.onAdFailedToLoad(error);
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                        SdkLogEventManager.logClickAdsEvent(context, id);
                    }
                })
                .withNativeAdOptions(adOptions)
                .build();
        adLoader.loadAds(getAdRequest(), countAd);
    }

    private void loadNative(final Context context, final ShimmerFrameLayout containerShimmer, final FrameLayout frameLayout, final String id, final int layout) {

        if (AppPurchase.getInstance().isPurchased(context)) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }

        frameLayout.removeAllViews();
        frameLayout.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();


        AdLoader adLoader = new AdLoader.Builder(context, id)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.VISIBLE);
                        @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
                                .inflate(layout, null);
                        nativeAd.setOnPaidEventListener(adValue -> {
                            SdkLogEventManager.logPaidAdImpression(context,
                                    adValue,
                                    id,
                                    nativeAd.getResponseInfo().getMediationAdapterClassName(), AdType.NATIVE);
                            if (tokenAdjust != null) {
                                SdkLogEventManager.logPaidAdjustWithToken(adValue, id, tokenAdjust);
                            }
                        });
                        populateUnifiedNativeAdView(nativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }


                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        SdkLogEventManager.logClickAdsEvent(context, id);
                    }
                })
                .withNativeAdOptions(adOptions)
                .build();

        adLoader.loadAd(getAdRequest());
    }

    private void loadNative(final Context context, final ShimmerFrameLayout containerShimmer, final FrameLayout frameLayout, final String id, final int layout, final AdCallback callback) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        frameLayout.removeAllViews();
        frameLayout.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
//
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();


        AdLoader adLoader = new AdLoader.Builder(context, id)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.VISIBLE);
                        @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
                                .inflate(layout, null);
                        nativeAd.setOnPaidEventListener(adValue -> {
                            SdkLogEventManager.logPaidAdImpression(context,
                                    adValue,
                                    id,
                                    nativeAd.getResponseInfo().getMediationAdapterClassName(), AdType.NATIVE);
                            if (tokenAdjust != null) {
                                SdkLogEventManager.logPaidAdjustWithToken(adValue, id, tokenAdjust);
                            }
                        });
                        populateUnifiedNativeAdView(nativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                    }


                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                        SdkLogEventManager.logClickAdsEvent(context, id);
                    }
                })
                .withNativeAdOptions(adOptions)
                .build();


        adLoader.loadAd(getAdRequest());
    }

    public void loadNativeAdsFullScreen(Context context, String id, final AdCallback callback) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }

        VideoOptions videoOptions =
                new VideoOptions.Builder().setStartMuted(false).build();
        NativeAdOptions adOptions =
                new NativeAdOptions.Builder()
                        .setMediaAspectRatio(MediaAspectRatio.PORTRAIT)
                        .setVideoOptions(videoOptions)
                        .build();
        AdLoader adLoader = new AdLoader.Builder(context, id)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        callback.onUnifiedNativeAdLoaded(nativeAd);
                        nativeAd.setOnPaidEventListener(adValue -> {
                            SdkLogEventManager.logPaidAdImpression(context,
                                    adValue,
                                    id,
                                    nativeAd.getResponseInfo().getMediationAdapterClassName(), AdType.NATIVE);
                            if (tokenAdjust != null) {
                                SdkLogEventManager.logPaidAdjustWithToken(adValue, id, tokenAdjust);
                            }
                        });
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        callback.onAdFailedToLoad(error);
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                        SdkLogEventManager.logClickAdsEvent(context, id);
                    }
                })
                .withNativeAdOptions(adOptions)
                .build();
        adLoader.loadAds(getAdRequest(), 5);

    }

    public void loadNativeAdsFullScreen(final Context context, final ShimmerFrameLayout containerShimmer, final FrameLayout frameLayout, final String id, final int layout, final AdCallback callback) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        frameLayout.removeAllViews();
        frameLayout.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setMediaAspectRatio(MediaAspectRatio.PORTRAIT)
                .setVideoOptions(videoOptions)
                .build();


        AdLoader adLoader = new AdLoader.Builder(context, id)
                .forNativeAd(nativeAd -> {
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.VISIBLE);
                    @SuppressLint("InflateParams")
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
                            .inflate(layout, null);
                    nativeAd.setOnPaidEventListener(adValue -> {

                        SdkLogEventManager.logPaidAdImpression(context,
                                adValue,
                                id,
                                nativeAd.getResponseInfo().getMediationAdapterClassName(), AdType.NATIVE);
                        if (tokenAdjust != null) {
                            SdkLogEventManager.logPaidAdjustWithToken(adValue, id, tokenAdjust);
                        }
                    });
                    populateUnifiedNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                    }


                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        if (callback != null) {
                            callback.onAdClicked();
                        }
                        SdkLogEventManager.logClickAdsEvent(context, id);
                    }
                })
                .withNativeAdOptions(adOptions)
                .build();


        adLoader.loadAds(getAdRequest(), 5);

    }


    boolean i = false;
    boolean j = false;
    boolean k = false;

    private ApNativeAd m;
    private ApNativeAd l;

    public void loadNative3SameTime(Activity activity, String ID_NATIVE_PRIORITY, String ID_NATIVE_MEDIUM, String ID_NATIVE_NORMAL, int layout, final AdCallback adCallback) {
        this.i = false;
        this.j = false;
        this.k = false;
        this.m = null;
        this.l = null;
        loadNativeAdResultCallback(activity, ID_NATIVE_PRIORITY, layout, new AdCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError apAdError) {
                super.onAdFailedToLoad(apAdError);
                Log.e(TAG, "onAdFailedToLoad: loadAdNative3Sametime priority - " + apAdError.getMessage());
                if (j) {
                    if (m != null) {
                        adCallback.onNativeAdLoaded(m);
                        return;
                    } else if (k) {
                        if (l != null) {
                            adCallback.onNativeAdLoaded(l);
                            return;
                        } else {
                            adCallback.onAdFailedToLoad(apAdError);
                            return;
                        }
                    } else {
                        i = true;
                        return;
                    }
                }
                i = true;
            }


            @Override
            public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                super.onNativeAdLoaded(apNativeAd);
                Log.d(TAG, "onNativeAdLoaded: loadAdNative3Sametime priority");
                adCallback.onNativeAdLoaded(apNativeAd);
            }
        });
        loadNativeAdResultCallback(activity, ID_NATIVE_MEDIUM, layout, new AdCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError apAdError) {
                super.onAdFailedToLoad(apAdError);

                Log.e(TAG, "onAdFailedToLoad: loadAdNative3Sametime medium - " + apAdError.getMessage());
                if (i && k) {
                    if (l != null) {
                        adCallback.onNativeAdLoaded(l);
                        return;
                    } else {
                        adCallback.onAdFailedToLoad(apAdError);
                        return;
                    }
                }
                m = null;
                j = true;
            }


            @Override
            public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                super.onNativeAdLoaded(apNativeAd);
                Log.d(TAG, "onNativeAdLoaded: loadAdNative3Sametime medium");
                if (i) {
                    adCallback.onNativeAdLoaded(apNativeAd);
                    return;
                }
                m = apNativeAd;
                j = true;
            }
        });
        loadNativeAdResultCallback(activity, ID_NATIVE_NORMAL, layout, new AdCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                Log.e(TAG, "onAdFailedToLoad: loadAdNative3Sametime normal - " + adError.getMessage());
                if (i && j && m == null) {
                    adCallback.onAdFailedToLoad(adError);
                    return;
                }
                l = null;
                k = true;
            }


            @Override
            public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                super.onNativeAdLoaded(apNativeAd);
                Log.d(TAG, "onNativeAdLoaded: loadAdNative3Sametime normal");
                if (i && j && m == null) {
                    adCallback.onNativeAdLoaded(apNativeAd);
                    return;
                }
                l = apNativeAd;
                k = true;
            }
        });
    }


    public void loadNativePriorityAlternate(final Activity activity, String ID_NATIVE_PRIORITY, final String ID_NATIVE_NORMAL, final int layout, final AdCallback adCallback) {
        loadNativeAdResultCallback(activity, ID_NATIVE_PRIORITY, layout, new AdCallback() {


            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError apAdError) {
                super.onAdFailedToLoad(apAdError);
                Log.e(TAG, "onAdFailedToLoad: loadAdNativeLanguageAlternate priority - " + apAdError.getMessage());
                loadNativeAdResultCallback(activity, ID_NATIVE_NORMAL, layout, new AdCallback() {
                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError apAdError2) {
                        super.onAdFailedToLoad(apAdError2);
                        Log.e(TAG, "onAdFailedToLoad: loadAdNativeLanguageAlternate normal - " + apAdError2.getMessage());
                        adCallback.onAdFailedToLoad(apAdError2);
                    }

                    @Override
                    public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                        super.onNativeAdLoaded(apNativeAd);
                        Log.d(TAG, "onNativeAdLoaded: loadAdNativeLanguageAlternate normal");
                        adCallback.onNativeAdLoaded(apNativeAd);
                    }
                });
            }

            @Override
            public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                super.onNativeAdLoaded(apNativeAd);
                Log.d(TAG, "onNativeAdLoaded: loadAdNativeLanguageAlternate priority");
                adCallback.onNativeAdLoaded(apNativeAd);
            }
        });
    }


    public void loadNativePrioritySameTime(Activity activity, String ID_NATIVE_PRIORITY, String ID_NATIVE_NORMAL, int layout, final AdCallback callback) {
        this.i = false;
        this.k = false;
        this.l = null;
        loadNativeAdResultCallback(activity, ID_NATIVE_PRIORITY, layout, new AdCallback() {


            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError apAdError) {
                super.onAdFailedToLoad(apAdError);

                Log.e(TAG, "onAdFailedToLoad: loadAdNativeLanguageSametime priority - " + apAdError.getMessage());
                if (k) {
                    if (l != null) {
                        callback.onNativeAdLoaded(l);
                        return;
                    } else {
                        callback.onAdFailedToLoad(apAdError);
                        return;
                    }
                }
                i = true;
            }

            @Override
            public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                super.onNativeAdLoaded(apNativeAd);
                Log.d(TAG, "onNativeAdLoaded: loadAdNativeLanguageSametime priority");
                callback.onNativeAdLoaded(apNativeAd);
            }
        });
        loadNativeAdResultCallback(activity, ID_NATIVE_NORMAL, layout, new AdCallback() {
            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError apAdError) {
                super.onAdFailedToLoad(apAdError);

                Log.e(TAG, "onAdFailedToLoad: loadAdNativeLanguageSametime normal - " + apAdError.getMessage());
                if (!i) {
                    k = true;
                    l = null;
                    return;
                }
                callback.onAdFailedToLoad(apAdError);
            }

            @Override
            public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                super.onNativeAdLoaded(apNativeAd);
                Log.d(TAG, "onNativeAdLoaded: loadAdNativeLanguageSametime normal");
                if (!i) {
                    k = true;
                    l = apNativeAd;
                    return;
                }
                callback.onNativeAdLoaded(apNativeAd);
            }
        });
    }


    public void loadNative3Alternate(Activity activity, String ID_NATIVE_PRIORITY, String ID_NATIVE_MEDIUM, String ID_NATIVE_NORMAL, int layout, AdCallback callback) {
        loadNativeAdResultCallback(activity, ID_NATIVE_PRIORITY, layout, new AnonymousClass25(callback, activity, ID_NATIVE_MEDIUM, layout, ID_NATIVE_NORMAL));
    }

    class AnonymousClass25 extends AdCallback {


        AdCallback callback;
        Activity activity;
        String ID_NATIVE_MEDIUM;
        int layout;
        String ID_NATIVE_NORMAL;

        AnonymousClass25(AdCallback adCallback, Activity activity, String ID_NATIVE_MEDIUM, int layout, String ID_NATIVE_NORMAL) {
            this.callback = adCallback;
            this.activity = activity;
            this.ID_NATIVE_MEDIUM = ID_NATIVE_MEDIUM;
            this.layout = layout;
            this.ID_NATIVE_NORMAL = ID_NATIVE_NORMAL;
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();
            this.callback.onAdClicked();
        }

        @Override
        public void onAdFailedToLoad(@Nullable LoadAdError apAdError) {
            super.onAdFailedToLoad(apAdError);

            Log.e(TAG, "onAdFailedToLoad: loadAdNative3Alternate priority - " + apAdError.getMessage());
            loadNativeAdResultCallback(this.activity, this.ID_NATIVE_MEDIUM, this.layout, new AdCallback() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    callback.onAdClicked();
                }

                @Override
                public void onAdFailedToLoad(@Nullable LoadAdError apAdError2) {
                    super.onAdFailedToLoad(apAdError2);

                    Log.e(TAG, "onAdFailedToLoad: loadAdNative3Alternate medium - " + apAdError2.getMessage());

                    loadNativeAdResultCallback(activity, ID_NATIVE_NORMAL, layout, new AdCallback() {
                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            callback.onAdClicked();
                        }


                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError apAdError3) {
                            super.onAdFailedToLoad(apAdError3);
                            Log.e(TAG, "onAdFailedToLoad: loadAdNative3Alternate normal - " + apAdError3.getMessage());
                            onAdFailedToLoad(apAdError3);

                        }

                        @Override
                        public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                            super.onNativeAdLoaded(apNativeAd);
                            Log.d(TAG, "onNativeAdLoaded: loadAdNative3Alternate normal");
                            callback.onNativeAdLoaded(apNativeAd);
                        }
                    });

                }


                @Override
                public void onNativeAdLoaded(ApNativeAd apNativeAd) {
                    super.onNativeAdLoaded(apNativeAd);
                    Log.d(TAG, "onNativeAdLoaded: loadAdNative3Alternate medium");
                    callback.onNativeAdLoaded(apNativeAd);
                }
            });
        }


        @Override
        public void onNativeAdLoaded(ApNativeAd apNativeAd) {
            super.onNativeAdLoaded(apNativeAd);
            Log.d(TAG, "onNativeAdLoaded: loadAdNative3Alternate priority");
            this.callback.onNativeAdLoaded(apNativeAd);
        }
    }


    public void loadNativeAdResultCallback(Activity activity, String nativeId, final int layout, final AdCallback callback) {
        Admob.getInstance().loadNativeAd(activity, nativeId, new AdCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                callback.onAdFailedToLoad(loadAdError);
            }

            @Override // com.ads.control.funtion.AdCallback
            public void onAdFailedToShow(AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                callback.onAdImpression();
            }

            @Override // com.ads.control.funtion.AdCallback
            public void onUnifiedNativeAdLoaded(NativeAd nativeAd) {
                super.onUnifiedNativeAdLoaded(nativeAd);
                callback.onNativeAdLoaded(new ApNativeAd(layout, nativeAd));
            }
        });
    }

    public void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        try {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        try {
            if (nativeAd.getBody() == null) {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getCallToAction() == null) {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getIcon() == null) {
                Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
            } else {
                ((ImageView) adView.getIconView()).setImageDrawable(
                        nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getPrice() == null) {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getStarRating() == null) {
                Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
            } else {
                ((RatingBar) Objects.requireNonNull(adView.getStarRatingView())).setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getStarRating() == null) {
                (adView.findViewById(R.id.ad_rating_text)).setVisibility(View.INVISIBLE);
            } else {
                ((TextView) Objects.requireNonNull((adView.findViewById(R.id.ad_rating_text)))).setText(nativeAd.getStarRating().floatValue() + "");
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getAdvertiser() == null) {
                adView.getAdvertiserView().setVisibility(View.INVISIBLE);
            } else {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        adView.setNativeAd(nativeAd);

    }


    private RewardedAd rewardedAd;

    public void initRewardAds(Context context, String id) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }
        this.nativeId = id;
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }
        RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Admob.this.rewardedAd = rewardedAd;
                Admob.this.rewardedAd.setOnPaidEventListener(adValue -> {
                    SdkLogEventManager.logPaidAdImpression(context,
                            adValue,
                            rewardedAd.getAdUnitId(), Admob.this.rewardedAd.getResponseInfo().getMediationAdapterClassName()
                            , AdType.REWARDED);
                    if (tokenAdjust != null) {
                        SdkLogEventManager.logPaidAdjustWithToken(adValue, rewardedAd.getAdUnitId(), tokenAdjust);
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }
        });
    }

    public void initRewardAds(Context context, String id, AdCallback callback) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }
        this.nativeId = id;
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }
        RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                callback.onRewardAdLoaded(rewardedAd);
                Admob.this.rewardedAd = rewardedAd;
                Admob.this.rewardedAd.setOnPaidEventListener(adValue -> {
                    SdkLogEventManager.logPaidAdImpression(context,
                            adValue,
                            rewardedAd.getAdUnitId(),
                            Admob.this.rewardedAd.getResponseInfo().getMediationAdapterClassName()
                            , AdType.REWARDED);
                    if (tokenAdjust != null) {
                        SdkLogEventManager.logPaidAdjustWithToken(adValue, rewardedAd.getAdUnitId(), tokenAdjust);
                    }
                });

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                callback.onAdFailedToLoad(loadAdError);
                Admob.this.rewardedAd = null;
            }
        });
    }

    public void getRewardInterstitial(Context context, String id, AdCallback callback) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }
        this.nativeId = id;
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }
        RewardedInterstitialAd.load(context, id, getAdRequest(), new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedAd) {
                callback.onRewardAdLoaded(rewardedAd);
                rewardedAd.setOnPaidEventListener(adValue -> {
                    SdkLogEventManager.logPaidAdImpression(context,
                            adValue,
                            rewardedAd.getAdUnitId(),
                            rewardedAd.getResponseInfo().getMediationAdapterClassName()
                            , AdType.REWARDED);
                    if (tokenAdjust != null) {
                        SdkLogEventManager.logPaidAdjustWithToken(adValue, rewardedAd.getAdUnitId(), tokenAdjust);
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                callback.onAdFailedToLoad(loadAdError);
            }
        });
    }

    public RewardedAd getRewardedAd() {

        return rewardedAd;
    }

    public void showRewardAds(final Activity context, final RewardCallback adCallback) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            adCallback.onUserEarnedReward(null);
            return;
        }
        if (rewardedAd == null) {
            initRewardAds(context, nativeId);
            adCallback.onRewardedAdFailedToShow(0);
            return;
        } else {
            Admob.this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    isRewardAdLoaded = false;
                    if (prepareLoadingRewardAdsDialog != null && prepareLoadingRewardAdsDialog.isShowing() && !((Activity) context).isDestroyed())
                        prepareLoadingRewardAdsDialog.dismiss();
                    if (adCallback != null)
                        adCallback.onRewardedAdClosed();

                    AppOpenManager.getInstance().setInterstitialShowing(false);

                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    isRewardAdLoaded = false;
                    if (prepareLoadingRewardAdsDialog != null && prepareLoadingRewardAdsDialog.isShowing() && !((Activity) context).isDestroyed())
                        prepareLoadingRewardAdsDialog.dismiss();
                    if (adCallback != null)
                        adCallback.onRewardedAdFailedToShow(adError.getCode());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    isRewardAdLoaded = true;
                    AppOpenManager.getInstance().setInterstitialShowing(true);
                    rewardedAd = null;
                }

                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    SdkLogEventManager.logClickAdsEvent(context, rewardedAd.getAdUnitId());
                }
            });
            rewardedAd.show(context, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (adCallback != null) {
                        adCallback.onUserEarnedReward(rewardItem);

                    }
                }
            });
        }
    }

    public void showRewardInterstitial(final Activity activity, RewardedInterstitialAd rewardedInterstitialAd, final RewardCallback adCallback) {
        if (AppPurchase.getInstance().isPurchased(activity)) {
            adCallback.onUserEarnedReward(null);
            return;
        }
        if (rewardedInterstitialAd == null) {
            initRewardAds(activity, nativeId);

            adCallback.onRewardedAdFailedToShow(0);
            return;
        } else {
            rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (adCallback != null)
                        adCallback.onRewardedAdClosed();

                    AppOpenManager.getInstance().setInterstitialShowing(false);

                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    if (adCallback != null)
                        adCallback.onRewardedAdFailedToShow(adError.getCode());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();

                    AppOpenManager.getInstance().setInterstitialShowing(true);

                }

                public void onAdClicked() {
                    super.onAdClicked();
                    SdkLogEventManager.logClickAdsEvent(activity, rewardedAd.getAdUnitId());
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                }
            });
            rewardedInterstitialAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (adCallback != null) {
                        adCallback.onUserEarnedReward(rewardItem);
                    }
                }
            });
        }
    }

    public void showRewardAds(final Activity context, RewardedAd rewardedAd, final RewardCallback adCallback) {
        if (AppPurchase.getInstance().isPurchased(context)) {
            adCallback.onUserEarnedReward(null);
            return;
        }
        if (rewardedAd == null) {
            initRewardAds(context, nativeId);

            adCallback.onRewardedAdFailedToShow(0);
            return;
        } else {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (adCallback != null)
                        adCallback.onRewardedAdClosed();


                    AppOpenManager.getInstance().setInterstitialShowing(false);

                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    if (adCallback != null)
                        adCallback.onRewardedAdFailedToShow(adError.getCode());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();

                    AppOpenManager.getInstance().setInterstitialShowing(true);
                    initRewardAds(context, nativeId);
                }

                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    if (adCallback != null) {
                        adCallback.onAdClicked();
                    }
                    SdkLogEventManager.logClickAdsEvent(context, rewardedAd.getAdUnitId());
                }
            });
            rewardedAd.show(context, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (adCallback != null) {
                        adCallback.onUserEarnedReward(rewardItem);

                    }
                }
            });
        }
    }


    public SdkAdAdapter getNativeRepeatAdapter(final Activity activity, String str, int i, int i2, RecyclerView.Adapter adapter, final SdkAdPlacer.Listener listener, int i3, boolean isRepeting) {
        return new SdkAdAdapter(getNativeRepeatAdapters(activity, str, i, i2, adapter, listener, i3,isRepeting));
    }

    public AdmobRecyclerAdapter getNativeRepeatAdapters(Activity activity, String str, int i, int i2, RecyclerView.Adapter adapter, SdkAdPlacer.Listener listener, int i3, boolean isRepeting) {
        SdkAdPlacerSettings aperoAdPlacerSettings = new SdkAdPlacerSettings(i, i2);
        aperoAdPlacerSettings.setAdUnitId(str);
        aperoAdPlacerSettings.setListener(listener);
        if (isRepeting) {
            aperoAdPlacerSettings.setRepeatingInterval(i3);
        } else {
            aperoAdPlacerSettings.setFixedPosition(i3);
        }

        return new AdmobRecyclerAdapter(aperoAdPlacerSettings, adapter, activity);
    }


    @SuppressLint("HardwareIds")
    public String getDeviceId(Activity activity) {
        String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        return md5(android_id).toUpperCase();
    }

    private String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
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
            e.printStackTrace();
        }
        return "";
    }

    PrepareLoadingRewardAdsDialog prepareLoadingRewardAdsDialog;
    boolean isRewardAdLoaded = false;
    boolean isRewardAdClose = false;


    public void initLoadAndShowRewardAds(Context context, String id, RewardCallback callback) {
        isRewardAdClose = false;
        if (AppPurchase.getInstance().isPurchased(context)) {
            return;
        }


        this.nativeId = id;

        try {
            if (prepareLoadingRewardAdsDialog != null && prepareLoadingRewardAdsDialog.isShowing())
                prepareLoadingRewardAdsDialog.dismiss();
            prepareLoadingRewardAdsDialog = new PrepareLoadingRewardAdsDialog((Activity) context);
            try {
                prepareLoadingRewardAdsDialog.show();

                AppOpenManager.getInstance().setInterstitialShowing(true);
            } catch (Exception e) {
                assert callback != null;
                callback.onRewardedAdFailedToShow(0);
                return;
            }
        } catch (Exception e) {
            prepareLoadingRewardAdsDialog = null;
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRewardAdLoaded) {
                    prepareLoadingRewardAdsDialog.iv_close.setVisibility(View.VISIBLE);
                }
            }
        }, 7000);


        prepareLoadingRewardAdsDialog.iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Admob.this.rewardedAd = null;
                isRewardAdLoaded = true;
                prepareLoadingRewardAdsDialog.iv_close.setVisibility(View.GONE);
                if (prepareLoadingRewardAdsDialog != null && prepareLoadingRewardAdsDialog.isShowing() && !((Activity) context).isDestroyed())
                    prepareLoadingRewardAdsDialog.dismiss();
                if (callback != null) {
                    callback.onRewardedAdFailedToShow(10);
                }
            }
        });
        RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
//                callback.onRewardAdLoaded(rewardedAd);
                Log.e("RewardedAd", "onAdLoaded");
//                isRewardAdLoaded = true;
                isRewardAdLoaded = true;
                Admob.this.rewardedAd = rewardedAd;
                Admob.this.rewardedAd.setOnPaidEventListener(adValue -> {
                    SdkLogEventManager.logPaidAdImpression(context,
                            adValue,
                            rewardedAd.getAdUnitId(),
                            Admob.this.rewardedAd.getResponseInfo().getMediationAdapterClassName()
                            , AdType.REWARDED);
                    if (tokenAdjust != null) {
                        SdkLogEventManager.logPaidAdjustWithToken(adValue, rewardedAd.getAdUnitId(), tokenAdjust);
                    }
                });
                showRewardAds((Activity) context, callback);

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                callback.onRewardedAdFailedToShow(loadAdError.getCode());
                Log.e("RewardedAd", "onRewardedAdFailedToShow");
                Admob.this.rewardedAd = null;
                isRewardAdLoaded = false;
                if (prepareLoadingRewardAdsDialog != null && prepareLoadingRewardAdsDialog.isShowing() && !((Activity) context).isDestroyed())
                    prepareLoadingRewardAdsDialog.dismiss();
            }
        });
    }


    public void requestLoadBanner(Activity activity, String adunitId, String str2, AdCallback adCallback, Boolean bool, String str3, BannerSize bannerSize) {
        AdSize adSize = null;

        if (AppPurchase.getInstance().isPurchased(activity)) {
            adCallback.onAdFailedToLoad(new LoadAdError(101, "App isPurchased", "", null, null));
            return;
        }

        try {
            AdView adView = new AdView(activity);
            adView.setAdUnitId(adunitId);
            AdSize a3 = getAdSize(activity, bool, str3);
            int i = AnonymousClass59.f463a[bannerSize.ordinal()];
            if (i == 1) {
                adSize = getAdSize(activity, bool, str3);
            } else if (i == 2) {
                adSize = AdSize.FULL_BANNER;
            } else if (i == 3) {
                adSize = AdSize.LEADERBOARD;
            } else if (i == 4) {
                adSize = AdSize.LARGE_BANNER;
            } else if (i == 5) {
                adSize = AdSize.MEDIUM_RECTANGLE;
            } else {
                adView.setAdSize(a3);
                adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                if (str2 != null && !str2.isEmpty()) {
                    adView.loadAd(adRequestNew(str2));
                }
                adView.setAdListener(new AnonymousClass30(adCallback, adView, adunitId));
                adView.loadAd(getAdRequest());
            }
            a3 = adSize;
            adView.setAdSize(a3);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            if (str2 != null) {
                adView.loadAd(adRequestNew(str2));
            }
            adView.setAdListener(new AnonymousClass30(adCallback, adView, adunitId));
            adView.loadAd(getAdRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AdRequest adRequestNew(String str) {
        AdRequest.Builder builder = new AdRequest.Builder();
        Bundle bundle = new Bundle();
        bundle.putString("collapsible", str);
        builder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);
//        if (this.o) {
//            AdColonyBundleBuilder.setShowPrePopup(true);
//            AdColonyBundleBuilder.setShowPostPopup(true);
//            builder.addNetworkExtrasBundle(AdColonyAdapter.class, AdColonyBundleBuilder.build());
//        }
//        if (this.p) {
//            builder.addNetworkExtrasBundle(ApplovinAdapter.class, new AppLovinExtras.Builder().setMuteAudio(true).build());
//        }
        return builder.build();
    }


    static class AnonymousClass59 {


        static final int[] f463a;

        static {
            int[] iArr = new int[BannerSize.values().length];
            f463a = iArr;
            try {
                iArr[BannerSize.ADAPTIVE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                f463a[BannerSize.FULL_BANNER.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                f463a[BannerSize.LEADERBOARD.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                f463a[BannerSize.LARGE_BANNER.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                f463a[BannerSize.MEDIUM_RECTANGLE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }


//    private AdRequest a(String str) {
//        AdRequest.Builder builder = new AdRequest.Builder();
//        Bundle bundle = new Bundle();
//        bundle.putString("collapsible", str);
//        builder.addNetworkExtrasBundle(AdMobAdapter.class, bundle);
////        if (this.o) {
////            AdColonyBundleBuilder.setShowPrePopup(true);
////            AdColonyBundleBuilder.setShowPostPopup(true);
////            builder.addNetworkExtrasBundle(AdColonyAdapter.class, AdColonyBundleBuilder.build());
////        }
////        if (this.p) {
////            builder.addNetworkExtrasBundle(ApplovinAdapter.class, new AppLovinExtras.Builder().setMuteAudio(true).build());
////        }
//        return builder.build();
//    }


//    private AdSize a(Activity activity, Boolean bool, String str) {
//        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        defaultDisplay.getMetrics(displayMetrics);
//        int i = (int) (displayMetrics.widthPixels / displayMetrics.density);
//        if (bool.booleanValue()) {
//            if (str.equalsIgnoreCase(BANNER_INLINE_LARGE_STYLE)) {
//                return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(activity, i);
//            }
//            return AdSize.getInlineAdaptiveBannerAdSize(i, 50);
//        }
//        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, i);
//    }


    public final static int SPLASH_ADS = 0;
    public final static int RESUME_ADS = 1;
    private final static int BANNER_ADS = 2;
    private final static int INTERS_ADS = 3;
    private final static int REWARD_ADS = 4;
    private final static int NATIVE_ADS = 5;


    private class AnonymousClass30 extends AdListener {
        AdCallback adCallback;
        AdView adView;
        String adunitId;

        public AnonymousClass30(AdCallback adCallback, AdView adView, String adunitId) {
            this.adCallback = adCallback;
            this.adView = adView;
            this.adunitId = adunitId;
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();
            if (disableAdResumeWhenClickAds) {
                AppOpenManager.getInstance().disableAdResumeByClickAction();
            }
            AdCallback adCallback = this.adCallback;
            if (adCallback != null) {
                adCallback.onAdClicked();
                Log.d(TAG, "onAdClicked");
            }
            SdkLogEventManager.logClickAdsEvent(context, this.adunitId);
        }

        @Override
        public void onAdFailedToLoad(LoadAdError loadAdError) {
            this.adCallback.onAdFailedToLoad(loadAdError);
        }

        @Override // com.google.android.gms.ads.AdListener
        public void onAdImpression() {
            super.onAdImpression();
            AdCallback adCallback = this.adCallback;
            if (adCallback != null) {
                adCallback.onAdImpression();
            }
        }

        @Override
        public void onAdLoaded() {
            Log.d(TAG, "Banner adapter class name: " + this.adView.getResponseInfo().getMediationAdapterClassName());
            this.adCallback.onBannerLoaded(this.adView);
            final AdView adView = this.adView;
            adView.setOnPaidEventListener(new OnPaidEventListener() {
                @Override // com.google.android.gms.ads.OnPaidEventListener
                public final void onPaidEvent(AdValue adValue) {
                    Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());
                    SdkLogEventManager.logPaidAdImpression(context, adValue, adView.getAdUnitId(), String.valueOf(adView.getResponseInfo()), AdType.BANNER);
                }
            });
        }
    }
}
