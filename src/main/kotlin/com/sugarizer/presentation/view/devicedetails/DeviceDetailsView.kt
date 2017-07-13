package com.sugarizer.presentation.view.devicedetails.view.devicedetails

import com.jfoenix.controls.JFXDialog
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import se.vidstige.jadb.managers.PackageManager
import java.io.IOException
import javax.inject.Inject


class DeviceDetailsPresenter(val device: DeviceModel) : StackPane() {

    @Inject lateinit var jadb: JADB

    @FXML lateinit var listPackage: ListView<String>
    @FXML lateinit var name: Label
    @FXML lateinit var model: Label
    @FXML lateinit var romName: Label
    @FXML lateinit var romVersion: Label
    @FXML lateinit var apiVersion: Label

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/device-details.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        //dialogPane.scene.window.setOnCloseRequest { close() }

        //title = "Details of " + device.name.get()

        try {
            loader.load<StackPane>()
            //dialogPane.content = view
            //dialogContainer = view

//            PackageManager(device.jadbDevice).packages.forEach {
//                listPackage.items.add(it.toString())
//            }

            name.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.product.name", ""))
            model.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.product.model", ""))
            romName.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.cm.releasetype", ""))
            romVersion.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.modversion", ""))
            apiVersion.text = jadb.convertStreamToString(device.jadbDevice.executeShell("getprop ro.build.version.sdk", ""))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

class DeviceDetailsView() : StackPane() {

}