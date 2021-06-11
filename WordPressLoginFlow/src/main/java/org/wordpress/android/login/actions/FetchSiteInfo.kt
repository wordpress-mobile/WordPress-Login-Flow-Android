package org.wordpress.android.login.actions

import kotlinx.coroutines.suspendCancellableCoroutine
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.ASYNC
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.SiteActionBuilder
import org.wordpress.android.fluxc.store.SiteStore.OnConnectSiteInfoChecked
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Wraps fetchConnectSiteInfo action into a coroutine
 */
class FetchSiteInfo @Inject constructor(
    private val dispatcher: Dispatcher
) {
    private var continuation: Continuation<OnConnectSiteInfoChecked>? = null

    init {
        dispatcher.register(this)
    }

    fun dispose() {
        dispatcher.unregister(this)
    }

    suspend fun fetchSiteInfo(siteAddress: String) = suspendCancellableCoroutine<OnConnectSiteInfoChecked> {
        continuation = it
        dispatcher.dispatch(SiteActionBuilder.newFetchConnectSiteInfoAction(siteAddress))
    }

    @Subscribe(threadMode = ASYNC)
    fun onFetchedConnectSiteInfo(event: OnConnectSiteInfoChecked) {
        continuation?.resume(event)
    }
}
