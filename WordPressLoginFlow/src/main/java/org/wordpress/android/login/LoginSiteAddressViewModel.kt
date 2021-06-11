package org.wordpress.android.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wordpress.android.login.util.ResourceProvider
import javax.inject.Inject

class LoginSiteAddressViewModel @Inject constructor(
    val analyticsListener: LoginAnalyticsListener,
    val resourceProvider: ResourceProvider,
) : ViewModel() {
    private val siteAddressValidator = LoginSiteAddressValidator()

    val cleanedSiteAddress: String
        get() = siteAddressValidator.cleanedSiteAddress

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

    override fun onCleared() {
        super.onCleared()
        siteAddressValidator.dispose()
    }

    fun setAddress(siteAddress: String) {
        siteAddressValidator.setAddress(siteAddress)
    }

    private fun showError(messageId: Int) {
        val message = resourceProvider.getString(messageId)
        analyticsListener.trackFailure(message)
        _onInputErrorMessage.value = message
    }
}
