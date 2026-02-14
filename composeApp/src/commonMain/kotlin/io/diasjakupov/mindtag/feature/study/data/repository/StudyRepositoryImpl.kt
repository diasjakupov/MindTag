package io.diasjakupov.mindtag.feature.study.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.FlashCardEntity
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.local.StudySessionEntity
import io.diasjakupov.mindtag.feature.study.domain.model.AnswerOption
import io.diasjakupov.mindtag.feature.study.domain.model.CardType
import io.diasjakupov.mindtag.feature.study.domain.model.Difficulty
import io.diasjakupov.mindtag.feature.study.domain.model.FlashCard
import io.diasjakupov.mindtag.feature.study.domain.model.SessionStatus
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.model.StudySession
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class StudyRepositoryImpl(
    private val db: MindTagDatabase,
) : StudyRepository {

    private val tag = "StudyRepo"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createSession(
        type: SessionType,
        subjectId: String?,
        questionCount: Int,
        timeLimitSeconds: Int?,
    ): StudySession {
        Logger.d(tag, "createSession: type=$type, questionCount=$questionCount, subjectId=$subjectId")
        val id = kotlin.uuid.Uuid.random().toString()
        val now = Clock.System.now().toEpochMilliseconds()

        db.studySessionEntityQueries.insert(
            id = id,
            subject_id = subjectId,
            session_type = type.name,
            started_at = now,
            finished_at = null,
            total_questions = questionCount.toLong(),
            time_limit_seconds = timeLimitSeconds?.toLong(),
            status = SessionStatus.IN_PROGRESS.name,
        )

        Logger.d(tag, "createSession: success — id=$id")
        return StudySession(
            id = id,
            subjectId = subjectId,
            sessionType = type,
            startedAt = now,
            finishedAt = null,
            totalQuestions = questionCount,
            timeLimitSeconds = timeLimitSeconds,
            status = SessionStatus.IN_PROGRESS,
        )
    }

    override fun getSession(sessionId: String): Flow<StudySession?> =
        db.studySessionEntityQueries.selectById(sessionId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun completeSession(sessionId: String) {
        Logger.d(tag, "completeSession: sessionId=$sessionId")
        val now = Clock.System.now().toEpochMilliseconds()
        db.studySessionEntityQueries.finish(
            finished_at = now,
            status = SessionStatus.COMPLETED.name,
            id = sessionId,
        )
    }

    override fun getCardsForSession(subjectId: String?, count: Int): Flow<List<FlashCard>> {
        val now = Clock.System.now().toEpochMilliseconds()
        val query = if (subjectId != null) {
            db.flashCardEntityQueries.selectDueCardsBySubject(subjectId, now)
        } else {
            db.flashCardEntityQueries.selectDueCards(now)
        }
        return query.asFlow().mapToList(Dispatchers.IO).map { entities ->
            val cards = entities.map { it.toDomain() }
            val result = if (cards.size >= count) {
                cards.take(count)
            } else {
                val dueIds = cards.map { it.id }.toSet()
                val allQuery = if (subjectId != null) {
                    db.flashCardEntityQueries.selectBySubjectId(subjectId)
                } else {
                    db.flashCardEntityQueries.selectAll()
                }
                val allCards = allQuery.executeAsList().map { it.toDomain() }
                val remaining = allCards.filter { it.id !in dueIds }.shuffled()
                (cards + remaining).take(count)
            }
            Logger.d(tag, "getCardsForSession: fetched ${result.size} cards (requested=$count, subjectId=$subjectId)")
            result
        }
    }

    override fun getSubjects(): Flow<List<Subject>> =
        db.subjectEntityQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { Subject(id = it.id, name = it.name, colorHex = it.color_hex, iconName = it.icon_name) }
            }

    override suspend fun getDueCardCount(): Int {
        val now = Clock.System.now().toEpochMilliseconds()
        return db.flashCardEntityQueries.selectDueCards(now)
            .executeAsList()
            .size
    }

    private fun FlashCardEntity.toDomain() = FlashCard(
        id = id,
        question = question,
        type = CardType.valueOf(type),
        difficulty = Difficulty.valueOf(difficulty),
        subjectId = subject_id,
        correctAnswer = correct_answer,
        options = options_json?.let {
            json.decodeFromString<List<AnswerOption>>(it)
        } ?: emptyList(),
        sourceNoteIds = source_note_ids_json?.let {
            json.decodeFromString<List<String>>(it)
        } ?: emptyList(),
        aiExplanation = ai_explanation,
        easeFactor = ease_factor.toFloat(),
        intervalDays = interval_days.toInt(),
        repetitions = repetitions.toInt(),
        nextReviewAt = next_review_at,
    )

    private fun StudySessionEntity.toDomain() = StudySession(
        id = id,
        subjectId = subject_id,
        sessionType = SessionType.valueOf(session_type),
        startedAt = started_at,
        finishedAt = finished_at,
        totalQuestions = total_questions.toInt(),
        timeLimitSeconds = time_limit_seconds?.toInt(),
        status = SessionStatus.valueOf(status),
    )
}
