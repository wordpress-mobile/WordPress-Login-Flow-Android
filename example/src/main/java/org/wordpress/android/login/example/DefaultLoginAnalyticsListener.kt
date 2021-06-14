package org.wordpress.android.login.example

import android.util.Log
import org.wordpress.android.login.LoginAnalyticsListener
import org.wordpress.android.login.LoginAnalyticsListener.CreatedAccountSource

class DefaultLoginAnalyticsListener : LoginAnalyticsListener {
    private fun log(name: String) {
        Log.d("LoginAnalyticsListener", name)
    }

    override fun trackAnalyticsSignIn(isWpcomLogin: Boolean) {
        log("trackAnalyticsSignIn")
    }

    override fun trackCreatedAccount(username: String?, email: String?, source: CreatedAccountSource) {
        log("trackCreatedAccount")
    }

    override fun trackEmailFormViewed() {
        log("trackEmailFormViewed")
    }

    override fun trackInsertedInvalidUrl() {
        log("trackInsertedInvalidUrl")
    }

    override fun trackLoginAccessed() {
        log("trackLoginAccessed")
    }

    override fun trackLoginAutofillCredentialsFilled() {
        log("trackLoginAutofillCredentialsFilled")
    }

    override fun trackLoginAutofillCredentialsUpdated() {
        log("trackLoginAutofillCredentialsUpdated")
    }

    override fun trackLoginFailed(errorContext: String?, errorType: String?, errorDescription: String?) {
        log("trackLoginFailed")
    }

    override fun trackLoginForgotPasswordClicked() {
        log("trackLoginForgotPasswordClicked")
    }

    override fun trackLoginMagicLinkExited() {
        log("trackLoginMagicLinkExited")
    }

    override fun trackLoginMagicLinkOpened() {
        log("trackLoginMagicLinkOpened")
    }

    override fun trackLoginMagicLinkOpenEmailClientClicked() {
        log("trackLoginMagicLinkOpenEmailClientClicked")
    }

    override fun trackLoginMagicLinkSucceeded() {
        log("trackLoginMagicLinkOpenEmailClientClicked")
    }

    override fun trackLoginSocial2faNeeded() {
        log("trackLoginMagicLinkOpenEmailClientClicked")
    }

    override fun trackLoginSocialSuccess() {
        log("trackLoginMagicLinkOpenEmailClientClicked")
    }

    override fun trackMagicLinkFailed(properties: Map<String, *>) {
        log("trackMagicLinkFailed")
    }

    override fun trackSignupMagicLinkOpenEmailClientViewed() {
        log("trackSignupMagicLinkOpenEmailClientViewed")
    }

    override fun trackLoginMagicLinkOpenEmailClientViewed() {
        log("trackLoginMagicLinkOpenEmailClientViewed")
    }

    override fun trackMagicLinkRequested() {
        log("trackMagicLinkRequested")
    }

    override fun trackMagicLinkRequestFormViewed() {
        log("trackMagicLinkRequestFormViewed")
    }

    override fun trackPasswordFormViewed(isSocialChallenge: Boolean) {
        log("trackPasswordFormViewed")
    }

    override fun trackSignupCanceled() {
        log("trackSignupCanceled")
    }

    override fun trackSignupEmailButtonTapped() {
        log("trackSignupEmailButtonTapped")
    }

    override fun trackSignupEmailToLogin() {
        log("trackSignupEmailToLogin")
    }

    override fun trackSignupGoogleButtonTapped() {
        log("trackSignupGoogleButtonTapped")
    }

    override fun trackSignupMagicLinkFailed() {
        log("trackSignupMagicLinkFailed")
    }

    override fun trackSignupMagicLinkOpened() {
        log("trackSignupMagicLinkOpened")
    }

    override fun trackSignupMagicLinkOpenEmailClientClicked() {
        log("trackSignupMagicLinkOpenEmailClientClicked")
    }

    override fun trackSignupMagicLinkSent() {
        log("trackSignupMagicLinkSent")
    }

    override fun trackSignupMagicLinkSucceeded() {
        log("trackSignupMagicLinkSucceeded")
    }

