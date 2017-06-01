package com.sugarizer.domain.abstraction

import com.sugarizer.domain.interactor.Interactor

abstract class UseCase<T>(threadExecutor: java.util.concurrent.Executor, postThreadExecutor: java.util.concurrent.Executor) : Interactor<T> {
    val threadExecutor: java.util.concurrent.Executor = threadExecutor
    val postThreadExecutor: java.util.concurrent.Executor = postThreadExecutor

    var observer: io.reactivex.Observer<T> = null!!

    abstract fun createObservable() : io.reactivex.Observable<T>

    override fun execute(observer: io.reactivex.Observer<T>) {
        createObservable()
                .subscribeOn(io.reactivex.schedulers.Schedulers.from(threadExecutor))
                .observeOn(io.reactivex.schedulers.Schedulers.from(postThreadExecutor))
                .subscribe(observer)
    }

    override fun execute(): io.reactivex.Observable<T> {
        return createObservable()
    }
}