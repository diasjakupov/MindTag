package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.core.config.DevConfig
import io.diasjakupov.mindtag.data.local.MindTagDatabase

object DatabaseSeeder {

    // Legacy hardcoded subject IDs that used to be seeded into the local DB.
    // Deleted on every launch so existing installations stop showing them as
    // chips on the Study Hub screen.
    private val LEGACY_SEED_SUBJECT_IDS = listOf(
        "subj-bio-101",
        "subj-econ-101",
        "subj-cs-101",
    )

    fun seedIfEmpty(db: MindTagDatabase) {
        db.transaction {
            LEGACY_SEED_SUBJECT_IDS.forEach { db.subjectEntityQueries.delete(it) }
        }

        if (db.subjectEntityQueries.selectAll().executeAsList().isNotEmpty()) return

        db.transaction {
            if (DevConfig.ENABLE_SEED_DATA) {
                SeedData.populate(db)
            } else {
                SeedData.populateSubjectsOnly(db)
            }
        }
    }
}
