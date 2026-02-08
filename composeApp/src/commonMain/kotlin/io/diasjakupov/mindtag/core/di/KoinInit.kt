package io.diasjakupov.mindtag.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(platformModule: Module) {
    startKoin {
        modules(listOf(platformModule) + appModules)
    }
}
