package io.diasjakupov.mindtag.data.seed

import io.diasjakupov.mindtag.data.local.MindTagDatabase

object SeedData {

    // Subject IDs
    private const val BIO = "subj-bio-101"
    private const val ECON = "subj-econ-101"
    private const val CS = "subj-cs-101"

    // Note IDs — Biology
    private const val N_BIO_1 = "note-bio-cell-division"
    private const val N_BIO_2 = "note-bio-dna-replication"
    private const val N_BIO_3 = "note-bio-photosynthesis"
    private const val N_BIO_4 = "note-bio-krebs-cycle"
    private const val N_BIO_5 = "note-bio-evolution"

    // Note IDs — Economics
    private const val N_ECON_1 = "note-econ-supply-demand"
    private const val N_ECON_2 = "note-econ-gdp"
    private const val N_ECON_3 = "note-econ-monetary-policy"
    private const val N_ECON_4 = "note-econ-market-structures"
    private const val N_ECON_5 = "note-econ-trade"

    // Note IDs — CS
    private const val N_CS_1 = "note-cs-binary-search-trees"
    private const val N_CS_2 = "note-cs-big-o"
    private const val N_CS_3 = "note-cs-graph-algorithms"
    private const val N_CS_4 = "note-cs-sorting"
    private const val N_CS_5 = "note-cs-dynamic-programming"

    private val now = 1738886400000L // 2025-02-07 00:00 UTC
    private val yesterday = now - 86_400_000L
    private val twoDaysAgo = now - 172_800_000L

    // Session IDs
    private const val SESSION_CS_QUICK = "session-cs-quick-1"
    private const val SESSION_BIO_EXAM = "session-bio-exam-1"

    fun populateSubjectsOnly(db: MindTagDatabase) {
        insertSubjects(db)
    }

    fun populate(db: MindTagDatabase) {
        insertSubjects(db)
        // Notes and semantic links now come from the server API
        insertFlashCards(db)
        insertStudySessions(db)
        insertQuizAnswers(db)
    }

    private fun insertSubjects(db: MindTagDatabase) {
        db.subjectEntityQueries.insert(BIO, "Biology 101", "#22C55E", "leaf", 0.65, 5, 3, twoDaysAgo, now)
        db.subjectEntityQueries.insert(ECON, "Economics 101", "#F59E0B", "trending_up", 0.42, 5, 2, twoDaysAgo, now)
        db.subjectEntityQueries.insert(CS, "Computer Science", "#135BEC", "code", 0.78, 5, 4, twoDaysAgo, now)
    }

