# Diploma Project Specification: MindTag

**Project Title:** MindTag – Development of a mobile application for the automated construction of a semantic graph of study notes using language models.

**Student Name:** [Your Name]
**Department:** [Your Department]
**Date:** January 2026

---

## 1. Abstract

MindTag is an intelligent, cross-platform mobile application designed to address the challenge of information fragmentation in higher education. Engineering students generate vast amounts of unstructured data—lecture notes, code snippets, and definitions—across disparate subjects. Traditional note-taking tools lead to data silos, making comprehensive exam preparation difficult.

MindTag utilizes a hybrid architecture: an "offline-first" Kotlin Multiplatform (KMP) client for seamless data capture, and backend employing Large Language Models (LLMs) and Vector Embeddings for semantic analysis. The system automatically processes raw text to construct a dynamic **Semantic Knowledge Graph**, identifying implicit connections between concepts without manual tagging. This graph powers an adaptive "Smart Study Session" mode that improves retention through active recall and contextual synthesis.

---

## 2. Problem Statement

The core problem addressed by this project is **academic knowledge fragmentation**.

* **The Issue:** Students often fail to connect related concepts learned at different times or in different courses (e.g., linking a *Linear Algebra* concept from Year 1 to a *Machine Learning* algorithm in Year 3).
* **Existing Limitations:** Current tools rely on manual organization (folders, tags), which is time-consuming and brittle. Standard flashcard apps (like Anki) treat facts in isolation, ignoring the relational nature of complex engineering topics.
* **The Goal:** To move beyond passive note storage and create a proactive system that automatically organizes knowledge into a coherent mental model, reducing the cognitive load during exam preparation.

---

## 3. Main Objectives

1.  **Architectural Foundation:** To design and implement a scalable, cross-platform mobile architecture using **Kotlin Multiplatform (KMP)** and Clean Architecture principles, ensuring robust offline-first data synchronization with a server-side analysis module.
2.  **Semantic Analysis Pipeline:** To develop a backend system for the vectorization of unstructured text data, utilizing LLMs to automatically identify implicit semantic connections between discrete notes and construct a dynamic knowledge graph.
3.  **User Experience Strategy:** To define and implement user scenarios that address the specific needs of engineering students, focusing on friction-free data ingestion and high-value knowledge retrieval.
4.  **MVP & Validation:** To develop a Minimum Viable Product (MVP) validated through an **"Exam Preparation" flow**. This flow must demonstrate the system's ability to perform semantic retrieval (searching by concept rather than keyword) and visual aggregation of contextually linked materials.

---

## 4. Technical Architecture

The project employs a modern, hybrid intelligence stack designed for performance and scalability.

### 4.1 Mobile Client (Edge) + Desktop version
* **Framework:** Kotlin Multiplatform (KMP) sharing business logic across Android and iOS.
* **UI:** Jetpack Compose Multiplatform for a unified, declarative UI.
* **Architecture:** MVI (Model-View-Intent)
* **Local Data:** SQLDelight or Room for robust offline-first capability and local caching.

### 4.2 Backend (Cloud Core)
---

## 5. Core Functionality & User Flows

The application focuses on two primary workflows designed to minimize user effort and maximize learning value.

### Flow A: The "Fire and Forget" Ingestion Cycle
* **User Action:** The student quickly captures raw text after a lecture (e.g., *"Fragments have their own lifecycle distinct from Activities"*).
* **System Action (Local):** The note is saved immediately to the local database.
* **System Action (Remote):** In the background, the note is synced to the backend. The text is converted into a vector embedding. A similarity search identifies existing notes with a high cosine similarity score.
* **Value Realization:** The user receives a non-intrusive notification: *"New note linked to context: 'Android Activity Lifecycle' (from 3 weeks ago)."* No manual tagging is required.

### Flow B: The "Scoped Smart Session" (Exam Prep)
This flow addresses the question: *"How do I use this to prepare for an exam?"*

* **Step 1: Scope Selection (The Dashboard):** The student views progress rings for different subjects and selects a specific domain to study, e.g., **"Android Development"**. This ensures questions remain contextually relevant.
* **Step 2: Adaptive Generation (The "AI Director"):** The backend dynamically generates a sequence of **Smart Flashcards** based on graph topology within that scope:
    * **Mechanic 1: Deep Dive (Retention):** If a note is isolated or has a low retention score, the system generates a fact-check question (e.g., *"Define a Fragment"*).
    * **Mechanic 2: Synthesis (Understanding):** If two notes have a strong semantic link but haven't been reviewed together, the system generates a bridging question (e.g., *"Compare the lifecycle of a Fragment vs. an Activity regarding state retention"*).
* **Step 3: Active Recall & Feedback:** The student answers the card and rates their confidence (Easy/Hard).
    * *"Hard"* ratings prioritize the note for sooner review (Spaced Repetition).
    * *"Easy"* ratings visually strengthen the connection edge in the Knowledge Graph.

---

## 6. UI/UX Design Strategy

The design philosophy is "Minimalist Academic," focusing on reducing cognitive load.

1.  **The Dashboard (Macro View):** A high-level view of knowledge mastery. It uses progress rings per category to motivate the user to engage with neglected subjects.
2.  **The Session View (Micro View):** A distraction-free interface for the "Smart Flashcards." The UI clearly indicates whether the current task is a "Fact Check" or a "Synthesis Connection."
3.  **The Graph Visualization:** An interactive visual map allowing students to explore their mental model, revealing the "hidden" connections found by the AI.

---

## 7. Validation & Metrics

To ensure product-market fit and academic rigor, the MVP includes integrated feedback loops based on standard metrics.

* **CSAT (Customer Satisfaction Score):** Post-session micro-surveys (e.g., *"How relevant were these questions?"* 1-5 stars) to tune the semantic relevance thresholds of the backend algorithms.
* **NPS (Net Promoter Score):** Periodic prompts triggered by successful link discovery (e.g., *"Would you recommend MindTag?"* 0-10 scale) to measure overall perceived value.
* **Implicit Retention Tracking:** Monitoring the pass/fail rate of flashcards over time to validate the efficacy of the spaced repetition algorithm.

---

## 8. Scientific Novelty & Key Features

* **Automated Graph Construction:** Utilizing unsupervised semantic analysis to build a knowledge structure without requiring manual user input.
* **Context-Aware Study Methodology:** Moving beyond isolated fact memorization by programmatically generating study materials that emphasize the *relationships* between concepts.
* **Scoped Semantic Search:** Implementing domain-specific vector search boundaries to ensure study sessions remain focused on a single subject area while still leveraging the power of AI connection finding.