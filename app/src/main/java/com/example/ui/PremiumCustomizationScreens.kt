package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.GoldDark
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.SlateGrey
import com.example.ui.theme.VioletDark
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// 🎨 PREMIUM CUSTOMIZATION SCREEN
// ==========================================

@Composable
fun PremiumCustomizationScreen(
    viewModel: VLearnViewModel,
    onBack: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Character") } // Character, Weapon, Profile

    val selectedHero by viewModel.selectedHeroClass.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))))
    ) {
        // Holographic cyber background grid
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.12f)) {
            val rows = 30
            val cols = 30
            val rh = size.height / rows
            val cw = size.width / cols
            for (i in 0..rows) {
                drawLine(Color(0xFF38BDF8), Offset(0f, i * rh), Offset(size.width, i * rh), 1.5f)
            }
            for (j in 0..cols) {
                drawLine(Color(0xFF38BDF8), Offset(j * cw, 0f), Offset(j * cw, size.height), 1.5f)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    "ARMORY & HERO LAB",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.SansSerif
                )

                // Fancy level capsule
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "MY SQUAD",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Character", "Weapon", "Profile").forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .let {
                                if (isSelected) {
                                    it.background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF3B82F6),
                                                Color(0xFF8B5CF6)
                                            )
                                        )
                                    )
                                } else {
                                    it.background(Color.Transparent)
                                }
                            }
                            .clickable { activeTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.uppercase(),
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTab) {
                "Character" -> CharacterCustomizationTab(viewModel, selectedHero)
                "Weapon" -> WeaponCustomizationTab(viewModel)
                "Profile" -> ProfileCustomizationTab(viewModel)
            }
        }
    }
}

// ==========================================
// 👤 CHARACTER CUSTOMIZATION TAB
// ==========================================

