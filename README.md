# SDK-Studio
- Admob
- Mediation Admob (Facebook, Applovin, Vungle, Pangle, Mintegral)
- Adjust
- Firebase auto log tracking event, tROAS
# Import Module
~~~
    maven { url "https://jitpack.io" }
        maven {
            url 'https://artifact.bytedance.com/repository/pangle/'
        }
        maven {
            url 'https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea'
        }

        maven {
            url = 'https://maven.pkg.github.com/hardik575/QtonzSdk'
            credentials {
                username = "username"
                password = "password"
            }

        }
  
    implementation 'com.github.hardik575:qtonzsdk:$version'
    implementation 'com.google.android.play:core:1.10.3'
    implementation 'com.facebook.shimmer:shimmer:0.5.0'
    implementation 'com.google.android.gms:play-services-ads:21.4.0'
    implementation 'androidx.multidex:multidex:2.0.1'
~~~
# Setup environment with id ads for project
~~~    
      productFlavors {
      appDev {
                manifestPlaceholders = [ad_app_id: "ca-app-pub-3940256099942544~3347511713"]
                buildConfigField "String", "inter", "\"ca-app-pub-3940256099942544/1033173712\""
                buildConfigField "String", "banner", "\"ca-app-pub-3940256099942544/6300978111\""
                buildConfigField "String", "native", "\"ca-app-pub-3940256099942544/2247696110\""
                buildConfigField "String", "open_resume", "\"ca-app-pub-3940256099942544/3419835294\""
                buildConfigField "String", "RewardedAd", "\"ca-app-pub-3940256099942544/5224354917\""
                buildConfigField "Boolean", "build_debug", "true"
           }
       appProd {
            // ADS CONFIG BEGIN (required)
                manifestPlaceholders = [ad_app_id: "ca-app-pub-3940256099942544~3347511713"]
                buildConfigField "String", "inter", "\"ca-app-pub-3940256099942544/1033173712\""
                buildConfigField "String", "banner", "\"ca-app-pub-3940256099942544/6300978111\""
                buildConfigField "String", "native", "\"ca-app-pub-3940256099942544/2247696110\""
                buildConfigField "String", "open_resume", "\"ca-app-pub-3940256099942544/3419835294\""
                buildConfigField "String", "RewardedAd", "\"ca-app-pub-3940256099942544/5224354917\""
                buildConfigField "Boolean", "build_debug", "false"
            // ADS CONFIG END (required)
           }
      }
~~~
**AndroidManifest.xml**
~~~
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="${ad_app_id}" />

        // Config SDK Facebook
        <meta-data
            android:name="com.facebook.sdk.ApplicationId"
            android:value="@string/facebook_app_id" />
        <meta-data
            android:name="com.facebook.sdk.ClientToken"
            android:value="@string/facebook_client_token" />

        <meta-data android:name="com.facebook.sdk.AutoInitEnabled"
            android:value="true"/>
        <meta-data android:name="com.facebook.sdk.AutoLogAppEventsEnabled"
            android:value="true"/>

        <meta-data android:name="com.facebook.sdk.AdvertiserIDCollectionEnabled"
            android:value="true"/>
~~~

# Class Application
~~~
public class App extends AdsMultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        initAds();
        initBilling();
    }

    private void initAds() {
        String environment = BuildConfig.DEBUG ? SdkAdConfig.ENVIRONMENT_DEVELOP : SdkAdConfig.ENVIRONMENT_PRODUCTION;
        mSdkAdConfig = new SdkAdConfig(this, environment);

        AdjustConfig adjustConfig = new AdjustConfig(true, getString(R.string.adjust_token));
        mSdkAdConfig.setAdjustConfig(adjustConfig);
        mSdkAdConfig.setFacebookClientToken(getString(R.string.facebook_client_token));
        mSdkAdConfig.setAdjustTokenTiktok(getString(R.string.tiktok_token));

        mSdkAdConfig.setIdAdResume("");

        SdkAd.getInstance().init(this, mSdkAdConfig);
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        Admob.getInstance().setOpenActivityAfterShowInterAds(true);
        AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity.class);
    }

    private void initBilling() {
        List<String> listIAP = new ArrayList<>();
        listIAP.add("android.test.purchased");
        List<String> listSub = new ArrayList<>();
        AppPurchase.getInstance().initBilling(this, listIAP, listSub);
    }
}

~~~

