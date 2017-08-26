package com.sugarizer.view.devicedetails.view.devicedetails

import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXRippler
import com.sugarizer.model.DeviceModel
import com.sugarizer.utils.shared.JADB
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemDevice
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.managers.PackageManager
import java.io.IOException
import javax.inject.Inject


class DeviceDetailsPresenter(val device: ListItemDevice) : Dialog<String>() {

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

        title = "Details of " + device.nameLabel.text

        try {
            loader.load<StackPane>()
            dialogPane.content = view

            PackageManager(device.device).packages.forEach {
                var tmp = it.toString()
                var lastI = tmp.lastIndexOf(".")
                if (lastI > 0)
                    listPackage.items.add(tmp.substring(lastI + 1, tmp.length))
                else
                    listPackage.items.add(tmp)
            }

            name.text = jadb.convertStreamToString(device.device.executeShell("getprop ro.product.name", ""))
            deviceID.text = " Device ID"
            macAddress.text = "Mac Address"

            ping.onMouseClicked = onClickPing()
            println(jadb.convertStreamToString(device.device.executeShell("settings get secure android_id", "")))
            println(jadb.convertStreamToString(device.device.executeShell("adb shell settings get secure bluetooth_address", "")))
            deviceID.text = jadb.convertStreamToString(device.device.executeShell("settings get secure android_id", ""))
            macAddress.text = jadb.convertStreamToString(device.device.executeShell("settings get secure bluetooth_address", ""))
//            romName.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.cm.releasetype", ""))
//            romVersion.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.modversion", ""))
//            apiVersion.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.build.version.sdk", ""))

            println(jadb.convertStreamToString(device.device.executeShell("netcfg", "")))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onClickPing(): EventHandler<MouseEvent> {
        return EventHandler {
            jadb.ping(device.device)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe()
        }
    }
}

class DeviceDetailsView() : StackPane() {

}