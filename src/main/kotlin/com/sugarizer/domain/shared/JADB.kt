package com.sugarizer.domain.shared

import com.sugarizer.BuildConfig
import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.main.Main
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import se.vidstige.jadb.DeviceDetectionListener
import se.vidstige.jadb.DeviceWatcher
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import java.net.ConnectException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class JADB {

    @Inject lateinit var bus: RxBus
    @Inject lateinit var stringUtils: StringUtils

    var listJadb = mutableListOf<JadbDevice>()
    var connection: JadbConnection = JadbConnection()

    var onChangedObservable: Observable<DeviceEventModel> = Observable.create {}

    init {
        Main.appComponent.inject(this)

        startADB()
    }

    fun startADB() {
        var process: Process? = null

        when (OsCheck.operatingSystemType) {
            OsCheck.OSType.Windows -> { process = Runtime.getRuntime().exec(arrayOf("./adb/windows/platform-tools/adb.exe", "start-server")) }
            OsCheck.OSType.Linux -> { process = Runtime.getRuntime().exec(arrayOf("./adb/linux/platform-tools/adb", "start-server"))}
            OsCheck.OSType.MacOS -> { process = Runtime.getRuntime().exec(arrayOf("./adb/MACOS/platform-tools/adb", "start-server")) }
            OsCheck.OSType.Other -> { println("OS invalid") }
        }

        process?.let {
            println(convertStreamToString(it.inputStream))
        }
    }

    fun stopADB() {
        var process: Process? = null

        when (OsCheck.operatingSystemType) {
            OsCheck.OSType.Windows -> { process = Runtime.getRuntime().exec(arrayOf("./adb/windows/platform-tools/adb.exe", "kill-server")) }
            OsCheck.OSType.Linux -> { process = Runtime.getRuntime().exec(arrayOf("./adb/linux/platform-tools/adb", "kill-server"))}
            OsCheck.OSType.MacOS -> { process = Runtime.getRuntime().exec(arrayOf("./adb/MACOS/platform-tools/adb", "kill-server")) }
            OsCheck.OSType.Other -> { println("OS invalid") }
        }

        process?.let {
            println(convertStreamToString(it.inputStream))
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
                var watcher: DeviceWatcher = connection.createDeviceWatcher(object : DeviceDetectionListener {
                    override fun onDetect(devices: MutableList<se.vidstige.jadb.JadbDevice>?) {
                        println("onDetect")

                        if (devices != null) {
                            listJadb.filter { !devices.contains(it) }
                                    .forEach {
                                        listJadb.remove(it)
                                        subscriber.onNext(DeviceEventModel(DeviceEventModel.Status.REMOVED, DeviceModel(it)))
                                    }

                            devices.filter { !listJadb.contains(it) }
                                    .forEach {
                                        var deviceModel: DeviceModel = DeviceModel(it)

                                        Observable.create<String> {
                                            while (deviceModel.isDeviceOffline()) {
                                                println("Device Offline")
                                                Thread.sleep(1000)
                                            }

                                            it.onComplete()
                                        }
                                                .subscribeOn(Schedulers.computation())
                                                .observeOn(Schedulers.io())
                                                .doOnComplete {
                                                    println("On Complete")
                                                    deviceModel.hasPackage(BuildConfig.APP_PACKAGE).subscribe {
                                                        if (it) {
                                                            deviceModel.startApp().doOnComplete { deviceModel.sendLog("Connected to Sugarizer Deployment Tool") }.subscribe()
                                                        } else {
                                                            changeAction(deviceModel.jadbDevice, "Installing Application...")
                                                            deviceModel.installAPK(BuildConfig.APK_LOCATION)
                                                                    .doOnComplete { deviceModel.startApp().doOnComplete {
                                                                        deviceModel.sendLog("Connected to Sugarizer Deployment Tool")
                                                                        changeAction(deviceModel.jadbDevice, "Application installed")
                                                                    }.subscribe()
                                                                    }.subscribe()
                                                        }
                                                    }
                                                }.subscribe()

                                        listJadb.add(it)

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

                watcher.watch()
            } catch (e: java.net.ConnectException) {
                println("Error: Connection refused (adb is started ?)")
            }
        }
        }
    }

    fun watchChanged(): io.reactivex.Observable<DeviceEventModel> {
        return onChangedObservable
    }

    fun convertStreamToString(`is`: java.io.InputStream): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}