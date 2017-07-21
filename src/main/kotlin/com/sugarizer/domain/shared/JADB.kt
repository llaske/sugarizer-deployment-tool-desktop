package com.sugarizer.domain.shared

import com.sugarizer.BuildConfig
import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.main.Main
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.ApkParsers
import se.vidstige.jadb.*
import se.vidstige.jadb.managers.Package
import se.vidstige.jadb.managers.PackageManager
import java.io.File
import java.net.ConnectException
import java.sql.SQLData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class JADB {

    @Inject lateinit var bus: RxBus
    @Inject lateinit var stringUtils: StringUtils

    var listJadb = FXCollections.observableArrayList<JadbDevice>()
    var listDevice = mutableListOf<DeviceModel>()
    var connection: JadbConnection = JadbConnection()
    var watcher: DeviceWatcher? = null

    init {
        Main.appComponent.inject(this)

        startADB()
    }

    fun startADB() {
        try {

            var process: Process? = null

            when (OsCheck.operatingSystemType) {
                OsCheck.OSType.Windows -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_WINDOWS_PATH, "start-server")) }
                OsCheck.OSType.Linux -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_LINUX_PATH, "start-server")) }
                OsCheck.OSType.MacOS -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_MAC_PATH, "start-server")) }
                OsCheck.OSType.Other -> { println("OS invalid") }
            }

            process?.let {
                println(convertStreamToString(it.inputStream))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            var alert = Alert(Alert.AlertType.ERROR)

            alert.title = "Error"
            alert.headerText = null
            alert.contentText = "ADB not started"
        }
    }

    fun stopADB() {
        try {
            var process: Process? = null

            when (OsCheck.operatingSystemType) {
                OsCheck.OSType.Windows -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_WINDOWS_PATH, "kill-server")) }
                OsCheck.OSType.Linux -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_LINUX_PATH, "kill-server")) }
                OsCheck.OSType.MacOS -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_MAC_PATH, "kill-server")) }
                OsCheck.OSType.Other -> { println("OS invalid") }
            }

            process?.let {
                println(convertStreamToString(it.inputStream))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun numberDevice(): Int {
        try {
            return connection.devices.size
        } catch (e: ConnectException) {
            return 0
        }
    }

    fun changeAction(device: JadbDevice, action: String) {
        var device = DeviceModel(device)

        device.setAction(action)

        bus.send(DeviceEventModel(DeviceEventModel.Status.CHANGED, device))
    }

    fun changeAction(name: String, action: String) {
//        for ((value, presentation.device) in list.withIndex()) {
//            if (presentation.device.name.get().equals(name)) {
//                //changeAction(list[value], action)
//            }
//        }
    }

    fun changeAction(index: Int, action: String) {
        if (connection.devices.size > index) {
            changeAction(connection.devices[index], action)
        }
    }

    fun watcher(): io.reactivex.Observable<DeviceEventModel> {
        return io.reactivex.Observable.create {
            subscriber -> run {

            try {
                watcher = connection.createDeviceWatcher(object : DeviceDetectionListener {
                    override fun onDetect(devices: MutableList<se.vidstige.jadb.JadbDevice>?) {
                        println("onDetect")

                        if (devices != null) {
                            listJadb.filter { !devices.contains(it) }
                                    .forEach {
                                        listJadb.remove(it)

                                        listDevice.filter {
                                            it.name.get().equals(it.name.get()) }
                                                .forEach { listDevice.remove(it) }

                                        println("Serial Device Removed: " + it.serial)

                                        subscriber.onNext(DeviceEventModel(DeviceEventModel.Status.REMOVED, DeviceModel(it)))
                                    }

                            devices.filter { !listJadb.contains(it) }
                                    .forEach {
                                        var deviceModel: DeviceModel = DeviceModel(it)

                                        Observable.create<String> {
                                            while (isDeviceOffline(deviceModel.jadbDevice)) {
                                                println("Device Offline")
                                                Thread.sleep(1000)
                                            }

                                            it.onComplete()
                                        }
                                                .subscribeOn(Schedulers.computation())
                                                .observeOn(Schedulers.io())
                                                .doOnComplete {
                                                    println("On Complete")
                                                    hasPackage(deviceModel.jadbDevice, BuildConfig.APP_PACKAGE).subscribe {
                                                        if (it) {
                                                            startApp(deviceModel.jadbDevice).doOnComplete {
                                                                sendLog(deviceModel.jadbDevice, "Connected to Sugarizer Deployment Tool")
                                                            }.subscribe()
                                                        } else {
                                                            changeAction(deviceModel.jadbDevice, "Installing Application...")
                                                            installAPK(deviceModel.jadbDevice, File(BuildConfig.APK_LOCATION), false)
                                                                    .doOnComplete {
                                                                        startApp(deviceModel.jadbDevice)
                                                                                .doOnComplete {
                                                                                    sendLog(deviceModel.jadbDevice, "Connected to Sugarizer Deployment Tool")
                                                                                    changeAction(deviceModel.jadbDevice, "Application installed")
                                                                                }.subscribe()
                                                                    }
                                                                    .doOnError {  }
                                                                    .subscribe()
                                                        }
                                                    }
                                                }.subscribe()

                                        listJadb.add(it)
                                        listDevice.add(DeviceModel(it))

                                        Thread.sleep(1000)

                                        deviceModel.reload()

                                        subscriber.onNext(DeviceEventModel(DeviceEventModel.Status.ADDED, deviceModel))
                                    }
                        }
                    }

                    override fun onException(e: Exception?) {
                        if (e != null) {
                            println("onException: " + e.printStackTrace())
                        }
                    }
                })

                watcher?.let {
                    it.watch()
                }
            } catch (e: java.net.ConnectException) {
                println("Error: Connection refused (adb is started ?)")
            }
        }
        }
    }

    fun convertStreamToString(`is`: java.io.InputStream): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    fun push(jadbDevice: JadbDevice, localFile: String, remoteFile: String) : Observable<String> {
        return Observable.create {
            jadbDevice.push(File(localFile), RemoteFile(remoteFile))

            it.onComplete()
        }
    }

    fun pull(jadbDevice: JadbDevice, remoteFile: String, localFile: String) : Observable<String> {
        return Observable.create {
            jadbDevice.pull(RemoteFile(remoteFile), File(localFile))

            it.onComplete()
        }
    }

    fun installAPK(jadbDevice: JadbDevice, file: File, force: Boolean) : Observable<String> {
        return Observable.create { subscriber ->
            run {
                if (!file.exists()) {
                    throw Throwable("File not found: " + file.name)
                }

                changeAction(jadbDevice, "Installing: " + file.name)
                sendLog(jadbDevice, "Installing: " + file.name)

                try {
                    PackageManager(jadbDevice).install(file)
                    changeAction(jadbDevice, file.name + " installed")
                    sendLog(jadbDevice, file.name + " installed")

                    subscriber.onComplete()
                } catch (e: JadbException) {
                    changeAction(jadbDevice, file.name + " already installed")
                    sendLog(jadbDevice, file.name + " already installed")

                    if (force) {
                        uninstallApp(jadbDevice, ApkFile(file).apkMeta.packageName)
                                .subscribeOn(Schedulers.computation())
                                .observeOn(JavaFxScheduler.platform())
                                .doOnComplete {
                                    sendLog(jadbDevice, "Reinstalling " + file.name)
                                    installAPK(jadbDevice, file, false)
                                            .subscribeOn(Schedulers.computation())
                                            .observeOn(JavaFxScheduler.platform())
                                            .doOnComplete {
                                                sendLog(jadbDevice, file.name +  " Reinstalled")

                                                subscriber.onComplete()
                                            }
                                            .subscribe()
                                }
                                .subscribe()
                    } else {
                        subscriber.onComplete()
                    }
                }
            }
        }
    }

    fun remove(jadbDevice: JadbDevice, file: String) : Observable<String> {
        return Observable.create {
            PackageManager(jadbDevice).remove(RemoteFile(file))

            it.onComplete()
        }
    }

    fun getListPackage(jadbDevice: JadbDevice) : Observable<List<String>> {
        return Observable.create {
            var list: MutableList<String> = mutableListOf()
            var returnCMD: String = stringUtils.convertStreamToString(jadbDevice.executeShell(BuildConfig.CMD_LIST_PACKAGE))

            returnCMD.split("\n").forEach {
                list.add(it.removePrefix("package:"))
            }

            it.onNext(list)
        }
    }

    fun hasPackage(jadbDevice: JadbDevice, name: String) : Observable<Boolean> {
        return Observable.create { has ->
            run {
                getListPackage(jadbDevice).subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.io())
                        .doOnComplete { has.onComplete() }
                        .subscribe {
                            has.onNext(it.contains(name))

                            has.onComplete()
                        }
            }
        }
    }

    fun ping(jadbDevice: JadbDevice): Observable<Any> {
        return Observable.create<Any> {
            jadbDevice.executeShell(BuildConfig.CMD_PING)
            sendLog(jadbDevice, "Ping")

            it.onComplete()
        }
    }

    fun sendLog(jadbDevice: JadbDevice, send: String) {
        Observable.create<String> {
            it.onNext(stringUtils.convertStreamToString(jadbDevice.executeShell(BuildConfig.CMD_LOG + " --es extra_log \"" + send + "\"")))
        }
                .subscribeOn(Schedulers.computation())
                .subscribe {
                    println(it)
                }
    }

    fun startApp(jadbDevice: JadbDevice): Observable<Boolean> {
        return Observable.create {
            println(stringUtils.convertStreamToString(jadbDevice.executeShell("am start -n " + BuildConfig.APP_PACKAGE + "/" + BuildConfig.APP_PACKAGE + ".EmptyActivity")))

            it.onComplete()
        }
    }

    fun uninstallApp(jadbDevice: JadbDevice, name: String): Observable<String> {
        println("Test 1")
        return Observable.create {
            sendLog(jadbDevice, "Uninstalling " + name)
            PackageManager(jadbDevice).uninstall(Package(name))
            sendLog(jadbDevice, name + " Uninstalled")

            it.onComplete()
        }
    }

    fun isDeviceOffline(jadbDevice: JadbDevice): Boolean {
        try {
            jadbDevice.state
            return false
        } catch (e: JadbException) {
            return true
        }
    }

    fun stopWatching(){
        watcher?.let {
            it.stop()
        }
    }
}