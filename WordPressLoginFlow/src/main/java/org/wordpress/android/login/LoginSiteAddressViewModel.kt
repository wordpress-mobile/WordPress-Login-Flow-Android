package org.wordpress.android.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.store.SiteStore.ConnectSiteInfoPayload
import org.wordpress.android.fluxc.store.SiteStore.OnConnectSiteInfoChecked
import org.wordpress.android.fluxc.utils.AppLogWrapper
import org.wordpress.android.login.LoginMode.JETPACK_LOGIN_ONLY
import org.wordpress.android.login.LoginMode.SELFHOSTED_ONLY
import org.wordpress.android.login.LoginMode.WOO_LOGIN_MODE
import org.wordpress.android.login.LoginMode.WPCOM_LOGIN_ONLY
import org.wordpress.android.login.LoginSiteAddressResult.GotConnectedSiteInfo
import org.wordpress.android.login.LoginSiteAddressResult.GotWpComSiteInfo
import org.wordpress.android.login.LoginSiteAddressResult.HandleSiteAddressError
import org.wordpress.android.login.actions.FetchSiteInfo
import org.wordpress.android.login.util.Event
import org.wordpress.android.login.util.NetworkUtilsWrapper
import org.wordpress.android.login.util.ResourceProvider
import org.wordpress.android.login.util.UrlUtilsWrapper
import org.wordpress.android.util.AppLog.T.API
import javax.inject.Inject

class LoginSiteAddressViewModel @Inject constructor(
    val analyticsListener: LoginAnalyticsListener,
    val appLog: AppLogWrapper,
    val networkUtils: NetworkUtilsWrapper,
    val urlUtils: UrlUtilsWrapper,
    val resourceProvider: ResourceProvider,
    val fetchSiteInfo: FetchSiteInfo
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

    override fun onCleared() {
        super.onCleared()
        siteAddressValidator.dispose()
        fetchSiteInfo.dispose()
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

    private fun onFetchedConnectSiteInfo(
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
                else -> handleConnectSiteInfoForWordPress(event.info, loginMode)
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

    private fun handleConnectSiteInfoForWordPress(siteInfo: ConnectSiteInfoPayload, loginMode: LoginMode) {
        if (siteInfo.isWPCom) {
            // It's a Simple or Atomic site
            if (loginMode == SELFHOSTED_ONLY) {
                // We're only interested in self-hosted sites
                if (siteInfo.hasJetpack) {
                    // This is an Atomic site, so treat it as self-hosted and start the discovery process
                    initiateDiscovery()
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
                initiateDiscovery()
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

    private fun initiateDiscovery() {
        TODO("Not yet implemented")
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
