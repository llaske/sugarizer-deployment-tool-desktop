package com.sugarizer.view.device.type

import com.sugarizer.Main
import com.sugarizer.listitem.ListItemDevice
import com.sugarizer.utils.shared.JADB
import com.sugarizer.view.device.DeviceContract
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class APK(val view: DeviceContract.View) {
    @Inject lateinit var jadb: JADB

    init {
        Main.appComponent.inject(this)
    }

    fun start(listAPK: MutableList<File>, listDevice: List<ListItemDevice>){
        var max = (listAPK.size * listDevice.size).toDouble()
        var num = 0.0
        view.showProgressFlash("Install: 0 %")

        Observable.create<Any> { subscriber ->
            listDevice.forEach {
                it.changeState(ListItemDevice.State.WORKING)
                install(it, listAPK, 0, subscriber)
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe({
                    ++num
                    view.showProgressFlash("Install: " + Math.round((num / max) * 100) + " %")

                    if (num == max) {
                        view.hideProgressFlash()
                        listAPK.clear()
                    }
                }, {}, {})
    }

    fun install(device: ListItemDevice, listAPK: List<File>, index: Int, subscriber: ObservableEmitter<Any>){
        jadb.installAPK(device.device, listAPK[index], false)
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe({}, {}, {
                    subscriber.onNext("")
                    if (index < listAPK.size - 1) {
                        install(device, listAPK, index + 1, subscriber)
                    } else {
                        device.changeState(ListItemDevice.State.FINISH)
                    }
                })
    }
}