-- =====================================================
-- SnapNotes — Dummy Data Seed (Supabase PostgreSQL)
-- Run in Supabase Dashboard -> SQL Editor (paste whole file, Run)
--
-- ADDITIVE and non-destructive: it only INSERTs. It never deletes or
-- updates existing rows, so your own notes, quizzes, XP and settings are
-- safe. The dummy data is attached to the EXISTING user_id = 1.
--
-- Idempotent: on re-run it detects the seeded sentinel note and skips,
-- so you will not get duplicates.
--
-- Note: if user_id 1 does not exist yet, it is created as:
--   email: alex@snapnotes.test   password: password123
-- An existing user 1 is never modified.
-- =====================================================

BEGIN;

DO $seed$
BEGIN
    -- Additive + idempotent. If the sentinel note is already present the
    -- seed has run before, so skip everything and leave the data alone.
    IF EXISTS (SELECT 1 FROM notes WHERE user_id = 1
               AND title = 'Cell Biology — Mitosis and Meiosis') THEN
        RAISE NOTICE 'SnapNotes dummy data for user 1 already present; skipping seed.';
        RETURN;
    END IF;

-- -----------------------------------------------------
-- 1. Demo user (user_id = 1)
--     Only created if missing; your existing user 1 is untouched.
-- -----------------------------------------------------
INSERT INTO users (user_id, name, email, password_hash, role)
VALUES (1, 'Alex Johnson', 'alex@snapnotes.test',
        '$2a$12$SlFhYcYzqtBSasHgXJt5N.BNNyJ9gFbGDmpgsdVGoKR8f7wMfoTr2', 'student')
ON CONFLICT (user_id) DO NOTHING;

PERFORM setval(pg_get_serial_sequence('users', 'user_id'),
               GREATEST((SELECT MAX(user_id) FROM users), 1));

-- -----------------------------------------------------
-- 2. Settings (skipped if you already saved settings)
-- -----------------------------------------------------
INSERT INTO user_settings (user_id, theme, accent, ai_model, notifications)
VALUES (1, 'dark', 'violet', 'llama-3.3-70b-versatile', TRUE)
ON CONFLICT (user_id) DO NOTHING;

-- -----------------------------------------------------
-- 3. Subject folders
-- -----------------------------------------------------
INSERT INTO subjects (user_id, name) VALUES
    (1, 'Biology'),
    (1, 'Calculus'),
    (1, 'World History'),
    (1, 'Computer Science')
ON CONFLICT (user_id, name) DO NOTHING;

-- -----------------------------------------------------
-- 4. Notes (spread over the last week -> gives a 7-day streak)
-- -----------------------------------------------------
INSERT INTO notes (user_id, title, pdf_path, extracted_text, summary, mindmap_json, created_at, bookmarked, subject_id)
VALUES
(1, 'Cell Biology — Mitosis and Meiosis',
 'studily-uploads/cell-biology.pdf',
 $t$Cell division is the process by which a parent cell divides into two or more daughter cells. Mitosis produces two genetically identical diploid cells and is used for growth and repair. It has four main phases: prophase, metaphase, anaphase, and telophase. Meiosis produces four genetically unique haploid gametes and involves two rounds of division, meiosis I and meiosis II. Crossing over during prophase I and independent assortment during metaphase I generate genetic diversity. Errors in meiosis can cause aneuploidy such as trisomy 21.$t$,
 $j${"summary":"Mitosis creates two identical diploid cells for growth and repair, while meiosis creates four unique haploid gametes for reproduction. Both share prophase, metaphase, anaphase and telophase, but meiosis adds a second division and generates diversity through crossing over and independent assortment.","key_concepts":["Mitosis vs meiosis","Phases of mitosis","Crossing over","Independent assortment","Haploid vs diploid","Aneuploidy"],"definitions":["Mitosis: division producing two genetically identical diploid cells","Meiosis: two-round division producing four genetically unique haploid cells","Crossing over: exchange of DNA between homologous chromosomes in prophase I","Aneuploidy: an abnormal number of chromosomes in a cell"],"exam_tips":["Memorise the order prophase, metaphase, anaphase, telophase","Distinguish meiosis I from meiosis II","Link nondisjunction to conditions such as trisomy 21"]}$j$,
 $j${"label":"Cell Division","children":[{"label":"Mitosis","children":[{"label":"Prophase","children":[]},{"label":"Metaphase","children":[]},{"label":"Anaphase","children":[]},{"label":"Telophase","children":[]}]},{"label":"Meiosis","children":[{"label":"Meiosis I","children":[]},{"label":"Meiosis II","children":[]}]},{"label":"Genetic Diversity","children":[{"label":"Crossing over","children":[]},{"label":"Independent assortment","children":[]}]}]}$j$,
 NOW() - INTERVAL '6 days', TRUE,
 (SELECT id FROM subjects WHERE user_id = 1 AND name = 'Biology')),

