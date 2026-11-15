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

/* ---------- profil ---------- */

@Composable
fun SettingRow(icon: ImageVector, tint: Color, title: String, sub: String, trailing: String? = null, onClick: () -> Unit) {
    val p = LocalPal.current
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBubble(icon, tint, 38); Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) { Text(title, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text(sub, color = p.sub, fontSize = 11.sp) }
        if (trailing != null) Text(trailing, color = Sky, fontWeight = FontWeight.Bold, fontSize = 13.sp) else Icon(Icons.Filled.ChevronRight, null, tint = p.sub)
    }
}

@Composable
fun Profile(s: AppState, open: (Sheet) -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    KCard {
        Box(Modifier.align(Alignment.CenterHorizontally).size(78.dp).clip(CircleShape).background(Blue.copy(alpha = .25f)), contentAlignment = Alignment.Center) {
            Text("MT", color = Sky, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        Text(USER, color = p.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
        Text(PHONE, color = p.sub, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.CenterHorizontally))
        Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp).clip(RoundedCornerShape(20.dp)).border(1.dp, Green.copy(alpha = .5f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Shield, null, tint = Green, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Niveau 2 (Plafond 15M GNF/jour)", color = Green, fontSize = 12.sp) }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = p.border)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(p.card2).clickable { open(Sheet.MyQr) }.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCode2, null, tint = Sky, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Mon QR Code", color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(p.card2).clickable { open(Sheet.Settings) }.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, null, tint = Sky, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Sécurité & PIN", color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    KCard {
        Label("PLAFONDS & SÉCURITÉ")
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Dépenses quotidiennes", color = p.sub, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("${s.daily.gnf()} / ${LIMIT.gnf()}", color = p.text, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
        }
        LinearProgressIndicator(progress = { (s.daily.toFloat() / LIMIT).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Blue, trackColor = p.card2)
    }
    KCard {
        SettingRow(Icons.Filled.DarkMode, Purple, "Thème d'affichage", if (s.dark) "Actuellement : Mode Sombre" else "Actuellement : Mode Clair", if (s.dark) "Sombre" else "Clair") { s.dark = !s.dark; s.save() }
        HorizontalDivider(color = p.border)
        SettingRow(Icons.Filled.Fingerprint, Sky, "Empreinte digitale & Code PIN", "Biométrie et code secret à 4 chiffres") { open(Sheet.Settings) }
        HorizontalDivider(color = p.border)
        SettingRow(Icons.Filled.PhoneAndroid, Purple, "Changer de Numéro / Connexion", "Vérification SMS et compte") { s.msg = "Vérification SMS disponible après intégration bancaire (démo)" }
        HorizontalDivider(color = p.border)
        SettingRow(Icons.Filled.Headset, Green, "Assistance Client 24/7", "Numéro vert gratuit : 8181 (Guinée)", "8181") { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:8181"))) }
        HorizontalDivider(color = p.border)
        SettingRow(Icons.Filled.LocationOn, Orange, "Trouver un Kiosque Kisan", "Plus de 2 500 agents à Conakry & Régions") {
            try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Kiosque+Kisan+Conakry"))) } catch (e: Exception) { s.msg = "Aucune application de carte trouvée" }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Row(Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, p.border, RoundedCornerShape(12.dp)).clickable { s.reset(); s.msg = "Données de démo réinitialisées" }.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Refresh, null, tint = p.text, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Réinitialiser les données de démo", color = p.text, fontSize = 13.sp)
        }
    }
    Text("Kisan Kisan Prototype. Les transferts réels seront activés après intégration avec un partenaire bancaire agréé.", color = p.sub, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
}