# GDPR
~~~  

  if (isEnableUMP()) {
            adsConsentManager = new AdsConsentManager(this);
            adsConsentManager.requestUMP(
                    canRequestAds -> runOnUiThread(this::loadSplash));
        } else {
        SdkAd.getInstance().initAdsNetwork();
            loadSplash();
        }

~~~
# Ad Splash Interstitial
~~~   
SdkAd.getInstance().loadSplashInterstitialAds(this, BuildConfig.ad_interstitial_splash, 25000, 5000, new AdCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        });
~~~
# Ad Splash AppOpen
~~~         
 
  // Splash AppOpen load
 
        AppOpenManager.getInstance().loadOpenAppAdSplash(
                this,
                "ca-app-pub-3940256099942544/9257395921",
                timeDelay,
                timeout,
                false,
                new AdCallback() {
                    @Override
                    public void onAdSplashReady() {
                        super.onAdSplashReady();
                        if (isDestroyed() || isFinishing()) return;
                        showAdsOpenAppSplash();
                    }

                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        if (isDestroyed() || isFinishing()) return;
                        SDKApp.getApplication().isAdCloseSplash.postValue(true);
                        navigateToNextScreen();
                    }

                    @Override
                    public void onAdFailedToLoad(@Nullable LoadAdError i) {
                        super.onAdFailedToLoad(i);
                
                        if (isDestroyed() || isFinishing()) return;
                        SDKApp.getApplication().isAdCloseSplash.postValue(true);
                        navigateToNextScreen();
                    }
                }
        );   
        
  // Splash AppOpen Show
  
  
      private void showAdsOpenAppSplash() {
        AppOpenManager.getInstance().showAppOpenSplash(
                this,
                new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        if (isDestroyed() || isFinishing()) return;
                        navigateToNextScreen();
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        if (isDestroyed() || isFinishing()) return;
                        SDKApp.getApplication().isAdCloseSplash.postValue(true);
                        navigateToNextScreen();
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        if (isDestroyed() || isFinishing()) return;
                        SDKApp.getApplication().isAdCloseSplash.postValue(true);
                        navigateToNextScreen();
                    }
                }
        );
    }  
        
~~~   

# Ad Banner
~~~   
SdkAd.getInstance().loadBanner(this, BuildConfig.ad_banner);
~~~   

# Ad Collapsible Banner
~~~   
SdkAd.getInstance().loadCollapsibleBanner(this, BuildConfig.ad_banner, AppConstant.CollapsibleGravity.BOTTOM, new AdCallback());
~~~   

# Native: Load And Show
~~~   
SdkAd.getInstance().loadNativeAd(this, BuildConfig.ad_native, R.layout.native_large, frAds, shimmerAds, new AdCallback() {
            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                frAds.removeAllViews();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                frAds.removeAllViews();
            }
        });
~~~   

# Native: Load
~~~   
private ApNativeAd mApNativeAd;
SdkAd.getInstance().loadNativeAdResultCallback(this, BuildConfig.ad_native, R.layout.native_large, new AdCallback() {
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);

                mApNativeAd = nativeAd;
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);

                mApNativeAd = null;
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);

                mApNativeAd = null;
            }
        });
        
        
        
 // Load priority native and default native ad by sametime:
        SdkAd.getInstance().loadNativePrioritySameTime(
            this,
            ID_NATIVE_PRIORITY,
            ID_NATIVE_NORMAL,
            R.layout.custom_native_admod_medium_rate,
            object : AdCallback() {
              override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                  super.onNativeAdLoaded(nativeAd)
                  //save or show native 
              }
              
              override fun onAdFailedToLoad(adError: ApAdError?) {
                  super.onAdFailedToLoad(adError)   
                  // gone layout ad native 
              }
            }
        )
	    
// Load priority native and default native ad by alternate:
        SdkAd.getInstance().loadNativePriorityAlternate(
            this,
            ID_NATIVE_PRIORITY,
            ID_NATIVE_NORMAL,
            R.layout.custom_native_admod_medium_rate,
            object : AdCallback() {
               override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                  super.onNativeAdLoaded(nativeAd)
                  //save or show native 
               }
              
               override fun onAdFailedToLoad(adError: ApAdError?) {
                  super.onAdFailedToLoad(adError)   
                  // gone layout ad native 
               }
            }
        )
        
// Load priority native, medium native and default native ad by sametime:
        SdkAd.getInstance().loadNative3SameTime(
            this,
            ID_NATIVE_PRIORITY,
            ID_NATIVE_MEDIUM,
            ID_NATIVE_NORMAL,
            R.layout.custom_native_ad,
            object : AdCallback() {
               override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                  super.onNativeAdLoaded(nativeAd)
                  //save or show native 
               }
              
               override fun onAdFailedToLoad(adError: ApAdError?) {
                  super.onAdFailedToLoad(adError)   
                  // gone layout ad native 
              }
            }
        )
        
