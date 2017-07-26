package com.sugarizer.domain.model

import com.jfoenix.controls.JFXPopup
import com.sugarizer.BuildConfig
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.StringUtils
import com.sugarizer.main.Main
import com.sun.org.apache.xpath.internal.operations.Bool
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXMLLoader
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.JadbException
import se.vidstige.jadb.RemoteFile
import se.vidstige.jadb.managers.PackageManager
import java.io.File
import javax.inject.Inject

class DeviceModel(device: JadbDevice) {

    @Inject lateinit var stringUtils: StringUtils
    @Inject lateinit var jadb: JADB

    var jadbDevice: JadbDevice = device
    val name = SimpleStringProperty("name")
    val status = SimpleStringProperty("status")
    val action = SimpleStringProperty("action")
    val ping = SimpleStringProperty("ping")
    val model = SimpleStringProperty("model")
    val version = SimpleStringProperty("version")
    val serial = SimpleStringProperty("serial")
    var udid = SimpleStringProperty("udid")

    init {
        Main.appComponent.inject(this)

        name.set(device.serial)
        action.set("Nothing")
        ping.set("Identify Device")

        try {
            status.set(device.state.toString())
        } catch (e: JadbException) {
            //e.printStackTrace()
        }
    }

    fun getDevice(): JadbDevice { return jadbDevice }

    fun setDevice(device: JadbDevice) {
        jadbDevice = device
    }

    fun setName(newName: String) {
        name.set(newName)
    }

    fun setStatus(newStatus: String) {
        status.set(newStatus)
    }

    fun setAction(newAction: String) {
        action.set(newAction)
    }

    fun nameProperty(): SimpleStringProperty { return name }
    fun statusProperty(): SimpleStringProperty { return status }
    fun actionProperty(): SimpleStringProperty { return action }
    fun pingProperty(): SimpleStringProperty { return ping }

    fun reload(){
        try {
            serial.set(jadbDevice.serial)
            name.set(jadb.convertStreamToString(jadbDevice.executeShell("getprop ro.product.name", "")))
            model.set(jadb.convertStreamToString(jadbDevice.executeShell("getprop ro.product.model", "")))
            version.set(jadb.convertStreamToString(jadbDevice.executeShell("getprop ro.build.version.sdk", "")))
            udid.set(jadb.convertStreamToString(jadbDevice.executeShell("settings get secure android_id")))

            println("UDID: " + udid.get())
        } catch (e: JadbException) {
            e.printStackTrace()
        }
    }
}
