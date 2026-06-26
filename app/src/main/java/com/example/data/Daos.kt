package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY title ASC")
    fun getAllCoursesFlow(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE isEnrolled = 1")
    fun getEnrolledCoursesFlow(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Update
    suspend fun updateCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Int)
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllStudyPlansFlow(): Flow<List<StudyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(plan: StudyPlan)

    @Update
    suspend fun updateStudyPlan(plan: StudyPlan)

    @Query("DELETE FROM study_plans WHERE id = :id")
    suspend fun deleteStudyPlanById(id: Int)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY frontText ASC")
    fun getAllFlashcardsFlow(): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE subject = :subject")
    fun getFlashcardsBySubjectFlow(subject: String): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: Flashcard)

    @Update
    suspend fun updateFlashcard(card: Flashcard)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcardById(id: Int)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY lastUpdated DESC")
    fun getAllNotesFlow(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface ForumMessageDao {
    @Query("SELECT * FROM forum_messages ORDER BY timestamp DESC")
    fun getAllForumMessagesFlow(): Flow<List<ForumMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumMessage(message: ForumMessage)
}
