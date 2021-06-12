package org.wordpress.android.login

sealed class LoginSiteAddressNavigation {
    data class ShowHttpAuthDialog(
        val url: String
    ) : LoginSiteAddressNavigation()
}
