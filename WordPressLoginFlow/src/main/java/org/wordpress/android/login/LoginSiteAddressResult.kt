package org.wordpress.android.login

import org.wordpress.android.fluxc.network.MemorizingTrustManager
import org.wordpress.android.fluxc.store.SiteStore.ConnectSiteInfoPayload
import org.wordpress.android.login.LoginListener.SelfSignedSSLCallback

sealed class LoginSiteAddressResult {
    data class GotConnectedSiteInfo(
        val siteAddress: String,
        val siteAddressAfterRedirects: String?,
        val hasJetpack: Boolean
    ) : LoginSiteAddressResult()

    data class HandleSiteAddressError(
        val siteInfo: ConnectSiteInfoPayload
    ) : LoginSiteAddressResult()

    data class GotWpComSiteInfo(
        val siteAddress: String
    ) : LoginSiteAddressResult()

    data class GotXmlRpcEndpoint(
        val inputSiteAddress: String,
        val endpointSiteAddress: String?
    ) : LoginSiteAddressResult()

    data class AlreadyLoggedInWpCom(
        val oldSitesIds: List<Int>
    ) : LoginSiteAddressResult()

    data class HandleSslCertificateError(
        val memorizingTrustManager: MemorizingTrustManager,
        val selfSignedSSLCallback: SelfSignedSSLCallback
    ) : LoginSiteAddressResult()
}
