package org.wordpress.android.login.util

import org.wordpress.android.fluxc.store.SiteStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteUtilsWrapper @Inject constructor() {
    fun getCurrentSiteIds(siteStore: SiteStore, selfHostedOnly: Boolean): List<Int> =
            SiteUtils.getCurrentSiteIds(siteStore, selfHostedOnly)
}
