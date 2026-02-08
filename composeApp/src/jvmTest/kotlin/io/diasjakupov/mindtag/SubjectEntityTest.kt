package io.diasjakupov.mindtag

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SubjectEntityTest {

    private lateinit var database: MindTagDatabase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        database = MindTagDatabase(driver)
    }

    @Test
    fun insertAndSelectById() {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert(
            id = "subj-1",
            name = "Biology",
            color_hex = "#22C55E",
            icon_name = "leaf",
            progress = 0.5,
            total_notes = 10,
            reviewed_notes = 5,
            created_at = now,
            updated_at = now,
        )

        val subject = database.subjectEntityQueries.selectById("subj-1").executeAsOneOrNull()
        assertNotNull(subject)
        assertEquals("Biology", subject.name)
        assertEquals("#22C55E", subject.color_hex)
        assertEquals("leaf", subject.icon_name)
        assertEquals(0.5, subject.progress)
        assertEquals(10L, subject.total_notes)
        assertEquals(5L, subject.reviewed_notes)
    }

    @Test
    fun selectAllReturnsOrderedByName() {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-z", "Zoology", "#FF0000", "animal", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-a", "Algebra", "#0000FF", "math", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-m", "Music", "#00FF00", "music", 0.0, 0, 0, now, now)

        val subjects = database.subjectEntityQueries.selectAll().executeAsList()
        assertEquals(3, subjects.size)
        assertEquals("Algebra", subjects[0].name)
        assertEquals("Music", subjects[1].name)
        assertEquals("Zoology", subjects[2].name)
    }

    @Test
    fun updateSubject() {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-1", "Bio", "#22C55E", "leaf", 0.0, 0, 0, now, now)

        database.subjectEntityQueries.update(
            name = "Biology 101",
            color_hex = "#00FF00",
            icon_name = "science",
            progress = 0.75,
            total_notes = 20,
            reviewed_notes = 15,
            updated_at = now + 1000,
            id = "subj-1",
        )

        val updated = database.subjectEntityQueries.selectById("subj-1").executeAsOneOrNull()
        assertNotNull(updated)
        assertEquals("Biology 101", updated.name)
        assertEquals("#00FF00", updated.color_hex)
        assertEquals("science", updated.icon_name)
        assertEquals(0.75, updated.progress)
        assertEquals(20L, updated.total_notes)
        assertEquals(15L, updated.reviewed_notes)
    }

    @Test
    fun deleteSubject() {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-1", "Biology", "#22C55E", "leaf", 0.0, 0, 0, now, now)

        database.subjectEntityQueries.delete("subj-1")

        val result = database.subjectEntityQueries.selectById("subj-1").executeAsOneOrNull()
        assertNull(result)
    }

    @Test
    fun deleteAllSubjects() {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-1", "Bio", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-2", "CS", "#135BEC", "code", 0.0, 0, 0, now, now)

        database.subjectEntityQueries.deleteAll()

        val subjects = database.subjectEntityQueries.selectAll().executeAsList()
        assertTrue(subjects.isEmpty())
    }

    @Test
    fun insertOrReplaceOverwritesExisting() {
        val now = System.currentTimeMillis()
        database.subjectEntityQueries.insert("subj-1", "Bio", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        database.subjectEntityQueries.insert("subj-1", "Biology Updated", "#00FF00", "science", 0.5, 5, 3, now, now + 1000)

        val subjects = database.subjectEntityQueries.selectAll().executeAsList()
        assertEquals(1, subjects.size)
        assertEquals("Biology Updated", subjects[0].name)
    }

    @Test
    fun selectByIdReturnsNullForNonExistent() {
        val result = database.subjectEntityQueries.selectById("nonexistent").executeAsOneOrNull()
        assertNull(result)
    }
}
