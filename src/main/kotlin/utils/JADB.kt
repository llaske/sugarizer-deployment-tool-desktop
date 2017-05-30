package utils

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import model.Device
import model.DeviceEvent
import se.vidstige.jadb.*
import java.net.ConnectException

class JADB {
    var listJadb = mutableListOf<JadbDevice>()
    var connection: JadbConnection = JadbConnection()

    var onChangedObservable: Observable<DeviceEvent> = Observable.create {}

    init {
        try {
            connection.devices.forEach { device ->
                run {
                    try {
                        println("Name: " + device.serial)
                        println("State: " + device.state)
                    } catch (e: JadbException) {
                        println("Error: device offline")
                    }
                }
            }
        } catch (e: ConnectException){
            println("Error: connection refuse")
        }
    }

    fun numberDevice(): Int {
        return connection.devices.size
    }

    fun changeAction(device: JadbDevice, action: String) {
        onChangedObservable.doOnNext {
            var device = Device(device)

            device.setAction(action)

            DeviceEvent(DeviceEvent.Status.CHANGED, device)
        }
    }

    fun changeAction(name: String, action: String) {
//        for ((value, device) in list.withIndex()) {
//            if (device.name.get().equals(name)) {
//                //changeAction(list[value], action)
//            }
//        }
    }

    fun changeAction(index: Int, action: String) {
        println("Size: " + listJadb.size)
        if (connection.devices.size > index) {
            println("Gone change action of device: " + connection.devices[index].serial)
            //list[index].setAction(action)
            changeAction(connection.devices[index], action)
        }
    }

    fun watcher(): Observable<DeviceEvent> {
        return Observable.create {
            subscriber -> run {

            try {
                var watcher: DeviceWatcher = connection.createDeviceWatcher(object : DeviceDetectionListener {
                    override fun onDetect(devices: MutableList<JadbDevice>?) {
                        println("onDetect")

                        if (devices != null) {
                            listJadb.filter { !devices.contains(it) }
                                    .forEach {
                                        println("Size: " + listJadb.size)
                                        listJadb.remove(it)
                                        subscriber.onNext(DeviceEvent(DeviceEvent.Status.REMOVED, Device(it)))
                                    }

                            devices.filter { !listJadb.contains(it) }
                                    .forEach {
                                        listJadb.add(it)
                                        subscriber.onNext(DeviceEvent(DeviceEvent.Status.ADDED, Device(it)))
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
            } catch (e: JadbException) {
                println("Error: Connection refused (adb is started ?)")
            }
        }
        }
    }

    fun watchChanged(): Observable<DeviceEvent> {
        return onChangedObservable
    }
}