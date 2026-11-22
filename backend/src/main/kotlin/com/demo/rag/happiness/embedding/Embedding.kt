package com.demo.rag.happiness.embedding

data class Embedding(
    val values: FloatArray,
) {

    val dimension: Int get() = values.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Embedding

        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }

    override fun toString(): String {
        return "\"Embedding(dimension=\$dimension, first=\${values.take(3)})"
    }
}
