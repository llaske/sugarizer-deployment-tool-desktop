package model

class DeviceEvent(status: Status, device: Device) {
    val status: Status = status
    val device: Device = device

    enum class Status {
        REMOVED,
        ADDED,
        CHANGED
    }
}