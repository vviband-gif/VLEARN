package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        Course::class,
        StudyPlan::class,
        Flashcard::class,
        Note::class,
        ForumMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VLearnDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun courseDao(): CourseDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun noteDao(): NoteDao
    abstract fun forumMessageDao(): ForumMessageDao

    companion object {
        @Volatile
        private var INSTANCE: VLearnDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): VLearnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VLearnDatabase::class.java,
                    "vlearn_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(VLearnDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class VLearnDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(db: VLearnDatabase) {
            val profileDao = db.userProfileDao()
            val courseDao = db.courseDao()
            val planDao = db.studyPlanDao()
            val cardDao = db.flashcardDao()
            val noteDao = db.noteDao()
            val messageDao = db.forumMessageDao()

            // 1. User Profile
            profileDao.insertOrUpdateProfile(
                UserProfile(
                    id = "current_user",
                    name = "Alex Mercer",
                    role = "Student",
                    schoolName = "Evergreen High School",
                    xp = 1850,
                    coins = 450,
                    gems = 60,
                    level = 6,
                    streak = 14,
                    lastActiveTime = System.currentTimeMillis(),
                    country = "United States",
                    gradeClass = "Grade 10",
                    subjects = "Science, Math, Programming",
                    avatar = "avatar_1",
                    username = "alex_mercer",
                    isOnboarded = false,
                    isLoggedIn = false
                )
            )

            // 2. Default Courses
            val initialCourses = listOf(
                Course(
                    title = "Introduction to Jetpack Compose",
                    subject = "Programming",
                    description = "Learn modern Android UI development using Jetpack Compose, state management, and modern patterns.",
                    instructor = "Dr. Helen Cho",
                    isEnrolled = true,
                    progressPercent = 40,
                    syllabus = "1. Introduction to Compose;2. Layouts, Columns & Rows;3. State and Recomposition;4. Modifiers & Sizing;5. Theme & Material 3 Styling;6. Animations in Compose;7. Navigation Compose;8. Final Capstone Project"
                ),
                Course(
                    title = "Calculus I: Limits & Derivatives",
                    subject = "Mathematics",
                    description = "Understand the fundamental concepts of limits, continuity, rates of change, and basic differentiation.",
                    instructor = "Prof. Silas Vance",
                    isEnrolled = true,
                    progressPercent = 20,
                    syllabus = "1. What is a Limit?;2. Standard Limit Properties;3. Continuity & Intermediate Value Theorem;4. Definition of a Derivative;5. Power, Product, and Quotient Rules;6. Chain Rule;7. Implicit Differentiation;8. Exam Practice"
                ),
                Course(
                    title = "Organic Chemistry Fundamentals",
                    subject = "Science",
                    description = "A deep dive into organic structures, functional groups, hybridization, and fundamental organic reactions.",
                    instructor = "Dr. Clara Oswald",
                    isEnrolled = false,
                    progressPercent = 0,
                    syllabus = "1. Hybridization & Molecular Orbitals;2. Alkanes and Cycloalkanes;3. Stereochemistry & Isomers;4. Nucleophilic Substitutions;5. Elimination Reactions;6. Spectroscopy (NMR, IR);7. Final Review"
                ),
                Course(
                    title = "Sovereign Personal Finance",
                    subject = "Commerce",
                    description = "Take absolute control of budgeting, compounding interest, investments, retirement accounts, and debt elimination strategies.",
                    instructor = "Michael Sterling",
                    isEnrolled = false,
                    progressPercent = 0,
                    syllabus = "1. Cashflow & Emergency Reserves;2. Power of Compound Interest;3. Index Funds and ETF Allocations;4. Understanding High-Interest Debt;5. Minimizing Taxable Drag;6. Real Estate Foundations;7. Financial Independence Roadmap"
                ),
                Course(
                    title = "Interactive Spanish: Beginners",
                    subject = "Languages",
                    description = "Master essential conversational Spanish including introductions, shopping, traveling, and core verb conjugations.",
                    instructor = "Sofia Ruiz",
                    isEnrolled = false,
                    progressPercent = 0,
                    syllabus = "1. Conversational Starters;2. Ser vs Estar;3. Numbers, Calendar, and Time;4. Asking for Directions;5. Ordering Food;6. Travel Vocabulary;7. Present Tense Conjugations"
                )
            )
            initialCourses.forEach { courseDao.insertCourse(it) }

            // 3. Study Plans
            val initialPlans = listOf(
                StudyPlan(
                    title = "Review Compose State management",
                    dueDate = "Today, 5:00 PM",
                    estimatedTime = 45,
                    isCompleted = false,
                    category = "Study"
                ),
                StudyPlan(
                    title = "Math Assignment 4: Derivative rules",
                    dueDate = "Tomorrow, 2:00 PM",
                    estimatedTime = 90,
                    isCompleted = false,
                    category = "Homework"
                ),
                StudyPlan(
                    title = "Web Development live tutorial",
                    dueDate = "Saturday, 11:00 AM",
                    estimatedTime = 60,
                    isCompleted = false,
                    category = "Live Class"
                )
            )
            initialPlans.forEach { planDao.insertStudyPlan(it) }

            // 4. Flashcards
            val initialCards = listOf(
                Flashcard(
                    frontText = "What is a Composable function?",
                    backText = "A function annotated with @Composable that describes how to map application state to a UI hierarchy in modern Android.",
                    subject = "Programming"
                ),
                Flashcard(
                    frontText = "Explain 'Recomposition' in Compose.",
                    backText = "The process of re-executing Composable functions when their underlying state/inputs change, updating the node tree.",
                    subject = "Programming"
                ),
                Flashcard(
                    frontText = "What is the Chain Rule in Calculus?",
                    backText = "A formula for computing the derivative of the composition of two or more functions: (f ∘ g)'(x) = f'(g(x)) * g'(x).",
                    subject = "Mathematics"
                ),
                Flashcard(
                    frontText = "Difference between Ser and Estar?",
                    backText = "Ser is used for permanent or lasting characteristics (identity, origin, profession). Estar is used for temporary states, locations, and feelings.",
                    subject = "Languages"
                )
            )
            initialCards.forEach { cardDao.insertFlashcard(it) }

            // 5. Notes
            val initialNotes = listOf(
                Note(
                    title = "Math: Limits Intuition",
                    content = "A limit is the value that a function approaches as the input approaches some value. It describes functional behavior *near* a point rather than *at* that point. Example: lim(x->2) (x^2 - 4)/(x - 2) = 4.",
                    subject = "Mathematics"
                ),
                Note(
                    title = "Compose: State vs Event",
                    content = "Unidirectional Data Flow (UDF) is key. State flows DOWN (from ViewModel to Composables) and Events flow UP (from UI actions back to ViewModel). This ensures predictability and easy testing.",
                    subject = "Programming"
                )
            )
            initialNotes.forEach { noteDao.insertNote(it) }

            // 6. Forum Messages
            val initialMessages = listOf(
                ForumMessage(
                    senderName = "Marcus Aurelius",
                    senderRole = "Student",
                    messageContent = "Does anyone want to form a weekly virtual study group for Calculus derivatives? We can quiz each other on Thursdays!",
                    subject = "Mathematics"
                ),
                ForumMessage(
                    senderName = "Dr. Helen Cho",
                    senderRole = "Teacher",
                    messageContent = "Remember class, the live session on Compose Custom Modifiers will begin tomorrow morning at 10 AM UTC. Please have your IDEs ready!",
                    subject = "Programming"
                )
            )
            initialMessages.forEach { messageDao.insertForumMessage(it) }
        }
    }
}
