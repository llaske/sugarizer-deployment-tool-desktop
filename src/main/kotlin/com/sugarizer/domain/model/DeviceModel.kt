package com.sugarizer.domain.model

import com.sugarizer.BuildConfig
import com.sugarizer.domain.shared.StringUtils
import com.sugarizer.main.Main
import com.sun.org.apache.xpath.internal.operations.Bool
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.JadbException
import se.vidstige.jadb.RemoteFile
import se.vidstige.jadb.managers.PackageManager
import java.io.File
import javax.inject.Inject

class DeviceModel(device: JadbDevice) {

    @Inject lateinit var stringUtils: StringUtils

    var jadbDevice: JadbDevice = device
    val name = SimpleStringProperty("name")
    val status = SimpleStringProperty("status")
    val action = SimpleStringProperty("action")
    val ping = SimpleStringProperty("ping")

    init {
        Main.appComponent.inject(this)

        name.set(device.serial)
        action.set("Nothing")
        ping.set("Ping")

        try {
            status.set(device.state.toString())
        } catch (e: JadbException) {
            e.printStackTrace()
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
}
