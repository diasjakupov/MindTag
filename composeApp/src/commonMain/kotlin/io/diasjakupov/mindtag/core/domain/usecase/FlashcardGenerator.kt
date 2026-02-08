package io.diasjakupov.mindtag.core.domain.usecase

object FlashcardGenerator {

    private const val MAX_CARDS = 5
    private const val MIN_SENTENCE_WORDS = 8

    data class GeneratedCard(
        val question: String,
        val correctAnswer: String,
        val type: String,
        val difficulty: String,
        val subjectId: String,
        val sourceNoteIdsJson: String,
        val explanation: String,
    )

    fun generate(
        noteTitle: String,
        noteContent: String,
        subjectId: String,
        noteId: String,
    ): List<GeneratedCard> {
        val sentences = splitSentences(noteContent)
            .filter { it.split(" ").size >= MIN_SENTENCE_WORDS }

        if (sentences.isEmpty()) return emptyList()

        val sourceJson = "[\"$noteId\"]"

        return sentences.take(MAX_CARDS).mapIndexed { index, sentence ->
            val difficulty = when {
                index < 2 -> "EASY"
                index < 4 -> "MEDIUM"
                else -> "HARD"
            }

            val question = createQuestion(sentence, noteTitle)
            GeneratedCard(
                question = question,
                correctAnswer = sentence.trim(),
                type = "FACT_CHECK",
                difficulty = difficulty,
                subjectId = subjectId,
                sourceNoteIdsJson = sourceJson,
                explanation = "From your note: $noteTitle",
            )
        }
    }

    private fun splitSentences(text: String): List<String> {
        return text.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun createQuestion(sentence: String, noteTitle: String): String {
        val words = sentence.split(" ")
        if (words.size < MIN_SENTENCE_WORDS) return "True or False: $sentence"

        val blankTarget = findBlankTarget(words)
        return if (blankTarget != null) {
            sentence.replace(blankTarget, "_____") + " (Fill in the blank)"
        } else {
            "According to your notes on $noteTitle: True or False — $sentence"
        }
    }

    private fun findBlankTarget(words: List<String>): String? {
        // Strategy 1: Look for a sequence of capitalized words (proper nouns / key terms)
        // Skip the first word since it's always capitalized after a period
        val candidates = mutableListOf<String>()
        var i = 1
        while (i < words.size) {
            if (words[i].first().isUpperCase() && words[i].all { it.isLetter() }) {
                val start = i
                while (i < words.size && words[i].first().isUpperCase()) i++
                candidates.add(words.subList(start, i).joinToString(" "))
            }
            i++
        }
        if (candidates.isNotEmpty()) {
            return candidates.maxByOrNull { it.length }
        }

        // Strategy 2: Look for technical-sounding words (long words)
        val technicalWords = words.drop(1)
            .filter { it.length > 6 && it.all { c -> c.isLetter() } }

        return technicalWords.maxByOrNull { it.length }
    }
}
