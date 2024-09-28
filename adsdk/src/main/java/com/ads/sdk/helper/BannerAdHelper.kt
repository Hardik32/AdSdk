package com.ads.sdk.helper

import AdsHelper
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.sdk.R
import com.ads.sdk.ads.SdkAd
import com.ads.sdk.funtion.AdCallback
import com.ads.sdk.helper.banner.BannerAdConfig
import com.ads.sdk.helper.banner.DefaultCallback
import com.ads.sdk.helper.banner.param.AdBannerState
import com.ads.sdk.helper.banner.param.AdBannerState.Cancel
import com.ads.sdk.helper.banner.param.AdBannerState.Loading
import com.ads.sdk.helper.banner.param.BannerAdParam
//import com.applovin.mediation.ads.MaxAdView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


class BannerAdHelper(activity: Activity, lifecycleOwner: LifecycleOwner, config: BannerAdConfig) :
    AdsHelper<BannerAdConfig, BannerAdParam>(activity, lifecycleOwner, config) {
    val activity: Activity = activity
    val _adBannerState: MutableStateFlow<AdBannerState>
    private var bannerAdView: ViewGroup? = null
    private var bannerContentView: FrameLayout? = null
    private val impressionOnResume: AtomicBoolean
    private var job: Job? = null
    public val lifecycleOwner: LifecycleOwner = lifecycleOwner
    private val listAdCallback: CopyOnWriteArrayList<AdCallback>
    private val resumeCount: AtomicInteger
    private var shimmerLayoutView: ShimmerFrameLayout? = null
    var timeShowAdImpression: Long = 0


    init {
        val MutableStateFlow: MutableStateFlow<AdBannerState> =
            MutableStateFlow<AdBannerState>(if (canRequestAds()) AdBannerState.None else AdBannerState.Fail)
        this._adBannerState = MutableStateFlow
        this.listAdCallback = CopyOnWriteArrayList<AdCallback>()
        this.resumeCount = AtomicInteger(0)
        this.impressionOnResume = AtomicBoolean(true)
        registerAdListener(defaultCallback)

        observeLifecycleEvents()

        observeLifecycleEvents2()


        observeAdBannerState(_adBannerState)

    }

    fun observeAdBannerState(
        adBannerStateFlow: StateFlow<AdBannerState>
    ) {
        adBannerStateFlow
            .onEach { adBannerState ->
                handleShowAds(adBannerState)
            }
            .launchIn(lifecycleOwner.lifecycleScope) // launches in lifecycle scope of the lifecycle owner
    }


    fun observeLifecycleEvents2() {

        logZ("observeLifecycleEvents2: ${isActiveState()}")

        lifecycleEventState
            .debounce(config.getTimeDebounceResume())
            .onEach { event ->

                logZ("observeLifecycleEvents2 ${event} times")
                if (event == Lifecycle.Event.ON_RESUME) {
                    resumeCount.incrementAndGet()
                    logZ("Resume repeat ${resumeCount.get()} times")

                    logZ("isActiveState: ${isActiveState()}")
                    logZ("getBannerAdView: ${getBannerAdView()}")
                    logZ("canRequestAds: ${canRequestAds()}")
                    logZ("canReloadAd: ${canReloadAd()}")
                    logZ("impressionOnResume: ${impressionOnResume.get()}")

                    if (!isActiveState()) {
                        logInterruptExecute("Request when resume")
                    }

                    if (resumeCount.get() > 1 &&
                        getBannerAdView() != null &&
                        canRequestAds() &&
                        canReloadAd() &&
                        isActiveState() &&
                        impressionOnResume.get()
                    ) {
                        logZ("requestAds on resume")
                        requestAds(BannerAdParam.Reload as BannerAdParam)
                    }

                    if (!impressionOnResume.get()) {
                        impressionOnResume.set(true)
                    }
                } else {
                    logZ("Not ON_RESUME event: $event")
                }
            }
            .launchIn(lifecycleOwner.lifecycleScope)
    }

//    @OptIn(FlowPreview::class)
//    fun observeLifecycleEvents2() {
//        getLifecycleEventState()
//            .debounce(config.getTimeDebounceResume())
//            .onEach { event ->
//                if (event == Lifecycle.Event.ON_RESUME) {
//                    resumeCount.incrementAndGet()
//                    logZ("Resume repeat ${resumeCount.get()} times")
//
//                    if (!isActiveState()) {
//                        logInterruptExecute("Request when resume")
//                    }
//
//                    if (resumeCount.get() > 1 &&
//                        getBannerAdView() != null &&
//                        canRequestAds() &&
//                        canReloadAd() &&
//                        isActiveState() &&
//                        impressionOnResume.get()
//                    ) {
//                        logZ("requestAds on resume")
//                        requestAds(BannerAdParam.Reload as BannerAdParam)
//                    }
//
//                    if (!impressionOnResume.get()) {
//                        impressionOnResume.set(true)
//                    }
//                }else{
//                    logZ("requestAds on resume new")
//                }
//            }
//            .launchIn(lifecycleOwner.lifecycleScope)
//    }

    fun observeLifecycleEvents() {
        lifecycleEventState.onEach { event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    if (!canRequestAds()) {
                        bannerContentView?.visibility = View.GONE
                        shimmerLayoutView?.visibility = View.GONE
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (!canShowAds() && isActiveState()) {
                        cancel()
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        val bannerAdView = getBannerAdView()
//                        if (bannerAdView is MaxAdView && config.getCanReloadAds()) {
//                            (bannerAdView as? MaxAdView)?.stopAutoRefresh()
//                        }
                    } catch (th: Throwable) {
                        // Handle the exception
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    if (config.getRemoteAdWhenStop()) {
                        try {
                            val bannerAdView = getBannerAdView()
                            bannerAdView?.parent?.let { parent ->
                                if (parent is ViewGroup) {
                                    parent.removeView(bannerAdView)
                                }
                            }
                        } catch (th: Throwable) {
                            // Handle the exception
                        }
                    }
                    cancelAutoReload()
                }

                Lifecycle.Event.ON_START -> {
                    if (config.getRemoteAdWhenStop()) {
                        if (canShowAds() && bannerContentView != null) {
                            val bannerAdView = getBannerAdView()
                            if (bannerAdView != null) {
                                showAd(bannerContentView, bannerAdView)
                            }
                        }
                    }
                }

                else -> {
                    // Handle other lifecycle events if needed
                }
            }
        }
            .launchIn(lifecycleOwner.lifecycleScope) // Use lifecycleScope for launching coroutines
    }

//    fun handleShowAds(adBannerState: AdBannerState?) {
//        var frameLayout: FrameLayout? = null
//        var z = true
//        if (!canShowAds() && this.bannerContentView != null) {
//            val collapsibleGravity: String = config.getCollapsibleGravity()
//            if (!(collapsibleGravity == null || collapsibleGravity.length == 0)) {
//                removeBannerCollapseIfNeed(bannerContentView!!)
//            }
//        }
//        if (bannerContentView != null) {
//            bannerContentView?.setVisibility(if ((adBannerState is AdBannerState.Cancel) || !canShowAds()) View.GONE else View.VISIBLE)
//        }
//        if (shimmerLayoutView != null) {
//            if (adBannerState !is AdBannerState.Loading || this.bannerAdView != null) {
//                z = false
//            }
//            shimmerLayoutView?.setVisibility(if (z) View.VISIBLE else View.GONE)
//        }
//        if (adBannerState !is AdBannerState.Loaded || (bannerContentView.also {
//                frameLayout = it
//            }) == null) {
//            return
//        }
//        showAd(frameLayout, (adBannerState as AdBannerState.Loaded).adBanner)
//    }


    fun handleShowAds(adBannerState: AdBannerState?) {
        var frameLayout: FrameLayout? = null
        var shouldShowShimmer = true

        Log.e("handleShowAds", " canShowAds() " + canShowAds())
        Log.e("handleShowAds", " bannerContentView) " + bannerContentView)
        Log.e("handleShowAds", " collapsibleGravity) " + config.getCollapsibleGravity())

        if (!canShowAds() && this.bannerContentView != null) {
//        if (canShowAds() && this.bannerContentView != null) {
            val collapsibleGravity: String = config.getCollapsibleGravity()
            if (collapsibleGravity.isNotEmpty()) {
                removeBannerCollapseIfNeed(bannerContentView!!)
            }
        }

        // Set visibility of bannerContentView based on ad state and visibility condition
//        bannerContentView?.visibility =
//            if ((adBannerState is AdBannerState.Cancel) || !canShowAds()) {
//                View.GONE
//            } else {
//                View.VISIBLE
//            }

        // Show or hide shimmer layout based on loading state and ad view existence
//        shimmerLayoutView?.visibility =
//            if (adBannerState is AdBannerState.Loading && this.bannerAdView == null) {
//                View.VISIBLE
//            } else {
//                View.GONE
//            }

        if (bannerContentView != null) {
            bannerContentView?.setVisibility(if (adBannerState is Cancel || !canShowAds()) View.GONE else View.VISIBLE)
        }

        if (shimmerLayoutView != null) {
            if (adBannerState !is Loading || bannerAdView != null) {
                shouldShowShimmer = false
            }
            shimmerLayoutView?.setVisibility(if (shouldShowShimmer) View.VISIBLE else View.GONE)
        }


        // If the ad is loaded, handle showing it
        if (adBannerState is AdBannerState.Loaded) {
            frameLayout = bannerContentView

            // Check if the adBanner (ViewGroup) has a parent, remove it from the old parent if needed
            val adBanner = adBannerState.adBanner
            val parent = adBanner.parent as? ViewGroup
            parent?.removeView(adBanner) // Safely remove from parent if it has one

            // Add the adBanner to the frameLayout (if it's valid and not null)
            frameLayout?.addView(adBanner)

            // Show the ad
            showAd(frameLayout, adBanner)
        }
    }


    fun invokeAdListener(function1: Function1<AdCallback, Unit>) {
        for (obj in this.listAdCallback) {
            function1.invoke(obj)
        }
    }

    private fun removeBannerCollapseIfNeed(viewGroup: ViewGroup) {
        Log.e("handleShowAds", " removeBannerCollapseIfNeed) " + viewGroup)
        val tempView = viewGroup;
        val collapsibleGravity: String = config.getCollapsibleGravity()
        if (collapsibleGravity == null || collapsibleGravity.length == 0) {
            return
        }
        val childCount: Int = viewGroup.getChildCount()
        for (i in 0 until childCount) {
            val childAt: View = viewGroup.getChildAt(i)
//            if (childAt is AdView && childAt.id == R.id.banner_container) {
            if (childAt is AdView) {
                childAt.destroy()
                childAt.setVisibility(View.GONE)
                viewGroup.removeView(childAt)
                return
            }
        }
    }

    fun showAd(frameLayout: FrameLayout?, viewGroup: ViewGroup?) {
        impressionOnResume.set(lifecycleEventState.value === Lifecycle.Event.ON_RESUME)
        if (frameLayout?.indexOfChild(viewGroup) != -1) {
            logZ("bannerContentView has contains adView")
            return
        }
        frameLayout.setBackgroundColor(-1)
        val view = View(frameLayout.getContext())
        val view2 = View(frameLayout.getContext())
        view.setBackgroundColor(-1973791)
        val height: Int = frameLayout.getHeight()
        removeBannerCollapseIfNeed(frameLayout)
        val dimensionPixelOffset: Int =
            frameLayout.getContext().getResources().getDimensionPixelOffset(R.dimen._1sdp)
        frameLayout.removeAllViews()
        frameLayout.addView(view2, 0, height)
        val parent: ViewParent = viewGroup!!.getParent()
        val viewGroup2: ViewGroup? = if (parent is ViewGroup) parent as ViewGroup else null
        if (viewGroup2 != null) {
            viewGroup2.removeView(viewGroup)
        }
        frameLayout.addView(viewGroup, -1, -2)
        val layoutParams: ViewGroup.LayoutParams = viewGroup.getLayoutParams()
        if (layoutParams != null) {
            val layoutParams2: FrameLayout.LayoutParams = layoutParams as FrameLayout.LayoutParams
            layoutParams2.setMargins(0, dimensionPixelOffset, 0, 0)
            layoutParams2.gravity = 81
            viewGroup.setLayoutParams(layoutParams2)
            frameLayout.addView(view, -1, dimensionPixelOffset)
            updateHeightBannerMax(frameLayout)
            return
        }
        throw NullPointerException("null cannot be cast to non-null type android.widget.FrameLayout.LayoutParams")
    }

    private fun updateHeightBannerMax(viewGroup: ViewGroup) {
//        if (AperoAd.getInstance().getMediationProvider() == 0) {
//            return;
//        }
        val dimensionPixelSize: Int =
            activity.getResources().getDimensionPixelSize(R.dimen.banner_height)
        val layoutParams: ViewGroup.LayoutParams = viewGroup.getLayoutParams()
        layoutParams.width = -1
        layoutParams.height = dimensionPixelSize + 5
        viewGroup.setLayoutParams(layoutParams)
    }

    // com.ads.control.helper.AdsHelper
//    override fun cancel() {
//        logZ("cancel() called")
//        getFlagActive().compareAndSet(true, false)
//        this.bannerAdView = null
//        BuildersKt.launch(
//            lifecycleOwner.getLifecycle(),
//            Dispatchers.getIO(),
//            CoroutineStart.DEFAULT
//        ) { coroutineScope: CoroutineScope?, continuation: Continuation<Unit>? ->
//            val coroutine_suspended: Any = getCOROUTINE_SUSPENDED.getCOROUTINE_SUSPENDED()
//            val adBannerState: MutableStateFlow<AdBannerState> = getAdBannerState()
//            val cancel: Cancel = AdBannerState.Cancel.INSTANCE
//
//            if (adBannerState.emit(cancel, continuation) === coroutine_suspended) {
//                return@launch coroutine_suspended
//            }
//            Unit
//        }
//    }

    override fun cancel() {
        logZ("cancel() called")
        getFlagActive().compareAndSet(true, false)
        bannerAdView = null
        // Launch a coroutine in the IO dispatcher
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val adBannerState = getAdBannerState()
            val cancel = AdBannerState.Cancel
            adBannerState.emit(cancel)
        }
    }


    protected fun cancelAutoReload() {
        logZ("cancelAutoReload")
        try {
            val job = this.job
            if (job != null) {
                job.cancel(null)
            } else {
            }
        } catch (th: Throwable) {
        }
        this.job = null
    }

    fun getAdBannerState(): MutableStateFlow<AdBannerState> {
        return this._adBannerState
    }

    val bannerAdConfig: BannerAdConfig
        get() = this.config

    fun getBannerAdView(): ViewGroup? {
        return this.bannerAdView
    }

    val bannerState: StateFlow<AdBannerState>
        get() = _adBannerState.asStateFlow()

    protected val defaultCallback: AdCallback
        get() = DefaultCallback(this)

    fun invokeListenerAdCallback(): AdCallback {
        return object : AdCallback() {
            // com.ads.control.funtion.AdCallback
            override fun onAdClicked() {
                super.onAdClicked()

                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onAdClicked()
                    Unit
                }
            }

            // com.ads.control.funtion.AdCallback
            override fun onAdClosed() {
                super.onAdClosed()

                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onAdClosed()
                    Unit
                }
            }

            // com.ads.control.funtion.AdCallback
            override fun onAdFailedToLoad(loadAdError: LoadAdError?) {
                super.onAdFailedToLoad(loadAdError)
                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onAdFailedToLoad(loadAdError)
                    Unit
                }
            }

            // com.ads.control.funtion.AdCallback
            override fun onAdFailedToShow(adError: AdError?) {
                super.onAdFailedToShow(adError)
                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onAdFailedToShow(adError)
                    Unit
                }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onAdImpression()
                    Unit
                }
            }

            override fun onAdLeftApplication() {
                super.onAdLeftApplication()
                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onAdLeftApplication()
                    Unit
                }
            }

            // com.ads.control.funtion.AdCallback
            override fun onAdLoaded() {
                super.onAdLoaded()

                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onAdLoaded()
                    Unit
                }
            }

            override fun onAdSplashReady() {
                super.onAdSplashReady()

                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onAdSplashReady()
                    Unit
                }
            }

            override fun onBannerLoaded(viewGroup: ViewGroup?) {
                super.onBannerLoaded(viewGroup)
                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onBannerLoaded(viewGroup)
                    Unit
                }
            }

            override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                super.onInterstitialLoad(interstitialAd)
                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onInterstitialLoad(interstitialAd)
                    Unit
                }
            }

            override fun onInterstitialShow() {
                super.onInterstitialShow()

                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onInterstitialShow()
                    Unit
                }
            }

            override fun onNextAction() {
                super.onNextAction()

                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onNextAction()
                    Unit
                }
            }

            // com.ads.control.funtion.AdCallback
            override fun onRewardAdLoaded(rewardedAd: RewardedAd) {
                super.onRewardAdLoaded(rewardedAd)

                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onRewardAdLoaded(rewardedAd)
                    Unit
                }
            }

            override fun onUnifiedNativeAdLoaded(unifiedNativeAd: NativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd)
                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onUnifiedNativeAdLoaded(unifiedNativeAd)
                    Unit
                }
            }

            override fun onRewardAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                super.onRewardAdLoaded(rewardedInterstitialAd)
                this@BannerAdHelper.invokeAdListener { adCallback: AdCallback ->
                    adCallback.onRewardAdLoaded(rewardedInterstitialAd)
                    Unit
                }
            }
        }
    }

