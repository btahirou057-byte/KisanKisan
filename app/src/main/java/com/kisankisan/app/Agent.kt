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
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

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
    val ctx = LocalContext.current
    var mode by remember { mutableIntStateOf(0) }
    var cName by remember { mutableStateOf("") }
    var cPhone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var round by remember { mutableIntStateOf(0) }
    var unlocked by remember { mutableStateOf(false) }
    var agentPin by remember { mutableStateOf("") }
    var gateErr by remember { mutableStateOf<String?>(null) }

    fun onScanned(raw: String) {
        if (raw.startsWith("kisankisan://")) {
            val u = Uri.parse(raw)
            cPhone = u.getQueryParameter("phone") ?: ""
            cName = u.getQueryParameter("name") ?: "Client"
        } else {
            s.msg = "QR non reconnu"
        }
    }

    fun doOp(a: Long): String? {
        if (cPhone.isBlank()) return "Scannez d'abord le QR du client."
        if (a <= 0L) return "Entrez un montant valide."
        val mine = cPhone.filter { it.isDigit() }.endsWith(PHONE.filter { it.isDigit() }.takeLast(9))
        when (mode) {
            0 -> {
                if (a > ag.floatBal) return "Float agent insuffisant."
                if (mine) { val e = s.deposit("Agent Kisan", a); if (e != null) return e }
                ag.floatBal -= a; ag.commission += a / 200
                ag.add("Dépôt ${a.gnf()} pour $cName")
            }
            1 -> {
                if (mine) {
                    if (code.length != 6 || code != s.lastCode) return "Code de retrait incorrect."
                    s.lastCode = ""
                }
                ag.floatBal += a; ag.commission += a / 200
                ag.add("Retrait ${a.gnf()} pour $cName")
            }
            else -> {
                if (a > ag.floatBal) return "Float agent insuffisant."
                if (mine) { val e = s.deposit("Transfert agent", a); if (e != null) return e }
                ag.floatBal -= a
                ag.add("Transfert ${a.gnf()} vers $cName")
            }
        }
        return null
    }

    KSheet("Espace Agent Kisan", Icons.Filled.SupportAgent, close) {
        if (!unlocked) {
            InfoBox("Accès réservé aux agents", "Entrez votre code agent pour continuer. (Démo : 0000)", Icons.Filled.Lock)
            KField(agentPin, { agentPin = it }, "Code agent (4 chiffres)", secret = true)
            gateErr?.let { Text(it, color = Color(0xFFEF4444), fontSize = 13.sp) }
            KButton("Ouvrir l'espace agent") { if (agentPin == "0000") { unlocked = true; gateErr = null } else gateErr = "Code agent incorrect." }
        } else {
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
                if (mode == 0) "Scannez le QR du client, puis entrez le montant reçu en espèces. Son compte est crédité et votre float baisse."
                else if (mode == 1) "Scannez le QR du client, puis demandez-lui son code de retrait à 6 chiffres. Vous lui remettez les espèces."
                else "Scannez le QR du client qui doit recevoir l'argent de votre float.",
            )
            if (cPhone.isBlank()) {
                KButton("Scanner le QR du client") {
                    GmsBarcodeScanning.getClient(ctx).startScan()
                        .addOnSuccessListener { b -> onScanned(b.rawValue ?: "") }
                        .addOnFailureListener { s.msg = "Scanner indisponible sur cet appareil" }
                }
                Text(
                    "Test sans second téléphone : utiliser mon propre compte (démo)", color = Sky, fontSize = 12.sp,
                    modifier = Modifier.clickable { cPhone = PHONE; cName = USER }.padding(vertical = 4.dp),
                )
            } else {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(p.card2).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(Icons.Filled.Person, Sky, 40); Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(cName, color = p.text, fontWeight = FontWeight.Bold)
                        Text(cPhone, color = p.sub, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text("Changer", color = Sky, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { cPhone = ""; cName = ""; code = "" })
                }
                if (mode == 1) KField(code, { code = it }, "Code de retrait du client (6 chiffres)", number = true)
                key(round) {
                    AmountPinForm(
                        s, if (mode == 0) "Valider le dépôt client" else if (mode == 1) "Valider le retrait client" else "Envoyer au client",
                        false, "Opération agent effectuée", onOk = { cPhone = ""; cName = ""; code = ""; round++ },
                    ) { a -> doOp(a) }
                }
            }
            Label("DERNIÈRES OPÉRATIONS")
            if (ag.log.isEmpty()) Text("Aucune opération.", color = p.sub, fontSize = 12.sp)
            ag.log.take(8).forEach { Text(it, color = p.text, fontSize = 12.sp) }
        }
    }
}
