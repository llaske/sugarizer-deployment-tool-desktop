package com.sugarizer.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.utils.shared.FileUtils
import com.sugarizer.view.createinstruction.CreateInstructionView
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.text.Normalizer
import javax.inject.Inject

class InstructionsModel {
    @SerializedName("instructions")
    var intructions: MutableList<Instruction> = mutableListOf()
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
    @Inject lateinit var fileUtils: FileUtils

    @SerializedName("number_apk")
    var numberApk: Int? = 0

    @SerializedName("apks")
    var apks: List<String>? = null

    fun toInstruction(order: Int, choosedDirectory: List<File>): Instruction {
        Main.appComponent.inject(this)
        var instructionModel: Instruction = Instruction()
        var listApk: MutableList<String> = mutableListOf()

        choosedDirectory.forEach {
            println("Path: " + it.absolutePath.substring(0, it.absolutePath.lastIndexOf(fileUtils.separator)) + fileUtils.separator + StringUtils.stripAccents(it.name))
            var file = File(it.absolutePath.substring(0, it.absolutePath.lastIndexOf(fileUtils.separator)) + fileUtils.separator + StringUtils.stripAccents(it.name))
            it.renameTo(file)
            listApk.add(file.absolutePath)
        }

        this.numberApk = listApk.size
        this.apks = listApk

        instructionModel.data = Gson().toJson(this)
        instructionModel.ordre = order
        instructionModel.type = CreateInstructionView.Type.APK

        return instructionModel
    }
}

class KeyModel(idKey: Int = 0, textKey: String) {
    @SerializedName("id_key")
    var idKey: Int = idKey

    @SerializedName("text_ket")
    var textKey: String = textKey
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

class OpenAppModel(text: String = "") {
    @SerializedName("package")
    var package_name: String = text
}

class DeleteFileModel(file: String = "") {
    @SerializedName("file_name")
    var file_name: String = file
}