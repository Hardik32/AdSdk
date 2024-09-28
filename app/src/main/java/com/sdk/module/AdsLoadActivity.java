package com.sdk.module;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ads.sdk.admob.AppOpenManager;
import com.ads.sdk.ads.SdkAd;
import com.ads.sdk.funtion.AdCallback;
import com.ads.sdk.helper.BannerAdHelper;
import com.ads.sdk.helper.banner.BannerAdConfig;
import com.ads.sdk.helper.banner.param.BannerAdParam;
import com.ads.sdk.util.AppConstant;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;


import org.jetbrains.annotations.Nullable;

import static com.sdk.module.SDKApp.mApNativeAd;

public class AdsLoadActivity extends AppCompatActivity {

    int checkId = 0;

    View banerView;
    FrameLayout fr_ads;
    FrameLayout fr_ads_banner;
    FrameLayout layoutAdNative;
    ShimmerFrameLayout shimmer_native;
    ShimmerFrameLayout shimmerContainerNative;
    TextView txtView;
    Button btnNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_load);


        banerView = findViewById(R.id.banerView);
        btnNext = findViewById(R.id.btnNext);

        fr_ads = findViewById(R.id.fr_ads);
        fr_ads_banner = findViewById(R.id.fr_ads_banner);
        shimmer_native = findViewById(R.id.shimmer_native);
        txtView = findViewById(R.id.txtView);
        shimmerContainerNative = findViewById(R.id.shimmerContainerNative);
        layoutAdNative = findViewById(R.id.layoutAdNative);
        loadBannerAd();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdsLoadActivity.this, LangugeActivity.class);
                startActivity(intent);
            }
        });

        checkId = getIntent().getIntExtra("id", 0);
        if (checkId == 1) {
            banerView.setVisibility(View.VISIBLE);
            fr_ads.setVisibility(View.GONE);
            SdkAd.getInstance().loadBanner(this, SDKApp.getApplication().bannerId);

        } else if (checkId == 2) {
            fr_ads.setVisibility(View.GONE);

            banerView.setVisibility(View.VISIBLE);


            SdkAd.getInstance().loadCollapsibleBanner(this, SDKApp.getApplication().bannerCollapsingId, AppConstant.CollapsibleGravity.BOTTOM, new AdCallback());

        } else if (checkId == 3) {
            banerView.setVisibility(View.GONE);
            fr_ads.setVisibility(View.VISIBLE);

            SdkAd.getInstance().loadNativeAd(this, SDKApp.getApplication().nativeId, R.layout.native_medium, fr_ads, shimmer_native, new AdCallback() {
                @Override
                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                    super.onAdFailedToLoad(i);
                    fr_ads.removeAllViews();
                }

                @Override
                public void onAdFailedToShow(@Nullable AdError adError) {
                    super.onAdFailedToShow(adError);
                    fr_ads.removeAllViews();
                }
            });

        } else if (checkId == 4) {
            banerView.setVisibility(View.GONE);
            fr_ads.setVisibility(View.VISIBLE);

            if (mApNativeAd != null) {
                SdkAd.getInstance().populateNativeAdView(this, mApNativeAd, fr_ads, shimmer_native);
            }
        } else if (checkId == 5) {
            txtView.setVisibility(View.VISIBLE);
            banerView.setVisibility(View.GONE);
            fr_ads.setVisibility(View.GONE);
        } else if (checkId == 6) {
            txtView.setVisibility(View.VISIBLE);
            banerView.setVisibility(View.GONE);
            fr_ads.setVisibility(View.GONE);
        } else if (checkId == 7) {
            txtView.setText("RewardAd Loded");
            txtView.setVisibility(View.VISIBLE);
            banerView.setVisibility(View.GONE);
            fr_ads.setVisibility(View.GONE);
        } else if (checkId == 8) {
            txtView.setText("three tap to Loded ad");
            txtView.setVisibility(View.VISIBLE);
            banerView.setVisibility(View.GONE);
            fr_ads.setVisibility(View.GONE);
        } else if (checkId == 9) {

            banerView.setVisibility(View.GONE);
            fr_ads.setVisibility(View.GONE);
            layoutAdNative.setVisibility(View.VISIBLE);


            SdkAd.getInstance().loadNativeAd(this, SDKApp.getApplication().nativeId, R.layout.native_large, layoutAdNative, shimmerContainerNative, new AdCallback() {
                @Override
                public void onAdFailedToLoad(@Nullable LoadAdError i) {
                    super.onAdFailedToLoad(i);
                    fr_ads.removeAllViews();
                }

                @Override
                public void onAdFailedToShow(@Nullable AdError adError) {
                    super.onAdFailedToShow(adError);
                    fr_ads.removeAllViews();
                }
            });

        }
    }


    private final void loadBannerAd() {
        BannerAdHelper initBannerAd = initBannerAd();

        initBannerAd.setBannerContentView(fr_ads_banner).setTagForDebug("BANNER=>>>");
        initBannerAd.requestAds((BannerAdParam) BannerAdParam.Request.create());
        initBannerAd.registerAdListener(new AdCallback() {
            @Override // com.ads.control.funtion.AdCallback
            public void onBannerLoaded(ViewGroup viewGroup) {
                super.onBannerLoaded(viewGroup);
                Log.w("BANNER=>>>", "onBannerLoaded activity");
            }
        });
    }


    private BannerAdHelper initBannerAd() {
//        String asString = RemoteConfigKt.get(this.remoteConfig, AperoConstantsKt.banner_all_high_KEY).asString();
//        boolean asBoolean = RemoteConfigKt.get(this.remoteConfig, AperoConstantsKt.Banner_all_KEY).asBoolean();
//        if (Intrinsics.areEqual(asString, AperoAd.REQUEST_TYPE.ALTERNATE)) {
//            Log.d("BANNER_Perm", "banner ad 2id loading: ");
//            return new BannerAd2FloorHelper(this, this, new BannerAd2FloorConfig(BuildConfig.Banner_all, BuildConfig.Banner_all_high, asBoolean, true));
//        }
        Log.d("BANNER_Perm", "banner ad 1id loading: ");
        return new BannerAdHelper(this, this, new BannerAdConfig(SDKApp.getApplication().bannerId, true, true));
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppOpenManager.getInstance().enableAppResume();
    }
}