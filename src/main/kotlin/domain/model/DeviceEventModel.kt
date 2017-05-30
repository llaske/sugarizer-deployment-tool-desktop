package domain.model

class DeviceEventModel(status: domain.model.DeviceEventModel.Status, device: DeviceModel) {
    val status: domain.model.DeviceEventModel.Status = status
    val device: DeviceModel = device

    enum class Status {
        REMOVED,
        ADDED,
        CHANGED
    }
}