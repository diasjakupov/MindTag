package io.diasjakupov.mindtag

import androidx.compose.ui.window.ComposeUIViewController
import io.diasjakupov.mindtag.core.database.DatabaseDriverFactory
import io.diasjakupov.mindtag.core.di.initKoin
import org.koin.dsl.module

fun MainViewController(): platform.UIKit.UIViewController {
    initKoin(module {
        single { DatabaseDriverFactory() }
    })
    return ComposeUIViewController { App() }
}