@Composable
fun CharacterCustomizationTab(viewModel: VLearnViewModel, heroClass: String) {
    val hairStyle by viewModel.currentHairstyle.collectAsStateWithLifecycle()
    val hairColor by viewModel.currentHairColor.collectAsStateWithLifecycle()
    val eyes by viewModel.currentEyes.collectAsStateWithLifecycle()
    val face by viewModel.currentFace.collectAsStateWithLifecycle()
    val outfit by viewModel.currentOutfit.collectAsStateWithLifecycle()
    val armor by viewModel.currentArmor.collectAsStateWithLifecycle()
    val cape by viewModel.currentCape.collectAsStateWithLifecycle()
    val backpack by viewModel.currentBackpack.collectAsStateWithLifecycle()
    val gloves by viewModel.currentGloves.collectAsStateWithLifecycle()
    val boots by viewModel.currentBoots.collectAsStateWithLifecycle()
    val aura by viewModel.currentAura.collectAsStateWithLifecycle()

    var showSuccessAnim by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Live Character Portrait Presentation Panel (Left Side)
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                .border(2.dp, Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFF8B5CF6))), RoundedCornerShape(20.dp))
        ) {
            // Live Particle aura background
            val transition = rememberInfiniteTransition(label = "aura")
            val auraTick by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
                label = "aurarot"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val auraColor = when (aura) {
                    "Super Saiyan Golden Spark" -> Color(0xFFF59E0B)
                    "Cosmic Purple Nebula" -> Color(0xFFA855F7)
                    "Digital Green Matrix" -> Color(0xFF10B981)
                    else -> Color(0xFF38BDF8)
                }

                // Render customized background glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(auraColor.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.45f),
                        radius = size.width * 0.65f
                    )
                )

                // Render flowing aura sparks
                val numParticles = 12
                for (i in 0 until numParticles) {
                    val angle = (i * (360f / numParticles) + auraTick) * (Math.PI / 180f)
                    val r = size.width * 0.28f + sin(auraTick * 0.05f + i) * 20f
                    val px = size.width / 2f + cos(angle).toFloat() * r
                    val py = size.height * 0.45f + sin(angle).toFloat() * r
                    drawCircle(auraColor.copy(alpha = 0.45f), 12f + sin(auraTick * 0.1f) * 4f, Offset(px, py))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top status and Class
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        heroClass.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "STATUS: READY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF10B981)
                    )
                }

                // Holographic character drawing using a beautiful combination of nested elements
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (heroClass) {
                                "Warrior" -> "⚔"
                                "Sniper" -> "🎯"
                                "Guardian" -> "🛡"
                                "Scholar" -> "🔮"
                                "Mage" -> "✨"
                                else -> "👤"
                            },
                            fontSize = 62.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "COSMIC AURA ON",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                // Summary of active visual styling
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "HAIR: $hairStyle (${if (hairColor == Color(0xFF38BDF8)) "Cyan" else "Neon Green"})",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "EYES: $eyes",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "SUIT: $outfit",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "AURA: $aura",
                            fontSize = 10.sp,
                            color = GoldDark,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Button(
                    onClick = { showSuccessAnim = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("SAVE COSMETICS", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }

            if (showSuccessAnim) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable { showSuccessAnim = false },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✨ SYNC SUCCESSFUL ✨", color = GoldDark, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text("Cosmetics updated securely on cloud nodes", color = Color.White, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("[TAP TO CLOSE]", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }

        // Customizer Selection Panel (Right Side)
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CustomizationGroup("HAIRSTYLE & COIF") {
                listOf("Cyber Bob", "Spiky Shonen", "Long Flowing", "Neon Ponytail").forEach { style ->
                    val isSel = hairStyle == style
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFF3B82F6) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentHairstyle.value = style }
                            .padding(10.dp)
                    ) {
                        Text(style, color = if (isSel) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            CustomizationGroup("HAIR COLOR MATRIX") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Color(0xFF38BDF8) to "Neon Blue",
                        Color(0xFF10B981) to "Cyber Green",
                        Color(0xFFEF4444) to "Crimson",
                        Color(0xFFEC4899) to "Sakura Pink"
                    ).forEach { (color, name) ->
                        val isSel = hairColor == color
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, if (isSel) Color.White else Color.Transparent, CircleShape)
                                .clickable { viewModel.currentHairColor.value = color }
                        )
                    }
                }
            }

            CustomizationGroup("EYE SPECULUM") {
                listOf("Cosmic Glow", "Sharinggan Red", "Cat-Eye Green", "Cybernetic Blue").forEach { opt ->
                    val isSel = eyes == opt
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF8B5CF6).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFF8B5CF6) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentEyes.value = opt }
                            .padding(10.dp)
                    ) {
                        Text(opt, color = if (isSel) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            CustomizationGroup("SUIT & ARMOR INTEGRATION") {
                listOf("Mecha Battle-Suit", "Aether Cloak", "Shinobi Gear", "Casual Scholar").forEach { opt ->
                    val isSel = outfit == opt
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFFF59E0B) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentOutfit.value = opt }
                            .padding(10.dp)
                    ) {
                        Text(opt, color = if (isSel) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            CustomizationGroup("AURA FIELD GENERATOR") {
                listOf("Cosmic Purple Nebula", "Super Saiyan Golden Spark", "Digital Green Matrix", "None").forEach { opt ->
                    val isSel = aura == opt
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF10B981).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFF10B981) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentAura.value = opt }
                            .padding(10.dp)
                    ) {
                        Text(opt, color = if (isSel) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomizationGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 1.2.sp
        )
        content()
    }
}

// ==========================================
// ⚔ WEAPON CUSTOMIZATION TAB
// ==========================================

