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
    val clip = LocalClipboardManager.current
    var tab by remember { mutableIntStateOf(0) }
    val ag = remember { AgentState(ctx) }
    var showAgent by remember { mutableStateOf(false) }
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
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(Blue, Color(0xFF0E2E8C))))
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
        val tiles = listOf(
            HomeTile("Transfert", Icons.Filled.Send, Sky) { open(Sheet.Transfer()) },
            HomeTile("Paiements", Icons.Filled.Storefront, Sky) { go(Page.Services) },
            HomeTile("Dépôt", Icons.Filled.Download, Sky) { open(Sheet.Deposit) },
            HomeTile("Retrait", Icons.Filled.Upload, Sky) { open(Sheet.Withdraw) },
            HomeTile("Scanner", Icons.Filled.QrCodeScanner, Sky) { open(Sheet.Scan) },
            HomeTile("Coffre", Icons.Filled.Savings, Sky) { open(Sheet.Vault) },
            HomeTile("Historique", Icons.Filled.History, Sky) { go(Page.History) },
            HomeTile("Agent", Icons.Filled.SupportAgent, Sky) { showAgent = true },
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
            }
        }
    }
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Mon QR Code Kisan Kisan", color = p.text, fontWeight = FontWeight.Bold)
                Text("Présentez pour recevoir de l'argent", color = p.sub, fontSize = 12.sp)
            }
            Row(Modifier.clip(RoundedCornerShape(10.dp)).background(p.card2).padding(3.dp)) {
                listOf("Recevoir", "Payer").forEachIndexed { i, l ->
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (tab == i) Blue else Color.Transparent).clickable { tab = i }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(l, color = if (tab == i) Color.White else p.sub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 10.dp), color = p.border)
        if (tab == 0) {
            Box(Modifier.align(Alignment.CenterHorizontally).size(210.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(10.dp), contentAlignment = Alignment.Center) {
                Image(remember { qr(qrPayload()) }, null, Modifier.fillMaxSize(), filterQuality = androidx.compose.ui.graphics.FilterQuality.None)
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF0284C7)), contentAlignment = Alignment.Center) {
                    Text("KK", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
            Row(Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(PHONE, color = p.text, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Row(Modifier.clip(RoundedCornerShape(8.dp)).background(p.card2).clickable { clip.setText(AnnotatedString(PHONE)); s.msg = "Numéro copié" }.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ContentCopy, null, tint = p.sub, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Copier", color = p.sub, fontSize = 12.sp)
                }
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.QrCodeScanner, null, tint = Sky, modifier = Modifier.size(56.dp))
                Text("Payez un commerçant, un ami ou une facture en scannant son QR code.", color = p.sub, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                KButton("Scanner pour payer") { open(Sheet.Scan) }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 10.dp), color = p.border)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Row(Modifier.clickable { open(Sheet.MyQr) }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Fullscreen, null, tint = Sky, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Plein écran", color = Sky, fontSize = 13.sp)
            }
            Row(Modifier.clickable { share(ctx, "Payez-moi sur Kisan Kisan : $PHONE\n${qrPayload()}") }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bolt, null, tint = Orange, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Envoyer à un proche", color = p.text, fontSize = 13.sp)
            }
        }
    }
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Groups, null, tint = Sky, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
            Label(s.t("Bénéficiaires fréquents").uppercase())
            Spacer(Modifier.weight(1f))
            Text("Nouveau +", color = Sky, fontSize = 12.sp, modifier = Modifier.clickable { open(Sheet.AddContact) })
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.clickable { open(Sheet.AddContact) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(52.dp).clip(CircleShape).border(1.5.dp, Sky, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, null, tint = Sky) }
                Text("Ajouter", color = p.sub, fontSize = 12.sp)
            }
            s.contacts.take(4).forEachIndexed { i, c ->
                Column(Modifier.clickable { open(Sheet.Transfer(c)) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Avatar(c, i); Text(c.name.substringBefore(" "), color = p.text, fontSize = 12.sp)
                }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        KCard(Modifier.weight(1f), { go(Page.Services) }) {
            Row { IconBubble(Icons.Filled.ReceiptLong, Orange); Spacer(Modifier.weight(1f)); Icon(Icons.Filled.ChevronRight, null, tint = p.sub) }
            Text("Payer Factures", color = p.text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text("EDG, SEG, Canal+, Pass Fibre", color = p.sub, fontSize = 11.sp)
        }
        KCard(Modifier.weight(1f), { open(Sheet.Vault) }) {
            Row { IconBubble(Icons.Filled.Savings, Green); Spacer(Modifier.weight(1f)); Icon(Icons.Filled.ChevronRight, null, tint = p.sub) }
            Text("Coffre Kisan Kisan", color = p.text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text("Épargne sécurisée & projets", color = p.sub, fontSize = 11.sp)
        }
    }
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Label(s.t("Dernières transactions").uppercase()); Spacer(Modifier.weight(1f))
            Text(s.t("Voir tout") + " ›", color = Sky, fontSize = 13.sp, modifier = Modifier.clickable { go(Page.History) })
        }
        s.txs.take(4).forEach { t -> TxRow(t) { open(Sheet.Receipt(t)) } }
    }
    if (showAgent) AgentSheet(s, ag) { showAgent = false }
}
