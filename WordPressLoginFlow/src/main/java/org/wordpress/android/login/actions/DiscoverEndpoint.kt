package org.wordpress.android.login.actions

import kotlinx.coroutines.suspendCancellableCoroutine
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.ASYNC
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.AuthenticationActionBuilder
import org.wordpress.android.fluxc.store.AccountStore.OnDiscoveryResponse
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Wraps discoverEndpoint action into a coroutine
 */
class DiscoverEndpoint @Inject constructor(
    private val dispatcher: Dispatcher
) {
    private var continuation: Continuation<OnDiscoveryResponse>? = null

    init {
        dispatcher.register(this)
    }

    fun dispose() {
        dispatcher.unregister(this)
    }

    suspend fun discoverEndpoint(siteAddress: String) = suspendCancellableCoroutine<OnDiscoveryResponse> {
        continuation = it
        dispatcher.dispatch(AuthenticationActionBuilder.newDiscoverEndpointAction(siteAddress))
    }

    @Subscribe(threadMode = ASYNC)
    fun onDiscoverySucceeded(event: OnDiscoveryResponse) {
        continuation?.resume(event)
    }
}
