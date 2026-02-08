package io.diasjakupov.mindtag.feature.study.domain.model

import kotlinx.serialization.Serializable

data class FlashCard(
    val id: String,
    val question: String,
    val type: CardType,
    val difficulty: Difficulty,
    val subjectId: String,
    val correctAnswer: String,
    val options: List<AnswerOption>,
    val sourceNoteIds: List<String>,
    val aiExplanation: String?,
    val easeFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val nextReviewAt: Long?,
)

enum class CardType { FACT_CHECK, SYNTHESIS, MULTIPLE_CHOICE }

enum class Difficulty { EASY, MEDIUM, HARD }

@Serializable
data class AnswerOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
)
