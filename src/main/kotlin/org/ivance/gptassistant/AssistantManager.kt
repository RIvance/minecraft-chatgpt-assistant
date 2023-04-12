package org.ivance.gptassistant

import org.apache.logging.log4j.Logger
import java.net.Proxy

class AssistantManager(private val logger: Logger) {

    var service: AssistantService? = null
        private set

    var token: String? = null
        set(value) = value?.let {
            field = it
            service = service?.copy(token = it) ?: run {
                AssistantService(it, logger, timeoutSecond, proxy, config)
            }
        } ?: Unit

    var proxy: Proxy? = null
        set(value) = value?.let {
            field = it
            service = service?.copy(proxy = it)
        } ?: Unit

    var timeoutSecond: Long = 0L
        set(value) = value.let {
            field = it
            service = service?.copy(timeoutSecond = it)
        }

    var config: RequestConfig = RequestConfig.DEFAULT
        set(value) = value.let {
            field = it
            service = service?.copy(config = it)
        }
}