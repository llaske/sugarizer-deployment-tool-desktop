package domain.interactor

import io.reactivex.Observable
import io.reactivex.Observer


interface Interactor<T> {
    fun execute(observer: Observer<T>)

    fun execute() : Observable<T>
}