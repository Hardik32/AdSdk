package com.sdk.module;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.ads.sdk.ads.SdkAd;
import com.facebook.shimmer.ShimmerFrameLayout;

public class LangugeActivity extends AppCompatActivity {

    FrameLayout layoutAdNative;
    ShimmerFrameLayout shimmerContainerNative;
    AppCompatButton btnLoadSecondAd;
    AppCompatButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languge);

        layoutAdNative = findViewById(R.id.layoutAdNative);
        shimmerContainerNative = findViewById(R.id.shimmerContainerNative);
        btnLoadSecondAd = findViewById(R.id.btnLoadSecondAd);
        btnNext = findViewById(R.id.btnNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LangugeActivity.this, MbitActivity.class);
                startActivity(intent);
                finish();
            }
        });


        btnLoadSecondAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SDKApp.getApplication().nativeAdsLanguage2.getValue() != null) {
                    Log.e("SecondAdLoad", "==>" + "if");
                    SdkAd.getInstance().populateNativeAdView(LangugeActivity.this, SDKApp.getApplication().nativeAdsLanguage2.getValue(), layoutAdNative, shimmerContainerNative);
                } else {
                    Log.e("SecondAdLoad", "==>" + "else");
                }
            }
        });

        if (SDKApp.getApplication().nativeAdsLanguage.getValue() != null) {
            SdkAd.getInstance().populateNativeAdView(this, SDKApp.getApplication().nativeAdsLanguage.getValue(), layoutAdNative, shimmerContainerNative);
        }


    }
}