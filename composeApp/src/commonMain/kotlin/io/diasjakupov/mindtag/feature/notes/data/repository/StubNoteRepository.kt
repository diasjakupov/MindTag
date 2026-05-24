package io.diasjakupov.mindtag.feature.notes.data.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.random.Random

/**
 * Stub [NoteRepository] used in [io.diasjakupov.mindtag.core.config.AppEnvironment.TEST] mode.
 *
 * Provides a hardcoded set of realistic software-engineering notes so that every screen can be
 * exercised visually without a running backend. Create / update / delete mutate the in-memory list.
 *
 * Artificial network-style delays (with jitter) are added to each network-facing call so loading
 * states render on camera. No real network calls are made.
 */
class StubNoteRepository : NoteRepository {

    private val now: Long get() = Clock.System.now().toEpochMilliseconds()

    // Reference "now" captured once so the staggered timestamps stay stable for the session.
    private val base: Long = now
    private val day = 24 * 3600_000L

    private fun summaryOf(content: String): String {
        val flat = content.trim().replace(Regex("\\s+"), " ")
        return if (flat.length > 150) "${flat.take(150)}…" else flat
    }

    private fun readTime(content: String): Int =
        (content.trim().split(Regex("\\s+")).size / 200).coerceAtLeast(1)

    private fun note(
        id: Long,
        title: String,
        content: String,
        subject: String,
        weekNumber: Int,
        ageDays: Long,
    ): Note {
        val trimmed = content.trimIndent()
        return Note(
            id = id,
            title = title,
            content = trimmed,
            summary = summaryOf(trimmed),
            subjectId = subject,
            subjectName = subject,
            weekNumber = weekNumber,
            readTimeMinutes = readTime(trimmed),
            createdAt = base - ageDays * day,
            updatedAt = base - ageDays * day,
        )
    }

    private val oop = "Object-Oriented Programming"
    private val algo = "Algorithms"
    private val ds = "Data Structures"
    private val patterns = "Design Patterns"

