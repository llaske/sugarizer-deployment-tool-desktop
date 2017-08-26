package com.sugarizer.model

import se.vidstige.jadb.JadbDevice

class DeviceEventModel(status: DeviceEventModel.Status, device: JadbDevice) {
    val status: DeviceEventModel.Status = status
    val device: JadbDevice = device

    enum class Status {
        REMOVED,
        ADDED,
        CHANGED,
        UNAUTHORIZED,
        IDLE
    }
}