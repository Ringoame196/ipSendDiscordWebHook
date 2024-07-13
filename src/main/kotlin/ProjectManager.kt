package org.example

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

class ProjectManager {
    fun loadProperties(filePath: File): Properties {
        val properties = Properties()
        try {
            FileInputStream(filePath).use { fileInput ->
                properties.load(fileInput)
            }
        } catch (e: IOException) {
            println("エラー: プロパティファイルの読み込みに失敗しました。${e.message}")
        }
        return properties
    }
    fun makeProjectFile(filePath: File) {
        filePath.writeText("webhookURL=null")
    }
}