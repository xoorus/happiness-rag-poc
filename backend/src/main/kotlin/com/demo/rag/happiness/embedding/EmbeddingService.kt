package com.demo.rag.happiness.embedding

interface EmbeddingService {
    fun embedQuery(query: String): Embedding
    fun embedDocuments(documents: List<EmbeddingDocument>): List<Embedding>
}