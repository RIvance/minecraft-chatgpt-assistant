package org.ivance.gptassistant.core

import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.OpenAiHttpException
import com.theokanning.openai.service.OpenAiService
import com.theokanning.openai.service.OpenAiService.*
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import org.apache.logging.log4j.Logger
import org.ivance.gptassistant.config.RequestConfig
import java.net.Proxy
import java.net.SocketException
import java.time.Duration

class AssistantService @JvmOverloads constructor(
    private val token: String,
    private val logger: Logger,
    private val timeoutSecond: Long = 0L,
    private val proxy: Proxy? = null,
    private val modelBuilder: AssistantModel.Builder = ChatAssistantModel.builder(),
    var requestConfig: RequestConfig = RequestConfig.DEFAULT
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

    private val model: AssistantModel = modelBuilder.build(service, logger)

    private fun failRequest(player: PlayerEntity, reason: String) {
        player.sendMessage(Text.literal("The assistant is unable to finish your request: $reason"), false)
    }

    fun executeCommandByPrompt(player: PlayerEntity, prompt: String) {
        try {
            val command = model.getCommand(player, prompt.trim(':'), requestConfig)
            if (!command.startsWith("/")) {
                failRequest(player, command)
                return
            }
            logger.info("Executing command `$command` for player ${player.name.string}")
            player.server?.commandManager?.executeWithPrefix(player.commandSource, command) ?: run {
                MinecraftClient.getInstance().player?.sendCommand(command.trim('/'))
            }
        } catch (exception: OpenAiHttpException) {
            failRequest(player, "Unable to reach OpenAI: ${exception.message ?: "Unknown error"}")
        } catch (exception: Exception) {
            if (exception.cause is SocketException) {
                failRequest(player,
                    "Unable to reach OpenAI: ${exception.message ?: "Unknown error"}. " +
                    "Please check your internet connection or proxy settings. "
                )
            } else {
                failRequest(player, exception.message ?: "Unknown error")
            }
        }
    }
}