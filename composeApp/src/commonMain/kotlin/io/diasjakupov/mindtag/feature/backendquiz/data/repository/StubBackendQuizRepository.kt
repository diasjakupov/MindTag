package io.diasjakupov.mindtag.feature.backendquiz.data.repository

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.dto.AnswerRequestDto
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.AttemptStart
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuestionResult
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizDetail
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizQuestion
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizStatus
import io.diasjakupov.mindtag.feature.backendquiz.domain.model.QuizSummary
import io.diasjakupov.mindtag.feature.backendquiz.domain.repository.BackendQuizRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.random.Random

/**
 * Stub [BackendQuizRepository] used in [io.diasjakupov.mindtag.core.config.AppEnvironment.TEST] mode.
 *
 * Pre-seeded with one READY quiz per note in [StubNoteRepository] (14 quizzes total, one per note,
 * each authored to test what that specific note covers) so "Quiz Me" always lands on questions that
 * are about the note you tapped, not a generic fallback. All mutations (generate, delete, start
 * attempt, submit answers) operate on in-memory state only, with artificial network-style delays so
 * loading states render on camera.
 *
 * The attempt result deliberately forces one question to read as wrong per quiz, so the results
 * screen always shows a mixed score (typically 75-80% on the longer quizzes, 75% on 4-question
 * quizzes, 80% on the 5-question Polymorphism quiz) with both green and red detailed-analysis
 * cards plus the AI-insight block on the missed question.
 *
 * No real network calls are made.
 */
class StubBackendQuizRepository : BackendQuizRepository {

    private val now: String
        get() = Clock.System.now().toString()

    // ─── Quiz A — note 3, Polymorphism (5 questions) ──────────────────────────

