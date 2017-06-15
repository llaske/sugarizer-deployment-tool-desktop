package com.sugarizer.domain.model

import com.google.gson.annotations.SerializedName

class InstructionsModel<T> where T : Model{
    enum class Type {
        INTALL_APK,
        PUSH_FILE,
        DELETE_FILE
    }

    @SerializedName("instrcutions")
    var intructions: List<Instruction<T>>? = null
}

class Instruction<T> where T : Model{
    @SerializedName("type")
    var type: InstructionsModel.Type? = null

    @SerializedName("ordre")
    var ordre: Int? = null

    @SerializedName("data")
    var data: T? = null
}

class InstallApkModel : Model() {
    @SerializedName("number_apk")
    var numberApk: Int? = 0

    @SerializedName("apks")
    var apks: List<String>? = null
}

abstract class Model {

}