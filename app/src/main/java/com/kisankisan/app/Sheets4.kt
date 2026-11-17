package com.kisankisan.app

import android.app.Activity
import android.content.Context
import android.content.Intent
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
fun SetRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    val p = LocalPal.current
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Sky, modifier = Modifier.size(26.dp)); Spacer(Modifier.width(16.dp))
        Text(title, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SetSwitchRow(icon: ImageVector, title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalPal.current
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Sky, modifier = Modifier.size(26.dp)); Spacer(Modifier.width(16.dp))
        Text(title, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked, onChange)
    }
}

@Composable
fun SetGroup(title: String?, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPal.current
    if (title != null) Text(title, color = p.text, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(top = 6.dp))
    KCard { content() }
}

@Composable
fun SettingsSheet(s: AppState, close: () -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    val act = ctx as? FragmentActivity
    var sec by remember { mutableStateOf("") }
    var thr by remember { mutableStateOf(s.threshold.toString()) }
    var promo by remember { mutableStateOf("") }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    fun tog(k: String) { sec = if (sec == k) "" else k }
    KSheet("Paramètres", Icons.Filled.Settings, close) {
        SetGroup(null) {
            SetRow(Icons.Filled.PersonAdd, "Ajouter un autre compte") { s.msg = "Ajouter un autre compte : bientôt disponible (démo)" }
        }
        SetGroup("Partager") {
            SetRow(Icons.Filled.Share, "Inviter un ami à rejoindre Kisan Kisan") { share(ctx, "Rejoins-moi sur Kisan Kisan, l'application de paiement mobile de Guinée.") }
            HorizontalDivider(color = p.border)
            SetRow(Icons.Filled.AutoAwesome, "Utiliser le code promotionnel") { tog("promo") }
            if (sec == "promo") {
                KField(promo, { promo = it }, "Code promotionnel")
                Spacer(Modifier.height(8.dp))
                KButton("Appliquer") {
                    if (promo.isBlank()) s.msg = "Entrez un code" else { s.msg = "Code enregistré : " + promo.trim(); promo = ""; sec = "" }
                }
            }
        }
        SetGroup("Support") {
            SetRow(Icons.Filled.Call, "Contactez le service client") { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:8181"))) }
            HorizontalDivider(color = p.border)
            SetRow(Icons.Filled.Assessment, "Vérifiez votre plafond") { tog("plafond") }
            if (sec == "plafond") {
                Text("Plafond journalier : ${LIMIT.gnf()} · Niveau 2", color = p.text, fontSize = 13.sp)
                Text("Dépensé aujourd'hui : ${s.daily.gnf()}", color = p.sub, fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp))
                LinearProgressIndicator(progress = { (s.daily.toFloat() / LIMIT).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Blue, trackColor = p.card2)
                Spacer(Modifier.height(8.dp))
            }
            HorizontalDivider(color = p.border)
            SetRow(Icons.Filled.LocationOn, "Trouvez les agents à proximité") {
                try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Kiosque+Kisan+Conakry"))) } catch (e: Exception) { s.msg = "Aucune application de carte trouvée" }
            }
        }
        SetGroup("Préférences") {
            SetSwitchRow(Icons.Filled.DarkMode, "Thème sombre", s.dark) { s.dark = it; s.save() }
            HorizontalDivider(color = p.border)
            SetRow(Icons.Filled.Language, "Langue : ${s.lang}") { tog("lang") }
            if (sec == "lang") {
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
            HorizontalDivider(color = p.border)
            SetRow(Icons.Filled.NotificationsActive, "Alerte solde minimum") { tog("alert") }
            if (sec == "alert") {
                SetSwitchRow(Icons.Filled.Savings, "Activer l'alerte de solde bas", s.alertOn) { s.alertOn = it; s.save() }
                BalanceRow(s)
                Spacer(Modifier.height(10.dp))
                KField(thr, { thr = it }, "Seuil d'alerte en Francs Guinéens (GNF)", number = true, suffix = "GNF")
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(50_000L, 100_000L, 200_000L, 500_000L, 1_000_000L).forEach { v -> Chip(v.num(), thr == v.toString()) { thr = v.toString() } }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KButton("Appliquer ce seuil", Modifier.weight(1f)) { s.threshold = thr.toLongOrNull() ?: s.threshold; s.save(); s.msg = "Seuil enregistré : ${s.threshold.gnf()}" }
                    OutlinedButton(onClick = { s.notify("Test : alerte de solde bas (${s.balance.gnf()})", true); s.msg = "Notification de test envoyée" }, modifier = Modifier.height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Tester", color = Orange) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        SetGroup("Sécurité") {
            SetRow(Icons.Filled.PhoneAndroid, "Vos appareils connectés") { tog("devices") }
            if (sec == "devices") {
                Text("Cet appareil · Android · connecté maintenant", color = p.sub, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            HorizontalDivider(color = p.border)
            SetSwitchRow(Icons.Filled.Fingerprint, "Connexion par empreinte digitale", s.bio) { on ->
                if (!on) { s.bio = false; s.save() }
                else if (act != null && bioAvailable(act)) askBio(act) { s.bio = true; s.save() }
                else s.msg = "Aucune empreinte enregistrée sur cet appareil"
            }
            HorizontalDivider(color = p.border)
            SetRow(Icons.Filled.Shield, "Modifiez votre code secret") { tog("pin") }
            if (sec == "pin") {
                KField(oldPin, { oldPin = it }, "Ancien PIN", secret = true)
                Spacer(Modifier.height(8.dp))
                KField(newPin, { newPin = it }, "Nouveau PIN (4 chiffres)", secret = true)
                Spacer(Modifier.height(8.dp))
                KButton("Enregistrer le nouveau PIN") {
                    if (oldPin != s.pin) s.msg = "Ancien PIN incorrect" else if (newPin.length != 4) s.msg = "Le PIN doit avoir 4 chiffres"
                    else { s.pin = newPin; s.save(); s.msg = "Code PIN modifié"; sec = ""; oldPin = ""; newPin = "" }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        SetGroup(null) {
            SetRow(Icons.Filled.PowerSettingsNew, "Se déconnecter ($PHONE)") { (ctx as? Activity)?.finishAffinity() }
        }
    }
}
