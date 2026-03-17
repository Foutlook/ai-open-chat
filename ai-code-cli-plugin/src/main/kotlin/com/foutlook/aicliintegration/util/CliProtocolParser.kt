package com.foutlook.aicliintegration.util

import com.foutlook.aicliintegration.model.CliResponse
import com.foutlook.aicliintegration.model.FileChange
import com.foutlook.aicliintegration.model.FileOperation
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * CLI 协议解析器
 */
object CliProtocolParser {
    private val gson = Gson()

    /**
     * 解析 JSON 格式的 CLI 响应
     */
    fun parseJsonResponse(json: String): CliResponse {
        return try {
            val jsonObject = gson.fromJson(json, JsonObject::class.java)
            
            val success = jsonObject.get("success")?.asBoolean ?: false
            val message = jsonObject.get("message")?.asString ?: ""
            val error = jsonObject.get("error")?.asString
            
            val changedFiles = mutableListOf<FileChange>()
            jsonObject.getAsJsonArray("changed_files")?.forEach { element ->
                val fileObj = element.asJsonObject
                val path = fileObj.get("path").asString
                val operation = FileOperation.valueOf(fileObj.get("operation").asString.uppercase())
                val content = fileObj.get("content")?.asString
                val diff = fileObj.get("diff")?.asString
                
                changedFiles.add(FileChange(path, operation, content, diff))
            }
            
            val suggestions = mutableListOf<String>()
            jsonObject.getAsJsonArray("suggestions")?.forEach { element ->
                suggestions.add(element.asString)
            }
            
            CliResponse(success, message, changedFiles, suggestions, error)
        } catch (e: Exception) {
            CliResponse(false, "Failed to parse response", error = e.message)
        }
    }

    /**
     * 解析纯文本格式的 CLI 响应
     */
    fun parseTextResponse(text: String): CliResponse {
        return CliResponse(true, text)
    }

    /**
     * 格式化 JSON 请求
     */
    fun formatJsonRequest(action: String, params: Map<String, Any>): String {
        val jsonObject = JsonObject()
        jsonObject.addProperty("action", action)
        params.forEach { (key, value) ->
            when (value) {
                is String -> jsonObject.addProperty(key, value)
                is Number -> jsonObject.addProperty(key, value)
                is Boolean -> jsonObject.addProperty(key, value)
                else -> jsonObject.addProperty(key, value.toString())
            }
        }
        return gson.toJson(jsonObject)
    }
}
