package com.ads.sdk.adapternative;

import androidx.recyclerview.widget.RecyclerView;

//import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;

public class SdkAdAdapter {
    private AdmobRecyclerAdapter admobRecyclerAdapter;
//    private MaxRecyclerAdapter maxRecyclerAdapter;

    public SdkAdAdapter(AdmobRecyclerAdapter admobRecyclerAdapter) {
        this.admobRecyclerAdapter = admobRecyclerAdapter;
    }

    public void destroy() {
//        MaxRecyclerAdapter maxRecyclerAdapter = this.maxRecyclerAdapter;
//        if (maxRecyclerAdapter != null) {
//            maxRecyclerAdapter.destroy();
//        }
    }

    public RecyclerView.Adapter getAdapter() {
        AdmobRecyclerAdapter admobRecyclerAdapter = this.admobRecyclerAdapter;
//        return admobRecyclerAdapter != null ? admobRecyclerAdapter : this.maxRecyclerAdapter;
        return admobRecyclerAdapter;
    }

    public int getOriginalPosition(int i) {
//        MaxRecyclerAdapter maxRecyclerAdapter = this.maxRecyclerAdapter;
//        if (maxRecyclerAdapter != null) {
//            return maxRecyclerAdapter.getOriginalPosition(i);
//        }
        AdmobRecyclerAdapter admobRecyclerAdapter = this.admobRecyclerAdapter;
        if (admobRecyclerAdapter != null) {
            return admobRecyclerAdapter.getOriginalPosition(i);
        }
        return 0;
    }

    public void loadAds() {
//        MaxRecyclerAdapter maxRecyclerAdapter = this.maxRecyclerAdapter;
//        if (maxRecyclerAdapter != null) {
//            maxRecyclerAdapter.loadAds();
//        }
    }

    public void notifyItemRemoved(int i) {
//        MaxRecyclerAdapter maxRecyclerAdapter = this.maxRecyclerAdapter;
//        if (maxRecyclerAdapter != null) {
//            maxRecyclerAdapter.notifyItemRemoved(i);
//        }
    }

    public void setCanRecyclable(boolean z) {
        AdmobRecyclerAdapter admobRecyclerAdapter = this.admobRecyclerAdapter;
        if (admobRecyclerAdapter != null) {
            admobRecyclerAdapter.setCanRecyclable(z);
        }
    }

    public void setNativeFullScreen(boolean z) {
        AdmobRecyclerAdapter admobRecyclerAdapter = this.admobRecyclerAdapter;
        if (admobRecyclerAdapter != null) {
            admobRecyclerAdapter.setNativeFullScreen(z);
        }
    }

//    public SdkAdAdapter(MaxRecyclerAdapter maxRecyclerAdapter) {
//        this.maxRecyclerAdapter = maxRecyclerAdapter;
//    }
}