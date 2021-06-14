package org.wordpress.android.login.example

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.wordpress.android.login.LoginListener
import org.wordpress.android.login.example.databinding.LoginPrologueFragmentBinding

class LoginPrologueFragment : Fragment(R.layout.login_prologue_fragment) {
    private lateinit var loginListener: LoginListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is LoginListener) {
            throw RuntimeException("$context must implement LoginListener")
        }
        loginListener = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(LoginPrologueFragmentBinding.bind(view)) {
            emailButton.setOnClickListener {
                TODO("Not yet implemented")
            }

            siteAddressButton.setOnClickListener {
                loginListener.loginViaSiteAddress()
            }
        }
    }

    companion object {
        val TAG: String = LoginPrologueFragment::class.java.name
    }
}
