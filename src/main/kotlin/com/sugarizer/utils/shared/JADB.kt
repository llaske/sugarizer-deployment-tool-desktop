package com.sugarizer.utils.shared

import com.sugarizer.BuildConfig
import com.sugarizer.model.DeviceEventModel
import com.sugarizer.model.DeviceModel
import com.sugarizer.Main
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.collections.FXCollections
import javafx.scene.control.Alert
import net.dongliu.apk.parser.ApkFile
import se.vidstige.jadb.*
import se.vidstige.jadb.managers.Package
import se.vidstige.jadb.managers.PackageManager
import java.io.File
import java.net.ConnectException
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

            process?.let { println(convertStreamToString(it.inputStream)) }
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

    fun startWatching() {
        watcher()
                .subscribeOn(Schedulers.newThread())
                .observeOn(JavaFxScheduler.platform())
                .subscribe()
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

    private fun watcher(): io.reactivex.Observable<DeviceEventModel> {
        return io.reactivex.Observable.create { subscriber -> run {
            try {
                watcher = connection.createDeviceWatcher(object : DeviceDetectionListener {
                    override fun onDetect(devices: MutableList<se.vidstige.jadb.JadbDevice>?) {
                        if (devices != null) {
                            listJadb.filter { !devices.contains(it) }
                                    .forEach { onRemoveDevice(it, subscriber) }

                            devices.filter { !listJadb.contains(it) }
                                    .forEach { onNewDevice(it, subscriber)
                                            .subscribeOn(Schedulers.newThread())
                                            .subscribe() }
                        }
                    }

                    override fun onException(e: Exception?) {
                        e?.let { println("onException: " + it.printStackTrace()) }
                    }
                })

                watcher?.let { it.watch() }
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
            try {
                var list: MutableList<String> = mutableListOf()
                var returnCMD: String = stringUtils.convertStreamToString(jadbDevice.executeShell(BuildConfig.CMD_LIST_PACKAGE))

                returnCMD.split("\n").forEach {
                    list.add(it.removePrefix("package:"))
                }

                it.onNext(list)
            } catch (e: JadbException) {
                e.printStackTrace()
            }
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

    fun checkAuthorization(device: JadbDevice): Observable<String> {
        return Observable.create<String> { subscriber ->
            var process: Process? = null

            when (OsCheck.operatingSystemType) {
                OsCheck.OSType.Windows -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_WINDOWS_PATH, "devices")) }
                OsCheck.OSType.Linux -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_LINUX_PATH, "devices")) }
                OsCheck.OSType.MacOS -> { process = Runtime.getRuntime().exec(arrayOf(BuildConfig.OS_MAC_PATH, "devices")) }
                OsCheck.OSType.Other -> { println("OS invalid") }
            }

            process?.let {
                var tmp = convertStreamToString(it.inputStream)

                println(tmp)

                var indexOfDevice = tmp.indexOf(device.serial)
                var indexOfN = tmp.indexOf("\n", indexOfDevice)
                var line = tmp.substring(indexOfDevice, indexOfN)
                var status = line.substring(line.indexOf("\t") + 1, line.length)
                status = status.substring(0, status.length - 1)

//                println("Device Serial: " + device.serial)
//                println("Device find: " + indexOfDevice)
//                println("Device n: " + indexOfN)
//                println("Line: " + line)
                println("Status: " + status)

                subscriber.onNext(status)
                subscriber.onComplete()
            }
        }
    }

    private fun onNewDevice(device: JadbDevice, subscriber: ObservableEmitter<DeviceEventModel>): Observable<Any> {
        return Observable.create {
            var deviceModel = DeviceModel(device)

            bus.send(DeviceEventModel(DeviceEventModel.Status.ADDED, deviceModel))

            listenAuth(deviceModel)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.computation())
                    .subscribe({},{},{
                        hasPackage(deviceModel.jadbDevice, BuildConfig.APP_PACKAGE).subscribe({
                            Thread.sleep(1000)

                            if (it) {
                                startApp(deviceModel.jadbDevice).doOnComplete { sendLog(deviceModel.jadbDevice, "Connected to Sugarizer Deployment Tool") }.subscribe()
                            } else {
                                installAPK(deviceModel.jadbDevice, File(BuildConfig.APK_LOCATION), false)
                                        .subscribe({},{},{ startApp(deviceModel.jadbDevice).subscribe({},{},{}) })
                            }

                            listJadb.add(device)
                            listDevice.add(DeviceModel(device))

                            deviceModel.reload()
                        },{},{})
                    })
        }
    }

    private fun onRemoveDevice(it: JadbDevice, subscriber: ObservableEmitter<DeviceEventModel>) {
        listJadb.remove(it)

        listDevice.filter {
            it.name.get() == it.name.get()
        }.forEach { listDevice.remove(it) }

        subscriber.onNext(DeviceEventModel(DeviceEventModel.Status.REMOVED, DeviceModel(it)))
    }

    private fun listenAuth(device: DeviceModel): Observable<JadbDevice> {
        return Observable.create { checkAuthAndRetryWhenNot(device, it) }
    }

    private fun checkAuthAndRetryWhenNot(device: DeviceModel, subscriber: ObservableEmitter<JadbDevice>){
        checkAuthorization(device.jadbDevice)
                .subscribeOn(Schedulers.newThread())
                .subscribe {
                    Thread.sleep(1000)
                    if (it == "unauthorized") {
                        bus.send(DeviceEventModel(DeviceEventModel.Status.UNAUTHORIZED, device))
                        checkAuthAndRetryWhenNot(device, subscriber);
                    } else {
                        bus.send(DeviceEventModel(DeviceEventModel.Status.IDLE, device))
                        subscriber.onComplete()
                    }
                }
    }
}