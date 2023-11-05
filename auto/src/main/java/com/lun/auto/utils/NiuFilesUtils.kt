package com.lun.auto.utils

import android.content.Context
import android.os.Environment
import java.io.*

class NiuFilesUtils {
    companion object {
        @JvmStatic
        fun write(path: String, content: String) {
            val file = File(path)
            file.parentFile?.mkdirs() // 创建目录
            val writer = FileWriter(file)
            writer.write(content)
            writer.flush()
            writer.close()
        }

        fun writeFile(filePath: String, content: String) {
            val file = File(filePath)
            val fos = FileOutputStream(file)
            val writer = BufferedWriter(OutputStreamWriter(fos))
            writer.write(content)
            writer.close()
        }

        fun readFile(filePath: String): String? {
            val file = File(filePath)
            return if (file.exists()) {
                val bufferedReader = file.bufferedReader()
                val content = bufferedReader.use { it.readText() }
                content
            } else {
                println("File not found")
                null
            }
        }

        /* 判断文件是否存在 */
        fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        /* 向指定路径的文件中追加内容 */
        fun appendFile(filePath: String, content: String) {
            val fileWriter = FileWriter(filePath, true)
            fileWriter.use {
                it.write(content)
            }
        }

        /* 创建一个文件或文件夹并返回是否创建成功。如果文件所在文件夹不存在，则先创建他所在的一系列文件夹。如果文件已经存在，则直接返回false */
        fun createFileWithDirs(path: String): Boolean {
            val file = File(path)
            return try {
                if (path.endsWith("/")) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.createNewFile()
                }
            } catch (e: Exception) {
                false
            }
        }

        /* 删除文件或空文件夹，返回是否删除成功。 */
        @JvmStatic
        fun removeFile(filePath: String): Boolean {
            val file = File(filePath)
            return try {
                file.delete()
            } catch (e: IOException) {
                false
            }
        }

        /* 删除文件夹，如果文件夹不为空，则删除该文件夹的所有内容再删除该文件夹，返回是否全部删除成功。 */
        fun removeFileDir(directoryPath: String): Boolean {
            val directory = File(directoryPath)
            if (!directory.exists()) {
                return false
            }
            return try {
                directory.deleteRecursively()
                true
            } catch (e: IOException) {
                false
            }
        }

        /* 把bytes写入到文件path中。如果文件存在则覆盖，不存在则创建。 */
        fun writeBytesToFile(filePath: String, bytes: ByteArray): Boolean {
            return try {
                val outputStream = FileOutputStream(filePath, false) // 打开文件时启用覆盖模式
                outputStream.write(bytes)
                outputStream.close()
                true
            } catch (e: Exception) {
                false
            }
        }

        fun moveFile(sourceFilePath: String, targetFilePath: String): Boolean {
            val sourceFile = File(sourceFilePath)
            val targetFile = File(targetFilePath)
            if (sourceFile.exists() && sourceFile.isFile) {
                if (targetFile.exists()) {
                    // 如果目标文件存在，则尝试先删除
                    targetFile.delete()
                }
                // 移动文件
                return sourceFile.renameTo(targetFile)
            }
            return false
        }

        fun copyFile(sourceFilePath: String, targetFilePath: String): Boolean {
            val sourceFile = File(sourceFilePath)
            val targetFile = File(targetFilePath)
            if (sourceFile.exists() && sourceFile.isFile) {
                if (targetFile.exists()) {
                    // 如果目标文件存在，则尝试先删除
                    targetFile.delete()
                }
                // 创建输入流和输出流
                val input = FileInputStream(sourceFile)
                val output = FileOutputStream(targetFile)
                // 复制文件
                input.copyTo(output)
                // 关闭输入流和输出流
                input.close()
                output.close()
                return true
            }
            return false
        }

        fun cwd(context: Context): String {
            return File(context.externalCacheDir, Environment.DIRECTORY_DOWNLOADS).toString()
        }
    }
}