    // ageDays staggered from ~6 weeks ago (42d) to ~2 days ago to give a believable recency order.
    private val notes = mutableListOf(
        note(
            1L, "Encapsulation and access modifiers",
            """
            Encapsulation bundles state and the methods that operate on it inside one unit and restricts direct outside access. Fields are kept private; reads and writes go through public methods so the object enforces its own invariants. Access modifiers — private, protected, public — express the contract. The payoff is that internal representation can change without breaking callers, as long as the public surface stays stable. A BankAccount that exposes deposit() and withdraw() but hides its balance field can never be pushed into a negative state by a caller that forgot a check.
            """,
            oop, 2, 40,
        ),
        note(
            2L, "Inheritance and the Liskov Substitution Principle",
            """
            Inheritance lets a subclass reuse and extend a base class. The Liskov Substitution Principle constrains it: a subtype must be usable anywhere its base type is expected, without surprising the caller. The classic violation is Square extends Rectangle — setting width on a square must also change height, which breaks code written against Rectangle's contract. LSP is why "is-a" should mean behavioural substitutability, not just shared fields. When inheritance fights the contract, prefer composition.
            """,
            oop, 3, 35,
        ),
        note(
            3L, "Polymorphism and dynamic dispatch",
            """
            Polymorphism lets one interface stand for many concrete types, so a caller works against an abstraction and the runtime picks the right implementation. Dynamic dispatch is the mechanism: the method called is resolved from the object's actual type at run time, not its declared type. A List<Shape> can hold circles and squares; calling area() runs each shape's own version. This is what removes long if/switch chains on a type tag and is the foundation most design patterns build on.
            """,
            oop, 4, 28,
        ),
        note(
            4L, "Abstraction and interfaces",
            """
            Abstraction exposes what an object does and hides how. An interface is a pure contract — method signatures with no implementation — that decouples callers from concrete classes. Code that depends on a PaymentProcessor interface does not care whether the implementation is Stripe or PayPal. This is the basis of the Dependency Inversion Principle: high-level modules depend on abstractions, not on details. Interfaces are also what make polymorphism and most patterns possible.
            """,
            oop, 2, 38,
        ),
        note(
            5L, "Big-O notation and asymptotic analysis",
            """
            Big-O describes how an algorithm's running time or space grows as the input grows, ignoring constants and lower-order terms. O(1) is constant, O(log n) logarithmic, O(n) linear, O(n log n) typical of good sorts, O(n^2) quadratic. It answers "will this still be fast when the input is a million items?" — a question that timing a single run can't. Analysis focuses on the worst case unless stated otherwise. Knowing the Big-O of the operations on a data structure is how you choose the right one.
            """,
            algo, 1, 42,
        ),
        note(
            6L, "Recursion and divide and conquer",
            """
            A recursive function solves a problem by calling itself on smaller inputs until it hits a base case. Divide and conquer is the strategy: split the problem, solve the parts, combine the results. Merge sort and binary search are textbook examples. The cost is stack depth and repeated work if subproblems overlap — which is what memoisation and dynamic programming fix. Every recursion needs a base case and a step that strictly shrinks the input, or it never terminates.
            """,
            algo, 3, 33,
        ),
        note(
            7L, "Sorting: merge sort vs quicksort",
            """
            Merge sort splits the array in half, sorts each half, and merges — guaranteed O(n log n) but needs O(n) extra space and is stable. Quicksort partitions around a pivot and recurses on each side — O(n log n) on average, O(n^2) in the worst case, but sorts in place and is usually faster in practice due to cache behaviour. The choice is a trade-off: predictable time and stability versus memory and average speed. Both are divide-and-conquer.
            """,
            algo, 5, 20,
        ),
        note(
            8L, "Graph traversal: BFS and DFS",
            """
            Breadth-first search explores a graph level by level using a queue; depth-first search goes as deep as possible before backtracking, using a stack or recursion. BFS finds the shortest path in an unweighted graph; DFS is natural for cycle detection, topological sort, and exploring connected components. Both visit every reachable node once, giving O(V + E). The data structure backing the frontier — queue versus stack — is the only real difference in the core loop.
            """,
            algo, 6, 15,
        ),
        note(
            9L, "Arrays and dynamic arrays",
            """
            An array stores elements in contiguous memory, giving O(1) random access by index but a fixed size. A dynamic array (ArrayList, Vector) wraps an array and grows by allocating a larger backing store and copying — usually doubling capacity, which makes append O(1) amortised. Insertion or deletion in the middle is O(n) because elements must shift. Arrays are the default when you need fast indexed reads and mostly append at the end.
            """,
            ds, 1, 41,
        ),
        note(
            10L, "Hash tables and collision resolution",
            """
            A hash table maps keys to buckets via a hash function, giving average O(1) insert, lookup, and delete. Collisions — two keys landing in the same bucket — are handled by chaining (a list per bucket) or open addressing (probing for the next free slot). Performance depends on a good hash function and keeping the load factor low; the table resizes when it gets too full. Worst case degrades to O(n) if everything collides. This is the structure behind most "constant-time lookup" claims.
            """,
            ds, 4, 26,
        ),
        note(
            11L, "Binary search trees",
            """
            A binary search tree keeps keys ordered: every left descendant is smaller, every right descendant larger. That invariant makes search, insert, and delete O(log n) — when the tree is balanced. An unbalanced tree degrades to a linked list at O(n), which is why self-balancing variants like AVL and red-black trees exist. In-order traversal of a BST yields the keys in sorted order. Search is recursive: compare, then go left or right.
            """,
            ds, 5, 18,
        ),
        note(
            12L, "The Strategy pattern",
            """
            Strategy defines a family of interchangeable algorithms behind a common interface and lets the client pick one at run time. Instead of a method full of if/switch branches choosing behaviour, each behaviour is its own class implementing the strategy interface. A sorter that accepts a Comparator, or a payment flow that accepts a PaymentStrategy, is Strategy in action. It leans entirely on polymorphism and favours composition over inheritance.
            """,
            patterns, 7, 9,
        ),
        note(
            13L, "The Factory Method pattern",
            """
            Factory Method defines an interface for creating an object but lets subclasses decide which concrete class to instantiate. It moves the new call out of client code, so adding a new product type doesn't force edits everywhere it's constructed. The client depends on the product interface, not the concrete class — Dependency Inversion again. It's the go-to when a class can't anticipate which objects it must create.
            """,
            patterns, 7, 6,
        ),
        note(
            14L, "The Observer pattern",
            """
            Observer sets up a one-to-many dependency: when a subject changes state, all its registered observers are notified automatically. The subject keeps a list of observers and calls a common update() on each — it doesn't know or care what they do. This decouples the thing that changes from the things that react, and it's the backbone of event systems, UI data binding, and reactive streams. Adding a new observer requires no change to the subject.
            """,
            patterns, 8, 2,
        ),
    )

    private val subjectColors = mapOf(
        oop to "#135BEC",      // blue
        algo to "#A855F7",     // purple
        ds to "#22C55E",       // green
        patterns to "#F59E0B", // amber
    )

    /**
     * Section-4 link map (F2 + F4). Each source note maps to an ordered list of related note ids.
     * Returned up to 4 deep so the "≥3 related notes" criterion always holds, with intentional
     * cross-subject links (OOP ↔ Design Patterns, Algorithms ↔ Data Structures).
     */
    private val relatedLinks: Map<Long, List<Long>> = mapOf(
        3L to listOf(12L, 13L, 14L, 4L),
        4L to listOf(3L, 12L, 13L),
        2L to listOf(13L, 3L, 1L),
        1L to listOf(4L, 2L, 10L),
        5L to listOf(10L, 7L, 11L, 9L),
        6L to listOf(11L, 7L, 8L),
        7L to listOf(9L, 5L, 6L),
        8L to listOf(6L, 11L, 5L),
        9L to listOf(10L, 7L, 5L),
        10L to listOf(5L, 9L, 1L),
        11L to listOf(6L, 5L, 8L),
        12L to listOf(3L, 4L, 13L),
        13L to listOf(2L, 4L, 12L),
        14L to listOf(3L, 4L, 12L),
    )