//    fun loadBannerAd() {
//
//        if (canRequestAds()) {
//
//            do {
//            } while (!_adBannerState.compareAndSet(
//                    _adBannerState.value,
//                    AdBannerState.Loading
//                )
//            )
//            SdkAd.getInstance().requestLoadBanner(
//                this.activity,
//                config.getIdAds(), bannerAdConfig.getCollapsibleGravity(),
//                config.getSize(), invokeListenerAdCallback()
//            )
//        }
//    }

    fun loadBannerAd() {
        if (canRequestAds()) {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    val mutableStateFlow: MutableStateFlow<AdBannerState> = _adBannerState
                    do {
                    } while (!mutableStateFlow.compareAndSet(
                            mutableStateFlow.value,
                            AdBannerState.Loading
                        )
                    )
                    SdkAd.getInstance().requestLoadBanner(
                        activity,
                        config.getIdAds(), bannerAdConfig.getCollapsibleGravity(),
                        config.getSize(), invokeListenerAdCallback()
                    )
                }
            }
        }
    }

    fun registerAdListener(adCallback: AdCallback) {
        listAdCallback.add(adCallback)
    }


    //    public final void requestAutoReloadAd() {
    //        if (!adBannerState.getValue().equals(AdBannerState.Loading.INSTANCE) && canReloadAd() && config.getEnableAutoReload()) {
    //            logZ("requestAutoReloadAd setup");
    //
    //            // Cancel the existing job if it's active
    //            if (job != null) {
    //                job.cancel(null);
    //            }
    //
    //            // Launch a new coroutine for auto-reloading the ad
    //            job = LifecycleOwnerKt.getLifecycleScope(lifecycleOwner).launch(null, null, (coroutineScope, continuation) -> {
    //                try {
    //                    reloadAd();
    //                } catch (Exception e) {
    //                    logZ("Error during ad reload: " + e.getMessage());
    //                }
    //                return Unit.INSTANCE;
    //            });
    //
    //            // Start the job if it's not null
    //            if (job != null) {
    //                job.start();
    //            }
    //        }
    //    }
    fun requestAutoReloadAd() {
        if (_adBannerState.value != AdBannerState.Loading && canReloadAd() && config.enableAutoReload) {
            logZ("requestAutoReloadAd setup")
            try {
                job?.cancel()
            } catch (th: Throwable) {
                // Handle any exception if necessary
            }

            job = lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val autoReloadTime = config.autoReloadTime
                delay(autoReloadTime)

                logZ("requestAutoReloadAd")
                requestAds(BannerAdParam.Reload)
            }

            job?.start()
        }
    }


    fun setBannerAdView(viewGroup: ViewGroup?) {
        this.bannerAdView = viewGroup
    }

    fun setBannerContentView(nativeContentView: FrameLayout): BannerAdHelper {
        try {


            if (nativeContentView != null) {
                Log.e("AdsLoadActivity", "Banner container not  null!")
            } else {
                Log.e("AdsLoadActivity", "Banner container is null!")
            }

            this.bannerContentView = nativeContentView
            this.shimmerLayoutView =
                nativeContentView.findViewById<ShimmerFrameLayout>(R.id.shimmer_container_banner)

            val currentState = lifecycleOwner.lifecycle.currentState
            val stateCreated = Lifecycle.State.CREATED
            val stateResumed = Lifecycle.State.RESUMED

            if (currentState >= stateCreated && currentState <= stateResumed) {
                if (!canRequestAds()) {
                    nativeContentView.visibility = View.GONE
                    shimmerLayoutView?.visibility = View.GONE
                }

                bannerAdView?.let { viewGroup ->
                    if (canShowAds()) {
                        showAd(nativeContentView, viewGroup)
                    }
                }
            }
        } catch (th: Throwable) {
            // Handle exceptions
        }
        return this
    }

    fun setShimmerLayoutView(shimmerLayoutView: ShimmerFrameLayout): BannerAdHelper {
        try {
            this.shimmerLayoutView = shimmerLayoutView

            val currentState = lifecycleOwner.lifecycle.currentState
            val stateCreated = Lifecycle.State.CREATED
            val stateResumed = Lifecycle.State.RESUMED

            if (currentState >= stateCreated && currentState <= stateResumed && !canRequestAds()) {
                shimmerLayoutView.visibility = View.GONE
            }
        } catch (th: Throwable) {
            // Handle exceptions
        }
        return this
    }


