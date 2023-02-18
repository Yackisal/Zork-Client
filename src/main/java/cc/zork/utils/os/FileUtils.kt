package cc.zork.utils.os

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

object FileUtils {
    @JvmStatic
    fun readFile(file: File): String {
        val result = StringBuilder()
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val fIn = FileInputStream(file)
            BufferedReader(InputStreamReader(fIn)).use { bufferedReader ->
                var str: String?
                while (bufferedReader.readLine().also { str = it } != null) {
                    result.append(str)
                    result.append(System.lineSeparator())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result.toString()
    }

    fun readFile(fileName: String?): String {
        val file = File(fileName).absoluteFile
        return readFile(file)
    }

    fun saveFile(fileName: String?, context: String?) {
        val file = File(fileName).absoluteFile
        try {
            if (!file.exists()) {
                Files.createDirectories(file.parentFile.toPath())
                file.createNewFile()
            }

            Files.write(
                Paths.get(fileName),
                context!!.toByteArray(StandardCharsets.UTF_8)
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun readInputStream(inputStream: InputStream?): String {
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) stringBuilder.append(line).append('\n')
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }
}