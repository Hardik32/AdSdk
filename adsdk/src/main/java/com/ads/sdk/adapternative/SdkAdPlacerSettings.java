package com.ads.sdk.adapternative;


public class SdkAdPlacerSettings {
    private String adUnitId;
    private boolean isRepeatingAd;
    private int layoutAdPlaceHolder;
    private int layoutCustomAd;
    private SdkAdPlacer.Listener listener;
    private int positionFixAd;

    public SdkAdPlacerSettings(String str, int i, int i2) {
        this.positionFixAd = -1;
        this.isRepeatingAd = false;
        this.layoutCustomAd = -1;
        this.layoutAdPlaceHolder = -1;
        this.adUnitId = str;
        this.layoutCustomAd = i;
        this.layoutAdPlaceHolder = i2;
    }

    public String getAdUnitId() {
        return this.adUnitId;
    }

    public int getLayoutAdPlaceHolder() {
        return this.layoutAdPlaceHolder;
    }

    public int getLayoutCustomAd() {
        return this.layoutCustomAd;
    }

    public SdkAdPlacer.Listener getListener() {
        return this.listener;
    }

    public int getPositionFixAd() {
        return this.positionFixAd;
    }

    public boolean isRepeatingAd() {
        return this.isRepeatingAd;
    }

    public void setAdUnitId(String str) {
        this.adUnitId = str;
    }

    public void setFixedPosition(int i) {
        this.positionFixAd = i-1;
        this.isRepeatingAd = false;
    }

    public void setLayoutAdPlaceHolder(int i) {
        this.layoutAdPlaceHolder = i;
    }

    public void setLayoutCustomAd(int i) {
        this.layoutCustomAd = i;
    }

    public void setListener(SdkAdPlacer.Listener listener) {
        this.listener = listener;
    }

    public void setRepeatingInterval(int i) {
        this.positionFixAd = i - 1;
        this.isRepeatingAd = true;
    }

    public SdkAdPlacerSettings(int i, int i2) {
        this.positionFixAd = -1;
        this.isRepeatingAd = false;
        this.layoutCustomAd = -1;
        this.layoutAdPlaceHolder = -1;
        this.adUnitId = this.adUnitId;
        this.layoutCustomAd = i;
        this.layoutAdPlaceHolder = i2;
    }
}
