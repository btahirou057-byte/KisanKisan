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

/* ---------- QR plein écran ---------- */

@Composable
fun MyQrSheet(s: AppState, close: () -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    val clip = LocalClipboardManager.current
    var bright by remember { mutableStateOf(false) }
    var custom by remember { mutableStateOf(false) }
    var amt by remember { mutableStateOf("") }
    DisposableEffect(bright) {
        val w = (ctx as? Activity)?.window
        if (w != null) { val a = w.attributes; a.screenBrightness = if (bright) 1f else -1f; w.attributes = a }
        onDispose { val a = w?.attributes; if (w != null && a != null) { a.screenBrightness = -1f; w.attributes = a } }
    }
    val payload = qrPayload(amt.toLongOrNull() ?: 0L)
    KSheet("Mon QR Code Kisan Kisan", Icons.Filled.QrCode2, close) {
        Text("Recevez des francs guinéens en 1 seconde", color = p.sub, fontSize = 12.sp)
        Box(Modifier.align(Alignment.CenterHorizontally).size(76.dp).clip(CircleShape).background(Blue.copy(alpha = .25f)), contentAlignment = Alignment.Center) { Text("MT", color = Sky, fontWeight = FontWeight.Bold, fontSize = 22.sp) }
        Text(USER, color = p.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(PHONE, color = p.sub, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.CenterHorizontally))
        Box(Modifier.align(Alignment.CenterHorizontally).size(260.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(12.dp), contentAlignment = Alignment.Center) {
            Image(remember(payload) { qr(payload) }, null, Modifier.fillMaxSize(), filterQuality = androidx.compose.ui.graphics.FilterQuality.None)
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0284C7)), contentAlignment = Alignment.Center) { Text("KK", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) }
        }
        Text("L'expéditeur scanne ce code avec son application Kisan Kisan pour valider le virement.", color = p.sub, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).clickable { custom = !custom }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AttachMoney, null, tint = Sky); Text("Définir un montant précis", color = Sky, fontWeight = FontWeight.SemiBold)
            }
            Chip("Luminosité +", bright) { bright = !bright }
        }
        if (custom) KField(amt, { amt = it }, "Montant demandé", number = true, suffix = "GNF")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(p.card2).clickable { clip.setText(AnnotatedString(PHONE)); s.msg = "Numéro copié" }.padding(14.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ContentCopy, null, tint = p.text, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Copier numéro", color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(p.card2).clickable { share(ctx, "Payez-moi sur Kisan Kisan : $payload") }.padding(14.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Share, null, tint = p.text, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Partager lien", color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
/* ---------- coffre, contacts, notifications, reçu ---------- */

@Composable
fun VaultSheet(s: AppState, close: () -> Unit) {
    val p = LocalPal.current
    var lock by remember { mutableStateOf(true) }
    KSheet("Coffre Kisan Kisan", Icons.Filled.Savings, close) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Argent à l'abri", color = p.sub, fontSize = 12.sp)
            Text(s.vault.gnf(), color = Green, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        BalanceRow(s)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeBox("Mettre au coffre", Icons.Filled.Lock, lock, Modifier.weight(1f)) { lock = true }
            ModeBox("Retirer du coffre", Icons.Filled.Download, !lock, Modifier.weight(1f)) { lock = false }
        }
        key(lock) {
            AmountPinForm(s, if (lock) "Verrouiller au coffre" else "Retirer vers mon solde", true, "Opération effectuée", onOk = close) { a ->
                if (lock) s.toVault(a) else s.fromVault(a)
            }
        }
    }
}

@Composable
fun AddContactSheet(s: AppState, close: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    KSheet("Nouveau bénéficiaire", Icons.Filled.Groups, close) {
        KField(name, { name = it }, "Nom complet")
        KField(phone, { phone = it }, "Numéro (Guinée)", hint = "+224 6...")
        KButton("Ajouter") {
            if (name.isBlank() || phone.isBlank()) s.msg = "Remplissez le nom et le numéro" else { s.addContact(name.trim(), phone.trim()); s.msg = "Bénéficiaire ajouté"; close() }
        }
    }
}

@Composable
fun NotifsSheet(s: AppState, close: () -> Unit) {
    val p = LocalPal.current
    LaunchedEffect(Unit) { s.readAll() }
    KSheet("Notifications", Icons.Filled.Notifications, close) {
        if (s.notifs.isEmpty()) Text("Aucune notification.", color = p.sub)
        s.notifs.forEach { n ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(p.card2).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NotificationsActive, null, tint = Orange); Spacer(Modifier.width(10.dp)); Text(n, color = p.text, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ReceiptSheet(t: Tx, close: () -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    KSheet("Reçu Kisan Kisan", Icons.Filled.ReceiptLong, close) {
        listOf("Opération" to t.sub, "Libellé" to t.title, "Montant" to ((if (t.amount > 0) "+" else "-") + t.amount.gnf()), "Date" to t.time, "Statut" to "Réussi", "Référence" to "KK-${t.id.toString().padStart(6, '0')}").forEach { (k, v) ->
            Row(Modifier.fillMaxWidth()) { Text(k, color = p.sub, modifier = Modifier.weight(1f)); Text(v, color = p.text, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1.4f)) }
        }
        KButton("Partager le reçu") { share(ctx, "Reçu Kisan Kisan\n${t.sub} · ${t.title}\n${t.amount.gnf()}\n${t.time}\nRéf. KK-${t.id.toString().padStart(6, '0')}") }
    }
}
