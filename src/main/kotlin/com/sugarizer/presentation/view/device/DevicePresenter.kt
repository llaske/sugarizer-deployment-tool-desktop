package com.sugarizer.presentation.view.device

import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXPopup
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsView
import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.RxBus
import com.sugarizer.domain.shared.StringUtils
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import tornadofx.useMaxSize

class DevicePresenter(val view: DeviceContract.View, val jadb: JADB, val rxBus: RxBus, val stringUtils: StringUtils) : DeviceContract.Presenter {

    init {
        jadb.watcher()
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe { deviceEvent ->
                    run {
                        when (deviceEvent.status) {
                            DeviceEventModel.Status.ADDED -> { view.onDeviceAdded(deviceEvent) }
                            DeviceEventModel.Status.REMOVED -> { view.onDeviceRemoved(deviceEvent) }
                            else -> { }
                        }
                    }
                }

        rxBus.toObservable().subscribe { deviceEvent -> run { view.onDeviceChanged(deviceEvent) } }
    }

    override fun onPingClick(device: DeviceModel): EventHandler<ActionEvent> {
        return EventHandler {
            jadb.hasPackage(device.jadbDevice, "com.sugarizer.sugarizerdeploymenttoolapp")
                    .subscribeOn(Schedulers.computation())
                    .doOnComplete { jadb.ping(device.jadbDevice) }
                    .subscribe { println(it) }
        }
    }

    override fun onTableRowDoubleClick(selectedItem: DeviceModel) {
        println("Double Click")
        var dialog = JFXDialog(view.getParent(), DeviceDetailsPresenter(selectedItem), JFXDialog.DialogTransition.TOP)
        dialog.useMaxSize = false

        dialog.show()
    }

    override fun start() {
        view.test()
    }

}