package com.moovel.gpsrecorderplayer.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations

fun <X, Y> LiveData<X>.map(func: (X) -> Y): LiveData<Y> = Transformations.map(this, func)

fun <X, Y> LiveData<X>.switchMap(func: (X) -> LiveData<Y>?): LiveData<Y> = Transformations.switchMap(this, func)

@Suppress("UNCHECKED_CAST")
fun <X> LiveData<X>.observe(owner: LifecycleOwner, observer: (X) -> Unit) = observe(owner, Observer { observer(it as X) })
