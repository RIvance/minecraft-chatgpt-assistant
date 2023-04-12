package org.ivance.gptassistant

import com.theokanning.openai.service.OpenAiService
import net.minecraft.entity.player.PlayerEntity
import org.apache.logging.log4j.Logger

abstract class AssistantModel(val logger: Logger) {

    abstract val service: OpenAiService

    open var systemPrompt: String = DEFAULT_SYSTEM_PROMPT

    fun getPlayerInfoPrompt(player: PlayerEntity): String {
        return (
            "The Minecraft version is ${player.server?.version}. " +
            "Player ${player.name.string} is at (${player.x}, ${player.y}, ${player.z}) in the ${player.world.dimension} dimension. "
        )
    }

    abstract fun getCommand(player: PlayerEntity, prompt: String, config: RequestConfig): String

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