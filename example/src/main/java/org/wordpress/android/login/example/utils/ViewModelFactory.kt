package org.wordpress.android.login.example.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ViewModelFactory @Inject constructor(
    private val viewModelsMap: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(viewModelClass: Class<T>): T {
        val creator = viewModelsMap[viewModelClass] ?: viewModelsMap.entries.firstOrNull {
            viewModelClass.isAssignableFrom(it.key)
        }?.value
        ?: throw IllegalArgumentException("View model not found [$viewModelClass]. " +
                "Have you added corresponding method into the ViewModelModule.")
        return creator.get() as T
    }
}
