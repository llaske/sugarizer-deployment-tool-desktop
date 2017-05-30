package domain.model

import io.reactivex.Observable
import javafx.beans.property.SimpleStringProperty
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.JadbException
import se.vidstige.jadb.RemoteFile
import se.vidstige.jadb.managers.PackageManager
import java.io.File

class DeviceModel(device: JadbDevice) {
    var jadbDevice: JadbDevice = device
    val name =  SimpleStringProperty("name")
    val status = SimpleStringProperty("status")
    val action = SimpleStringProperty("action")

    init {
        name.set(device.serial)
        action.set("Nothing")

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

    fun push(localFile: String, remoteFile: String) : Observable<String> {
        return Observable.create {
            jadbDevice.push(File(localFile), RemoteFile(remoteFile))

            it.onComplete()
        }
    }

    fun pull(remoteFile: String, localFile: String) : Observable<String> {
        return Observable.create {
            jadbDevice.pull(RemoteFile(remoteFile), File(localFile))

            it.onComplete()
        }
    }

    fun installAPK(file: String) : Observable<String> {
       return Observable.create {
           PackageManager(jadbDevice).install(File(file))

           it.onComplete()
       }
    }

    fun remove(file: String) : Observable<String> {
        return Observable.create {
            PackageManager(jadbDevice).remove(RemoteFile(file))

            it.onComplete()
        }
    }
}
