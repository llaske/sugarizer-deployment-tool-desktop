package com.sugarizer.domain.shared

import com.sugarizer.domain.model.DeviceEventModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RxBus {
    var bus:PublishSubject<DeviceEventModel> = PublishSubject.create()

    fun send(objects: DeviceEventModel) {
        bus.onNext(objects)
    }

    fun toObservable() : Observable<DeviceEventModel> {
        return bus
    }

    fun hasObservers() : Boolean {
        return bus.hasObservers()
    }
}