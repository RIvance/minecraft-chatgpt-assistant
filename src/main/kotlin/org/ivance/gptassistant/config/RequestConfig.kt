package org.ivance.gptassistant.config

data class RequestConfig(
    val maxTokens: Int,
    val temperature: Double,
    val topP: Double,
    val frequencyPenalty: Double,
    val presencePenalty: Double,
) {
    fun maxTokens(maxTokens: Int) = copy(maxTokens = maxTokens)
    fun temperature(temperature: Double) = copy(temperature = temperature)
    fun topP(topP: Double) = copy(topP = topP)
    fun frequencyPenalty(frequencyPenalty: Double) = copy(frequencyPenalty = frequencyPenalty)
    fun presencePenalty(presencePenalty: Double) = copy(presencePenalty = presencePenalty)

    companion object {
        val DEFAULT = RequestConfig(
            maxTokens = 128,
            temperature = 0.9,
            topP = 1.0,
            frequencyPenalty = 0.0,
            presencePenalty = 0.0,
        )
    }
}