    override fun trackSignupSocialAccountsNeedConnecting() {
        log("trackSignupSocialAccountsNeedConnecting")
    }

    override fun trackSignupSocialButtonFailure() {
        log("trackSignupSocialButtonFailure")
    }

    override fun trackSignupSocialToLogin() {
        log("trackSignupSocialToLogin")
    }

    override fun trackSignupTermsOfServiceTapped() {
        log("trackSignupTermsOfServiceTapped")
    }

    override fun trackSocialButtonStart() {
        log("trackSocialButtonStart")
    }

    override fun trackSocialAccountsNeedConnecting() {
        log("trackSocialAccountsNeedConnecting")
    }

    override fun trackSocialButtonClick() {
        log("trackSocialButtonClick")
    }

    override fun trackSocialButtonFailure() {
        log("trackSocialButtonFailure")
    }

    override fun trackSocialConnectFailure() {
        log("trackSocialConnectFailure")
    }

    override fun trackSocialConnectSuccess() {
        log("trackSocialConnectSuccess")
    }

    override fun trackSocialErrorUnknownUser() {
        log("trackSocialErrorUnknownUser")
    }

    override fun trackSocialFailure(errorContext: String?, errorType: String?, errorDescription: String?) {
        log("trackSocialFailure")
    }

    override fun trackTwoFactorFormViewed() {
        log("trackTwoFactorFormViewed")
    }

    override fun trackUrlFormViewed() {
        log("trackUrlFormViewed")
    }

    override fun trackUrlHelpScreenViewed() {
        log("trackUrlHelpScreenViewed")
    }

    override fun trackUsernamePasswordFormViewed() {
        log("trackUsernamePasswordFormViewed")
    }

    override fun trackWpComBackgroundServiceUpdate(properties: Map<String, *>) {
        log("trackWpComBackgroundServiceUpdate")
    }

    override fun trackConnectedSiteInfoRequested(url: String?) {
        log("trackConnectedSiteInfoRequested")
    }

    override fun trackConnectedSiteInfoFailed(
        url: String?,
        errorContext: String?,
        errorType: String?,
        errorDescription: String?
    ) {
        log("trackConnectedSiteInfoFailed")
    }

    override fun trackConnectedSiteInfoSucceeded(properties: Map<String, *>) {
        log("trackConnectedSiteInfoSucceeded")
    }

    override fun trackFailure(message: String?) {
        log("trackFailure")
    }

    override fun trackSendCodeWithTextClicked() {
        log("trackSendCodeWithTextClicked")
    }

    override fun trackSubmit2faCodeClicked() {
        log("trackSubmit2faCodeClicked")
    }

    override fun trackSubmitClicked() {
        log("trackSubmitClicked")
    }

    override fun trackRequestMagicLinkClick() {
        log("trackRequestMagicLinkClick")
    }

    override fun trackLoginWithPasswordClick() {
        log("trackLoginWithPasswordClick")
    }

    override fun trackShowHelpClick() {
        log("trackShowHelpClick")
    }

    override fun trackDismissDialog() {
        log("trackDismissDialog")
    }

    override fun trackSelectEmailField() {
        log("trackSelectEmailField")
    }

    override fun trackPickEmailFromHint() {
        log("trackPickEmailFromHint")
    }

    override fun trackShowEmailHints() {
        log("trackShowEmailHints")
    }

    override fun emailFormScreenResumed() {
        log("emailFormScreenResumed")
    }

    override fun trackSocialSignupConfirmationViewed() {
        log("trackSocialSignupConfirmationViewed")
    }

    override fun trackCreateAccountClick() {
        log("trackCreateAccountClick")
    }

    override fun emailPasswordFormScreenResumed() {
        log("emailPasswordFormScreenResumed")
    }

    override fun siteAddressFormScreenResumed() {
        log("siteAddressFormScreenResumed")
    }

    override fun magicLinkRequestScreenResumed() {
        log("magicLinkRequestScreenResumed")
    }

    override fun magicLinkSentScreenResumed() {
        log("magicLinkSentScreenResumed")
    }

    override fun usernamePasswordScreenResumed() {
        log("usernamePasswordScreenResumed")
    }
}
