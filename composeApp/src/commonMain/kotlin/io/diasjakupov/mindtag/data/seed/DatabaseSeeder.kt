package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.core.config.DevConfig
import io.diasjakupov.mindtag.data.local.MindTagDatabase

object DatabaseSeeder {
    fun seedIfEmpty(db: MindTagDatabase) {
        val count = db.subjectEntityQueries.selectAll().executeAsList().size
        if (count > 0) return

        db.transaction {
            if (DevConfig.ENABLE_SEED_DATA) {
                SeedData.populate(db)
            } else {
                SeedData.populateSubjectsOnly(db)
            }
        }
    }
}
