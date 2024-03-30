package ddcMan

import com.formdev.flatlaf.util.SystemInfo
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

fun getPlatformDataDir(): String {
    if (SystemInfo.isMacOS) {
        val dir = System.getProperty("user.home") + "/Library/Application Support/DDCMan"
        Files.createDirectories(Paths.get(dir))
        return dir
    } else if (SystemInfo.isWindows) {
        val dir = System.getenv("LOCALAPPDATA") + "\\DDCMan"
        Files.createDirectories(Paths.get(dir))
        return dir
    }
    val dir = System.getProperty("user.home") + "/.DDCMan"
    Files.createDirectories(Paths.get(dir))
    return dir
}

fun extractScriptToDataFolder() {
    val applicationSupportDir = getPlatformDataDir()
    val beautify = object {}.javaClass.getResourceAsStream("/js/beautify.js")
    val file = File("$applicationSupportDir/js/beautify.js")
    file.parentFile.mkdirs()
    val writer = FileOutputStream(file)
    IOUtils.copy(beautify, writer)
    beautify?.close()
    writer.close()
}