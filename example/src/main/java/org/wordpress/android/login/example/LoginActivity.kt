package org.wordpress.android.login.example

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.android.AndroidInjection
import org.wordpress.android.fluxc.network.MemorizingTrustManager
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.ConnectSiteInfoPayload
import org.wordpress.android.login.AuthOptions
import org.wordpress.android.login.LoginListener
import org.wordpress.android.login.LoginListener.SelfSignedSSLCallback
import org.wordpress.android.login.LoginMode
import org.wordpress.android.login.LoginSiteAddressFragment
import java.util.ArrayList
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), LoginListener {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        setContentView(R.layout.login_activity)
        showFragment(LoginPrologueFragment(), LoginPrologueFragment.TAG)
    }

    private fun showFragment(
        fragment: Fragment,
        tag: String
    ) = supportFragmentManager.beginTransaction().apply {
        replace(R.id.fragment_container, fragment, tag)
        commit()
    }

    private fun slideInFragment(
        fragment: Fragment,
        shouldAddToBackStack: Boolean,
        tag: String
    ) = supportFragmentManager.beginTransaction().apply {
        setCustomAnimations(
                R.anim.activity_slide_in_from_right,
                R.anim.activity_slide_out_to_left,
                R.anim.activity_slide_in_from_left,
                R.anim.activity_slide_out_to_right
        )
        replace(R.id.fragment_container, fragment, tag)
        if (shouldAddToBackStack) addToBackStack(null)
        commitAllowingStateLoss()
    }

    override fun getLoginMode(): LoginMode {
        return LoginMode.FULL
    }

    override fun startOver() {
        TODO("Not yet implemented")
    }

    override fun gotWpcomEmail(email: String?, verifyEmail: Boolean, authOptions: AuthOptions?) {
        TODO("Not yet implemented")
    }

    override fun gotUnregisteredEmail(email: String?) {
        TODO("Not yet implemented")
    }

    override fun gotUnregisteredSocialAccount(
        email: String?,
        displayName: String?,
        idToken: String?,
        photoUrl: String?,
        service: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun loginViaSiteAddress() {
        slideInFragment(LoginSiteAddressFragment(), true, LoginSiteAddressFragment.TAG)
    }

    override fun loginViaSocialAccount(
        email: String?,
        idToken: String?,
        service: String?,
        isPasswordRequired: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun loggedInViaSocialAccount(oldSiteIds: ArrayList<Int>?, doLoginUpdate: Boolean) {
        TODO("Not yet implemented")
    }

    override fun loginViaWpcomUsernameInstead() {
        TODO("Not yet implemented")
    }

    override fun loginViaSiteCredentials(inputSiteAddress: String?) {
        TODO("Not yet implemented")
    }

    override fun helpEmailScreen(email: String?) {
        TODO("Not yet implemented")
    }

    override fun helpSocialEmailScreen(email: String?) {
        TODO("Not yet implemented")
    }

    override fun addGoogleLoginFragment(isSignupFromLoginEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun showHelpFindingConnectedEmail() {
        TODO("Not yet implemented")
    }

    override fun onTermsOfServiceClicked() {
        TODO("Not yet implemented")
    }

    override fun showMagicLinkSentScreen(email: String?, allowPassword: Boolean) {
        TODO("Not yet implemented")
    }

    override fun usePasswordInstead(email: String?) {
        TODO("Not yet implemented")
    }

    override fun helpMagicLinkRequest(email: String?) {
        TODO("Not yet implemented")
    }

    override fun openEmailClient(isLogin: Boolean) {
        TODO("Not yet implemented")
    }

    override fun helpMagicLinkSent(email: String?) {
        TODO("Not yet implemented")
    }

    override fun forgotPassword(url: String?) {
        TODO("Not yet implemented")
    }

    override fun useMagicLinkInstead(email: String?, verifyEmail: Boolean) {
        TODO("Not yet implemented")
    }

    override fun needs2fa(email: String?, password: String?) {
        TODO("Not yet implemented")
    }

    override fun needs2faSocial(
        email: String?,
        userId: String?,
        nonceAuthenticator: String?,
        nonceBackup: String?,
        nonceSms: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun needs2faSocialConnect(email: String?, password: String?, idToken: String?, service: String?) {
        TODO("Not yet implemented")
    }

    override fun loggedInViaPassword(oldSitesIds: ArrayList<Int>?) {
        TODO("Not yet implemented")
    }

    override fun helpEmailPasswordScreen(email: String?) {
        TODO("Not yet implemented")
    }

    override fun alreadyLoggedInWpcom(oldSitesIds: ArrayList<Int>?) {
        TODO("Not yet implemented")
    }

    override fun gotWpcomSiteInfo(siteAddress: String?) {
        TODO("Not yet implemented")
    }

    override fun gotConnectedSiteInfo(siteAddress: String, redirectUrl: String?, hasJetpack: Boolean) {
        TODO("Not yet implemented")
    }

    override fun gotXmlRpcEndpoint(inputSiteAddress: String?, endpointAddress: String?) {
        TODO("Not yet implemented")
    }

    override fun handleSslCertificateError(
        memorizingTrustManager: MemorizingTrustManager?,
        callback: SelfSignedSSLCallback?
    ) {
        TODO("Not yet implemented")
    }

    override fun helpSiteAddress(url: String?) {
        TODO("Not yet implemented")
    }

    override fun helpFindingSiteAddress(username: String?, siteStore: SiteStore?) {
        TODO("Not yet implemented")
    }

    override fun handleSiteAddressError(siteInfo: ConnectSiteInfoPayload?) {
        TODO("Not yet implemented")
    }

    override fun saveCredentialsInSmartLock(
        username: String?,
        password: String?,
        displayName: String,
        profilePicture: Uri?
    ) {
        TODO("Not yet implemented")
    }

    override fun loggedInViaUsernamePassword(oldSitesIds: ArrayList<Int>?) {
        TODO("Not yet implemented")
    }

    override fun helpUsernamePassword(url: String?, username: String?, isWpcom: Boolean) {
        TODO("Not yet implemented")
    }

    override fun helpNoJetpackScreen(
        siteAddress: String?,
        endpointAddress: String?,
        username: String?,
        password: String?,
        userAvatarUrl: String?,
        checkJetpackAvailability: Boolean?
    ) {
        TODO("Not yet implemented")
    }

    override fun helpHandleDiscoveryError(
        siteAddress: String?,
        endpointAddress: String?,
        username: String?,
        password: String?,
        userAvatarUrl: String?,
        errorMessage: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun help2FaScreen(email: String?) {
        TODO("Not yet implemented")
    }

    override fun startPostLoginServices() {
        TODO("Not yet implemented")
    }

    override fun helpSignupEmailScreen(email: String?) {
        TODO("Not yet implemented")
    }

    override fun helpSignupMagicLinkScreen(email: String?) {
        TODO("Not yet implemented")
    }

    override fun helpSignupConfirmationScreen(email: String?) {
        TODO("Not yet implemented")
    }

    override fun showSignupMagicLink(email: String?) {
        TODO("Not yet implemented")
    }

    override fun showSignupSocial(
        email: String?,
        displayName: String?,
        idToken: String?,
        photoUrl: String?,
        service: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun showSignupToLoginMessage() {
        TODO("Not yet implemented")
    }
}
