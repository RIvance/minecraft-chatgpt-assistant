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
import org.ivance.gptassistant.util.format
import java.net.Proxy
import java.net.SocketException
import java.time.Duration

class AssistantService @JvmOverloads constructor(
    private val token: String,
    private val logger: Logger,
    private val timeoutSecond: Long = 0L,
    private val proxy: Proxy? = null,
    modelBuilder: AssistantModel.Builder = ChatAssistantModel.builder(),
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

    private fun PlayerEntity.executeCommand(command: String) {
        logger.info("Executing command `$command` for player ${this.name.string}")
        this.server?.commandManager?.executeWithPrefix(this.commandSource, command) ?: run {
            MinecraftClient.getInstance().player?.networkHandler?.sendCommand(command.trim('/')) ?: {
                failRequest(this, "Unable to send command to the server")
            }
        }
    }

    fun executeCommandByPrompt(player: PlayerEntity, prompt: String) {
        try {
            val response = model.getResponse(player, player.format(prompt), requestConfig)
            logger.info("Response from OpenAI: $response")
            val commands = response.split("\n").filter { it.isNotBlank() }.map(::parseCommandFromText)
            if (!commands[0].startsWith("/")) {
                failRequest(player, response)
            } else {
                commands.forEach { player.executeCommand(it) }
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

    private fun parseCommandFromText(text: String): String {
        val singleBacktickQuotedPattern = "`(.*)`".toRegex()
        val threeBacktickQuotedPattern = "```(.*)```".toRegex()
        return (
            singleBacktickQuotedPattern.find(text)?.groupValues?.get(1) ?:
            threeBacktickQuotedPattern.find(text)?.groupValues?.get(1) ?:
            text
        ).trim()
    }
}
