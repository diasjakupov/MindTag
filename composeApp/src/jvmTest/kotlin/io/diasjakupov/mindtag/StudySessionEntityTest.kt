package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StudySessionEntityTest {

    private lateinit var database: MindTagDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-bio", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
    }

    @Test
    fun insertAndSelectById() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert(
            id = "session-1",
            subject_id = "subj-bio",
            session_type = "QUIZ",
            started_at = now,
            finished_at = null,
            total_questions = 10,
            time_limit_seconds = null,
            status = "IN_PROGRESS",
        )

        val session = database.studySessionEntityQueries.selectById("session-1").executeAsOneOrNull()
        assertNotNull(session)
        assertEquals("subj-bio", session.subject_id)
        assertEquals("QUIZ", session.session_type)
        assertEquals(10L, session.total_questions)
        assertNull(session.finished_at)
        assertEquals("IN_PROGRESS", session.status)
    }

    @Test
    fun selectBySubjectId() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", "subj-bio", "QUIZ", now, null, 10, null, "IN_PROGRESS")
        database.studySessionEntityQueries.insert("s2", "subj-bio", "QUIZ", now + 1000, null, 20, 600, "IN_PROGRESS")
        database.studySessionEntityQueries.insert("s3", null, "QUIZ", now + 2000, null, 5, null, "IN_PROGRESS")

        val bioSessions = database.studySessionEntityQueries.selectBySubjectId("subj-bio").executeAsList()
        assertEquals(2, bioSessions.size)
        // Ordered by started_at DESC
        assertEquals("s2", bioSessions[0].id)
        assertEquals("s1", bioSessions[1].id)
    }

    @Test
    fun finishSession() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", "subj-bio", "QUIZ", now, null, 10, null, "IN_PROGRESS")

        val finishedAt = now + 300_000L
        database.studySessionEntityQueries.finish(
            finished_at = finishedAt,
            status = "COMPLETED",
            id = "s1",
        )

        val session = database.studySessionEntityQueries.selectById("s1").executeAsOneOrNull()
        assertNotNull(session)
        assertEquals(finishedAt, session.finished_at)
        assertEquals("COMPLETED", session.status)
    }

    @Test
    fun selectActiveReturnsInProgressSession() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", "subj-bio", "QUIZ", now, now + 300_000, 10, null, "COMPLETED")
        database.studySessionEntityQueries.insert("s2", "subj-bio", "QUIZ", now + 1000, null, 20, 600, "IN_PROGRESS")

        val active = database.studySessionEntityQueries.selectActive().executeAsOneOrNull()
        assertNotNull(active)
        assertEquals("s2", active.id)
    }

    @Test
    fun selectActiveReturnsNullWhenNoActiveSessions() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", "subj-bio", "QUIZ", now, now + 300_000, 10, null, "COMPLETED")

        val active = database.studySessionEntityQueries.selectActive().executeAsOneOrNull()
        assertNull(active)
    }

    @Test
    fun nullSubjectIdIsAllowed() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", null, "QUIZ", now, null, 10, null, "IN_PROGRESS")

        val session = database.studySessionEntityQueries.selectById("s1").executeAsOneOrNull()
        assertNotNull(session)
        assertNull(session.subject_id)
    }

    @Test
    fun timeLimitSecondsCanBeNull() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", "subj-bio", "QUIZ", now, null, 10, null, "IN_PROGRESS")

        val session = database.studySessionEntityQueries.selectById("s1").executeAsOneOrNull()
        assertNotNull(session)
        assertNull(session.time_limit_seconds)
    }

    @Test
    fun timeLimitSecondsCanBeSet() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", "subj-bio", "QUIZ", now, null, 20, 600, "IN_PROGRESS")

        val session = database.studySessionEntityQueries.selectById("s1").executeAsOneOrNull()
        assertNotNull(session)
        assertEquals(600L, session.time_limit_seconds)
    }

    @Test
    fun deleteSession() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", "subj-bio", "QUIZ", now, null, 10, null, "IN_PROGRESS")

        database.studySessionEntityQueries.delete("s1")

        val result = database.studySessionEntityQueries.selectById("s1").executeAsOneOrNull()
        assertNull(result)
    }

    @Test
    fun deleteAllSessions() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s1", "subj-bio", "QUIZ", now, null, 10, null, "IN_PROGRESS")
        database.studySessionEntityQueries.insert("s2", "subj-bio", "QUIZ", now, null, 20, 600, "IN_PROGRESS")

        database.studySessionEntityQueries.deleteAll()

        val sessions = database.studySessionEntityQueries.selectAll().executeAsList()
        assertTrue(sessions.isEmpty())
    }

    @Test
    fun selectAllOrderedByStartedAtDesc() {
        val now = System.currentTimeMillis()
        database.studySessionEntityQueries.insert("s-old", "subj-bio", "QUIZ", now, null, 10, null, "IN_PROGRESS")
        database.studySessionEntityQueries.insert("s-new", "subj-bio", "QUIZ", now + 5000, null, 20, 600, "IN_PROGRESS")

        val sessions = database.studySessionEntityQueries.selectAll().executeAsList()
        assertEquals(2, sessions.size)
        assertEquals("s-new", sessions[0].id)
        assertEquals("s-old", sessions[1].id)
    }
}