    /**
     * Section-5 curated AI-search map (F3). Each entry: trigger words (all must be present in the
     * lower-cased query) → ordered result note ids. Checked top-to-bottom; first match wins. Falls
     * back to substring filtering for anything unrecognised.
     */
    private data class CuratedQuery(val triggers: List<String>, val resultIds: List<Long>)

    private val curatedQueries = listOf(
        CuratedQuery(listOf("many forms", "runtime"), listOf(3L, 4L, 12L)),
        CuratedQuery(listOf("if-else", "behaviour"), listOf(12L, 3L, 4L)),
        CuratedQuery(listOf("if-else", "behavior"), listOf(12L, 3L, 4L)),
        CuratedQuery(listOf("constant-time", "lookup"), listOf(10L, 9L)),
        CuratedQuery(listOf("scales", "algorithm"), listOf(5L, 7L)),
        CuratedQuery(listOf("polymorphism"), listOf(3L, 12L, 13L)),
    )

    // ─── Read ────────────────────────────────────────────────────────────────

    override suspend fun getNotes(subjectFilter: String?): List<Note> {
        delay(Random.nextLong(500, 700)) // ~600ms — library list load → shimmer shows
        return if (subjectFilter != null) notes.filter { it.subjectId == subjectFilter }
        else notes.toList()
    }

    override suspend fun getNoteById(id: Long): Note? =
        notes.find { it.id == id }

    override suspend fun getRelatedNotes(noteId: Long): List<RelatedNote> {
        delay(Random.nextLong(700, 900)) // ~800ms — "semantic analysis" feel on note detail
        val orderedIds = relatedLinks[noteId] ?: emptyList()
        return orderedIds.mapNotNull { relatedId ->
            val related = notes.find { it.id == relatedId } ?: return@mapNotNull null
            RelatedNote(
                noteId = related.id,
                title = related.title,
                subjectName = related.subjectName,
                subjectColorHex = subjectColors[related.subjectId] ?: "#135BEC",
            )
        }
    }

    override suspend fun getSubjects(): List<Subject> {
        delay(Random.nextLong(300, 500)) // ~400ms — filter chips load
        return notes.map { it.subjectId }.distinct().map { name ->
            Subject(
                id = name,
                name = name,
                colorHex = subjectColors[name] ?: "#135BEC",
            )
        }
    }

    // ─── Search ──────────────────────────────────────────────────────────────

    override suspend fun searchNotes(query: String, page: Int, size: Int): PaginatedNotes {
        delay(Random.nextLong(350, 550)) // ~450ms — fast literal search
        val filtered = notes.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
        }
        return PaginatedNotes(notes = filtered, page = 0, hasMore = false)
    }

    override suspend fun listNotesBySubject(subject: String, page: Int, size: Int): PaginatedNotes {
        val filtered = notes.filter { it.subjectId.equals(subject, ignoreCase = true) }
        return PaginatedNotes(notes = filtered, page = 0, hasMore = false)
    }

    override suspend fun semanticSearch(query: String): List<Note> {
        delay(Random.nextLong(900, 1100)) // ~1000ms — "AI" search feels heavier than keyword
        val q = query.trim().lowercase()

        // Curated paraphrase / conceptual queries (section 5).
        val curated = curatedQueries.firstOrNull { entry ->
            entry.triggers.all { q.contains(it) }
        }
        if (curated != null) {
            return curated.resultIds.mapNotNull { id -> notes.find { it.id == id } }
        }

        // Fallback: existing substring filter.
        return notes.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
        }
    }

    // ─── Write ───────────────────────────────────────────────────────────────

    override suspend fun createNote(title: String, content: String, subjectName: String): Note {
        delay(Random.nextLong(450, 650)) // ~500ms — save latency
        val newId = (notes.maxOfOrNull { it.id } ?: 0L) + 1L
        val note = Note(
            id = newId,
            title = title,
            content = content,
            summary = summaryOf(content),
            subjectId = subjectName,
            subjectName = subjectName,
            weekNumber = null,
            readTimeMinutes = readTime(content),
            createdAt = now,
            updatedAt = now,
        )
        notes.add(note)
        return note
    }

    override suspend fun updateNote(id: Long, title: String, content: String, subjectName: String) {
        delay(Random.nextLong(450, 650)) // ~500ms — save latency
        val index = notes.indexOfFirst { it.id == id }
        if (index == -1) throw Exception("Note $id not found in stub data")
        val existing = notes[index]
        notes[index] = existing.copy(
            title = title,
            content = content,
            summary = summaryOf(content),
            subjectId = subjectName,
            subjectName = subjectName,
            updatedAt = now,
        )
    }

    override suspend fun deleteNote(id: Long) {
        notes.removeAll { it.id == id }
    }
}