    private val polymorphismQuestions = listOf(
        QuizQuestion(
            id = 301L,
            questionText = "What is dynamic dispatch?",
            options = listOf(
                "Resolving the method to call from the object's actual type at run time",
                "Choosing the method at compile time from the declared type",
                "Allocating memory for objects on the heap",
                "Compiling bytecode to machine code lazily",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 302L,
            questionText = "Polymorphism most directly lets you replace what?",
            options = listOf(
                "Long if/switch chains on a type tag",
                "Recursion with iteration",
                "Arrays with linked lists",
                "Private fields with public ones",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 303L,
            questionText = "Which is required for runtime polymorphism in most OO languages?",
            options = listOf(
                "A shared interface or base type",
                "A static method",
                "A final class",
                "A global variable",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 304L,
            questionText = "A List<Shape> calling area() on each element demonstrates —",
            options = listOf("Polymorphism", "Encapsulation", "Memoisation", "Hashing"),
            orderIndex = 3,
        ),
        QuizQuestion(
            id = 305L,
            questionText = "Which design pattern leans most directly on polymorphism?",
            options = listOf("Strategy", "Singleton", "Flyweight", "Memento"),
            orderIndex = 4,
        ),
    )

    // ─── Quiz B — note 5, Big-O (4 questions) ─────────────────────────────────

    private val bigOQuestions = listOf(
        QuizQuestion(
            id = 501L,
            questionText = "O(n log n) is the typical complexity of —",
            options = listOf(
                "Efficient comparison sorts",
                "Hash lookups",
                "Array indexing",
                "Constant work",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 502L,
            questionText = "What does Big-O ignore?",
            options = listOf(
                "Constants and lower-order terms",
                "The worst case",
                "The input size",
                "The algorithm entirely",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 503L,
            questionText = "Average-case lookup in a well-sized hash table is —",
            options = listOf("O(1)", "O(log n)", "O(n)", "O(n^2)"),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 504L,
            questionText = "Quadratic time is written as —",
            options = listOf("O(n^2)", "O(n)", "O(log n)", "O(1)"),
            orderIndex = 3,
        ),
    )

    // ─── Quiz C — note 10, Hash tables (4 questions) ──────────────────────────

    private val hashQuestions = listOf(
        QuizQuestion(
            id = 1001L,
            questionText = "Two keys hashing to the same bucket is called a —",
            options = listOf("Collision", "Rotation", "Rehash", "Probe"),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 1002L,
            questionText = "Chaining resolves collisions by —",
            options = listOf(
                "Storing a list per bucket",
                "Resizing immediately",
                "Rejecting the key",
                "Sorting the bucket",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 1003L,
            questionText = "Hash-table performance degrades to O(n) when —",
            options = listOf(
                "Most keys collide",
                "The table is empty",
                "Keys are integers",
                "The load factor is low",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 1004L,
            questionText = "The load factor measures —",
            options = listOf(
                "How full the table is",
                "The hash length",
                "The key size",
                "The probe count",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz D — note 1, Encapsulation ───────────────────────────────────────

    private val encapsulationQuestions = listOf(
        QuizQuestion(
            id = 101L,
            questionText = "What does encapsulation do?",
            options = listOf(
                "Bundles state with the methods that operate on it and restricts direct outside access",
                "Lets a subclass reuse a base class's fields",
                "Splits behaviour across multiple inheritance paths",
                "Allows one interface to stand for many types",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 102L,
            questionText = "Which access modifier blocks all outside callers?",
            options = listOf("private", "public", "protected", "package-private"),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 103L,
            questionText = "The main payoff of encapsulation is —",
            options = listOf(
                "Internal representation can change without breaking callers",
                "Faster method dispatch at run time",
                "Smaller compiled binary size",
                "Stronger compile-time type checking",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 104L,
            questionText = "A BankAccount that exposes deposit/withdraw but hides its balance can —",
            options = listOf(
                "Enforce a non-negative invariant in one place",
                "Run faster than one with a public balance field",
                "Avoid being subclassed",
                "Skip thread-safety entirely",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz E — note 2, Inheritance and the Liskov Substitution Principle ──

    private val inheritanceQuestions = listOf(
        QuizQuestion(
            id = 201L,
            questionText = "The Liskov Substitution Principle says —",
            options = listOf(
                "A subtype must be usable anywhere its base type is expected",
                "A class must inherit from at most one parent",
                "A subclass must override every method of its base",
                "A subclass must not add new methods",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 202L,
            questionText = "The classic LSP violation is —",
            options = listOf(
                "Square extends Rectangle",
                "Cat extends Animal",
                "ArrayList extends List",
                "Manager extends Employee",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 203L,
            questionText = "When inheritance fights the contract, prefer —",
            options = listOf(
                "Composition",
                "Multiple inheritance",
                "Static helpers",
                "Reflection",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 204L,
            questionText = "\"Is-a\" relationships should mean —",
            options = listOf(
                "Behavioural substitutability, not just shared fields",
                "Identical memory layout",
                "Identical constructor signatures",
                "Living in the same package",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz F — note 4, Abstraction and interfaces ──────────────────────────

    private val abstractionQuestions = listOf(
        QuizQuestion(
            id = 401L,
            questionText = "An interface is —",
            options = listOf(
                "A pure contract of method signatures with no implementation",
                "A class that cannot be instantiated but holds state",
                "A function pointer with a fixed signature",
                "A subtype of every class in the language",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 402L,
            questionText = "The Dependency Inversion Principle says high-level modules depend on —",
            options = listOf(
                "Abstractions, not concrete details",
                "The lowest-level utility class",
                "Static methods only",
                "The framework's main entry class",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 403L,
            questionText = "Code that depends on a PaymentProcessor interface does not care —",
            options = listOf(
                "Whether the implementation is Stripe or PayPal",
                "How big the order total is",
                "Whether the user is logged in",
                "What language the app is written in",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 404L,
            questionText = "Abstraction exposes —",
            options = listOf(
                "What an object does and hides how",
                "The object's memory address",
                "The object's full class hierarchy",
                "The object's serialised form",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz G — note 6, Recursion and divide and conquer ────────────────────

    private val recursionQuestions = listOf(
        QuizQuestion(
            id = 601L,
            questionText = "Every recursive function needs —",
            options = listOf(
                "A base case and a step that shrinks the input",
                "A loop counter",
                "A global accumulator",
                "A return type of void",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 602L,
            questionText = "Divide and conquer means —",
            options = listOf(
                "Split the problem, solve the parts, combine the results",
                "Try every option until one works",
                "Pre-compute every answer in advance",
                "Run multiple threads in parallel",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 603L,
            questionText = "The hidden cost of recursion is —",
            options = listOf(
                "Stack depth and repeated work on overlapping subproblems",
                "Slower garbage collection",
                "More disk I/O",
                "Larger source files",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 604L,
            questionText = "Memoisation fixes —",
            options = listOf(
                "Repeated work on overlapping subproblems",
                "Excessive stack depth",
                "Infinite recursion",
                "Poor cache locality",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz H — note 7, Sorting: merge sort vs quicksort ────────────────────

    private val sortingQuestions = listOf(
        QuizQuestion(
            id = 701L,
            questionText = "Merge sort guarantees —",
            options = listOf(
                "O(n log n) time but needs O(n) extra space",
                "O(n) time on already-sorted input",
                "O(log n) space and constant time",
                "O(n^2) worst case",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 702L,
            questionText = "Quicksort's worst case is —",
            options = listOf("O(n^2)", "O(n log n)", "O(log n)", "O(n)"),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 703L,
            questionText = "Which property does merge sort have that quicksort doesn't?",
            options = listOf(
                "Stability",
                "In-place sorting",
                "Constant extra space",
                "Cache friendliness",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 704L,
            questionText = "Both merge sort and quicksort use —",
            options = listOf(
                "Divide and conquer",
                "Hashing",
                "Greedy selection",
                "Dynamic programming",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz I — note 8, Graph traversal: BFS and DFS ────────────────────────

    private val graphTraversalQuestions = listOf(
        QuizQuestion(
            id = 801L,
            questionText = "BFS uses which data structure for its frontier?",
            options = listOf("A queue", "A stack", "A hash set", "A binary heap"),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 802L,
            questionText = "On an unweighted graph, BFS finds —",
            options = listOf(
                "The shortest path in number of edges",
                "The cheapest path by edge weight",
                "The longest acyclic path",
                "A minimum spanning tree",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 803L,
            questionText = "DFS is natural for —",
            options = listOf(
                "Cycle detection and topological sort",
                "Computing shortest paths",
                "Finding the median element",
                "Sorting an array",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 804L,
            questionText = "Both BFS and DFS run in —",
            options = listOf("O(V + E)", "O(V log V)", "O(E^2)", "O(V * E)"),
            orderIndex = 3,
        ),
    )

    // ─── Quiz J — note 9, Arrays and dynamic arrays ───────────────────────────

    private val arrayQuestions = listOf(
        QuizQuestion(
            id = 901L,
            questionText = "Random access by index in an array is —",
            options = listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 902L,
            questionText = "A dynamic array grows by —",
            options = listOf(
                "Allocating a larger backing store and copying",
                "Splitting itself into two halves",
                "Compressing existing elements in place",
                "Linking to a new tail node",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 903L,
            questionText = "Inserting in the middle of an array is —",
            options = listOf(
                "O(n) because trailing elements must shift",
                "O(1) amortised",
                "O(log n) via binary search",
                "O(n^2) in the worst case",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 904L,
            questionText = "Appending to a dynamic array is —",
            options = listOf(
                "O(1) amortised",
                "O(log n) always",
                "O(n) on every append",
                "O(n^2) when the table is full",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz K — note 11, Binary search trees ────────────────────────────────

    private val bstQuestions = listOf(
        QuizQuestion(
            id = 1101L,
            questionText = "A BST's invariant is —",
            options = listOf(
                "Every left descendant is smaller, every right descendant larger",
                "All leaves sit at the same depth",
                "Every node has exactly two children",
                "The root holds the median value",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 1102L,
            questionText = "In-order traversal of a BST yields —",
            options = listOf(
                "Keys in sorted order",
                "Keys in insertion order",
                "Keys grouped by depth",
                "A random permutation of the keys",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 1103L,
            questionText = "An unbalanced BST degrades search to —",
            options = listOf("O(n)", "O(log n)", "O(1)", "O(n log n)"),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 1104L,
            questionText = "Self-balancing BST variants include —",
            options = listOf(
                "AVL and red-black trees",
                "Quad trees and oct trees",
                "B-trees and tries",
                "Heaps and treaps",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz L — note 12, The Strategy pattern ───────────────────────────────

    private val strategyQuestions = listOf(
        QuizQuestion(
            id = 1201L,
            questionText = "Strategy replaces —",
            options = listOf(
                "if/switch branches that choose behaviour",
                "Recursion with iteration",
                "Inheritance with reflection",
                "Methods with operators",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 1202L,
            questionText = "Each strategy in the pattern is —",
            options = listOf(
                "Its own class implementing a common interface",
                "A static method on a utility class",
                "A field on the client object",
                "A free-standing global function",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 1203L,
            questionText = "Strategy favours —",
            options = listOf(
                "Composition over inheritance",
                "Inheritance over composition",
                "Global state over parameters",
                "Reflection over polymorphism",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 1204L,
            questionText = "A sorter that accepts a Comparator is —",
            options = listOf(
                "The Strategy pattern in action",
                "An Observer in disguise",
                "A Singleton variant",
                "A Factory Method",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz M — note 13, The Factory Method pattern ─────────────────────────

    private val factoryQuestions = listOf(
        QuizQuestion(
            id = 1301L,
            questionText = "Factory Method moves —",
            options = listOf(
                "The new() call out of client code",
                "Validation into the constructor",
                "State into a singleton",
                "Logic into a database trigger",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 1302L,
            questionText = "With Factory Method, the client depends on —",
            options = listOf(
                "The product interface, not the concrete class",
                "A global registry of factories",
                "The factory's internal state",
                "The runtime classloader",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 1303L,
            questionText = "Factory Method is the go-to when —",
            options = listOf(
                "A class can't anticipate which objects it must create",
                "A class needs exactly one instance",
                "A class wraps an external API",
                "A class is purely stateless",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 1304L,
            questionText = "Adding a new product type with Factory Method requires —",
            options = listOf(
                "A new factory subclass, not edits at every call site",
                "A global registry update",
                "Recompiling the client",
                "A schema migration",
            ),
            orderIndex = 3,
        ),
    )

    // ─── Quiz N — note 14, The Observer pattern ───────────────────────────────

    private val observerQuestions = listOf(
        QuizQuestion(
            id = 1401L,
            questionText = "Observer sets up a —",
            options = listOf(
                "One-to-many dependency between a subject and its observers",
                "One-to-one channel between caller and callee",
                "Two-way binding via property setters",
                "Many-to-many graph of services",
            ),
            orderIndex = 0,
        ),
        QuizQuestion(
            id = 1402L,
            questionText = "When the subject's state changes, observers are —",
            options = listOf(
                "Notified automatically via a common update() method",
                "Polled at a fixed interval",
                "Recreated from scratch",
                "Sorted by priority",
            ),
            orderIndex = 1,
        ),
        QuizQuestion(
            id = 1403L,
            questionText = "Observer is the backbone of —",
            options = listOf(
                "Event systems, UI data binding, reactive streams",
                "Sorting algorithms",
                "Lexical scoping",
                "Memory allocation",
            ),
            orderIndex = 2,
        ),
        QuizQuestion(
            id = 1404L,
            questionText = "Adding a new observer requires —",
            options = listOf(
                "No change to the subject",
                "A schema migration",
                "Recompiling the whole app",
                "Reordering all existing observers",
            ),
            orderIndex = 3,
        ),
    )

    // ─── In-memory quiz store ─────────────────────────────────────────────────

    private val quizzes = mutableListOf(
        QuizSummary(
            id = 1L,
            noteId = 3L,
            noteTitleSnapshot = "Polymorphism and dynamic dispatch",
            status = QuizStatus.READY,
            questionCount = polymorphismQuestions.size,
            createdAt = "2026-04-20T10:00:00Z",
            generatedAt = "2026-04-20T10:00:30Z",
        ),
        QuizSummary(
            id = 2L,
            noteId = 5L,
            noteTitleSnapshot = "Big-O notation and asymptotic analysis",
            status = QuizStatus.READY,
            questionCount = bigOQuestions.size,
            createdAt = "2026-04-21T14:00:00Z",
            generatedAt = "2026-04-21T14:00:45Z",
        ),
        QuizSummary(
            id = 3L,
            noteId = 10L,
            noteTitleSnapshot = "Hash tables and collision resolution",
            status = QuizStatus.READY,
            questionCount = hashQuestions.size,
            createdAt = "2026-04-22T09:00:00Z",
            generatedAt = "2026-04-22T09:00:40Z",
        ),
        QuizSummary(
            id = 4L,
            noteId = 1L,
            noteTitleSnapshot = "Encapsulation and access modifiers",
            status = QuizStatus.READY,
            questionCount = encapsulationQuestions.size,
            createdAt = "2026-04-23T08:00:00Z",
            generatedAt = "2026-04-23T08:00:30Z",
        ),
        QuizSummary(
            id = 5L,
            noteId = 2L,
            noteTitleSnapshot = "Inheritance and the Liskov Substitution Principle",
            status = QuizStatus.READY,
            questionCount = inheritanceQuestions.size,
            createdAt = "2026-04-23T11:00:00Z",
            generatedAt = "2026-04-23T11:00:30Z",
        ),
        QuizSummary(
            id = 6L,
            noteId = 4L,
            noteTitleSnapshot = "Abstraction and interfaces",
            status = QuizStatus.READY,
            questionCount = abstractionQuestions.size,
            createdAt = "2026-04-24T09:00:00Z",
            generatedAt = "2026-04-24T09:00:30Z",
        ),
        QuizSummary(
            id = 7L,
            noteId = 6L,
            noteTitleSnapshot = "Recursion and divide and conquer",
            status = QuizStatus.READY,
            questionCount = recursionQuestions.size,
            createdAt = "2026-04-25T10:00:00Z",
            generatedAt = "2026-04-25T10:00:30Z",
        ),
        QuizSummary(
            id = 8L,
            noteId = 7L,
            noteTitleSnapshot = "Sorting: merge sort vs quicksort",
            status = QuizStatus.READY,
            questionCount = sortingQuestions.size,
            createdAt = "2026-04-26T13:00:00Z",
            generatedAt = "2026-04-26T13:00:30Z",
        ),
        QuizSummary(
            id = 9L,
            noteId = 8L,
            noteTitleSnapshot = "Graph traversal: BFS and DFS",
            status = QuizStatus.READY,
            questionCount = graphTraversalQuestions.size,
            createdAt = "2026-04-27T11:00:00Z",
            generatedAt = "2026-04-27T11:00:30Z",
        ),
        QuizSummary(
            id = 10L,
            noteId = 9L,
            noteTitleSnapshot = "Arrays and dynamic arrays",
            status = QuizStatus.READY,
            questionCount = arrayQuestions.size,
            createdAt = "2026-04-28T09:00:00Z",
            generatedAt = "2026-04-28T09:00:30Z",
        ),
        QuizSummary(
            id = 11L,
            noteId = 11L,
            noteTitleSnapshot = "Binary search trees",
            status = QuizStatus.READY,
            questionCount = bstQuestions.size,
            createdAt = "2026-04-29T15:00:00Z",
            generatedAt = "2026-04-29T15:00:30Z",
        ),
        QuizSummary(
            id = 12L,
            noteId = 12L,
            noteTitleSnapshot = "The Strategy pattern",
            status = QuizStatus.READY,
            questionCount = strategyQuestions.size,
            createdAt = "2026-04-30T10:00:00Z",
            generatedAt = "2026-04-30T10:00:30Z",
        ),
        QuizSummary(
            id = 13L,
            noteId = 13L,
            noteTitleSnapshot = "The Factory Method pattern",
            status = QuizStatus.READY,
            questionCount = factoryQuestions.size,
            createdAt = "2026-05-01T09:00:00Z",
            generatedAt = "2026-05-01T09:00:30Z",
        ),
        QuizSummary(
            id = 14L,
            noteId = 14L,
            noteTitleSnapshot = "The Observer pattern",
            status = QuizStatus.READY,
            questionCount = observerQuestions.size,
            createdAt = "2026-05-02T14:00:00Z",
            generatedAt = "2026-05-02T14:00:30Z",
        ),
    )

    private val attempts = mutableListOf<AttemptResult>()
    private var nextAttemptId = 100L
    private var nextQuizId = 15L

    private val letters = listOf("A", "B", "C", "D")

    // ─── Quiz CRUD ────────────────────────────────────────────────────────────

    override suspend fun generateQuiz(noteId: Long): ApiResult<QuizSummary> {
        delay(Random.nextLong(1400, 1600)) // ~1500ms — kicks off "Generating quiz..." status
        // Demo: if a quiz already exists for this note (pre-seeded for notes 3/5/10), return it so
        // the "Quiz Me" loading sequence plays through and lands on the curated question set instead
        // of erroring out with a 409.
        val existing = quizzes.find { it.noteId == noteId && it.status != QuizStatus.ERROR }
        if (existing != null) {
            return ApiResult.Success(existing)
        }
        val newQuiz = QuizSummary(
            id = nextQuizId++,
            noteId = noteId,
            noteTitleSnapshot = "Stub Note $noteId",
            status = QuizStatus.READY,
            questionCount = polymorphismQuestions.size,
            createdAt = now,
            generatedAt = now,
        )
        quizzes.add(newQuiz)
        return ApiResult.Success(newQuiz)
    }

    override suspend fun pollUntilReady(
        quizId: Long,
        maxAttempts: Int,
        intervalMs: Long,
    ): ApiResult<QuizSummary> {
        delay(Random.nextLong(2000, 2400)) // ~2200ms — "Waiting for AI..." marquee F5 loading moment
        val quiz = quizzes.find { it.id == quizId }
            ?: return ApiResult.Error("Quiz $quizId not found", 404)
        return ApiResult.Success(quiz)
    }

    override suspend fun getQuiz(quizId: Long): ApiResult<QuizDetail> {
        val summary = quizzes.find { it.id == quizId }
            ?: return ApiResult.Error("Quiz $quizId not found", 404)
        val questions = questionsForQuiz(quizId)
        return ApiResult.Success(
            QuizDetail(
                id = summary.id,
                noteId = summary.noteId,
                noteTitleSnapshot = summary.noteTitleSnapshot,
                status = summary.status,
                questions = questions,
                createdAt = summary.createdAt,
                generatedAt = summary.generatedAt,
            )
        )
    }

    override suspend fun getAllQuizzes(): ApiResult<List<QuizSummary>> =
        ApiResult.Success(quizzes.toList())

    override suspend fun getQuizzesForNote(noteId: Long): ApiResult<List<QuizSummary>> =
        ApiResult.Success(quizzes.filter { it.noteId == noteId })

    override suspend fun deleteQuiz(quizId: Long): ApiResult<Unit> {
        val removed = quizzes.removeAll { it.id == quizId }
        return if (removed) ApiResult.Success(Unit)
        else ApiResult.Error("Quiz $quizId not found", 404)
    }

    // ─── Attempts ─────────────────────────────────────────────────────────────

    override suspend fun startAttempt(quizId: Long): ApiResult<AttemptStart> {
        delay(Random.nextLong(450, 650)) // ~500ms — "Starting quiz..."
        val summary = quizzes.find { it.id == quizId }
            ?: return ApiResult.Error("Quiz $quizId not found", 404)
        if (summary.status != QuizStatus.READY) {
            return ApiResult.Error("Quiz is not in READY status", 409)
        }
        return ApiResult.Success(
            AttemptStart(
                attemptId = nextAttemptId++,
                quizId = quizId,
                noteTitleSnapshot = summary.noteTitleSnapshot,
                startedAt = now,
                questions = questionsForQuiz(quizId),
            )
        )
    }

    override suspend fun submitAttempt(
        quizId: Long,
        attemptId: Long,
        answers: List<AnswerRequestDto>,
    ): ApiResult<AttemptResult> {
        delay(Random.nextLong(1000, 1200)) // ~1100ms — server scoring
        val questions = questionsForQuiz(quizId)
        val correctAnswers = correctAnswersForQuiz(quizId)
        // Question ids that are forced to read as WRONG so the result is a believable mix
        // (~75-80%). These also feed the F6 spaced-repetition "wrong material resurfaces" story.
        val forcedWrong = forcedWrongForQuiz(quizId)

        val questionResults = questions.map { q ->
            val correct = correctAnswers[q.id] ?: "A"
            // Hardcode the demo answers: every question reads as correct except the forced-wrong
            // ones, which take a deterministic incorrect option. This guarantees the green/red mix
            // on camera regardless of what the user tapped.
            val userAnswer = if (q.id in forcedWrong) {
                letters.firstOrNull { it != correct } ?: correct
            } else {
                correct
            }
            QuestionResult(
                questionId = q.id,
                questionText = q.questionText,
                options = q.options,
                userAnswer = userAnswer,
                correctAnswer = correct,
                correct = userAnswer == correct,
                explanation = explanationForQuestion(q.id),
            )
        }

        val correctCount = questionResults.count { it.correct }
        val score = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0

        val result = AttemptResult(
            attemptId = attemptId,
            quizId = quizId,
            noteTitleSnapshot = quizzes.find { it.id == quizId }?.noteTitleSnapshot ?: "",
            score = score,
            totalQuestions = questions.size,
            correctAnswers = correctCount,
            startedAt = now,
            completedAt = now,
            questionResults = questionResults,
        )
        attempts.add(result)
        return ApiResult.Success(result)
    }

    override suspend fun getAttemptResult(quizId: Long, attemptId: Long): ApiResult<AttemptResult> {
        delay(Random.nextLong(700, 900)) // ~800ms — result fetch
        val result = attempts.find { it.attemptId == attemptId && it.quizId == quizId }
            ?: return ApiResult.Error("Attempt $attemptId not found", 404)
        return ApiResult.Success(result)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun questionsForQuiz(quizId: Long): List<QuizQuestion> = when (quizId) {
        1L -> polymorphismQuestions
        2L -> bigOQuestions
        3L -> hashQuestions
        4L -> encapsulationQuestions
        5L -> inheritanceQuestions
        6L -> abstractionQuestions
        7L -> recursionQuestions
        8L -> sortingQuestions
        9L -> graphTraversalQuestions
        10L -> arrayQuestions
        11L -> bstQuestions
        12L -> strategyQuestions
        13L -> factoryQuestions
        14L -> observerQuestions
        else -> polymorphismQuestions // default for dynamically-generated stub quizzes
    }

    /** Maps questionId -> correct answer letter (A/B/C/D based on correct option index). */
    private fun correctAnswersForQuiz(quizId: Long): Map<Long, String> {
        // Every authored question puts the correct option at index 0, so all map to "A".
        val correctIndices: Map<Long, Int> = when (quizId) {
            1L -> mapOf(301L to 0, 302L to 0, 303L to 0, 304L to 0, 305L to 0)
            2L -> mapOf(501L to 0, 502L to 0, 503L to 0, 504L to 0)
            3L -> mapOf(1001L to 0, 1002L to 0, 1003L to 0, 1004L to 0)
            4L -> mapOf(101L to 0, 102L to 0, 103L to 0, 104L to 0)
            5L -> mapOf(201L to 0, 202L to 0, 203L to 0, 204L to 0)
            6L -> mapOf(401L to 0, 402L to 0, 403L to 0, 404L to 0)
            7L -> mapOf(601L to 0, 602L to 0, 603L to 0, 604L to 0)
            8L -> mapOf(701L to 0, 702L to 0, 703L to 0, 704L to 0)
            9L -> mapOf(801L to 0, 802L to 0, 803L to 0, 804L to 0)
            10L -> mapOf(901L to 0, 902L to 0, 903L to 0, 904L to 0)
            11L -> mapOf(1101L to 0, 1102L to 0, 1103L to 0, 1104L to 0)
            12L -> mapOf(1201L to 0, 1202L to 0, 1203L to 0, 1204L to 0)
            13L -> mapOf(1301L to 0, 1302L to 0, 1303L to 0, 1304L to 0)
            14L -> mapOf(1401L to 0, 1402L to 0, 1403L to 0, 1404L to 0)
            else -> mapOf(301L to 0, 302L to 0, 303L to 0, 304L to 0, 305L to 0)
        }
        return correctIndices.mapValues { (_, idx) ->
            letters.getOrElse(idx) { "A" }
        }
    }

    /**
     * Question ids forced to read as wrong on the result screen, to produce a believable mixed
     * score (75-80% for 4-5 question quizzes). The forced-wrong question varies across quizzes so
     * the result screen highlights different concepts on camera each time.
     */
    private fun forcedWrongForQuiz(quizId: Long): Set<Long> = when (quizId) {
        1L -> setOf(303L)  // missed "shared interface/base type"
        2L -> setOf(504L)  // missed "O(n^2)"
        3L -> setOf(1003L) // missed "most keys collide"
        4L -> setOf(103L)  // missed "internal representation can change"
        5L -> setOf(202L)  // missed "Square extends Rectangle"
        6L -> setOf(402L)  // missed "abstractions, not concrete details"
        7L -> setOf(603L)  // missed "stack depth and repeated work"
        8L -> setOf(703L)  // missed "stability"
        9L -> setOf(803L)  // missed "cycle detection and topological sort"
        10L -> setOf(904L) // missed "O(1) amortised"
        11L -> setOf(1102L) // missed "keys in sorted order"
        12L -> setOf(1202L) // missed "common interface"
        13L -> setOf(1302L) // missed "product interface, not concrete class"
        14L -> setOf(1402L) // missed "notified automatically via update()"
        else -> setOf(303L)
    }

    private fun explanationForQuestion(questionId: Long): String = when (questionId) {
        // Quiz 1 — Polymorphism (note 3)
        301L -> "Dynamic dispatch resolves the method to call from the object's actual runtime type, not its declared type."
        302L -> "Polymorphism removes long if/switch chains on a type tag by dispatching on type."
        303L -> "Runtime polymorphism needs a shared interface or base type the caller programs against."
        304L -> "Calling area() on each Shape runs that shape's own implementation — polymorphism."
        305L -> "Strategy swaps interchangeable algorithms behind one interface, leaning entirely on polymorphism."
        // Quiz 2 — Big-O (note 5)
        501L -> "Efficient comparison sorts like merge sort and heapsort run in O(n log n)."
        502L -> "Big-O drops constants and lower-order terms, keeping only the dominant growth rate."
        503L -> "A well-sized hash table gives average O(1) lookup thanks to direct bucket addressing."
        504L -> "Quadratic time is O(n^2) — runtime grows with the square of the input size."
        // Quiz 3 — Hash tables (note 10)
        1001L -> "Two keys landing in the same bucket is a collision, resolved by chaining or probing."
        1002L -> "Chaining stores a list (or chain) of entries per bucket so colliding keys coexist."
        1003L -> "When most keys collide, lookups walk long chains and degrade from O(1) to O(n)."
        1004L -> "The load factor is the ratio of stored entries to buckets — how full the table is."
        // Quiz 4 — Encapsulation (note 1)
        101L -> "Encapsulation bundles state with the methods that operate on it and restricts direct outside access."
        102L -> "Private members are inaccessible from outside the class; reads and writes go through public methods."
        103L -> "Encapsulation lets internal representation change without breaking callers, as long as the public surface stays stable."
        104L -> "Hiding the balance behind deposit/withdraw lets the object enforce its non-negative invariant in one place."
        // Quiz 5 — Inheritance / LSP (note 2)
        201L -> "LSP: a subtype must be usable anywhere its base type is expected, without surprising the caller."
        202L -> "Square extends Rectangle is the textbook LSP violation — setting width on a square also has to change height, breaking Rectangle's contract."
        203L -> "When inheritance fights the contract, composition is preferred — it avoids LSP-violating subclasses."
        204L -> "'Is-a' should mean behavioural substitutability, not just shared fields, or LSP breaks."
        // Quiz 6 — Abstraction (note 4)
        401L -> "An interface is a pure contract — method signatures with no implementation — that decouples callers from concrete classes."
        402L -> "Dependency Inversion: high-level modules depend on abstractions, not on concrete details."
        403L -> "Code against an interface doesn't care which concrete implementation backs it (Stripe, PayPal, …)."
        404L -> "Abstraction exposes what an object does and hides how it does it."
        // Quiz 7 — Recursion (note 6)
        601L -> "Every recursion needs a base case and a step that strictly shrinks the input, or it never terminates."
        602L -> "Divide and conquer splits the problem, solves the parts recursively, and combines the results."
        603L -> "Recursion costs stack depth, and recurring on overlapping subproblems wastes work."
        604L -> "Memoisation caches subproblem results so overlapping subproblems aren't recomputed — the basis of dynamic programming."
        // Quiz 8 — Sorting (note 7)
        701L -> "Merge sort is guaranteed O(n log n) but allocates O(n) extra space for merging."
        702L -> "Quicksort's worst case is O(n^2), though it averages O(n log n) and usually beats merge sort in practice."
        703L -> "Merge sort is stable; quicksort generally isn't — equal keys can swap order during partitioning."
        704L -> "Both merge sort and quicksort are divide-and-conquer algorithms."
        // Quiz 9 — BFS/DFS (note 8)
        801L -> "BFS explores level by level using a queue."
        802L -> "On an unweighted graph, BFS finds the shortest path counted in edges."
        803L -> "DFS goes deep before backtracking, which makes it natural for cycle detection and topological sort."
        804L -> "Both BFS and DFS visit every reachable node and edge once, giving O(V + E)."
        // Quiz 10 — Arrays (note 9)
        901L -> "Contiguous memory means array indexing is O(1) — a direct address calculation."
        902L -> "A dynamic array allocates a larger backing store and copies its elements over when it runs out of room."
        903L -> "Inserting in the middle is O(n) because every trailing element has to shift over by one."
        904L -> "Append is O(1) amortised — doubling on resize spreads the copy cost across many cheap appends."
        // Quiz 11 — BST (note 11)
        1101L -> "A BST keeps every left descendant smaller and every right descendant larger than the node."
        1102L -> "In-order traversal visits left, node, right — yielding the keys in sorted order."
        1103L -> "An unbalanced BST degrades to a linked list, dragging search to O(n)."
        1104L -> "AVL and red-black trees are self-balancing BST variants that keep operations O(log n)."
        // Quiz 12 — Strategy (note 12)
        1201L -> "Strategy replaces a method full of if/switch branches on a type tag with one class per behaviour."
        1202L -> "Each strategy is its own class implementing the shared strategy interface — interchangeable behind that contract."
        1203L -> "Strategy favours composition over inheritance: pick a strategy at run time instead of subclassing."
        1204L -> "A sorter that accepts a Comparator is textbook Strategy — the comparison rule is pluggable."
        // Quiz 13 — Factory Method (note 13)
        1301L -> "Factory Method moves the new() call out of client code so callers stay decoupled from concrete classes."
        1302L -> "The client depends on the product interface; the concrete class is chosen by the factory."
        1303L -> "Use Factory Method when a class can't anticipate which concrete objects it must create."
        1304L -> "Adding a new product type means writing a new factory subclass — the client code doesn't change."
        // Quiz 14 — Observer (note 14)
        1401L -> "Observer establishes a one-to-many dependency: one subject, many observers."
        1402L -> "When the subject changes state, it calls a common update() on every registered observer automatically."
        1403L -> "Observer underpins event systems, UI data binding, and reactive streams."
        1404L -> "Observers register themselves — adding a new one requires no change to the subject."
        else -> "See your notes for a detailed explanation."
    }
}
