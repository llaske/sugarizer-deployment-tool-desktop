package com.sugarizer.domain.model

import com.google.gson.annotations.SerializedName

class InstructionsModel {
    enum class Type {
        INTALL_APK,
        PUSH_FILE,
        DELETE_FILE
    }

    @SerializedName("instrcutions")
    var intructions: List<Instruction>? = null
}

class Instruction {
    @SerializedName("type")
    var type: InstructionsModel.Type? = null

    @SerializedName("ordre")
    var ordre: Int? = null

    @SerializedName("data")
    var data: String? = null
}

class InstallApkModel {
    @SerializedName("number_apk")
    var numberApk: Int? = 0

    @SerializedName("apks")
    var apks: List<String>? = null
}