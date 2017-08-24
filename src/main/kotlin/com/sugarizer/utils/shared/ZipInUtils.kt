package com.sugarizer.utils.shared

import com.google.gson.Gson
import com.sugarizer.listitem.ListItemChoosenInstruction
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.model.InstallApkModel
import com.sugarizer.model.Instruction
import com.sugarizer.model.InstructionsModel
import com.sugarizer.view.createinstruction.CreateInstructionView
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class ZipInUtils(val name: String, val fileUtils: FileUtils, val listInstruction: MutableList<ListItemChoosenInstruction>) {
    var zipFile = File(name)
    var zipStream = FileOutputStream(zipFile)
    var zipInstruction = ZipEntry("instructions.json")
    var zipOut = ZipOutputStream(zipStream)
    var instruction =  InstructionsModel()
    var tmpInstruction = mutableListOf<Instruction>()

    init {

    }

    fun startZiping(): Observable<Any> {
        return Observable.create<Any> { subscriber ->
            instruction.intructions = mutableListOf()
            instruction.intructions.clear()

            println("Size Instr: " + instruction.intructions.size)
            listInstruction.forEach {
                println("Do I go there ?")
                tmpInstruction.add(it.instruction)
            }

            zipOut.putNextEntry(ZipEntry("apks" + fileUtils.separator))
            zipOut.closeEntry()
            zipOut.putNextEntry(ZipEntry("files" + fileUtils.separator))
            zipOut.closeEntry()

            tmpInstruction.let {
                for (tmp in it) {
                    println("INSTRUCTION NOT NULL ?")
                    tmp.ordre?.let {
                        println("Type:" + tmp.type)
                        when (tmp.type) {
                            CreateInstructionView.Type.APK -> { zipInstallApk(Gson().fromJson(tmp.data, InstallApkModel::class.java), it) }
                            CreateInstructionView.Type.SWIPE, CreateInstructionView.Type.TEXT,
                            CreateInstructionView.Type.KEY, CreateInstructionView.Type.LONGCLICK,
                            CreateInstructionView.Type.CLICK, CreateInstructionView.Type.SLEEP,
                            CreateInstructionView.Type.OPENAPP, CreateInstructionView.Type.DELETE -> {
                                (instruction.intructions as MutableList).add(tmp) }
                            else -> { }
                        }
                        println("FINISH TYPE")
                    }
                    println("FINISH CYCLE")
                }

                println("FINISH")
                subscriber.onComplete()
            }
        }
    }

    fun finishZip(){
        println("FINISH ZIPPING")
        zipOut.putNextEntry(zipInstruction)

        var data = Gson().toJson(instruction, InstructionsModel::class.java).toByteArray()

        zipOut.write(data, 0, data.size)
        zipOut.closeEntry()

        zipOut.close()
        println("ZIP FINISHED")
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

        println("Est-ce que je passe la ?")
        installApk.numberApk = model.apks?.size
        installApk.apks = mutableListOf()

        model.apks?.forEach {
            var file = File(it)

            println("apks" + fileUtils.separator + file.name)
            zipOut.putNextEntry(ZipEntry("apks" + fileUtils.separator + file.name))

            (installApk.apks as MutableList<String>)?.add("apks" + fileUtils.separator + file.name)

            copy(file, zipOut)
            zipOut.closeEntry()
            println("END ARCHIVE")
        }

        var tmp = Instruction()

        tmp.data = Gson().toJson(installApk)
        tmp.type = CreateInstructionView.Type.APK
        tmp.ordre = ordre

        (instruction.intructions as MutableList).add(tmp)

        println("END ZIP APK")
    }
}