package domain.abstraction

import domain.interactor.Interactor
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor

abstract class UseCase<T>(threadExecutor: Executor, postThreadExecutor: Executor) : Interactor<T> {
    val threadExecutor: Executor = threadExecutor
    val postThreadExecutor: Executor = postThreadExecutor

    var observer: Observer<T> = null!!

    abstract fun createObservable() : Observable<T>

    override fun execute(observer: Observer<T>) {
        createObservable()
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(Schedulers.from(postThreadExecutor))
                .subscribe(observer)
    }

    override fun execute(): Observable<T> {
        return createObservable()
    }
}