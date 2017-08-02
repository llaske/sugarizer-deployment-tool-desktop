package com.sugarizer.utils.shared

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sugarizer.model.InstructionsModel
import io.reactivex.Observable
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipOutUtils {
    enum class STATUS {
        NOT_COMPLETE,
        IN_PROGRESS,
        COMPLETE
    }

    private val BUFFER_SIZE = 4096

    var status = STATUS.NOT_COMPLETE
    var instruction: InstructionsModel? = null

    init {

    }

    fun loadZip(path: String): Observable<STATUS> {
        status = STATUS.NOT_COMPLETE

        return Observable.create {
            it.onNext(STATUS.IN_PROGRESS)

            unzip(path, "tmp")

            val turnsType = object : TypeToken<InstructionsModel>() {}.type
            instruction = Gson().fromJson(getStringFromFile("tmp\\instructions.json"), turnsType)

            println(getStringFromFile("tmp\\instructions.json"))

            it.onNext(STATUS.COMPLETE)
            it.onComplete()
        }
    }

    @Throws(IOException::class)
    fun unzip(zipFilePath: String, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        val zipIn = ZipInputStream(FileInputStream(zipFilePath))
        var entry: ZipEntry? = zipIn.nextEntry
        // iterates over entries in the zip file
        while (entry != null) {
            val filePath = destDirectory + File.separator + entry.name
            println(filePath)
            if (entry.isDirectory || filePath[filePath.lastIndex].toString() == "\\") {
                println("Directory")
                // if the entry is a directory, make the directory
                val dir = File(filePath)
                dir.mkdir()
            } else {
                println("File")
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath)
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()
    }

    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        var file = File(filePath)

        File(file.parent).mkdirs()

        val bos = BufferedOutputStream(FileOutputStream(filePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read = zipIn.read(bytesIn)
        while (read != -1) {
            bos.write(bytesIn, 0, read)

            read = zipIn.read(bytesIn)
        }
        bos.close()
    }

    fun getStringFromFile(file: String): String {
        var input: InputStream = FileInputStream(file)
        var buf: BufferedReader = BufferedReader(InputStreamReader(input))
        var sb: java.lang.StringBuilder = StringBuilder()

        for (line in buf.readLines()) {
            sb.append(line).append("\n")
        }

        System.out.println("Contents : " + sb.toString())

        return sb.toString()
    }

    fun deleteTmp(){
    }
}