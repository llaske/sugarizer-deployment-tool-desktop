package com.sugarizer.view.devicedetails.view.devicedetails

import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXRippler
import com.sugarizer.model.DeviceModel
import com.sugarizer.utils.shared.JADB
import com.sugarizer.Main
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import se.vidstige.jadb.managers.PackageManager
import java.io.IOException
import javax.inject.Inject


class DeviceDetailsPresenter(val device: DeviceModel) : Dialog<String>() {

    @Inject lateinit var jadb: JADB

    @FXML lateinit var listPackage: JFXListView<String>
    @FXML lateinit var ping: JFXRippler
    @FXML lateinit var name: Label
    @FXML lateinit var macAddress: Label
    @FXML lateinit var deviceID: Label

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/dialog/device-details.fxml"))

        var view = DeviceDetailsView()
        loader.setRoot(view)
        loader.setController(this)

        dialogPane.scene.window.setOnCloseRequest { close() }

        title = "Details of " + device.name.get()

        try {
            loader.load<StackPane>()
            dialogPane.content = view

            PackageManager(device.jadbDevice).packages.forEach {
                listPackage.items.add(it.toString())
            }

            name.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.product.name", ""))
            deviceID.text = " Device ID"
            macAddress.text = "Mac Address"

            ping.onMouseClicked = onClickPing()
//            model.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.product.model", ""))
//            romName.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.cm.releasetype", ""))
//            romVersion.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.modversion", ""))
//            apiVersion.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.build.version.sdk", ""))

            println(jadb.convertStreamToString(device.jadbDevice.executeShell("netcfg", "")))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onClickPing(): EventHandler<MouseEvent> {
        return EventHandler {
            jadb.ping(device.jadbDevice)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe()
        }
    }
}

class DeviceDetailsView() : StackPane() {

}