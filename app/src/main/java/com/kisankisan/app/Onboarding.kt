package com.kisankisan.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun fmtPhone(d: String) = "+224 " + d.take(3) + " " + d.drop(3).take(2) + " " + d.drop(5).take(2) + " " + d.drop(7).take(2)

@Composable
fun Onboarding(s: AppState) {
    val p = LocalPal.current
    var step by remember { mutableIntStateOf(0) }
    var phone by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().background(p.bg)) {
        Row(
            Modifier.fillMaxWidth().background(Brush.linearGradient(0f to Color(0xFFC8081B), 0.42f to Color(0xFF00114E), 0.72f to Color(0xFF00114E), 1f to Color(0xFF008A2E), start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, 0f))).statusBarsPadding().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                Text("KK", color = Blue, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Kisan Kisan", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Guinée · Simple & Sécurisé", color = Color.White.copy(alpha = .85f), fontSize = 12.sp)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Étape ${step + 1} sur 4", color = Sky, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LinearProgressIndicator(progress = { (step + 1) / 4f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = Sky, trackColor = p.card2)
            when (step) {
                0 -> {
                    Text("Créez votre compte", color = p.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Entrez votre numéro de téléphone guinéen. Nous vous enverrons un code de vérification.", color = p.sub, fontSize = 14.sp)
                    KField(phone, { phone = it.filter(Char::isDigit).take(9); err = null }, "Numéro de téléphone", number = true, hint = "6XX XX XX XX")
                    InfoBox("", "Le préfixe +224 est ajouté automatiquement.")
                    KButton("Recevoir le code SMS") {
                        if (phone.length != 9 || !phone.startsWith("6")) err = "Entrez un numéro à 9 chiffres commençant par 6."
                        else { sent = (100000..999999).random().toString(); code = ""; err = null; step = 1 }
                    }
                }
                1 -> {
                    Text("Vérification", color = p.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Un code à 6 chiffres a été envoyé au ${fmtPhone(phone)}.", color = p.sub, fontSize = 14.sp)
                    InfoBox("Mode démonstration", "Aucun SMS n'est envoyé dans cette version. Votre code de test : $sent", Icons.Filled.Info, Orange)
                    KField(code, { code = it.filter(Char::isDigit).take(6); err = null }, "Code de vérification", number = true)
                    KButton("Vérifier") { if (code == sent) { err = null; step = 2 } else err = "Code incorrect." }
                    Text("Changer de numéro", color = Sky, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { step = 0; err = null })
                }
                2 -> {
                    Text("Votre nom", color = p.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Comme sur votre pièce d'identité.", color = p.sub, fontSize = 14.sp)
                    KField(first, { first = it; err = null }, "Prénom")
                    KField(last, { last = it; err = null }, "Nom")
                    KButton("Continuer") {
                        if (first.isBlank() || last.isBlank()) err = "Entrez votre prénom et votre nom." else { err = null; step = 3 }
                    }
                }
                else -> {
                    Text("Votre code secret", color = p.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Ce code à 4 chiffres confirme chaque envoi d'argent. Ne le partagez avec personne.", color = p.sub, fontSize = 14.sp)
                    KField(pin, { pin = it; err = null }, "Code PIN (4 chiffres)", secret = true)
                    KField(pin2, { pin2 = it; err = null }, "Confirmez le code PIN", secret = true)
                    KButton("Créer mon compte") {
                        if (pin.length != 4) err = "Le code doit avoir 4 chiffres."
                        else if (pin != pin2) err = "Les deux codes ne sont pas identiques."
                        else if (pin == "1234" || pin.all { it == pin[0] }) err = "Choisissez un code moins évident."
                        else {
                            s.register(first.trim() + " " + last.trim(), fmtPhone(phone), pin)
                            s.msg = "Compte créé. Bienvenue !"
                        }
                    }
                }
            }
            err?.let { Text(it, color = Color(0xFFEF4444), fontSize = 13.sp) }
        }
    }
}
