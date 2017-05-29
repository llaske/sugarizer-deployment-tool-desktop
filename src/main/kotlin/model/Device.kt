package model

import io.reactivex.Observable
import javafx.beans.property.SimpleStringProperty
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.RemoteFile
import java.io.File

class Device(device: JadbDevice) {
    val jadbDevice: JadbDevice = device
    val name =  SimpleStringProperty("name")
    val status = SimpleStringProperty("status")
    val action = SimpleStringProperty("action")

    init {
        name.set(device.serial)
        status.set(device.state.toString())
        action.set("Nothing")
    }

    fun getDevice(): JadbDevice {
        return jadbDevice
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

    fun push(localFile: String, remoteFile: String) : Observable<String> {
        return Observable.create { subscriber ->
            run {
                jadbDevice.push(File(localFile), RemoteFile(remoteFile))

                subscriber.onComplete()
            }
        }
    }
}