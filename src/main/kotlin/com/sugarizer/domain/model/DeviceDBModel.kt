package com.sugarizer.domain.model

class DeviceDBModel(name: String, serial: String, model: String, version: String) {
    companion object {
        val NAME_TABLE = "devices"
        val DEVICE_ID = "music_id"
        val DEVICE_NAME = "music_name"
        val DEVICE_SERIAL = "device_serial"
        val DEVICE_UDID = "device_udid"
        val DEVICE_MODEL = "device_model"
        var DEVICE_VERSION_NAME = "device_version_name"

        val sqlCreate = "CREATE TABLE IF NOT EXISTS $NAME_TABLE " +
                "($DEVICE_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $DEVICE_NAME TEXT NOT NULL," +
                " $DEVICE_SERIAL TEXT NOT NULL," +
                " $DEVICE_UDID TEXT," +
                " $DEVICE_MODEL TEXT NOT NULL," +
                " $DEVICE_VERSION_NAME INT NOT NULL)"
    }

    var deviceID: Int = -1
    var deviceName: String = name
    var deviceSerial: String = serial
    var deviceUDID: String = ""
    var deviceModel: String = model
    var deviceVersionName: String = version
}