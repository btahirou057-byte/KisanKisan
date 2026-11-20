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

/* ---------- accueil ---------- */

class HomeTile(val l: String, val icon: ImageVector, val c: Color, val f: () -> Unit)

@Composable
fun Home(s: AppState, open: (Sheet) -> Unit, go: (Page) -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    val ag = remember { AgentState(ctx) }
    var showAgent by remember { mutableStateOf(false) }
    var showCredit by remember { mutableStateOf(false) }
    var showPlus by remember { mutableStateOf(false) }
    var showBills by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(s.t("Bienvenue") + ", ", color = p.sub, fontSize = 13.sp)
        Text("Mamadou", color = p.text, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Row(Modifier.clip(RoundedCornerShape(20.dp)).background(Blue.copy(alpha = .18f)).border(1.dp, Blue.copy(alpha = .5f), RoundedCornerShape(20.dp))
            .clickable { open(Sheet.Vault) }.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Savings, null, tint = Orange, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Coffre : ${s.vault.gnf()}", color = p.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(Color(0xFF0C2388), Color(0xFF02104F))))
        .padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .10f)).border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(16.dp)).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.t("Solde principal disponible"), color = Color.White.copy(alpha = .85f), fontSize = 13.sp)
                IconButton({ s.hide = !s.hide; s.save() }, Modifier.size(32.dp)) {
                    Icon(if (s.hide) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Text(if (s.hide) "••••••  GNF" else s.balance.gnf(), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("Plafond journalier : 15 000 000 GNF · Niveau 2", color = Color.White.copy(alpha = .7f), fontSize = 11.sp)
        }
    }
    KCard {
        Box(Modifier.align(Alignment.CenterHorizontally).size(140.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).clickable { open(Sheet.MyQr) }.padding(8.dp), contentAlignment = Alignment.Center) {
            Image(remember { qr(qrPayload()) }, null, Modifier.fillMaxSize(), filterQuality = androidx.compose.ui.graphics.FilterQuality.None)
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF0284C7)), contentAlignment = Alignment.Center) {
                Text("KK", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
        Row(Modifier.align(Alignment.CenterHorizontally).clickable { open(Sheet.Scan) }.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.QrCodeScanner, null, tint = Sky, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
            Text("Scanner", color = Sky, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
    KCard {
        val tiles = listOf(
            HomeTile("Transfert", Icons.Filled.Send, Sky) { open(Sheet.Transfer()) },
            HomeTile("Paiements", Icons.Filled.Storefront, Sky) { go(Page.Services) },
            HomeTile("Crédit", Icons.Filled.PhoneAndroid, Sky) { showCredit = true },
            HomeTile("Coffre", Icons.Filled.Savings, Sky) { open(Sheet.Vault) },
            HomeTile("Dépôt", Icons.Filled.Download, Sky) { open(Sheet.Deposit) },
            HomeTile("Retrait", Icons.Filled.Upload, Sky) { open(Sheet.Withdraw) },
            HomeTile("Factures", Icons.Filled.ReceiptLong, Sky) { showBills = true },
            HomeTile("Plus", Icons.Filled.Apps, p.sub) { showPlus = true },
        )
        tiles.chunked(4).forEach { r ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                r.forEach { t ->
                    Column(Modifier.weight(1f).clickable { t.f() }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(56.dp).clip(CircleShape).background(Blue.copy(alpha = .20f)), contentAlignment = Alignment.Center) {
                            Icon(t.icon, null, tint = t.c, modifier = Modifier.size(26.dp))
                        }
                        Text(s.t(t.l), color = p.text, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                repeat(4 - r.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Label(s.t("Dernières transactions").uppercase()); Spacer(Modifier.weight(1f))
            Text(s.t("Voir tout") + " ›", color = Sky, fontSize = 13.sp, modifier = Modifier.clickable { go(Page.History) })
        }
        s.txs.take(10).forEachIndexed { i, t ->
            if (i > 0) HorizontalDivider(color = p.border)
            TxRow(t) { open(Sheet.Receipt(t)) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.Center) {
            Row(Modifier.clip(RoundedCornerShape(24.dp)).background(Blue.copy(alpha = .2f)).clickable { go(Page.History) }.padding(horizontal = 22.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, null, tint = Sky, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                Text("Rechercher", color = Sky, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
    if (showAgent) AgentSheet(s, ag) { showAgent = false }
    if (showCredit) CreditSheet(s) { showCredit = false }
    if (showPlus) PlusSheet(s, open, go, { showAgent = true }) { showPlus = false }
    if (showBills) BillsPicker(open) { showBills = false }
}

@Composable
fun BillsPicker(open: (Sheet) -> Unit, close: () -> Unit) {
    val p = LocalPal.current
    KSheet("Payer une facture", Icons.Filled.ReceiptLong, close) {
        BILLS.forEach { b ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(p.card2).clickable { close(); open(Sheet.Bill(b.n)) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBubble(b.icon, b.c, 40); Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(b.n, color = p.text, fontWeight = FontWeight.Bold); Text(b.d, color = p.sub, fontSize = 12.sp) }
                Icon(Icons.Filled.ChevronRight, null, tint = p.sub)
            }
        }
    }
}
