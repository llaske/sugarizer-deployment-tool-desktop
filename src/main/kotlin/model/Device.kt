package model

import javafx.beans.property.SimpleStringProperty
import se.vidstige.jadb.JadbDevice

class Device(device: JadbDevice) {
    val name = SimpleStringProperty("", "name", device.serial)
    val status = SimpleStringProperty("", "status", device.state.toString())
    val action = SimpleStringProperty("", "action", "Nothing")

    fun getName(): String {
        return name.get()
    }

    fun getStatus(): String {
        return if (status.get() == null) "Offline" else status.get()
    }

    fun getAction(): String {
        return if (action.get() == null) "Nothing" else action.get()
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
}