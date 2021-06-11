package org.wordpress.android.login

import org.wordpress.android.fluxc.store.SiteStore.ConnectSiteInfoPayload

sealed class LoginSiteAddressResult {
    data class HandleSiteAddressError(val siteInfo: ConnectSiteInfoPayload) : LoginSiteAddressResult()

    data class GotConnectedSiteInfo(
        val siteAddress: String,
        val siteAddressAfterRedirects: String?,
        val hasJetpack: Boolean
    ) : LoginSiteAddressResult()

    data class GotWpComSiteInfo(val siteAddress: String) : LoginSiteAddressResult()
}
