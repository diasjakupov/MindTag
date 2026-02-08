package io.diasjakupov.mindtag.feature.study.domain.repository

import io.diasjakupov.mindtag.feature.study.domain.model.FlashCard
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.model.StudySession
import kotlinx.coroutines.flow.Flow

interface StudyRepository {
    suspend fun createSession(
        type: SessionType,
        subjectId: String?,
        questionCount: Int = 10,
        timeLimitSeconds: Int? = null,
    ): StudySession

    fun getSession(sessionId: String): Flow<StudySession?>
    suspend fun completeSession(sessionId: String)
    fun getCardsForSession(subjectId: String?, count: Int): Flow<List<FlashCard>>
}
