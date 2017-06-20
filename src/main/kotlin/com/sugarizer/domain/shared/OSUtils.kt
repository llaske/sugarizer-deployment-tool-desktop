package com.sugarizer.domain.shared


object OsCheck {
    enum class OSType {
        Windows, MacOS, Linux, Other
    }

    var detectedOS: OSType? = null

    val operatingSystemType: OSType
        get() {
            if (detectedOS == null) {
                val OS = System.getProperty("os.name", "generic").toLowerCase(java.util.Locale.ENGLISH)
                if (OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0) {
                    detectedOS = OSType.MacOS
                } else if (OS.indexOf("win") >= 0) {
                    detectedOS = OSType.Windows
                } else if (OS.indexOf("nux") >= 0) {
                    detectedOS = OSType.Linux
                } else {
                    detectedOS = OSType.Other
                }
            }
            return detectedOS!!
        }
}