package io.diasjakupov.mindtag.test

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.study.domain.model.FlashCard
import io.diasjakupov.mindtag.feature.study.domain.model.SessionStatus
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.model.StudySession
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

class FakeStudyRepository : StudyRepository {

    private val sessionsFlow = MutableStateFlow<List<StudySession>>(emptyList())
    private val flashCardsFlow = MutableStateFlow<List<FlashCard>>(emptyList())

    private var nextSessionId = 1

    fun setSessions(sessions: List<StudySession>) {
        sessionsFlow.value = sessions
    }

    fun setFlashCards(cards: List<FlashCard>) {
        flashCardsFlow.value = cards
    }

    override suspend fun createSession(
        type: SessionType,
        subjectId: String?,
        questionCount: Int,
        timeLimitSeconds: Int?,
    ): StudySession {
        val session = StudySession(
            id = "session-${nextSessionId++}",
            subjectId = subjectId,
            sessionType = type,
            startedAt = Clock.System.now().toEpochMilliseconds(),
            finishedAt = null,
            totalQuestions = questionCount,
            timeLimitSeconds = timeLimitSeconds,
            status = SessionStatus.IN_PROGRESS,
        )
        sessionsFlow.update { it + session }
        return session
    }

    override fun getSession(sessionId: String): Flow<StudySession?> {
        return sessionsFlow.map { sessions -> sessions.find { it.id == sessionId } }
    }

    override suspend fun completeSession(sessionId: String) {
        sessionsFlow.update { sessions ->
            sessions.map { session ->
                if (session.id == sessionId) session.copy(
                    status = SessionStatus.COMPLETED,
                    finishedAt = Clock.System.now().toEpochMilliseconds(),
                )
                else session
            }
        }
    }

    override fun getCardsForSession(subjectId: String?, count: Int): Flow<List<FlashCard>> {
        return flashCardsFlow.map { cards ->
            val filtered = if (subjectId != null) cards.filter { it.subjectId == subjectId } else cards
            filtered.take(count)
        }
    }

    override fun getSubjects(): Flow<List<Subject>> = flowOf(emptyList())

    override suspend fun getDueCardCount(): Int = 0
}
