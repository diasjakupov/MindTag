package io.diasjakupov.mindtag.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(MindTagDatabase.Schema, "mindtag.db")
    }
}
