package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserProgressEntityTest {

    private lateinit var database: MindTagDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)

        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-bio", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-cs", "CS", "#135BEC", "code", 0.0, 0, 0, now, now)
    }

    @Test
    fun insertAndSelectBySubjectId() {
        val now = System.currentTimeMillis()
        database.userProgressEntityQueries.insert(
            subject_id = "subj-bio",
            mastery_percent = 65.0,
            notes_reviewed = 3,
            total_notes = 5,
            avg_quiz_score = 72.0,
            current_streak = 4,
            total_xp = 1250,
            last_studied_at = now,
        )

        val progress = database.userProgressEntityQueries.selectBySubjectId("subj-bio").executeAsOneOrNull()
        assertNotNull(progress)
        assertEquals(65.0, progress.mastery_percent)
        assertEquals(3L, progress.notes_reviewed)
        assertEquals(5L, progress.total_notes)
        assertEquals(72.0, progress.avg_quiz_score)
        assertEquals(4L, progress.current_streak)
        assertEquals(1250L, progress.total_xp)
        assertEquals(now, progress.last_studied_at)
    }

    @Test
    fun selectAll() {
        val now = System.currentTimeMillis()
        database.userProgressEntityQueries.insert("subj-bio", 65.0, 3, 5, 72.0, 4, 1250, now)
        database.userProgressEntityQueries.insert("subj-cs", 78.0, 4, 5, 85.0, 7, 2100, now)

        val allProgress = database.userProgressEntityQueries.selectAll().executeAsList()
        assertEquals(2, allProgress.size)
    }

    @Test
    fun updateProgress() {
        val now = System.currentTimeMillis()
        database.userProgressEntityQueries.insert("subj-bio", 65.0, 3, 5, 72.0, 4, 1250, now)

        val updatedTime = now + 86_400_000L
        database.userProgressEntityQueries.update(
            mastery_percent = 80.0,
            notes_reviewed = 4,
            total_notes = 5,
            avg_quiz_score = 85.0,
            current_streak = 5,
            total_xp = 1500,
            last_studied_at = updatedTime,
            subject_id = "subj-bio",
        )

        val updated = database.userProgressEntityQueries.selectBySubjectId("subj-bio").executeAsOneOrNull()
        assertNotNull(updated)
        assertEquals(80.0, updated.mastery_percent)
        assertEquals(4L, updated.notes_reviewed)
        assertEquals(85.0, updated.avg_quiz_score)
        assertEquals(5L, updated.current_streak)
        assertEquals(1500L, updated.total_xp)
        assertEquals(updatedTime, updated.last_studied_at)
    }

    @Test
    fun deleteBySubjectId() {
        val now = System.currentTimeMillis()
        database.userProgressEntityQueries.insert("subj-bio", 65.0, 3, 5, 72.0, 4, 1250, now)
        database.userProgressEntityQueries.insert("subj-cs", 78.0, 4, 5, 85.0, 7, 2100, now)

        database.userProgressEntityQueries.delete("subj-bio")

        val bioProgress = database.userProgressEntityQueries.selectBySubjectId("subj-bio").executeAsOneOrNull()
        assertNull(bioProgress)

        val csProgress = database.userProgressEntityQueries.selectBySubjectId("subj-cs").executeAsOneOrNull()
        assertNotNull(csProgress)
    }

    @Test
    fun deleteAll() {
        val now = System.currentTimeMillis()
        database.userProgressEntityQueries.insert("subj-bio", 65.0, 3, 5, 72.0, 4, 1250, now)
        database.userProgressEntityQueries.insert("subj-cs", 78.0, 4, 5, 85.0, 7, 2100, now)

        database.userProgressEntityQueries.deleteAll()

        val allProgress = database.userProgressEntityQueries.selectAll().executeAsList()
        assertTrue(allProgress.isEmpty())
    }

    @Test
    fun lastStudiedAtCanBeNull() {
        database.userProgressEntityQueries.insert("subj-bio", 0.0, 0, 0, 0.0, 0, 0, null)

        val progress = database.userProgressEntityQueries.selectBySubjectId("subj-bio").executeAsOneOrNull()
        assertNotNull(progress)
        assertNull(progress.last_studied_at)
    }

    @Test
    fun insertOrReplaceOverwritesExisting() {
        val now = System.currentTimeMillis()
        database.userProgressEntityQueries.insert("subj-bio", 65.0, 3, 5, 72.0, 4, 1250, now)
        database.userProgressEntityQueries.insert("subj-bio", 90.0, 5, 5, 95.0, 10, 3000, now + 1000)

        val allProgress = database.userProgressEntityQueries.selectAll().executeAsList()
        assertEquals(1, allProgress.size)
        assertEquals(90.0, allProgress[0].mastery_percent)
    }

    @Test
    fun selectBySubjectIdReturnsNullForNonExistent() {
        val result = database.userProgressEntityQueries.selectBySubjectId("nonexistent").executeAsOneOrNull()
        assertNull(result)
    }
}
