package domain.shared

import io.reactivex.Observable
import domain.model.DeviceEventModel
import domain.model.DeviceModel
import se.vidstige.jadb.*
import java.net.ConnectException

class JADB {
    var listJadb = mutableListOf<JadbDevice>()
    var connection: JadbConnection = JadbConnection()

    var onChangedObservable: Observable<DeviceEventModel> = Observable.create {}

    init {
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
        onChangedObservable.doOnNext {
            var device = DeviceModel(device)

            device.setAction(action)

            DeviceEventModel(DeviceEventModel.Status.CHANGED, device)
        }
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

    fun watcher(): Observable<DeviceEventModel> {
        return Observable.create {
            subscriber -> run {

            try {
                var watcher: DeviceWatcher = connection.createDeviceWatcher(object : DeviceDetectionListener {
                    override fun onDetect(devices: MutableList<JadbDevice>?) {
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
            } catch (e: ConnectException) {
                println("Error: Connection refused (adb is started ?)")
            }
        }
        }
    }

    fun watchChanged(): Observable<DeviceEventModel> {
        return onChangedObservable
    }

    fun convertStreamToString(`is`: java.io.InputStream): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}