(1, 'Photosynthesis and Cellular Respiration',
 'studily-uploads/photosynthesis.pdf',
 $t$Photosynthesis converts light energy into chemical energy stored in glucose. It occurs in chloroplasts and has two stages: the light-dependent reactions in the thylakoid membranes and the Calvin cycle in the stroma. The light reactions produce ATP and NADPH and release oxygen from water. The Calvin cycle uses ATP and NADPH to fix carbon dioxide into glucose. Cellular respiration reverses the process in mitochondria, using glycolysis, the Krebs cycle, and oxidative phosphorylation to produce ATP. Together the two processes cycle carbon and oxygen through ecosystems.$t$,
 $j${"summary":"Photosynthesis captures light energy to build glucose while releasing oxygen, and cellular respiration breaks glucose down to release usable ATP. The two processes are complementary: the products of one are the inputs of the other.","key_concepts":["Light-dependent reactions","Calvin cycle","ATP and NADPH","Glycolysis","Krebs cycle","Oxidative phosphorylation"],"definitions":["Photosynthesis: conversion of light energy into chemical energy in glucose","Calvin cycle: light-independent reactions that fix CO2 into sugar","Glycolysis: breakdown of glucose into pyruvate in the cytoplasm","Oxidative phosphorylation: ATP production driven by the electron transport chain"],"exam_tips":["Compare where each stage happens: thylakoid, stroma, cytoplasm, mitochondrial matrix","Remember the light reactions make ATP and NADPH for the Calvin cycle","Trace the flow of electrons from water to NADPH to oxygen"]}$j$,
 $j${"label":"Energy in Cells","children":[{"label":"Photosynthesis","children":[{"label":"Light reactions","children":[]},{"label":"Calvin cycle","children":[]}]},{"label":"Respiration","children":[{"label":"Glycolysis","children":[]},{"label":"Krebs cycle","children":[]},{"label":"Electron transport","children":[]}]}]}$j$,
 NOW() - INTERVAL '5 days', FALSE,
 (SELECT id FROM subjects WHERE user_id = 1 AND name = 'Biology')),

(1, 'Derivatives and the Chain Rule',
 'studily-uploads/derivatives.pdf',
 $t$A derivative measures the instantaneous rate of change of a function. The power rule states that the derivative of x to the n is n times x to the n minus one. The product rule and quotient rule handle products and ratios of functions. The chain rule is used to differentiate composite functions: the derivative of f of g of x equals f prime of g of x times g prime of x. Higher order derivatives describe acceleration and curvature. Derivatives are used to find tangent lines, extrema, and rates in applied problems.$t$,
 $j${"summary":"Derivatives give the instantaneous rate of change of a function. Core techniques include the power, product, quotient, and chain rules, which together let you differentiate almost any elementary function.","key_concepts":["Definition of the derivative","Power rule","Product rule","Quotient rule","Chain rule","Higher order derivatives"],"definitions":["Derivative: the instantaneous rate of change of a function","Chain rule: rule for differentiating composite functions","Critical point: a point where the derivative is zero or undefined"],"exam_tips":["Always identify the outer and inner function before applying the chain rule","Check whether a product or quotient rule is really needed before expanding","Use derivatives to locate maxima and minima in optimisation problems"]}$j$,
 $j${"label":"Differentiation","children":[{"label":"Basic Rules","children":[{"label":"Power rule","children":[]},{"label":"Constant rule","children":[]}]},{"label":"Combined Rules","children":[{"label":"Product rule","children":[]},{"label":"Quotient rule","children":[]},{"label":"Chain rule","children":[]}]},{"label":"Applications","children":[{"label":"Tangent lines","children":[]},{"label":"Optimisation","children":[]}]}]}$j$,
 NOW() - INTERVAL '4 days', TRUE,
 (SELECT id FROM subjects WHERE user_id = 1 AND name = 'Calculus')),

(1, 'Integration Techniques',
 NULL,
 $t$Integration is the reverse of differentiation and computes accumulated change and area under a curve. The fundamental theorem of calculus links definite integrals to antiderivatives. Substitution reverses the chain rule, while integration by parts reverses the product rule and follows the formula u dv equals uv minus the integral of v du. Partial fractions decompose rational functions, and trigonometric identities simplify many integrals. Knowing when to use each technique is the main challenge.$t$,
 $j${"summary":"Integration reverses differentiation and measures accumulated change. The fundamental theorem of calculus connects antiderivatives to definite integrals, and techniques such as substitution, by parts, and partial fractions handle increasingly complex integrands.","key_concepts":["Fundamental theorem of calculus","Substitution","Integration by parts","Partial fractions","Definite vs indefinite integrals","Area under a curve"],"definitions":["Antiderivative: a function whose derivative is the original function","Definite integral: the signed area under a curve between two limits","Integration by parts: technique derived from the product rule"],"exam_tips":["Match the integrand to the technique instead of forcing one method","Remember the constant of integration for indefinite integrals","Use symmetry to simplify definite integrals over symmetric intervals"]}$j$,
 $j${"label":"Integration","children":[{"label":"Core Idea","children":[{"label":"Antiderivatives","children":[]},{"label":"Fundamental theorem","children":[]}]},{"label":"Techniques","children":[{"label":"Substitution","children":[]},{"label":"By parts","children":[]},{"label":"Partial fractions","children":[]}]},{"label":"Applications","children":[{"label":"Area under curve","children":[]}]}]}$j$,
 NOW() - INTERVAL '3 days', FALSE,
 (SELECT id FROM subjects WHERE user_id = 1 AND name = 'Calculus')),

