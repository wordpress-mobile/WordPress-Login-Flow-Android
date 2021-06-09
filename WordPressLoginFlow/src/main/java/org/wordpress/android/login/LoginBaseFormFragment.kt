package org.wordpress.android.login

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.MAIN
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.AccountAction.FETCH_ACCOUNT
import org.wordpress.android.fluxc.action.AccountAction.FETCH_SETTINGS
import org.wordpress.android.fluxc.generated.AccountActionBuilder
import org.wordpress.android.fluxc.generated.SiteActionBuilder
import org.wordpress.android.fluxc.store.AccountStore
import org.wordpress.android.fluxc.store.AccountStore.AccountErrorType.SETTINGS_FETCH_REAUTHORIZATION_REQUIRED_ERROR
import org.wordpress.android.fluxc.store.AccountStore.OnAccountChanged
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.OnSiteChanged
import org.wordpress.android.fluxc.store.SiteStore.SiteErrorType.DUPLICATE_SITE
import org.wordpress.android.login.LoginMode.JETPACK_LOGIN_ONLY
import org.wordpress.android.login.util.SiteUtils
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T.API
import org.wordpress.android.util.EditTextUtils
import org.wordpress.android.util.ToastUtils
import org.wordpress.android.util.ToastUtils.Duration.LONG
import javax.inject.Inject

abstract class LoginBaseFormFragment<LoginListenerType> : Fragment(), TextWatcher {
    protected var bottomButton: Button? = null
    private var mProgressDialog: ProgressDialog? = null
    @JvmField protected var mLoginListener: LoginListenerType? = null
    protected var isInProgress = false
    private var mLoginFinished = false

    @Inject protected lateinit var mDispatcher: Dispatcher
    @Inject protected lateinit var mSiteStore: SiteStore
    @Inject protected lateinit var mAccountStore: AccountStore
    @Inject protected lateinit var mAnalyticsListener: LoginAnalyticsListener

    @get:LayoutRes protected abstract val contentLayout: Int
    protected abstract fun setupLabel(label: TextView?)
    protected abstract fun setupContent(rootView: ViewGroup?)
    protected abstract fun setupBottomButton(button: Button?)
    @get:StringRes protected abstract val progressBarText: Int
    protected open fun listenForLogin(): Boolean {
        return true
    }

    protected open val editTextToFocusOnStart: EditText?
        get() = null

