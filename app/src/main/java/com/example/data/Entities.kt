package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "user_profile")
@JsonClass(generateAdapter = true)
data class UserProfile(
    @PrimaryKey val id: String = "current_user",
    val name: String = "Alex Mercer",
    val role: String = "Student", // Student, Teacher, Parent, Admin
    val schoolName: String = "Evergreen High School",
    val xp: Int = 1250,
    val coins: Int = 340,
    val gems: Int = 45,
    val level: Int = 5,
    val streak: Int = 12,
    val lastActiveTime: Long = System.currentTimeMillis(),
    val country: String = "United States",
    val gradeClass: String = "Grade 10",
    val subjects: String = "Science, Math, Programming",
    val avatar: String = "avatar_1",
    val username: String = "alex_mercer",
    val isOnboarded: Boolean = false,
    val isLoggedIn: Boolean = false,
    
    // Extended RPG Progress & Cloud Save Fields
    val unlockedHeroes: String = "Warrior,Sniper,Guardian",
    val selectedHero: String = "Warrior",
    val unlockedWeapons: String = "Beginner Blaster,Plasma Rifle",
    val selectedWeapon: String = "Beginner Blaster",
    val unlockedSkins: String = "Beginner Blaster:Default,Plasma Rifle:Default",
    val selectedSkins: String = "Beginner Blaster:Default,Plasma Rifle:Default",
    val achievements: String = "First Milestone,Syllabus Conqueror,Immune Defender",
    val quizHistory: String = "Physics Quiz: 100%, Biology Quiz: 85%",
    val aiWorlds: String = "Gravity Kingdom,Human Body World,Chemical Abyss",
    val uploadedPdfs: String = "Physics Chapter 1: Newton's Laws.pdf,Biology Chapter 4: Cellular Photosynthesis.docx",
    val gameSettings: String = "SFX:On,Music:On,FPS:60"
)

@Entity(tableName = "courses")
@JsonClass(generateAdapter = true)
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String, // Science, Math, Programming, Languages, Commerce
    val description: String,
    val instructor: String,
    val isEnrolled: Boolean = false,
    val progressPercent: Int = 0,
    val syllabus: String // Semicolon separated list of lessons
)

@Entity(tableName = "study_plans")
@JsonClass(generateAdapter = true)
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dueDate: String,
    val estimatedTime: Int, // in minutes
    val isCompleted: Boolean = false,
    val category: String // Study, Exam, Homework, Live Class
)

@Entity(tableName = "flashcards")
@JsonClass(generateAdapter = true)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val frontText: String,
    val backText: String,
    val subject: String,
    val isLearned: Boolean = false
)

@Entity(tableName = "notes")
@JsonClass(generateAdapter = true)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val subject: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "forum_messages")
@JsonClass(generateAdapter = true)
data class ForumMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderRole: String,
    val messageContent: String,
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String
)
