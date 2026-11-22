package com.demo.rag.happiness.embedding

data class OllamaEmbedRequestDto(
    val model: String,
    val input: List<String>,
    val truncate: Boolean,
)