    protected abstract fun onHelp()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    protected open fun createMainView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ViewGroup? {
        val rootView = inflater.inflate(R.layout.login_form_screen, container, false) as ViewGroup?
        val formContainer = rootView?.findViewById<View>(R.id.login_form_content_stub) as ViewStub?
        formContainer?.layoutResource = contentLayout
        formContainer?.inflate()
        return rootView
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = createMainView(inflater, container, savedInstanceState)
        setupLabel(rootView?.findViewById<View>(R.id.label) as TextView?)
        setupContent(rootView)
        bottomButton = rootView?.findViewById(R.id.bottom_button)
        setupBottomButton(bottomButton)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<View>(R.id.toolbar) as Toolbar?
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            buildToolbar(toolbar, actionBar)
        }
        if (savedInstanceState == null) {
            EditTextUtils.showSoftInput(editTextToFocusOnStart)
        }
    }

    protected open fun buildToolbar(toolbar: Toolbar?, actionBar: ActionBar) {
        val toolbarIcon = toolbar?.findViewById<View>(R.id.toolbar_icon)
        if (toolbarIcon != null) {
            toolbarIcon.visibility = View.VISIBLE
        }
        actionBar.setDisplayShowTitleEnabled(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            isInProgress = savedInstanceState.getBoolean(KEY_IN_PROGRESS)
            mLoginFinished = savedInstanceState.getBoolean(KEY_LOGIN_FINISHED)
            if (isInProgress) {
                startProgress()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // this will throw if parent activity doesn't implement the login listener interface
        @Suppress("UNCHECKED_CAST")
        mLoginListener = context as LoginListenerType
    }

    override fun onDetach() {
        super.onDetach()
        mLoginListener = null
    }

    override fun onStart() {
        super.onStart()
        if (listenForLogin()) {
            mDispatcher.register(this)
        }
    }

    override fun onStop() {
        super.onStop()
        if (listenForLogin()) {
            mDispatcher.unregister(this)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IN_PROGRESS, isInProgress)
        outState.putBoolean(KEY_LOGIN_FINISHED, mLoginFinished)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_login, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.help) {
            mAnalyticsListener.trackShowHelpClick()
            onHelp()
            return true
        }
        return false
    }

    override fun onDestroy() {
        endProgress()
        super.onDestroy()
    }

    override fun onDestroyView() {
        bottomButton = null
        mProgressDialog?.setOnCancelListener(null)
        mProgressDialog = null
        super.onDestroyView()
    }

    protected fun startProgressIfNeeded() {
        if (!isInProgress) {
            startProgress()
        }
    }

    @JvmOverloads
    protected fun startProgress(cancellable: Boolean = true) {
        bottomButton?.isEnabled = false
        mProgressDialog = ProgressDialog.show(activity, "", activity?.getString(progressBarText), true, cancellable) {
            endProgressIfNeeded()
        }
        isInProgress = true
    }

    protected fun endProgressIfNeeded() {
        if (isInProgress) {
            endProgress()
        }
    }

    @CallSuper
    protected open fun endProgress() {
        isInProgress = false
        mProgressDialog?.apply {
            cancel()
            setOnCancelListener(null)
        }
        mProgressDialog = null
        bottomButton?.isEnabled = true
    }

    protected fun doFinishLogin() {
        if (mLoginFinished) {
            onLoginFinished(false)
            return
        }
        if (mProgressDialog == null) {
            startProgress()
        }
        mProgressDialog?.setCancelable(false)
        mDispatcher.dispatch(AccountActionBuilder.newFetchAccountAction())
    }

    protected open fun onLoginFinished() {}
    protected fun onLoginFinished(success: Boolean) {
        mLoginFinished = true
        if (success && mLoginListener != null) {
            onLoginFinished()
        }
        endProgress()
    }

    protected fun saveCredentialsInSmartLock(loginListener: LoginListener?, username: String?, password: String?) {
        // mUsername and mPassword are null when the user log in with a magic link
        loginListener?.saveCredentialsInSmartLock(
                username, password, mAccountStore.account.displayName,
                Uri.parse(mAccountStore.account.avatarUrl)
        )
    }

    // OnChanged events
    @Subscribe(threadMode = MAIN)
    open fun onAccountChanged(event: OnAccountChanged) {
        if (!isAdded || mLoginFinished) {
            return
        }
        if (event.isError) {
            AppLog.e(API, "onAccountChanged has error: " + event.error.type + " - " + event.error.message)
            if (event.error.type == SETTINGS_FETCH_REAUTHORIZATION_REQUIRED_ERROR) {
                // This probably means we're logging in to 2FA-enabled account with a non-production WP.com client id.
                // A few WordPress.com APIs like /me/settings/ won't work for this account.
                ToastUtils.showToast(context, R.string.error_disabled_apis, LONG)
            } else {
                ToastUtils.showToast(context, R.string.error_fetch_my_profile, LONG)
                onLoginFinished(false)
                return
            }
        }
        if (event.causeOfChange == FETCH_ACCOUNT) {
            // The user's account info has been fetched and stored - next, fetch the user's settings
            mDispatcher.dispatch(AccountActionBuilder.newFetchSettingsAction())
        } else if (event.causeOfChange == FETCH_SETTINGS) {
            // The user's account settings have also been fetched and stored - now we can fetch the user's sites
            val payload = SiteUtils.getFetchSitesPayload(isJetpackAppLogin)
            mDispatcher.dispatch(SiteActionBuilder.newFetchSitesAction(payload))
            mDispatcher.dispatch(AccountActionBuilder.newFetchSubscriptionsAction())
        }
    }

    protected open val isJetpackAppLogin: Boolean
        get() = (mLoginListener is LoginListener && (mLoginListener as LoginListener).loginMode == JETPACK_LOGIN_ONLY)

    @Subscribe(threadMode = MAIN)
    open fun onSiteChanged(event: OnSiteChanged) {
        if (!isAdded || mLoginFinished) {
            return
        }
        if (event.isError) {
            AppLog.e(API, "onSiteChanged has error: " + event.error.type + " - " + event.error.toString())
            if (!isAdded || event.error.type != DUPLICATE_SITE) {
                onLoginFinished(false)
                return
            }
            if (event.rowsAffected == 0) {
                // If there is a duplicate site and not any site has been added, show an error and
                // stop the sign in process
                ToastUtils.showToast(context, R.string.cannot_add_duplicate_site)
                onLoginFinished(false)
                return
            } else {
                // If there is a duplicate site, notify the user something could be wrong,
                // but continue the sign in process
                ToastUtils.showToast(context, R.string.duplicate_site_detected)
            }
        }
        onLoginFinished(true)
    }

    companion object {
        private const val KEY_IN_PROGRESS = "KEY_IN_PROGRESS"
        private const val KEY_LOGIN_FINISHED = "KEY_LOGIN_FINISHED"
    }
}