@Composable
fun WeaponCustomizationTab(viewModel: VLearnViewModel) {
    val selectedWeapon by viewModel.selectedWeapon.collectAsStateWithLifecycle()
    val charm by viewModel.currentWeaponCharm.collectAsStateWithLifecycle()
    val effect by viewModel.currentKillEffect.collectAsStateWithLifecycle()
    val sound by viewModel.currentSoundPack.collectAsStateWithLifecycle()
    val inspect by viewModel.currentInspectAnim.collectAsStateWithLifecycle()

    var activeSkinIndex by remember { mutableStateOf(0) }
    val skins = listOf("Default Slate", "Gold Plated", "Neon Cyber", "Obsidian Shadow", "Cosmic Stardust")

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Weapon live presentation 3D rotation mockup (Left)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            val rotationTransition = rememberInfiniteTransition(label = "weaponrotate")
            val weaponAngle by rotationTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
                label = "rot"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val skinColor = when (activeSkinIndex) {
                    1 -> Color(0xFFFBBF24) // Gold
                    2 -> Color(0xFFEC4899) // Neon pink
                    3 -> Color(0xFF7C3AED) // Obsidian
                    4 -> Color(0xFF06B6D4) // Cosmic
                    else -> Color(0xFF94A3B8) // Default
                }

                // Render weapon line outline spinning
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h * 0.45f
                val length = w * 0.35f
                val radians = (weaponAngle * Math.PI / 180f)

                val xEnd = cx + cos(radians).toFloat() * length
                val yEnd = cy + sin(radians).toFloat() * length
                val xStart = cx - cos(radians).toFloat() * (length * 0.2f)
                val yStart = cy - sin(radians).toFloat() * (length * 0.2f)

                // Backglow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(skinColor.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = length * 1.5f
                    )
                )

                // Weapon Body bar
                drawLine(
                    color = skinColor,
                    start = Offset(xStart, yStart),
                    end = Offset(xEnd, yEnd),
                    strokeWidth = 14f
                )

                // Weapon Scope/Crossbar
                val perpRad = radians + Math.PI / 2
                val crossLength = 30f
                drawLine(
                    color = skinColor,
                    start = Offset(cx - cos(perpRad).toFloat() * crossLength, cy - sin(perpRad).toFloat() * crossLength),
                    end = Offset(cx + cos(perpRad).toFloat() * crossLength, cy + sin(perpRad).toFloat() * crossLength),
                    strokeWidth = 6f
                )

                // Energy glow tip
                drawCircle(Color.White, 8f, Offset(xEnd, yEnd))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    selectedWeapon.uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "ACTIVE SKIN: ${skins[activeSkinIndex].uppercase()}",
                        color = GoldDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Button(
                    onClick = { /* Play inspect animation */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TRIGGER INSPECTION", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Weapon Customizer Option lists (Right)
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CustomizationGroup("5 COSMETIC SKIN CHANNELS") {
                skins.forEachIndexed { idx, skin ->
                    val isSel = activeSkinIndex == idx
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF10B981).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFF10B981) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeSkinIndex = idx }
                            .padding(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(skin, color = if (isSel) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            if (isSel) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            CustomizationGroup("WEAPON CHARMS") {
                listOf("Gemini AI Brain", "Mini Golden Medal", "Pixel Heart", "None").forEach { ch ->
                    val isSel = charm == ch
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF38BDF8).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFF38BDF8) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentWeaponCharm.value = ch }
                            .padding(10.dp)
                    ) {
                        Text(ch, color = if (isSel) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            CustomizationGroup("MUTATOR AUDIO PACKS") {
                listOf("Sleek Synthwave Laser", "8-Bit Retro Game", "Heavy Artillery", "Traditional Pew-Pew").forEach { sd ->
                    val isSel = sound == sd
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFFA855F7).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFFA855F7) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentSoundPack.value = sd }
                            .padding(10.dp)
                    ) {
                        Text(sd, color = if (isSel) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 🏆 PROFILE / COSMETIC BANNER TAB
// ==========================================

@Composable
fun ProfileCustomizationTab(viewModel: VLearnViewModel) {
    val emote by viewModel.currentEmote.collectAsStateWithLifecycle()
    val nameCard by viewModel.currentNameCard.collectAsStateWithLifecycle()
    val banner by viewModel.currentProfileBanner.collectAsStateWithLifecycle()
    val frame by viewModel.currentAvatarFrame.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mock Name Card & Profile Banner Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.horizontalGradient(
                            when (banner) {
                                "Cosmic Abyss" -> listOf(Color(0xFF0F172A), Color(0xFF7C3AED))
                                "Sunset Horizon" -> listOf(Color(0xFFEA580C), Color(0xFFD97706))
                                "Emerald Jungle" -> listOf(Color(0xFF047857), Color(0xFF10B981))
                                else -> listOf(Color(0xFF1E293B), Color(0xFF334155))
                            }
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar Frame Preview
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(
                                width = 3.dp,
                                color = when (frame) {
                                    "Neon Cyan Border" -> Color(0xFF22D3EE)
                                    "Flaming Golden Crest" -> Color(0xFFFBBF24)
                                    "Obsidian Spikes" -> Color(0xFF8B5CF6)
                                    else -> Color.White
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎓", fontSize = 32.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "ELITE SCHOLAR ALEX",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            "TITLE: $nameCard",
                            color = GoldDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "ACTIVE EMOTE: $emote",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomizationGroup("ACTIVE EMOTE CONFIG") {
                    listOf("GG WP", "Salt Splash", "Flex Muscles", "Tutor Thumbs Up").forEach { em ->
                        val isSel = emote == em
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF22D3EE).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFF22D3EE) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentEmote.value = em }
                            .padding(10.dp)
                        ) {
                            Text(em, color = if (isSel) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomizationGroup("AVATAR BORDERS") {
                    listOf("Neon Cyan Border", "Flaming Golden Crest", "Obsidian Spikes").forEach { fr ->
                        val isSel = frame == fr
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFFFBBF24).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isSel) Color(0xFFFBBF24) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentAvatarFrame.value = fr }
                            .padding(10.dp)
                        ) {
                            Text(fr, color = if (isSel) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ⚙ PREMIUM TECHNICAL SETTINGS SCREEN
// ==========================================

@Composable
fun PremiumSettingsScreen(
    viewModel: VLearnViewModel,
    onBack: () -> Unit
) {
    var activeCategory by remember { mutableStateOf("Controls") } // Controls, Graphics, Audio, Accessibility

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    "CONVENIENCE & HARDWARE",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )

                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sidebar Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Controls", "Graphics", "Audio", "Accessibility").forEach { cat ->
                    val isSel = activeCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF38BDF8) else Color.Transparent)
                            .clickable { activeCategory = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            cat.uppercase(),
                            color = if (isSel) Color(0xFF0F172A) else Color.Gray,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details based on selected category
            Box(modifier = Modifier.weight(1f)) {
                when (activeCategory) {
                    "Controls" -> SettingsControlsTab(viewModel)
                    "Graphics" -> SettingsGraphicsTab(viewModel)
                    "Audio" -> SettingsAudioTab(viewModel)
                    "Accessibility" -> SettingsAccessibilityTab(viewModel)
                }
            }
        }
    }
}

// ==========================================
// ⚙ SETTINGS - CONTROLS TAB
// ==========================================

@Composable
fun SettingsControlsTab(viewModel: VLearnViewModel) {
    val sizeMult by viewModel.buttonSizeMultiplier.collectAsStateWithLifecycle()
    val transparency by viewModel.buttonTransparency.collectAsStateWithLifecycle()
    val aimAssist by viewModel.aimAssistEnabled.collectAsStateWithLifecycle()
    val gyro by viewModel.gyroEnabled.collectAsStateWithLifecycle()
    val autoPickup by viewModel.autoPickupEnabled.collectAsStateWithLifecycle()

    var showDragCustomizer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { showDragCustomizer = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("INTERACTIVE DRAG-AND-DROP HUD EDITOR", fontWeight = FontWeight.Black)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("HUD GENERAL SCALINGS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)

                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Button Size Multiplier", color = Color.Gray, fontSize = 11.sp)
                        Text(String.format("%.2fx", sizeMult), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = sizeMult,
                        onValueChange = { viewModel.buttonSizeMultiplier.value = it },
                        valueRange = 0.7f..1.5f
                    )
                }

                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Button Opacity / Transparency", color = Color.Gray, fontSize = 11.sp)
                        Text(String.format("%.0f%%", transparency * 100f), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = transparency,
                        onValueChange = { viewModel.buttonTransparency.value = it },
                        valueRange = 0.3f..1.0f
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("TACTICAL INTELLIGENCE", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)

                ControlToggleRow("Smart Aim Assist (Recommended)", aimAssist) { viewModel.aimAssistEnabled.value = it }
                ControlToggleRow("Gyroscope Tilt Navigation", gyro) { viewModel.gyroEnabled.value = it }
                ControlToggleRow("Auto Pickup Learning Supplies", autoPickup) { viewModel.autoPickupEnabled.value = it }
            }
        }

        if (showDragCustomizer) {
            DragAndDropLayoutEditor(viewModel, onDismiss = { showDragCustomizer = false })
        }
    }
}

@Composable
fun ControlToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 12.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ==========================================
// 🎯 INTERACTIVE DRAG-AND-DROP HUD EDITOR
// ==========================================

@Composable
fun DragAndDropLayoutEditor(
    viewModel: VLearnViewModel,
    onDismiss: () -> Unit
) {
    // Current positions from ViewModel
    val joyPos by viewModel.joystickPositionPercent.collectAsStateWithLifecycle()
    val atkPos by viewModel.attackButtonPositionPercent.collectAsStateWithLifecycle()
    val sk1Pos by viewModel.skill1PositionPercent.collectAsStateWithLifecycle()
    val sk2Pos by viewModel.skill2PositionPercent.collectAsStateWithLifecycle()
    val sk3Pos by viewModel.skill3PositionPercent.collectAsStateWithLifecycle()
    val ultPos by viewModel.ultPositionPercent.collectAsStateWithLifecycle()
    val jumpPos by viewModel.jumpPositionPercent.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DRAG BUTTONS TO CUSTOMIZE POSITIONS", fontWeight = FontWeight.Black, color = GoldDark, fontSize = 12.sp)
                Button(
                    onClick = {
                        // Reset defaults
                        viewModel.joystickPositionPercent.value = android.graphics.PointF(0.18f, 0.75f)
                        viewModel.attackButtonPositionPercent.value = android.graphics.PointF(0.85f, 0.78f)
                        viewModel.skill1PositionPercent.value = android.graphics.PointF(0.72f, 0.88f)
                        viewModel.skill2PositionPercent.value = android.graphics.PointF(0.76f, 0.72f)
                        viewModel.skill3PositionPercent.value = android.graphics.PointF(0.88f, 0.60f)
                        viewModel.ultPositionPercent.value = android.graphics.PointF(0.92f, 0.44f)
                        viewModel.jumpPositionPercent.value = android.graphics.PointF(0.94f, 0.90f)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("RESET DEFAULTS", fontSize = 10.sp)
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("SAVE & EXIT", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smartphone viewport mockup
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            ) {
                val cw = constraints.maxWidth.toFloat()
                val ch = constraints.maxHeight.toFloat()

                // Background watermark grid
                Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
                    val steps = 15
                    for (i in 0..steps) {
                        drawLine(Color.White, Offset(0f, i * (size.height / steps)), Offset(size.width, i * (size.height / steps)))
                        drawLine(Color.White, Offset(i * (size.width / steps), 0f), Offset(i * (size.width / steps), size.height))
                    }
                }

                // Render Left Joystick drag spot
                Box(
                    modifier = Modifier
                        .offset(
                            x = (joyPos.x * cw / 2.75f).dp,
                            y = (joyPos.y * ch / 2.75f).dp
                        )
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8).copy(alpha = 0.25f))
                        .border(2.dp, Color(0xFF38BDF8), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = ((joyPos.x * cw + dragAmount.x) / cw).coerceIn(0.05f, 0.95f)
                                val newY = ((joyPos.y * ch + dragAmount.y) / ch).coerceIn(0.05f, 0.95f)
                                viewModel.joystickPositionPercent.value = android.graphics.PointF(newX, newY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("JOYSTICK", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Render Attack Button drag spot
                Box(
                    modifier = Modifier
                        .offset(
                            x = (atkPos.x * cw / 2.75f).dp,
                            y = (atkPos.y * ch / 2.75f).dp
                        )
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.35f))
                        .border(2.dp, Color(0xFFEF4444), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = ((atkPos.x * cw + dragAmount.x) / cw).coerceIn(0.05f, 0.95f)
                                val newY = ((atkPos.y * ch + dragAmount.y) / ch).coerceIn(0.05f, 0.95f)
                                viewModel.attackButtonPositionPercent.value = android.graphics.PointF(newX, newY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("ATTACK", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Render Skill 1
                Box(
                    modifier = Modifier
                        .offset(
                            x = (sk1Pos.x * cw / 2.75f).dp,
                            y = (sk1Pos.y * ch / 2.75f).dp
                        )
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFA855F7).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFFA855F7), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = ((sk1Pos.x * cw + dragAmount.x) / cw).coerceIn(0.05f, 0.95f)
                                val newY = ((sk1Pos.y * ch + dragAmount.y) / ch).coerceIn(0.05f, 0.95f)
                                viewModel.skill1PositionPercent.value = android.graphics.PointF(newX, newY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SKILL 1", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Render Skill 2
                Box(
                    modifier = Modifier
                        .offset(
                            x = (sk2Pos.x * cw / 2.75f).dp,
                            y = (sk2Pos.y * ch / 2.75f).dp
                        )
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFA855F7).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFFA855F7), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = ((sk2Pos.x * cw + dragAmount.x) / cw).coerceIn(0.05f, 0.95f)
                                val newY = ((sk2Pos.y * ch + dragAmount.y) / ch).coerceIn(0.05f, 0.95f)
                                viewModel.skill2PositionPercent.value = android.graphics.PointF(newX, newY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SKILL 2", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Render Skill 3
                Box(
                    modifier = Modifier
                        .offset(
                            x = (sk3Pos.x * cw / 2.75f).dp,
                            y = (sk3Pos.y * ch / 2.75f).dp
                        )
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFA855F7).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFFA855F7), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = ((sk3Pos.x * cw + dragAmount.x) / cw).coerceIn(0.05f, 0.95f)
                                val newY = ((sk3Pos.y * ch + dragAmount.y) / ch).coerceIn(0.05f, 0.95f)
                                viewModel.skill3PositionPercent.value = android.graphics.PointF(newX, newY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SKILL 3", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Ultimate
                Box(
                    modifier = Modifier
                        .offset(
                            x = (ultPos.x * cw / 2.75f).dp,
                            y = (ultPos.y * ch / 2.75f).dp
                        )
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAB308).copy(alpha = 0.35f))
                        .border(2.dp, Color(0xFFEAB308), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = ((ultPos.x * cw + dragAmount.x) / cw).coerceIn(0.05f, 0.95f)
                                val newY = ((ultPos.y * ch + dragAmount.y) / ch).coerceIn(0.05f, 0.95f)
                                viewModel.ultPositionPercent.value = android.graphics.PointF(newX, newY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("ULT", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Black)
                }

                // Jump
                Box(
                    modifier = Modifier
                        .offset(
                            x = (jumpPos.x * cw / 2.75f).dp,
                            y = (jumpPos.y * ch / 2.75f).dp
                        )
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFF10B981), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = ((jumpPos.x * cw + dragAmount.x) / cw).coerceIn(0.05f, 0.95f)
                                val newY = ((jumpPos.y * ch + dragAmount.y) / ch).coerceIn(0.05f, 0.95f)
                                viewModel.jumpPositionPercent.value = android.graphics.PointF(newX, newY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("JUMP", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// ⚙ SETTINGS - GRAPHICS TAB
// ==========================================

@Composable
fun SettingsGraphicsTab(viewModel: VLearnViewModel) {
    val quality by viewModel.graphicsQuality.collectAsStateWithLifecycle()
    val fps by viewModel.fpsRate.collectAsStateWithLifecycle()
    val shadows by viewModel.shadowsEnabled.collectAsStateWithLifecycle()
    val bloom by viewModel.bloomEnabled.collectAsStateWithLifecycle()
    val blur by viewModel.motionBlurEnabled.collectAsStateWithLifecycle()
    val aa by viewModel.antiAliasingEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("HARDWARE SPEED RESOLUTION", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)

                Text("Rendering Profile Preset", color = Color.Gray, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Low", "Medium", "High", "Ultra").forEach { q ->
                        val isSel = quality == q
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.05f))
                                .clickable { viewModel.graphicsQuality.value = q }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(q, color = if (isSel) Color(0xFF0F172A) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Framerate Limit (FPS Override)", color = Color.Gray, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(30, 60, 90, 120).forEach { rate ->
                        val isSel = fps == rate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.05f))
                                .clickable { viewModel.fpsRate.value = rate }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${rate} FPS", color = if (isSel) Color(0xFF0F172A) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ADVANCED SHADER OVERLAYS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)

                ControlToggleRow("Volumetric Dynamic Shadows", shadows) { viewModel.shadowsEnabled.value = it }
                ControlToggleRow("Bloom Light Glow Effects", bloom) { viewModel.bloomEnabled.value = it }
                ControlToggleRow("Fast Motion Blur (Action Sweeps)", blur) { viewModel.motionBlurEnabled.value = it }
                ControlToggleRow("Multisample Anti-Aliasing (MSAA)", aa) { viewModel.antiAliasingEnabled.value = it }
            }
        }
    }
}

// ==========================================
// ⚙ SETTINGS - AUDIO TAB
// ==========================================

@Composable
fun SettingsAudioTab(viewModel: VLearnViewModel) {
    val master by viewModel.masterVolume.collectAsStateWithLifecycle()
    val music by viewModel.musicVolume.collectAsStateWithLifecycle()
    val sfx by viewModel.sfxVolume.collectAsStateWithLifecycle()
    val voices by viewModel.characterVoicesVolume.collectAsStateWithLifecycle()
    val tutor by viewModel.aiTutorVoiceVolume.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("ACOUSTIC CHANNELS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)

                AudioSliderRow("Master Volume Level", master) { viewModel.masterVolume.value = it }
                AudioSliderRow("Atmospheric Background Music", music) { viewModel.musicVolume.value = it }
                AudioSliderRow("Dynamic SFX (Impacts & Shoots)", sfx) { viewModel.sfxVolume.value = it }
                AudioSliderRow("Hero Character Voice Packets", voices) { viewModel.characterVoicesVolume.value = it }
                AudioSliderRow("AI Study Tutor Voice Synthesis", tutor) { viewModel.aiTutorVoiceVolume.value = it }
            }
        }
    }
}

@Composable
fun AudioSliderRow(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = Color.White, fontSize = 11.sp)
            Text(String.format("%.0f%%", value * 100f), color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f)
    }
}

// ==========================================
// ⚙ SETTINGS - ACCESSIBILITY TAB
// ==========================================

@Composable
fun SettingsAccessibilityTab(viewModel: VLearnViewModel) {
    val mode by viewModel.colorblindMode.collectAsStateWithLifecycle()
    val textSz by viewModel.fontSizeModifier.collectAsStateWithLifecycle()
    val contrast by viewModel.highContrast.collectAsStateWithLifecycle()
    val leftHand by viewModel.leftHandControls.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("CHROMATIC FILTERS & CONTRAST", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)

                Text("Colorblind Simulation Mode", color = Color.Gray, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("None", "Protanopia", "Deuteranopia", "Tritanopia").forEach { m ->
                        val isSel = mode == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFFA855F7) else Color.White.copy(alpha = 0.05f))
                                .clickable { viewModel.colorblindMode.value = m }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(m, color = if (isSel) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }

                ControlToggleRow("High Contrast Elements Glow", contrast) { viewModel.highContrast.value = it }
                ControlToggleRow("Left-Handed Joystick Flip Layout", leftHand) { viewModel.leftHandControls.value = it }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("TEXT & CAPTIONS RESOLUTION", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Caption Font Scale Size", color = Color.Gray, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.fontSizeModifier.value = (textSz - 2).coerceAtLeast(10) }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                        }
                        Text("${textSz}sp", color = Color.White, fontWeight = FontWeight.Black)
                        IconButton(onClick = { viewModel.fontSizeModifier.value = (textSz + 2).coerceAtMost(24) }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
