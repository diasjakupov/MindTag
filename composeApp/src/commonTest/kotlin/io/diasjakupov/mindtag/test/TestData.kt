package io.diasjakupov.mindtag.test

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.home.domain.model.DashboardData
import io.diasjakupov.mindtag.feature.home.domain.model.ReviewCard
import io.diasjakupov.mindtag.feature.home.domain.model.TaskType
import io.diasjakupov.mindtag.feature.home.domain.model.UpNextTask
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.study.domain.model.AnswerOption
import io.diasjakupov.mindtag.feature.study.domain.model.CardType
import io.diasjakupov.mindtag.feature.study.domain.model.ConfidenceRating
import io.diasjakupov.mindtag.feature.study.domain.model.Difficulty
import io.diasjakupov.mindtag.feature.study.domain.model.FlashCard
import io.diasjakupov.mindtag.feature.study.domain.model.QuizAnswer
import io.diasjakupov.mindtag.feature.study.domain.model.QuizAnswerDetail
import io.diasjakupov.mindtag.feature.study.domain.model.SessionResult
import io.diasjakupov.mindtag.feature.study.domain.model.SessionStatus
import io.diasjakupov.mindtag.feature.study.domain.model.SessionType
import io.diasjakupov.mindtag.feature.study.domain.model.StudySession

object TestData {

    // --- Subjects ---

    val mathSubject = Subject(
        id = "subj-1",
        name = "Mathematics",
        colorHex = "#FF5733",
        iconName = "calculate",
    )

    val physicsSubject = Subject(
        id = "subj-2",
        name = "Physics",
        colorHex = "#33A1FF",
        iconName = "science",
    )

    val subjects = listOf(mathSubject, physicsSubject)

    // --- Notes ---

    val algebraNote = Note(
        id = "note-1",
        title = "Linear Algebra Basics",
        content = "Vectors, matrices, and transformations...",
        summary = "Core linear algebra concepts",
        subjectId = "subj-1",
        weekNumber = 1,
        readTimeMinutes = 5,
        createdAt = 1700000000000L,
        updatedAt = 1700000000000L,
    )

    val calculusNote = Note(
        id = "note-2",
        title = "Calculus Fundamentals",
        content = "Limits, derivatives, and integrals...",
        summary = "Intro to calculus",
        subjectId = "subj-1",
        weekNumber = 2,
        readTimeMinutes = 8,
        createdAt = 1700100000000L,
        updatedAt = 1700100000000L,
    )

    val mechanicsNote = Note(
        id = "note-3",
        title = "Newtonian Mechanics",
        content = "Force, motion, energy...",
        summary = "Classical mechanics overview",
        subjectId = "subj-2",
        weekNumber = 1,
        readTimeMinutes = 6,
        createdAt = 1700200000000L,
        updatedAt = 1700200000000L,
    )

    val notes = listOf(algebraNote, calculusNote, mechanicsNote)

    // --- Related Notes ---

    val relatedNote = RelatedNote(
        noteId = "note-2",
        title = "Calculus Fundamentals",
        subjectName = "Mathematics",
        subjectIconName = "calculate",
        subjectColorHex = "#FF5733",
        similarityScore = 0.85f,
    )

    // --- FlashCards ---

    val flashCard1 = FlashCard(
        id = "card-1",
        question = "What is a vector?",
        type = CardType.MULTIPLE_CHOICE,
        difficulty = Difficulty.EASY,
        subjectId = "subj-1",
        correctAnswer = "A quantity with magnitude and direction",
        options = listOf(
            AnswerOption("opt-1", "A scalar value", false),
            AnswerOption("opt-2", "A quantity with magnitude and direction", true),
            AnswerOption("opt-3", "A type of matrix", false),
        ),
        sourceNoteIds = listOf("note-1"),
        aiExplanation = "Vectors represent both magnitude and direction in space.",
        easeFactor = 2.5f,
        intervalDays = 1,
        repetitions = 0,
        nextReviewAt = null,
    )

    val flashCard2 = FlashCard(
        id = "card-2",
        question = "Newton's second law states F = ?",
        type = CardType.FACT_CHECK,
        difficulty = Difficulty.MEDIUM,
        subjectId = "subj-2",
        correctAnswer = "ma",
        options = emptyList(),
        sourceNoteIds = listOf("note-3"),
        aiExplanation = null,
        easeFactor = 2.5f,
        intervalDays = 1,
        repetitions = 0,
        nextReviewAt = null,
    )

    val flashCards = listOf(flashCard1, flashCard2)

    // --- Study Sessions ---

    val activeSession = StudySession(
        id = "session-1",
        subjectId = "subj-1",
        sessionType = SessionType.QUICK_QUIZ,
        startedAt = 1700000000000L,
        finishedAt = null,
        totalQuestions = 10,
        timeLimitSeconds = null,
        status = SessionStatus.IN_PROGRESS,
    )

    val completedSession = StudySession(
        id = "session-2",
        subjectId = "subj-2",
        sessionType = SessionType.EXAM_MODE,
        startedAt = 1700000000000L,
        finishedAt = 1700001800000L,
        totalQuestions = 5,
        timeLimitSeconds = 1800,
        status = SessionStatus.COMPLETED,
    )

    // --- Quiz Answers ---

    val correctAnswer = QuizAnswer(
        id = "ans-1",
        sessionId = "session-1",
        cardId = "card-1",
        userAnswer = "A quantity with magnitude and direction",
        isCorrect = true,
        confidenceRating = ConfidenceRating.EASY,
        timeSpentSeconds = 12,
        answeredAt = 1700000012000L,
    )

    val wrongAnswer = QuizAnswer(
        id = "ans-2",
        sessionId = "session-1",
        cardId = "card-2",
        userAnswer = "mv",
        isCorrect = false,
        confidenceRating = ConfidenceRating.HARD,
        timeSpentSeconds = 25,
        answeredAt = 1700000037000L,
    )

    // --- Session Results ---

    val sessionResult = SessionResult(
        session = completedSession,
        scorePercent = 80,
        totalCorrect = 4,
        totalQuestions = 5,
        timeSpentFormatted = "15:00",
        xpEarned = 50,
        currentStreak = 3,
        answers = listOf(
            QuizAnswerDetail(
                cardId = "card-1",
                question = "What is a vector?",
                userAnswer = "A quantity with magnitude and direction",
                correctAnswer = "A quantity with magnitude and direction",
                isCorrect = true,
                aiInsight = null,
            ),
        ),
    )

    // --- Dashboard ---

    val reviewCard = ReviewCard(
        noteId = "note-1",
        noteTitle = "Linear Algebra Basics",
        subjectName = "Mathematics",
        subjectColorHex = "#FF5733",
        subjectIconName = "calculate",
        progressPercent = 0.6f,
        dueCardCount = 3,
        weekNumber = 1,
    )

    val upNextTask = UpNextTask(
        id = "task-1",
        title = "Review Linear Algebra",
        subtitle = "3 cards due",
        type = TaskType.REVIEW,
    )

    val dashboardData = DashboardData(
        userName = "Test User",
        totalNotesCount = 3,
        totalReviewsDue = 5,
        currentStreak = 3,
        reviewCards = listOf(reviewCard),
        upNextTasks = listOf(upNextTask),
    )
}
