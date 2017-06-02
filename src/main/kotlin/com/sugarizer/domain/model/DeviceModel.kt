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
           println("Before Install")
           PackageManager(jadbDevice).install(File(file))
           println("After Install")

           it.onComplete()
       }
    }

    fun remove(file: String) : Observable<String> {
        return Observable.create {
            PackageManager(jadbDevice).remove(RemoteFile(file))

            it.onComplete()
        }
    }

    fun getListPackage() : Observable<List<String>> {
        return Observable.create {
            var list: MutableList<String> = mutableListOf()
            var returnCMD: String = stringUtils.convertStreamToString(jadbDevice.executeShell(BuildConfig.CMD_LIST_PACKAGE))

            returnCMD.split("\n").forEach {
                list.add(it.removePrefix("package:"))
            }

            it.onNext(list)
        }
    }

    fun hasPackage(name: String) : Observable<Boolean> {
        return Observable.create { has ->
            run {
                getListPackage().subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.io())
                        .subscribe {
                            has.onNext(it.contains(name))
                        }
            }
        }
    }

    fun ping() {
        jadbDevice.executeShell(BuildConfig.CMD_PING)
        sendLog("Ping")
    }

    fun sendLog(send: String) {
        println(stringUtils.convertStreamToString(jadbDevice.executeShell(BuildConfig.CMD_LOG + " --es extra_log \"" + send + "\"")))
    }

    fun startApp(): Observable<Boolean> {
        return Observable.create {
            println(stringUtils.convertStreamToString(jadbDevice.executeShell("am start -n " + BuildConfig.APP_PACKAGE + "/" + BuildConfig.APP_PACKAGE + ".EmptyActivity")))

            it.onComplete()
        }
    }

    fun isDeviceOffline(): Boolean {
        try {
            jadbDevice.state
            return false
        } catch (e: JadbException) {
            return true
        }
    }
}
