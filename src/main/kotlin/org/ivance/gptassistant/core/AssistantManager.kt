package org.ivance.gptassistant.core

import org.apache.logging.log4j.Logger
import org.ivance.gptassistant.config.AssistantConfig
import java.net.InetSocketAddress
import java.net.Proxy

class AssistantManager(private val logger: Logger) {

    private val modelBuilders = mutableMapOf<String, AssistantModel.Builder>().also {
        ChatAssistantModel.Ident.values().forEach { ident ->
            it[ident.toString()] = ChatAssistantModel.builder(ident)
        }
        CompletionAssistantModel.Ident.values().forEach { ident ->
            it[ident.toString()] = CompletionAssistantModel.builder(ident)
        }
    }.toMap()

    var config: AssistantConfig = AssistantConfig.loadFromFile()
        set(value) = value.let { config ->
            if (field != config) {
                field = config
                this.service = createServiceFromConfig(config)
                try {
                    config.saveToFile()
                } catch (exception: Exception) {
                    logger.error("Failed to save config to file", exception)
                }
                service = service?.copy(
                    token = config.token,
                    timeoutSecond = config.timeoutSecond,
                    proxy = config.proxy?.let(AssistantManager::parseProxy),
                    modelBuilder = modelBuilders[config.model]!!,
                    requestConfig = config.requestConfig,
                )
            }
        }

    var service: AssistantService? = createServiceFromConfig(config)
        private set

    private fun createServiceFromConfig(config: AssistantConfig): AssistantService? {
        return if (config.token.isNotBlank()) {
            AssistantService(
                token = config.token,
                logger = logger,
                timeoutSecond = config.timeoutSecond,
                proxy = config.proxy?.let(::parseProxy),
                requestConfig = config.requestConfig
            )
        } else null
    }

    companion object {

        private fun parseProxyAddress(address: String): Pair<String, UShort> {
            val parts = address.trim().split(':')
            if (parts.size != 2) {
                throw IllegalArgumentException("Invalid proxy address: $address")
            }
            val host = parts[0].trim()
            val port = parts[1].trim().toUShort()
            return Pair(host, port)
        }

        private fun parseProxy(address: String): Proxy {
            return if (address.startsWith("http://") || address.startsWith("https://")) {
                val (host, port) = parseProxyAddress(address.substring(7))
                Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port.toInt()))
            } else if (address.startsWith("socks://")) {
                val (host, port) = parseProxyAddress(address.substring(8))
                Proxy(Proxy.Type.SOCKS, InetSocketAddress(host, port.toInt()))
            } else {
                val (host, port) = parseProxyAddress(address)
                Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port.toInt()))
            }
        }
    }
}