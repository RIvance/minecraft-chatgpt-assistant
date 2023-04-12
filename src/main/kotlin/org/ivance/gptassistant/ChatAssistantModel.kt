package org.ivance.gptassistant

import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestBuilder
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import net.minecraft.entity.player.PlayerEntity
import org.apache.logging.log4j.Logger

class ChatAssistantModel private constructor(
    override val service: OpenAiService, logger: Logger,
    private val modelIdent: Ident = Ident.GPT_35_TURBO,
) : AssistantModel(logger) {

    enum class Ident(name: String) {
        GPT_35_TURBO("gpt-3.5-turbo"),
        GPT_35_TURBO_0301("gpt-3.5-turbo-0301"),
    }

    private fun createChatCompletionRequestBuilder(config: RequestConfig): ChatCompletionRequestBuilder {
        return ChatCompletionRequest.builder()
            .model(modelIdent.name)
            .temperature(config.temperature)
            .maxTokens(config.maxTokens)
            .topP(config.topP)
            .frequencyPenalty(config.frequencyPenalty)
            .presencePenalty(config.presencePenalty)
    }

    override fun getCommand(player: PlayerEntity, prompt: String, config: RequestConfig): String {
        val finalSystemPrompt = systemPrompt + getPlayerInfoPrompt(player)
        logger.info("System prompt: $finalSystemPrompt")
        logger.info("User prompt: $prompt")
        return service.createChatCompletion(
            createChatCompletionRequestBuilder(config).messages(
                listOf(
                    ChatMessage("system", finalSystemPrompt),
                    ChatMessage("user", prompt)
                )
            ).build()
        ).choices[0].message.content.trim().let {
            if (!it.startsWith("/")) "/$it" else it
        }
    }

    companion object {
        fun builder(modelIdent: Ident = Ident.GPT_35_TURBO) = object : Builder() {
            override fun build(service: OpenAiService, logger: Logger): AssistantModel {
                return ChatAssistantModel(service, logger, modelIdent)
            }
        }
    }
}