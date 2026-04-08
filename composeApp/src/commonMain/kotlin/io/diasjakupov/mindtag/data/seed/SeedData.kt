package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.data.local.MindTagDatabase

object SeedData {

    fun populateSubjectsOnly(db: MindTagDatabase) {
        // Subjects are populated dynamically from notes fetched via the server API
        // (see NoteRepositoryImpl.cacheNotes). Nothing is seeded locally.
    }

    fun populate(db: MindTagDatabase) {
        // Notes, subjects, and semantic links all come from the server API.
        // Flashcards, sessions, and answers come from saving backend quiz results
        // via "Save to Study". Nothing is seeded locally.
    }
}
