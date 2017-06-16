package com.sugarizer.domain.shared

import com.google.gson.Gson
import com.sugarizer.domain.model.InstallApkModel
import com.sugarizer.domain.model.InstructionsModel
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipInUtils(val name: String, val instructionsModel: InstructionsModel<*>) {
    var zipFile = File(name)
    var zipStream = FileOutputStream(zipFile)
    var zipInstruction = ZipEntry("instructions.json")
    var zipOut = ZipOutputStream(zipStream)

    init {
        zipOut.putNextEntry(ZipEntry("apks\\"))
        zipOut.closeEntry()
        zipOut.putNextEntry(ZipEntry("files\\"))
        zipOut.closeEntry()

        zipOut.putNextEntry(zipInstruction)

        var data = Gson().toJson(instructionsModel, InstructionsModel::class.java).toByteArray()

        zipOut.write(data, 0, data.size)
        zipOut.closeEntry()
    }

    fun startZiping(){
        instructionsModel.intructions?.forEach {
            when (it.type) {
                InstructionsModel.Type.INTALL_APK -> { zipInstallApk(it.data as InstallApkModel) }
                InstructionsModel.Type.PUSH_FILE -> {  }
                InstructionsModel.Type.DELETE_FILE -> {  }
            }
        }
    }

    fun finishZip(){
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

    private fun zipInstallApk(model: InstallApkModel){
        model.apks?.forEach {
            var file = File(it)

            zipOut.putNextEntry(ZipEntry("apks\\" + file.name))
            copy(file, zipOut)
            zipOut.closeEntry()
        }
    }
}