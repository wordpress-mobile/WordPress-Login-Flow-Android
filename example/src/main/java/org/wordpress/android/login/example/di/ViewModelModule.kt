package org.wordpress.android.login.example.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.wordpress.android.login.LoginSiteAddressViewModel
import org.wordpress.android.login.example.LoginViewModel
import org.wordpress.android.login.example.utils.ViewModelFactory
import org.wordpress.android.login.example.utils.ViewModelKey

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun viewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun loginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginSiteAddressViewModel::class)
    abstract fun loginSiteAddressViewModel(viewModel: LoginSiteAddressViewModel): ViewModel
}
