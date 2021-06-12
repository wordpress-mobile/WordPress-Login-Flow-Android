package org.wordpress.android.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.network.HTTPAuthManager
import org.wordpress.android.fluxc.network.MemorizingTrustManager
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.ERRONEOUS_SSL_CERTIFICATE
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.GENERIC_ERROR
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.HTTP_AUTH_REQUIRED
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.INVALID_URL
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.MISSING_XMLRPC_METHOD
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.NO_SITE_ERROR
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.WORDPRESS_COM_SITE
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.XMLRPC_BLOCKED
import org.wordpress.android.fluxc.network.discovery.SelfHostedEndpointFinder.DiscoveryError.XMLRPC_FORBIDDEN
import org.wordpress.android.fluxc.store.AccountStore
import org.wordpress.android.fluxc.store.AccountStore.OnDiscoveryResponse
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.ConnectSiteInfoPayload
import org.wordpress.android.fluxc.store.SiteStore.OnConnectSiteInfoChecked
import org.wordpress.android.login.LoginMode.JETPACK_LOGIN_ONLY
import org.wordpress.android.login.LoginMode.SELFHOSTED_ONLY
import org.wordpress.android.login.LoginMode.WOO_LOGIN_MODE
import org.wordpress.android.login.LoginMode.WPCOM_LOGIN_ONLY
import org.wordpress.android.login.LoginSiteAddressNavigation.ShowHttpAuthDialog
import org.wordpress.android.login.LoginSiteAddressResult.AlreadyLoggedInWpCom
import org.wordpress.android.login.LoginSiteAddressResult.GotConnectedSiteInfo
import org.wordpress.android.login.LoginSiteAddressResult.GotWpComSiteInfo
import org.wordpress.android.login.LoginSiteAddressResult.GotXmlRpcEndpoint
import org.wordpress.android.login.LoginSiteAddressResult.HandleSiteAddressError
import org.wordpress.android.login.LoginSiteAddressResult.HandleSslCertificateError
import org.wordpress.android.login.actions.DiscoverEndpoint
import org.wordpress.android.login.actions.FetchSiteInfo
import org.wordpress.android.login.util.AppLogWrapper
import org.wordpress.android.login.util.Event
import org.wordpress.android.login.util.NetworkUtilsWrapper
import org.wordpress.android.login.util.ResourceProvider
import org.wordpress.android.login.util.SiteUtilsWrapper
import org.wordpress.android.login.util.UrlUtilsWrapper
import org.wordpress.android.util.AppLog.T.API
import org.wordpress.android.util.AppLog.T.NUX
import javax.inject.Inject

