package io.diasjakupov.mindtag.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = File("mindtag.db")
        val dbExists = dbFile.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:mindtag.db")
        if (!dbExists) {
            MindTagDatabase.Schema.create(driver)
        }
        return driver
    }
}
