package com.kisankisan.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Blue = Color(0xFF1636B4)
val Sky = Color(0xFF4C8DFF)
val Green = Color(0xFF22C55E)
val Orange = Color(0xFFF59E0B)
val Purple = Color(0xFFA855F7)
const val LIMIT = 15_000_000L
var PHONE = "+224 624 88 99 00"
var USER = "Mamadou Tahirou Bah"

fun userInitials() = USER.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1).uppercase() }

class Pal(val bg: Color, val card: Color, val card2: Color, val border: Color, val text: Color, val sub: Color)

fun pal(dark: Boolean) =
    if (dark) Pal(Color(0xFF000E47), Color(0xFF0A1A5C), Color(0xFF122670), Color(0xFF21388A), Color(0xFFEAF0FF), Color(0xFF9AA9D6))
    else Pal(Color(0xFFEFF2F6), Color.White, Color(0xFFEAEEF6), Color(0xFFD9DFEC), Color(0xFF00114E), Color(0xFF5C6890))

val LocalPal = staticCompositionLocalOf { pal(true) }

fun Long.num() = kotlin.math.abs(this).toString().reversed().chunked(3).joinToString("\u00A0").reversed()
fun Long.gnf() = num() + "\u00A0GNF"

fun qr(s: String): ImageBitmap {
    val n = 400
    val m = QRCodeWriter().encode(s, BarcodeFormat.QR_CODE, n, n, mapOf<EncodeHintType, Any>(EncodeHintType.MARGIN to 1))
    val b = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
    for (x in 0 until n) for (y in 0 until n) b.setPixel(x, y, if (m.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
    return b.asImageBitmap()
}

class Contact(val name: String, val phone: String, val fav: Boolean)
class Tx(val id: Long, val title: String, val sub: String, val time: String, val amount: Long, val type: String)

val Contact.initials: String
    get() = name.trim().split(" ").filter { it.isNotEmpty() }.let { w -> (w.first().take(1) + (if (w.size > 1) w.last().take(1) else "")).uppercase() }

fun category(t: Tx) = when (t.type) {
    "withdraw" -> "Retrait"
    "vault" -> "Coffre-fort"
    "transfer" -> "Transfert"
    "bill" -> "Factures"
    "merchant" -> if (t.sub.contains("QR")) "Supermarché" else "Courses & Alimentation"
    else -> "Autres"
}

private val EN = mapOf(
    "Accueil" to "Home", "Historique" to "History", "Profil" to "Profile", "Bienvenue" to "Welcome",
    "Transfert" to "Transfer", "Scanner" to "Scan", "Dépôt" to "Deposit", "Retrait" to "Withdraw",
    "Solde principal disponible" to "Available main balance", "Dernières transactions" to "Latest transactions",
    "Voir tout" to "See all", "Bénéficiaires fréquents" to "Frequent recipients", "Paramètres" to "Settings",
)

object Notifier {
    fun alert(ctx: Context, text: String) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("kk", "Alertes Kisan Kisan", NotificationManager.IMPORTANCE_DEFAULT))
        if (Build.VERSION.SDK_INT >= 33 && ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val n = NotificationCompat.Builder(ctx, "kk").setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Kisan Kisan").setContentText(text).setAutoCancel(true).build()
        try { NotificationManagerCompat.from(ctx).notify(1, n) } catch (e: SecurityException) { }
    }
}

fun bioAvailable(a: FragmentActivity) =
    BiometricManager.from(a).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

fun askBio(a: FragmentActivity, onOk: () -> Unit) {
    if (!bioAvailable(a)) { onOk(); return }
    val cb = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onOk() }
    }
    val info = BiometricPrompt.PromptInfo.Builder().setTitle("Kisan Kisan").setSubtitle("Confirmez avec votre empreinte")
        .setNegativeButtonText("Annuler").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK).build()
    BiometricPrompt(a, ContextCompat.getMainExecutor(a), cb).authenticate(info)
}

class AppState(private val ctx: Context) {
    private val sp = ctx.getSharedPreferences("kk", Context.MODE_PRIVATE)
    var dark by mutableStateOf(true)
    var hide by mutableStateOf(false)
    var balance by mutableLongStateOf(2_255_000L)
    var vault by mutableLongStateOf(950_000L)
    var daily by mutableLongStateOf(745_000L)
    var threshold by mutableLongStateOf(200_000L)
    var alertOn by mutableStateOf(true)
    var bio by mutableStateOf(false)
    var lang by mutableStateOf("Français")
    var pin by mutableStateOf("1234")
    var unread by mutableIntStateOf(1)
    var msg by mutableStateOf<String?>(null)
    var lastCode by mutableStateOf("")
    var registered by mutableStateOf(false)
    var userName by mutableStateOf("")
    var userPhone by mutableStateOf("")
    val txs = mutableStateListOf<Tx>()
    val contacts = mutableStateListOf<Contact>()
    val notifs = mutableStateListOf<String>()

