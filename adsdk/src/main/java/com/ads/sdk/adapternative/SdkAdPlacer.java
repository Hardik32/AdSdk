package com.ads.sdk.adapternative;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ads.sdk.R;
import com.ads.sdk.admob.Admob;
import com.ads.sdk.ads.wrapper.ApAdValue;
import com.ads.sdk.ads.wrapper.ApNativeAd;
import com.ads.sdk.ads.wrapper.StatusAd;
import com.ads.sdk.funtion.AdCallback;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SdkAdPlacer {
    private Activity activity;
    private RecyclerView.Adapter adapterOriginal;
    private SdkAdPlacerSettings settings;
    String TAG = "SdkAdPlacer";
    private HashMap<Integer, ApNativeAd> listAd = new HashMap<>();
    private List<Integer> listPositionAd = new ArrayList();
    private int countLoadAd = 0;
    private boolean isNativeFullScreen = false;


    public interface Listener {
        default void onAdBindHolder(View view, int i) {
        }

        void onAdClicked();

        void onAdImpression();

        default void onAdLoadFail(View view, int i) {
        }

        void onAdLoaded(int i);

        default void onAdPopulate(View view, int i) {
        }

        void onAdRemoved(int i);

        void onAdRevenuePaid(ApAdValue apAdValue);
    }

    public SdkAdPlacer(SdkAdPlacerSettings sdkAdPlacerSettings, RecyclerView.Adapter adapter, Activity activity) {
        this.settings = sdkAdPlacerSettings;
        this.adapterOriginal = adapter;
        this.activity = activity;
        configData();
    }

    static int access$108(SdkAdPlacer sdkAdPlacer) {
        int i = sdkAdPlacer.countLoadAd;
        sdkAdPlacer.countLoadAd = i + 1;
        return i;
    }


    public void populateAdToViewHolder(RecyclerView.ViewHolder viewHolder, NativeAd nativeAd, int i) {
        NativeAdView nativeAdView = (NativeAdView) LayoutInflater.from(this.activity).inflate(this.settings.getLayoutCustomAd(), (ViewGroup) null);
        FrameLayout frameLayout = (FrameLayout) viewHolder.itemView.findViewById(R.id.fl_adplaceholder);
        ShimmerFrameLayout shimmerFrameLayout = (ShimmerFrameLayout) viewHolder.itemView.findViewById(R.id.shimmer_container_native);
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
        Admob.getInstance().populateUnifiedNativeAdView(nativeAd, nativeAdView);
        Log.i(this.TAG, "native ad in recycle loaded position: " + i + "  title: " + nativeAd.getHeadline() + "   count child ads:" + frameLayout.getChildCount());
        frameLayout.removeAllViews();
        frameLayout.addView(nativeAdView);
    }

    public void configData() {
        if (this.settings.isRepeatingAd()) {
            int i = 0;
            for (int itemCount = this.adapterOriginal.getItemCount(); i <= itemCount - this.settings.getPositionFixAd(); itemCount++) {
                int positionFixAd = i + this.settings.getPositionFixAd();
                if (this.listAd.get(Integer.valueOf(positionFixAd)) == null) {
                    this.listAd.put(Integer.valueOf(positionFixAd), new ApNativeAd(StatusAd.AD_INIT));
                    this.listPositionAd.add(Integer.valueOf(positionFixAd));
                }
                i = positionFixAd + 1;
            }
            return;
        }
        this.listPositionAd.add(Integer.valueOf(this.settings.getPositionFixAd()));
        this.listAd.put(Integer.valueOf(this.settings.getPositionFixAd()), new ApNativeAd(StatusAd.AD_INIT));
    }

    public int getAdjustedCount() {
        int i;
        if (this.settings.isRepeatingAd()) {
            i = this.adapterOriginal.getItemCount() / this.settings.getPositionFixAd();
        } else {
            i = this.adapterOriginal.getItemCount() >= this.settings.getPositionFixAd() ? 1 : 0;
        }
        return this.adapterOriginal.getItemCount() + Math.min(i, this.listAd.size());
    }

    public int getOriginalPosition(int i) {
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            if (this.listAd.get(Integer.valueOf(i3)) != null) {
                i2++;
            }
        }
        int i4 = i - i2;
        Log.d(this.TAG, "getOriginalPosition: " + i4);
        return i4;
    }

    public boolean isAdPosition(int i) {
        return this.listAd.get(Integer.valueOf(i)) != null;
    }


    public  void controladsnativeAdsAdPlacer(final int i, final RecyclerView.ViewHolder viewHolder) {
        final ApNativeAd apNativeAd = new ApNativeAd(StatusAd.AD_LOADING);
        this.listAd.put(Integer.valueOf(i), apNativeAd);
        AdCallback adCallback = new AdCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                SdkAdPlacer.this.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                ((ShimmerFrameLayout) viewHolder.itemView.findViewById(R.id.shimmer_container_native)).setVisibility(8);
                SdkAdPlacer.this.onAdLoadFail(viewHolder.itemView, i);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                SdkAdPlacer.this.onAdImpression();
            }

            @Override
            public void onUnifiedNativeAdLoaded(NativeAd nativeAd) {
                super.onUnifiedNativeAdLoaded(nativeAd);
                nativeAd.setOnPaidEventListener(new OnPaidEventListener() {
                    @Override // com.google.android.gms.ads.OnPaidEventListener
                    public void onPaidEvent(AdValue adValue) {
                        SdkAdPlacer.this.onAdRevenuePaid(new ApAdValue(adValue));
                    }
                });
                SdkAdPlacer.this.onAdLoaded(i);
                apNativeAd.setAdmobNativeAd(nativeAd);
                apNativeAd.setStatus(StatusAd.AD_LOADED);
                SdkAdPlacer.this.listAd.put(Integer.valueOf(i), apNativeAd);
                SdkAdPlacer.this.populateAdToViewHolder(viewHolder, nativeAd, i);
                SdkAdPlacer.this.onAdPopulate(viewHolder.itemView, i);
            }
        };
