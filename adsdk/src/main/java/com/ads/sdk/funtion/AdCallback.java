package com.ads.sdk.funtion;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.sdk.ads.wrapper.ApAdError;
import com.ads.sdk.ads.wrapper.ApInterstitialAd;
import com.ads.sdk.ads.wrapper.ApNativeAd;
import com.ads.sdk.ads.wrapper.ApRewardItem;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;


public class AdCallback {

    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onNormalInterSplashLoaded() {
    }

    public void onAdFailedToLoad(@Nullable LoadAdError i) {
    }

    public void onAdFailedToShow(@Nullable AdError adError) {
    }

    public void onAdFailedToShowHigh(@Nullable AdError adError) {
    }

    public void onAdPriorityFailedToLoad(@Nullable LoadAdError adError) {
    }

    public void onAdFailedToShowMedium(@Nullable AdError adError) {
    }

    public void onAdFailedToShowAll(@Nullable AdError adError) {
    }

    public void onAdLoaded() {
    }

    public void onAdLoadedHigh() {
    }

    public void onAdLoadedAll() {
    }

    public void onAdSplashReady() {
    }

    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {

    }

    public void onInterPriorityMediumLoaded(@Nullable ApInterstitialAd apInterstitialAd) {

    }

    public void onInterPriorityLoaded(@Nullable ApInterstitialAd apInterstitialAd) {

    }

    public void onAppOpenAdMediumLoad(@Nullable AppOpenAd appOpenAd) {

    }

    public void onAppOpenAdHighLoad(@Nullable AppOpenAd appOpenAd) {

    }

    public void onAdPriorityMediumFailedToShow(@Nullable ApAdError adError) {

    }

    public void onAdPriorityMediumFailedToLoad(@Nullable ApAdError adError) {

    }
    public void onAdPriorityFailedToShow(@Nullable ApAdError adError) {

    }

    public void onAdSplashPriorityReady() {

    }

    public void onAdSplashPriorityMediumReady() {

    }


    public void onApInterstitialLoad(@Nullable ApInterstitialAd apInterstitialAd) {

    }


    public void onAdClicked() {
    }

    public void onAdClickedHigh() {
    }

    public void onAdClickedMedium() {
    }

    public void onAdClickedAll() {
    }


    public void onAdImpression() {
    }

    public void onRewardAdLoaded(RewardedAd rewardedAd) {
    }

    public void onUserEarnedReward(ApRewardItem apRewardItem) {
    }

    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
    }


    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {

    }

    public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {

    }

    public void onInterstitialShow() {

    }

    public void onAdLeftApplication() {
    }

    public void onBannerLoaded(ViewGroup viewGroup) {
    }



}
