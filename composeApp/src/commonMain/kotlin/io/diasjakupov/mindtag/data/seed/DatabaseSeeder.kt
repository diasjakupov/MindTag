package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.core.config.DevConfig
import io.diasjakupov.mindtag.data.local.MindTagDatabase

object DatabaseSeeder {
    fun seedIfEmpty(db: MindTagDatabase) {
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