//    fun setBannerContentView(nativeContentView: FrameLayout): BannerAdHelper {
//        try {
//            this.bannerContentView = nativeContentView
//            this.shimmerLayoutView =
//                nativeContentView.findViewById<View>(R.id.shimmer_container_banner) as ShimmerFrameLayout
//            val state = Lifecycle.State.CREATED
//            val state2 = Lifecycle.State.RESUMED
//            val currentState: Lifecycle.State =
//                lifecycleOwner.getLifecycle().getCurrentState()
//            if (currentState.compareTo(state) >= 0 && currentState.compareTo(state2) <= 0) {
//                if (!canRequestAds()) {
//                    nativeContentView.setVisibility(View.GONE)
//                    val shimmerFrameLayout: ShimmerFrameLayout? = this.shimmerLayoutView
//                    if (shimmerFrameLayout != null) {
//                        shimmerFrameLayout.setVisibility(View.GONE)
//                    }
//                }
//                val viewGroup: ViewGroup? = this.bannerAdView
//                if (canShowAds() && viewGroup != null) {
//                    showAd(nativeContentView, viewGroup)
//                }
//            }
//        } catch (th: Throwable) {
//        }
//        return this
//    }
//
//    fun setShimmerLayoutView(shimmerLayoutView: ShimmerFrameLayout): BannerAdHelper {
//        try {
//            this.shimmerLayoutView = shimmerLayoutView
//            val state = Lifecycle.State.CREATED
//            val state2 = Lifecycle.State.RESUMED
//            val currentState: Lifecycle.State =
//                lifecycleOwner.getLifecycle().getCurrentState()
//            if ((currentState.compareTo(state) >= 0 && currentState.compareTo(state2) <= 0) && !canRequestAds()) {
//                shimmerLayoutView.setVisibility(View.GONE)
//            }
//        } catch (th: Throwable) {
//        }
//        return this
//    }

    fun unregisterAdListener(adCallback: AdCallback) {
        listAdCallback.remove(adCallback)
    }

    fun unregisterAllAdListener() {
        listAdCallback.clear()
    }

    override fun requestAds(param: BannerAdParam) {
        logZ("requestAds with param: ${param::class.simpleName}")
        if (canRequestAds()) {
            lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                when (param) {
                    is BannerAdParam.Request -> {
                        getFlagActive().compareAndSet(false, true)
                        loadBannerAd()
                    }

                    is BannerAdParam.Ready -> {
                        getFlagActive().compareAndSet(false, true)
                        setBannerAdView(param.bannerAds)
                        val adBannerState = getAdBannerState()
                        val loaded = AdBannerState.Loaded(param.bannerAds)
                        adBannerState.emit(loaded)
                    }

                    is BannerAdParam.Clickable -> {
                        if (isActiveState() && canRequestAds() && canReloadAd() &&
                            getAdBannerState().value != AdBannerState.Loading
                        ) {
                            if (timeShowAdImpression + param.minimumTimeKeepAdsDisplay < System.currentTimeMillis()) {
                                loadBannerAd()
                            }
                        } else {
                            logInterruptExecute("requestAds Clickable")
                        }
                    }

                    is BannerAdParam.Reload -> {
                        getFlagActive().compareAndSet(false, true)
                        loadBannerAd()
                    }
                }
            }
        } else if (isOnline() || bannerAdView != null) {
            // Optional: Handle this case
        } else {
            cancel()
        }
    }


}