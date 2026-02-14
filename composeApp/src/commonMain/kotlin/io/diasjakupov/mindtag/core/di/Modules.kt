package io.diasjakupov.mindtag.core.di

import io.diasjakupov.mindtag.core.data.AppPreferences
import io.diasjakupov.mindtag.core.database.DatabaseDriverFactory
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.HttpClientFactory
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.seed.DatabaseSeeder
import io.diasjakupov.mindtag.feature.auth.data.AuthApi
import io.diasjakupov.mindtag.feature.auth.data.AuthRepositoryImpl
import io.diasjakupov.mindtag.feature.auth.domain.AuthRepository
import io.diasjakupov.mindtag.feature.auth.domain.LoginUseCase
import io.diasjakupov.mindtag.feature.auth.domain.RegisterUseCase
import io.diasjakupov.mindtag.feature.auth.presentation.AuthViewModel
import io.diasjakupov.mindtag.feature.notes.data.api.NoteApi
import io.diasjakupov.mindtag.feature.notes.data.repository.NoteRepositoryImpl
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import io.diasjakupov.mindtag.feature.notes.domain.usecase.CreateNoteUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetNoteWithConnectionsUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetNotesUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
import io.diasjakupov.mindtag.feature.library.presentation.LibraryViewModel
import io.diasjakupov.mindtag.feature.notes.presentation.create.NoteCreateViewModel
import io.diasjakupov.mindtag.feature.notes.presentation.detail.NoteDetailViewModel
import io.diasjakupov.mindtag.feature.study.data.repository.QuizRepositoryImpl
import io.diasjakupov.mindtag.feature.study.data.repository.StudyRepositoryImpl
import io.diasjakupov.mindtag.feature.study.domain.repository.QuizRepository
import io.diasjakupov.mindtag.feature.study.domain.repository.StudyRepository
import io.diasjakupov.mindtag.feature.study.domain.usecase.GetResultsUseCase
import io.diasjakupov.mindtag.feature.study.domain.usecase.StartQuizUseCase
import io.diasjakupov.mindtag.feature.study.domain.usecase.SubmitAnswerUseCase
import io.diasjakupov.mindtag.feature.study.presentation.hub.StudyHubViewModel
import io.diasjakupov.mindtag.feature.study.presentation.quiz.QuizViewModel
import io.diasjakupov.mindtag.feature.study.presentation.results.ResultsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single {
        MindTagDatabase(get()).also { db ->
            DatabaseSeeder.seedIfEmpty(db)
        }
    }
    single { AppPreferences(get()) }
}

val networkModule = module {
    single { AuthManager(get()) }
    single { HttpClientFactory.create(get()) }
    single { AuthApi(get(), get()) }
    single { NoteApi(get(), get()) }
}

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
}

val repositoryModule = module {
    single<NoteRepository> { NoteRepositoryImpl(get(), get()) }
    single<StudyRepository> { StudyRepositoryImpl(get()) }
    single<QuizRepository> { QuizRepositoryImpl(get()) }
}

val useCaseModule = module {
    factory { GetNotesUseCase(get()) }
    factory { GetNoteWithConnectionsUseCase(get()) }
    factory { CreateNoteUseCase(get()) }
    factory { GetSubjectsUseCase(get()) }
    factory { StartQuizUseCase(get()) }
    factory { SubmitAnswerUseCase(get(), get()) }
    factory { GetResultsUseCase(get()) }
}

val viewModelModule = module {
    viewModel { LibraryViewModel(get()) }
    viewModel { (noteId: Long?) -> NoteCreateViewModel(get(), get(), get(), noteId) }
    viewModel { (noteId: Long) -> NoteDetailViewModel(noteId, get(), get(), get(), get()) }
    viewModel { StudyHubViewModel(get(), get()) }
    viewModel { (sessionId: String) -> QuizViewModel(sessionId, get(), get()) }
    viewModel { (sessionId: String) -> ResultsViewModel(sessionId, get()) }
    viewModel { AuthViewModel(get(), get()) }
}

val appModules: List<Module> = listOf(
    databaseModule,
    networkModule,
    authModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)
