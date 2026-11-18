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

class PlusItem(val l: String, val icon: ImageVector, val f: () -> Unit)

@Composable
fun PlusSheet(s: AppState, open: (Sheet) -> Unit, go: (Page) -> Unit, onAgent: () -> Unit, close: () -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    val items = listOf(
        PlusItem("Factures", Icons.Filled.ReceiptLong) { close(); go(Page.Services) },
        PlusItem("Scanner", Icons.Filled.QrCodeScanner) { close(); open(Sheet.Scan) },
        PlusItem("Profil", Icons.Filled.Person) { close(); go(Page.Profile) },
        PlusItem("Agent", Icons.Filled.SupportAgent) { close(); onAgent() },
        PlusItem("Assistance", Icons.Filled.Headset) { close(); ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:8181"))) },
        PlusItem("Nouveau contact", Icons.Filled.PersonAdd) { close(); open(Sheet.AddContact) },
        PlusItem("Paramètres", Icons.Filled.Settings) { close(); open(Sheet.Settings) },
        PlusItem("Tontine", Icons.Filled.Groups) { close(); open(Sheet.Tontine) },
    )
    KSheet("Plus de services", Icons.Filled.Apps, close) {
        items.chunked(4).forEach { r ->
            Row(Modifier.fillMaxWidth()) {
                r.forEach { t ->
                    Column(Modifier.weight(1f).clickable { t.f() }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(56.dp).clip(CircleShape).background(Blue.copy(alpha = .20f)), contentAlignment = Alignment.Center) {
                            Icon(t.icon, null, tint = Sky, modifier = Modifier.size(26.dp))
                        }
                        Text(t.l, color = p.text, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CreditSheet(s: AppState, close: () -> Unit) {
    var op by remember { mutableStateOf("Orange") }
    var num by remember { mutableStateOf("") }
    KSheet("Crédit téléphonique", Icons.Filled.PhoneAndroid, close) {
        Text("Choisir l'opérateur", color = LocalPal.current.text, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Orange", "MTN", "Cellcom").forEach { o ->
                ModeBox(o, Icons.Filled.PhoneAndroid, op == o, Modifier.weight(1f)) { op = o }
            }
        }
        BalanceRow(s)
        KField(num, { num = it }, "Numéro à recharger", hint = "Ex: 624 88 99 00")
        AmountPinForm(s, "Acheter le crédit", true, "Crédit envoyé", "Montant du crédit (GNF)", listOf(5_000L, 10_000L, 20_000L, 50_000L, 100_000L), onOk = close) { a ->
            if (num.filter { it.isDigit() }.length < 8) "Entrez un numéro valide." else s.payBill("Crédit $op", num.trim(), a)
        }
    }
}
