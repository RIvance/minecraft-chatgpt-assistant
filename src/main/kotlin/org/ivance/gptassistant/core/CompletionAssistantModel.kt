package org.ivance.gptassistant.core

import com.theokanning.openai.completion.CompletionRequest
import com.theokanning.openai.completion.CompletionRequest.CompletionRequestBuilder
import com.theokanning.openai.service.OpenAiService
import net.minecraft.entity.player.PlayerEntity
import org.apache.logging.log4j.Logger
import org.ivance.gptassistant.config.RequestConfig

class CompletionAssistantModel private constructor(
    override val service: OpenAiService, logger: Logger,
    private val modelIdent: Ident = Ident.DAVINCI_003,
) : AssistantModel(logger) {

    enum class Ident(private val ident: String) {
        DAVINCI_001("text-davinci-001"),
        DAVINCI_002("text-davinci-002"),
        DAVINCI_003("text-davinci-003"),
        ADA_001("text-ada-001"),
        CURIE_001("text-curie-001");

        override fun toString(): String {
            return this.ident
        }
    }

    private fun createCompletionRequestBuilder(config: RequestConfig): CompletionRequestBuilder {
        return CompletionRequest.builder()
            .model(modelIdent.toString())
            .temperature(config.temperature)
            .maxTokens(config.maxTokens)
            .topP(config.topP)
            .frequencyPenalty(config.frequencyPenalty)
            .stop(listOf(" Player:", " Assistant:"))
            .presencePenalty(config.presencePenalty)
    }

    override fun getResponse(player: PlayerEntity, prompt: String, config: RequestConfig): String {
        val finalPrompt = systemPrompt + getPlayerInfoPrompt(player) + prompt
        return service.createCompletion(
            createCompletionRequestBuilder(config).prompt(finalPrompt).build()
        ).choices[0].text.trim().let {
            if (!it.startsWith("/")) "/$it" else it
        }
    }

    companion object {
        fun builder(modelIdent: Ident = Ident.DAVINCI_003) = object : Builder() {
            override fun build(service: OpenAiService, logger: Logger): AssistantModel {
                return CompletionAssistantModel(service, logger, modelIdent)
            }
        }
    }
}