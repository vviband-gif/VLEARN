package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserProfile
import com.example.ui.theme.GoldDark
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.SlateGrey
import com.example.ui.theme.VioletDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// --- RPG Game Data Models ---

data class HeroClass(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val passive: String,
    val skills: List<String>,
    val ultimate: String,
    val statAttack: Float,
    val statDefense: Float,
    val statSpeed: Float,
    val primaryColor: Color
)

data class BattleWeapon(
    val name: String,
    val damage: Int,
    val type: String, // Ranged, Melee
    val category: String, // Knife, Energy Blade, Sword, Hammer, Bow, Pistol, Rifle, Blaster, Laser, Plasma
    val description: String,
    val icon: ImageVector,
    val rarity: String, // Common, Uncommon, Rare, Epic, Legendary, Cosmic
    val attackSpeed: Float,
    val range: Float,
    val cooldown: Int,
    val color: Color,
    val bulletColor: Color = Color.Cyan,
    val bulletWidth: Float = 8f
)

data class RealmConcept(
    val name: String,
    val worldName: String,
    val bossName: String,
    val description: String,
    val color: Color,
    val questions: List<BattleQuestion>
)

data class BattleQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class GameEnemy(
    var id: Int,
    var name: String,
    var x: Float,
    var y: Float,
    var maxHp: Float,
    var currentHp: Float,
    var speed: Float,
    val color: Color,
    var isBoss: Boolean = false
)

data class GameProjectile(
    var id: Int,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val radius: Float = 8f
)

