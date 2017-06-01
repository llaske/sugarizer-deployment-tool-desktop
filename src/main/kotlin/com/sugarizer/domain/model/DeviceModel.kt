package com.sugarizer.domain.model

import io.reactivex.Observable
import javafx.beans.property.SimpleStringProperty
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.JadbException
import se.vidstige.jadb.RemoteFile
import se.vidstige.jadb.managers.PackageManager
import java.io.File

class DeviceModel(device: se.vidstige.jadb.JadbDevice) {
    var jadbDevice: se.vidstige.jadb.JadbDevice = device
    val name = javafx.beans.property.SimpleStringProperty("name")
    val status = javafx.beans.property.SimpleStringProperty("status")
    val action = javafx.beans.property.SimpleStringProperty("action")

    init {
        name.set(device.serial)
        action.set("Nothing")

        try {
            status.set(device.state.toString())
        } catch (e: se.vidstige.jadb.JadbException) {
            e.printStackTrace()
        }
    }

    fun getDevice(): se.vidstige.jadb.JadbDevice { return jadbDevice }

    fun setDevice(device: se.vidstige.jadb.JadbDevice) {
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

    fun nameProperty(): javafx.beans.property.SimpleStringProperty { return name }
    fun statusProperty(): javafx.beans.property.SimpleStringProperty { return status }
    fun actionProperty(): javafx.beans.property.SimpleStringProperty { return action }

    fun push(localFile: String, remoteFile: String) : io.reactivex.Observable<String> {
        return io.reactivex.Observable.create {
            jadbDevice.push(java.io.File(localFile), se.vidstige.jadb.RemoteFile(remoteFile))

            it.onComplete()
        }
    }

    fun pull(remoteFile: String, localFile: String) : io.reactivex.Observable<String> {
        return io.reactivex.Observable.create {
            jadbDevice.pull(se.vidstige.jadb.RemoteFile(remoteFile), java.io.File(localFile))

            it.onComplete()
        }
    }

    fun installAPK(file: String) : io.reactivex.Observable<String> {
       return io.reactivex.Observable.create {
           se.vidstige.jadb.managers.PackageManager(jadbDevice).install(java.io.File(file))

           it.onComplete()
       }
    }

    fun remove(file: String) : io.reactivex.Observable<String> {
        return io.reactivex.Observable.create {
            se.vidstige.jadb.managers.PackageManager(jadbDevice).remove(se.vidstige.jadb.RemoteFile(file))

            it.onComplete()
        }
    }
}
