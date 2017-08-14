package com.sugarizer.utils.shared

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sugarizer.model.InstructionsModel
import io.reactivex.Observable
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipOutUtils(val fileUtils: FileUtils) {
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
            instruction = Gson().fromJson(getStringFromFile("tmp" + fileUtils.separator + "instructions.json"), turnsType)

            it.onNext(STATUS.COMPLETE)

            Thread.sleep(1000)

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
        while (entry != null) {
            val filePath = destDirectory + File.separator + entry.name
            if (entry.isDirectory || filePath[filePath.lastIndex].toString() == fileUtils.separator) {
                val dir = File(filePath)
                dir.mkdir()
            } else {
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

        return sb.toString()
    }

    fun deleteTmp(){
    }
}