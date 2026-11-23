package com.demo.rag.happiness.ingestion

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MarkdownChunkerTest {

    private val chunker = MarkdownChunker()

    @Test
    fun `split cuts the markdown into one section per level 2 heading`() {
        val markdown = """
            ## Article 6 – Livraison
            Livraison en France métropolitaine.
            Frais : 6,90 €.

            ## Article 9 – Échange de taille
            Échange sous 15 jours.
        """.trimIndent()

        val sections = chunker.split(markdown)

        assertEquals(
            listOf(
                Section(
                    title = "Article 6 – Livraison",
                    content = "Livraison en France métropolitaine.\nFrais : 6,90 €.",
                ),
                Section(
                    title = "Article 9 – Échange de taille",
                    content = "Échange sous 15 jours.",
                ),
            ),
            sections,
        )
    }
}
