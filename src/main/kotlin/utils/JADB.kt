package utils

import io.reactivex.rxkotlin.toObservable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import model.Device
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.JadbException
import java.net.ConnectException
import java.util.*

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
                    .subscribe()


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
}