    init {
        if (!load()) reset()
        if (registered && userName.isNotBlank()) { USER = userName; PHONE = userPhone }
    }

    fun register(name: String, phone: String, newPin: String) {
        userName = name; userPhone = phone; pin = newPin; registered = true
        USER = name; PHONE = phone
        save()
    }

    fun t(fr: String) = if (lang == "Anglais") (EN[fr] ?: fr) else fr

    fun reset() {
        dark = true; hide = false; balance = 2_255_000L; vault = 950_000L; daily = 745_000L
        threshold = 200_000L; alertOn = true; bio = false; pin = if (registered) pin else "1234"; unread = 1; lang = "Français"
        txs.clear()
        txs.addAll(listOf(
            Tx(14, "Agent Kiosque Agréé", "Retrait d'argent", "12 nov., 12:50", -100_000L, "withdraw"),
            Tx(13, "Carte Bancaire", "Dépôt d'argent", "12 nov., 12:50", 50_000L, "deposit"),
            Tx(12, "Coffre (Projet Personnel)", "Verrouillé au Coffre", "12 nov., 12:48", -100_000L, "vault"),
            Tx(11, "Fatoumata Binta Diallo", "Transfert d'argent", "28 oct., 12:26", -10_000L, "transfer"),
            Tx(10, "Fatoumata Binta Diallo", "Transfert d'argent", "28 oct., 12:24", -100_000L, "transfer"),
            Tx(9, "Agent Kiosque Agréé", "Retrait d'argent", "28 oct., 12:21", -100_000L, "withdraw"),
            Tx(8, "Supermarché Prima Kipé", "Paiement QR Commerçant", "28 oct., 12:21", -85_000L, "merchant"),
            Tx(7, "Agent Kisan (KK-AGN-0412)", "Dépôt d'argent", "28 oct., 12:20", 250_000L, "deposit"),
            Tx(6, "Fatoumata Binta Diallo", "Transfert d'argent", "28 oct., 11:43", -150_000L, "transfer"),
            Tx(5, "Supermarché Prima Kipé", "Paiement Commerçant", "27 oct., 22:08", -125_000L, "merchant"),
            Tx(4, "Agent Kisan Madina", "Dépôt d'argent", "26 oct., 18:08", 500_000L, "deposit"),
            Tx(3, "EDG (Électricité de Guinée)", "Facture Électricité", "25 oct., 16:08", -75_000L, "bill"),
            Tx(2, "Alpha Oumar Camara", "Transfert reçu", "24 oct., 16:08", 300_000L, "received"),
            Tx(1, "Guichet Kiosque Kaloum", "Retrait d'argent", "23 oct., 12:08", -200_000L, "withdraw"),
        ))
        contacts.clear()
        contacts.addAll(listOf(
            Contact("Fatoumata Binta Diallo", "+224 621 12 34 56", true),
            Contact("Alpha Oumar Camara", "+224 628 45 67 89", true),
            Contact("Mariama Barry", "+224 622 33 44 55", false),
            Contact("Ibrahima Sow", "+224 664 11 22 33", false),
        ))
        notifs.clear()
        notifs.add("Bienvenue sur Kisan Kisan. Réglez votre alerte de solde bas dans les paramètres.")
        save()
    }

    fun save() {
        val o = JSONObject()
        o.put("dark", dark).put("hide", hide).put("bal", balance).put("vault", vault).put("daily", daily)
            .put("thr", threshold).put("alert", alertOn).put("bio", bio).put("lang", lang).put("pin", pin).put("unread", unread).put("reg", registered).put("un", userName).put("up", userPhone)
        val ta = JSONArray()
        txs.forEach { ta.put(JSONObject().put("i", it.id).put("t", it.title).put("s", it.sub).put("w", it.time).put("a", it.amount).put("y", it.type)) }
        o.put("txs", ta)
        val ca = JSONArray()
        contacts.forEach { ca.put(JSONObject().put("n", it.name).put("p", it.phone).put("f", it.fav)) }
        o.put("contacts", ca)
        val na = JSONArray()
        notifs.forEach { na.put(it) }
        o.put("notifs", na)
        sp.edit().putString("s", o.toString()).apply()
    }

