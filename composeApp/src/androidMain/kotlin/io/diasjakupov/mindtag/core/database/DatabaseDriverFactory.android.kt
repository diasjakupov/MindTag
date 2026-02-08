package io.diasjakupov.mindtag.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.diasjakupov.mindtag.data.local.MindTagDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(MindTagDatabase.Schema, context, "mindtag.db")
    }
}
