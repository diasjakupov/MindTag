package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.core.config.DevConfig
import io.diasjakupov.mindtag.data.local.MindTagDatabase

object DatabaseSeeder {
    fun seedIfEmpty(db: MindTagDatabase) {
        if (!DevConfig.ENABLE_SEED_DATA) return
        val count = db.subjectEntityQueries.selectAll().executeAsList().size
        if (count == 0) {
            db.transaction {
                SeedData.populate(db)
            }
        }
    }
}
