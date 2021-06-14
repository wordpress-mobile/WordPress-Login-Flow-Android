package org.wordpress.android.login.example

import com.yarolegovich.wellsql.WellSql
import dagger.android.DaggerApplication
import org.wordpress.android.fluxc.persistence.WellSqlConfig
import org.wordpress.android.login.example.di.DaggerAppComponent
import javax.inject.Inject

class ExampleApp : DaggerApplication() {
    @Inject lateinit var wellSqlConfig: WellSqlConfig

    override fun onCreate() {
        super.onCreate()
        WellSql.init(wellSqlConfig)
    }

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}
