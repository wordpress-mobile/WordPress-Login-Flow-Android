package org.wordpress.android.login

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.MAIN
import org.wordpress.android.fluxc.generated.AuthenticationActionBuilder
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.WORDPRESS_COM_SITE
import org.wordpress.android.fluxc.store.AccountStore.OnDiscoveryResponse
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T.API
import org.wordpress.android.util.AppLog.T.NUX
import org.wordpress.android.util.NetworkUtils

abstract class LoginBaseDiscoveryFragment : LoginBaseFormFragment<LoginListener?>() {
    @JvmField var mLoginBaseDiscoveryListener: LoginBaseDiscoveryListener? = null

    interface LoginBaseDiscoveryListener {
        val requestedSiteAddress: String?
        fun handleWpComDiscoveryError(failedEndpoint: String?)
        fun handleDiscoverySuccess(endpointAddress: String?)
        fun handleDiscoveryError(error: DiscoveryError?, failedEndpoint: String?)
    }

    override fun onDetach() {
        super.onDetach()
        mLoginBaseDiscoveryListener = null
    }

    fun initiateDiscovery() {
        if (mLoginBaseDiscoveryListener == null || !NetworkUtils.checkConnection(activity)) {
            // Fragment was detached or there's no active network connection
            return
        }

        // Start the discovery process
        mDispatcher.dispatch(
                AuthenticationActionBuilder.newDiscoverEndpointAction(mLoginBaseDiscoveryListener?.requestedSiteAddress)
        )
    }

    @Subscribe(threadMode = MAIN)
    fun onDiscoverySucceeded(event: OnDiscoveryResponse) {
        if (mLoginBaseDiscoveryListener == null) {
            // Ignore the event if the fragment is detached
            return
        }
        // bail if user canceled
        if (mLoginBaseDiscoveryListener?.requestedSiteAddress == null) {
            return
        }
        if (!isAdded) {
            return
        }
        if (event.isError) {
            endProgressIfNeeded()
            mAnalyticsListener.trackLoginFailed(event.javaClass.simpleName, event.error.name, event.error.toString())
            AppLog.e(API, "onDiscoveryResponse has error: " + event.error.name + " - " + event.error.toString())
            handleDiscoveryError(event.error, event.failedEndpoint)
            return
        }
        AppLog.i(NUX, "Discovery succeeded, endpoint: " + event.xmlRpcEndpoint)
        mLoginBaseDiscoveryListener?.handleDiscoverySuccess(event.xmlRpcEndpoint)
    }

    private fun handleDiscoveryError(error: DiscoveryError, failedEndpoint: String) {
        mAnalyticsListener.trackFailure(error.name + " - " + failedEndpoint)
        if (error == WORDPRESS_COM_SITE) {
            mLoginBaseDiscoveryListener?.handleWpComDiscoveryError(failedEndpoint)
        } else {
            mLoginBaseDiscoveryListener?.handleDiscoveryError(error, failedEndpoint)
        }
    }
}