(1, 'The French Revolution',
 NULL,
 $t$The French Revolution began in 1789 amid financial crisis, food shortages, and resentment of the privileged estates. The storming of the Bastille on 14 July 1789 became its defining symbol. The Declaration of the Rights of Man asserted liberty and equality. The monarchy was abolished in 1792 and Louis XVI was executed in 1793. The Reign of Terror under Robespierre saw mass executions before his fall in 1794. The revolution reshaped European politics and spread ideas of nationalism and popular sovereignty.$t$,
 $j${"summary":"The French Revolution overthrew the monarchy and the old social order between 1789 and 1799. Driven by inequality, financial crisis, and Enlightenment ideas, it produced radical change and the Reign of Terror before ending in the rise of Napoleon.","key_concepts":["Estates General","Storming of the Bastille","Declaration of the Rights of Man","Reign of Terror","Fall of the monarchy","Rise of Napoleon"],"definitions":["Bastille: a Paris fortress and prison stormed on 14 July 1789","Reign of Terror: period of mass executions led by Robespierre","Estates General: assembly representing the three French estates"],"exam_tips":["Sequence the key dates 1789, 1792, 1793, and 1799","Link Enlightenment ideas to revolutionary slogans","Explain how the Terror turned public opinion against the radicals"]}$j$,
 $j${"label":"French Revolution","children":[{"label":"Causes","children":[{"label":"Financial crisis","children":[]},{"label":"Social inequality","children":[]},{"label":"Enlightenment ideas","children":[]}]},{"label":"Key Events","children":[{"label":"Bastille 1789","children":[]},{"label":"Republic 1792","children":[]},{"label":"Reign of Terror","children":[]}]},{"label":"Outcomes","children":[{"label":"Napoleon","children":[]},{"label":"Nationalism","children":[]}]}]}$j$,
 NOW() - INTERVAL '2 days', FALSE,
 (SELECT id FROM subjects WHERE user_id = 1 AND name = 'World History')),

(1, 'Trees and Graph Traversal',
 'studily-uploads/trees-graphs.pdf',
 $t$A tree is a connected acyclic graph with a root, parent and child relationships, and no cycles. Binary search trees keep left children smaller and right children larger, giving average logarithmic search. Balanced trees such as AVL and red-black trees guarantee logarithmic height. Graph traversal visits vertices systematically: depth-first search explores as deep as possible using a stack or recursion, while breadth-first search explores level by level using a queue. Both run in time proportional to vertices plus edges and underpin shortest-path and cycle detection algorithms.$t$,
 $j${"summary":"Trees are acyclic connected graphs used for hierarchical data, and balanced variants keep operations logarithmic. Graphs are traversed with depth-first search using a stack or breadth-first search using a queue, both running in linear time.","key_concepts":["Tree terminology","Binary search trees","Balanced trees","Depth-first search","Breadth-first search","Time complexity of traversal"],"definitions":["Tree: a connected acyclic graph with a root","Binary search tree: a tree where left children are smaller and right children are larger","Breadth-first search: traversal that explores all neighbours before going deeper"],"exam_tips":["Remember DFS uses a stack and BFS uses a queue","Know that BST search is O(log n) on average but O(n) when skewed","Practice tracing traversal orders on a small tree"]}$j$,
 $j${"label":"Trees and Graphs","children":[{"label":"Trees","children":[{"label":"Binary search tree","children":[]},{"label":"Balanced trees","children":[]}]},{"label":"Traversal","children":[{"label":"Depth-first","children":[]},{"label":"Breadth-first","children":[]}]},{"label":"Complexity","children":[{"label":"O(V + E)","children":[]}]}]}$j$,
 NOW() - INTERVAL '1 day', TRUE,
 (SELECT id FROM subjects WHERE user_id = 1 AND name = 'Computer Science')),

