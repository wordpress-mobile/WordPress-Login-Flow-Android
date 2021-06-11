package org.wordpress.android.login.util

import org.wordpress.android.util.NetworkUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkUtilsWrapper @Inject constructor(
    val contextProvider: ContextProvider
) {
    fun isNetworkAvailable() = NetworkUtils.isNetworkAvailable(contextProvider.getContext())
}
