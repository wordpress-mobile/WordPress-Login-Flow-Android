package org.wordpress.android.login.example.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.wordpress.android.login.example.LoginActivity
import org.wordpress.android.login.example.LoginPrologueFragment

@Module
abstract class AppModule {
    @Binds
    abstract fun bindContext(application: Application): Context

    @ContributesAndroidInjector
    abstract fun loginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun loginPrologueFragment(): LoginPrologueFragment
}
