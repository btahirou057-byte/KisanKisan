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

/* ---------- dépôt / retrait ---------- */

@Composable
fun ModeBox(text: String, icon: ImageVector, sel: Boolean, mod: Modifier, onClick: () -> Unit) {
    val p = LocalPal.current
    Column(mod.clip(RoundedCornerShape(12.dp)).background(p.card2).border(1.dp, if (sel) Blue else p.border, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (sel) Sky else p.sub); Text(text, color = if (sel) p.text else p.sub, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun DepositSheet(s: AppState, close: () -> Unit) {
    KSheet("Dépôt d'argent", Icons.Filled.Download, close) {
        InfoBox("Le dépôt se fait chez un agent Kisan Kisan", "Rendez-vous chez un agent agréé avec votre numéro $PHONE ou présentez votre QR Code. L'agent dépose l'argent sur votre compte. Les dépôts sont 100% gratuits sans commission.", Icons.Filled.Storefront)
        InfoBox("", "Vous ne pouvez pas créditer votre compte vous-même : seul un agent peut déposer de l'argent pour vous.")
        KButton("J'ai compris", onClick = close)
    }
}

@Composable
fun WithdrawSheet(s: AppState, close: () -> Unit) {
    val p = LocalPal.current
    var code by remember { mutableStateOf<String?>(null) }
    KSheet("Retirer de l'argent", Icons.Filled.Upload, close) {
        if (code != null) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Votre code de retrait", color = p.sub); Text(code!!.chunked(3).joinToString(" "), color = Green, fontSize = 36.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Valable 15 minutes. Donnez ce code à l'agent pour recevoir vos espèces.", color = p.sub, fontSize = 12.sp)
            }
            KButton("Terminer", onClick = close)
        } else {
            BalanceRow(s)
            InfoBox("Retrait chez un agent", "Après confirmation de votre code PIN, un code à 6 chiffres valable 15 minutes est créé et le montant est mis de côté. L'agent vous remet les espèces quand vous lui donnez ce code.", Icons.Filled.Schedule)
            AmountPinForm(s, "Générer le code de retrait", true, "Code de retrait généré", "Montant à retirer (GNF)", onOk = { code = s.lastCode }) { a -> s.withdraw("Agent Kiosque", a) }
        }
    }
}
/* ---------- scanner ---------- */

@Composable
fun ScanSheet(s: AppState, open: (Sheet) -> Unit, close: () -> Unit) {
    val p = LocalPal.current
    val ctx = LocalContext.current
    KSheet("Scanner un QR Code", Icons.Filled.QrCodeScanner, close) {
        Box(Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF05080F)), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(190.dp)) {
                val c = Color(0xFF4C8DFF); val l = 40f; val w = 8f
                fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                    drawLine(c, Offset(x, y), Offset(x + dx * l, y), w, StrokeCap.Round)
                    drawLine(c, Offset(x, y), Offset(x, y + dy * l), w, StrokeCap.Round)
                }
                corner(0f, 0f, 1f, 1f); corner(size.width, 0f, -1f, 1f); corner(0f, size.height, 1f, -1f); corner(size.width, size.height, -1f, -1f)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CameraAlt, null, tint = Color.Gray, modifier = Modifier.size(34.dp))
                Text("Pointez vers le code QR Kisan Kisan", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Commerçant, ami ou facture", color = Color.Gray, fontSize = 11.sp)
            }
        }
        Text("Alignez le QR code dans le cadre. La détection et la redirection sont automatiques.", color = p.sub, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        KButton("Ouvrir la caméra") {
            GmsBarcodeScanning.getClient(ctx).startScan()
                .addOnSuccessListener { b ->
                    val raw = b.rawValue ?: ""
                    if (raw.startsWith("kisankisan://")) {
                        val u = Uri.parse(raw)
                        open(Sheet.Transfer(Contact(u.getQueryParameter("name") ?: "Bénéficiaire", u.getQueryParameter("phone") ?: "", false)))
                    } else s.msg = "Code non reconnu"
                }
                .addOnFailureListener { s.msg = "Scanner indisponible sur cet appareil" }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Label("CODES DE DÉMONSTRATION RAPIDES", modifier = Modifier.weight(1f)); Label("1 clic pour scanner", Sky)
        }
        val demos = listOf(Triple("Supermarché Prima Kipé", 85_000L, Icons.Filled.Storefront), Triple("Boutique Mode Kouléoun", 140_000L, Icons.Filled.Storefront),
            Triple("Fatoumata", 0L, Icons.Filled.Person), Triple("Restaurant Le Cèdre", 60_000L, Icons.Filled.Storefront))
        demos.chunked(2).forEach { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                r.forEach { (n, a, ic) ->
                    Row(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(p.card2).clickable {
                        if (a > 0) open(Sheet.Merchant(n, a)) else open(Sheet.Transfer(s.contacts.first()))
                    }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(ic, null, tint = Sky, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(6.dp))
                        Column { Text(n, color = p.text, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1); Text(if (a > 0) a.gnf() else "Payer un proche", color = Green, fontSize = 11.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantSheet(s: AppState, name: String, amount: Long, close: () -> Unit) {
    KSheet("Payer un commerçant", Icons.Filled.Storefront, close) {
        InfoBox(name, "Paiement QR sécurisé · Frais 0 GNF", Icons.Filled.Storefront)
        BalanceRow(s)
        AmountPinForm(s, "Payer", true, "Paiement effectué", initial = amount, onOk = close) { a -> s.payMerchant(name, a) }
    }
}

@Composable
fun BillSheet(s: AppState, name: String, close: () -> Unit) {
    var ref by remember { mutableStateOf("") }
    val svc = BILLS.firstOrNull { it.n == name }
    KSheet("Payer · $name", svc?.icon ?: Icons.Filled.ReceiptLong, close) {
        InfoBox(name, (svc?.d ?: "") + " · Frais 0 GNF", svc?.icon ?: Icons.Filled.ReceiptLong, svc?.c ?: Sky)
        BalanceRow(s)
        KField(ref, { ref = it }, "Numéro de compteur / abonné")
        AmountPinForm(s, "Payer la facture", true, "Facture payée", onOk = close) { a -> if (ref.isBlank()) "Entrez la référence." else s.payBill(name, ref, a) }
    }
}