// Load priority native, medium native and default native ad by alternate:
        SdkAd.getInstance().loadNative3Alternate(
            this,
            ID_NATIVE_PRIORITY,
            ID_NATIVE_MEDIUM,
            ID_NATIVE_NORMAL,
            R.layout.custom_native_ad,
            object : AdCallback() {
               override fun onNativeAdLoaded(nativeAd: ApNativeAd) {
                  super.onNativeAdLoaded(nativeAd)
                  //save or show native 
               }
              
               override fun onAdFailedToLoad(adError: ApAdError?) {
                  super.onAdFailedToLoad(adError)   
                  // gone layout ad native 
               }
            }
        )        
~~~   

# Native: Show
~~~   
if (mApNativeAd != null) {
            SdkAd.getInstance().populateNativeAdView(this, mApNativeAd, frAds, shimmerAds);
        }
        
~~~
# Native: Layout Big
~~~   
<com.google.android.gms.ads.nativead.NativeAdView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <RelativeLayout
        android:id="@+id/ad_unit_content"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_native_ad"
        android:orientation="vertical">

        <LinearLayout
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <LinearLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:paddingStart="25dip"
                android:paddingTop="8dip"
                android:paddingEnd="8dip"
                android:paddingBottom="8dip">

                <ImageView
                    android:id="@+id/ad_app_icon"
                    android:layout_width="35dip"
                    android:layout_height="35dip"
                    android:adjustViewBounds="true" />

                <LinearLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginLeft="5dip"
                    android:orientation="vertical">

                    <TextView
                        android:id="@+id/ad_headline"
                        android:layout_width="fill_parent"
                        android:layout_height="wrap_content"
                        android:ellipsize="end"
                        android:maxLines="2"
                        android:textColor="@color/black"
                        android:textSize="@dimen/_10sdp" />


                    <LinearLayout
                        android:layout_width="fill_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:paddingHorizontal="10dp"
                        android:paddingVertical="5dp">

                        <TextView
                            android:id="@+id/ad_advertiser"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:ellipsize="end"
                            android:lines="2"
                            android:textColor="@color/black"
                            android:textSize="12sp"
                            android:textStyle="bold" />

                        <TextView
                            android:id="@+id/ad_body"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:textColor="@color/black"
                            android:textSize="12sp" />

                    </LinearLayout>


                </LinearLayout>

            </LinearLayout>


            <com.google.android.gms.ads.nativead.MediaView
                android:id="@+id/ad_media"
                android:layout_width="match_parent"
                android:layout_height="120dp"
                android:layout_gravity="center_horizontal"
                android:layout_marginTop="@dimen/_5sdp" />

            <androidx.appcompat.widget.AppCompatButton
                android:id="@+id/ad_call_to_action"
                android:layout_width="match_parent"
                android:layout_height="@dimen/_30sdp"
                android:layout_marginHorizontal="@dimen/_10sdp"
                android:layout_marginTop="@dimen/_5sdp"
                android:layout_marginBottom="@dimen/_10sdp"
                android:background="@drawable/ads_bg_lib"
                android:gravity="center"
                android:text="Install"
                android:textColor="@color/colorWhite"
                android:textSize="@dimen/_12sdp"
                android:textStyle="bold" />
        </LinearLayout>

        <TextView
            style="@style/AppTheme.Ads"
            android:layout_alignParentLeft="true"
            android:layout_alignParentTop="true" />

    </RelativeLayout>

</com.google.android.gms.ads.nativead.NativeAdView>

