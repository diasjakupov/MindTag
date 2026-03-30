package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.data.local.MindTagDatabase

object SeedData {

    // Subject IDs
    private const val BIO = "subj-bio-101"
    private const val ECON = "subj-econ-101"
    private const val CS = "subj-cs-101"

    private val now = 1738886400000L // 2025-02-07 00:00 UTC
    private val twoDaysAgo = now - 172_800_000L

    fun populateSubjectsOnly(db: MindTagDatabase) {
        insertSubjects(db)
    }

    fun populate(db: MindTagDatabase) {
        insertSubjects(db)
        // Notes and semantic links now come from the server API
        // Flashcards, sessions, and answers are NOT seeded — they come from
        // saving backend quiz results via "Save to Study"
    }

    private fun insertSubjects(db: MindTagDatabase) {
        db.subjectEntityQueries.insert(BIO, "Biology 101", "#22C55E", "leaf", 0.65, 5, 3, twoDaysAgo, now)
        db.subjectEntityQueries.insert(ECON, "Economics 101", "#F59E0B", "trending_up", 0.42, 5, 2, twoDaysAgo, now)
        db.subjectEntityQueries.insert(CS, "Computer Science", "#135BEC", "code", 0.78, 5, 4, twoDaysAgo, now)
    }


}
