package com.sdk.module;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;


import com.ads.sdk.adapternative.SdkAdAdapter;
import com.ads.sdk.adapternative.SdkAdPlacer;
import com.ads.sdk.ads.SdkAd;
import com.ads.sdk.ads.wrapper.ApAdValue;

import java.util.ArrayList;
import java.util.List;

public class SimpleListActivity extends AppCompatActivity {
    private static final String TAG = "SimpleListActivity";
    SdkAdAdapter adAdapter;
    int layoutCustomNative = R.layout.native_medium;
    String idNative = "";
    SwipeRefreshLayout swRefresh;
    ListSimpleAdapter originalAdapter;
    RecyclerView recyclerView;
    SdkAdPlacer.Listener listener = new SdkAdPlacer.Listener() {
        @Override
        public void onAdLoaded(int i) {
            Log.i(TAG, "onAdLoaded native list: " + i);
        }

        @Override
        public void onAdRemoved(int i) {
            Log.i(TAG, "onAdRemoved: " + i);
        }

        @Override
        public void onAdClicked() {

        }

        @Override
        public void onAdRevenuePaid(ApAdValue adValue) {

        }

        @Override
        public void onAdImpression() {

        }

    };

    private List<String> sampleData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);
        swRefresh = findViewById(R.id.swRefresh);
        addSampleData();
        // init adapter origin
        originalAdapter = new ListSimpleAdapter(new ListSimpleAdapter.Listener() {
            @Override
            public void onRemoveItem(int pos) {
//                adAdapter.notifyItemRemoved(adAdapter.getOriginalPosition(pos));
                adAdapter.getAdapter().notifyDataSetChanged();
//                setupAdAdapter();
            }
        });
        originalAdapter.setSampleData(sampleData);
        recyclerView = findViewById(R.id.rvListSimple);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        layoutCustomNative = R.layout.native_medium;
        idNative = SDKApp.getApplication().nativeId;


        setupAdAdapter();
        swRefresh.setOnRefreshListener(() -> {
            sampleData.add("Item add new");
            adAdapter.getAdapter().notifyDataSetChanged();
            swRefresh.setRefreshing(false);
        });
    }

    private void setupAdAdapter() {
//        if (!AppPurchase.getInstance().isPurchased()) {
        adAdapter = SdkAd.getInstance().getNativeRepeatAdapter(this, idNative, layoutCustomNative, R.layout.adapter_shimmer_native_medium,
                originalAdapter, listener, 5,true);

        recyclerView.setAdapter(adAdapter.getAdapter());
        adAdapter.loadAds();
//        } else {
//            recyclerView.setAdapter(originalAdapter);
//        }
    }

    private void addSampleData() {
        for (int i = 0; i < 30; i++) {
            sampleData.add("Item " + i);
        }
    }

    @Override
    public void onDestroy() {
        adAdapter.destroy();
        super.onDestroy();
    }
}
