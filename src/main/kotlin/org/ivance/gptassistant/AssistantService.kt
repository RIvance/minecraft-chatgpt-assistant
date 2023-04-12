package org.ivance.gptassistant

import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.OpenAiHttpException
import com.theokanning.openai.service.OpenAiService
import com.theokanning.openai.service.OpenAiService.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import org.apache.logging.log4j.Logger
import java.net.Proxy
import java.time.Duration

class AssistantService @JvmOverloads constructor(
    private val token: String,
    private val logger: Logger,
    private val timeoutSecond: Long = 0L,
    private val proxy: Proxy? = null,
    var config: RequestConfig = RequestConfig.DEFAULT
) {
    private val service: OpenAiService by lazy {
        logger.info("Initializing OpenAI service")
        val timeout = Duration.ofSeconds(timeoutSecond)
        proxy?.let { proxy ->
            logger.info("Using proxy $proxy")
            val client = defaultClient(token, timeout).newBuilder().proxy(proxy).build()
            val retrofit = defaultRetrofit(client, defaultObjectMapper())
            OpenAiService(retrofit.create(OpenAiApi::class.java))
        } ?: OpenAiService(token, timeout)
    }

    private var model: AssistantModel = ChatAssistantModel.builder().build(service, logger)

    fun setModel(builder: AssistantModel.Builder) {
        this.model = builder.build(service, logger)
    }

    private fun failRequest(player: PlayerEntity, reason: String) {
        player.sendMessage(Text.literal("The assistant is unable to finish your request: $reason"), false)
    }

    fun executeCommandByPrompt(player: PlayerEntity, prompt: String) {
        try {
            val command = model.getCommand(player, prompt.trim(':'), config)
            logger.info("Executing command `$command` for player ${player.name.string}")
            if (player.server?.commandManager?.executeWithPrefix(player.commandSource, command) == 0) {
                failRequest(player, "Failed to execute command `$command`")
            }
        } catch (exception: OpenAiHttpException) {
            failRequest(player, "Unable to reach OpenAI: ${exception.message ?: "Unknown error"}")
        } catch (exception: Exception) {
            failRequest(player, exception.message ?: "Unknown error")
        }
    }

    fun copy(
        token: String = this.token,
        logger: Logger = this.logger,
        timeoutSecond: Long = this.timeoutSecond,
        proxy: Proxy? = this.proxy,
        config: RequestConfig = this.config
    ) = AssistantService(token, logger, timeoutSecond, proxy, config)
}