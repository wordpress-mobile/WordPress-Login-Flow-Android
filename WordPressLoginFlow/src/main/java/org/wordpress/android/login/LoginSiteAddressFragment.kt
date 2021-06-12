package org.wordpress.android.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import org.wordpress.android.login.LoginMode.SHARE_INTENT
import org.wordpress.android.login.LoginSiteAddressNavigation.ShowHttpAuthDialog
import org.wordpress.android.login.LoginSiteAddressResult.AlreadyLoggedInWpCom
import org.wordpress.android.login.LoginSiteAddressResult.GotConnectedSiteInfo
import org.wordpress.android.login.LoginSiteAddressResult.GotWpComSiteInfo
import org.wordpress.android.login.LoginSiteAddressResult.GotXmlRpcEndpoint
import org.wordpress.android.login.LoginSiteAddressResult.HandleSiteAddressError
import org.wordpress.android.login.LoginSiteAddressResult.HandleSslCertificateError
import org.wordpress.android.login.util.observeEvent
import org.wordpress.android.login.widgets.WPLoginInputRow
import org.wordpress.android.util.ToastUtils
import javax.inject.Inject

class LoginSiteAddressFragment : LoginBaseFormFragment<LoginListener?>() {
    private var mSiteAddressInput: WPLoginInputRow? = null

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: LoginSiteAddressViewModel

    @get:LayoutRes override val contentLayout: Int
        get() = R.layout.login_site_address_screen
    @get:LayoutRes override val progressBarText: Int
        get() = R.string.login_checking_site_address

    // TODO Move to ViewModel
    override fun setupLabel(label: TextView?) {
        if (mLoginListener?.loginMode == SHARE_INTENT) {
            label?.setText(R.string.enter_site_address_share_intent)
        } else {
            label?.setText(R.string.enter_site_address)
        }
    }

    override fun setupContent(rootView: ViewGroup?) {
        // important for accessibility - talkback
        requireActivity().setTitle(R.string.site_address_login_title)

        mSiteAddressInput = rootView?.findViewById<WPLoginInputRow>(R.id.login_site_address_row)?.apply {
            if (BuildConfig.DEBUG) {
                editText?.setText(BuildConfig.DEBUG_WPCOM_WEBSITE_URL)
            }
            editText?.doAfterTextChanged {
                viewModel.setAddress(it?.toString().orEmpty())
            }
            setOnEditorCommitListener { submit() }
        }

        rootView?.findViewById<View>(R.id.login_site_address_help_button)?.setOnClickListener {
            // TODO Move to ViewModel
            mAnalyticsListener.trackShowHelpClick()
            showSiteAddressHelp()
        }
    }

    override fun setupBottomButton(button: Button?) {
        button?.setOnClickListener { submit() }
    }

    override fun buildToolbar(toolbar: Toolbar?, actionBar: ActionBar) {
        // TODO Move to ViewModel
        actionBar.setTitle(R.string.log_in)
    }

    override val editTextToFocusOnStart: EditText?
        get() = mSiteAddressInput?.editText

    override fun onHelp() {
        // TODO Move to ViewModel
        mLoginListener?.helpSiteAddress(null)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginSiteAddressViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onEnableSubmitButton.observe(viewLifecycleOwner) { enable ->
            bottomButton?.isEnabled = enable
        }
        viewModel.onInputErrorMessage.observe(viewLifecycleOwner) { errorMessage ->
            mSiteAddressInput?.setError(errorMessage)
        }
        viewModel.onToastMessage.observeEvent(viewLifecycleOwner) { message ->
            ToastUtils.showToast(requireContext(), message)
        }
        viewModel.onShowProgress.observe(viewLifecycleOwner) { show ->
            if (show) {
                startProgressIfNeeded()
            } else {
                endProgressIfNeeded()
            }
        }
        viewModel.onResult.observeEvent(viewLifecycleOwner) { result ->
            when (result) {
                is GotConnectedSiteInfo -> mLoginListener?.gotConnectedSiteInfo(
                        result.siteAddress,
                        result.siteAddressAfterRedirects,
                        result.hasJetpack
                )
                is HandleSiteAddressError -> mLoginListener?.handleSiteAddressError(result.siteInfo)
                is GotWpComSiteInfo -> mLoginListener?.gotWpcomSiteInfo(result.siteAddress)
                is GotXmlRpcEndpoint -> mLoginListener?.gotXmlRpcEndpoint(
                        result.inputSiteAddress,
                        result.endpointSiteAddress
                )
                is AlreadyLoggedInWpCom -> mLoginListener?.alreadyLoggedInWpcom(ArrayList(result.oldSitesIds))
                is HandleSslCertificateError -> mLoginListener?.handleSslCertificateError(
                        result.memorizingTrustManager,
                        result.selfSignedSSLCallback
                )
            }
        }
        viewModel.onNavigation.observeEvent(viewLifecycleOwner) { event ->
            when (event) {
                is ShowHttpAuthDialog -> askForHttpAuthCredentials(event.url)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            // TODO Move to ViewModel
            mAnalyticsListener.trackUrlFormViewed()
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO Move to ViewModel
        mAnalyticsListener.siteAddressFormScreenResumed()
    }

    override fun onDestroyView() {
        mSiteAddressInput = null
        super.onDestroyView()
    }

    private fun submit() {
        mLoginListener?.loginMode?.let { viewModel.submit(it) }
    }

    private fun askForHttpAuthCredentials(url: String) {
        val loginHttpAuthDialogFragment = LoginHttpAuthDialogFragment.newInstance(url)
        loginHttpAuthDialogFragment.setTargetFragment(this, LoginHttpAuthDialogFragment.DO_HTTP_AUTH)
        loginHttpAuthDialogFragment.show(requireFragmentManager(), LoginHttpAuthDialogFragment.TAG)
    }

    private fun showSiteAddressHelp() {
        LoginSiteAddressHelpDialogFragment().show(requireFragmentManager(), LoginSiteAddressHelpDialogFragment.TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LoginHttpAuthDialogFragment.DO_HTTP_AUTH && resultCode == Activity.RESULT_OK) {
            val loginMode = mLoginListener?.loginMode
            data?.let {
                val url = data.getStringExtra(LoginHttpAuthDialogFragment.ARG_URL)
                val httpUsername = data.getStringExtra(LoginHttpAuthDialogFragment.ARG_USERNAME)
                val httpPassword = data.getStringExtra(LoginHttpAuthDialogFragment.ARG_PASSWORD)
                if (loginMode != null && url != null && httpUsername != null && httpPassword != null) {
                    viewModel.submitHttpCredentials(loginMode, httpUsername, httpPassword, url)
                }
            }
        }
    }

    companion object {
        const val TAG = "login_site_address_fragment_tag"
    }
}
