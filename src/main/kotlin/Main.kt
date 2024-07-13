package org.example

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

fun main() {
    val webHookURL = loadWebHookURL() // webhookURLを取得する
    val apiURL = URL("https://api.ipify.org") // グローバルIPを取得するためのapiのURL
    if (webHookURL == null) {
        println("Tokenが設定されていません")
        return
    }
    val globalIP = acquisitionGlobalIP(apiURL) // グローバルIP取得
    sendDiscordWebHook(webHookURL, globalIP)
}

private fun loadWebHookURL():URL? {
    val projectManager = ProjectManager()
    val propertiesFile = File("./webhook.properties")
    if (propertiesFile.exists()) {
        val projectFile = projectManager.loadProperties(propertiesFile) // プロジェクトファイルを読み込む
        val webhookURL = projectFile.getProperty("webhookURL")
        return if (webhookURL == "null" || webhookURL == "") {
            null
        } else {
            URL(webhookURL) // WebHookURLを返す
        }
    } else {
        // ファイルがなければ生成する
        projectManager.makeProjectFile(propertiesFile)
        return null
    }
}

private fun acquisitionGlobalIP(apiURL:URL):String {
    return apiURL.readText() // グローバルIPをapiで取得
}

private fun sendDiscordWebHook(webHookURL: URL, globalIP: String) {
    val connection = webHookURL.openConnection() as HttpURLConnection

    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json; utf-8")
    connection.doOutput = true

    val redColor = 0x008000 // 16進数の緑色を整数で表現

    val jsonPayload = """
        {
            "embeds": [
                {
                    "title": "IP表示",
                    "description": "$globalIP",
                    "color": $redColor
                }
            ]
        }
    """.trimIndent()

    try {
        connection.outputStream.use { os ->
            val input = jsonPayload.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }

        val responseCode = connection.responseCode
        if (responseCode == 204) {
            println("Webhook sent successfully.")
        } else {
            println("Failed to send webhook. Response code: $responseCode")
            connection.errorStream.bufferedReader().use {
                val responseBody = it.readText()
                println("Error response: $responseBody")
            }
        }
    } catch (e: IOException) {
        println("An error occurred while sending the webhook: ${e.message}")
    } finally {
        connection.disconnect()
    }
}