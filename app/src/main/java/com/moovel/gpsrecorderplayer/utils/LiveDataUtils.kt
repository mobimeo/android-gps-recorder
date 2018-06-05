package com.moovel.gpsrecorderplayer.utils

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations

fun <X, Y> LiveData<X>.map(func: (X) -> Y): LiveData<Y> = Transformations.map(this, func)

fun <X, Y> LiveData<X>.switchMap(func: (X) -> LiveData<Y>?): LiveData<Y> = Transformations.switchMap(this, func)

@Suppress("UNCHECKED_CAST")
fun <X> LiveData<X>.observe(owner: LifecycleOwner, observer: (X) -> Unit) = observe(owner, Observer { observer(it as X) })
