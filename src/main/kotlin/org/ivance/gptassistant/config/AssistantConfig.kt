package org.ivance.gptassistant.config

import org.ivance.gptassistant.core.CompletionAssistantModel

interface AssistantConfig {

    val token: String
    val proxy: String?
    val timeoutSecond: Long
    val model: String
    val requestConfig: RequestConfig

    companion object {
        val DEFAULT = object : AssistantConfig {
            override val token: String = ""
            override val proxy: String? = null
            override val timeoutSecond: Long = 0L
            override val model: String = CompletionAssistantModel.Ident.DAVINCI_001.toString()
            override val requestConfig: RequestConfig = RequestConfig.DEFAULT
        }
    }
}
