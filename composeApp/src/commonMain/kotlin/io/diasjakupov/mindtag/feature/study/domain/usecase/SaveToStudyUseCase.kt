package io.diasjakupov.mindtag.feature.study.domain.usecase

import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuestionResult
import io.diasjakupov.mindtag.feature.study.domain.model.AnswerOption
import io.diasjakupov.mindtag.feature.study.domain.model.CardType
import io.diasjakupov.mindtag.feature.study.domain.model.Difficulty
import io.diasjakupov.mindtag.feature.study.domain.model.FlashCard
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository

class SaveToStudyUseCase(
    private val studyRepository: StudyRepository,
) {
    suspend operator fun invoke(
        questions: List<QuestionResult>,
        subjectId: String,
        sourceNoteId: Long?,
        saveAll: Boolean,
    ): Int {
        val toSave = if (saveAll) questions else questions.filter { !it.correct }
        if (toSave.isEmpty()) return 0

        val cards = toSave.map { qr ->
            val correctIndex = letterToIndex(qr.correctAnswer)
            val correctText = qr.options.getOrElse(correctIndex) { qr.options.firstOrNull() ?: "" }

            FlashCard(
                id = "quiz-${qr.questionId}",
                question = qr.questionText,
                type = CardType.MULTIPLE_CHOICE,
                difficulty = Difficulty.MEDIUM,
                subjectId = subjectId,
                correctAnswer = correctText,
                options = qr.options.mapIndexed { i, text ->
                    AnswerOption(
                        id = "opt-${qr.questionId}-$i",
                        text = text,
                        isCorrect = i == correctIndex,
                    )
                },
                sourceNoteIds = if (sourceNoteId != null) listOf(sourceNoteId.toString()) else emptyList(),
                aiExplanation = qr.explanation,
                easeFactor = 2.5f,
                intervalDays = 0,
                repetitions = 0,
                nextReviewAt = null,
            )
        }

        studyRepository.saveFlashCards(cards)
        return cards.size
    }

    private fun letterToIndex(letter: String): Int = when (letter.uppercase()) {
        "A" -> 0; "B" -> 1; "C" -> 2; "D" -> 3; else -> 0
    }
}
