package com.sugarizer.model

class DeviceEventModel(status: DeviceEventModel.Status, device: DeviceModel) {
    val status: DeviceEventModel.Status = status
    val device: DeviceModel = device

    enum class Status {
        REMOVED,
        ADDED,
        CHANGED,
        UNAUTHORIZED
    }
}