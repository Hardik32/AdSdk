import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ads.sdk.billing.AppPurchase
import com.ads.sdk.helper.IAdsConfig
import com.ads.sdk.helper.banner.param.IAdsParam
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicBoolean


abstract class AdsHelper<C : IAdsConfig, P : IAdsParam>(
    private val context: Activity,
    private val lifecycleOwner: LifecycleOwner,
    val config: C
) {

    private val flagActive = AtomicBoolean(false)
    private var flagUserEnableReload: Boolean = true
    public val lifecycleEventState = MutableStateFlow(Lifecycle.Event.ON_ANY)
    private var tag: String = context.javaClass.simpleName

    init {
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            lifecycleEventState.tryEmit(event)
            if (event == Lifecycle.Event.ON_DESTROY) {
                val observer: LifecycleObserver = MyLifecycleObserver(context)
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        })
    }

    fun canReloadAd(): Boolean {
        return config.canReloadAds && flagUserEnableReload
    }


    internal class MyLifecycleObserver // Constructor takes context
        (private val context: Context) : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            // Use context here
            if (event == Lifecycle.Event.ON_START) {
                Toast.makeText(context, "Lifecycle ON_START", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun canRequestAds(): Boolean {
        return canShowAds() && isOnline()
    }

    fun canShowAds(): Boolean {
        return !AppPurchase.getInstance().isPurchased && config.canShowAds
    }

    abstract fun cancel()

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    fun logInterruptExecute(message: String) {
        logZ("$message not executed because cancel() was called")
    }

    fun logZ(message: String) {
        Log.d(this.javaClass.simpleName, "$tag: $message")
    }

    abstract fun requestAds(param: P)

    fun setFlagUserEnableReload(flag: Boolean) {
        flagUserEnableReload = flag
        logZ("setFlagUserEnableReload($flagUserEnableReload)")
    }

    fun setTagForDebug(tag: String) {
        this.tag = tag
    }

    fun isActiveState(): Boolean {
        return flagActive.get()
    }

    fun getFlagActive(): AtomicBoolean {
        return flagActive
    }


}
