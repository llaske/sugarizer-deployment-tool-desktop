package com.sugarizer.utils.shared

import com.google.gson.Gson
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.model.InstallApkModel
import com.sugarizer.model.Instruction
import com.sugarizer.model.InstructionsModel
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipInUtils(val name: String, val instructionsModel: InstructionsModel) {
    var zipFile = File(name)
    var zipStream = FileOutputStream(zipFile)
    var zipInstruction = ZipEntry("instructions.json")
    var zipOut = ZipOutputStream(zipStream)
    var instruction =  InstructionsModel()

    init {
        instruction.intructions = mutableListOf()

        zipOut.putNextEntry(ZipEntry("apks\\"))
        zipOut.closeEntry()
        zipOut.putNextEntry(ZipEntry("files\\"))
        zipOut.closeEntry()
    }

    fun startZiping(){
        instructionsModel.intructions?.forEach {
            when (it.type) {
                ListItemInstruction.Type.APK -> { zipInstallApk(Gson().fromJson(it.data, InstallApkModel::class.java), it?.ordre!!) }
                ListItemInstruction.Type.PUSH -> {  }
                ListItemInstruction.Type.DELETE -> {  }
                ListItemInstruction.Type.SWIPE, ListItemInstruction.Type.TEXT,
                ListItemInstruction.Type.KEY, ListItemInstruction.Type.LONGCLICK,
                ListItemInstruction.Type.CLICK, ListItemInstruction.Type.SLEEP
                -> { (instruction.intructions as MutableList).add(it) }
            }
        }
    }

    fun finishZip(){
        zipOut.putNextEntry(zipInstruction)

        var data = Gson().toJson(instruction, InstructionsModel::class.java).toByteArray()

        zipOut.write(data, 0, data.size)
        zipOut.closeEntry()

        zipOut.close()
    }

    fun copy(file: File, outputStream: OutputStream){
        var input = FileInputStream(file);
        try {
            copy(input, outputStream);
        } finally {
            input.close();
        }
    }

    @Throws(IOException::class)
    private fun copy(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        while (true) {
            val readCount = `in`.read(buffer)
            if (readCount < 0) {
                break
            }
            out.write(buffer, 0, readCount)
        }
    }

    private fun zipInstallApk(model: InstallApkModel, ordre: Int){
        var installApk = InstallApkModel()

        installApk.numberApk = model.apks?.size
        installApk.apks = mutableListOf()

        model.apks?.forEach {
            var file = File(it)

            zipOut.putNextEntry(ZipEntry("apks\\" + file.name))

            (installApk.apks as MutableList<String>)?.add("apks\\" + file.name)

            copy(file, zipOut)
            zipOut.closeEntry()
        }

        var tmp = Instruction()

        tmp.data = Gson().toJson(installApk)
        tmp.type = ListItemInstruction.Type.APK
        tmp.ordre = ordre

        (instruction.intructions as MutableList).add(tmp)
    }
}