    private fun insertFlashCards(db: MindTagDatabase) {
        val q = db.flashCardEntityQueries
        var i = 1

        // Biology flashcards (10)
        q.insert("card-${i++}", "What are the four stages of mitosis in order?",
            "MULTIPLE_CHOICE", "MEDIUM", BIO, "Prophase, Metaphase, Anaphase, Telophase",
            """[{"id":"a","text":"Prophase, Metaphase, Anaphase, Telophase","isCorrect":true},{"id":"b","text":"Interphase, Metaphase, Anaphase, Cytokinesis","isCorrect":false},{"id":"c","text":"Prophase, Anaphase, Metaphase, Telophase","isCorrect":false},{"id":"d","text":"G1, S, G2, M phase","isCorrect":false}]""",
            "[\"$N_BIO_1\"]", "Remember PMAT: Prophase (chromosomes condense), Metaphase (align at middle), Anaphase (pull apart), Telophase (two nuclei form).",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "DNA replication is described as semiconservative. What does this mean?",
            "MULTIPLE_CHOICE", "MEDIUM", BIO, "Each new DNA molecule contains one original strand and one newly synthesized strand",
            """[{"id":"a","text":"Both strands are newly synthesized","isCorrect":false},{"id":"b","text":"Each new molecule has one original and one new strand","isCorrect":true},{"id":"c","text":"The entire original molecule is conserved intact","isCorrect":false},{"id":"d","text":"Only the leading strand is conserved","isCorrect":false}]""",
            "[\"$N_BIO_2\"]", "Meselson-Stahl experiment (1958) confirmed this model using nitrogen isotopes.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Which molecule is the primary product of the Calvin cycle?",
            "MULTIPLE_CHOICE", "EASY", BIO, "G3P (glyceraldehyde-3-phosphate)",
            """[{"id":"a","text":"G3P (glyceraldehyde-3-phosphate)","isCorrect":true},{"id":"b","text":"Glucose","isCorrect":false},{"id":"c","text":"ATP","isCorrect":false},{"id":"d","text":"NADPH","isCorrect":false}]""",
            "[\"$N_BIO_3\"]", "The Calvin cycle produces G3P, which is then used to synthesize glucose and other sugars.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How does photosynthesis relate to cellular respiration in terms of energy flow?",
            "MULTIPLE_CHOICE", "HARD", BIO, "They are complementary: photosynthesis stores light energy as glucose, respiration releases it as ATP",
            """[{"id":"a","text":"They are complementary: photosynthesis stores light energy as glucose, respiration releases it as ATP","isCorrect":true},{"id":"b","text":"They are identical processes that both produce glucose","isCorrect":false},{"id":"c","text":"Photosynthesis occurs only in animals, respiration only in plants","isCorrect":false},{"id":"d","text":"Both processes consume oxygen and release carbon dioxide","isCorrect":false}]""",
            "[\"$N_BIO_3\",\"$N_BIO_4\"]", "6CO2 + 6H2O -> C6H12O6 + 6O2 (photosynthesis) is the reverse of respiration.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What is the maximum ATP yield from one molecule of glucose in aerobic respiration?",
            "MULTIPLE_CHOICE", "MEDIUM", BIO, "36-38 ATP",
            """[{"id":"a","text":"2 ATP","isCorrect":false},{"id":"b","text":"12 ATP","isCorrect":false},{"id":"c","text":"36-38 ATP","isCorrect":true},{"id":"d","text":"100 ATP","isCorrect":false}]""",
            "[\"$N_BIO_4\"]", "Glycolysis yields 2, Krebs cycle 2, and oxidative phosphorylation ~32-34 ATP.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Which of the following is NOT a mechanism of evolution?",
            "MULTIPLE_CHOICE", "HARD", BIO, "Spontaneous generation",
            """[{"id":"a","text":"Genetic drift","isCorrect":false},{"id":"b","text":"Gene flow","isCorrect":false},{"id":"c","text":"Spontaneous generation","isCorrect":true},{"id":"d","text":"Non-random mating","isCorrect":false}]""",
            "[\"$N_BIO_5\"]", "The four mechanisms are: natural selection, genetic drift, gene flow, and mutation. Non-random mating also affects allele frequencies.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Why is DNA replication essential before cell division?",
            "MULTIPLE_CHOICE", "MEDIUM", BIO, "Each daughter cell needs a complete genome copy",
            """[{"id":"a","text":"Each daughter cell needs a complete genome copy","isCorrect":true},{"id":"b","text":"It helps the cell produce more protein","isCorrect":false},{"id":"c","text":"It provides energy for the division process","isCorrect":false},{"id":"d","text":"It is not essential — cells can divide without it","isCorrect":false}]""",
            "[\"$N_BIO_1\",\"$N_BIO_2\"]", "This links S phase (DNA replication) to M phase (mitosis) in the cell cycle.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Where do the light-dependent reactions of photosynthesis occur?",
            "MULTIPLE_CHOICE", "EASY", BIO, "Thylakoid membranes",
            """[{"id":"a","text":"Stroma","isCorrect":false},{"id":"b","text":"Thylakoid membranes","isCorrect":true},{"id":"c","text":"Cell membrane","isCorrect":false},{"id":"d","text":"Mitochondrial matrix","isCorrect":false}]""",
            "[\"$N_BIO_3\"]", "Light reactions in thylakoids produce ATP and NADPH; the Calvin cycle in the stroma uses them to fix CO2.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What enzyme unwinds the DNA double helix during replication?",
            "MULTIPLE_CHOICE", "EASY", BIO, "Helicase",
            """[{"id":"a","text":"DNA polymerase","isCorrect":false},{"id":"b","text":"Ligase","isCorrect":false},{"id":"c","text":"Helicase","isCorrect":true},{"id":"d","text":"Primase","isCorrect":false}]""",
            "[\"$N_BIO_2\"]", "Helicase breaks hydrogen bonds between base pairs. Primase adds RNA primers, polymerase extends.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What does the Hardy-Weinberg equilibrium model predict?",
            "MULTIPLE_CHOICE", "HARD", BIO, "Allele frequencies remain constant when no evolutionary forces act",
            """[{"id":"a","text":"All species will eventually go extinct","isCorrect":false},{"id":"b","text":"Allele frequencies remain constant when no evolutionary forces act","isCorrect":true},{"id":"c","text":"Dominant alleles always increase in frequency","isCorrect":false},{"id":"d","text":"Populations always evolve toward greater fitness","isCorrect":false}]""",
            "[\"$N_BIO_5\"]", "HW requires: no mutation, no selection, large population, random mating, no gene flow.",
            2.5, 0, 0, null, now)

        // Economics flashcards (10)
        q.insert("card-${i++}", "What happens to equilibrium price when demand increases and supply remains constant?",
            "MULTIPLE_CHOICE", "EASY", ECON, "Equilibrium price increases",
            """[{"id":"a","text":"Equilibrium price increases","isCorrect":true},{"id":"b","text":"Equilibrium price decreases","isCorrect":false},{"id":"c","text":"Equilibrium price stays the same","isCorrect":false},{"id":"d","text":"Supply automatically increases to match","isCorrect":false}]""",
            "[\"$N_ECON_1\"]", "Demand curve shifts right, moving along the fixed supply curve to a higher price point.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What are the three approaches to calculating GDP?",
            "MULTIPLE_CHOICE", "MEDIUM", ECON, "Expenditure, income, and production approaches",
            """[{"id":"a","text":"Expenditure, income, and production","isCorrect":true},{"id":"b","text":"Supply, demand, and equilibrium","isCorrect":false},{"id":"c","text":"Fiscal, monetary, and trade","isCorrect":false},{"id":"d","text":"Micro, macro, and international","isCorrect":false}]""",
            "[\"$N_ECON_2\"]", "Expenditure: C+I+G+NX. All three approaches should yield the same GDP figure.",
            2.5, 0, 0, null, now)

        // TRUE_FALSE cards (13-22) — Economics + CS mix
        q.insert("card-${i++}", "Expansionary monetary policy lowers interest rates.",
            "TRUE_FALSE", "MEDIUM", ECON, "True",
            """[{"id":"a","text":"True","isCorrect":true},{"id":"b","text":"False","isCorrect":false}]""",
            "[\"$N_ECON_3\"]", "Central bank buys bonds, increasing money supply and reducing rates.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "A central bank rate cut decreases GDP growth.",
            "TRUE_FALSE", "HARD", ECON, "False",
            """[{"id":"a","text":"True","isCorrect":false},{"id":"b","text":"False","isCorrect":true}]""",
            "[\"$N_ECON_2\",\"$N_ECON_3\"]", "Lower rates boost investment and consumption, increasing aggregate demand and GDP.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "In perfect competition, firms earn zero economic profit in the long run.",
            "TRUE_FALSE", "EASY", ECON, "True",
            """[{"id":"a","text":"True","isCorrect":true},{"id":"b","text":"False","isCorrect":false}]""",
            "[\"$N_ECON_4\"]", "Free entry and exit drives economic profit to zero as new firms enter profitable markets.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Comparative advantage is based on lower opportunity cost, not total output.",
            "TRUE_FALSE", "MEDIUM", ECON, "True",
            """[{"id":"a","text":"True","isCorrect":true},{"id":"b","text":"False","isCorrect":false}]""",
            "[\"$N_ECON_5\"]", "Ricardo's key insight: what matters is relative (opportunity) cost, not absolute productivity.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Tariffs increase total welfare for all participants in trade.",
            "TRUE_FALSE", "HARD", ECON, "False",
            """[{"id":"a","text":"True","isCorrect":false},{"id":"b","text":"False","isCorrect":true}]""",
            "[\"$N_ECON_5\",\"$N_ECON_1\"]", "Tariffs reduce consumer surplus and create deadweight loss.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "The GDP expenditure formula is C + I + G + NX.",
            "TRUE_FALSE", "EASY", ECON, "True",
            """[{"id":"a","text":"True","isCorrect":true},{"id":"b","text":"False","isCorrect":false}]""",
            "[\"$N_ECON_2\"]", "Consumption + Investment + Government spending + Net exports.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Central banks use buying and selling government bonds in open market operations.",
            "TRUE_FALSE", "MEDIUM", ECON, "True",
            """[{"id":"a","text":"True","isCorrect":true},{"id":"b","text":"False","isCorrect":false}]""",
            "[\"$N_ECON_3\"]", "Buying bonds injects money (lower rates). Selling bonds withdraws money (higher rates).",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "A monopoly can persist in the long run due to low barriers to entry.",
            "TRUE_FALSE", "MEDIUM", ECON, "False",
            """[{"id":"a","text":"True","isCorrect":false},{"id":"b","text":"False","isCorrect":true}]""",
            "[\"$N_ECON_4\"]", "Monopolies persist due to HIGH barriers to entry (patents, economies of scale, licenses).",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "The worst-case time complexity for searching an unbalanced BST is O(log n).",
            "TRUE_FALSE", "MEDIUM", CS, "False",
            """[{"id":"a","text":"True","isCorrect":false},{"id":"b","text":"False","isCorrect":true}]""",
            "[\"$N_CS_1\"]", "An unbalanced BST can degenerate into a linked list, making worst-case O(n).",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "The time complexity of binary search is O(log n).",
            "TRUE_FALSE", "EASY", CS, "True",
            """[{"id":"a","text":"True","isCorrect":true},{"id":"b","text":"False","isCorrect":false}]""",
            "[\"$N_CS_2\"]", "Each comparison halves the search space, giving log2(n) steps.",
            2.5, 0, 0, null, now)

        // FLASHCARD cards (23-32) — CS
        q.insert("card-${i++}", "What data structures do BFS and DFS use respectively?",
            "FLASHCARD", "EASY", CS, "BFS uses a queue (FIFO for level-order); DFS uses a stack (LIFO for depth-first).",
            "[]",
            "[\"$N_CS_3\"]", "Queue = FIFO for level-order; Stack = LIFO for depth-first exploration.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Why is the lower bound for comparison-based sorting O(n log n)?",
            "FLASHCARD", "HARD", CS, "A decision tree must have n! leaves, requiring height >= log2(n!). This information-theoretic argument applies to all comparison sorts.",
            "[]",
            "[\"$N_CS_4\",\"$N_CS_2\"]", "This information-theoretic argument applies to all comparison sorts.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What two properties must a problem have to be solvable with dynamic programming?",
            "FLASHCARD", "MEDIUM", CS, "Overlapping subproblems (same subproblem solved multiple times) and optimal substructure (optimal solution contains optimal sub-solutions).",
            "[]",
            "[\"$N_CS_5\"]", "Both properties must be present for DP to be applicable.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Compare the space complexity of merge sort vs. quick sort.",
            "FLASHCARD", "MEDIUM", CS, "Merge sort: O(n) extra space. Quick sort: O(log n) average (stack frames). This space trade-off is why quick sort is often preferred.",
            "[]",
            "[\"$N_CS_4\"]", "This space trade-off is a key reason quick sort is often preferred in practice.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Which algorithm finds shortest paths in a weighted graph with non-negative edges?",
            "FLASHCARD", "EASY", CS, "Dijkstra's algorithm. It uses a priority queue (min-heap) for greedy edge relaxation.",
            "[]",
            "[\"$N_CS_3\"]", "Dijkstra uses a priority queue (min-heap) for greedy edge relaxation.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How can graph algorithms be applied to evolutionary biology?",
            "FLASHCARD", "HARD", CS, "Phylogenetic trees are graphs. BFS/DFS can traverse them and MST algorithms can infer evolutionary distances between species.",
            "[]",
            "[\"$N_CS_3\",\"$N_BIO_5\"]", "Cross-disciplinary application connecting CS graph theory to evolutionary biology.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What is the difference between memoization and tabulation in DP?",
            "FLASHCARD", "MEDIUM", CS, "Memoization is top-down (recursive + cache). Tabulation is bottom-up (iterative table). Both avoid recomputation but differ in traversal order.",
            "[]",
            "[\"$N_CS_5\"]", "Both avoid recomputation but differ in traversal order and stack usage.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How does BST structure relate to sorting?",
            "FLASHCARD", "MEDIUM", CS, "In-order traversal of a BST yields elements in sorted order. This connects tree data structures to sorting algorithms.",
            "[]",
            "[\"$N_CS_1\",\"$N_CS_4\"]", "This connects tree data structures to sorting algorithms through in-order traversal.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How does dynamic programming relate to optimization in economics?",
            "FLASHCARD", "HARD", CS, "Both break complex decisions into subproblems. The knapsack problem mirrors budget allocation. Richard Bellman developed DP partly from economic optimization theory.",
            "[]",
            "[\"$N_CS_5\",\"$N_ECON_5\"]", "Richard Bellman developed DP partly from economic optimization theory.",
            2.5, 0, 0, null, now)

        q.insert("card-${i}", "What is the time complexity of inserting into an AVL tree and why?",
            "FLASHCARD", "MEDIUM", CS, "O(log n). AVL trees maintain balance with rotations after each insertion, guaranteeing O(log n) height.",
            "[]",
            "[\"$N_CS_1\",\"$N_CS_2\"]", "AVL trees maintain balance with rotations, guaranteeing O(log n) height.",
            2.5, 0, 0, null, now)
    }

