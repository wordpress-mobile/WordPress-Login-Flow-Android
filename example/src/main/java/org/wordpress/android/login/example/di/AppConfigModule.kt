package org.wordpress.android.login.example.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.wordpress.android.fluxc.network.UserAgent
import org.wordpress.android.fluxc.network.rest.wpcom.auth.AppSecrets
import org.wordpress.android.fluxc.persistence.WellSqlConfig
import org.wordpress.android.login.LoginAnalyticsListener
import org.wordpress.android.login.example.BuildConfig
import org.wordpress.android.login.example.DefaultLoginAnalyticsListener

@Module
class AppConfigModule {
    @Provides
    fun provideAppSecrets(): AppSecrets {
        return AppSecrets(BuildConfig.OAUTH_APP_ID, BuildConfig.OAUTH_APP_SECRET)
    }

    @Provides
    fun provideUserAgent(appContext: Context): UserAgent {
        return UserAgent(appContext, "login-example-android")
    }

    @Provides
    fun provideAnalyticsListener(): LoginAnalyticsListener {
        return DefaultLoginAnalyticsListener()
    }

    @Provides
    fun provideWellSqlConfig(appContext: Context): WellSqlConfig {
        return WellSqlConfig(appContext)
    }
}
