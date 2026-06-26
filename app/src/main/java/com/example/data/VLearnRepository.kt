package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class VLearnRepository(private val db: VLearnDatabase) {

    // DAOs
    private val profileDao = db.userProfileDao()
    private val courseDao = db.courseDao()
    private val planDao = db.studyPlanDao()
    private val cardDao = db.flashcardDao()
    private val noteDao = db.noteDao()
    private val messageDao = db.forumMessageDao()

    // Flows
    val userProfile: Flow<UserProfile?> = profileDao.getUserProfileFlow()
    val allCourses: Flow<List<Course>> = courseDao.getAllCoursesFlow()
    val enrolledCourses: Flow<List<Course>> = courseDao.getEnrolledCoursesFlow()
    val allStudyPlans: Flow<List<StudyPlan>> = planDao.getAllStudyPlansFlow()
    val allFlashcards: Flow<List<Flashcard>> = cardDao.getAllFlashcardsFlow()
    val allNotes: Flow<List<Note>> = noteDao.getAllNotesFlow()
    val forumMessages: Flow<List<ForumMessage>> = messageDao.getAllForumMessagesFlow()

    // Gamification Rewards
    suspend fun addXpAndCoins(xpAmount: Int, coinAmount: Int) {
        val currentProfile = profileDao.getUserProfile() ?: UserProfile()
        var newXp = currentProfile.xp + xpAmount
        var newLevel = currentProfile.level
        var newCoins = currentProfile.coins + coinAmount

        // 500 XP per level
        val requiredXpForNextLevel = 500
        val targetLevel = (newXp / requiredXpForNextLevel) + 1
        var levelUpGems = 0
        if (targetLevel > newLevel) {
            levelUpGems = (targetLevel - newLevel) * 10
            newLevel = targetLevel
        }

        profileDao.insertOrUpdateProfile(
            currentProfile.copy(
                xp = newXp,
                level = newLevel,
                coins = newCoins,
                gems = currentProfile.gems + levelUpGems
            )
        )
    }

    suspend fun incrementStreak() {
        val currentProfile = profileDao.getUserProfile() ?: UserProfile()
        profileDao.insertOrUpdateProfile(
            currentProfile.copy(
                streak = currentProfile.streak + 1,
                lastActiveTime = System.currentTimeMillis()
            )
        )
    }

    // Profile Management
    suspend fun updateProfile(profile: UserProfile) {
        profileDao.insertOrUpdateProfile(profile)
    }

    // Courses
    suspend fun insertCourse(course: Course) = courseDao.insertCourse(course)
    suspend fun enrollInCourse(courseId: Int) {
        allCourses.firstOrNull()?.find { it.id == courseId }?.let { course ->
            courseDao.updateCourse(course.copy(isEnrolled = true, progressPercent = 0))
            addXpAndCoins(100, 20) // Award XP for enrolling
        }
    }
    suspend fun updateCourseProgress(courseId: Int, progress: Int) {
        allCourses.firstOrNull()?.find { it.id == courseId }?.let { course ->
            val finalProgress = progress.coerceIn(0, 100)
            courseDao.updateCourse(course.copy(progressPercent = finalProgress))
            if (finalProgress == 100 && course.progressPercent < 100) {
                // Award completion
                addXpAndCoins(300, 50)
            }
        }
    }
    suspend fun deleteCourse(id: Int) = courseDao.deleteCourseById(id)

    // Study Plans
    suspend fun insertStudyPlan(plan: StudyPlan) = planDao.insertStudyPlan(plan)
    suspend fun toggleStudyPlanCompleted(id: Int) {
        allStudyPlans.firstOrNull()?.find { it.id == id }?.let { plan ->
            val newState = !plan.isCompleted
            planDao.updateStudyPlan(plan.copy(isCompleted = newState))
            if (newState) {
                addXpAndCoins(50, 10) // Award completed task
            } else {
                addXpAndCoins(-50, -10)
            }
        }
    }
    suspend fun deleteStudyPlan(id: Int) = planDao.deleteStudyPlanById(id)

    // Flashcards
    suspend fun insertFlashcard(card: Flashcard) = cardDao.insertFlashcard(card)
    suspend fun toggleFlashcardLearned(id: Int) {
        allFlashcards.firstOrNull()?.find { it.id == id }?.let { card ->
            cardDao.updateFlashcard(card.copy(isLearned = !card.isLearned))
            if (!card.isLearned) {
                addXpAndCoins(10, 2)
            }
        }
    }
    suspend fun deleteFlashcard(id: Int) = cardDao.deleteFlashcardById(id)

    // Notes
    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.insertNote(note) // insert handles replacement
    suspend fun deleteNote(id: Int) = noteDao.deleteNoteById(id)

    // Forums
    suspend fun sendForumMessage(message: ForumMessage) {
        messageDao.insertForumMessage(message)
        addXpAndCoins(15, 2) // Award for posting in discussion board
    }
}
