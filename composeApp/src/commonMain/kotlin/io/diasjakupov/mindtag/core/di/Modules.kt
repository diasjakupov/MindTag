package io.diasjakupov.mindtag.core.di

import io.diasjakupov.mindtag.core.data.AppPreferences
import io.diasjakupov.mindtag.core.database.DatabaseDriverFactory
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.seed.DatabaseSeeder
import io.diasjakupov.mindtag.feature.home.data.repository.DashboardRepositoryImpl
import io.diasjakupov.mindtag.feature.home.domain.repository.DashboardRepository
import io.diasjakupov.mindtag.feature.home.domain.usecase.GetDashboardUseCase
import io.diasjakupov.mindtag.feature.home.presentation.HomeViewModel
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
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerViewModel
import io.diasjakupov.mindtag.feature.study.presentation.hub.StudyHubViewModel
import io.diasjakupov.mindtag.feature.study.presentation.quiz.QuizViewModel
import io.diasjakupov.mindtag.feature.study.presentation.results.ResultsViewModel
import io.diasjakupov.mindtag.feature.onboarding.presentation.OnboardingViewModel
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileViewModel
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

val repositoryModule = module {
    single<NoteRepository> { NoteRepositoryImpl(get()) }
    single<StudyRepository> { StudyRepositoryImpl(get()) }
    single<QuizRepository> { QuizRepositoryImpl(get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }
}

val useCaseModule = module {
    factory { GetNotesUseCase(get()) }
    factory { GetNoteWithConnectionsUseCase(get()) }
    factory { CreateNoteUseCase(get()) }
    factory { GetSubjectsUseCase(get()) }
    factory { StartQuizUseCase(get()) }
    factory { SubmitAnswerUseCase(get(), get()) }
    factory { GetResultsUseCase(get()) }
    factory { GetDashboardUseCase(get()) }
}

val viewModelModule = module {
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { (noteId: String?) -> NoteCreateViewModel(get(), get(), get(), noteId) }
    viewModel { (noteId: String) -> NoteDetailViewModel(noteId, get(), get(), get(), get()) }
    viewModel { StudyHubViewModel(get(), get()) }
    viewModel { (sessionId: String) -> QuizViewModel(sessionId, get(), get()) }
    viewModel { (sessionId: String) -> ResultsViewModel(sessionId, get()) }
    viewModel { PlannerViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
}

val coreModule = module {
    // ViewModels and shared services will be registered here
}

val appModules: List<Module> = listOf(
    databaseModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
    coreModule,
)
