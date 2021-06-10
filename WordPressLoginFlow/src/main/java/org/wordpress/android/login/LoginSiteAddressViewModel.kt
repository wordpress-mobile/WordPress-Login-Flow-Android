package org.wordpress.android.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class LoginSiteAddressViewModel @Inject constructor() : ViewModel() {
    private val loginSiteAddressValidator = LoginSiteAddressValidator()

    val isSiteAddressValid: LiveData<Boolean> = loginSiteAddressValidator.isValid
    val errorMessageResId: LiveData<Int?> = loginSiteAddressValidator.errorMessageResId

    val cleanedSiteAddress: String
        get() = loginSiteAddressValidator.cleanedSiteAddress

    override fun onCleared() {
        super.onCleared()
        loginSiteAddressValidator.dispose()
    }

    fun setAddress(siteAddress: String) {
        loginSiteAddressValidator.setAddress(siteAddress)
    }
}