(1, 'Big-O Complexity Analysis',
 NULL,
 $t$Big-O notation describes how the running time or space of an algorithm grows as input size grows, ignoring constants and lower order terms. Common classes in increasing order are constant, logarithmic, linear, linearithmic, quadratic, and exponential. Binary search is logarithmic, merge sort is linearithmic, and naive nested loops are often quadratic. Amortised analysis describes average cost over a sequence of operations, such as dynamic array resizing. Analysing complexity lets engineers compare algorithms and predict scalability.$t$,
 $j${"summary":"Big-O notation captures how an algorithm scales as input grows. Recognising the common complexity classes and how loops and recursion contribute to them lets you compare algorithms and predict performance.","key_concepts":["Asymptotic notation","Common complexity classes","Worst case vs average case","Amortised analysis","Space complexity","Recurrence relations"],"definitions":["Big-O: an upper bound on growth rate ignoring constants","Amortised analysis: average cost per operation over a sequence","Linearithmic: complexity of order n log n"],"exam_tips":["Drop constants and lower order terms when simplifying","Count nested loops and recursion depth to estimate complexity","Distinguish worst case from average case in your answers"]}$j$,
 $j${"label":"Big-O","children":[{"label":"Classes","children":[{"label":"Constant","children":[]},{"label":"Logarithmic","children":[]},{"label":"Linear","children":[]},{"label":"Linearithmic","children":[]},{"label":"Quadratic","children":[]}]},{"label":"Analysis","children":[{"label":"Worst case","children":[]},{"label":"Amortised","children":[]},{"label":"Space","children":[]}]}]}$j$,
 NOW(), FALSE,
 (SELECT id FROM subjects WHERE user_id = 1 AND name = 'Computer Science')),

(1, 'The Cold War in 10 Events',
 NULL,
 $t$The Cold War was a decades-long geopolitical rivalry between the United States and the Soviet Union. Key events include the Yalta and Potsdam conferences, the Berlin Blockade and Airlift, the formation of NATO and the Warsaw Pact, the Korean War, the Cuban Missile Crisis, the construction of the Berlin Wall, the Space Race, the Vietnam War, detente, and the fall of the Berlin Wall in 1989. The rivalry shaped global alliances, technology, and culture until the Soviet collapse in 1991.$t$,
 $j${"summary":"The Cold War was an ideological and strategic rivalry between the United States and the Soviet Union that shaped global politics from the late 1940s until 1991. Its flashpoints included Berlin, Korea, Cuba, and Vietnam.","key_concepts":["Containment","NATO and Warsaw Pact","Cuban Missile Crisis","Berlin Wall","Space Race","Detente"],"definitions":["Cold War: prolonged geopolitical rivalry without direct large-scale combat between the superpowers","Containment: US policy of limiting Soviet expansion","Detente: a period of eased tensions"],"exam_tips":["Order the major events chronologically","Link each event to the wider theme of superpower rivalry","Use the fall of the Berlin Wall as a turning point in your essays"]}$j$,
 $j${"label":"Cold War","children":[{"label":"Origins","children":[{"label":"Yalta and Potsdam","children":[]},{"label":"Containment","children":[]}]},{"label":"Flashpoints","children":[{"label":"Berlin","children":[]},{"label":"Cuba","children":[]},{"label":"Korea and Vietnam","children":[]}]},{"label":"End","children":[{"label":"Detente","children":[]},{"label":"Fall of the Wall 1989","children":[]}]}]}$j$,
 NOW() - INTERVAL '25 days', FALSE,
 (SELECT id FROM subjects WHERE user_id = 1 AND name = 'World History'));

