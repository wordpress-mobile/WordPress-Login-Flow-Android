package org.wordpress.android.login.util

import org.wordpress.android.util.UrlUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlUtilsWrapper @Inject constructor() {
    fun removeXmlRpcSuffix(siteAddress: String): String = UrlUtils.removeXmlrpcSuffix(siteAddress)
    fun removeScheme(urlString: String?): String = UrlUtils.removeScheme(urlString).orEmpty()
}
