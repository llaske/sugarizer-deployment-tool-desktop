package com.sugarizer.domain.interactor


interface Interactor<T> {
    fun execute(observer: io.reactivex.Observer<T>)

    fun execute() : io.reactivex.Observable<T>
}