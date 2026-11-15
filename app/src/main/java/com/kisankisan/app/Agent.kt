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

import android.content.Context

class AgentState(ctx: Context) {
    private val sp = ctx.getSharedPreferences("kk_agent", Context.MODE_PRIVATE)
    var floatBal by mutableLongStateOf(sp.getLong("float", 5_000_000L))
    var commission by mutableLongStateOf(sp.getLong("com", 0L))
    val log = mutableStateListOf<String>()

    init {
        (sp.getString("log", "") ?: "").split("\n").filter { it.isNotBlank() }.forEach { log.add(it) }
    }

    fun add(line: String) {
        log.add(0, line)
        while (log.size > 30) log.removeAt(log.size - 1)
        sp.edit().putLong("float", floatBal).putLong("com", commission).putString("log", log.joinToString("\n")).apply()
    }
}

@Composable
fun AgentSheet(s: AppState, ag: AgentState, close: () -> Unit) {
    val p = LocalPal.current
    var mode by remember { mutableIntStateOf(0) }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var round by remember { mutableIntStateOf(0) }

    fun doOp(a: Long): String? {
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 8) return "Entrez un numéro de client valide."
        if (a <= 0L) return "Entrez un montant valide."
        val mine = digits.endsWith("624889900")
        when (mode) {
            0 -> {
                if (a > ag.floatBal) return "Float agent insuffisant."
                if (mine) { val e = s.deposit("Agent Kisan", a); if (e != null) return e }
                ag.floatBal -= a; ag.commission += a / 200
                ag.add("Dépôt ${a.gnf()} pour $phone")
            }
            1 -> {
                if (mine) {
                    if (code.length != 6 || code != s.lastCode) return "Code de retrait incorrect."
                    s.lastCode = ""
                }
                ag.floatBal += a; ag.commission += a / 200
                ag.add("Retrait ${a.gnf()} pour $phone")
            }
            else -> {
                if (a > ag.floatBal) return "Float agent insuffisant."
                if (mine) { val e = s.deposit("Transfert agent", a); if (e != null) return e }
                ag.floatBal -= a
                ag.add("Transfert ${a.gnf()} vers $phone")
            }
        }
        return null
    }

    KSheet("Espace Agent Kisan", Icons.Filled.SupportAgent, close) {
        KCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.Storefront, Orange, 44); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Agent KK-AGN-0412", color = p.text, fontWeight = FontWeight.Bold)
                    Text("Kiosque Madina · Conakry", color = p.sub, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Column(Modifier.weight(1f)) { Label("FLOAT DISPONIBLE"); Text(ag.floatBal.gnf(), color = p.text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                Column(Modifier.weight(1f)) { Label("COMMISSIONS", Green); Text(ag.commission.gnf(), color = Green, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeBox("Dépôt client", Icons.Filled.Download, mode == 0, Modifier.weight(1f)) { mode = 0 }
            ModeBox("Retrait client", Icons.Filled.Upload, mode == 1, Modifier.weight(1f)) { mode = 1 }
            ModeBox("Transfert", Icons.Filled.Send, mode == 2, Modifier.weight(1f)) { mode = 2 }
        }
        InfoBox(
            if (mode == 0) "Dépôt client" else if (mode == 1) "Retrait client" else "Transfert vers un client",
            if (mode == 0) "Le client vous remet des espèces, son compte est crédité et votre float baisse."
            else if (mode == 1) "Le client montre son code de retrait à 6 chiffres. Vous lui remettez les espèces."
            else "Vous envoyez de l'argent de votre float vers le compte d'un autre client.",
        )
        KField(phone, { phone = it }, "Numéro du client (Guinée)", hint = "Ex: 624 88 99 00")
        if (mode == 1) KField(code, { code = it }, "Code de retrait du client (6 chiffres)", number = true)
        key(round) {
            AmountPinForm(
                s, if (mode == 0) "Valider le dépôt client" else if (mode == 1) "Valider le retrait client" else "Envoyer au client",
                true, "Opération agent effectuée", onOk = { phone = ""; code = ""; round++ },
            ) { a -> doOp(a) }
        }
        Label("DERNIÈRES OPÉRATIONS")
        if (ag.log.isEmpty()) Text("Aucune opération.", color = p.sub, fontSize = 12.sp)
        ag.log.take(8).forEach { Text(it, color = p.text, fontSize = 12.sp) }
    }
}