-- -----------------------------------------------------
-- 5. Flashcards (5 per note)
-- -----------------------------------------------------
INSERT INTO flashcards (note_id, question, answer)
SELECT n.note_id, f.q, f.a
FROM (VALUES
    ('Cell Biology — Mitosis and Meiosis', 'How many daughter cells does mitosis produce?', 'Two genetically identical diploid cells'),
    ('Cell Biology — Mitosis and Meiosis', 'What is produced by meiosis?', 'Four genetically unique haploid gametes'),
    ('Cell Biology — Mitosis and Meiosis', 'In which phase does crossing over occur?', 'Prophase I of meiosis'),
    ('Cell Biology — Mitosis and Meiosis', 'What are the four phases of mitosis?', 'Prophase, metaphase, anaphase, telophase'),
    ('Cell Biology — Mitosis and Meiosis', 'What is aneuploidy?', 'An abnormal number of chromosomes in a cell'),

    ('Photosynthesis and Cellular Respiration', 'Where do the light-dependent reactions occur?', 'In the thylakoid membranes of the chloroplast'),
    ('Photosynthesis and Cellular Respiration', 'What does the Calvin cycle produce?', 'Glucose, by fixing carbon dioxide'),
    ('Photosynthesis and Cellular Respiration', 'Where does glycolysis take place?', 'In the cytoplasm'),
    ('Photosynthesis and Cellular Respiration', 'What are the three stages of cellular respiration?', 'Glycolysis, the Krebs cycle, and oxidative phosphorylation'),
    ('Photosynthesis and Cellular Respiration', 'Which gas is released by photosynthesis?', 'Oxygen, from the splitting of water'),

    ('Derivatives and the Chain Rule', 'What does a derivative measure?', 'The instantaneous rate of change of a function'),
    ('Derivatives and the Chain Rule', 'State the power rule.', 'The derivative of x to the n is n times x to the n minus one'),
    ('Derivatives and the Chain Rule', 'What is the chain rule?', 'The derivative of a composite is the outer derivative times the inner derivative'),
    ('Derivatives and the Chain Rule', 'When is the quotient rule used?', 'To differentiate a ratio of two functions'),
    ('Derivatives and the Chain Rule', 'What is a critical point?', 'A point where the derivative is zero or undefined'),

    ('Integration Techniques', 'What does integration compute?', 'Accumulated change and the area under a curve'),
    ('Integration Techniques', 'Which rule reverses the chain rule?', 'Substitution'),
    ('Integration Techniques', 'Which rule reverses the product rule?', 'Integration by parts'),
    ('Integration Techniques', 'What does the fundamental theorem of calculus link?', 'Definite integrals and antiderivatives'),
    ('Integration Techniques', 'When is partial fractions useful?', 'When integrating a rational function'),

    ('The French Revolution', 'In which year did the French Revolution begin?', '1789'),
    ('The French Revolution', 'What event on 14 July 1789 became its symbol?', 'The storming of the Bastille'),
    ('The French Revolution', 'Who led the Reign of Terror?', 'Robespierre'),
    ('The French Revolution', 'When was the monarchy abolished?', '1792'),
    ('The French Revolution', 'What did the Declaration of the Rights of Man assert?', 'Liberty and equality'),

    ('Trees and Graph Traversal', 'What data structure does depth-first search use?', 'A stack, or recursion'),
    ('Trees and Graph Traversal', 'What data structure does breadth-first search use?', 'A queue'),
    ('Trees and Graph Traversal', 'What is a tree?', 'A connected acyclic graph with a root'),
    ('Trees and Graph Traversal', 'What is the time complexity of graph traversal?', 'Proportional to vertices plus edges, O(V + E)'),
    ('Trees and Graph Traversal', 'In a binary search tree, how do left and right children compare?', 'Left children are smaller, right children are larger'),

    ('Big-O Complexity Analysis', 'What does Big-O notation describe?', 'How running time or space grows as input size grows'),
    ('Big-O Complexity Analysis', 'Which is faster growing: O(n) or O(log n)?', 'O(n) grows faster than O(log n)'),
    ('Big-O Complexity Analysis', 'What is the complexity of binary search?', 'O(log n)'),
    ('Big-O Complexity Analysis', 'What is the complexity of merge sort?', 'O(n log n), linearithmic'),
    ('Big-O Complexity Analysis', 'What is amortised analysis?', 'The average cost per operation over a sequence of operations'),

    ('The Cold War in 10 Events', 'Between which two superpowers was the Cold War fought?', 'The United States and the Soviet Union'),
    ('The Cold War in 10 Events', 'In which year did the Berlin Wall fall?', '1989'),
    ('The Cold War in 10 Events', 'What was containment?', 'The US policy of limiting Soviet expansion'),
    ('The Cold War in 10 Events', 'What was detente?', 'A period of eased tensions between the superpowers'),
    ('The Cold War in 10 Events', 'Which 1962 event brought the world close to nuclear war?', 'The Cuban Missile Crisis')
) AS f(note_title, q, a)
JOIN notes n ON n.user_id = 1 AND n.title = f.note_title;