    private fun load(): Boolean {
        try {
            val raw = sp.getString("s", null) ?: return false
            val o = JSONObject(raw)
            dark = o.getBoolean("dark"); hide = o.getBoolean("hide"); balance = o.getLong("bal"); vault = o.getLong("vault")
            daily = o.getLong("daily"); threshold = o.getLong("thr"); alertOn = o.getBoolean("alert"); bio = o.getBoolean("bio")
            lang = o.getString("lang"); pin = o.getString("pin"); unread = o.getInt("unread")
            registered = o.optBoolean("reg", false); userName = o.optString("un", ""); userPhone = o.optString("up", "")
            txs.clear()
            val ta = o.getJSONArray("txs")
            for (i in 0 until ta.length()) {
                val x = ta.getJSONObject(i)
                txs.add(Tx(x.getLong("i"), x.getString("t"), x.getString("s"), x.getString("w"), x.getLong("a"), x.getString("y")))
            }
            contacts.clear()
            val ca = o.getJSONArray("contacts")
            for (i in 0 until ca.length()) {
                val x = ca.getJSONObject(i)
                contacts.add(Contact(x.getString("n"), x.getString("p"), x.getBoolean("f")))
            }
            notifs.clear()
            val na = o.getJSONArray("notifs")
            for (i in 0 until na.length()) notifs.add(na.getString(i))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun now() = SimpleDateFormat("d MMM, HH:mm", Locale.FRENCH).format(Date())
    private fun nid() = (txs.maxOfOrNull { it.id } ?: 0L) + 1

    fun notify(text: String, system: Boolean) {
        notifs.add(0, text); unread++
        if (system) Notifier.alert(ctx, text)
        save()
    }

    fun readAll() { unread = 0; save() }

    fun checkDebit(a: Long): String? = when {
        a <= 0L -> "Entrez un montant valide."
        a > balance -> "Solde insuffisant."
        daily + a > LIMIT -> "Plafond journalier dépassé."
        else -> null
    }

    private fun record(title: String, sub: String, delta: Long, type: String) {
        balance += delta
        if (delta < 0 && type != "vault") daily -= delta
        txs.add(0, Tx(nid(), title, sub, now(), delta, type))
        if (delta < 0 && alertOn && balance < threshold) notify("Solde bas : il vous reste ${balance.gnf()}", true)
        save()
    }

    fun transfer(to: String, a: Long): String? = checkDebit(a) ?: run { record(to, "Transfert d'argent", -a, "transfer"); null }
    fun deposit(mode: String, a: Long): String? {
        if (a <= 0L) return "Entrez un montant valide."
        record(if (mode.startsWith("Agent")) "Agent Kisan" else mode, "Dépôt d'argent", a, "deposit"); return null
    }
    fun withdraw(mode: String, a: Long): String? {
        val e = checkDebit(a)
        if (e != null) return e
        lastCode = (100000..999999).random().toString()
        record(if (mode.startsWith("Agent")) "Agent Kiosque Agréé" else "Borne Kisan", "Retrait d'argent", -a, "withdraw"); return null
    }
    fun payBill(name: String, ref: String, a: Long): String? = checkDebit(a) ?: run { record(name, "Facture · réf. $ref", -a, "bill"); null }
    fun payMerchant(name: String, a: Long): String? = checkDebit(a) ?: run { record(name, "Paiement QR Commerçant", -a, "merchant"); null }
    fun toVault(a: Long): String? {
        if (a <= 0L) return "Entrez un montant valide."
        if (a > balance) return "Solde insuffisant."
        vault += a; record("Coffre (Projet Personnel)", "Verrouillé au Coffre", -a, "vault"); return null
    }
    fun fromVault(a: Long): String? {
        if (a <= 0L) return "Entrez un montant valide."
        if (a > vault) return "Montant supérieur au coffre."
        vault -= a; record("Coffre vers solde principal", "Retiré du Coffre", a, "deposit"); return null
    }
    fun toggleFav(c: Contact) {
        val i = contacts.indexOf(c)
        if (i >= 0) { contacts[i] = Contact(c.name, c.phone, !c.fav); save() }
    }
    fun addContact(name: String, phone: String) { contacts.add(Contact(name, phone, false)); save() }
}

class MainActivity : FragmentActivity() {
    private val perm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            perm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            val s = remember { AppState(applicationContext) }
            var locked by remember { mutableStateOf(s.bio) }
            val p = pal(s.dark)
            MaterialTheme(colorScheme = if (s.dark) darkColorScheme(primary = Blue) else lightColorScheme(primary = Blue)) {
                CompositionLocalProvider(LocalPal provides p) {
                    if (!s.registered) {
                        Onboarding(s)
                    } else if (locked) {
                        LaunchedEffect(Unit) { askBio(this@MainActivity) { locked = false } }
                        Column(Modifier.fillMaxSize().background(p.bg).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Lock, null, tint = Blue, modifier = Modifier.size(64.dp))
                            Text("Kisan Kisan verrouillé", color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                            Button(onClick = { askBio(this@MainActivity) { locked = false } }) { Text("Déverrouiller") }
                        }
                    } else App(s)
                }
            }
        }
    }
}
