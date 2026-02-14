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

        q.insert("card-${i++}", "How does expansionary monetary policy affect interest rates?",
            "MULTIPLE_CHOICE", "MEDIUM", ECON, "It lowers interest rates, stimulating borrowing",
            """[{"id":"a","text":"It raises interest rates to slow inflation","isCorrect":false},{"id":"b","text":"It lowers interest rates, stimulating borrowing","isCorrect":true},{"id":"c","text":"It has no effect on interest rates","isCorrect":false},{"id":"d","text":"It eliminates interest rates entirely","isCorrect":false}]""",
            "[\"$N_ECON_3\"]", "Central bank buys bonds (open market operations), increasing money supply and reducing rates.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How might a central bank's rate cut affect GDP growth?",
            "MULTIPLE_CHOICE", "HARD", ECON, "Lower rates boost investment and consumption, increasing aggregate demand and GDP",
            """[{"id":"a","text":"Lower rates boost investment and consumption, increasing aggregate demand and GDP","isCorrect":true},{"id":"b","text":"Lower rates reduce GDP by discouraging saving","isCorrect":false},{"id":"c","text":"Rate cuts only affect inflation, not GDP","isCorrect":false},{"id":"d","text":"GDP is unrelated to monetary policy","isCorrect":false}]""",
            "[\"$N_ECON_2\",\"$N_ECON_3\"]", "This connects monetary policy transmission to national income accounting.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "In which market structure do firms earn zero economic profit in the long run?",
            "MULTIPLE_CHOICE", "EASY", ECON, "Perfect competition",
            """[{"id":"a","text":"Perfect competition","isCorrect":true},{"id":"b","text":"Monopoly","isCorrect":false},{"id":"c","text":"Oligopoly","isCorrect":false},{"id":"d","text":"Monopsony","isCorrect":false}]""",
            "[\"$N_ECON_4\"]", "Free entry and exit drives economic profit to zero as new firms enter profitable markets.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How does comparative advantage differ from absolute advantage?",
            "MULTIPLE_CHOICE", "MEDIUM", ECON, "Comparative advantage is about lower opportunity cost, not total output",
            """[{"id":"a","text":"They mean the same thing","isCorrect":false},{"id":"b","text":"Comparative advantage is about lower opportunity cost, not total output","isCorrect":true},{"id":"c","text":"Absolute advantage considers opportunity cost","isCorrect":false},{"id":"d","text":"Comparative advantage only applies to large countries","isCorrect":false}]""",
            "[\"$N_ECON_5\"]", "Ricardo's key insight: what matters is relative (opportunity) cost, not absolute productivity.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How do tariffs affect consumer surplus and total welfare?",
            "MULTIPLE_CHOICE", "HARD", ECON, "Tariffs reduce consumer surplus and create deadweight loss",
            """[{"id":"a","text":"Tariffs increase total welfare for everyone","isCorrect":false},{"id":"b","text":"Tariffs reduce consumer surplus and create deadweight loss","isCorrect":true},{"id":"c","text":"Tariffs have no effect on consumer surplus","isCorrect":false},{"id":"d","text":"Tariffs only benefit foreign producers","isCorrect":false}]""",
            "[\"$N_ECON_5\",\"$N_ECON_1\"]", "The net welfare effect is negative because deadweight loss exceeds the redistribution gains.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What does the GDP expenditure formula C + I + G + NX represent?",
            "MULTIPLE_CHOICE", "EASY", ECON, "Consumption + Investment + Government spending + Net exports",
            """[{"id":"a","text":"Consumption + Investment + Government spending + Net exports","isCorrect":true},{"id":"b","text":"Cost + Income + Growth + Net exchange","isCorrect":false},{"id":"c","text":"Capital + Interest + Goods + National expenditure","isCorrect":false},{"id":"d","text":"Credit + Insurance + Grants + New exports","isCorrect":false}]""",
            "[\"$N_ECON_2\"]", "Each component measures a different source of spending in the economy.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What is the main tool central banks use in open market operations?",
            "MULTIPLE_CHOICE", "MEDIUM", ECON, "Buying and selling government bonds",
            """[{"id":"a","text":"Printing more currency","isCorrect":false},{"id":"b","text":"Buying and selling government bonds","isCorrect":true},{"id":"c","text":"Setting minimum wages","isCorrect":false},{"id":"d","text":"Controlling stock market prices","isCorrect":false}]""",
            "[\"$N_ECON_3\"]", "Buying bonds injects money → lower rates. Selling bonds withdraws money → higher rates.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "A monopoly can persist in the long run because of:",
            "MULTIPLE_CHOICE", "MEDIUM", ECON, "High barriers to entry",
            """[{"id":"a","text":"High barriers to entry","isCorrect":true},{"id":"b","text":"Perfect information among consumers","isCorrect":false},{"id":"c","text":"Free entry and exit of firms","isCorrect":false},{"id":"d","text":"Identical products across all firms","isCorrect":false}]""",
            "[\"$N_ECON_4\"]", "Patents, economies of scale, and government licenses create barriers that sustain monopoly power.",
            2.5, 0, 0, null, now)

        // CS flashcards (12)
        q.insert("card-${i++}", "What is the worst-case time complexity for searching in an unbalanced BST?",
            "MULTIPLE_CHOICE", "MEDIUM", CS, "O(n)",
            """[{"id":"a","text":"O(1)","isCorrect":false},{"id":"b","text":"O(log n)","isCorrect":false},{"id":"c","text":"O(n)","isCorrect":true},{"id":"d","text":"O(n log n)","isCorrect":false}]""",
            "[\"$N_CS_1\"]", "An unbalanced BST can degenerate into a linked list with height n.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What is the time complexity of binary search?",
            "MULTIPLE_CHOICE", "EASY", CS, "O(log n)",
            """[{"id":"a","text":"O(1)","isCorrect":false},{"id":"b","text":"O(log n)","isCorrect":true},{"id":"c","text":"O(n)","isCorrect":false},{"id":"d","text":"O(n^2)","isCorrect":false}]""",
            "[\"$N_CS_2\"]", "Each comparison halves the search space, giving log2(n) steps.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What data structure does BFS use and what does DFS use?",
            "MULTIPLE_CHOICE", "EASY", CS, "BFS uses a queue; DFS uses a stack",
            """[{"id":"a","text":"BFS uses a queue; DFS uses a stack","isCorrect":true},{"id":"b","text":"BFS uses a stack; DFS uses a queue","isCorrect":false},{"id":"c","text":"Both use a priority queue","isCorrect":false},{"id":"d","text":"Both use a stack","isCorrect":false}]""",
            "[\"$N_CS_3\"]", "Queue = FIFO for level-order; Stack = LIFO for depth-first exploration.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Why is the lower bound for comparison-based sorting O(n log n)?",
            "MULTIPLE_CHOICE", "HARD", CS, "A decision tree must have n! leaves, requiring height >= log2(n!)",
            """[{"id":"a","text":"A decision tree must have n! leaves, requiring height >= log2(n!)","isCorrect":true},{"id":"b","text":"All algorithms need at least n log n memory","isCorrect":false},{"id":"c","text":"It is impossible to compare elements faster","isCorrect":false},{"id":"d","text":"The CPU can only process log n comparisons per cycle","isCorrect":false}]""",
            "[\"$N_CS_4\",\"$N_CS_2\"]", "This information-theoretic argument applies to all comparison sorts.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What two properties must a problem have to be solvable with dynamic programming?",
            "MULTIPLE_CHOICE", "MEDIUM", CS, "Overlapping subproblems and optimal substructure",
            """[{"id":"a","text":"Overlapping subproblems and optimal substructure","isCorrect":true},{"id":"b","text":"Constant time lookup and linear space","isCorrect":false},{"id":"c","text":"Sorted input and binary division","isCorrect":false},{"id":"d","text":"Greedy choice property and matroid structure","isCorrect":false}]""",
            "[\"$N_CS_5\"]", "Overlapping subproblems = same subproblem solved multiple times. Optimal substructure = optimal solution contains optimal sub-solutions.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Compare the space complexity of merge sort vs. quick sort.",
            "MULTIPLE_CHOICE", "MEDIUM", CS, "Merge sort: O(n) extra space; Quick sort: O(log n) average",
            """[{"id":"a","text":"Merge sort: O(n) extra space; Quick sort: O(log n) average","isCorrect":true},{"id":"b","text":"Both use O(1) extra space","isCorrect":false},{"id":"c","text":"Merge sort: O(1); Quick sort: O(n)","isCorrect":false},{"id":"d","text":"Both use O(n) extra space","isCorrect":false}]""",
            "[\"$N_CS_4\"]", "This space trade-off is a key reason quick sort is often preferred in practice.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "Which algorithm finds shortest paths in a weighted graph with non-negative edges?",
            "MULTIPLE_CHOICE", "EASY", CS, "Dijkstra's algorithm",
            """[{"id":"a","text":"DFS","isCorrect":false},{"id":"b","text":"BFS","isCorrect":false},{"id":"c","text":"Dijkstra's algorithm","isCorrect":true},{"id":"d","text":"Bubble sort","isCorrect":false}]""",
            "[\"$N_CS_3\"]", "Dijkstra uses a priority queue (min-heap) for greedy edge relaxation.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How can graph algorithms be applied to analyze evolutionary trees in biology?",
            "MULTIPLE_CHOICE", "HARD", CS, "Phylogenetic trees are graphs; BFS/DFS traverse them and MST infers evolutionary distances",
            """[{"id":"a","text":"Phylogenetic trees are graphs; BFS/DFS traverse them and MST infers evolutionary distances","isCorrect":true},{"id":"b","text":"Graph algorithms cannot be applied to biology","isCorrect":false},{"id":"c","text":"Only sorting algorithms apply to biological data","isCorrect":false},{"id":"d","text":"Evolution follows a stack-based LIFO pattern","isCorrect":false}]""",
            "[\"$N_CS_3\",\"$N_BIO_5\"]", "This is a cross-disciplinary application connecting CS graph theory to evolutionary biology.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "What is the difference between memoization and tabulation in DP?",
            "MULTIPLE_CHOICE", "MEDIUM", CS, "Memoization is top-down (recursive + cache); tabulation is bottom-up (iterative table)",
            """[{"id":"a","text":"They are the same technique with different names","isCorrect":false},{"id":"b","text":"Memoization is top-down (recursive + cache); tabulation is bottom-up (iterative table)","isCorrect":true},{"id":"c","text":"Memoization uses more memory than tabulation always","isCorrect":false},{"id":"d","text":"Tabulation requires recursion while memoization does not","isCorrect":false}]""",
            "[\"$N_CS_5\"]", "Both avoid recomputation but differ in traversal order and stack usage.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How does BST structure relate to sorting?",
            "MULTIPLE_CHOICE", "MEDIUM", CS, "In-order traversal of a BST yields elements in sorted order",
            """[{"id":"a","text":"BSTs cannot be used for sorting","isCorrect":false},{"id":"b","text":"In-order traversal of a BST yields elements in sorted order","isCorrect":true},{"id":"c","text":"Pre-order traversal gives sorted output","isCorrect":false},{"id":"d","text":"BSTs sort data during insertion in O(1) time","isCorrect":false}]""",
            "[\"$N_CS_1\",\"$N_CS_4\"]", "This connects tree data structures to sorting algorithms through in-order traversal.",
            2.5, 0, 0, null, now)

        q.insert("card-${i++}", "How does dynamic programming relate to optimization in economics?",
            "MULTIPLE_CHOICE", "HARD", CS, "Both break complex decisions into subproblems; the knapsack problem mirrors budget allocation",
            """[{"id":"a","text":"Both break complex decisions into subproblems; the knapsack problem mirrors budget allocation","isCorrect":true},{"id":"b","text":"DP has no connection to economics","isCorrect":false},{"id":"c","text":"Economics only uses greedy algorithms","isCorrect":false},{"id":"d","text":"DP was invented for stock trading specifically","isCorrect":false}]""",
            "[\"$N_CS_5\",\"$N_ECON_5\"]", "Richard Bellman developed DP partly from economic optimization theory.",
            2.5, 0, 0, null, now)

        q.insert("card-${i}", "What is the time complexity of inserting into an AVL tree?",
            "MULTIPLE_CHOICE", "MEDIUM", CS, "O(log n)",
            """[{"id":"a","text":"O(1)","isCorrect":false},{"id":"b","text":"O(log n)","isCorrect":true},{"id":"c","text":"O(n)","isCorrect":false},{"id":"d","text":"O(n log n)","isCorrect":false}]""",
            "[\"$N_CS_1\",\"$N_CS_2\"]", "AVL trees maintain balance with rotations, guaranteeing O(log n) height.",
            2.5, 0, 0, null, now)
    }

    private fun insertStudySessions(db: MindTagDatabase) {
        val q = db.studySessionEntityQueries

        // CS Quick Quiz — 5 questions, completed yesterday
        q.insert(
            SESSION_CS_QUICK, CS, "QUICK_QUIZ",
            yesterday, yesterday + 180_000L, // 3 min duration
            5, null, "COMPLETED"
        )

        // Biology Exam Mode — 7 questions, completed today, 10-min time limit
        q.insert(
            SESSION_BIO_EXAM, BIO, "EXAM_MODE",
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
