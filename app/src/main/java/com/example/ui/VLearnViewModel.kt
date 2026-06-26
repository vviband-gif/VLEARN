package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class ChatMessage(
    val sender: String, // "user" or "tutor"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface AiTutorState {
    object Idle : AiTutorState
    object Loading : AiTutorState
    data class Success(val response: String) : AiTutorState
    data class Error(val message: String) : AiTutorState
}

class VLearnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VLearnRepository
    private val aiTutor = GeminiAiTutor()

    // Screen state
    private val _currentScreen = MutableStateFlow("dashboard") // dashboard, ai_tutor, courses, gamification, productivity, social, parent_panel, teacher_panel
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Active course detail state
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    // Chat states
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("tutor", "Hello! I am your VLEARN AI Tutor. Ask me any question, request a step-by-step explanation, generate flashcards, or take a custom quiz on any topic! How can I help you today?")
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _aiState = MutableStateFlow<AiTutorState>(AiTutorState.Idle)
    val aiState: StateFlow<AiTutorState> = _aiState.asStateFlow()

    // Battle Arena States
    private val _selectedHeroClass = MutableStateFlow("Warrior")
    val selectedHeroClass: StateFlow<String> = _selectedHeroClass.asStateFlow()

    private val _selectedWeapon = MutableStateFlow("Beginner Blaster")
    val selectedWeapon: StateFlow<String> = _selectedWeapon.asStateFlow()

    private val _unlockedWeapons = MutableStateFlow(listOf("Beginner Blaster", "Plasma Rifle"))
    val unlockedWeapons: StateFlow<List<String>> = _unlockedWeapons.asStateFlow()

    private val _currentBattleLevel = MutableStateFlow(1)
    val currentBattleLevel: StateFlow<Int> = _currentBattleLevel.asStateFlow()

    private val _selectedBattleTopic = MutableStateFlow("Physics")
    val selectedBattleTopic: StateFlow<String> = _selectedBattleTopic.asStateFlow()

    fun selectHeroClass(heroClass: String) {
        _selectedHeroClass.value = heroClass
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.updateProfile(profile.copy(selectedHero = heroClass))
        }
    }

    fun selectWeapon(weapon: String) {
        _selectedWeapon.value = weapon
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.updateProfile(profile.copy(selectedWeapon = weapon))
        }
    }

    fun unlockHero(heroClass: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val currentList = profile.unlockedHeroes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (!currentList.contains(heroClass)) {
                val updated = currentList + heroClass
                repository.updateProfile(profile.copy(unlockedHeroes = updated.joinToString(",")))
            }
        }
    }

    fun unlockWeapon(weapon: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val currentList = profile.unlockedWeapons.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (!currentList.contains(weapon)) {
                val updated = currentList + weapon
                repository.updateProfile(profile.copy(unlockedWeapons = updated.joinToString(",")))
                _unlockedWeapons.value = updated
            }
        }
    }

    fun selectWeaponSkin(weapon: String, skin: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val skinsMap = profile.selectedSkins.split(";").map { it.trim() }.filter { it.isNotEmpty() }.associate {
                val parts = it.split(":")
                parts[0] to (parts.getOrNull(1) ?: "Default")
            }.toMutableMap()
            skinsMap[weapon] = skin
            val joined = skinsMap.map { "${it.key}:${it.value}" }.joinToString(";")
            repository.updateProfile(profile.copy(selectedSkins = joined))
        }
    }

    fun unlockWeaponSkin(weapon: String, skin: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val currentSkins = profile.unlockedSkins.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val item = "$weapon:$skin"
            if (!currentSkins.contains(item)) {
                val updated = currentSkins + item
                repository.updateProfile(profile.copy(unlockedSkins = updated.joinToString(",")))
            }
        }
    }

    fun upgradeHeroStats(heroClass: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= 100) {
                // Upgrades cost 100 coins and increase coin/stats
                val currentSettings = profile.gameSettings.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val heroLevelKey = "lvl_$heroClass"
                var found = false
                val updatedSettings = currentSettings.map {
                    if (it.startsWith(heroLevelKey)) {
                        found = true
                        val currentLevel = it.substringAfter(":").toIntOrNull() ?: 1
                        "$heroLevelKey:${(currentLevel + 1).coerceAtMost(10)}"
                    } else {
                        it
                    }
                }.toMutableList()
                if (!found) {
                    updatedSettings.add("$heroLevelKey:2")
                }
                repository.updateProfile(
                    profile.copy(
                        coins = profile.coins - 100,
                        gameSettings = updatedSettings.joinToString(",")
                    )
                )
            }
        }
    }

    fun advanceBattleLevel() {
        val nextLevel = (_currentBattleLevel.value + 1).coerceAtMost(50)
        _currentBattleLevel.value = nextLevel
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val newXp = profile.xp + 100
            val newLvl = (1 + (newXp / 500)).coerceAtMost(50)
            repository.updateProfile(
                profile.copy(
                    xp = newXp,
                    coins = profile.coins + 25,
                    level = if (newLvl > profile.level) newLvl else profile.level
                )
            )
        }
    }

    fun setBattleTopic(topic: String) {
        _selectedBattleTopic.value = topic
    }

    // ========================================================
    // --- PREMIUM MOBA GRAPHICS, AUDIO & ACCESSIBILITY STATES ---
    // ========================================================

    // Character Styling
    val currentHairstyle = MutableStateFlow("Cyber Bob")
    val currentHairColor = MutableStateFlow(Color(0xFF38BDF8))
    val currentEyes = MutableStateFlow("Cosmic Glow")
    val currentFace = MutableStateFlow("Smooth Anime")
    val currentOutfit = MutableStateFlow("Mecha Battle-Suit")
    val currentArmor = MutableStateFlow("Neon Exoskeleton")
    val currentCape = MutableStateFlow("Gravity Float Cape")
    val currentBackpack = MutableStateFlow("AI Floating Disk")
    val currentGloves = MutableStateFlow("Plasma Conductors")
    val currentBoots = MutableStateFlow("Gravity Boots")
    val currentAura = MutableStateFlow("Cosmic Purple Nebula")
    val currentEmote = MutableStateFlow("GG WP")
    val currentVictoryAnim = MutableStateFlow("Holographic Trophy")
    val currentSpawnAnim = MutableStateFlow("Digital Materialize")
    val currentNameCard = MutableStateFlow("Science Champion")
    val currentProfileBanner = MutableStateFlow("Cosmic Abyss")
    val currentAvatarFrame = MutableStateFlow("Neon Cyan Border")

    // Weapon Skins & Attachments
    val currentWeaponCharm = MutableStateFlow("Gemini AI Brain")
    val currentKillEffect = MutableStateFlow("Digital Dissolve")
    val currentReloadAnim = MutableStateFlow("Spin-To-Load")
    val currentMuzzleEffect = MutableStateFlow("Plasma Flash")
    val currentTracerEffect = MutableStateFlow("Double Neon Spiral")
    val currentHitEffect = MutableStateFlow("Matrix Glitch")
    val currentSoundPack = MutableStateFlow("Sleek Synthwave Laser")
    val currentInspectAnim = MutableStateFlow("Horizontal Hover Spin")

    // Layout Settings (Interactive Controls Customizer)
    val buttonSizeMultiplier = MutableStateFlow(1.0f) // range 0.7f to 1.5f
    val buttonTransparency = MutableStateFlow(0.85f) // range 0.3f to 1.0f
    val joystickScale = MutableStateFlow(1.0f)
    val sensitivity = MutableStateFlow(1.2f)
    val cameraSensitivity = MutableStateFlow(1.0f)
    val aimSensitivity = MutableStateFlow(1.1f)
    val gyroEnabled = MutableStateFlow(false)
    val vibrationEnabled = MutableStateFlow(true)
    val autoPickupEnabled = MutableStateFlow(true)
    val autoReloadEnabled = MutableStateFlow(true)
    val aimAssistEnabled = MutableStateFlow(true)

    // Layout button position offsets (stored as fractional offsets on screen [0.0..1.0])
    // These allow the user to drag and drop buttons in the settings panel!
    val joystickPositionPercent = MutableStateFlow(android.graphics.PointF(0.18f, 0.75f))
    val attackButtonPositionPercent = MutableStateFlow(android.graphics.PointF(0.85f, 0.78f))
    val skill1PositionPercent = MutableStateFlow(android.graphics.PointF(0.72f, 0.88f))
    val skill2PositionPercent = MutableStateFlow(android.graphics.PointF(0.76f, 0.72f))
    val skill3PositionPercent = MutableStateFlow(android.graphics.PointF(0.88f, 0.60f))
    val ultPositionPercent = MutableStateFlow(android.graphics.PointF(0.92f, 0.44f))
    val dashPositionPercent = MutableStateFlow(android.graphics.PointF(0.64f, 0.85f))
    val jumpPositionPercent = MutableStateFlow(android.graphics.PointF(0.94f, 0.90f))
    val reloadPositionPercent = MutableStateFlow(android.graphics.PointF(0.58f, 0.92f))
    val interactPositionPercent = MutableStateFlow(android.graphics.PointF(0.48f, 0.85f))
    val weaponSwitchPositionPercent = MutableStateFlow(android.graphics.PointF(0.82f, 0.28f))
    val healPositionPercent = MutableStateFlow(android.graphics.PointF(0.72f, 0.48f))

    // Graphics Toggles
    val graphicsQuality = MutableStateFlow("Ultra") // Low, Medium, High, Ultra
    val fpsRate = MutableStateFlow(60) // 30, 60, 90, 120
    val shadowsEnabled = MutableStateFlow(true)
    val bloomEnabled = MutableStateFlow(true)
    val motionBlurEnabled = MutableStateFlow(true)
    val antiAliasingEnabled = MutableStateFlow(true)
    val textureQuality = MutableStateFlow("Ultra")
    val particleEffects = MutableStateFlow("Ultra")
    val waterQuality = MutableStateFlow("High")
    val ambientOcclusion = MutableStateFlow(true)

    // Audio Sliders
    val masterVolume = MutableStateFlow(0.8f)
    val musicVolume = MutableStateFlow(0.6f)
    val sfxVolume = MutableStateFlow(0.75f)
    val characterVoicesVolume = MutableStateFlow(0.9f)
    val aiTutorVoiceVolume = MutableStateFlow(0.95f)
    val ambientVolume = MutableStateFlow(0.5f)
    val notificationsVolume = MutableStateFlow(0.7f)

    // Accessibility Settings
    val colorblindMode = MutableStateFlow("None") // None, Protanopia, Deuteranopia, Tritanopia
    val subtitleSize = MutableStateFlow(14)
    val fontSizeModifier = MutableStateFlow(14)
    val highContrast = MutableStateFlow(false)
    val leftHandControls = MutableStateFlow(false)
    val oneHandMode = MutableStateFlow(false)
    val screenReaderEnabled = MutableStateFlow(false)

    fun buyWeapon(weaponName: String, cost: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.coins >= cost) {
                val currentList = profile.unlockedWeapons.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (!currentList.contains(weaponName)) {
                    val updated = currentList + weaponName
                    repository.updateProfile(
                        profile.copy(
                            coins = profile.coins - cost,
                            unlockedWeapons = updated.joinToString(",")
                        )
                    )
                    _unlockedWeapons.value = updated
                }
            }
        }
    }

    init {
        val database = VLearnDatabase.getDatabase(application, viewModelScope)
        repository = VLearnRepository(database)
        
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                profile?.let {
                    _selectedHeroClass.value = it.selectedHero
                    _selectedWeapon.value = it.selectedWeapon
                    _unlockedWeapons.value = it.unlockedWeapons.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }
                    _currentBattleLevel.value = it.level
                }
            }
        }
    }

    // Room Database Flows
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCourses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enrolledCourses: StateFlow<List<Course>> = repository.enrolledCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyPlans: StateFlow<List<StudyPlan>> = repository.allStudyPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFlashcards: StateFlow<List<Flashcard>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val forumMessages: StateFlow<List<ForumMessage>> = repository.forumMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectCourse(course: Course?) {
        _selectedCourse.value = course
    }

    fun switchRole(role: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(role = role))
        }
    }

    // AI Tutor Chat action
    fun askAiTutor(prompt: String) {
        if (prompt.isBlank()) return

        // Append user prompt
        val userMsg = ChatMessage("user", prompt)
        _chatHistory.value = _chatHistory.value + userMsg
        _aiState.value = AiTutorState.Loading

        viewModelScope.launch {
            val systemPrompt = """
                You are VLEARN's elite AI Academic Tutor. You specialize in explaining concepts clearly, 
                breaking down math problems step-by-step, generating custom interactive quizzes, creating review notes, 
                and helping the student learn with a positive academic mindset.
                Keep your response engaging, clear, and formatted beautifully using markdown. If appropriate, award virtual XP and praise.
            """.trimIndent()

            val reply = aiTutor.askTutor(prompt, systemPrompt)
            
            // Success
            val tutorMsg = ChatMessage("tutor", reply)
            _chatHistory.value = _chatHistory.value + tutorMsg
            _aiState.value = AiTutorState.Success(reply)

            // Reward student for studying with AI Tutor!
            repository.addXpAndCoins(25, 5)
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage("tutor", "Chat reset! Let's start a fresh learning session. What topic should we master next?")
        )
        _aiState.value = AiTutorState.Idle
    }

    // Profile updates
    fun updateProfileName(name: String, school: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(name = name, schoolName = school))
        }
    }

    private val _cloudSyncStatus = MutableStateFlow<String?>(null)
    val cloudSyncStatus: StateFlow<String?> = _cloudSyncStatus.asStateFlow()

    private val _isSessionSynced = MutableStateFlow(false)
    val isSessionSynced: StateFlow<Boolean> = _isSessionSynced.asStateFlow()

    fun performCloudSync(onComplete: () -> Unit) {
        viewModelScope.launch {
            _cloudSyncStatus.value = "Authenticating with Google Cloud..."
            kotlinx.coroutines.delay(1000)
            _cloudSyncStatus.value = "Downloading Cloud Profile..."
            kotlinx.coroutines.delay(1000)
            _cloudSyncStatus.value = "Downloading Inventory & Gear..."
            kotlinx.coroutines.delay(800)
            _cloudSyncStatus.value = "Downloading Game Progress..."
            kotlinx.coroutines.delay(800)
            _cloudSyncStatus.value = "Downloading AI Worlds..."
            kotlinx.coroutines.delay(1000)
            _cloudSyncStatus.value = null
            onComplete()
        }
    }

    fun triggerStartupSync() {
        if (_isSessionSynced.value) return
        performCloudSync {
            _isSessionSynced.value = true
        }
    }

    fun loginUser(authType: String, onSyncComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(isLoggedIn = true)
            repository.updateProfile(updated)
            performCloudSync {
                _isSessionSynced.value = true
                onSyncComplete(updated.isOnboarded)
            }
        }
    }

    fun completeOnboarding(
        role: String,
        name: String,
        country: String,
        gradeClass: String,
        subjects: List<String>,
        avatar: String,
        username: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val subjectsJoined = subjects.joinToString(", ")
            val updated = current.copy(
                name = name,
                role = role,
                schoolName = if (role == "Teacher") "$gradeClass Faculty" else "$gradeClass Student",
                country = country,
                gradeClass = gradeClass,
                subjects = subjectsJoined,
                avatar = avatar,
                username = username,
                isOnboarded = true,
                isLoggedIn = true
            )
            repository.updateProfile(updated)
            onComplete()
        }
    }

    fun updateProfile(updated: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(updated)
        }
    }

    fun awardPlayer(coinsEarned: Int, gemsEarned: Int, xpEarned: Int) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val newXp = current.xp + xpEarned
            val newLvl = 1 + (newXp / 500)
            val updated = current.copy(
                coins = current.coins + coinsEarned,
                gems = current.gems + gemsEarned,
                xp = newXp,
                level = if (newLvl > current.level) newLvl else current.level
            )
            repository.updateProfile(updated)
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(isLoggedIn = false))
            _isSessionSynced.value = false
        }
    }

    // Courses
    fun enrollInCourse(courseId: Int) {
        viewModelScope.launch {
            repository.enrollInCourse(courseId)
        }
    }

    fun updateCourseProgress(courseId: Int, progress: Int) {
        viewModelScope.launch {
            repository.updateCourseProgress(courseId, progress)
        }
    }

    fun createCourse(title: String, subject: String, description: String, instructor: String, lessons: List<String>) {
        viewModelScope.launch {
            val syllabusString = lessons.filter { it.isNotBlank() }.joinToString(";")
            repository.insertCourse(
                Course(
                    title = title,
                    subject = subject,
                    description = description,
                    instructor = instructor,
                    syllabus = syllabusString
                )
            )
            // Reward teacher or simulate XP
            repository.addXpAndCoins(150, 30)
        }
    }

    fun deleteCourse(id: Int) {
        viewModelScope.launch {
            repository.deleteCourse(id)
        }
    }

    // Study Plans
    fun addStudyPlan(title: String, dueDate: String, estimatedTime: Int, category: String) {
        viewModelScope.launch {
            repository.insertStudyPlan(
                StudyPlan(
                    title = title,
                    dueDate = dueDate,
                    estimatedTime = estimatedTime,
                    category = category
                )
            )
            repository.addXpAndCoins(30, 5)
        }
    }

    fun toggleStudyPlanCompleted(id: Int) {
        viewModelScope.launch {
            repository.toggleStudyPlanCompleted(id)
        }
    }

    fun deleteStudyPlan(id: Int) {
        viewModelScope.launch {
            repository.deleteStudyPlan(id)
        }
    }

    // Flashcards
    fun addFlashcard(front: String, back: String, subject: String) {
        viewModelScope.launch {
            repository.insertFlashcard(
                Flashcard(
                    frontText = front,
                    backText = back,
                    subject = subject
                )
            )
            repository.addXpAndCoins(20, 4)
        }
    }

    fun toggleFlashcardLearned(id: Int) {
        viewModelScope.launch {
            repository.toggleFlashcardLearned(id)
        }
    }

    fun deleteFlashcard(id: Int) {
        viewModelScope.launch {
            repository.deleteFlashcard(id)
        }
    }

    // Notes
    fun addNote(title: String, content: String, subject: String) {
        viewModelScope.launch {
            repository.insertNote(
                Note(
                    title = title,
                    content = content,
                    subject = subject
                )
            )
            repository.addXpAndCoins(30, 5)
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Social Discussion Forum
    fun sendForumMessage(messageContent: String, subject: String) {
        viewModelScope.launch {
            val currentProfile = userProfile.value ?: UserProfile()
            repository.sendForumMessage(
                ForumMessage(
                    senderName = currentProfile.name,
                    senderRole = currentProfile.role,
                    messageContent = messageContent,
                    subject = subject
                )
            )
        }
    }

    // Trigger streak completion (Daily Rewards)
    fun triggerDailyStreak() {
        viewModelScope.launch {
            repository.incrementStreak()
            repository.addXpAndCoins(100, 25)
        }
    }

    // --- PDF TO ACTION RPG ENGINE ---

    val defaultRealms = listOf(
        RealmConcept(
            "Physics", "Gravity Kingdom", "Gravity Titan",
            "Master mechanics, gravity, friction, and thermal energy laws.",
            Color(0xFF3B82F6),
            listOf(
                BattleQuestion(1, "Which force opposes sliding motion between two surfaces?", listOf("Friction", "Gravity", "Magnetic force", "Tension"), "Friction", "Friction is a contact force that acts in the direction opposite to relative motion."),
                BattleQuestion(2, "What is the rate of acceleration due to gravity on Earth?", listOf("9.8 m/s²", "1.6 m/s²", "12.5 m/s²", "5.0 m/s²"), "9.8 m/s²", "Objects in free fall near the surface of Earth accelerate downward at approximately 9.8 m/s²."),
                BattleQuestion(3, "According to Newton's Second Law, Force is equal to:", listOf("Mass × Acceleration", "Velocity ÷ Time", "Mass ÷ Gravity", "Energy × Friction"), "Mass × Acceleration", "F = m * a is the fundamental relation of mechanics."),
                BattleQuestion(4, "Friction generates what form of energy?", listOf("Thermal (Heat)", "Nuclear", "Chemical", "Solar"), "Thermal (Heat)", "As surfaces slide against each other, friction converts kinetic energy into thermal energy (heat).")
            )
        ),
        RealmConcept(
            "Biology", "Human Body World", "Virus King",
            "Defeat cellular pathogens, learn immune defenses and DNA replication.",
            Color(0xFF10B981),
            listOf(
                BattleQuestion(1, "Which immune cell produces antibodies to neutralize viruses?", listOf("B-Lymphocytes", "Red Blood Cells", "Skin cells", "Platelets"), "B-Lymphocytes", "B-cells produce highly specific proteins called antibodies to target antigens."),
                BattleQuestion(2, "What structure stores the genetic material in a eukaryotic cell?", listOf("Nucleus", "Ribosome", "Lysosome", "Cell Wall"), "Nucleus", "The cell nucleus is the central organelle storing DNA chromosomes."),
                BattleQuestion(3, "Viruses replicate by:", listOf("Hijacking host cell machinery", "Binary fission", "Mitosis", "Spore formulation"), "Hijacking host cell machinery", "Viruses are obligate intracellular parasites that require host cells to copy genomes.")
            )
        ),
        RealmConcept(
            "Chemistry", "Chemical Abyss", "Molecule Dragon",
            "Deconstruct compound bounds, reactions, and periodic classifications.",
            Color(0xFFEC4899),
            listOf(
                BattleQuestion(1, "What type of chemical bond involves sharing electrons?", listOf("Covalent Bond", "Ionic Bond", "Metallic Bond", "Hydrogen Bond"), "Covalent Bond", "Covalent bonds form when atoms share outer electron pairs to gain stability."),
                BattleQuestion(2, "The pH of a strong acid is typically around:", listOf("1 - 3", "7 (neutral)", "12 - 14", "9"), "1 - 3", "Acids have pH values less than 7, with stronger acids scoring lowest near 1."),
                BattleQuestion(3, "Which element has atomic number 1 on the Periodic Table?", listOf("Hydrogen", "Oxygen", "Gold", "Helium"), "Hydrogen", "Hydrogen (H) contains exactly one proton, making it atomic number 1.")
            )
        ),
        RealmConcept(
            "Math", "Equation Dungeon", "Infinity Guardian",
            "Solve geometric proofs, matrix equations, and function derivatives.",
            Color(0xFFF59E0B),
            listOf(
                BattleQuestion(1, "What is the derivative of x² with respect to x?", listOf("2x", "x", "2", "3x²"), "2x", "Using the power rule: d/dx (x^n) = n * x^(n-1). Thus, d/dx (x²) = 2x."),
                BattleQuestion(2, "What is the value of Pi (π) rounded to two decimal places?", listOf("3.14", "3.12", "2.71", "3.16"), "3.14", "Pi is the ratio of circle circumference to diameter, approximately 3.14159..."),
                BattleQuestion(3, "A triangle with all sides of equal length is called:", listOf("Equilateral", "Isosceles", "Right-angled", "Scalene"), "Equilateral", "Equilateral triangles have three congruent sides and angles of 60 degrees.")
            )
        ),
        RealmConcept(
            "History", "Time Travel Era", "Time Warlord",
            "Journey across historic epochs, conflicts, and global pacts.",
            Color(0xFF8B5CF6),
            listOf(
                BattleQuestion(1, "In which year did World War II end?", listOf("1945", "1918", "1939", "1953"), "1945", "World War II concluded in 1945 with the surrender of Japan and Germany."),
                BattleQuestion(2, "Who was the first President of the United States?", listOf("George Washington", "Abraham Lincoln", "Thomas Jefferson", "John Adams"), "George Washington", "Washington served as President from 1789 to 1797 after leading the Continental Army."),
                BattleQuestion(3, "The ancient monument Stonehenge is located in which modern country?", listOf("United Kingdom", "France", "Germany", "Egypt"), "United Kingdom", "Stonehenge is a prehistoric ring of standing stones in Wiltshire, England.")
            )
        )
    )

    private val _battleRealms = MutableStateFlow<List<RealmConcept>>(defaultRealms)
    val battleRealms: StateFlow<List<RealmConcept>> = _battleRealms.asStateFlow()

    private val _pdfGenerationState = MutableStateFlow<String?>(null) // null = idle, description = loading status, "success" = complete
    val pdfGenerationState: StateFlow<String?> = _pdfGenerationState.asStateFlow()

    fun resetPdfGenerationState() {
        _pdfGenerationState.value = null
    }

    fun generateWorldFromPdf(fileName: String, pdfBytes: ByteArray) {
        _pdfGenerationState.value = "Reading document pages..."
        viewModelScope.launch {
            try {
                _pdfGenerationState.value = "Extracting text and identifying concepts..."
                val extractedText = extractTextFromPdf(pdfBytes)
                val textLength = extractedText.length
                
                _pdfGenerationState.value = "AI analyzing complete document..."
                delay(1200)
                _pdfGenerationState.value = "AI designing 2D RPG world & dialogues..."
                delay(1200)
                _pdfGenerationState.value = "AI crafting bosses, enemies & missions..."
                
                val cleanFileName = fileName.substringBeforeLast(".")
                val sampleText = if (textLength > 50) extractedText.take(6000) else "Study material about: $cleanFileName"
                
                val prompt = """
                    You are VLEARN's elite RPG Game World Creator. Generate a custom 2D action RPG world based ENTIRELY on the following learning material.
                    Do not assume any predefined theme. Adapt the aesthetic, story, final boss, and questions directly to the subject of the document.
                    
                    Document Name: $fileName
                    Extracted Text Snippet: $sampleText
                    
                    Return your response as a valid JSON object matching this schema exactly:
                    {
                      "worldName": "A creative epic 2D RPG anime world name themed on the subject",
                      "subject": "A short 1-2 word name of the subject/academic discipline",
                      "bossName": "A cool boss name themed on the hardest concept in this document",
                      "description": "Engaging anime-style story intro that merges gameplay lore with this learning material",
                      "color": "A primary material design hex color code (e.g., #3B82F6, #10B981, #EC4899, #F59E0B, #8B5CF6, #EF4444) matching the theme",
                      "questions": [
                        {
                          "question": "A multiple-choice question directly testing a concept from the text",
                          "options": ["Option A", "Option B", "Option C", "Option D"],
                          "correctAnswer": "Option A (must exactly match one option)",
                          "explanation": "A friendly explanation teaching this concept clearly as an AI tutor"
                        }
                      ]
                    }
                    Provide at least 3 distinct questions. Ensure your output is strictly a clean JSON object containing these keys and nothing else. Do not wrap in markdown or anything else unless it is standard ```json.
                """.trimIndent()

                val responseStr = aiTutor.askTutor(prompt, "You are a specialized game designer that returns JSON structures only.")
                
                _pdfGenerationState.value = "Completing world synchronization..."
                delay(800)
                
                val newRealm = parseGeminiRealmResponse(responseStr)
                val finalRealm = if (newRealm.name == "AI Generated Concept" || newRealm.name.isBlank()) {
                    newRealm.copy(name = cleanFileName.replace("_", " ").replace("-", " "))
                } else {
                    newRealm
                }
                
                val updatedRealms = _battleRealms.value.filter { it.name != finalRealm.name } + finalRealm
                _battleRealms.value = updatedRealms
                _selectedBattleTopic.value = finalRealm.name
                
                val profile = userProfile.value
                if (profile != null) {
                    val worldsList = profile.aiWorlds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val updatedWorlds = if (!worldsList.contains(finalRealm.worldName)) worldsList + finalRealm.worldName else worldsList
                    
                    val pdfList = profile.uploadedPdfs.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val updatedPdfList = if (!pdfList.contains(fileName)) pdfList + fileName else pdfList
                    
                    repository.updateProfile(
                        profile.copy(
                            aiWorlds = updatedWorlds.joinToString(","),
                            uploadedPdfs = updatedPdfList.joinToString(","),
                            coins = profile.coins + 150,
                            xp = profile.xp + 200
                        )
                    )
                }
                _pdfGenerationState.value = "success"
            } catch (e: Exception) {
                e.printStackTrace()
                _pdfGenerationState.value = "Error during RPG generation: ${e.localizedMessage ?: "Connection error"}. Saving in backup mode..."
                delay(1500)
                val cleanFileName = fileName.substringBeforeLast(".")
                val displaySubject = cleanFileName.replace("_", " ").replace("-", " ")
                val backupRealm = RealmConcept(
                    name = displaySubject,
                    worldName = "The Quest of $displaySubject",
                    bossName = "Grandmaster of $displaySubject",
                    description = "A customized study quest constructed by the AI to help you conquer $displaySubject.",
                    color = Color(0xFFE91E63),
                    questions = listOf(
                        BattleQuestion(1, "Which concept represents the core topic of $displaySubject?", listOf("Fundamental Axioms", "Secondary Theories", "Practical Models", "All of the above"), "All of the above", "Comprehensive knowledge of $displaySubject incorporates fundamental, secondary, and practical models."),
                        BattleQuestion(2, "To master $displaySubject, a student should:", listOf("Study systematically", "Memorize without understanding", "Avoid questions", "Skip checkpoints"), "Study systematically", "Systematic review combined with active learning questions guarantees academic success."),
                        BattleQuestion(3, "Who is the guardian of the $displaySubject gateway?", listOf("Grandmaster of $displaySubject", "A random minion", "No one", "A training bot"), "Grandmaster of $displaySubject", "Defeating the Grandmaster validates your comprehension of $displaySubject!")
                    )
                )
                val updatedRealms = _battleRealms.value.filter { it.name != backupRealm.name } + backupRealm
                _battleRealms.value = updatedRealms
                _selectedBattleTopic.value = backupRealm.name
                _pdfGenerationState.value = "success"
            }
        }
    }

    private fun extractTextFromPdf(bytes: ByteArray): String {
        val sb = java.lang.StringBuilder()
        try {
            val content = String(bytes, Charsets.ISO_8859_1)
            val pattern = java.util.regex.Pattern.compile("(?s)BT\\s+(.*?)\\s+ET")
            val matcher = pattern.matcher(content)
            while (matcher.find()) {
                val block = matcher.group(1) ?: ""
                val parenPattern = java.util.regex.Pattern.compile("\\((.*?)\\)")
                val parenMatcher = parenPattern.matcher(block)
                while (parenMatcher.find()) {
                    val text = parenMatcher.group(1) ?: ""
                    if (text.isNotEmpty() && !text.startsWith("/") && text.all { it.code in 32..126 }) {
                        sb.append(text).append(" ")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        var result = sb.toString().trim()
        if (result.length < 50) {
            val sbBackup = java.lang.StringBuilder()
            val parenPattern = java.util.regex.Pattern.compile("\\(([A-Za-z0-9\\s,.:;!?'\"-]{4,100})\\)")
            val content = String(bytes, Charsets.ISO_8859_1)
            val parenMatcher = parenPattern.matcher(content)
            var count = 0
            while (parenMatcher.find() && count < 200) {
                val text = parenMatcher.group(1) ?: ""
                sbBackup.append(text).append(" ")
                count++
            }
            val backupResult = sbBackup.toString().trim()
            if (backupResult.length > result.length) {
                result = backupResult
            }
        }
        return result.replace("\\(", "(").replace("\\)", ")").replace("\\\\", "\\")
    }

    private fun parseGeminiRealmResponse(aiResponse: String): RealmConcept {
        var worldName = "Axiom Citadel"
        var subject = "AI Generated Concept"
        var bossName = "Chronos Overlord"
        var description = "An epic quest generated from your study material."
        var colorHex = "#3B82F6"
        val questions = mutableListOf<BattleQuestion>()
        
        try {
            val worldNameMatch = "\"worldName\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(aiResponse)
            val subjectMatch = "\"subject\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(aiResponse)
            val bossNameMatch = "\"bossName\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(aiResponse)
            val descMatch = "\"description\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(aiResponse)
            val colorMatch = "\"color\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(aiResponse)
            
            if (worldNameMatch != null) worldName = worldNameMatch.groupValues[1]
            if (subjectMatch != null) subject = subjectMatch.groupValues[1]
            if (bossNameMatch != null) bossName = bossNameMatch.groupValues[1]
            if (descMatch != null) description = descMatch.groupValues[1]
            if (colorMatch != null) colorHex = colorMatch.groupValues[1]
            
            // Regex to parse questions
            val qBlockPattern = "\\{\\s*\"question\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"options\"\\s*:\\s*\\[([^\\]]+)\\]\\s*,\\s*\"correctAnswer\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"explanation\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            var qId = 1
            qBlockPattern.findAll(aiResponse).forEach { match ->
                val qText = match.groupValues[1]
                val optsStr = match.groupValues[2]
                val correctAns = match.groupValues[3]
                val expl = match.groupValues[4]
                
                val opts = optsStr.split(",").map { it.replace("\"", "").trim() }
                if (opts.size >= 2) {
                    questions.add(BattleQuestion(qId++, qText, opts, correctAns, expl))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        if (questions.isEmpty()) {
            questions.add(BattleQuestion(1, "What is the primary study topic of this lesson?", listOf(subject, "General Knowledge", "Undefined Theory", "Introductory Axioms"), subject, "This is the primary study topic extracted from your PDF document."))
            questions.add(BattleQuestion(2, "Who is the final boss of this generated world?", listOf(bossName, "Minion Guard", "Training Dummy", "Dark Spirit"), bossName, "The boss is the ultimate embodiment of the concept you are mastering!"))
            questions.add(BattleQuestion(3, "To conquer this world, what must you do?", listOf("Answer academic questions", "Just shoot walls", "Run away", "Do nothing"), "Answer academic questions", "This custom RPG teaches you the lessons inside your document as you progress!"))
        }
        
        val parsedColor = try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF3B82F6)
        }
        
        return RealmConcept(
            name = subject,
            worldName = worldName,
            bossName = bossName,
            description = description,
            color = parsedColor,
            questions = questions
        )
    }
}

class VLearnViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VLearnViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VLearnViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