~~~ 
# Native: Layout small
~~~ 
<com.google.android.gms.ads.nativead.NativeAdView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <LinearLayout
            android:id="@+id/linearLayout3"
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:layout_marginEnd="8dp"
            android:orientation="horizontal"
            android:padding="8dip"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/textView3">

            <ImageView
                android:id="@+id/ad_app_icon"
                android:layout_width="35dip"
                android:layout_height="35dip"
                android:adjustViewBounds="true"
                android:src="@color/colorPrimary" />

            <LinearLayout
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_marginStart="5dip"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/ad_headline"
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:ellipsize="end"
                    android:maxLines="2"
                    android:textColor="@android:color/black"
                    android:textSize="@dimen/_14sdp" />

                <LinearLayout
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/ad_advertiser"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:gravity="bottom"
                        android:lines="1"
                        android:text="@string/bottom_sheet_behavior"
                        android:textColor="@color/colorAds"
                        android:textSize="@dimen/_8sdp"
                        android:textStyle="bold" />

                </LinearLayout>
            </LinearLayout>
        </LinearLayout>

        <TextView
            android:id="@+id/ad_body"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:maxLines="3"
            android:text="@string/appbar_scrolling_view_behavior"
            android:textSize="@dimen/_10sdp"
            app:layout_constraintEnd_toEndOf="@+id/linearLayout3"
            app:layout_constraintStart_toStartOf="@+id/linearLayout3"
            app:layout_constraintTop_toBottomOf="@+id/linearLayout3" />

        <androidx.appcompat.widget.AppCompatButton
            android:id="@+id/ad_call_to_action"
            android:layout_width="0dp"
            android:layout_height="@dimen/_30sdp"
            android:layout_marginBottom="8dp"
            android:background="@drawable/ads_bg_lib"
            android:gravity="center"
            android:text="install"
            android:textColor="@color/colorWhite"
            android:textSize="@dimen/_12sdp"
            android:textStyle="bold"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="@+id/ad_body"
            app:layout_constraintStart_toStartOf="@+id/ad_body"
            app:layout_constraintTop_toBottomOf="@+id/ad_body" />

        <TextView
            android:id="@+id/textView3"
            style="@style/AppTheme.Ads"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />
    </androidx.constraintlayout.widget.ConstraintLayout>


</com.google.android.gms.ads.nativead.NativeAdView>
~~~   

# Reward: Load
~~~
private RewardedAd rewardedAds;
SdkAd.getInstance().initRewardAds(this, BuildConfig.ad_reward, new AdCallback() {
                @Override
                public void onRewardAdLoaded(RewardedAd rewardedAd) {
                    super.onRewardAdLoaded(rewardedAd);
                    rewardedAds = rewardedAd;
                    Toast.makeText(MainActivity.this, "Ads Ready", Toast.LENGTH_SHORT).show();
                }
            });
~~~
# Reward: Show
~~~
private boolean isEarn = false;
btnShowReward.setOnClickListener(v -> {
            isEarn = false;
            SdkAd.getInstance().showRewardAds(MainActivity.this, rewardedAds, new RewardCallback() {
                @Override
                public void onUserEarnedReward(RewardItem var1) {
                    isEarn = true;
                }

                @Override
                public void onRewardedAdClosed() {
                    if (isEarn) {
                        // action intent
                    }
                }

                @Override
                public void onRewardedAdFailedToShow(int codeError) {

                }

                @Override
                public void onAdClicked() {

                }
            });
        });
~~~

# In-App Purchase
~~~
AppPurchase.getInstance().setPurchaseListener(new PurchaseListener() {
            @Override
            public void onProductPurchased(String productId, String transactionDetails) {
                Toast.makeText(MainActivity.this, "onProductPurchased", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void displayErrorMessage(String errorMsg) {
                Toast.makeText(MainActivity.this, "displayErrorMessage", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserCancelBilling() {
                Toast.makeText(MainActivity.this, "onUserCancelBilling", Toast.LENGTH_SHORT).show();
            }
        });

btnIap.setOnClickListener(v -> AppPurchase.getInstance().purchase(MainActivity.this, "android.test.purchased"));
~~~

# Intersial : Pre Load
~~~

   private ApInterstitialAd mInterstitialAd;

        SdkAd.getInstance().getInterstitialAds(MbitActivity.this, InterId, new AdCallback() {
            @Override
            public void onApInterstitialLoad(@Nullable ApInterstitialAd apInterstitialAd) {
                super.onApInterstitialLoad(apInterstitialAd);
                mInterstitialAd = apInterstitialAd;
                Toast.makeText(MbitActivity.this, "Ads Ready", Toast.LENGTH_SHORT).show();
            }
        });

~~~
# Intersial : Pre Load Show
~~~

       SdkAd.getInstance().forceShowInterstitial(MbitActivity.this, mInterstitialAd, new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                      

                    }
                }, true);
                
       ==> true is a reload value         

~~~

# Intersial : Ontime Load And Show
~~~

       SdkAd.getInstance().loadAndShowIntersialAd(MbitActivity.this, InterId, new AdCallback() {
             @Override
             public void onNextAction() {
                 super.onNextAction();
              
             }

             @Override
             public void onAdFailedToShow(@androidx.annotation.Nullable AdError adError) {
                 super.onAdFailedToShow(adError);
            
             }
         });
~~~


























