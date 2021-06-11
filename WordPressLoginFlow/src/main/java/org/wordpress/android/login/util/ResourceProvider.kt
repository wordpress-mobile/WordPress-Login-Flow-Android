package org.wordpress.android.login.util

import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import javax.inject.Inject

class ResourceProvider @Inject constructor(private val contextProvider: ContextProvider) {
    fun getString(@StringRes resourceId: Int) = contextProvider.getContext().getString(resourceId)

    fun getString(@StringRes resourceId: Int, vararg formatArgs: Any) =
            contextProvider.getContext().getString(resourceId, *formatArgs)

    fun getStringArray(id: Int): Array<String> = contextProvider.getContext().resources.getStringArray(id)

    fun getColor(@ColorRes resourceId: Int) = ContextCompat.getColor(contextProvider.getContext(), resourceId)

    fun getDimensionPixelSize(@DimenRes dimen: Int) =
            contextProvider.getContext().resources.getDimensionPixelSize(dimen)

    fun getDimension(@DimenRes dimen: Int) = contextProvider.getContext().resources.getDimension(dimen)

    fun getDimensionPixelOffset(id: Int) = contextProvider.getContext().resources.getDimensionPixelOffset(id)

    fun getDrawable(iconId: Int) = ContextCompat.getDrawable(contextProvider.getContext(), iconId)
}
