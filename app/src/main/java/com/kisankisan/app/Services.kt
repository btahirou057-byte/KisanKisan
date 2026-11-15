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

/* ---------- services ---------- */

@Composable
fun Services(open: (Sheet) -> Unit) {
    val p = LocalPal.current
    Text("Hub de Services", color = p.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Text("Tous vos paiements et opérations financières en Guinée", color = p.sub, fontSize = 13.sp)
    KCard {
        Label("OPÉRATIONS D'ARGENT"); Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf(Triple("Transfert", Icons.Filled.Send, Sheet.Transfer()), Triple("Scanner", Icons.Filled.QrCodeScanner, Sheet.Scan),
                Triple("Dépôt", Icons.Filled.Download, Sheet.Deposit), Triple("Retrait", Icons.Filled.Upload, Sheet.Withdraw)).forEach { (l, i, sh) ->
                Column(Modifier.weight(1f).clickable { open(sh) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    IconBubble(i, Sky, 48); Text(l, color = p.text, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) { Label("FACTURES & UTILITAIRES"); Spacer(Modifier.weight(1f)); Label("Frais 0 GNF", Green) }
        Spacer(Modifier.height(10.dp))
        BILLS.chunked(2).forEach { r ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                r.forEach { b ->
                    Row(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(p.card2).clickable { open(Sheet.Bill(b.n)) }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(b.icon, b.c, 36); Spacer(Modifier.width(8.dp))
                        Column { Text(b.n, color = p.text, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(b.d, color = p.sub, fontSize = 11.sp) }
                    }
                }
            }
        }
    }
    Label("ÉPARGNE & PROJETS")
    KCard(click = { open(Sheet.Vault) }, border = Green.copy(alpha = .5f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Filled.Savings, Green, 44); Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { Text("Coffre Kisan Kisan", color = p.text, fontWeight = FontWeight.Bold); Text("Mettez de l'argent de côté à l'abri des dépenses quotidiennes", color = Green, fontSize = 12.sp) }
            Icon(Icons.Filled.ChevronRight, null, tint = Green)
        }
    }
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Filled.Groups, Purple, 44); Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tontine Numérique Familiale", color = p.text, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, false))
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Purple.copy(alpha = .3f)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("Bientôt", color = Purple, fontSize = 10.sp) }
                }
                Text("Cotisations entre proches avec tirage automatique", color = p.sub, fontSize = 12.sp)
            }
        }
    }
    KCard {
        Row(verticalAlignment = Alignment.CenterVertically) { Label("COMMERÇANTS PARTENAIRES (CONAKRY)", modifier = Modifier.weight(1f)); Label("Payer en 1 scan", Sky) }
        MERCHANTS.forEach { (n, d) ->
            Row(Modifier.fillMaxWidth().clickable { open(Sheet.Merchant(n)) }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.Storefront, Sky, 36); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text(n, color = p.text, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(d, color = p.sub, fontSize = 11.sp) }
                Icon(Icons.Filled.ChevronRight, null, tint = p.sub)
            }
        }
    }
}