-- -----------------------------------------------------
-- 6. MCQs (5 per note, for notes used in quizzes)
-- -----------------------------------------------------
INSERT INTO mcqs (note_id, question, option_a, option_b, option_c, option_d, correct_answer, explanation, difficulty)
SELECT n.note_id, m.q, m.a, m.b, m.c, m.d, m.correct, m.expl, m.diff
FROM (VALUES
    ('Cell Biology — Mitosis and Meiosis', 'How many daughter cells does meiosis produce?', 'One', 'Two', 'Four', 'Eight', 'C', 'Meiosis performs two rounds of division and yields four haploid cells.', 'easy'),
    ('Cell Biology — Mitosis and Meiosis', 'Which process creates genetic diversity?', 'Binary fission', 'Crossing over', 'Cytokinesis', 'Interphase', 'B', 'Crossing over exchanges DNA between homologous chromosomes.', 'medium'),
    ('Cell Biology — Mitosis and Meiosis', 'Mitosis produces cells that are...', 'Haploid and unique', 'Diploid and identical', 'Haploid and identical', 'Diploid and unique', 'B', 'Mitosis yields two genetically identical diploid cells.', 'easy'),
    ('Cell Biology — Mitosis and Meiosis', 'When do sister chromatids separate in meiosis?', 'Prophase I', 'Anaphase I', 'Anaphase II', 'Telophase I', 'C', 'Sister chromatids separate during anaphase II.', 'hard'),
    ('Cell Biology — Mitosis and Meiosis', 'Trisomy 21 is an example of...', 'Aneuploidy', 'Polyploidy', 'Mutation of a single base', 'Deletion of a chromosome', 'A', 'Trisomy 21 is an abnormal chromosome count, an aneuploidy.', 'medium'),

    ('Photosynthesis and Cellular Respiration', 'Where does the Calvin cycle occur?', 'Thylakoid membrane', 'Stroma', 'Cytoplasm', 'Mitochondrial matrix', 'B', 'The Calvin cycle takes place in the stroma.', 'medium'),
    ('Photosynthesis and Cellular Respiration', 'What gas is released by photosynthesis?', 'Carbon dioxide', 'Nitrogen', 'Oxygen', 'Methane', 'C', 'Oxygen is released when water is split in the light reactions.', 'easy'),
    ('Photosynthesis and Cellular Respiration', 'Where does glycolysis occur?', 'Cytoplasm', 'Nucleus', 'Chloroplast', 'Golgi apparatus', 'A', 'Glycolysis takes place in the cytoplasm.', 'easy'),
    ('Photosynthesis and Cellular Respiration', 'What do the light reactions supply to the Calvin cycle?', 'Glucose and oxygen', 'ATP and NADPH', 'Pyruvate and water', 'Carbon dioxide', 'B', 'The light reactions produce ATP and NADPH.', 'medium'),
    ('Photosynthesis and Cellular Respiration', 'Cellular respiration mainly occurs in the...', 'Chloroplast', 'Ribosome', 'Mitochondrion', 'Vacuole', 'C', 'Mitochondria are the site of aerobic respiration.', 'easy'),

    ('Derivatives and the Chain Rule', 'What is the derivative of x cubed?', 'x squared', '2x', '3x squared', '3x', 'C', 'By the power rule the derivative of x cubed is 3x squared.', 'easy'),
    ('Derivatives and the Chain Rule', 'The chain rule is used to differentiate...', 'Sums of functions', 'Composite functions', 'Constant functions', 'Integrals', 'B', 'The chain rule handles composite functions.', 'medium'),
    ('Derivatives and the Chain Rule', 'Which rule differentiates a ratio of functions?', 'Power rule', 'Product rule', 'Quotient rule', 'Chain rule', 'C', 'The quotient rule handles ratios.', 'easy'),
    ('Derivatives and the Chain Rule', 'At a local maximum, the first derivative is...', 'Positive', 'Negative', 'Zero', 'Undefined only', 'C', 'The derivative is zero at a smooth local extremum.', 'medium'),
    ('Derivatives and the Chain Rule', 'What does the second derivative describe?', 'Slope only', 'Acceleration and curvature', 'Area', 'A constant', 'B', 'The second derivative describes acceleration and curvature.', 'hard'),

    ('Integration Techniques', 'Which technique reverses the product rule?', 'Substitution', 'Partial fractions', 'Integration by parts', 'Trig substitution', 'C', 'Integration by parts reverses the product rule.', 'medium'),
    ('Integration Techniques', 'Which technique reverses the chain rule?', 'Substitution', 'By parts', 'Partial fractions', 'Differentiation', 'A', 'Substitution reverses the chain rule.', 'medium'),
    ('Integration Techniques', 'What does a definite integral compute?', 'A slope', 'Signed area under a curve', 'A limit', 'A derivative', 'B', 'A definite integral gives signed area between limits.', 'easy'),
    ('Integration Techniques', 'What must be added to an indefinite integral?', 'A constant of integration', 'A limit', 'An absolute value', 'Nothing', 'A', 'Indefinite integrals require the constant of integration.', 'easy'),
    ('Integration Techniques', 'Which method suits a rational function?', 'Substitution', 'Partial fractions', 'By parts', 'Implicit differentiation', 'B', 'Partial fractions decompose rational functions.', 'hard'),

    ('Trees and Graph Traversal', 'Which traversal uses a queue?', 'Depth-first search', 'Breadth-first search', 'Pre-order', 'Post-order', 'B', 'Breadth-first search uses a queue.', 'easy'),
    ('Trees and Graph Traversal', 'What is the time complexity of BFS on a graph?', 'O(log V)', 'O(V + E)', 'O(V squared) always', 'O(E log V)', 'B', 'Traversal visits each vertex and edge once.', 'medium'),
    ('Trees and Graph Traversal', 'A tree with n nodes has how many edges?', 'n', 'n + 1', 'n - 1', '2n', 'C', 'A tree is acyclic and connected, so it has n - 1 edges.', 'medium'),
    ('Trees and Graph Traversal', 'In a balanced BST, search is approximately...', 'O(1)', 'O(log n)', 'O(n)', 'O(n log n)', 'B', 'Balanced height gives logarithmic search.', 'medium'),
    ('Trees and Graph Traversal', 'Which search is best for the shortest path in an unweighted graph?', 'DFS', 'BFS', 'Binary search', 'Quicksearch', 'B', 'BFS finds shortest paths in unweighted graphs.', 'hard'),

    ('Big-O Complexity Analysis', 'Which grows fastest?', 'O(log n)', 'O(n)', 'O(n log n)', 'O(n squared)', 'D', 'Quadratic growth dominates the others.', 'easy'),
    ('Big-O Complexity Analysis', 'What is the complexity of binary search?', 'O(1)', 'O(log n)', 'O(n)', 'O(n log n)', 'B', 'Binary search halves the search space each step.', 'easy'),
    ('Big-O Complexity Analysis', 'What is the complexity of merge sort?', 'O(n)', 'O(n log n)', 'O(n squared)', 'O(log n)', 'B', 'Merge sort is linearithmic.', 'medium'),
    ('Big-O Complexity Analysis', 'In Big-O, constants are...', 'Kept', 'Dropped', 'Doubled', 'Inverted', 'B', 'Big-O ignores constant factors.', 'easy'),
    ('Big-O Complexity Analysis', 'Amortised analysis describes...', 'Worst single operation', 'Average cost over a sequence', 'Space only', 'Recursion depth', 'B', 'Amortised analysis averages cost across many operations.', 'hard'),

    ('The Cold War in 10 Events', 'The Cold War was mainly between the US and...', 'China', 'Germany', 'The Soviet Union', 'Japan', 'C', 'The rivalry was between the United States and the Soviet Union.', 'easy'),
    ('The Cold War in 10 Events', 'When did the Berlin Wall fall?', '1961', '1979', '1989', '1991', 'C', 'The Berlin Wall fell in 1989.', 'easy'),
    ('The Cold War in 10 Events', 'What was containment?', 'Limiting Soviet expansion', 'Building the wall', 'A space mission', 'A treaty with China', 'A', 'Containment was the US policy of limiting Soviet expansion.', 'medium'),
    ('The Cold War in 10 Events', 'Which 1962 crisis nearly caused nuclear war?', 'Korean War', 'Cuban Missile Crisis', 'Vietnam War', 'Berlin Blockade', 'B', 'The Cuban Missile Crisis occurred in 1962.', 'medium'),
    ('The Cold War in 10 Events', 'What was detente?', 'A nuclear test', 'A period of eased tension', 'An alliance', 'A blockade', 'B', 'Detente was a period of eased superpower tensions.', 'hard')
) AS m(note_title, q, a, b, c, d, correct, expl, diff)
JOIN notes n ON n.user_id = 1 AND n.title = m.note_title;

