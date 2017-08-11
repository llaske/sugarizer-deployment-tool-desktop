package com.sugarizer.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.view.createinstruction.CreateInstructionView
import java.io.File

class InstructionsModel {
    @SerializedName("instructions")
    var intructions: List<Instruction>? = null
}

class Instruction {
    @SerializedName("type")
    var type: CreateInstructionView.Type? = null

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

    fun toInstruction(order: Int, choosedDirectory: File): Instruction {
        var instructionModel: Instruction = Instruction()
        var listApk: MutableList<String> = mutableListOf()

        choosedDirectory.listFiles()
                .filter { it.isFile && it.extension.equals("apk") }
                .mapTo(listApk) { it.absolutePath }

        this.numberApk = listApk.size
        this.apks = listApk

        instructionModel.data = Gson().toJson(this)
        instructionModel.ordre = order
        instructionModel.type = CreateInstructionView.Type.APK

        return instructionModel
    }
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