class LoginSiteAddressViewModel @Inject constructor(
    val analyticsListener: LoginAnalyticsListener,
    val appLog: AppLogWrapper,
    val networkUtils: NetworkUtilsWrapper,
    val urlUtils: UrlUtilsWrapper,
    val siteUtils: SiteUtilsWrapper,
    val resourceProvider: ResourceProvider,
    val httpAuthManager: HTTPAuthManager,
    val memorizingTrustManager: MemorizingTrustManager,
    val accountStore: AccountStore,
    val siteStore: SiteStore,
    val fetchSiteInfo: FetchSiteInfo,
    val discoverEndpoint: DiscoverEndpoint
) : ViewModel() {
    private val siteAddressValidator = LoginSiteAddressValidator()

    val onEnableSubmitButton: LiveData<Boolean> = siteAddressValidator.isValid

    private val _onInputErrorMessage = MutableLiveData<String?>()
    val onInputErrorMessage = MediatorLiveData<String?>().apply {
        addSource(_onInputErrorMessage) { value = it }
        addSource(siteAddressValidator.errorMessageResId) {
            if (it != null) {
                showError(it)
            } else {
                value = null
            }
        }
    }

    private val _onToastMessage = MutableLiveData<Event<Int>>()
    val onToastMessage: LiveData<Event<Int>> = _onToastMessage

    private val _onShowProgress = MutableLiveData<Boolean>()
    val onShowProgress: LiveData<Boolean> = _onShowProgress

    private val _onResult = MutableLiveData<Event<LoginSiteAddressResult>>()
    val onResult: LiveData<Event<LoginSiteAddressResult>> = _onResult

    private val _onNavigation = MutableLiveData<Event<LoginSiteAddressNavigation>>()
    val onNavigation: LiveData<Event<LoginSiteAddressNavigation>> = _onNavigation

    override fun onCleared() {
        super.onCleared()
        siteAddressValidator.dispose()
        fetchSiteInfo.dispose()
        discoverEndpoint.dispose()
    }

    fun setAddress(siteAddress: String) {
        siteAddressValidator.setAddress(siteAddress)
    }

    private fun showError(messageId: Int) {
        val message = resourceProvider.getString(messageId)
        analyticsListener.trackFailure(message)
        _onInputErrorMessage.postValue(message)
    }

    fun submit(loginMode: LoginMode) {
        if (siteAddressValidator.isValid.value != true) {
            return
        }
        if (!networkUtils.isNetworkAvailable()) {
            _onToastMessage.value = Event(R.string.no_network_message)
            return
        }
        analyticsListener.trackSubmitClicked()
        val requestedSiteAddress = siteAddressValidator.cleanedSiteAddress
        val siteAddressWithoutXmlRpcSuffix = urlUtils.removeXmlRpcSuffix(requestedSiteAddress)
        analyticsListener.trackConnectedSiteInfoRequested(siteAddressWithoutXmlRpcSuffix)
        _onShowProgress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = fetchSiteInfo.fetchSiteInfo(siteAddressWithoutXmlRpcSuffix)
            onFetchedConnectSiteInfo(result, requestedSiteAddress, loginMode)
        }
    }

    private suspend fun onFetchedConnectSiteInfo(
        event: OnConnectSiteInfoChecked,
        requestedSiteAddress: String,
        loginMode: LoginMode
    ) {
        if (event.isError) {
            analyticsListener.trackConnectedSiteInfoFailed(
                    requestedSiteAddress,
                    event.javaClass.simpleName,
                    event.error.type.name,
                    event.error.message
            )
            appLog.e(API, "onFetchedConnectSiteInfo has error: " + event.error.message)
            showError(R.string.invalid_site_url_message)
            _onShowProgress.postValue(false)
        } else {
            val hasJetpack = calculateHasJetpack(event.info)
            analyticsListener.trackConnectedSiteInfoSucceeded(createConnectSiteInfoProperties(event.info, hasJetpack))
            when (loginMode) {
                WOO_LOGIN_MODE -> handleConnectSiteInfoForWoo(event.info, hasJetpack)
                JETPACK_LOGIN_ONLY -> handleConnectSiteInfoForJetpack(event.info)
                else -> handleConnectSiteInfoForWordPress(event.info, loginMode, requestedSiteAddress)
            }
        }
    }

    private fun handleConnectSiteInfoForWoo(siteInfo: ConnectSiteInfoPayload, hasJetpack: Boolean) {
        _onShowProgress.postValue(false)
        if (!siteInfo.exists) {
            // Site does not exist
            showError(R.string.invalid_site_url_message)
        } else if (!siteInfo.isWordPress) {
            // Not a WordPress site
            _onResult.postValue(Event(HandleSiteAddressError(siteInfo)))
        } else {
            _onResult.postValue(
                    Event(
                            GotConnectedSiteInfo(
                                    siteInfo.url,
                                    siteInfo.urlAfterRedirects,
                                    hasJetpack
                            )
                    )
            )
        }
    }

    private suspend fun handleConnectSiteInfoForWordPress(
        siteInfo: ConnectSiteInfoPayload,
        loginMode: LoginMode,
        requestedSiteAddress: String
    ) {
        if (siteInfo.isWPCom) {
            // It's a Simple or Atomic site
            if (loginMode == SELFHOSTED_ONLY) {
                // We're only interested in self-hosted sites
                if (siteInfo.hasJetpack) {
                    // This is an Atomic site, so treat it as self-hosted and start the discovery process
                    initiateDiscovery(requestedSiteAddress, loginMode)
                    return
                }
            }
            _onShowProgress.postValue(false)
            _onResult.postValue(Event(GotWpComSiteInfo(urlUtils.removeScheme(siteInfo.url))))
        } else {
            // It's a Jetpack or self-hosted site
            if (loginMode == WPCOM_LOGIN_ONLY) {
                // We're only interested in WordPress.com accounts
                showError(R.string.enter_wpcom_or_jetpack_site)
                _onShowProgress.postValue(false)
            } else {
                // Start the discovery process
                initiateDiscovery(requestedSiteAddress, loginMode)
            }
        }
    }

    private fun handleConnectSiteInfoForJetpack(siteInfo: ConnectSiteInfoPayload) {
        _onShowProgress.postValue(false)
        if (siteInfo.hasJetpack && siteInfo.isJetpackConnected && siteInfo.isJetpackActive) {
            _onResult.postValue(Event(GotWpComSiteInfo(urlUtils.removeScheme(siteInfo.url))))
        } else {
            _onResult.postValue(Event(HandleSiteAddressError(siteInfo)))
        }
    }

    /**
     * TODO Do we still need this?
     *
     *  Originally, this was necessary for two reasons:
     *  1. Some Jetpack sites could have `isJetpackConnected` set to `true`
     *  while having `hasJetpack` and/or `isJetpackActive` set to `false`.
     *  2. Atomic sites on the other hand could have `hasJetpack` and/or `isJetpackActive`
     *  set to `true` while having `isJetpackConnected` set to `false`.
     *  We need to check if this is still the case and if not, remove this.
     *
     *  Internal reference: p99K0U-1vO-p2#comment-3574
     */
    private fun calculateHasJetpack(siteInfo: ConnectSiteInfoPayload): Boolean {
        var hasJetpack = false
        if (siteInfo.isWPCom && siteInfo.hasJetpack) {
            // This is likely an atomic site.
            hasJetpack = true
        } else if (siteInfo.isJetpackConnected) {
            hasJetpack = true
        }
        return hasJetpack
    }

    private fun createConnectSiteInfoProperties(
        siteInfo: ConnectSiteInfoPayload,
        hasJetpack: Boolean
    ) = mapOf<String, String?>(
            KEY_SITE_INFO_URL to siteInfo.url,
            KEY_SITE_INFO_URL_AFTER_REDIRECTS to siteInfo.urlAfterRedirects,
            KEY_SITE_INFO_EXISTS to siteInfo.exists.toString(),
            KEY_SITE_INFO_HAS_JETPACK to siteInfo.hasJetpack.toString(),
            KEY_SITE_INFO_IS_JETPACK_ACTIVE to siteInfo.isJetpackActive.toString(),
            KEY_SITE_INFO_IS_JETPACK_CONNECTED to siteInfo.isJetpackConnected.toString(),
            KEY_SITE_INFO_IS_WORDPRESS to siteInfo.isWordPress.toString(),
            KEY_SITE_INFO_IS_WPCOM to siteInfo.isWPCom.toString(),
            KEY_SITE_INFO_CALCULATED_HAS_JETPACK to hasJetpack.toString()
    )

    private suspend fun initiateDiscovery(requestedSiteAddress: String, loginMode: LoginMode) {
        if (!networkUtils.isNetworkAvailable()) {
            // There's no active network connection
            return
        }

        // Start the discovery process
        val result = discoverEndpoint.discoverEndpoint(requestedSiteAddress)
        onDiscoverySucceeded(result, requestedSiteAddress, loginMode)
    }

    private fun onDiscoverySucceeded(
        event: OnDiscoveryResponse,
        requestedSiteAddress: String,
        loginMode: LoginMode
    ) {
        if (event.isError) {
            _onShowProgress.postValue(false)
            analyticsListener.trackLoginFailed(event.javaClass.simpleName, event.error.name, event.error.toString())
            appLog.e(API, "onDiscoveryResponse has error: " + event.error.name + " - " + event.error.toString())
            handleDiscoveryError(event.error, event.failedEndpoint, loginMode)
            return
        }
        appLog.i(NUX, "Discovery succeeded, endpoint: " + event.xmlRpcEndpoint)
        handleDiscoverySuccess(event.xmlRpcEndpoint, requestedSiteAddress)
    }

    private fun handleDiscoveryError(
        error: DiscoveryError,
        failedEndpoint: String?,
        loginMode: LoginMode
    ) {
        analyticsListener.trackFailure(error.name + " - " + failedEndpoint)
        when (error) {
            ERRONEOUS_SSL_CERTIFICATE -> handleSslCertificateError(loginMode)
            HTTP_AUTH_REQUIRED -> failedEndpoint?.let { askForHttpAuthCredentials(it) }
            NO_SITE_ERROR -> showError(R.string.no_site_error)
            INVALID_URL -> {
                showError(R.string.invalid_site_url_message)
                analyticsListener.trackInsertedInvalidUrl()
            }
            MISSING_XMLRPC_METHOD -> showError(R.string.xmlrpc_missing_method_error)
            WORDPRESS_COM_SITE -> failedEndpoint?.let { handleWpComDiscoveryError(it) }
            XMLRPC_BLOCKED -> showError(R.string.xmlrpc_post_blocked_error)
            XMLRPC_FORBIDDEN -> showError(R.string.xmlrpc_endpoint_forbidden_error)
            GENERIC_ERROR -> showError(R.string.error_generic)
        }
    }

    private fun handleSslCertificateError(loginMode: LoginMode) {
        val result = HandleSslCertificateError(memorizingTrustManager) {
            // retry site lookup
            submit(loginMode) // TODO Maybe we should call initiateDiscovery directly here?
        }
        _onResult.postValue(Event(result))
    }

    private fun handleWpComDiscoveryError(failedEndpoint: String) {
        appLog.e(API, "Inputted a wpcom address in site address screen.")

        // If the user is already logged in a wordpress.com account, bail out
        if (accountStore.hasAccessToken()) {
            val currentUsername = accountStore.account.userName
            appLog.e(NUX, "User is already logged in WordPress.com: $currentUsername")
            val oldSitesIDs = siteUtils.getCurrentSiteIds(siteStore, true)
            _onResult.postValue(Event(AlreadyLoggedInWpCom(oldSitesIDs)))
        } else {
            _onResult.postValue(Event(GotWpComSiteInfo(failedEndpoint)))
        }
    }

    private fun handleDiscoverySuccess(endpointAddress: String, requestedSiteAddress: String) {
        appLog.i(NUX, "Discovery succeeded, endpoint: $endpointAddress")

        _onShowProgress.postValue(false)
        _onResult.postValue(Event(GotXmlRpcEndpoint(requestedSiteAddress, endpointAddress)))
    }

    private fun askForHttpAuthCredentials(url: String) {
        _onNavigation.postValue(Event(ShowHttpAuthDialog(url)))
    }

    fun submitHttpCredentials(loginMode: LoginMode, httpUsername: String, httpPassword: String, url: String) {
        httpAuthManager.addHTTPAuthCredentials(httpUsername, httpPassword, url, null)
        submit(loginMode) // TODO Maybe we should call initiateDiscovery directly here?
    }

    companion object {
        private const val KEY_SITE_INFO_URL = "url"
        private const val KEY_SITE_INFO_URL_AFTER_REDIRECTS = "url_after_redirects"
        private const val KEY_SITE_INFO_EXISTS = "exists"
        private const val KEY_SITE_INFO_HAS_JETPACK = "has_jetpack"
        private const val KEY_SITE_INFO_IS_JETPACK_ACTIVE = "is_jetpack_active"
        private const val KEY_SITE_INFO_IS_JETPACK_CONNECTED = "is_jetpack_connected"
        private const val KEY_SITE_INFO_IS_WORDPRESS = "is_wordpress"
        private const val KEY_SITE_INFO_IS_WPCOM = "is_wp_com"
        private const val KEY_SITE_INFO_CALCULATED_HAS_JETPACK = "login_calculated_has_jetpack"
    }
}
