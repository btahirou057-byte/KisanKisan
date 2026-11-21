package com.kisankisan.app

import android.content.Intent
import androidx.activity.compose.BackHandler
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

class Svc(val n: String, val d: String, val icon: ImageVector, val c: Color)

val BILLS = listOf(
    Svc("EDG Électricité", "Compteur & recharge", Icons.Filled.Bolt, Orange),
    Svc("SEG Eaux", "Société des eaux", Icons.Filled.WaterDrop, Color(0xFF14B8A6)),
    Svc("Canal+ / Startimes", "Bouquets TV", Icons.Filled.Tv, Purple),
    Svc("Fibre & 4G", "Pass internet", Icons.Filled.Wifi, Orange),
)
val MERCHANTS = listOf(
    "Supermarché Prima Kipé" to "Kipé, Ratoma - Conakry",
    "Boutique Mode Kouléoun" to "Grand Marché Madina - Conakry",
    "Pharmacie Centrale Almamya" to "Kaloum - Conakry",
    "Restaurant Le Cèdre" to "Camayenne, Dixinn - Conakry",
    "Station TotalEnergies Dixinn" to "Dixinn - Conakry",
)
val AVATARS = listOf(Color(0xFF0F9D6B), Color(0xFF1D5BFF), Color(0xFF9B1DFF), Color(0xFFE07000), Color(0xFFDB2777))

enum class Page(val label: String, val icon: ImageVector) {
    Home("Accueil", Icons.Filled.Home), Services("Services", Icons.Filled.GridView),
    History("Historique", Icons.Filled.History), Profile("Profil", Icons.Filled.Person)
}

fun qrPayload(amount: Long = 0L) =
    "kisankisan://pay?phone=" + Uri.encode("+" + PHONE.filter { it.isDigit() }) + "&name=" + Uri.encode(USER) + (if (amount > 0) "&amount=$amount" else "")

fun share(ctx: android.content.Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    ctx.startActivity(Intent.createChooser(i, "Partager"))
}

/* ---------- composants ---------- */

@Composable
fun KCard(mod: Modifier = Modifier, click: (() -> Unit)? = null, border: Color? = null, c: @Composable ColumnScope.() -> Unit) {
    val p = LocalPal.current
    val sh = RoundedCornerShape(18.dp)
    var m = mod.fillMaxWidth().clip(sh).background(p.card).border(1.dp, border ?: p.border, sh)
    if (click != null) m = m.clickable(onClick = click)
    Column(m.padding(14.dp), content = c)
}

@Composable
fun Label(t: String, color: Color = LocalPal.current.sub, modifier: Modifier = Modifier) = Text(t, modifier = modifier, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)

@Composable
fun IconBubble(icon: ImageVector, tint: Color, size: Int = 40) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size((size * 0.55f).dp))
    }
}

