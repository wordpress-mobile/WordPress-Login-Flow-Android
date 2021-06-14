package org.wordpress.android.login.example.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import org.wordpress.android.fluxc.module.OkHttpClientModule
import org.wordpress.android.fluxc.module.ReleaseNetworkModule
import org.wordpress.android.login.di.LoginFragmentModule
import org.wordpress.android.login.di.LoginServiceModule
import org.wordpress.android.login.example.ExampleApp
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            AppModule::class,
            ViewModelModule::class,
            // Login flow modules
            LoginFragmentModule::class,
            LoginServiceModule::class,
            // FluxC modules
            OkHttpClientModule::class,
            ReleaseNetworkModule::class,
            AppConfigModule::class,
        ]
)
interface AppComponent : AndroidInjector<ExampleApp> {
    override fun inject(app: ExampleApp)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }
}
