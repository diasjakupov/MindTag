package io.diasjakupov.mindtag.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:mindtag.db")
        MindTagDatabase.Schema.create(driver)
        return driver
    }
}
