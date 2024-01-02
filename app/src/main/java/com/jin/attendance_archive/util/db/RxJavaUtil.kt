package com.jin.attendance_archive.util.db

import com.jin.attendance_archive.util.Debug
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun <T> Single<T>.single(url: String): Single<T> {
    Debug.request(url)
    return subscribeOn(Schedulers.io())
        .doOnSuccess { Debug.response(it.toString()) }
        .doOnError { Debug.error(it) }
        .observeOn(Schedulers.io())
}

fun <T> Flowable<T>.flowable(): Flowable<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
}

fun Disposable.addTo(compositeDisposable: CompositeDisposable): Disposable =
    apply { compositeDisposable.add(this) }