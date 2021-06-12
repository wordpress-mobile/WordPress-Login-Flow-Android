package org.wordpress.android.login.util

import org.wordpress.android.util.AppLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogWrapper @Inject constructor() {
    fun d(tag: AppLog.T, message: String) = AppLog.d(tag, message)
    fun e(tag: AppLog.T, message: String) = AppLog.e(tag, message)
    fun i(tag: AppLog.T, message: String) = AppLog.i(tag, message)
}
