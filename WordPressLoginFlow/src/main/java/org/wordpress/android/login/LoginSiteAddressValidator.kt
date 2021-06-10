package org.wordpress.android.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wordpress.android.login.R.string
import org.wordpress.android.util.helpers.Debouncer
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Encapsulates the site address validation, cleaning, and error reporting of [LoginSiteAddressFragment].
 */
class LoginSiteAddressValidator @JvmOverloads constructor(
    private val debouncer: Debouncer = Debouncer()
) {
    private val _isValid = MutableLiveData(false)
    val isValid: LiveData<Boolean> = _isValid

    private val _errorMessageResId = MutableLiveData<Int?>()
    val errorMessageResId: LiveData<Int?> = _errorMessageResId

    var cleanedSiteAddress = ""
        private set

    fun setAddress(siteAddress: String) {
        cleanedSiteAddress = cleanSiteAddress(siteAddress)
        val isValid = siteAddressIsValid(cleanedSiteAddress)
        _isValid.value = isValid
        _errorMessageResId.value = null

        // Call debounce regardless if there was an error so that the previous Runnable will be cancelled.
        debouncer.debounce(Void::class.java, {
            if (!isValid && cleanedSiteAddress.isNotEmpty()) {
                _errorMessageResId.postValue(string.login_invalid_site_url)
            }
        }, SECONDS_DELAY_BEFORE_SHOWING_ERROR_MESSAGE, SECONDS)
    }

    private fun cleanSiteAddress(siteAddress: String) = siteAddress.trim { it <= ' ' }.replace("[\r\n]".toRegex(), "")

    private fun siteAddressIsValid(cleanedSiteAddress: String) = Patterns.WEB_URL.matcher(cleanedSiteAddress).matches()

    fun dispose() {
        debouncer.shutdown()
    }

    companion object {
        private const val SECONDS_DELAY_BEFORE_SHOWING_ERROR_MESSAGE = 2L
    }
}
