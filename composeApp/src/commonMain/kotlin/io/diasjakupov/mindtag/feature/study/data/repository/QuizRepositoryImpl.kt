package io.diasjakupov.mindtag.feature.study.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.study.domain.model.ConfidenceRating
import io.diasjakupov.mindtag.feature.study.domain.model.QuizAnswer
import io.diasjakupov.mindtag.feature.study.domain.model.QuizAnswerDetail
import io.diasjakupov.mindtag.feature.study.domain.model.SessionResult
import io.diasjakupov.mindtag.feature.study.domain.model.SessionStatus
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.model.StudySession
import io.diasjakupov.mindtag.feature.study.domain.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock

class QuizRepositoryImpl(
    private val db: MindTagDatabase,
) : QuizRepository {

    private val tag = "QuizRepo"

    override suspend fun submitAnswer(answer: QuizAnswer) {
        Logger.d(tag, "submitAnswer: cardId=${answer.cardId}, correct=${answer.isCorrect}")
        db.quizAnswerEntityQueries.insert(
            id = answer.id,
            session_id = answer.sessionId,
            card_id = answer.cardId,
            user_answer = answer.userAnswer,
            is_correct = if (answer.isCorrect) 1L else 0L,
            confidence_rating = answer.confidenceRating?.name,
            time_spent_seconds = answer.timeSpentSeconds.toLong(),
            answered_at = answer.answeredAt,
        )
    }

    override fun getSessionResults(sessionId: String): Flow<SessionResult?> {
        val sessionFlow = db.studySessionEntityQueries.selectById(sessionId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)

        val answersFlow = db.quizAnswerEntityQueries.selectBySessionId(sessionId)
            .asFlow()
            .mapToList(Dispatchers.IO)

        return combine(sessionFlow, answersFlow) { sessionEntity, answerEntities ->
            if (sessionEntity == null) return@combine null

            val session = StudySession(
                id = sessionEntity.id,
                subjectId = sessionEntity.subject_id,
                sessionType = SessionType.valueOf(sessionEntity.session_type),
                startedAt = sessionEntity.started_at,
                finishedAt = sessionEntity.finished_at,
                totalQuestions = sessionEntity.total_questions.toInt(),
                timeLimitSeconds = sessionEntity.time_limit_seconds?.toInt(),
                status = SessionStatus.valueOf(sessionEntity.status),
            )

            val answerDetails = answerEntities.map { answerEntity ->
                val cardEntity = db.flashCardEntityQueries
                    .selectById(answerEntity.card_id)
                    .executeAsOneOrNull()

                QuizAnswerDetail(
                    cardId = answerEntity.card_id,
                    question = cardEntity?.question ?: "",
                    userAnswer = answerEntity.user_answer,
                    correctAnswer = cardEntity?.correct_answer ?: "",
                    isCorrect = answerEntity.is_correct == 1L,
                    aiInsight = cardEntity?.ai_explanation,
                )
            }

            val totalCorrect = answerDetails.count { it.isCorrect }
            val totalQuestions = answerDetails.size.coerceAtLeast(1)
            val scorePercent = (totalCorrect * 100) / totalQuestions
            val xpEarned = totalCorrect * 10

            Logger.d(tag, "getSessionResults: score=$scorePercent%, correct=$totalCorrect/$totalQuestions")

            val timeSpentSeconds = if (session.finishedAt != null) {
                ((session.finishedAt - session.startedAt) / 1000).toInt()
            } else {
                answerEntities.sumOf { it.time_spent_seconds }.toInt()
            }
            val timeSpentFormatted = formatTime(timeSpentSeconds)

            val progress = session.subjectId?.let {
                db.userProgressEntityQueries.selectBySubjectId(it).executeAsOneOrNull()
            }
            val currentStreak = progress?.current_streak?.toInt() ?: 0

            SessionResult(
                session = session,
                scorePercent = scorePercent,
                totalCorrect = totalCorrect,
                totalQuestions = totalQuestions,
                timeSpentFormatted = timeSpentFormatted,
                xpEarned = xpEarned,
                currentStreak = currentStreak,
                answers = answerDetails,
            )
        }
    }

    override suspend fun updateCardSchedule(
        cardId: String,
        isCorrect: Boolean,
        confidence: ConfidenceRating?,
    ) {
        val card = db.flashCardEntityQueries.selectById(cardId).executeAsOneOrNull() ?: return

        val quality = when {
            !isCorrect -> 1
            confidence == ConfidenceRating.HARD -> 3
            confidence == ConfidenceRating.EASY -> 5
            else -> 4
        }

        val (newEf, newInterval, newRep) = calculateSm2(
            easeFactor = card.ease_factor.toFloat(),
            interval = card.interval_days.toInt(),
            repetitions = card.repetitions.toInt(),
            quality = quality,
        )

        Logger.d(tag, "updateCardSchedule: cardId=$cardId, correct=$isCorrect, newEF=$newEf, newInterval=$newInterval, newRep=$newRep")

        val now = Clock.System.now().toEpochMilliseconds()
        val nextReview = now + (newInterval.toLong() * 24 * 60 * 60 * 1000)

        db.flashCardEntityQueries.updateSpacedRepetition(
            ease_factor = newEf.toDouble(),
            interval_days = newInterval.toLong(),
            repetitions = newRep.toLong(),
            next_review_at = nextReview,
            id = cardId,
        )
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
    }
}

fun calculateSm2(
    easeFactor: Float,
    interval: Int,
    repetitions: Int,
    quality: Int,
): Triple<Float, Int, Int> {
    val newEf = maxOf(
        1.3f,
        easeFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f)),
    )
    if (quality < 3) return Triple(newEf, 1, 0)
    val newRep = repetitions + 1
    val newInterval = when (newRep) {
        1 -> 1
        2 -> 6
        else -> (interval * newEf).toInt()
    }
    return Triple(newEf, newInterval, newRep)
}