@Composable
fun KButton(text: String, mod: Modifier = Modifier, color: Color = Blue, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = mod.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun Chip(text: String, selected: Boolean, mod: Modifier = Modifier, onClick: () -> Unit) {
    val p = LocalPal.current
    val sh = RoundedCornerShape(10.dp)
    Box(mod.clip(sh).background(if (selected) Blue else p.card2).border(1.dp, if (selected) Blue else p.border, sh).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(text, color = if (selected) Color.White else p.text, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun KField(value: String, onChange: (String) -> Unit, label: String, mod: Modifier = Modifier, number: Boolean = false,
           secret: Boolean = false, suffix: String? = null, hint: String? = null) {
    val p = LocalPal.current
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(if (secret) v.filter(Char::isDigit).take(4) else if (number) v.filter(Char::isDigit).take(12) else v) },
        modifier = mod.fillMaxWidth(),
        label = { Text(label) },
        placeholder = if (hint != null) { { Text(hint, color = p.sub) } } else null,
        suffix = if (suffix != null) { { Text(suffix, color = p.sub) } } else null,
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (secret || number) KeyboardType.Number else KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = p.text, unfocusedTextColor = p.text, focusedBorderColor = Blue,
            unfocusedBorderColor = p.border, focusedLabelColor = Blue, unfocusedLabelColor = p.sub, cursorColor = Blue),
        shape = RoundedCornerShape(12.dp),
    )
}

fun txIcon(type: String): Pair<ImageVector, Color> = when (type) {
    "deposit", "received" -> Icons.Filled.Download to Green
    "withdraw" -> Icons.Filled.Upload to Sky
    "bill" -> Icons.Filled.ReceiptLong to Orange
    "merchant" -> Icons.Filled.Storefront to Purple
    else -> Icons.Filled.Send to Sky
}

@Composable
fun TxRow(t: Tx, onClick: () -> Unit) {
    val p = LocalPal.current
    val (ic, col) = txIcon(t.type)
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBubble(ic, col, 40)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(t.title, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
            Text(t.sub + " · " + t.time, color = p.sub, fontSize = 11.sp)
        }
        Spacer(Modifier.width(6.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text((if (t.amount > 0) "+" else "-") + t.amount.gnf(), color = if (t.amount > 0) Green else p.text, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Réussi", color = Green, fontSize = 10.sp)
        }
    }
}

@Composable
fun Avatar(c: Contact, i: Int, size: Int = 52) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(AVATARS[i % AVATARS.size]), contentAlignment = Alignment.Center) {
        Text(c.initials, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

/* ---------- application ---------- */

@Composable
fun App(s: AppState) {
    val p = LocalPal.current
    var page by remember { mutableStateOf(Page.Home) }
    var sheet by remember { mutableStateOf<Sheet>(Sheet.None) }
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(s.msg) { s.msg?.let { snack.showSnackbar(it); s.msg = null } }
    val open = { x: Sheet -> sheet = x }
    BackHandler(enabled = page != Page.Home) { page = Page.Home }
    Scaffold(
        containerColor = p.bg,
        snackbarHost = { SnackbarHost(snack) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().navigationBarsPadding()) {
            Header(s, open)
            if (page != Page.Home) Row(Modifier.fillMaxWidth().clickable { page = Page.Home }.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = p.text); Spacer(Modifier.width(12.dp))
                Text(s.t(page.label), color = p.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (page) {
                    Page.Home -> Home(s, open) { page = it }
                    Page.Services -> Services(open)
                    Page.History -> History(s, open)
                    Page.Profile -> Profile(s, open)
                }
            }
        }
    }
    SheetHost(sheet, s, open) { sheet = Sheet.None }
}

@Composable
fun HeaderBtn(icon: ImageVector, onClick: () -> Unit) {
    Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = .16f)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun Header(s: AppState, open: (Sheet) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Brush.linearGradient(0f to Color(0xFFC8081B), 0.42f to Color(0xFF00114E), 0.72f to Color(0xFF00114E), 1f to Color(0xFF008A2E), start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, 0f))).statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White), contentAlignment = Alignment.Center) {
            Text("KK", color = Blue, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Kisan ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Kisan", color = Color(0xFF9CC0FF), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.width(5.dp))
                Box(Modifier.clip(RoundedCornerShape(5.dp)).background(Color.White.copy(alpha = .18f)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                    Text("GN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("Guinée · Simple & Sécurisé", color = Color.White.copy(alpha = .85f), fontSize = 10.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            HeaderBtn(if (s.dark) Icons.Filled.WbSunny else Icons.Filled.DarkMode) { s.dark = !s.dark; s.save() }
            HeaderBtn(Icons.Filled.QrCode2) { open(Sheet.MyQr) }
            Box {
                HeaderBtn(Icons.Filled.Notifications) { open(Sheet.Notifs) }
                if (s.unread > 0) Box(Modifier.align(Alignment.TopEnd).size(16.dp).clip(CircleShape).background(Orange), contentAlignment = Alignment.Center) {
                    Text("${s.unread}", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            HeaderBtn(Icons.Filled.Settings) { open(Sheet.Settings) }
        }
    }
}