-- -----------------------------------------------------
-- 7. Quiz attempts (last week; scores deliberately mixed)
-- -----------------------------------------------------
INSERT INTO quiz_results (user_id, note_id, score, total_questions, attempt_date, elapsed_seconds)
VALUES
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Cell Biology — Mitosis and Meiosis'), 4, 5, NOW() - INTERVAL '6 days', 210),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Cell Biology — Mitosis and Meiosis'), 5, 5, NOW() - INTERVAL '5 days', 180),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Derivatives and the Chain Rule'), 3, 5, NOW() - INTERVAL '4 days', 300),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Integration Techniques'), 4, 5, NOW() - INTERVAL '3 days', 240),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Big-O Complexity Analysis'), 5, 5, NOW() - INTERVAL '1 day', 150),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Trees and Graph Traversal'), 4, 5, NOW(), 200);

-- Per-question answers, consistent with each result score
-- (first "score" MCQs of the note marked correct, the rest wrong).
INSERT INTO quiz_answers (result_id, mcq_id, user_id, chosen_answer, is_correct, note_question)
SELECT qr.id, m.id, qr.user_id,
       CASE WHEN m.rn <= qr.score THEN m.correct_answer
            ELSE CASE m.correct_answer
                     WHEN 'A' THEN 'B'
                     WHEN 'B' THEN 'A'
                     WHEN 'C' THEN 'D'
                     ELSE 'A'
                 END
       END,
       m.rn <= qr.score,
       m.question
FROM quiz_results qr
JOIN LATERAL (
    SELECT id, question, correct_answer,
           row_number() OVER (ORDER BY id) AS rn
    FROM mcqs
    WHERE note_id = qr.note_id
) m ON m.rn <= qr.total_questions
WHERE qr.user_id = 1;

-- -----------------------------------------------------
-- 8. Spaced-repetition reviews (some due now, some scheduled ahead)
-- -----------------------------------------------------
-- Note 1: all cards due for revision now.
INSERT INTO flashcard_reviews (card_id, user_id, ease_factor, interval_days, repetitions, due_date, last_rating, last_reviewed_at)
SELECT f.id, 1, 2.50, 1, 1, NOW() - INTERVAL '1 day', 3, NOW() - INTERVAL '2 days'
FROM flashcards f
JOIN notes n ON n.note_id = f.note_id
WHERE n.user_id = 1 AND n.title = 'Cell Biology — Mitosis and Meiosis';

-- Note 6: first two cards reviewed recently, scheduled in the future.
INSERT INTO flashcard_reviews (card_id, user_id, ease_factor, interval_days, repetitions, due_date, last_rating, last_reviewed_at)
SELECT f.id, 1, 2.60, 6, 3, NOW() + INTERVAL '5 days', 4, NOW() - INTERVAL '1 day'
FROM flashcards f
JOIN notes n ON n.note_id = f.note_id
WHERE n.user_id = 1 AND n.title = 'Trees and Graph Traversal'
ORDER BY f.id
LIMIT 2;

-- One card marked "again" and overdue, to exercise the hard-review path.
INSERT INTO flashcard_reviews (card_id, user_id, ease_factor, interval_days, repetitions, due_date, last_rating, last_reviewed_at)
SELECT f.id, 1, 1.70, 0, 0, NOW() - INTERVAL '3 days', 1, NOW() - INTERVAL '3 days'
FROM flashcards f
JOIN notes n ON n.note_id = f.note_id
WHERE n.user_id = 1 AND n.title = 'Photosynthesis and Cellular Respiration'
ORDER BY f.id
LIMIT 1;

