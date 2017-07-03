package com.sugarizer.domain.model

import com.google.gson.annotations.SerializedName

class InstructionsModel {
    enum class Type {
        INTALL_APK,
        PUSH_FILE,
        DELETE_FILE,
        INSTRUCTION_KEY,
        INSTRUCTION_CLICK,
        INSTRUCTION_LONG_CLICK,
        INSTRUCTION_SWIPE,
        INSTRUCTION_TEXT,
        SLEEP
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

class KeyModel(idKey: Int = 0) {
    @SerializedName("id_key")
    var idKey: Int = idKey
}

class ClickModel(x: Int = 0, y: Int = 0) {
    @SerializedName("x")
    var x: Int = x

    @SerializedName("y")
    var y: Int = y
}

class LongClickModel(x: Int = 0, y: Int = 0, duration: Int = 3000) {
    @SerializedName("x")
    var x: Int = x

    @SerializedName("y")
    var y: Int = y

    @SerializedName("duration")
    var duration: Int = duration
}

class SwipeModel(x1: Int = 0, y1: Int = 0, x2: Int = 0, y2: Int = 0, duration: Int = 3000) {
    @SerializedName("x1")
    var x1: Int = x1

    @SerializedName("y1")
    var y1: Int = y1

    @SerializedName("x2")
    var x2: Int = x2

    @SerializedName("y2")
    var y2: Int = y2

    @SerializedName("duration")
    var duration: Int = duration
}

class TextModel(text: String = "") {
    @SerializedName("text")
    var text: String = text
}

class SleepModel(sleep: Long = 3000) {
    @SerializedName("duration")
    var duration: Long = sleep
}