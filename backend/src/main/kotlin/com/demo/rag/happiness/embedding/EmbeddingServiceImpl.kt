package com.demo.rag.happiness.embedding

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Service
class EmbeddingServiceImpl(
    restClientBuilder: RestClient.Builder,
    @Value("\${ollama.url}") ollamaUrl: String,
    @Value("\${ollama.query-template}") private val queryTemplate: String,
    @Value("\${ollama.document-template}") private val documentTemplate: String,
    @Value("\${ollama.truncate}") private val truncate: Boolean,
    @Value("\${ollama.embedding-model}") private val embeddingModel: String,
) : EmbeddingService {

    private val restClient = restClientBuilder.baseUrl(ollamaUrl).build()

    override fun embedQuery(query: String): Embedding {
        require(query.isNotBlank()) { "Query must not be blank" }

        val input = queryTemplate.replace("{text}", query)

        return callOllama(listOf(input)).single()
    }

    override fun embedDocuments(documents: List<EmbeddingDocument>): List<Embedding> {
        require(documents.isNotEmpty()) { "Documents must not be empty" }
        TODO("Not yet implemented")
    }

    private fun callOllama(inputs: List<String>): List<Embedding> {
        val response = try {
            restClient.post()
                .uri("/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(OllamaEmbedRequestDto(embeddingModel, inputs, truncate))
                .retrieve()
                .body(OllamaEmbedResponseDto::class.java)
        } catch (e: RestClientException) {
            throw EmbeddingException("Ollama /api/embed call failed for ${inputs.size} input(s)", e)
        }

        if (response == null) {
            throw EmbeddingException("Ollama /api/embed returned an empty body for ${inputs.size} input(s)")
        }
        if (response.embeddings.size != inputs.size) {
            throw EmbeddingException(
                "Ollama /api/embed returned ${response.embeddings.size} embedding(s) for ${inputs.size} input(s)"
            )
        }

        return response.embeddings.map { Embedding(it.toFloatArray()) }
    }
}