-- -----------------------------------------------------
-- 9. AI chat history
-- -----------------------------------------------------
INSERT INTO chat_messages (user_id, note_id, role, content, created_at)
VALUES
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Trees and Graph Traversal'),
     'user', 'Why does breadth-first search use a queue instead of a stack?',
     NOW() - INTERVAL '1 day'),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Trees and Graph Traversal'),
     'assistant', 'A queue is first in first out, so the vertices discovered earliest are explored earliest. That produces level by level order and finds shortest paths in unweighted graphs. A stack would give depth-first order instead.',
     NOW() - INTERVAL '1 day' + INTERVAL '6 seconds'),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Big-O Complexity Analysis'),
     'user', 'Is O(n log n) better than O(n squared)?',
     NOW() - INTERVAL '20 hours'),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Big-O Complexity Analysis'),
     'assistant', 'Yes. For large inputs n log n grows much more slowly than n squared, so linearithmic algorithms like merge sort scale far better than quadratic ones.',
     NOW() - INTERVAL '20 hours' + INTERVAL '5 seconds'),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Cell Biology — Mitosis and Meiosis'),
     'user', 'What is the difference between meiosis I and meiosis II?',
     NOW() - INTERVAL '5 days'),
    (1, (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Cell Biology — Mitosis and Meiosis'),
     'assistant', 'Meiosis I separates homologous chromosomes and is where crossing over happens. Meiosis II looks more like mitosis and separates sister chromatids. The result is four haploid cells.',
     NOW() - INTERVAL '5 days' + INTERVAL '7 seconds');

-- -----------------------------------------------------
-- 10. Share link (secret token) on one note
-- -----------------------------------------------------
INSERT INTO note_shares (note_id, token)
VALUES (
    (SELECT note_id FROM notes WHERE user_id = 1 AND title = 'Cell Biology — Mitosis and Meiosis'),
    substr(md5(random()::text) || md5(random()::text), 1, 43)
);

-- -----------------------------------------------------
-- 11. XP events (drives the weekly leaderboard)
-- -----------------------------------------------------
INSERT INTO xp_events (user_id, xp, reason, created_at)
VALUES
    (1, 20, 'note_upload',      DATE_TRUNC('week', NOW()) + INTERVAL '2 hours'),
    (1, 50, 'quiz_completed',   DATE_TRUNC('week', NOW()) + INTERVAL '1 day 3 hours'),
    (1, 10, 'flashcard_review', DATE_TRUNC('week', NOW()) + INTERVAL '1 day 6 hours'),
    (1, 15, 'chat_question',    DATE_TRUNC('week', NOW()) + INTERVAL '2 days 1 hour'),
    (1, 30, 'study_session',    DATE_TRUNC('week', NOW()) + INTERVAL '2 days 5 hours'),
    (1, 25, 'note_upload',      NOW() - INTERVAL '9 days');

-- -----------------------------------------------------
-- 12. Badges actually supported by the seeded stats
--     (7-day streak + at least one completed quiz)
-- -----------------------------------------------------
INSERT INTO user_badges (user_id, badge_key, earned_at)
VALUES
    (1, 'first_quiz', NOW() - INTERVAL '6 days'),
    (1, 'streak_7',   NOW())
ON CONFLICT DO NOTHING;

END
$seed$;

COMMIT;

-- -----------------------------------------------------
-- Verification
-- -----------------------------------------------------
SELECT 'notes'           AS entity, COUNT(*) FROM notes           WHERE user_id = 1
UNION ALL SELECT 'subjects',        COUNT(*) FROM subjects        WHERE user_id = 1
UNION ALL SELECT 'flashcards',      COUNT(*) FROM flashcards f JOIN notes n ON n.note_id = f.note_id WHERE n.user_id = 1
UNION ALL SELECT 'mcqs',            COUNT(*) FROM mcqs m       JOIN notes n ON n.note_id = m.note_id WHERE n.user_id = 1
UNION ALL SELECT 'quiz_results',    COUNT(*) FROM quiz_results    WHERE user_id = 1
UNION ALL SELECT 'quiz_answers',    COUNT(*) FROM quiz_answers    WHERE user_id = 1
UNION ALL SELECT 'flashcard_reviews', COUNT(*) FROM flashcard_reviews WHERE user_id = 1
UNION ALL SELECT 'chat_messages',   COUNT(*) FROM chat_messages   WHERE user_id = 1
UNION ALL SELECT 'note_shares',     COUNT(*) FROM note_shares s JOIN notes n ON n.note_id = s.note_id WHERE n.user_id = 1
UNION ALL SELECT 'xp_events',       COUNT(*) FROM xp_events       WHERE user_id = 1
UNION ALL SELECT 'user_badges',     COUNT(*) FROM user_badges     WHERE user_id = 1;