    private fun insertStudySessions(db: MindTagDatabase) {
        val q = db.studySessionEntityQueries

        // CS Quiz — 5 questions, completed yesterday
        q.insert(
            SESSION_CS_QUICK, CS, "QUIZ",
            yesterday, yesterday + 180_000L, // 3 min duration
            5, null, "COMPLETED"
        )

        // Biology Quiz — 7 questions, completed today, 10-min time limit
        q.insert(
            SESSION_BIO_EXAM, BIO, "QUIZ",
            now - 900_000L, now - 300_000L, // 10 min ago, finished 5 min ago
            7, 600, "COMPLETED"
        )
    }

    private fun insertQuizAnswers(db: MindTagDatabase) {
        val q = db.quizAnswerEntityQueries
        var i = 1

        // CS Quick Quiz answers (5 questions, 4 correct) — CS cards start at card-21
        q.insert("answer-${i++}", SESSION_CS_QUICK, "card-21", "O(n)", 1, "EASY", 12, yesterday + 15_000L)
        q.insert("answer-${i++}", SESSION_CS_QUICK, "card-22", "O(log n)", 1, "EASY", 8, yesterday + 30_000L)
        q.insert("answer-${i++}", SESSION_CS_QUICK, "card-23", "BFS uses a queue; DFS uses a stack", 1, null, 18, yesterday + 60_000L)
        q.insert("answer-${i++}", SESSION_CS_QUICK, "card-24", "The CPU can only process log n comparisons per cycle", 0, "HARD", 25, yesterday + 100_000L)
        q.insert("answer-${i++}", SESSION_CS_QUICK, "card-25", "Overlapping subproblems and optimal substructure", 1, null, 15, yesterday + 130_000L)

        // Biology Exam Mode answers (7 questions, 5 correct)
        q.insert("answer-${i++}", SESSION_BIO_EXAM, "card-1", "Prophase, Metaphase, Anaphase, Telophase", 1, "EASY", 20, now - 850_000L)
        q.insert("answer-${i++}", SESSION_BIO_EXAM, "card-2", "Each new molecule has one original and one new strand", 1, null, 25, now - 800_000L)
        q.insert("answer-${i++}", SESSION_BIO_EXAM, "card-3", "G3P (glyceraldehyde-3-phosphate)", 1, "EASY", 15, now - 750_000L)
        q.insert("answer-${i++}", SESSION_BIO_EXAM, "card-4", "They are identical processes that both produce glucose", 0, "HARD", 40, now - 700_000L)
        q.insert("answer-${i++}", SESSION_BIO_EXAM, "card-5", "36-38 ATP", 1, null, 18, now - 650_000L)
        q.insert("answer-${i++}", SESSION_BIO_EXAM, "card-6", "Dominant alleles always increase in frequency", 0, "HARD", 35, now - 600_000L)
        q.insert("answer-${i}", SESSION_BIO_EXAM, "card-7", "Each daughter cell needs a complete genome copy", 1, null, 30, now - 550_000L)
    }

}
