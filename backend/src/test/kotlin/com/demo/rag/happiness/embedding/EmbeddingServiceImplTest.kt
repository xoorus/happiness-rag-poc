package com.demo.rag.happiness.embedding

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withException
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import java.io.IOException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class EmbeddingServiceImplTest {

    private lateinit var server: MockRestServiceServer
    private lateinit var service: EmbeddingServiceImpl

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder()
        // 1. Brancher le faux serveur AVANT que le service appelle builder.build()
        server = MockRestServiceServer.bindTo(builder).build()
        // 2. Puis construire le service
        service = EmbeddingServiceImpl(
            restClientBuilder = builder,
            ollamaUrl = OLLAMA_URL,
            queryTemplate = "task: search result | query: {text}",
            documentTemplate = "title: {title} | text: {text}",
            truncate = false,
            embeddingModel = MODEL,
        )
    }

    // ---------- embedQuery ----------

    @Test
    fun `embedQuery sends the prefixed query and returns the embedding`() {
        server.expect(requestTo("$OLLAMA_URL/api/embed"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(
                content().json(
                    """
                    {
                      "model": "$MODEL",
                      "input": ["task: search result | query: Puis-je retourner un album ?"],
                      "truncate": false
                    }
                    """
                )
            )
            .andRespond(withSuccess("""{"embeddings": [[0.1, 0.2, 0.3]]}""", MediaType.APPLICATION_JSON))

        val embedding = service.embedQuery("Puis-je retourner un album ?")

        assertContentEquals(floatArrayOf(0.1f, 0.2f, 0.3f), embedding.values)
        assertEquals(3, embedding.dimension)
        server.verify()
    }

    @Test
    fun `embedQuery rejects a blank query without calling Ollama`() {
        // Aucune attente déclarée : tout appel HTTP ferait échouer le test
        assertFailsWith<IllegalArgumentException> { service.embedQuery("   ") }

        server.verify()
    }

    @Test
    fun `embedQuery wraps an Ollama server error in an EmbeddingException`() {
        server.expect(requestTo("$OLLAMA_URL/api/embed"))
            .andRespond(withServerError())

        val exception = assertFailsWith<EmbeddingException> { service.embedQuery("question") }

        assertIs<HttpServerErrorException>(exception.cause)
        server.verify()
    }

    @Test
    fun `embedQuery wraps an unreachable Ollama in an EmbeddingException`() {
        server.expect(requestTo("$OLLAMA_URL/api/embed"))
            .andRespond(withException(IOException("Connection refused")))

        val exception = assertFailsWith<EmbeddingException> { service.embedQuery("question") }

        assertIs<ResourceAccessException>(exception.cause)
        server.verify()
    }

    @Test
    fun `embedQuery fails when Ollama returns more embeddings than inputs`() {
        server.expect(requestTo("$OLLAMA_URL/api/embed"))
            .andRespond(withSuccess("""{"embeddings": [[0.1, 0.2], [0.3, 0.4]]}""", MediaType.APPLICATION_JSON))

        assertFailsWith<EmbeddingException> { service.embedQuery("question") }

        server.verify()
    }

    @Test
    fun `embedQuery fails when Ollama returns an empty body`() {
        server.expect(requestTo("$OLLAMA_URL/api/embed"))
            .andRespond(withSuccess())

        assertFailsWith<EmbeddingException> { service.embedQuery("question") }

        server.verify()
    }

    // ---------- embedDocuments ----------

    @Test
    fun `embedDocuments sends all documents in one call with the document template and keeps the order`() {
        server.expect(requestTo("$OLLAMA_URL/api/embed"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(
                content().json(
                    """
                    {
                      "model": "$MODEL",
                      "input": [
                        "title: CGV > Article 7 > Retours | text: Vous disposez de 14 jours.",
                        "title: Album photo > Formats | text: Trois formats disponibles."
                      ],
                      "truncate": false
                    }
                    """
                )
            )
            .andRespond(withSuccess("""{"embeddings": [[0.1, 0.2], [0.3, 0.4]]}""", MediaType.APPLICATION_JSON))

        val embeddings = service.embedDocuments(
            listOf(
                EmbeddingDocument("CGV > Article 7 > Retours", "Vous disposez de 14 jours."),
                EmbeddingDocument("Album photo > Formats", "Trois formats disponibles."),
            )
        )

        assertEquals(2, embeddings.size)
        assertContentEquals(floatArrayOf(0.1f, 0.2f), embeddings[0].values)
        assertContentEquals(floatArrayOf(0.3f, 0.4f), embeddings[1].values)
        // verify() garantit aussi qu'il n'y a eu qu'UN seul appel (expect() = une fois par défaut)
        server.verify()
    }

    @Test
    fun `embedDocuments rejects an empty list without calling Ollama`() {
        assertFailsWith<IllegalArgumentException> { service.embedDocuments(emptyList()) }

        server.verify()
    }

    @Test
    fun `embedDocuments fails when Ollama returns fewer embeddings than documents`() {
        server.expect(requestTo("$OLLAMA_URL/api/embed"))
            .andRespond(withSuccess("""{"embeddings": [[0.1, 0.2]]}""", MediaType.APPLICATION_JSON))

        assertFailsWith<EmbeddingException> {
            service.embedDocuments(
                listOf(
                    EmbeddingDocument("Titre 1", "Texte 1"),
                    EmbeddingDocument("Titre 2", "Texte 2"),
                )
            )
        }

        server.verify()
    }

    companion object {
        private const val OLLAMA_URL = "http://ollama.test"
        private const val MODEL = "embeddinggemma:300m"
    }
}
