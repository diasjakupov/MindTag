package io.diasjakupov.mindtag.feature.home.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.home.domain.model.DashboardData
import io.diasjakupov.mindtag.feature.home.domain.model.ReviewCard
import io.diasjakupov.mindtag.feature.home.domain.model.TaskType
import io.diasjakupov.mindtag.feature.home.domain.model.UpNextTask
import io.diasjakupov.mindtag.feature.home.domain.repository.DashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock

class DashboardRepositoryImpl(
    private val db: MindTagDatabase,
) : DashboardRepository {

    private val tag = "DashboardRepo"

    override fun getDashboardData(): Flow<DashboardData> {
        val subjectsFlow = db.subjectEntityQueries.selectAll()
            .asFlow().mapToList(Dispatchers.IO)
        val notesFlow = db.noteEntityQueries.selectAll()
            .asFlow().mapToList(Dispatchers.IO)
        val progressFlow = db.userProgressEntityQueries.selectAll()
            .asFlow().mapToList(Dispatchers.IO)
        val nowMillis = Clock.System.now().toEpochMilliseconds()
        val dueCardsFlow = db.flashCardEntityQueries.selectDueCards(nowMillis)
            .asFlow().mapToList(Dispatchers.IO)

        return combine(subjectsFlow, notesFlow, progressFlow, dueCardsFlow) { subjects, notes, progressList, dueCards ->
            Logger.d(tag, "getDashboardData: emitting — notes=${notes.size}, dueCards=${dueCards.size}, subjects=${subjects.size}")

            val maxStreak = progressList.maxOfOrNull { it.current_streak.toInt() } ?: 0

            val reviewCards = subjects.map { subject ->
                val subjectNotes = notes.filter { it.subject_id == subject.id }
                val progress = progressList.find { it.subject_id == subject.id }
                val dueCount = dueCards.count { it.subject_id == subject.id }
                val latestNote = subjectNotes.maxByOrNull { it.updated_at }

                ReviewCard(
                    noteId = latestNote?.id ?: subject.id,
                    noteTitle = latestNote?.title ?: "${subject.name} Notes",
                    subjectName = subject.name,
                    subjectColorHex = subject.color_hex,
                    subjectIconName = subject.icon_name,
                    progressPercent = (progress?.mastery_percent?.toFloat() ?: 0f),
                    dueCardCount = dueCount,
                    weekNumber = latestNote?.week_number?.toInt(),
                )
            }.sortedByDescending { it.dueCardCount }

            val upNextTasks = buildUpNextTasks(subjects.map { it.name }, reviewCards)

            DashboardData(
                userName = "Alex",
                totalNotesCount = notes.size,
                totalReviewsDue = dueCards.size,
                currentStreak = maxStreak,
                reviewCards = reviewCards,
                upNextTasks = upNextTasks,
            )
        }
    }

    private fun buildUpNextTasks(
        subjectNames: List<String>,
        reviewCards: List<ReviewCard>,
    ): List<UpNextTask> {
        val tasks = mutableListOf<UpNextTask>()

        // First due review subject
        val topReview = reviewCards.firstOrNull { it.dueCardCount > 0 }
        if (topReview != null) {
            tasks.add(
                UpNextTask(
                    id = "task-review-${topReview.noteId}",
                    title = "Review: ${topReview.noteTitle}",
                    subtitle = "${topReview.subjectName} • ${topReview.dueCardCount} cards due",
                    type = TaskType.REVIEW,
                ),
            )
        }

        // Quiz task for second subject
        val quizSubject = reviewCards.getOrNull(1)
        if (quizSubject != null) {
            tasks.add(
                UpNextTask(
                    id = "task-quiz-${quizSubject.noteId}",
                    title = "Flashcards: ${quizSubject.subjectName}",
                    subtitle = "Knowledge Graph",
                    type = TaskType.QUIZ,
                ),
            )
        }

        // Note task
        val noteSubject = subjectNames.firstOrNull() ?: "General"
        tasks.add(
            UpNextTask(
                id = "task-note-add",
                title = "Add new notes",
                subtitle = "Keep your knowledge fresh",
                type = TaskType.NOTE,
            ),
        )

        return tasks.take(3)
    }
}
