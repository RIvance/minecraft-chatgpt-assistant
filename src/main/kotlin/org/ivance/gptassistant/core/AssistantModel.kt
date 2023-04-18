package org.ivance.gptassistant.core

import com.theokanning.openai.service.OpenAiService
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import org.apache.logging.log4j.Logger
import org.ivance.gptassistant.config.RequestConfig

abstract class AssistantModel(val logger: Logger) {

    abstract val service: OpenAiService

    open var systemPrompt: String = DEFAULT_SYSTEM_PROMPT

    fun getPlayerInfoPrompt(player: PlayerEntity): String {
        return (
            "The player ${player.name.string} is playing Minecraft ${MinecraftClient.getInstance().gameVersion}. "
        )
    }

    abstract fun getResponse(player: PlayerEntity, prompt: String, config: RequestConfig): String

    abstract class Builder {
        abstract fun build(service: OpenAiService, logger: Logger): AssistantModel
    }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = (
            "You are a system that built for translating player's requirements into Minecraft commands. " +
            "Your only task it to provide translated commands with line breaks. " +
            "Don't teach the player to do anything and don't ask or explain anything. " +
            "The commands should be executable without any modification. "
        )
    }
}
