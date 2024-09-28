package com.ads.sdk.adapternative;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;


public class AdmobRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public int TYPE_AD_VIEW = 0;
    private final int TYPE_CONTENT_VIEW = 1;
    private Activity activity;
    private SdkAdPlacer adPlacer;
    private AdapterDataObserver adapterDataObserver;
    private RecyclerView.Adapter adapterOriginal;
    private boolean canRecyclable;
    private boolean isNativeFullScreen;
    private final SdkAdPlacerSettings settings;

    private class AdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            AdmobRecyclerAdapter.this.adPlacer.configData();
            Log.d("AdapterDataObserver", "onChanged: ");
        }

        @Override
        public void onItemRangeChanged(int i, int i2) {
            Log.d("AdapterDataObserver", "onItemRangeChanged: ");
        }

        @Override
        public void onItemRangeInserted(int i, int i2) {
            Log.d("AdapterDataObserver", "onItemRangeInserted: ");
        }

        @Override
        public void onItemRangeMoved(int i, int i2, int i3) {
            Log.d("AdapterDataObserver", "onItemRangeMoved: ");
            AdmobRecyclerAdapter.this.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(int i, int i2) {
            Log.d("AdapterDataObserver", "onItemRangeRemoved: ");
        }

        private AdapterDataObserver() {
        }
    }


    private class SdkViewHolder extends RecyclerView.ViewHolder {
        public SdkViewHolder(View view) {
            super(view);
        }
    }

    public AdmobRecyclerAdapter(SdkAdPlacerSettings sdkAdPlacerSettings, RecyclerView.Adapter adapter, Activity activity) {
        AdapterDataObserver adapterDataObserver = new AdapterDataObserver();
        this.adapterDataObserver = adapterDataObserver;
        this.canRecyclable = false;
        this.isNativeFullScreen = false;
        this.adapterOriginal = adapter;
        registerAdapterDataObserver(adapterDataObserver);
        this.activity = activity;
        this.settings = sdkAdPlacerSettings;
        this.adPlacer = new SdkAdPlacer(sdkAdPlacerSettings, adapter, activity);
    }

    public void destroy() {
        RecyclerView.Adapter adapter = this.adapterOriginal;
        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(this.adapterDataObserver);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.adPlacer.getAdjustedCount();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        if (this.adPlacer.isAdPosition(i)) {
            return this.TYPE_AD_VIEW;
        }
        return 1;
    }

    public int getOriginalPosition(int i) {
        SdkAdPlacer sdkAdPlacer = this.adPlacer;
        if (sdkAdPlacer != null) {
            return sdkAdPlacer.getOriginalPosition(i);
        }
        return 0;
    }

    public void loadAds() {
        this.adPlacer.loadAds();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (this.adPlacer.isAdPosition(i)) {
            this.adPlacer.onAdBindHolder(viewHolder.itemView, i);
            viewHolder.setIsRecyclable(this.canRecyclable);
            this.adPlacer.renderAd(i, viewHolder);
            return;
        }
        this.adapterOriginal.onBindViewHolder(viewHolder, this.adPlacer.getOriginalPosition(i));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == this.TYPE_AD_VIEW) {
            return new SdkViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(this.settings.getLayoutAdPlaceHolder(), viewGroup, false));
        }
        return this.adapterOriginal.onCreateViewHolder(viewGroup, i);
    }

    public void setCanRecyclable(boolean z) {
        this.canRecyclable = z;
    }

    public void setNativeFullScreen(boolean z) {
        this.isNativeFullScreen = z;
        SdkAdPlacer sdkAdPlacer = this.adPlacer;
        if (sdkAdPlacer != null) {
            sdkAdPlacer.setNativeFullScreen(z);
        }
    }
}
