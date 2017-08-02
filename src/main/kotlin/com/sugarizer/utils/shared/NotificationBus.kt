package com.sugarizer.utils.shared

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class NotificationBus {
    var bus: PublishSubject<String> = PublishSubject.create()

    fun send(objects: String) {
        bus.onNext(objects)
    }

    fun toObservable() : Observable<String> {
        return bus
    }

    fun hasObservers() : Boolean {
        return bus.hasObservers()
    }
}