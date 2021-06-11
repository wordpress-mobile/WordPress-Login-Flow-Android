package org.wordpress.android.login.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * Merges LiveData sources using a given function. The function returns an object of a new type.
 * @param sources all source
 * @return new data source
 */
fun <T> merge(vararg sources: LiveData<T>): MediatorLiveData<T> {
    val mediator = MediatorLiveData<T>()
    for (source in sources) {
        mediator.addSource(source) {
            mediator.value = it
        }
    }
    return mediator
}