data class MapInteractable(
    val id: Int,
    val name: String, // "Treasure Chest", "Ancient Terminal", "Magic Gate"
    val x: Float,
    val y: Float,
    var isSolved: Boolean = false,
    val question: BattleQuestion,
    val icon: ImageVector
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BattleArenaScreen(viewModel: VLearnViewModel) {
    val selectedHero by viewModel.selectedHeroClass.collectAsStateWithLifecycle()
    val activeWeapon by viewModel.selectedWeapon.collectAsStateWithLifecycle()
    val unlockedWeapons by viewModel.unlockedWeapons.collectAsStateWithLifecycle()
    val battleLevel by viewModel.currentBattleLevel.collectAsStateWithLifecycle()
    val activeTopic by viewModel.selectedBattleTopic.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var isGameRunning by remember { mutableStateOf(false) }
    var isAssessmentMode by remember { mutableStateOf(false) }

    // Hardcoded premium 20 heroes
    val heroClasses = listOf(
        HeroClass("Guardian", Icons.Default.Security, "Sentinel of knowledge protecting allies with absolute aegis.", "Shield Regeneration", listOf("Holo-Barrier", "Shield Bash", "Iron Dome"), "Aegis Protocol", 0.5f, 1.0f, 0.4f, Color(0xFF3B82F6)),
        HeroClass("Warrior", Icons.Default.Shield, "Melee juggernaut with massive offensive power.", "Adrenaline Force", listOf("Heavy Strike", "Ground Slam", "Berserk Cleave"), "Blade Storm", 0.75f, 0.8f, 0.5f, Color(0xFFEF4444)),
        HeroClass("Knight", Icons.Default.MilitaryTech, "Holy armored champion of truth.", "Chivalry Code", listOf("Lance Lunge", "Shield Wall", "Valiant Charge"), "Heavenly Smite", 0.7f, 0.9f, 0.45f, Color(0xFFFFB300)),
        HeroClass("Archer", Icons.Default.CenterFocusStrong, "Precision bowman with swift speed.", "Sighting Focus", listOf("Triple Shot", "Pinpoint Arrow", "Wind Glide"), "Arrow Tempest", 0.8f, 0.5f, 0.85f, Color(0xFF10B981)),
        HeroClass("Sniper", Icons.Default.MyLocation, "Long-range precision shooter with orbital support.", "Eagle Eye Focus", listOf("Laser Sight", "Piercing Bullet", "Smoke Screen"), "Orbital Railgun", 0.95f, 0.4f, 0.75f, Color(0xFF06B6D4)),
        HeroClass("Engineer", Icons.Default.Construction, "Tech expert utilizing defense sentries and fields.", "Mechanical Overclock", listOf("Deploy Sentry", "Overclock Charge", "Tesla Mine"), "Nanite Swarm Field", 0.65f, 0.75f, 0.6f, Color(0xFF14B8A6)),
        HeroClass("Mage", Icons.Default.AutoAwesome, "Master of arcane elements and cosmic disruption.", "Mana Regeneration Flow", listOf("Arcane Burst", "Ice Shards", "Flame Wave"), "Supernova Meteor", 0.85f, 0.5f, 0.65f, Color(0xFFA855F7)),
        HeroClass("Ninja", Icons.Default.Bolt, "Stealth specialist striking from shadows.", "Shadow Step", listOf("Shuriken Fan", "Smoke Bomb", "Kunai Dash"), "Shadow Clone Slash", 0.9f, 0.4f, 0.95f, Color(0xFF06B6D4)),
        HeroClass("Assassin", Icons.Default.FlashOn, "Agile shadow warrior dealing high burst damage.", "Lethal Strike Accuracy", listOf("Twin Blades", "Vanish Cloak", "Poison Dart"), "Death Mark Execution", 0.95f, 0.35f, 0.95f, Color(0xFFEC4899)),
        HeroClass("Medic", Icons.Default.MedicalServices, "Combat healer reinforcing team shields.", "Nanomedic Aura", listOf("Heal Beam", "Regeneration Field", "Stim Pack Boost"), "Reviving Aurora Glow", 0.4f, 0.8f, 0.7f, Color(0xFF22C55E)),
        HeroClass("Scout", Icons.Default.DirectionsRun, "Swift runner bypassing enemy defenses.", "Fleetfoot Speed", listOf("Sprint Dash", "Recon Radar Scan", "Flashbang Blinding"), "Apex Overdrive", 0.6f, 0.5f, 1.0f, Color(0xFF38BDF8)),
        HeroClass("Heavy Gunner", Icons.Default.Campaign, "Tactical juggernaut with immense fire volume.", "Heavy Fortitude Base", listOf("Minigun Sweep", "Grenade Launch", "Suppressing Barrage"), "Total Weapon Discharge", 0.85f, 0.7f, 0.45f, Color(0xFFF97316)),
        HeroClass("Ranger", Icons.Default.Pets, "Wildland tracker utilizing traps and beasts.", "Survival Tracker", listOf("Hunting Trap", "Viper Arrow Shot", "Wolf Companion Call"), "Nature's Vengeance", 0.75f, 0.65f, 0.75f, Color(0xFF84CC16)),
        HeroClass("Cyber Soldier", Icons.Default.Psychology, "Augmented elite utilizing energetic shields.", "Augmented Core Speed", listOf("Plasma Repeater", "Jetpack Booster", "EMP Disruption"), "Core Fusion Overdrive", 0.8f, 0.7f, 0.8f, Color(0xFF3B82F6)),
        HeroClass("Robot", Icons.Default.SmartToy, "Machine intelligence with titanium chassis.", "Alloy Plating Shield", listOf("Rocket Fist Launch", "Laser Beam Sweep", "System Self Repair"), "Mega Beam Emission", 0.85f, 0.85f, 0.45f, Color(0xFF64748B)),
        HeroClass("Elementalist", Icons.Default.WbSunny, "Forces of nature controller.", "Prismatic Resonance", listOf("Gale Force Strike", "Earth Spike Rupture", "Pyroblast Fusion"), "Elemental Cataclysm", 0.9f, 0.55f, 0.65f, Color(0xFFF43F5E)),
        HeroClass("Shadow Hunter", Icons.Default.FilterDrama, "Ethereal void warrior manipulating gravity.", "Soul Drain Rebound", listOf("Void Portal Touch", "Shadow Slash Burst", "Fear Screamer Field"), "Void Collapse Explosion", 0.85f, 0.6f, 0.75f, Color(0xFF475569)),
        HeroClass("Paladin", Icons.Default.WorkspacePremium, "Shield of light restoring vitality.", "Holy Beacon Radiance", listOf("Smite Infusion", "Aura Aegis Shield", "Lay on Hands Heal"), "Heavenly Light Judgment", 0.75f, 0.95f, 0.4f, Color(0xFFFACC15)),
        HeroClass("Berserker", Icons.Default.Whatshot, "Raging berserker dealing more damage at low HP.", "Frenzied Bloodthirst", listOf("Anger Spike", "Heavy Battleaxe Cleave", "Roar of Fury"), "God of War Wrath", 0.95f, 0.5f, 0.65f, Color(0xFFDC2626)),
        HeroClass("Explorer", Icons.Default.Explore, "Adventurer with high speed and critical rates.", "Loot Navigation Radar", listOf("Grappling Hook", "Torch Ignite", "Pistol Whip Strike"), "Epic Cartographer Bounty", 0.65f, 0.6f, 0.85f, Color(0xFF14B8A6))
    )

    // Complete catalog of 50 weapons spanning all categories
    val weapons = listOf(
        // Combat knives (6)
        BattleWeapon("Carbon Steel Knife", 12, "Melee", "Knife", "Standard combat utility blade.", Icons.Default.Square, "Common", 1.5f, 150f, 10, Color(0xFF94A3B8)),
        BattleWeapon("Vibro-Dagger", 24, "Melee", "Knife", "High-frequency vibrating blade.", Icons.Default.FlashOn, "Uncommon", 1.8f, 160f, 8, Color(0xFF38BDF8)),
        BattleWeapon("Shadow Dirk", 38, "Melee", "Knife", "Injected with void particles.", Icons.Default.FilterDrama, "Rare", 2.0f, 170f, 6, Color(0xFF8B5CF6)),
        BattleWeapon("Quantum Carver", 55, "Melee", "Knife", "Subatomic matter separator.", Icons.Default.Bolt, "Epic", 2.2f, 180f, 5, Color(0xFFEC4899)),
        BattleWeapon("Nanotech Stiletto", 78, "Melee", "Knife", "Injects armor-dissolving nanites.", Icons.Default.AutoAwesome, "Legendary", 2.5f, 190f, 4, Color(0xFFF59E0B)),
        BattleWeapon("Cosmic Void Shard", 110, "Melee", "Knife", "Forged from a collapsed white dwarf.", Icons.Default.WbSunny, "Cosmic", 2.8f, 200f, 3, Color(0xFFFFB300)),

        // Energy blades & Swords (8)
        BattleWeapon("Beginner Blaster", 15, "Ranged", "Sci-fi Blaster", "Standard-issue educational beam weapon.", Icons.Default.Grain, "Common", 1.2f, 350f, 15, Color(0xFF94A3B8)),
        BattleWeapon("Recruit Sabre", 20, "Melee", "Sword", "Solid steel cadet training sword.", Icons.Default.Shield, "Common", 1.0f, 180f, 12, Color(0xFF94A3B8)),
        BattleWeapon("Titanium Katana", 35, "Melee", "Sword", "Lightweight carbon alloy sword.", Icons.Default.ContentCut, "Uncommon", 1.2f, 190f, 10, Color(0xFF38BDF8)),
        BattleWeapon("Plasma Claymore", 58, "Melee", "Sword", "Superheated broadsword with magnetic grip.", Icons.Default.Whatshot, "Rare", 0.9f, 200f, 14, Color(0xFFEF4444)),
        BattleWeapon("Photon Broadsword", 82, "Melee", "Sword", "Solidified thermal light saber.", Icons.Default.WbSunny, "Epic", 1.3f, 210f, 8, Color(0xFFFFB300)),
        BattleWeapon("Nebula Edge", 115, "Melee", "Sword", "Pulsating star energy blade.", Icons.Default.AutoAwesome, "Legendary", 1.4f, 220f, 6, Color(0xFFA855F7)),
        BattleWeapon("Apocalypse Saber", 145, "Melee", "Sword", "Ultimate cosmic divider.", Icons.Default.Bolt, "Cosmic", 1.5f, 240f, 5, Color(0xFFFFD700)),
        BattleWeapon("Chrono Slicer", 125, "Melee", "Sword", "Bends space-time forward during slashes.", Icons.Default.Timer, "Cosmic", 1.6f, 230f, 5, Color(0xFF06B6D4)),

        // Heavy Hammers & Maces (6)
        BattleWeapon("Iron Sledge", 40, "Melee", "Hammer", "Crude heavy demolition hammer.", Icons.Default.Gavel, "Common", 0.5f, 160f, 22, Color(0xFF94A3B8)),
        BattleWeapon("Kinetic Mallet", 65, "Melee", "Hammer", "Converts impact force into kinetic waves.", Icons.Default.Waves, "Uncommon", 0.6f, 170f, 18, Color(0xFF38BDF8)),
        BattleWeapon("Gravity Gavel", 95, "Melee", "Hammer", "Generates micro gravity anomalies on impact.", Icons.Default.HorizontalRule, "Rare", 0.5f, 180f, 16, Color(0xFF10B981)),
        BattleWeapon("Quake Warhammer", 130, "Melee", "Hammer", "Cracks the earth with tectonic force.", Icons.Default.Grid3x3, "Epic", 0.6f, 190f, 14, Color(0xFFF97316)),
        BattleWeapon("Supernova Breaker", 180, "Melee", "Hammer", "Emits mini fusion explosions on impact.", Icons.Default.RestartAlt, "Legendary", 0.7f, 200f, 12, Color(0xFFFFD700)),
        BattleWeapon("Infinity Star Pulverizer", 250, "Melee", "Hammer", "Heavy cosmic mass pulping tool.", Icons.Default.WorkspacePremium, "Cosmic", 0.8f, 220f, 10, Color(0xFFEC4899)),

        // Cyber Bows & Launchers (6)
        BattleWeapon("Recurve Bow", 18, "Ranged", "Bow", "Classic compound carbon bow.", Icons.Default.Gesture, "Common", 1.0f, 400f, 16, Color(0xFF94A3B8)),
        BattleWeapon("Compound Laser Bow", 32, "Ranged", "Bow", "Fires focused light arrow vectors.", Icons.Default.Share, "Uncommon", 1.2f, 420f, 14, Color(0xFF38BDF8)),
        BattleWeapon("Tactical Crossbow", 52, "Ranged", "Bow", "Automatic bolt feeder.", Icons.Default.ListAlt, "Rare", 1.1f, 440f, 12, Color(0xFF10B981)),
        BattleWeapon("Magnetic Arrow Launcher", 78, "Ranged", "Bow", "Coilgun launcher accelerating broadheads.", Icons.Default.ViewAgenda, "Epic", 1.3f, 460f, 10, Color(0xFFF59E0B)),
        BattleWeapon("Singularity Arbalest", 112, "Ranged", "Bow", "Fires unstable black hole arrows.", Icons.Default.BlurCircular, "Legendary", 1.4f, 480f, 8, Color(0xFFA855F7)),
        BattleWeapon("Astraea Cosmic Bow", 160, "Ranged", "Bow", "Constellation starlight shooter.", Icons.Default.Stars, "Cosmic", 1.6f, 500f, 6, Color(0xFFFFD700)),

        // Pistols & Sci-fi Blasters (8)
        BattleWeapon("9mm Service Pistol", 15, "Ranged", "Pistol", "Reliable semi-automatic sidearm.", Icons.Default.Send, "Common", 1.2f, 360f, 14, Color(0xFF94A3B8)),
        BattleWeapon("Luger Cyber Blaster", 28, "Ranged", "Pistol", "Compact plasma energy blaster.", Icons.Default.PowerSettingsNew, "Uncommon", 1.4f, 380f, 12, Color(0xFF38BDF8)),
        BattleWeapon("Pulse Handgun", 45, "Ranged", "Pistol", "Fires rapid kinetic electromagnetic pulses.", Icons.Default.WifiTethering, "Rare", 1.5f, 400f, 10, Color(0xFF10B981)),
        BattleWeapon("Dual Laser Revolvers", 64, "Ranged", "Pistol", "High spin photonic guns.", Icons.Default.SettingsAccessibility, "Epic", 1.8f, 410f, 8, Color(0xFFEC4899)),
        BattleWeapon("Plasma Magnum", 92, "Ranged", "Pistol", "Heavy charge burst handgun.", Icons.Default.OfflineBolt, "Legendary", 1.3f, 430f, 7, Color(0xFFEF4444)),
        BattleWeapon("Omega Particle Pistol", 135, "Ranged", "Pistol", "Disintegrates target molecules.", Icons.Default.Dangerous, "Cosmic", 1.6f, 450f, 6, Color(0xFFFFB300)),
        BattleWeapon("Chrono Repeater", 70, "Ranged", "Pistol", "Quick clock-wound bullet loader.", Icons.Default.Timer, "Epic", 1.7f, 420f, 8, Color(0xFF06B6D4)),
        BattleWeapon("Quasar Blaster", 105, "Ranged", "Pistol", "Shoots rotating cosmic beacon stars.", Icons.Default.Brightness5, "Legendary", 1.5f, 440f, 7, Color(0xFFA855F7)),

        // Assault & Sniper Rifles (8)
        BattleWeapon("Rifle Model 4", 25, "Ranged", "Rifle", "Standard military-issue carbine.", Icons.Default.LinearScale, "Common", 0.9f, 450f, 20, Color(0xFF94A3B8)),
        BattleWeapon("Tactical Carbine", 42, "Ranged", "Rifle", "Accurate triple burst rifle.", Icons.Default.GridGoldenratio, "Uncommon", 1.1f, 470f, 16, Color(0xFF38BDF8)),
        BattleWeapon("Heavy Sniper", 85, "Ranged", "Rifle", "High caliber armor-piercing long gun.", Icons.Default.Adjust, "Rare", 0.4f, 650f, 30, Color(0xFF06B6D4)),
        BattleWeapon("Arc Rifle", 62, "Ranged", "Rifle", "Fires chaining lightning bolts.", Icons.Default.ElectricBolt, "Epic", 1.3f, 500f, 14, Color(0xFFF59E0B)),
        BattleWeapon("Laser Railgun", 130, "Ranged", "Rifle", "Hypersonic copper rail projectile weapon.", Icons.Default.Maximize, "Legendary", 0.3f, 700f, 28, Color(0xFFFFD700)),
        BattleWeapon("Apex Plasma Rifle", 95, "Ranged", "Rifle", "Rapid energetic ball launcher.", Icons.Default.OfflineBolt, "Epic", 1.4f, 520f, 12, Color(0xFFEC4899)),
        BattleWeapon("Dimension Sniper", 210, "Ranged", "Rifle", "Fires through pocket dimensions safely.", Icons.Default.BlurOn, "Cosmic", 0.2f, 800f, 40, Color(0xFFA855F7)),
        BattleWeapon("Supercritical Phased Rifle", 150, "Ranged", "Rifle", "Shoots high speed quantum waves.", Icons.Default.Vibration, "Cosmic", 1.2f, 550f, 10, Color(0xFFFF5722)),

        // Heavy Machine Guns & Shotguns (8)
        BattleWeapon("Old-School Pump Shotgun", 35, "Ranged", "Shotgun", "Deals immense point-blank pellet damage.", Icons.Default.Grain, "Common", 0.7f, 250f, 22, Color(0xFF94A3B8)),
        BattleWeapon("Spitfire LMG", 28, "Ranged", "Rifle", "Sustained high-capacity automatic fire.", Icons.Default.DensityMedium, "Uncommon", 1.6f, 420f, 10, Color(0xFF38BDF8)),
        BattleWeapon("Tactical Scattergun", 56, "Ranged", "Shotgun", "Spreads magnetic shards horizontally.", Icons.Default.PivotTableChart, "Rare", 0.8f, 260f, 18, Color(0xFF10B981)),
        BattleWeapon("Vulcan Minigun", 40, "Ranged", "Rifle", "6-barrel rotary firepower monster.", Icons.Default.Autorenew, "Epic", 2.2f, 450f, 6, Color(0xFFF97316)),
        BattleWeapon("Maelstrom Plasma Sweeper", 72, "Ranged", "Laser", "Sweeping fire broad plasma arcs.", Icons.Default.Cyclone, "Legendary", 1.8f, 300f, 9, Color(0xFFEF4444)),
        BattleWeapon("Devastator Flak Cannon", 110, "Ranged", "Plasma", "Explosive shrapnel flak cannon.", Icons.Default.Report, "Epic", 1.0f, 380f, 15, Color(0xFFFFB300)),
        BattleWeapon("Giga-Watt Shockwave Gun", 140, "Ranged", "Laser", "Discharges heavy static fields.", Icons.Default.FlashOff, "Legendary", 1.2f, 320f, 11, Color(0xFF00E676)),
        BattleWeapon("Armageddon Shredder", 195, "Ranged", "Laser", "Cosmic heavy metal storm gun.", Icons.Default.Whatshot, "Cosmic", 2.0f, 480f, 5, Color(0xFFD500F9))
    )

    val realms by viewModel.battleRealms.collectAsStateWithLifecycle()

    val activeRealm = realms.firstOrNull { it.name == activeTopic } ?: realms.getOrElse(0) {
        RealmConcept(
            "General", "Study World", "Concept Guardian",
            "Master your study concepts.",
            Color(0xFF3B82F6),
            listOf(
                BattleQuestion(1, "Ready to start learning?", listOf("Yes", "Absolutely"), "Yes", "Let's begin!")
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .testTag("battle_arena_screen")
    ) {
        AnimatedContent(
            targetState = isGameRunning to isAssessmentMode,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "GameModeTransition"
        ) { (running, assessment) ->
            if (running) {
                ActiveBattleScreen(
                    viewModel = viewModel,
                    hero = heroClasses.firstOrNull { it.name == selectedHero } ?: heroClasses[0],
                    weapon = weapons.firstOrNull { it.name == activeWeapon } ?: weapons[0],
                    realm = activeRealm,
                    battleLevel = battleLevel,
                    onQuit = { isGameRunning = false },
                    onCompleteLevel = {
                        viewModel.advanceBattleLevel()
                        if (battleLevel % 10 == 9) {
                            isAssessmentMode = true
                        } else {
                            isGameRunning = false
                        }
                    }
                )
            } else if (assessment) {
                AssessmentScreen(
                    viewModel = viewModel,
                    realm = activeRealm,
                    onFinished = {
                        isAssessmentMode = false
                        isGameRunning = false
                    }
                )
            } else {
                BattleLobbyScreen(
                    viewModel = viewModel,
                    heroClasses = heroClasses,
                    weapons = weapons,
                    realms = realms,
                    selectedHero = selectedHero,
                    activeWeapon = activeWeapon,
                    unlockedWeapons = unlockedWeapons,
                    activeTopic = activeTopic,
                    battleLevel = battleLevel,
                    userProfile = userProfile,
                    onStartBattle = {
                        if (battleLevel % 10 == 0 && battleLevel > 0 && !isAssessmentMode) {
                            // Prompt Milestone Exam at multiples of 10 levels
                            isAssessmentMode = true
                        } else {
                            isGameRunning = true
                        }
                    }
                )
            }
        }
    }
}

// --- BATTLE LOBBY ---

@Composable
fun BattleLobbyScreen(
    viewModel: VLearnViewModel,
    heroClasses: List<HeroClass>,
    weapons: List<BattleWeapon>,
    realms: List<RealmConcept>,
    selectedHero: String,
    activeWeapon: String,
    unlockedWeapons: List<String>,
    activeTopic: String,
    battleLevel: Int,
    userProfile: UserProfile?,
    onStartBattle: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(0) } // 0 = Realm, 1 = Class, 2 = Armory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper Title HUD with glowing tech theme
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = GoldDark,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VLEARN AI RPG",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "A Side-Scrolling RPG powered by study materials",
                    color = SlateGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Current Level Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(IndigoDark, VioletDark)))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Lv. $battleLevel",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs in Lobby
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                    color = GoldDark,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("1. Select Realm", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("2. Choose Hero", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text("3. Armory", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedSection) {
                0 -> RealmSelectionTab(realms, activeTopic, onSelect = { viewModel.setBattleTopic(it) })
                1 -> HeroSelectionTab(viewModel, heroClasses, selectedHero, userProfile)
                2 -> ArmoryTab(viewModel, weapons, activeWeapon, unlockedWeapons, userProfile)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enter Arena CTA Button
        Button(
            onClick = onStartBattle,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldDark,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_battle_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (battleLevel % 10 == 0 && battleLevel > 0) "CHALLENGE MILESTONE EXAM" else "ENTER BATTLE ARENA",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// --- SUB-TABS ---

@Composable
fun RealmSelectionTab(
    realms: List<RealmConcept>,
    activeTopic: String,
    onSelect: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(realms) { realm ->
            val isSelected = realm.name == activeTopic
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) realm.color.copy(alpha = 0.15f) else Color(0xFF1E293B).copy(alpha = 0.6f)
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) realm.color else Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(realm.name) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(realm.color.copy(alpha = 0.2f))
                            .border(1.dp, realm.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (realm.name) {
                                "Physics" -> Icons.Default.Explore
                                "Biology" -> Icons.Default.Vaccines
                                "Chemistry" -> Icons.Default.Biotech
                                "Math" -> Icons.Default.Percent
                                else -> Icons.Default.HistoryEdu
                            },
                            contentDescription = null,
                            tint = realm.color,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = realm.worldName.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = realm.color
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(realm.color)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ACTIVE REALM", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                        Text(
                            text = realm.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = realm.description,
                            fontSize = 12.sp,
                            color = SlateGrey,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = GoldDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Boss: ${realm.bossName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSelectionTab(
    viewModel: VLearnViewModel,
    heroClasses: List<HeroClass>,
    selectedHero: String,
    userProfile: UserProfile?
) {
    val unlockedHeroes = remember(userProfile) {
        userProfile?.unlockedHeroes?.split(",")?.map { it.trim() } ?: listOf("Guardian", "Warrior", "Knight")
    }

    // AI Challenge dialog state
    var challengeHero by remember { mutableStateOf<HeroClass?>(null) }
    var step by remember { mutableStateOf(1) } // 1 = Quiz, 2 = Success, 3 = Fail
    var activeQIndex by remember { mutableStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }

    // Hardcoded challenge questions for unlocks
    val challengeQuestions = listOf(
        BattleQuestion(1, "What organelle converts glucose into ATP?", listOf("Mitochondria", "Nucleus", "Ribosome", "Lysosome"), "Mitochondria", "Mitochondria are the powerhouses of the cell, performing cellular respiration to produce energy."),
        BattleQuestion(2, "In programming, a function that calls itself is called:", listOf("Recursive", "Iterative", "Void", "Static"), "Recursive", "Recursion happens when a function calls itself directly or indirectly."),
        BattleQuestion(3, "Which element makes up the majority of Earth's atmosphere?", listOf("Nitrogen", "Oxygen", "Carbon Dioxide", "Hydrogen"), "Nitrogen", "Nitrogen comprises approximately 78% of the atmosphere.")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(heroClasses) { hero ->
            val isUnlocked = unlockedHeroes.contains(hero.name)
            val isSelected = hero.name == selectedHero

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) hero.primaryColor.copy(alpha = 0.15f) else Color(0xFF1E293B).copy(alpha = 0.6f)
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) hero.primaryColor else Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(hero.primaryColor.copy(alpha = 0.2f))
                                .border(1.dp, hero.primaryColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = hero.icon,
                                contentDescription = null,
                                tint = hero.primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = hero.name.uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = hero.description,
                                fontSize = 11.sp,
                                color = SlateGrey
                            )
                        }

                        if (isUnlocked) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(hero.primaryColor)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("EQUIPPED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.selectHeroClass(hero.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = hero.primaryColor, contentColor = Color.Black),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("SELECT", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        } else {
                            // Locked - Challenge Option
                            Button(
                                onClick = {
                                    challengeHero = hero
                                    step = 1
                                    activeQIndex = 0
                                    userAnswers.clear()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI QUIZ", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HeroStatBar("Attack", hero.statAttack, hero.primaryColor, Modifier.weight(1f))
                        HeroStatBar("Defense", hero.statDefense, hero.primaryColor, Modifier.weight(1f))
                        HeroStatBar("Speed", hero.statSpeed, hero.primaryColor, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ultimate: ${hero.ultimate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldDark
                        )
                        Text(
                            text = "Passive: ${hero.passive}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF67E8F9)
                        )
                    }

                    // Upgrade Stats Button using coins (costs 100 coins)
                    if (isUnlocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.upgradeHeroStats(hero.name) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = (userProfile?.coins ?: 0) >= 100
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Upgrade, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("UPGRADE STATS (100 Coins)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // AI CHALLENGE UNLOCK MODAL
    if (challengeHero != null) {
        val targetHero = challengeHero!!
        AlertDialog(
            onDismissRequest = { challengeHero = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Text(
                    text = if (step == 1) "UNLOCK HERO: ${targetHero.name.uppercase()}" else "AI CHALLENGE RESULT",
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    if (step == 1) {
                        val q = challengeQuestions[activeQIndex]
                        Text(
                            "Demonstrate academic mastery to unlock this hero! Question ${activeQIndex + 1}/3",
                            fontSize = 12.sp,
                            color = SlateGrey
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = q.question,
                                color = Color.White,
                                modifier = Modifier.padding(14.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        q.options.forEach { opt ->
                            val isSelected = userAnswers[q.id] == opt
                            Button(
                                onClick = { userAnswers[q.id] = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) GoldDark else Color(0xFF334155)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(opt, color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (step == 2) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(54.dp).align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "CRITICAL ACCURACY DEMONSTRATED!",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF22C55E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "You solved every question perfectly. Playable character ${targetHero.name} has been added to your collection!",
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(54.dp).align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "ACCURACY REQUIREMENT NOT MET",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Let's revise study plan flashcards and try again to unlock this premium combat hero class!",
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                if (step == 1) {
                    val q = challengeQuestions[activeQIndex]
                    Button(
                        onClick = {
                            if (activeQIndex < challengeQuestions.size - 1) {
                                activeQIndex++
                            } else {
                                // Grade
                                var correctCount = 0
                                challengeQuestions.forEach { question ->
                                    if (userAnswers[question.id] == question.correctAnswer) {
                                        correctCount++
                                    }
                                }
                                if (correctCount == challengeQuestions.size) {
                                    viewModel.unlockHero(targetHero.name)
                                    step = 2
                                } else {
                                    step = 3
                                }
                            }
                        },
                        enabled = userAnswers[q.id] != null,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black)
                    ) {
                        Text(if (activeQIndex < challengeQuestions.size - 1) "NEXT" else "SUBMIT", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { challengeHero = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("CLOSE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
fun HeroStatBar(label: String, progress: Float, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 10.sp, color = SlateGrey, fontWeight = FontWeight.Bold)
            Text("${(progress * 100).toInt()}%", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun ArmoryTab(
    viewModel: VLearnViewModel,
    weapons: List<BattleWeapon>,
    activeWeapon: String,
    unlockedWeapons: List<String>,
    userProfile: UserProfile?
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Knife", "Sword", "Hammer", "Bow", "Pistol", "Rifle", "Shotgun")

    // Skin Customization State
    var customizeWeapon by remember { mutableStateOf<BattleWeapon?>(null) }
    val activeWeaponSkin = remember(customizeWeapon, userProfile) {
        val skinsList = userProfile?.selectedSkins?.split(";")?.associate {
            val parts = it.split(":")
            parts[0] to (parts.getOrNull(1) ?: "Default")
        } ?: emptyMap()
        skinsList[customizeWeapon?.name] ?: "Default"
    }

    // AI Challenge dialog state for weapons
    var challengeWeapon by remember { mutableStateOf<BattleWeapon?>(null) }
    var step by remember { mutableStateOf(1) } // 1 = Quiz, 2 = Success, 3 = Fail
    var activeQIndex by remember { mutableStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }

    // Weapon Unlock Questions
    val challengeQuestions = listOf(
        BattleQuestion(1, "Which equation represents Newton's Second Law of Motion?", listOf("F = m * a", "E = m * c²", "v = d / t", "P = W / t"), "F = m * a", "Newton's Second Law establishes that force is mass times acceleration."),
        BattleQuestion(2, "What is the primary function of DNA inside cells?", listOf("Storing genetic instructions", "Synthesizing lipids", "Forming plasma membranes", "Transporting oxygen"), "Storing genetic instructions", "DNA contains all historical chromosome blue-prints for biological synthesis."),
        BattleQuestion(3, "The core components of an atom include:", listOf("Protons, Neutrons, Electrons", "Molecules, Cells, Grains", "Quarks, Waves, Fields", "Tensors, Matrices, Arrays"), "Protons, Neutrons, Electrons", "Atoms consist of protons, neutrons, and orbiting electrons.")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Category filters
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            categories.forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCategory == cat) GoldDark.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (selectedCategory == cat) GoldDark else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedCategory == cat) GoldDark else Color.White)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredWeapons = remember(selectedCategory) {
            if (selectedCategory == "All") weapons else weapons.filter { it.category == selectedCategory }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredWeapons) { weapon ->
                val isUnlocked = unlockedWeapons.contains(weapon.name)
                val isEquipped = activeWeapon == weapon.name

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEquipped) weapon.color.copy(alpha = 0.12f) else Color(0xFF1E293B).copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        width = if (isEquipped) 2.dp else 1.dp,
                        color = if (isEquipped) weapon.color else Color.White.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(weapon.color.copy(alpha = 0.15f))
                                .border(1.dp, weapon.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = weapon.icon,
                                contentDescription = null,
                                tint = weapon.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = weapon.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        weapon.rarity,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when(weapon.rarity) {
                                            "Cosmic" -> Color(0xFFEC4899)
                                            "Legendary" -> GoldDark
                                            "Epic" -> Color(0xFFA855F7)
                                            else -> SlateGrey
                                        }
                                    )
                                }
                            }
                            Text(
                                text = "DMG: ${weapon.damage}  |  SPD: ${weapon.attackSpeed}  |  RNG: ${weapon.range.toInt()}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = weapon.description,
                                fontSize = 10.sp,
                                color = SlateGrey
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Action Buttons
                        Column(horizontalAlignment = Alignment.End) {
                            if (isUnlocked) {
                                if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("EQUIPPED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.selectWeapon(weapon.name) },
                                        colors = ButtonDefaults.buttonColors(containerColor = weapon.color, contentColor = Color.Black),
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("EQUIP", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { customizeWeapon = weapon },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("SKINS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                // Locked - Study Quiz unlock!
                                Button(
                                    onClick = {
                                        challengeWeapon = weapon
                                        step = 1
                                        activeQIndex = 0
                                        userAnswers.clear()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("UNLOCK", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // WEAPON SKINS SELECTOR DIALOG
    if (customizeWeapon != null) {
        val targetWep = customizeWeapon!!
        val availableSkins = listOf("Default", "Ice", "Fire", "Shadow", "Galaxy")
        AlertDialog(
            onDismissRequest = { customizeWeapon = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Text("COSMETIC SKINS: ${targetWep.name.uppercase()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text("Select a cosmetic visual theme to coat projectile particles. (Visual only)", fontSize = 12.sp, color = SlateGrey)
                    Spacer(modifier = Modifier.height(14.dp))
                    availableSkins.forEach { skin ->
                        val isCurrent = activeWeaponSkin == skin
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                .border(1.dp, if (isCurrent) targetWep.color else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectWeaponSkin(targetWep.name, skin) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (skin) {
                                            "Ice" -> Color(0xFF67E8F9)
                                            "Fire" -> Color(0xFFF97316)
                                            "Shadow" -> Color(0xFF8B5CF6)
                                            "Galaxy" -> Color(0xFFEC4899)
                                            else -> targetWep.color
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(skin, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = targetWep.color)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { customizeWeapon = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black)
                ) {
                    Text("DONE", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // WEAPON UNLOCK MODAL
    if (challengeWeapon != null) {
        val targetWeapon = challengeWeapon!!
        AlertDialog(
            onDismissRequest = { challengeWeapon = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Text(
                    text = if (step == 1) "UNLOCK WEAPON: ${targetWeapon.name.uppercase()}" else "AI CHALLENGE RESULT",
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    if (step == 1) {
                        val q = challengeQuestions[activeQIndex]
                        Text(
                            "Demonstrate academic mastery to unlock this combat weapon! Question ${activeQIndex + 1}/3",
                            fontSize = 12.sp,
                            color = SlateGrey
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = q.question,
                                color = Color.White,
                                modifier = Modifier.padding(14.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        q.options.forEach { opt ->
                            val isSelected = userAnswers[q.id] == opt
                            Button(
                                onClick = { userAnswers[q.id] = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) GoldDark else Color(0xFF334155)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(opt, color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (step == 2) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(54.dp).align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "CRITICAL ACCURACY DEMONSTRATED!",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF22C55E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "You solved every question perfectly. Playable weapon ${targetWeapon.name} has been added to your collection!",
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(54.dp).align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "ACCURACY REQUIREMENT NOT MET",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Let's revise study plan flashcards and try again to unlock this premium combat weapon!",
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                if (step == 1) {
                    val q = challengeQuestions[activeQIndex]
                    Button(
                        onClick = {
                            if (activeQIndex < challengeQuestions.size - 1) {
                                activeQIndex++
                            } else {
                                // Grade
                                var correctCount = 0
                                challengeQuestions.forEach { question ->
                                    if (userAnswers[question.id] == question.correctAnswer) {
                                        correctCount++
                                    }
                                }
                                if (correctCount == challengeQuestions.size) {
                                    viewModel.unlockWeapon(targetWeapon.name)
                                    step = 2
                                } else {
                                    step = 3
                                }
                            }
                        },
                        enabled = userAnswers[q.id] != null,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black)
                    ) {
                        Text(if (activeQIndex < challengeQuestions.size - 1) "NEXT" else "SUBMIT", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { challengeWeapon = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("CLOSE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

// --- ACTIVE SIDE-SCROLLING BATTLE SCREEN ---

@Composable
fun ActiveBattleScreen(
    viewModel: VLearnViewModel,
    hero: HeroClass,
    weapon: BattleWeapon,
    realm: RealmConcept,
    battleLevel: Int,
    onQuit: () -> Unit,
    onCompleteLevel: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val sizeMult by viewModel.buttonSizeMultiplier.collectAsStateWithLifecycle()
    val transparency by viewModel.buttonTransparency.collectAsStateWithLifecycle()
    val joyPos by viewModel.joystickPositionPercent.collectAsStateWithLifecycle()
    val atkPos by viewModel.attackButtonPositionPercent.collectAsStateWithLifecycle()
    val sk1Pos by viewModel.skill1PositionPercent.collectAsStateWithLifecycle()
    val sk2Pos by viewModel.skill2PositionPercent.collectAsStateWithLifecycle()
    val sk3Pos by viewModel.skill3PositionPercent.collectAsStateWithLifecycle()
    val ultPos by viewModel.ultPositionPercent.collectAsStateWithLifecycle()
    val jumpPos by viewModel.jumpPositionPercent.collectAsStateWithLifecycle()

    val activeSkinsMap = remember(userProfile) {
        userProfile?.selectedSkins?.split(";")?.associate {
            val parts = it.split(":")
            parts[0] to (parts.getOrNull(1) ?: "Default")
        } ?: emptyMap()
    }
    val activeSkin = activeSkinsMap[weapon.name] ?: "Default"

    // Dynamic bullet colors depending on skin selection
    val projectileBulletColor = remember(activeSkin, weapon) {
        when(activeSkin) {
            "Ice" -> Color(0xFF67E8F9)
            "Fire" -> Color(0xFFF97316)
            "Shadow" -> Color(0xFF8B5CF6)
            "Galaxy" -> Color(0xFFEC4899)
            else -> weapon.bulletColor
        }
    }

    // Simulation loops states
    var playerX by remember { mutableStateOf(100f) }
    var playerY by remember { mutableStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var jumpVelocity by remember { mutableStateOf(0f) }

    val projectiles = remember { mutableStateListOf<GameProjectile>() }
    val enemyProjectiles = remember { mutableStateListOf<GameProjectile>() }

    var playerHp by remember { mutableStateOf(100f) }
    val maxPlayerHp = 100f
    var enemyHp by remember { mutableStateOf(150f) }
    val maxEnemyHp = 150f

    var ultEnergy by remember { mutableStateOf(0f) }
    var gameTicks by remember { mutableStateOf(0) }
    var combatMessage by remember { mutableStateOf("Stand near checkpoints and click INTERACT to clear concept barriers!") }

    val combatParticles = remember { mutableStateListOf<Offset>() }

    var levelSuccess by remember { mutableStateOf(false) }
    var levelFailed by remember { mutableStateOf(false) }

    // Visual combat animations trigger ticks
    var combatTimer by remember { mutableStateOf(0) }
    var skill1Cooldown by remember { mutableStateOf(0) } // ticks (30 ticks = 1 sec)
    var skill2Cooldown by remember { mutableStateOf(0) }
    var skill3Cooldown by remember { mutableStateOf(0) }

    // Map Interactive obstacles
    val gateSolved = remember { mutableStateOf(false) }
    val mapInteractables = remember(realm) {
        mutableStateListOf(
            MapInteractable(1, "Treasure Chest", 260f, 0f, false, realm.questions[0], Icons.Default.Inventory),
            MapInteractable(2, "Ancient Terminal", 520f, 0f, false, realm.questions[1 % realm.questions.size], Icons.Default.Psychology),
            MapInteractable(3, "Magic Gate", 740f, 0f, false, realm.questions[2 % realm.questions.size], Icons.Default.Lock)
        )
    }

    // Active challenge popup
    var activeChallenge by remember { mutableStateOf<MapInteractable?>(null) }
    var aiTeacherMessage by remember { mutableStateOf<String?>(null) }

    // Check interaction range
    val nearbyInteractable = remember(playerX) {
        mapInteractables.find { !it.isSolved && playerX in (it.x - 60f)..(it.x + 60f) }
    }

    // Physics Engine Loop
    LaunchedEffect(activeChallenge == null, aiTeacherMessage == null, !levelSuccess, !levelFailed) {
        while (activeChallenge == null && aiTeacherMessage == null && !levelSuccess && !levelFailed) {
            delay(32) // ~30fps loop
            gameTicks++

            if (combatTimer > 0) combatTimer--
            if (skill1Cooldown > 0) skill1Cooldown--
            if (skill2Cooldown > 0) skill2Cooldown--
            if (skill3Cooldown > 0) skill3Cooldown--

            // Jump gravity mechanics
            if (isJumping) {
                playerY += jumpVelocity
                jumpVelocity -= 1.6f
                if (playerY <= 0f) {
                    playerY = 0f
                    isJumping = false
                }
            }

            // Move bullets
            projectiles.forEach { it.x += it.vx }
            projectiles.removeAll { it.x > 1000f }

            enemyProjectiles.forEach { it.x += it.vx }
            enemyProjectiles.removeAll { it.x < 0f }

            // Collision check
            projectiles.forEach { p ->
                val bossX = 850f
                val bossY = 80f
                if (p.x in (bossX - 70f)..(bossX + 70f)) {
                    enemyHp = (enemyHp - weapon.damage).coerceAtLeast(0f)
                    ultEnergy = (ultEnergy + 5f).coerceAtMost(100f)
                    combatParticles.add(Offset(p.x, 350f))
                    if (enemyHp <= 0f) {
                        levelSuccess = true
                    }
                }
            }
            projectiles.clear()

            // Boss firing bullets
            if (gameTicks % 45 == 0 && enemyHp > 0 && gateSolved.value) {
                enemyProjectiles.add(
                    GameProjectile(
                        id = gameTicks,
                        x = 830f,
                        y = 380f - (0..100).random(),
                        vx = -14f,
                        vy = 0f,
                        color = realm.color
                    )
                )
            }

            // Bullet hits player
            val hitProj = enemyProjectiles.find { it.x in (playerX - 25f)..(playerX + 25f) }
            if (hitProj != null) {
                enemyProjectiles.remove(hitProj)
                playerHp = (playerHp - 15f).coerceAtLeast(0f)
                combatMessage = "Boss projectile impact! Shield energy depleted."
                if (playerHp <= 0f) {
                    levelFailed = true
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
    ) {
        val maxW = maxWidth
        val maxH = maxHeight

        // ==========================================
        // 🎮 GAME BACKGROUND & CANVAS RENDER
        // ==========================================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val groundY = canvasH * 0.82f

            // Cyber background sky glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B132B), Color(0xFF010409))
                )
            )

            // Neon horizontal floor boundary
            drawLine(
                color = realm.color.copy(alpha = 0.6f),
                start = Offset(0f, groundY),
                end = Offset(canvasW, groundY),
                strokeWidth = 4.dp.toPx()
            )

            // Floating background grid lines for cyber feel
            val gridCols = 16
            val spacing = canvasW / gridCols
            for (i in 0..gridCols) {
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(i * spacing, 0f),
                    end = Offset(i * spacing, groundY),
                    strokeWidth = 1f
                )
            }

            // Render Map checkpoints (Holographic energy pillars)
            mapInteractables.forEach { item ->
                val ix = item.x / 1000f * canvasW
                val iy = groundY - 30f
                val activeColor = if (item.isSolved) Color(0xFF10B981) else Color(0xFFEF4444)

                // Energy beam
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(activeColor.copy(alpha = 0.01f), activeColor.copy(alpha = 0.25f))
                    ),
                    topLeft = Offset(ix - 24f, 0f),
                    size = androidx.compose.ui.geometry.Size(48f, groundY)
                )

                // Holographic terminal base
                drawCircle(
                    color = activeColor,
                    radius = 18f,
                    center = Offset(ix, iy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(ix, iy)
                )
            }

            // Render Player Character with customizable styling & Aura animations
            val idleOffset = if (!isJumping) kotlin.math.sin((gameTicks * 0.25f).toDouble()).toFloat() * 6f else 0f
            val combatOffset = if (combatTimer > 0) 18f else 0f
            val characterScaleX = (playerX + combatOffset) / 1000f * canvasW
            val characterScaleY = groundY - (playerY / 500f * canvasH) - 45f + idleOffset

            // Glowing shield dome if Skill 2 (Force Field) is active
            if (skill2Cooldown > 120) {
                drawCircle(
                    color = Color(0xFF22D3EE).copy(alpha = 0.35f),
                    radius = 65f,
                    center = Offset(characterScaleX, characterScaleY)
                )
                drawCircle(
                    color = Color(0xFF22D3EE),
                    radius = 65f,
                    center = Offset(characterScaleX, characterScaleY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }

            // Pulse Aura
            drawCircle(
                color = hero.primaryColor.copy(alpha = 0.15f),
                radius = 50f + kotlin.math.sin((gameTicks * 0.15f).toDouble()).toFloat() * 8f,
                center = Offset(characterScaleX, characterScaleY)
            )

            // Outer Core body
            drawCircle(
                color = hero.primaryColor,
                radius = 26f,
                center = Offset(characterScaleX, characterScaleY)
            )

            // Inner glowing head core
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = Offset(characterScaleX + 10f, characterScaleY - 8f)
            )

            // Blaster Weapon nozzle
            drawLine(
                color = weapon.color,
                start = Offset(characterScaleX, characterScaleY),
                end = Offset(characterScaleX + 32f, characterScaleY + 4f),
                strokeWidth = 6f
            )

            // Render Boss (Cognitive Gate Overlord)
            if (enemyHp > 0) {
                val bossVisualX = canvasW * 0.85f
                val bossVisualY = groundY - 110f

                // Outer energy force shield
                drawCircle(
                    color = realm.color.copy(alpha = 0.1f),
                    radius = 95f + kotlin.math.cos((gameTicks * 0.1f).toDouble()).toFloat() * 12f,
                    center = Offset(bossVisualX, bossVisualY)
                )

                // Hexagonal Boss Armor Plates
                val bossPath = Path().apply {
                    moveTo(bossVisualX, bossVisualY - 90f)
                    lineTo(bossVisualX + 65f, bossVisualY - 20f)
                    lineTo(bossVisualX + 45f, bossVisualY + 70f)
                    lineTo(bossVisualX - 45f, bossVisualY + 70f)
                    lineTo(bossVisualX - 65f, bossVisualY - 20f)
                    close()
                }
                drawPath(path = bossPath, color = realm.color)

                // Glowing central AI core
                drawCircle(
                    color = Color.White,
                    radius = 22f + kotlin.math.sin((gameTicks * 0.2f).toDouble()).toFloat() * 4f,
                    center = Offset(bossVisualX, bossVisualY - 10f)
                )
            }

            // Render Active Projectiles
            projectiles.forEach { p ->
                val px = p.x / 1000f * canvasW
                val py = groundY - (p.y / 500f * canvasH) - 40f
                drawCircle(color = p.color, radius = p.radius + 2f, center = Offset(px, py))
                drawCircle(color = Color.White, radius = p.radius * 0.5f, center = Offset(px, py))
            }

            // Render Enemy incoming cognitive disruption bullets
            enemyProjectiles.forEach { ep ->
                val epx = ep.x / 1000f * canvasW
                val epy = groundY - (ep.y / 500f * canvasH) - 40f
                drawCircle(color = ep.color, radius = ep.radius + 3f, center = Offset(epx, epy))
            }

            // Floating hit sparks/particles
            combatParticles.forEach { pt ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = 18f + sin(gameTicks * 0.4f) * 4f,
                    center = pt
                )
            }
        }

        // ==========================================
        // 👤 TOP FLOATING HEADS-UP DISPLAY (HUD)
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Profile Dashboard (Top-Left)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, hero.primaryColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(hero.primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (hero.name) {
                                "Warrior" -> "⚔"
                                "Sniper" -> "🎯"
                                "Guardian" -> "🛡"
                                "Scholar" -> "🔮"
                                "Mage" -> "✨"
                                else -> "👤"
                            },
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Text(hero.name.uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("HP", color = Color(0xFFEF4444), fontWeight = FontWeight.Black, fontSize = 9.sp)
                            Box(
                                modifier = Modifier
                                    .width(85.dp)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(playerHp / maxPlayerHp)
                                        .background(Color(0xFF10B981))
                                )
                            }
                            Text("${playerHp.toInt()}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Boss Threat Level Indicator (Top-Center)
            if (enemyHp > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, realm.color.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "COGNITIVE GATE OVERLORD",
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            color = realm.color,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(enemyHp / maxEnemyHp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFEF4444), Color(0xFFF59E0B))
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // Technical details, quit & ping (Top-Right)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("⚡ Ping: 18ms", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                IconButton(
                    onClick = onQuit,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quit", tint = Color.White)
                }
            }
        }

        // Floating active message prompt banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Text(
                text = combatMessage.uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // ==========================================
        // 🕹️ FLOATING MOVEMENT CONTROLLERS (BOTTOM LEFT)
        // ==========================================
        Box(
            modifier = Modifier
                .offset(
                    x = (joyPos.x * maxW.value / 2.75f).dp,
                    y = (joyPos.y * maxH.value / 2.75f).dp
                )
                .size(140.dp)
                .alpha(transparency),
            contentAlignment = Alignment.Center
        ) {
            // Tactical D-Pad Ring
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                    .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), CircleShape)
            )

            // D-Pad Left
            IconButton(
                onClick = { playerX = (playerX - 45f).coerceAtLeast(80f) },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
                    .size((42 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                    .border(1.dp, Color(0xFF38BDF8), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Move Left", tint = Color.White)
            }

            // D-Pad Right
            IconButton(
                onClick = {
                    val limit = if (gateSolved.value) 860f else 720f
                    playerX = (playerX + 45f).coerceAtMost(limit)
                    if (!gateSolved.value && playerX >= 710f) {
                        combatMessage = "Magic Gate Locked! Click INTERACT on checkpoint to complete quiz!"
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
                    .size((42 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                    .border(1.dp, Color(0xFF38BDF8), CircleShape)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Move Right", tint = Color.White)
            }

            // Floating Sprint/Boost Action at center
            Box(
                modifier = Modifier
                    .size((40 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.9f))
                    .border(1.dp, Color.White, CircleShape)
                    .clickable {
                        // Trigger Sprint Boost speed multiplier
                        val limit = if (gateSolved.value) 860f else 720f
                        playerX = (playerX + 70f).coerceAtMost(limit)
                        combatMessage = "Thruster Sprint Boost Engaged!"
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsRun, contentDescription = "Sprint", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // ==========================================
        // 🔮 FLOATING TACTICAL ACTION WHEEL (BOTTOM RIGHT)
        // ==========================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .fillMaxHeight(0.6f)
                .fillMaxWidth(0.5f)
                .alpha(transparency)
        ) {
            // Interactive check point alert
            if (nearbyInteractable != null) {
                val item = nearbyInteractable!!
                Button(
                    onClick = { activeChallenge = item },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer(scaleX = sizeMult, scaleY = sizeMult)
                        .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("INTERACT CORE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                }
            }

            // Normal Attack (Weapon Shoot)
            Box(
                modifier = Modifier
                    .offset(
                        x = (atkPos.x * maxW.value / 4.5f).dp,
                        y = (atkPos.y * maxH.value / 4.5f).dp
                    )
                    .size((64 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(weapon.color)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable {
                        combatTimer = 6
                        projectiles.add(
                            GameProjectile(
                                id = gameTicks + 1000,
                                x = playerX + 40f,
                                y = playerY + 30f,
                                vx = 30f,
                                vy = 0f,
                                color = projectileBulletColor
                            )
                        )
                        combatMessage = "${hero.name} fired ${weapon.name} blaster!"
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = "Shoot Weapon", tint = Color.Black, modifier = Modifier.size(28.dp))
            }

            // Skill 1: Disruption Shock EMP Spread
            Box(
                modifier = Modifier
                    .offset(
                        x = (sk1Pos.x * maxW.value / 4.5f).dp,
                        y = (sk1Pos.y * maxH.value / 4.5f).dp
                    )
                    .size((48 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(if (skill1Cooldown <= 0) Color(0xFFA855F7) else Color.DarkGray)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable(enabled = skill1Cooldown <= 0) {
                        skill1Cooldown = 120 // 4 seconds (120 ticks)
                        projectiles.add(GameProjectile(gameTicks + 3000, playerX + 45f, playerY + 30f, 28f, 0f, projectileBulletColor))
                        projectiles.add(GameProjectile(gameTicks + 3001, playerX + 45f, playerY + 30f, 26f, 5f, projectileBulletColor))
                        projectiles.add(GameProjectile(gameTicks + 3002, playerX + 45f, playerY + 30f, 26f, -5f, projectileBulletColor))
                        enemyHp = (enemyHp - 18f).coerceAtLeast(0f)
                        combatMessage = "EMP Shockwave Discharged! Boss armor ruptured."
                        if (enemyHp <= 0f) { levelSuccess = true }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (skill1Cooldown > 0) {
                    Text("${(skill1Cooldown / 30f).toInt()}s", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                } else {
                    Icon(Icons.Default.OfflineBolt, contentDescription = "Skill 1", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Skill 2: Tech Shield Bubble
            Box(
                modifier = Modifier
                    .offset(
                        x = (sk2Pos.x * maxW.value / 4.5f).dp,
                        y = (sk2Pos.y * maxH.value / 4.5f).dp
                    )
                    .size((48 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(if (skill2Cooldown <= 0) Color(0xFF06B6D4) else Color.DarkGray)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable(enabled = skill2Cooldown <= 0) {
                        skill2Cooldown = 180 // 6 seconds
                        playerHp = (playerHp + 15f).coerceAtMost(maxPlayerHp)
                        combatMessage = "Aegis Tech Shield Activated! temporary invulnerability core deployed."
                    },
                contentAlignment = Alignment.Center
            ) {
                if (skill2Cooldown > 0) {
                    Text("${(skill2Cooldown / 30f).toInt()}s", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                } else {
                    Icon(Icons.Default.Shield, contentDescription = "Skill 2", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Skill 3: Nano Heal Potion
            Box(
                modifier = Modifier
                    .offset(
                        x = (sk3Pos.x * maxW.value / 4.5f).dp,
                        y = (sk3Pos.y * maxH.value / 4.5f).dp
                    )
                    .size((48 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(if (skill3Cooldown <= 0) Color(0xFF10B981) else Color.DarkGray)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable(enabled = skill3Cooldown <= 0) {
                        skill3Cooldown = 360 // 12 seconds
                        playerHp = (playerHp + 35f).coerceAtMost(maxPlayerHp)
                        combatMessage = "Nano-Heal Integration Successful! Integrated nanites repairing HP."
                    },
                contentAlignment = Alignment.Center
            ) {
                if (skill3Cooldown > 0) {
                    Text("${(skill3Cooldown / 30f).toInt()}s", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                } else {
                    Icon(Icons.Default.Healing, contentDescription = "Skill 3", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Ultimate Burst (Aether Storm)
            Box(
                modifier = Modifier
                    .offset(
                        x = (ultPos.x * maxW.value / 4.5f).dp,
                        y = (ultPos.y * maxH.value / 4.5f).dp
                    )
                    .size((52 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(if (ultEnergy >= 100f) Color(0xFFF59E0B) else Color.DarkGray)
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable(enabled = ultEnergy >= 100f) {
                        ultEnergy = 0f
                        enemyHp = (enemyHp - 70f).coerceAtLeast(0f)
                        combatMessage = "${hero.name} released ULTIMATE Apocalypse!"
                        repeat(8) {
                            combatParticles.add(Offset((400..850).random().toFloat(), (120..420).random().toFloat()))
                        }
                        if (enemyHp <= 0f) {
                            levelSuccess = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Ultimate Skill",
                    tint = if (ultEnergy >= 100f) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Jump Rocket Leap
            Box(
                modifier = Modifier
                    .offset(
                        x = (jumpPos.x * maxW.value / 4.5f).dp,
                        y = (jumpPos.y * maxH.value / 4.5f).dp
                    )
                    .size((46 * sizeMult).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF475569))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        if (!isJumping) {
                            isJumping = true
                            jumpVelocity = 23f
                            combatMessage = "Gravity leap engaged!"
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Jump", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }

    // INTERACTIVE MAP CHECKPOINT QUIZ DIALOG
    if (activeChallenge != null) {
        val target = activeChallenge!!
        val q = target.question
        var selectedOpt by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF0F172A),
            tonalElevation = 12.dp,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(target.icon, contentDescription = null, tint = realm.color)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(target.name.uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column {
                    Text("Resolve this concept question to bypass the map barrier and empower your hero stats!", color = SlateGrey, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(q.question, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    q.options.forEach { opt ->
                        val isSel = selectedOpt == opt
                        Button(
                            onClick = { selectedOpt = opt },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSel) realm.color else Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(opt, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedOpt == q.correctAnswer) {
                            target.isSolved = true
                            if (target.name == "Magic Gate") {
                                gateSolved.value = true
                                combatMessage = "Magic Gate unlocked! Proceed right to challenge the boss!"
                            } else if (target.name == "Treasure Chest") {
                                viewModel.awardPlayer(100, 0, 50)
                                ultEnergy = 100f
                                combatMessage = "Treasure opened! Awarded 100 Coins & charged Ultimate!"
                            } else {
                                enemyHp = (enemyHp - 45f).coerceAtLeast(0f)
                                ultEnergy = 100f
                                combatMessage = "Ancient Terminal solved! Forcefield offline, Boss de-shielded!"
                            }
                            activeChallenge = null
                        } else {
                            aiTeacherMessage = q.explanation
                            playerHp = (playerHp - 20f).coerceAtLeast(0f)
                            activeChallenge = null
                            if (playerHp <= 0f) {
                                levelFailed = true
                            }
                        }
                    },
                    enabled = selectedOpt != null,
                    colors = ButtonDefaults.buttonColors(containerColor = realm.color, contentColor = Color.White)
                ) {
                    Text("SUBMIT RESPONSE", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // CORRECTIVE AI TUTOR DIALOG
    if (aiTeacherMessage != null) {
        AlertDialog(
            onDismissRequest = { aiTeacherMessage = null },
            containerColor = Color(0xFF0F172A),
            icon = { Icon(Icons.Default.Psychology, contentDescription = null, tint = GoldDark, modifier = Modifier.size(36.dp)) },
            title = { Text("AI TEACHER LESSON", fontWeight = FontWeight.Black, color = Color.White) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Barrier check incorrect! Let's master this concept to proceed safely.", color = SlateGrey, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = aiTeacherMessage!!,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { aiTeacherMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black)
                ) {
                    Text("RESUME EXPLORATION", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // LEVEL SUCCESS VICTORY
    if (levelSuccess) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF0F172A),
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldDark, modifier = Modifier.size(56.dp)) },
            title = { Text("REALM VICTORY!", fontWeight = FontWeight.Black, color = Color.White, fontSize = 24.sp) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You successfully defeated ${realm.bossName} and guarded the gates of ${realm.worldName}!", color = SlateGrey, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        RewardBadge("+100 XP", Icons.Default.Bolt, Color(0xFF22C55E))
                        RewardBadge("+25 Coins", Icons.Default.Paid, Color(0xFFFFB300))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onCompleteLevel,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black)
                ) {
                    Text("CONTINUE QUEST", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // DEFEAT RE-TRIAL
    if (levelFailed) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF0F172A),
            title = { Text("DEFEAT!", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your shields collapsed! The ${realm.bossName} holds the concepts hostage.", color = SlateGrey, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tip: Revise study plan flashcards and ask the AI Tutor for custom hints before challenging the boss again!", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onQuit, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)), modifier = Modifier.weight(1f)) {
                        Text("LOBBY", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            playerHp = 100f
                            enemyHp = 150f
                            gateSolved.value = false
                            mapInteractables.forEach { it.isSolved = false }
                            playerX = 100f
                            levelFailed = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RETRY", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        )
    }
}

@Composable
fun RewardBadge(label: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
    }
}

// --- MILESTONE EXAM SCREEN (EVERY 10 LEVELS) ---

@Composable
fun AssessmentScreen(
    viewModel: VLearnViewModel,
    realm: RealmConcept,
    onFinished: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Intro, 2 = Quiz Active, 3 = Score Reveal, 4 = Explanation, 5 = Reward Unlocked Screen
    var score by remember { mutableStateOf(0) }
    var currentQIndex by remember { mutableStateOf(0) }
    val answers = remember { mutableStateMapOf<Int, String>() }

    val quizQuestions = realm.questions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (step) {
            1 -> {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldDark, modifier = Modifier.size(72.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("AI MILESTONE EXAM", fontWeight = FontWeight.Black, fontSize = 26.sp, color = Color.White, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Every 10 levels, the AI compiles a milestone examination directly from the ${realm.name} syllabus. You must score 80% or higher to unlock the next world tier and claim Legendary rewards!", color = SlateGrey, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { step = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("START EXAMINATION", fontWeight = FontWeight.Black)
                }
            }

            2 -> {
                val q = quizQuestions[currentQIndex]
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("CONCEPT TEST: ${realm.name.uppercase()}", color = realm.color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Q ${currentQIndex + 1}/${quizQuestions.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (currentQIndex + 1).toFloat() / quizQuestions.size },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = realm.color,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                    Text(q.question, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                q.options.forEach { opt ->
                    val isSelected = answers[q.id] == opt
                    Button(
                        onClick = { answers[q.id] = opt },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) realm.color else Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(opt, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (currentQIndex < quizQuestions.size - 1) {
                        Button(onClick = { currentQIndex++ }, enabled = answers[q.id] != null, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))) {
                            Text("NEXT QUESTION", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = {
                                var hits = 0
                                quizQuestions.forEach { question ->
                                    if (answers[question.id] == question.correctAnswer) hits++
                                }
                                score = ((hits.toFloat() / quizQuestions.size) * 100).toInt()
                                step = 3
                            },
                            enabled = answers[q.id] != null,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black)
                        ) {
                            Text("SUBMIT EXAM", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            3 -> {
                val passed = score >= 80
                Icon(
                    imageVector = if (passed) Icons.Default.SentimentSatisfiedAlt else Icons.Default.SentimentVeryDissatisfied,
                    contentDescription = null,
                    tint = if (passed) Color(0xFF22C55E) else Color(0xFFEF4444),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("EXAM SCORE: $score%", fontWeight = FontWeight.Black, fontSize = 28.sp, color = if (passed) Color(0xFF22C55E) else Color(0xFFEF4444))
                Text(if (passed) "EXCELLENT WORK! LEVEL COMPLETED!" else "REQUIRES REVISION", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateGrey)
                Spacer(modifier = Modifier.height(16.dp))

                if (passed) {
                    Text("Congratulations! You have demonstrated high mastery. Passing unlocks a random Legendary Hero, Weapon, and Skin in your collection!", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RewardBadge("Legendary Unlocks", Icons.Default.Square, Color(0xFFFFB300))
                        RewardBadge("2x Multiplier", Icons.Default.TrendingUp, Color(0xFFA855F7))
                    }
                } else {
                    Text("Score below 80%. Let's review incorrect answers and solutions, then retake the exam!", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!passed) {
                        Button(onClick = { step = 4 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)), modifier = Modifier.weight(1f)) {
                            Text("REVIEW ANSWERS", fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = {
                            if (passed) {
                                // Grant automatic epic unlocks on passing milestones!
                                viewModel.unlockHero("Paladin")
                                viewModel.unlockWeapon("Nebula Edge")
                                viewModel.unlockWeaponSkin("Nebula Edge", "Galaxy")
                                step = 5
                            } else {
                                currentQIndex = 0
                                answers.clear()
                                step = 2
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (passed) "PROCEED REWARDS" else "RETAKE EXAM", fontWeight = FontWeight.Bold)
                    }
                }
            }

            4 -> {
                Text("AI SOLUTIONS & EXPLORER", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(quizQuestions) { question ->
                        val userAns = answers[question.id]
                        val isCorrect = userAns == question.correctAnswer
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, if (isCorrect) Color(0xFF22C55E) else Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(question.question, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your Answer: $userAns", fontSize = 11.sp, color = if (isCorrect) Color(0xFF22C55E) else Color(0xFFEF4444))
                                Text("Correct Answer: ${question.correctAnswer}", fontSize = 11.sp, color = Color(0xFF22C55E))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Explanation: ${question.explanation}", fontSize = 11.sp, color = SlateGrey, lineHeight = 15.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { step = 3 }, colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black), modifier = Modifier.fillMaxWidth()) {
                    Text("BACK TO RESULT", fontWeight = FontWeight.Bold)
                }
            }

            5 -> {
                // Milestone victory award splash screen!
                Icon(Icons.Default.Celebration, contentDescription = null, tint = GoldDark, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("MILESTONE REWARDS GRANTED!", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFFFFB300))
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎁 NEW LEGENDARY HERO UNLOCKED: PALADIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("⚔️ NEW LEGENDARY SWORD UNLOCKED: NEBULA EDGE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("🌈 NEW COSMETIC SKIN UNLOCKED: GALAXY (NEBULA EDGE)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.advanceBattleLevel()
                        onFinished()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLAIM AND GO HOME", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