//        if (this.isNativeFullScreen) {
//            Admob.getInstance().loadNativeFullScreen(this.activity, this.settings.getAdUnitId(), adCallback);
//        } else {
            Admob.getInstance().loadNativeAd(this.activity, this.settings.getAdUnitId(), adCallback);
//        }
    }

    public void loadAds() {
        this.countLoadAd = 0;
        Admob.getInstance().loadNativeAds(this.activity, this.settings.getAdUnitId(), new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(NativeAd nativeAd) {
                super.onUnifiedNativeAdLoaded(nativeAd);
                ApNativeAd apNativeAd = new ApNativeAd(SdkAdPlacer.this.settings.getLayoutCustomAd(), nativeAd);
                apNativeAd.setStatus(StatusAd.AD_LOADED);
                SdkAdPlacer.this.listAd.put((Integer) SdkAdPlacer.this.listPositionAd.get(SdkAdPlacer.this.countLoadAd), apNativeAd);
                Log.i(SdkAdPlacer.this.TAG, "native ad in recycle loaded: " + SdkAdPlacer.this.countLoadAd);
                SdkAdPlacer.access$108(SdkAdPlacer.this);
            }
        }, Math.min(this.listAd.size(), this.settings.getPositionFixAd()));
    }

    public void onAdBindHolder(View view, int i) {
        Log.i(this.TAG, "Ad native bind holder ");
        if (this.settings.getListener() != null) {
            this.settings.getListener().onAdBindHolder(view, i);
        }
    }

    public void onAdClicked() {
        Log.i(this.TAG, "Ad native clicked ");
        if (this.settings.getListener() != null) {
            this.settings.getListener().onAdClicked();
        }
    }

    public void onAdImpression() {
        Log.i(this.TAG, "Ad native impression ");
        if (this.settings.getListener() != null) {
            this.settings.getListener().onAdImpression();
        }
    }

    public void onAdLoadFail(View view, int i) {
        Log.i(this.TAG, "Ad native load fail ");
        if (this.settings.getListener() != null) {
            this.settings.getListener().onAdLoadFail(view, i);
        }
    }

    public void onAdLoaded(int i) {
        Log.i(this.TAG, "Ad native loaded in pos: " + i);
        if (this.settings.getListener() != null) {
            this.settings.getListener().onAdLoaded(i);
        }
    }

    public void onAdPopulate(View view, int i) {
        Log.i(this.TAG, "Ad native populate ");
        if (this.settings.getListener() != null) {
            this.settings.getListener().onAdPopulate(view, i);
        }
    }

    public void onAdRemoved(int i) {
        Log.i(this.TAG, "Ad native removed in pos: " + i);
        if (this.settings.getListener() != null) {
            this.settings.getListener().onAdRemoved(i);
        }
    }

    public void onAdRevenuePaid(ApAdValue apAdValue) {
        Log.i(this.TAG, "Ad native revenue paid ");
        if (this.settings.getListener() != null) {
            this.settings.getListener().onAdRevenuePaid(apAdValue);
        }
    }

    public void renderAd(final int i, final RecyclerView.ViewHolder viewHolder) {
        if (this.listAd.get(Integer.valueOf(i)).getAdmobNativeAd() == null) {
            if (this.listAd.get(Integer.valueOf(i)).getStatus() != StatusAd.AD_LOADING) {
                onAdBindHolder(viewHolder.itemView, i);
                viewHolder.itemView.post(new Runnable() {
                    @Override // java.lang.Runnable
                    public final void run() {
                        SdkAdPlacer.this.controladsnativeAdsAdPlacer(i, viewHolder);
                    }
                });
            }
        } else if (this.listAd.get(Integer.valueOf(i)).getStatus() == StatusAd.AD_LOADED) {
            populateAdToViewHolder(viewHolder, this.listAd.get(Integer.valueOf(i)).getAdmobNativeAd(), i);
        }
    }

    public void setNativeFullScreen(boolean z) {
        this.isNativeFullScreen = z;
    }
}
