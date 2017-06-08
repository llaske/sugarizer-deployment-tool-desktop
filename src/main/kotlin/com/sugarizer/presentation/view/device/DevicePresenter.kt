package com.sugarizer.presentation.view.device

import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsView
import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.RxBus
import com.sugarizer.domain.shared.StringUtils
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.stage.Stage
import tornadofx.View

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
            device.hasPackage("com.sugarizer.sugarizerdeploymenttoolapp").subscribeOn(Schedulers.computation()).subscribe { println(it) }
            device.ping()
        }
    }

    override fun onTableRowDoubleClick(selectedItem: DeviceModel, modalStage: Stage) {
        var device: DeviceModel = selectedItem!!
        var details: View = DeviceDetailsView(device)
        var stage: Stage = Stage()

        stage.isFocused = true
        stage.initOwner(modalStage)
        details.openWindow()
    }

    override fun start() {
        view.test()
    }

}