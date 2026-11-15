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

sealed interface Sheet {
    data object None : Sheet
    data class Transfer(val pre: Contact? = null) : Sheet
    data object Deposit : Sheet
    data object Withdraw : Sheet
    data object Scan : Sheet
    data object MyQr : Sheet
    data object Settings : Sheet
    data object Notifs : Sheet
    data object Vault : Sheet
    data object AddContact : Sheet
    data class Bill(val name: String) : Sheet
    data class Merchant(val name: String, val amount: Long = 0L) : Sheet
    data class Receipt(val tx: Tx) : Sheet
}

@Composable
fun SheetHost(sh: Sheet, s: AppState, open: (Sheet) -> Unit, close: () -> Unit) {
    when (sh) {
        Sheet.None -> {}
        is Sheet.Transfer -> TransferSheet(s, sh.pre, close)
        Sheet.Deposit -> DepositSheet(s, close)
        Sheet.Withdraw -> WithdrawSheet(s, close)
        Sheet.Scan -> ScanSheet(s, open, close)
        Sheet.MyQr -> MyQrSheet(s, close)
        Sheet.Settings -> SettingsSheet(s, close)
        Sheet.Notifs -> NotifsSheet(s, close)
        Sheet.Vault -> VaultSheet(s, close)
        Sheet.AddContact -> AddContactSheet(s, close)
        is Sheet.Bill -> BillSheet(s, sh.name, close)
        is Sheet.Merchant -> MerchantSheet(s, sh.name, sh.amount, close)
        is Sheet.Receipt -> ReceiptSheet(sh.tx, close)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KSheet(title: String, icon: ImageVector, close: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPal.current
    ModalBottomSheet(
        onDismissRequest = close, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = p.card, dragHandle = null, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth().background(Blue).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
            IconButton(close) { Icon(Icons.Filled.Close, null, tint = Color.White) }
        }
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun BalanceRow(s: AppState) {
    val p = LocalPal.current
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Blue.copy(alpha = .5f), RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Solde actuel disponible :", color = p.sub, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(s.balance.gnf(), color = p.text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun InfoBox(title: String, text: String, icon: ImageVector = Icons.Filled.Shield, tint: Color = Sky) {
    val p = LocalPal.current
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(p.card2).padding(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
        Column { if (title.isNotEmpty()) Text(title, color = p.text, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(text, color = p.sub, fontSize = 12.sp) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.AmountPinForm(
    s: AppState, button: String, needPin: Boolean, ok: String, label: String = "Montant (GNF)",
    quick: List<Long> = listOf(50_000L, 100_000L, 200_000L, 500_000L, 1_000_000L), initial: Long = 0L,
    onOk: () -> Unit, submit: (Long) -> String?,
) {
    val p = LocalPal.current
    var amount by remember { mutableStateOf(if (initial > 0) initial.toString() else "") }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    KField(amount, { amount = it }, label, number = true, suffix = "GNF")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        quick.forEach { v -> Chip(v.num(), amount == v.toString()) { amount = v.toString() } }
    }
    if (needPin) KField(pin, { pin = it }, "Code PIN (4 chiffres)", secret = true)
    error?.let { Text(it, color = Color(0xFFEF4444), fontSize = 13.sp) }
    KButton(button) {
        if (needPin && pin != s.pin) { error = "Code PIN incorrect."; return@KButton }
        val e = submit(amount.toLongOrNull() ?: 0L)
        if (e != null) error = e else { s.msg = ok; onOk() }
    }
}
/* ---------- transfert ---------- */

@Composable
fun TransferSheet(s: AppState, pre: Contact?, close: () -> Unit) {
    val p = LocalPal.current
    var phone by remember { mutableStateOf(pre?.phone ?: "") }
    var name by remember { mutableStateOf(pre?.name ?: "") }
    var q by remember { mutableStateOf("") }
    KSheet("Transfert d'argent", Icons.Filled.Send, close) {
        BalanceRow(s)
        if (s.contacts.any { it.fav }) KCard(border = Orange.copy(alpha = .5f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, null, tint = Orange, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                Text("Contacts Favoris", color = Orange, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("Touchez ★ pour épingler", color = Orange, fontSize = 10.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                s.contacts.filter { it.fav }.forEach { c ->
                    Column(Modifier.clip(RoundedCornerShape(12.dp)).background(p.card2).clickable { phone = c.phone; name = c.name }.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Avatar(c, s.contacts.indexOf(c), 44); Text(c.name.substringBefore(" "), color = p.text, fontSize = 11.sp)
                    }
                }
            }
        }
        KField(phone, { phone = it }, "Numéro du bénéficiaire (Guinée)", hint = "Ex: 624 88 99 00 ou +224 6...")
        KField(name, { name = it }, "Nom du bénéficiaire (optionnel)", hint = "Ex: Fatoumata Diallo")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tous les contacts enregistrés", color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Groups, null, tint = Sky)
        }
        KField(q, { q = it }, "Filtrer les contacts...")
        s.contacts.filter { q.isBlank() || it.name.contains(q, true) || it.phone.contains(q) }.forEach { c ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(p.card2).clickable { phone = c.phone; name = c.name }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(c, s.contacts.indexOf(c), 36); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text(c.name, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text(c.phone, color = p.sub, fontSize = 11.sp) }
                Icon(if (c.fav) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = Orange, modifier = Modifier.clickable { s.toggleFav(c) })
            }
        }
        AmountPinForm(s, "Envoyer l'argent", true, "Transfert effectué", onOk = close) { a ->
            if (phone.isBlank() && name.isBlank()) "Choisissez un bénéficiaire." else s.transfer(name.ifBlank { phone }, a)
        }
    }
}
