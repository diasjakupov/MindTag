package io.diasjakupov.mindtag.feature.notes.data.repository

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.PaginatedNotes
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import io.diasjakupov.mindtag.feature.notes.domain.repository.NoteRepository
import kotlinx.datetime.Clock

/**
 * Stub [NoteRepository] used in [io.diasjakupov.mindtag.core.config.AppEnvironment.TEST] mode.
 *
 * Provides a hardcoded set of realistic notes so that every screen can be exercised visually
 * without a running backend. Create / update / delete mutate the in-memory list.
 *
 * No network calls are made.
 */
class StubNoteRepository : NoteRepository {

    private val now: Long get() = Clock.System.now().toEpochMilliseconds()

    private val notes = mutableListOf(
        Note(
            id = 1L,
            title = "Introduction to Machine Learning",
            content = """
                Machine learning (ML) is a branch of artificial intelligence (AI) focused on building
                systems that learn from data and improve their performance over time without being
                explicitly programmed.

                Core paradigms:
                  • Supervised Learning – the model trains on labelled input-output pairs
                    (e.g. spam classification, image recognition).
                  • Unsupervised Learning – the model finds hidden structure in unlabelled data
                    (e.g. clustering, dimensionality reduction).
                  • Reinforcement Learning – an agent learns by interacting with an environment
                    and receiving reward signals (e.g. game playing, robotics).

                Key algorithms include Linear Regression, Decision Trees, Random Forests, SVMs,
                and Neural Networks. Model evaluation uses metrics such as accuracy, precision,
                recall, F1 score, and AUC-ROC.
            """.trimIndent(),
            summary = "Overview of machine learning paradigms: supervised, unsupervised, and reinforcement learning.",
            subjectId = "Computer Science",
            subjectName = "Computer Science",
            weekNumber = 1,
            readTimeMinutes = 4,
            createdAt = now - 7 * 24 * 3600_000L,
            updatedAt = now - 7 * 24 * 3600_000L,
        ),
        Note(
            id = 2L,
            title = "Gradient Descent and Optimisation",
            content = """
                Gradient descent is the workhorse optimisation algorithm behind most modern ML models.

                The idea is simple: iteratively move the parameters θ in the direction of the steepest
                descent of the loss surface — i.e. subtract a fraction (learning rate η) of the gradient:
                    θ = θ - η ∇L(θ)

                Variants:
                  • Batch GD   – computes the gradient over the full dataset (slow but stable).
                  • SGD        – uses a single random sample per step (fast but noisy).
                  • Mini-batch GD – the practical compromise; batch sizes of 32–512 are common.

                Adaptive methods (Adam, RMSProp, AdaGrad) adjust the learning rate per parameter,
                which generally converges faster and is less sensitive to the initial η choice.

                Common pitfalls: vanishing / exploding gradients, saddle points, poor learning rate
                selection. Techniques such as gradient clipping, batch normalisation, and learning rate
                schedulers address these.
            """.trimIndent(),
            summary = "How gradient descent and its variants optimise the loss function during training.",
            subjectId = "Computer Science",
            subjectName = "Computer Science",
            weekNumber = 2,
            readTimeMinutes = 5,
            createdAt = now - 5 * 24 * 3600_000L,
            updatedAt = now - 5 * 24 * 3600_000L,
        ),
        Note(
            id = 3L,
            title = "Transformer Architecture",
            content = """
                Introduced in "Attention Is All You Need" (Vaswani et al., 2017), the Transformer
                is the foundational architecture behind large language models (LLMs) such as GPT and BERT.

                Key components:
                  1. Token Embedding – maps discrete tokens to dense vectors.
                  2. Positional Encoding – injects sequence order information.
                  3. Multi-Head Self-Attention – allows each position to attend to all others.
                     Attention(Q, K, V) = softmax(QKᵀ / √dₖ) V
                  4. Feed-Forward Sub-layer – two linear layers with ReLU in between.
                  5. Layer Normalisation & Residual Connections – stabilise and speed up training.

                Encoder-only (BERT), Decoder-only (GPT), and Encoder-Decoder (T5) variants exist.
                Scaling laws show that performance improves predictably with model size and data volume.
            """.trimIndent(),
            summary = "Architecture overview of the Transformer model — attention, positional encoding, and scaling.",
            subjectId = "Computer Science",
            subjectName = "Computer Science",
            weekNumber = 3,
            readTimeMinutes = 6,
            createdAt = now - 3 * 24 * 3600_000L,
            updatedAt = now - 3 * 24 * 3600_000L,
        ),
        Note(
            id = 4L,
            title = "Human Memory Systems",
            content = """
                Memory is not a single system but a collection of interrelated processes and stores.

                Atkinson-Shiffrin model (1968) distinguishes:
                  • Sensory Memory  – very brief (<1 s) capture of raw sensory input.
                  • Short-Term Memory (STM) – limited capacity (~7 ± 2 chunks, ~20 s duration).
                  • Long-Term Memory (LTM)  – practically unlimited storage.

                LTM sub-types:
                  • Declarative (Explicit): Episodic (personal events) vs Semantic (facts).
                  • Non-Declarative (Implicit): Procedural skills, priming, conditioning.

                Consolidation is the process by which memories are stabilised after encoding.
                The hippocampus is critical for new declarative memories; damage produces anterograde
                amnesia (inability to form new memories, as seen in patient H.M.).

                Spaced repetition exploits the spacing effect to strengthen long-term retention
                more efficiently than massed practice.
            """.trimIndent(),
            summary = "Overview of human memory systems: sensory, short-term, and long-term memory, with consolidation.",
            subjectId = "Cognitive Science",
            subjectName = "Cognitive Science",
            weekNumber = 1,
            readTimeMinutes = 5,
            createdAt = now - 2 * 24 * 3600_000L,
            updatedAt = now - 2 * 24 * 3600_000L,
        ),
        Note(
            id = 5L,
            title = "Spaced Repetition and SM-2",
            content = """
                The forgetting curve (Ebbinghaus, 1885) shows that memory retention decays
                exponentially over time unless the material is reviewed.

                Spaced repetition counters this by scheduling reviews just before the memory fades.
                Each successful review extends the interval to the next review — the easier a card
                is rated, the longer the interval before it resurfaces.

                The SM-2 algorithm (SuperMemo 2, Wozniak 1987) is widely used in software like Anki:
                  1. After each review, rate difficulty 0–5.
                  2. Compute the new interval:
                       – First review: interval = 1 day
                       – Second review: interval = 6 days
                       – Subsequent: interval = prev_interval × easiness_factor (EF)
                  3. Update EF: EF' = EF + (0.1 − (5 − rating) × (0.08 + (5 − rating) × 0.02))
                     EF is clamped to a minimum of 1.3.

                Items rated < 3 restart from interval 1 (relearning phase).
                Well-implemented SRS can reduce study time by 50-80 % vs blocked re-reading.
            """.trimIndent(),
            summary = "How the SM-2 spaced repetition algorithm fights the forgetting curve to maximise retention.",
            subjectId = "Cognitive Science",
            subjectName = "Cognitive Science",
            weekNumber = 2,
            readTimeMinutes = 5,
            createdAt = now - 1 * 24 * 3600_000L,
            updatedAt = now - 1 * 24 * 3600_000L,
        ),
    )

