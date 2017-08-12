package com.sugarizer.utils.shared

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sugarizer.BuildConfig
import com.sugarizer.model.InstructionsModel
import io.reactivex.Observable
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class FileUtils {

    val separator = when (OsCheck.operatingSystemType) {
        OsCheck.OSType.Windows -> {
            BuildConfig.FILE_SEPARATOR_WINDOWS
        }
        OsCheck.OSType.Linux -> {
            BuildConfig.FILE_SEPARATOR_LINUX
        }
        OsCheck.OSType.MacOS -> {
            BuildConfig.FILE_SEPARATOR_MAC
        }
        else -> {
            BuildConfig.FILE_SEPARATOR_LINUX
        }
    }
}