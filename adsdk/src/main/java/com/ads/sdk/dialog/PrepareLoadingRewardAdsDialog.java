package com.ads.sdk.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.ads.sdk.R;

public class PrepareLoadingRewardAdsDialog extends Dialog {

    public ImageView iv_close;

    public PrepareLoadingRewardAdsDialog(Context context) {
        super(context, R.style.AppTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_reward_loading_ads);

        iv_close = findViewById(R.id.iv_close);
        setCancelable(false);
    }

}
