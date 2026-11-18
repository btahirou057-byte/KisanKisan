package com.kisankisan.app

import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
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

const val ME = "Moi"

class TGroup(val id: Long, val name: String, val amount: Long, val rhythm: String) {
    val members = mutableStateListOf<String>()
    val paid = mutableStateListOf<String>()
    val winners = mutableStateListOf<String>()
    var round by mutableIntStateOf(1)
    val pot: Long get() = amount * members.size
    val finished: Boolean get() = members.isNotEmpty() && winners.size >= members.size
}

class TontineState(ctx: Context) {
    private val sp = ctx.getSharedPreferences("kk_tontine", Context.MODE_PRIVATE)
    val groups = mutableStateListOf<TGroup>()

    init {
        try {
            val a = JSONArray(sp.getString("g", "[]") ?: "[]")
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                val g = TGroup(o.getLong("id"), o.getString("n"), o.getLong("a"), o.getString("r"))
                g.round = o.getInt("t")
                val m = o.getJSONArray("m"); for (j in 0 until m.length()) g.members.add(m.getString(j))
                val pd = o.getJSONArray("p"); for (j in 0 until pd.length()) g.paid.add(pd.getString(j))
                val w = o.getJSONArray("w"); for (j in 0 until w.length()) g.winners.add(w.getString(j))
                groups.add(g)
            }
        } catch (e: Exception) { }
    }

    fun save() {
        val a = JSONArray()
        groups.forEach { g ->
            val o = JSONObject().put("id", g.id).put("n", g.name).put("a", g.amount).put("r", g.rhythm).put("t", g.round)
            o.put("m", JSONArray(g.members.toList()))
            o.put("p", JSONArray(g.paid.toList()))
            o.put("w", JSONArray(g.winners.toList()))
            a.put(o)
        }
        sp.edit().putString("g", a.toString()).apply()
    }

    fun create(name: String, amount: Long, rhythm: String, others: List<String>) {
        val g = TGroup(System.currentTimeMillis(), name, amount, rhythm)
        g.members.add(ME)
        g.members.addAll(others)
        groups.add(0, g)
        save()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TontineSheet(s: AppState, close: () -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    val ts = remember { TontineState(ctx) }
    var sel by remember { mutableStateOf<TGroup?>(null) }
    var creating by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var rhythm by remember { mutableStateOf("Mensuel") }
    var extra by remember { mutableStateOf("") }
    val chosen = remember { mutableStateListOf<String>() }
    var pin by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    val g = sel
    KSheet(if (creating) "Créer une tontine" else g?.name ?: "Tontine Numérique Familiale", Icons.Filled.Groups, close) {
        if (creating) {
            Text("← Retour", color = Sky, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { creating = false; err = null })
            KField(name, { name = it }, "Nom de la tontine", hint = "Ex: Famille Bah")
            KField(amount, { amount = it }, "Cotisation par personne et par tour", number = true, suffix = "GNF")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Hebdomadaire", "Mensuel").forEach { r -> Chip(r, rhythm == r) { rhythm = r } }
            }
            Label("MEMBRES (VOUS ÊTES INCLUS)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                s.contacts.forEach { c ->
                    Chip(c.name.substringBefore(" "), c.name in chosen) { if (c.name in chosen) chosen.remove(c.name) else chosen.add(c.name) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KField(extra, { extra = it }, "Autre membre (nom)", Modifier.weight(1f))
                OutlinedButton(onClick = { if (extra.isNotBlank() && extra.trim() !in chosen) chosen.add(extra.trim()); extra = "" }, modifier = Modifier.height(56.dp), shape = RoundedCornerShape(12.dp)) { Text("Ajouter") }
            }
            Text("Membres : " + (listOf(ME) + chosen).joinToString(", "), color = p.sub, fontSize = 12.sp)
            err?.let { Text(it, color = Color(0xFFEF4444), fontSize = 13.sp) }
            KButton("Créer la tontine") {
                val a = amount.toLongOrNull() ?: 0L
                if (name.isBlank()) err = "Donnez un nom à la tontine."
                else if (a <= 0L) err = "Entrez un montant valide."
                else if (chosen.isEmpty()) err = "Ajoutez au moins un autre membre."
                else {
                    ts.create(name.trim(), a, rhythm, chosen.toList())
                    s.msg = "Tontine créée"
                    name = ""; amount = ""; chosen.clear(); err = null; creating = false
                }
            }
        } else if (g != null) {
            Text("← Retour", color = Sky, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { sel = null; pin = ""; err = null })
            KCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tour ${minOf(g.round, g.members.size)} sur ${g.members.size}", color = p.text, fontWeight = FontWeight.Bold)
                        Text("${g.rhythm} · ${g.amount.gnf()} par personne", color = p.sub, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Label("CAGNOTTE", Green)
                        Text(g.pot.gnf(), color = Green, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            if (g.finished) InfoBox("Tontine terminée", "Tout le monde a reçu la cagnotte une fois.", Icons.Filled.Check, Green)
            Label("MEMBRES")
            g.members.forEach { m ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(p.card2).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(m, color = p.text, fontWeight = FontWeight.SemiBold)
                        if (m in g.winners) Text("A déjà reçu la cagnotte", color = Orange, fontSize = 11.sp)
                    }
                    if (m in g.paid) Text("Payé ✓", color = Green, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    else if (!g.finished) {
                        if (m == ME) Text("À payer", color = Orange, fontSize = 13.sp)
                        else Text("Simuler son paiement", color = Sky, fontSize = 12.sp, modifier = Modifier.clickable { g.paid.add(m); ts.save() })
                    }
                }
            }
            if (!g.finished && ME !in g.paid) {
                KField(pin, { pin = it }, "Code PIN pour cotiser ${g.amount.gnf()}", secret = true)
                err?.let { Text(it, color = Color(0xFFEF4444), fontSize = 13.sp) }
                KButton("Cotiser ${g.amount.gnf()}") {
                    if (pin != s.pin) err = "Code PIN incorrect."
                    else {
                        val e = s.payBill("Tontine ${g.name}", "tour ${g.round}", g.amount)
                        if (e != null) err = e else { g.paid.add(ME); ts.save(); pin = ""; err = null; s.msg = "Cotisation payée" }
                    }
                }
            }
            if (!g.finished && g.members.isNotEmpty() && g.paid.size >= g.members.size) {
                KButton("Faire le tirage au sort", color = Green) {
                    val w = g.members.filter { it !in g.winners }.random()
                    g.winners.add(w)
                    if (w == ME) s.deposit("Tontine ${g.name}", g.pot)
                    g.paid.clear()
                    g.round += 1
                    ts.save()
                    s.msg = if (w == ME) "Vous avez gagné ${g.pot.gnf()} !" else "$w reçoit la cagnotte de ${g.pot.gnf()}"
                }
            }
            if (g.winners.isNotEmpty()) {
                Label("GAGNANTS")
                g.winners.forEachIndexed { i, w -> Text("Tour ${i + 1} : $w", color = p.text, fontSize = 13.sp) }
            }
        } else {
            InfoBox("Épargnez en famille", "À chaque tour, tous les membres cotisent et une personne tirée au sort reçoit toute la cagnotte. (Mode démonstration)", Icons.Filled.Groups, Purple)
            if (ts.groups.isEmpty()) Text("Aucune tontine pour le moment.", color = p.sub, fontSize = 13.sp)
            ts.groups.forEach { x ->
                KCard(click = { sel = x }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(Icons.Filled.Groups, Purple, 44); Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(x.name, color = p.text, fontWeight = FontWeight.Bold)
                            Text("${x.rhythm} · ${x.amount.gnf()} · ${x.members.size} membres", color = p.sub, fontSize = 12.sp)
                            Text(if (x.finished) "Terminée" else "Tour ${x.round} · ${x.paid.size}/${x.members.size} ont cotisé", color = Sky, fontSize = 11.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = p.sub)
                    }
                }
            }
            KButton("Créer une tontine") { creating = true }
        }
    }
}
