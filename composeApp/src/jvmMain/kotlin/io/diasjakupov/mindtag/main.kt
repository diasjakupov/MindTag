package io.diasjakupov.mindtag

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.diasjakupov.mindtag.core.database.DatabaseDriverFactory
import io.diasjakupov.mindtag.core.di.initKoin
import io.diasjakupov.mindtag.core.network.TokenStorage
import org.koin.dsl.module

fun main() {
    initKoin(module {
        single { DatabaseDriverFactory() }
        single { TokenStorage() }
    })
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Mindtag",
        ) {
            App()
        }
    }
}
