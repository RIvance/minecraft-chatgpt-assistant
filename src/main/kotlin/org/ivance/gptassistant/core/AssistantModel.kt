package org.ivance.gptassistant.core

import com.theokanning.openai.service.OpenAiService
import net.minecraft.entity.player.PlayerEntity
import org.apache.logging.log4j.Logger
import org.ivance.gptassistant.config.RequestConfig

abstract class AssistantModel(val logger: Logger) {

    abstract val service: OpenAiService

    open var systemPrompt: String = DEFAULT_SYSTEM_PROMPT

    fun getPlayerInfoPrompt(player: PlayerEntity): String {
        return (
            "The player ${player.name.string} is playing Minecraft ${player.server?.version}. "
        )
    }

    protected abstract fun getResponse(player: PlayerEntity, prompt: String, config: RequestConfig): String

    fun getCommand(player: PlayerEntity, prompt: String, config: RequestConfig): String {
        val response = getResponse(player, prompt, config)
        logger.info("Response from OpenAI: $response")
        return parseCommandFromResponse(response)
    }

    private fun parseCommandFromResponse(response: String): String {
        val singleBacktickQuotedPattern = "`(.*)`".toRegex()
        val threeBacktickQuotedPattern = "```(.*)```".toRegex()
        return (
            singleBacktickQuotedPattern.find(response)?.groupValues?.get(1) ?:
            threeBacktickQuotedPattern.find(response)?.groupValues?.get(1) ?:
            response
        ).trim()
    }

    abstract class Builder {
        abstract fun build(service: OpenAiService, logger: Logger): AssistantModel
    }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = (
            "You are an assistant who knows everything about Minecraft. " +
            "You need to provide a command that satisfies the players' needs. " +
            "You should only show the command itself and don't provide any extra info. "
        )
    }
}