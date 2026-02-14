package io.diasjakupov.mindtag.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Library::class, Route.Library.serializer())
            subclass(Route.Study::class, Route.Study.serializer())
            subclass(Route.NoteCreate::class, Route.NoteCreate.serializer())
            subclass(Route.NoteDetail::class, Route.NoteDetail.serializer())
            subclass(Route.Quiz::class, Route.Quiz.serializer())
            subclass(Route.QuizResults::class, Route.QuizResults.serializer())
            subclass(Route.Auth::class, Route.Auth.serializer())
        }
    }
}
