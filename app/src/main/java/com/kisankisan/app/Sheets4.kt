package com.kisankisan.app

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/* ---------- paramètres ---------- */

@Composable
fun SettingsSheet(s: AppState, close: () -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    val act = ctx as? FragmentActivity
    var thr by remember { mutableStateOf(s.threshold.toString()) }
    var changePin by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    KSheet("Paramètres de l'application", Icons.Filled.Settings, close) {
        Label("APPARENCE & THÈME")
        KCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.DarkMode, Sky); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text("Thème Sombre (Dark Mode)", color = p.text, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Basculez toute l'interface en mode nuit ou jour", color = p.sub, fontSize = 11.sp) }
                Switch(s.dark, { s.dark = it; s.save() })
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeBox("Clair", Icons.Filled.WbSunny, !s.dark, Modifier.weight(1f)) { s.dark = false; s.save() }
                ModeBox("Sombre", Icons.Filled.DarkMode, s.dark, Modifier.weight(1f)) { s.dark = true; s.save() }
            }
        }
        Label("LANGUE DE L'INTERFACE")
        KCard {
            listOf(Triple("Français", "(Français)", "Guinée · Officiel"), Triple("Anglais", "(English)", "International"), Triple("Soussou", "(Sosoxui)", "Basse-Guinée"),
                Triple("Pular", "(Pulaar)", "Fouta-Djalon"), Triple("Maninka", "(Maninkakan)", "Haute-Guinée")).forEach { (n, nat, reg) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (s.lang == n) Blue else Color.Transparent).clickable { s.lang = n; s.save() }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(n, color = if (s.lang == n) Color.White else p.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(" $nat", color = if (s.lang == n) Color.White else p.sub, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(reg, color = if (s.lang == n) Color.White else p.sub, fontSize = 11.sp)
                    if (s.lang == n) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Label("ALERTE SOLDE MINIMUM")
        KCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.Savings, Orange); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text("Activer l'alerte de solde bas", color = p.text, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Notification automatique en cas de solde inférieur", color = p.sub, fontSize = 11.sp) }
                Switch(s.alertOn, { s.alertOn = it; s.save() })
            }
            Spacer(Modifier.height(10.dp))
            BalanceRow(s)
            Spacer(Modifier.height(10.dp))
            KField(thr, { thr = it }, "Seuil d'alerte en Francs Guinéens (GNF)", number = true, suffix = "GNF")
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(50_000L, 100_000L, 200_000L, 500_000L, 1_000_000L).forEach { v -> Chip(v.num(), thr == v.toString()) { thr = v.toString() } }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KButton("Appliquer ce seuil d'alerte", Modifier.weight(1f)) { s.threshold = thr.toLongOrNull() ?: s.threshold; s.save(); s.msg = "Seuil enregistré : ${s.threshold.gnf()}" }
                OutlinedButton(onClick = { s.notify("Test : alerte de solde bas (${s.balance.gnf()})", true); s.msg = "Notification de test envoyée" }, modifier = Modifier.height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Tester", color = Orange) }
            }
        }
        Label("SÉCURITÉ & AUTHENTIFICATION")
        KCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.Fingerprint, Sky); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text("Connexion par empreinte digitale", color = p.text, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Sécurisé via biométrie Android", color = p.sub, fontSize = 11.sp) }
                Switch(s.bio, { on ->
                    if (!on) { s.bio = false; s.save() }
                    else if (act != null && bioAvailable(act)) askBio(act) { s.bio = true; s.save() }
                    else s.msg = "Aucune empreinte enregistrée sur cet appareil"
                })
            }
        }
        KCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.Lock, Sky); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text("Changer le Code PIN", color = p.text, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Code secret à 4 chiffres (Démo : 1234)", color = p.sub, fontSize = 11.sp) }
                Text("Modifier", color = Sky, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { changePin = !changePin })
            }
            if (changePin) {
                Spacer(Modifier.height(10.dp))
                KField(oldPin, { oldPin = it }, "Ancien PIN", secret = true)
                Spacer(Modifier.height(8.dp))
                KField(newPin, { newPin = it }, "Nouveau PIN (4 chiffres)", secret = true)
                Spacer(Modifier.height(8.dp))
                KButton("Enregistrer le nouveau PIN") {
                    if (oldPin != s.pin) s.msg = "Ancien PIN incorrect" else if (newPin.length != 4) s.msg = "Le PIN doit avoir 4 chiffres"
                    else { s.pin = newPin; s.save(); s.msg = "Code PIN modifié"; changePin = false; oldPin = ""; newPin = "" }
                }
            }
        }
        Label("LANGUE, DEVISE & RÉGION")
        KCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.Language, Sky); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text("Devise Principale", color = p.text, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Franc Guinéen (GNF)", color = p.sub, fontSize = 11.sp) }
                Text("GNF", color = Green, fontWeight = FontWeight.Bold)
            }
        }
    }
}
