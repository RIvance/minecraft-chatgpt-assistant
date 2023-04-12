package org.ivance.gptassistant.config

import java.io.File
import java.util.*

data class AssistantConfig(
    var token: String = "",
    var proxy: String? = null,
    var timeoutSecond: Long = 0L,
    var model: String,
    var requestConfig: RequestConfig = RequestConfig.DEFAULT,
) {

    constructor(properties: Properties) : this (
        token = properties.getProperty("api-key")!!,
        proxy = properties.getProperty("proxy")!!.let { it.ifEmpty { null } },
        timeoutSecond = properties.getProperty("timeoutSecond")!!.toLong(),
        model = properties.getProperty("model")!!,
        requestConfig = RequestConfig(
            maxTokens = properties.getProperty("maxTokens")!!.toInt(),
            temperature = properties.getProperty("temperature")!!.toDouble(),
            topP = properties.getProperty("topP")!!.toDouble(),
            frequencyPenalty = properties.getProperty("frequencyPenalty")!!.toDouble(),
            presencePenalty = properties.getProperty("presencePenalty")!!.toDouble(),
        )
    )

    fun token(token: String) = copy(token = token)
    fun proxy(proxy: String?) = copy(proxy = proxy)
    fun timeoutSecond(timeoutSecond: Long) = copy(timeoutSecond = timeoutSecond)
    fun requestConfig(requestConfig: RequestConfig) = copy(requestConfig = requestConfig)
    fun model(model: String) = copy(model = model)
    fun maxTokens(maxTokens: Int) = copy(requestConfig = requestConfig.maxTokens(maxTokens))
    fun temperature(temperature: Double) = copy(requestConfig = requestConfig.temperature(temperature))
    fun topP(topP: Double) = copy(requestConfig = requestConfig.topP(topP))
    fun frequencyPenalty(frequencyPenalty: Double) = copy(requestConfig = requestConfig.frequencyPenalty(frequencyPenalty))
    fun presencePenalty(presencePenalty: Double) = copy(requestConfig = requestConfig.presencePenalty(presencePenalty))

    fun saveToFile() {
        File(CONFIG_FILE_PATH).also { file ->
            if (!file.exists()) createDefaultConfigFile(file)
            Properties().also {
                it.setProperty("api-key", token)
                it.setProperty("proxy", proxy ?: "")
                it.setProperty("timeoutSecond", timeoutSecond.toString())
                it.setProperty("model", model)
                it.setProperty("maxTokens", requestConfig.maxTokens.toString())
                it.setProperty("temperature", requestConfig.temperature.toString())
                it.setProperty("topP", requestConfig.topP.toString())
                it.setProperty("frequencyPenalty", requestConfig.frequencyPenalty.toString())
                it.setProperty("presencePenalty", requestConfig.presencePenalty.toString())
            }.store(file.outputStream(), "GPT Assistant Config")
        }
    }

    companion object {

        private const val CONFIG_FILE_PATH = "config/gptassistant.properties"

        private fun createDefaultConfigFile(file: File): AssistantConfig {
            file.run {
                if (this.exists()) this.delete()
                this.parentFile.mkdirs()
                this.createNewFile()
            }
            val properties = Properties().also {
                it.setProperty("api-key", "")
                it.setProperty("proxy", "")
                it.setProperty("timeoutSecond", "0")
                it.setProperty("model", "text-davinci-003")
                it.setProperty("maxTokens", "128")
                it.setProperty("temperature", "0.9")
                it.setProperty("topP", "1.0")
                it.setProperty("frequencyPenalty", "0.0")
                it.setProperty("presencePenalty", "0.0")
                it.store(file.outputStream(), "GPT Assistant Config")
            }
            return AssistantConfig(properties)
        }

        fun loadFromFile(): AssistantConfig {
            val properties = Properties()
            val file = File(CONFIG_FILE_PATH)
            return if (file.exists()) {
                try {
                    properties.load(file.inputStream())
                    AssistantConfig(properties)
                } catch (e: Exception) {
                    createDefaultConfigFile(file)
                }
            } else {
                createDefaultConfigFile(file)
            }
        }
    }
}