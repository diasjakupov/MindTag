package io.diasjakupov.mindtag.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Home::class, Route.Home.serializer())
            subclass(Route.Library::class, Route.Library.serializer())
            subclass(Route.Practice::class, Route.Practice.serializer())
            subclass(Route.Planner::class, Route.Planner.serializer())
            subclass(Route.Profile::class, Route.Profile.serializer())
            subclass(Route.NoteCreate::class, Route.NoteCreate.serializer())
            subclass(Route.NoteDetail::class, Route.NoteDetail.serializer())
            subclass(Route.Quiz::class, Route.Quiz.serializer())
            subclass(Route.QuizResults::class, Route.QuizResults.serializer())
            subclass(Route.Onboarding::class, Route.Onboarding.serializer())
        }
    }
}
