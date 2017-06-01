package com.sugarizer.domain.shared

import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.main.Main
import io.reactivex.Observable
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import javax.inject.Inject

class JADB {

    @Inject lateinit var bus: RxBus

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
        } catch (e: java.net.ConnectException) {
            return 0
        }
    }

    fun changeAction(device: se.vidstige.jadb.JadbDevice, action: String) {
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
        println("Size: " + listJadb.size)
        if (connection.devices.size > index) {
            println("Gone change action of presentation.device: " + connection.devices[index].serial)
            //list[index].setAction(action)
            changeAction(connection.devices[index], action)
        }
    }

    fun watcher(): io.reactivex.Observable<DeviceEventModel> {
        return io.reactivex.Observable.create {
            subscriber -> run {

            try {
                var watcher: se.vidstige.jadb.DeviceWatcher = connection.createDeviceWatcher(object : se.vidstige.jadb.DeviceDetectionListener {
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
                                        listJadb.add(it)
                                        subscriber.onNext(DeviceEventModel(DeviceEventModel.Status.ADDED, DeviceModel(it)))
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