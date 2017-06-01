package com.sugarizer.domain.interactor

import io.reactivex.Observable
import io.reactivex.Observer


interface Interactor<T> {
    fun execute(observer: io.reactivex.Observer<T>)

    fun execute() : io.reactivex.Observable<T>
}