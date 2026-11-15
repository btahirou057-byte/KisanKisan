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

/* ---------- historique ---------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun History(s: AppState, open: (Sheet) -> Unit) {
    val p = LocalPal.current
    val spent = s.txs.filter { it.amount < 0 }.sumOf { -it.amount }
    val inc = s.txs.filter { it.amount > 0 }.sumOf { it.amount }
    val months = listOf("Mai" to 410_000L, "Juin" to 680_000L, "Juil" to 950_000L, "Août" to 530_000L, "Sept" to 720_000L, "Oct" to spent)
    var sel by remember { mutableIntStateOf(4) }
    var chart by remember { mutableStateOf(true) }
    var menu by remember { mutableStateOf(false) }
    var q by remember { mutableStateOf("") }
    var f by remember { mutableStateOf("Tous") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Historique des transactions", color = p.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Consultez et téléchargez vos reçus officiels Kisan Kisan", color = p.sub, fontSize = 12.sp)
        }
        Box {
            Row(Modifier.clip(RoundedCornerShape(10.dp)).background(Blue).clickable { menu = true }.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.BarChart, null, tint = Color.White, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                Text(if (chart) "Graphique" else "Liste", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(menu, { menu = false }) {
                DropdownMenuItem({ Text("Graphique") }, { chart = true; menu = false })
                DropdownMenuItem({ Text("Liste") }, { chart = false; menu = false })
            }
        }
    }
    if (chart) KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("ANALYSE DES DÉPENSES", color = p.text, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Graphique mensuel en Francs Guinéens (GNF)", color = p.sub, fontSize = 11.sp) }
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Blue.copy(alpha = .2f)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("6 derniers mois", color = Sky, fontSize = 10.sp) }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFEF4444).copy(alpha = .4f), RoundedCornerShape(12.dp)).padding(10.dp)) { Label("Total Dépenses"); Text(spent.gnf(), color = p.text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).border(1.dp, Green.copy(alpha = .4f), RoundedCornerShape(12.dp)).padding(10.dp)) { Label("Total Entrées", Green); Text(inc.gnf(), color = Green, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(p.card2).padding(8.dp)) {
            Column { Text(months[sel].first, color = p.text, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(months[sel].second.gnf(), color = p.text, fontFamily = FontFamily.Monospace, fontSize = 12.sp); Text("Dépenses totales", color = p.sub, fontSize = 10.sp) }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().height(150.dp)) {
            Column(Modifier.fillMaxHeight().width(40.dp), verticalArrangement = Arrangement.SpaceBetween) {
                listOf("1200k", "900k", "600k", "300k", "0k").forEach { Text(it, color = p.sub, fontSize = 9.sp) }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Canvas(Modifier.fillMaxSize()) {
                    for (i in 0..4) {
                        val y = size.height * i / 4f
                        drawLine(p.border, Offset(0f, y), Offset(size.width, y), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                    }
                }
                Row(Modifier.fillMaxSize().padding(horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                    months.forEachIndexed { i, m ->
                        Box(Modifier.weight(1f).fillMaxHeight().clickable { sel = i }, contentAlignment = Alignment.BottomCenter) {
                            Box(Modifier.fillMaxWidth().fillMaxHeight((m.second / 1_200_000f).coerceIn(.02f, 1f)).clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(if (i == 5) Color(0xFF0284C7) else if (i == sel) Color(0xFFD1D5DB) else p.sub.copy(alpha = .4f)))
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 40.dp, top = 4.dp)) { months.forEach { Text(it.first, Modifier.weight(1f), color = p.sub, fontSize = 10.sp, textAlign = TextAlign.Center) } }
        Spacer(Modifier.height(10.dp))
        Label("RÉPARTITION PAR CATÉGORIE")
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            s.txs.filter { it.amount < 0 }.groupBy { category(it) }.forEach { (k, v) ->
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(p.card2).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("$k: ${v.sumOf { -it.amount }.gnf()}", color = p.text, fontSize = 11.sp) }
            }
        }
    }
    KField(q, { q = it }, "Rechercher par nom, référence ou motif...")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("Tous", "Transferts", "Dépôts", "Retraits", "Factures", "Commerçants").forEach { Chip(it, f == it) { f = it } }
    }
    val list = s.txs.filter { t ->
        (q.isBlank() || t.title.contains(q, true) || t.sub.contains(q, true)) && when (f) {
            "Transferts" -> t.type == "transfer" || t.type == "received"
            "Dépôts" -> t.type == "deposit"
            "Retraits" -> t.type == "withdraw"
            "Factures" -> t.type == "bill"
            "Commerçants" -> t.type == "merchant"
            else -> true
        }
    }
    KCard { if (list.isEmpty()) Text("Aucune transaction.", color = p.sub); list.forEach { t -> TxRow(t) { open(Sheet.Receipt(t)) } } }
}
