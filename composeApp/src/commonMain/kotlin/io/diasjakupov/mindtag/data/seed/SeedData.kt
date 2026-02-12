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
        insertUserProgress(db)
        insertStudySessions(db)
        insertQuizAnswers(db)
        insertPlannerTasks(db)
    }

    private fun insertSubjects(db: MindTagDatabase) {
        db.subjectEntityQueries.insert(BIO, "Biology 101", "#22C55E", "leaf", 0.65, 5, 3, twoDaysAgo, now)
        db.subjectEntityQueries.insert(ECON, "Economics 101", "#F59E0B", "trending_up", 0.42, 5, 2, twoDaysAgo, now)
        db.subjectEntityQueries.insert(CS, "Computer Science", "#135BEC", "code", 0.78, 5, 4, twoDaysAgo, now)
    }

    private fun insertNotes(db: MindTagDatabase) {
        val q = db.noteEntityQueries

        // Biology notes
        q.insert(N_BIO_1, "Cell Division & Mitosis", "Mitosis is the process by which a single cell divides to produce two identical daughter cells. The stages include prophase, metaphase, anaphase, and telophase. During prophase, chromatin condenses into visible chromosomes. The mitotic spindle forms and attaches to kinetochores during metaphase, aligning chromosomes at the cell equator.", "Overview of mitotic cell division stages and chromosome behavior.", BIO, 2, 4, twoDaysAgo, yesterday)
        q.insert(N_BIO_2, "DNA Replication & Repair", "DNA replication is semiconservative: each strand serves as a template. Helicase unwinds the double helix, primase synthesizes RNA primers, and DNA polymerase III extends the new strand. Leading strand synthesis is continuous while the lagging strand is synthesized in Okazaki fragments joined by DNA ligase.", "Mechanism of semiconservative DNA replication and key enzymes involved.", BIO, 3, 5, twoDaysAgo, yesterday)
        q.insert(N_BIO_3, "Photosynthesis: Light & Dark Reactions", "Photosynthesis occurs in chloroplasts through light-dependent reactions in thylakoid membranes (producing ATP and NADPH) and the Calvin cycle in the stroma (fixing CO2 into G3P). Photosystem II splits water, releasing O2, while Photosystem I reduces NADP+ to NADPH.", "Two-stage process of photosynthesis: light reactions and Calvin cycle.", BIO, 4, 6, twoDaysAgo, now)
        q.insert(N_BIO_4, "Cellular Respiration & Krebs Cycle", "Cellular respiration converts glucose to ATP through glycolysis (cytoplasm), the Krebs cycle (mitochondrial matrix), and oxidative phosphorylation (inner membrane). The Krebs cycle generates NADH and FADH2, which feed electrons into the electron transport chain for maximum ATP yield of ~36-38 per glucose.", "Breakdown of glucose to ATP via glycolysis, Krebs cycle, and ETC.", BIO, 5, 5, yesterday, now)
        q.insert(N_BIO_5, "Natural Selection & Evolution", "Darwin's theory of evolution by natural selection rests on variation, heritability, differential survival, and reproduction. Populations evolve over generations as allele frequencies shift due to selection pressures, genetic drift, gene flow, and mutation. Hardy-Weinberg equilibrium provides a null model for detecting evolution.", "Mechanisms of evolution including natural selection and genetic drift.", BIO, 6, 4, yesterday, now)

        // Economics notes
        q.insert(N_ECON_1, "Supply and Demand Curves", "The law of demand states quantity demanded falls as price rises (ceteris paribus), creating a downward-sloping demand curve. Supply curves slope upward. Market equilibrium occurs where supply equals demand. Shifts in either curve (due to income, preferences, input costs) move the equilibrium price and quantity.", "Fundamental market model of supply, demand, and equilibrium.", ECON, 1, 3, twoDaysAgo, yesterday)
        q.insert(N_ECON_2, "GDP & National Income Accounting", "Gross Domestic Product measures the total market value of final goods and services produced within a country in a given period. GDP can be calculated via expenditure (C + I + G + NX), income, or production approaches. Real GDP adjusts for inflation using a base-year price level.", "Methods and meaning of GDP measurement.", ECON, 2, 4, twoDaysAgo, yesterday)
        q.insert(N_ECON_3, "Monetary Policy & Central Banking", "Central banks use monetary policy tools (open market operations, reserve requirements, discount rate) to influence money supply and interest rates. Expansionary policy lowers rates to stimulate borrowing; contractionary policy raises rates to curb inflation. The Taylor Rule provides a guideline for setting the federal funds rate.", "How central banks manage money supply and interest rates.", ECON, 3, 5, yesterday, now)
        q.insert(N_ECON_4, "Market Structures: Perfect to Monopoly", "Market structures range from perfect competition (many firms, identical products, free entry) to monopoly (single firm, unique product, high barriers). Oligopoly and monopolistic competition lie between. Each structure has different implications for pricing power, efficiency, and consumer surplus.", "Comparison of competitive, oligopoly, monopolistic, and monopoly markets.", ECON, 4, 4, yesterday, now)
        q.insert(N_ECON_5, "International Trade & Comparative Advantage", "Comparative advantage explains why countries trade: each specializes in producing goods with the lowest opportunity cost. Ricardo's model shows both nations gain from trade even if one is more productive in everything (absolute advantage). Trade barriers like tariffs and quotas reduce these gains.", "Theory of comparative advantage and gains from trade.", ECON, 5, 3, yesterday, now)

        // CS notes
        q.insert(N_CS_1, "Binary Search Trees", "A binary search tree (BST) is a rooted binary tree where for every node, all values in the left subtree are smaller and all in the right are larger. Search, insert, and delete run in O(h) time where h is the height. Balanced BSTs (AVL, Red-Black) guarantee O(log n) height.", "BST properties, operations, and balanced variants.", CS, 2, 4, twoDaysAgo, yesterday)
        q.insert(N_CS_2, "Big-O Notation & Complexity Analysis", "Big-O notation describes the upper bound of an algorithm's growth rate. Common classes: O(1) constant, O(log n) logarithmic, O(n) linear, O(n log n) linearithmic, O(n^2) quadratic, O(2^n) exponential. Analyzing nested loops, recursive calls, and divide-and-conquer helps determine time complexity.", "Asymptotic analysis and common complexity classes.", CS, 1, 3, twoDaysAgo, yesterday)
        q.insert(N_CS_3, "Graph Algorithms: BFS & DFS", "Breadth-first search (BFS) explores level by level using a queue, finding shortest paths in unweighted graphs. Depth-first search (DFS) uses a stack (or recursion), useful for cycle detection and topological sorting. Dijkstra's algorithm extends BFS for weighted shortest paths using a priority queue.", "Graph traversal strategies and shortest path algorithms.", CS, 3, 5, twoDaysAgo, now)
        q.insert(N_CS_4, "Sorting Algorithms Compared", "Comparison sorts have a lower bound of O(n log n). Merge sort achieves this with O(n) extra space. Quick sort averages O(n log n) with O(1) extra space but O(n^2) worst case. Heap sort is in-place O(n log n). Non-comparison sorts like counting sort and radix sort run in O(n) for restricted inputs.", "Comparison of merge sort, quick sort, heap sort, and linear sorts.", CS, 4, 5, yesterday, now)
        q.insert(N_CS_5, "Dynamic Programming Fundamentals", "Dynamic programming solves problems with overlapping subproblems and optimal substructure by storing solutions to subproblems (memoization or tabulation). Classic examples: Fibonacci, knapsack, longest common subsequence, shortest paths. Top-down (recursive + memo) and bottom-up (iterative table) are the two main approaches.", "DP principles, memoization vs tabulation, and classic problems.", CS, 5, 4, yesterday, now)
    }

    private fun insertSemanticLinks(db: MindTagDatabase) {
        val q = db.semanticLinkEntityQueries
        var i = 1

        // Biology internal links
        q.insert("link-${i++}", N_BIO_1, N_BIO_2, 0.88, "PREREQUISITE", 0.9, now)   // cell division <-> DNA replication
        q.insert("link-${i++}", N_BIO_3, N_BIO_4, 0.92, "RELATED", 0.95, now)        // photosynthesis <-> respiration
        q.insert("link-${i++}", N_BIO_2, N_BIO_5, 0.75, "RELATED", 0.7, now)         // DNA replication <-> evolution
        q.insert("link-${i++}", N_BIO_1, N_BIO_5, 0.68, "RELATED", 0.6, now)         // cell division <-> evolution
        q.insert("link-${i++}", N_BIO_3, N_BIO_5, 0.60, "RELATED", 0.5, now)         // photosynthesis <-> evolution

        // Economics internal links
        q.insert("link-${i++}", N_ECON_1, N_ECON_4, 0.90, "PREREQUISITE", 0.95, now) // supply-demand <-> market structures
        q.insert("link-${i++}", N_ECON_2, N_ECON_3, 0.82, "RELATED", 0.8, now)       // GDP <-> monetary policy
        q.insert("link-${i++}", N_ECON_1, N_ECON_5, 0.78, "RELATED", 0.75, now)      // supply-demand <-> trade
        q.insert("link-${i++}", N_ECON_3, N_ECON_2, 0.80, "RELATED", 0.8, now)       // monetary policy -> GDP
        q.insert("link-${i++}", N_ECON_4, N_ECON_5, 0.72, "RELATED", 0.65, now)      // market structures <-> trade

        // CS internal links
        q.insert("link-${i++}", N_CS_2, N_CS_4, 0.93, "PREREQUISITE", 0.95, now)     // big-O <-> sorting
        q.insert("link-${i++}", N_CS_1, N_CS_3, 0.85, "RELATED", 0.85, now)          // BST <-> graph algorithms
        q.insert("link-${i++}", N_CS_2, N_CS_5, 0.88, "PREREQUISITE", 0.9, now)      // big-O <-> DP
        q.insert("link-${i++}", N_CS_3, N_CS_5, 0.80, "RELATED", 0.8, now)           // graph algorithms <-> DP
        q.insert("link-${i++}", N_CS_1, N_CS_4, 0.70, "RELATED", 0.65, now)          // BST <-> sorting

        // Cross-subject links
        q.insert("link-${i++}", N_CS_3, N_BIO_5, 0.45, "ANALOGY", 0.4, now)          // graph algorithms <-> evolution (phylogenetic trees)
        q.insert("link-${i++}", N_CS_5, N_ECON_5, 0.50, "ANALOGY", 0.45, now)        // DP <-> trade (optimization)
        q.insert("link-${i++}", N_BIO_4, N_ECON_2, 0.35, "ANALOGY", 0.3, now)        // Krebs cycle flows <-> GDP flows
        q.insert("link-${i++}", N_CS_2, N_ECON_4, 0.42, "ANALOGY", 0.35, now)        // complexity <-> market efficiency
        q.insert("link-${i}", N_BIO_3, N_ECON_1, 0.38, "ANALOGY", 0.3, now)          // photosynthesis inputs/outputs <-> supply-demand
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

    private fun insertUserProgress(db: MindTagDatabase) {
        val q = db.userProgressEntityQueries
        q.insert(BIO, 65.0, 3, 5, 72.0, 4, 1250, yesterday)
        q.insert(ECON, 42.0, 2, 5, 58.0, 2, 680, twoDaysAgo)
        q.insert(CS, 78.0, 4, 5, 85.0, 7, 2100, now)
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

    private fun insertPlannerTasks(db: MindTagDatabase) {
        val q = db.plannerTaskEntityQueries

        // Week 1 — all completed
        q.insert("t1_1", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Intro to Cognitive Science", "Psychology 101", "#3B82F6", "LECTURE", 1)
        q.insert("t1_2", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Chapter 1: Learning Fundamentals", "Psychology 101", "#3B82F6", "READING", 1)
        q.insert("t1_3", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Supply & Demand Basics", "Economics 101", "#F97316", "LECTURE", 1)
        q.insert("t1_4", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Week 1 Review Quiz", "Psychology 101", "#3B82F6", "QUIZ", 1)

        // Week 2 — mostly completed
        q.insert("t2_1", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Memory & Retention Models", "Psychology 101", "#3B82F6", "LECTURE", 1)
        q.insert("t2_2", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Organic Compound Structures", "Chemistry 201", "#22C55E", "LECTURE", 1)
        q.insert("t2_3", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Market Equilibrium Analysis", "Economics 101", "#F97316", "READING", 1)
        q.insert("t2_4", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Lab Report: Molecules", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
        q.insert("t2_5", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Concepts Quiz", "Psychology 101", "#3B82F6", "QUIZ", 1)

        // Week 3
        q.insert("t3_1", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Behavioral Economics Intro", "Economics 101", "#F97316", "LECTURE", 1)
        q.insert("t3_2", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Chapter 4: Decision Making", "Psychology 101", "#3B82F6", "READING", 1)
        q.insert("t3_3", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Reaction Kinetics Lab", "Chemistry 201", "#22C55E", "ASSIGNMENT", 1)
        q.insert("t3_4", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Applied Frameworks Quiz", "Economics 101", "#F97316", "QUIZ", 0)

        // Week 4 — current week, partially done
        q.insert("t4_1", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Review: Psych Chapters 1-4", "Psychology 101", "#3B82F6", "READING", 1)
        q.insert("t4_2", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Thermodynamics Lecture", "Chemistry 201", "#22C55E", "LECTURE", 1)
        q.insert("t4_3", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Macro vs Micro Economics", "Economics 101", "#F97316", "LECTURE", 0)
        q.insert("t4_4", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Practice Midterm Exam", "Psychology 101", "#3B82F6", "QUIZ", 0)

        // Week 5 — not started
        q.insert("t5_1", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Neuroplasticity & Learning", "Psychology 101", "#3B82F6", "LECTURE", 0)
        q.insert("t5_2", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Electrochemistry Basics", "Chemistry 201", "#22C55E", "LECTURE", 0)
        q.insert("t5_3", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "International Trade Theory", "Economics 101", "#F97316", "READING", 0)
        q.insert("t5_4", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Chem Lab: Electrochemistry", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
        q.insert("t5_5", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Advanced Topics Quiz", "Psychology 101", "#3B82F6", "QUIZ", 0)

        // Week 6 — not started
        q.insert("t6_1", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Comprehensive Review Session", "Psychology 101", "#3B82F6", "LECTURE", 0)
        q.insert("t6_2", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Final Lab Submission", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
        q.insert("t6_3", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Economics Policy Analysis", "Economics 101", "#F97316", "READING", 0)
    }
}