    private val subjectColors = mapOf(
        "Computer Science" to "#135BEC",
        "Cognitive Science" to "#A855F7",
    )

    // ─── Read ────────────────────────────────────────────────────────────────

    override suspend fun getNotes(subjectFilter: String?): List<Note> =
        if (subjectFilter != null) notes.filter { it.subjectId == subjectFilter }
        else notes.toList()

    override suspend fun getNoteById(id: Long): Note? =
        notes.find { it.id == id }

    override suspend fun getRelatedNotes(noteId: Long): List<RelatedNote> {
        val note = notes.find { it.id == noteId } ?: return emptyList()
        return notes
            .filter { it.id != noteId && it.subjectId == note.subjectId }
            .take(2)
            .map { related ->
                RelatedNote(
                    noteId = related.id,
                    title = related.title,
                    subjectName = related.subjectName,
                    subjectIconName = "book",
                    subjectColorHex = subjectColors[related.subjectId] ?: "#135BEC",
                    similarityScore = 0.85f,
                )
            }
    }

    override suspend fun getSubjects(): List<Subject> =
        notes.map { it.subjectId }.distinct().map { name ->
            Subject(
                id = name,
                name = name,
                colorHex = subjectColors[name] ?: "#135BEC",
                iconName = "book",
            )
        }

    // ─── Search ──────────────────────────────────────────────────────────────

    override suspend fun searchNotes(query: String, page: Int, size: Int): PaginatedNotes {
        val filtered = notes.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
        }
        return PaginatedNotes(notes = filtered, total = filtered.size.toLong(), page = 0, hasMore = false)
    }

    override suspend fun listNotesBySubject(subject: String, page: Int, size: Int): PaginatedNotes {
        val filtered = notes.filter { it.subjectId.equals(subject, ignoreCase = true) }
        return PaginatedNotes(notes = filtered, total = filtered.size.toLong(), page = 0, hasMore = false)
    }

    override suspend fun semanticSearch(query: String): List<Note> =
        notes.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
        }

    // ─── Write ───────────────────────────────────────────────────────────────

    override suspend fun createNote(title: String, content: String, subjectName: String): Note {
        val newId = (notes.maxOfOrNull { it.id } ?: 0L) + 1L
        val note = Note(
            id = newId,
            title = title,
            content = content,
            summary = if (content.length > 150) "${content.take(150)}..." else content,
            subjectId = subjectName,
            subjectName = subjectName,
            weekNumber = null,
            readTimeMinutes = (content.split(" ").size / 200).coerceAtLeast(1),
            createdAt = now,
            updatedAt = now,
        )
        notes.add(note)
        return note
    }

    override suspend fun updateNote(id: Long, title: String, content: String, subjectName: String) {
        val index = notes.indexOfFirst { it.id == id }
        if (index == -1) throw Exception("Note $id not found in stub data")
        val existing = notes[index]
        notes[index] = existing.copy(
            title = title,
            content = content,
            summary = if (content.length > 150) "${content.take(150)}..." else content,
            subjectId = subjectName,
            subjectName = subjectName,
            updatedAt = now,
        )
    }

    override suspend fun deleteNote(id: Long) {
        notes.removeAll { it.id == id }
    }
}
