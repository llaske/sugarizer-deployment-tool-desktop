package utils

import io.reactivex.rxkotlin.toObservable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import model.Device
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbException
import java.net.ConnectException

class JADB {
    var list: ObservableList<Device> = FXCollections.observableArrayList()

    init {
        try {
            val connection = JadbConnection()

            connection.devices.forEach { device ->
                run {
                    try {
                        println("Name: " + device.serial)
                        println("State: " + device.state)
                        list.add(Device(device))
                    } catch (e: JadbException) {
                        println("Error: device offline")
                    }
                }
            }

            list.toObservable()
        } catch (e: ConnectException){
            println("Error: connection refuse")
        }
    }

    fun getDevices(): ObservableList<Device> {
        return list
    }

    fun numberDevice(): Int {
        return list.size
    }

    fun changeAction(device: Device, action: String) {
        if (list.contains(device)) {
            changeAction(list.indexOf(device), action)
        }
    }

    fun changeAction(name: String, action: String) {
        for ((value, device) in list.withIndex()) {
            if (device.name.get().equals(name)) {
                changeAction(list[value], action)
            }
        }
    }

    fun changeAction(index: Int, action: String) {
        if (list.size > index) {
            println("Gone change action of device: " + list[index].name.get())
            list[index].setAction(action)
        }
    }
}