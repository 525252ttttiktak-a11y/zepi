package com.zepi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

val BG = Color(0xFF0B0F1A); val PANEL = Color(0xFF121829); val PANEL2 = Color(0xFF182038)
val LINE = Color(0xFF26304D); val TXT = Color(0xFFE8ECF8); val MUTED = Color(0xFF8F9ABB)
val PALETTES = listOf(
    Color(0xFF4A7DFF) to Color(0xFF8A5CFF), Color(0xFF19C3A6) to Color(0xFF2F80ED),
    Color(0xFFFF5C8A) to Color(0xFFFF9F43), Color(0xFFA8E063) to Color(0xFF19C3A6))

enum class Screen(val icon: String) { Chat("✦"), History("◷"), Commands("☰"), Settings("⚙") }
data class Msg(val text: String, val me: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ZepiApp() }
    }
}

// TODO: подключи нейросеть и распознавание речи здесь
fun onSend(text: String): String = "Принято: «$text»"
fun onMic(listening: Boolean) {}

@Composable
fun ZepiApp() {
    var screen by remember { mutableStateOf(Screen.Chat) }
    var pal by remember { mutableIntStateOf(0) }
    var listening by remember { mutableStateOf(false) }
    var pro by remember { mutableStateOf(false) }
    val msgs = remember { mutableStateListOf(Msg("Zepi к вашим услугам.", false)) }
    val (acc, acc2) = PALETTES[pal]
    val grad = Brush.linearGradient(listOf(acc, acc2))

    Row(Modifier.fillMaxSize().background(BG).systemBarsPadding().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.width(48.dp).fillMaxHeight().clip(RoundedCornerShape(18.dp)).background(PANEL).padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Screen.values().forEach { s -> IconBtn(s.icon, screen == s, grad) { screen = s } }
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(26.dp).clip(CircleShape).background(grad).clickable { pal = (pal + 1) % PALETTES.size })
        }
        Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(PANEL)) {
            Row(Modifier.padding(14.dp, 12.dp, 14.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("ZEPI", color = TXT, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, fontSize = 18.sp, modifier = Modifier.weight(1f))
                Text(if (pro) "РЕЖИМ: ПРО" else "РЕЖИМ: БАЗА", color = acc, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.border(1.dp, acc, CircleShape).clip(CircleShape).clickable { pro = !pro }.padding(12.dp, 6.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Orb(listening, acc, acc2, Modifier.size(110.dp))
                Column(Modifier.padding(start = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(acc))
                        Text(if (listening) "Слушаю" else "Отдыхаю", color = MUTED, modifier = Modifier.padding(start = 8.dp))
                    }
                    Text("CODEX", color = MUTED, fontSize = 11.sp, letterSpacing = 4.sp)
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp)) {
                when (screen) {
                    Screen.Chat -> ChatView(msgs, grad, acc)
                    Screen.History -> Hint("История разговоров появится здесь.")
                    Screen.Commands -> CommandsView(acc)
                    Screen.Settings -> SettingsView(pal, acc) { pal = it }
                }
            }
            InputBar(grad, listening, { listening = !listening; onMic(listening) }) { t ->
                msgs.add(Msg(t, true)); msgs.add(Msg(onSend(t), false)); screen = Screen.Chat
            }
        }
    }
}

@Composable
fun IconBtn(icon: String, on: Boolean, grad: Brush, onClick: () -> Unit) {
    val m = Modifier.size(38.dp).clip(RoundedCornerShape(13.dp))
    Box((if (on) m.background(grad) else m.background(PANEL2)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(icon, color = TXT, fontSize = 18.sp)
    }
}

@Composable
fun Hint(t: String) = Text(t, color = MUTED, modifier = Modifier.padding(top = 12.dp))

@Composable
fun Orb(listening: Boolean, acc: Color, acc2: Color, modifier: Modifier) {
    val tr = rememberInfiniteTransition(label = "orb")
    val rot by tr.animateFloat(0f, 360f, infiniteRepeatable(tween(if (listening) 6000 else 14000, easing = LinearEasing)), label = "r")
    val pulse by tr.animateFloat(0.94f, 1.06f,
        infiniteRepeatable(tween(if (listening) 700 else 2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "p")
    Canvas(modifier) {
        val r = size.minDimension / 2 * 0.78f * pulse
        val c = center
        drawCircle(Brush.radialGradient(listOf(acc.copy(alpha = 0.35f), Color.Transparent), c, r * 1.4f), r * 1.4f, c)
        val st = Stroke(1.6f)
        for (k in 0 until 8) {
            val a = Math.toRadians((rot + k * 22.5)).toFloat()
            val w = abs(cos(a)) * r
            drawOval(acc, Offset(c.x - w, c.y - r), Size(w * 2, r * 2), style = st)
        }
        for (i in -3..3) {
            val y = i * r / 4f
            val rr = sqrt(r * r - y * y)
            val h = rr * 0.28f
            drawOval(acc2, Offset(c.x - rr, c.y + y - h), Size(rr * 2, h * 2), style = st)
        }
    }
}

@Composable
fun ChatView(msgs: List<Msg>, grad: Brush, acc: Color) {
    Column {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(msgs) { m ->
                Box(Modifier.fillMaxWidth(), contentAlignment = if (m.me) Alignment.CenterEnd else Alignment.CenterStart) {
                    val b = Modifier.widthIn(max = 280.dp).clip(RoundedCornerShape(16.dp))
                    Text(m.text, color = TXT, fontSize = 14.sp,
                        modifier = (if (m.me) b.background(grad) else b.background(PANEL2)).padding(12.dp, 9.dp))
                }
            }
        }
        QuickSettings(acc)
    }
}

@Composable
fun SwitchRow(label: String, init: Boolean, acc: Color) {
    var on by remember { mutableStateOf(init) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TXT, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(on, { on = it }, colors = SwitchDefaults.colors(checkedTrackColor = acc))
    }
}

@Composable
fun QuickSettings(acc: Color) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).border(1.dp, LINE, RoundedCornerShape(16.dp)).padding(12.dp)) {
        Text("БЫСТРЫЕ НАСТРОЙКИ", color = MUTED, fontSize = 11.sp, letterSpacing = 2.sp)
        SwitchRow("Отклик только на «Zepi»", true, acc)
        SwitchRow("Озвучивать ответы", true, acc)
        SwitchRow("Слушать в фоне", false, acc)
    }
}

@Composable
fun CommandsView(acc: Color) {
    val list = remember { mutableStateListOf("Открыть YouTube", "Включить фонарик") }
    var new by remember { mutableStateOf("") }
    Column(Modifier.padding(top = 8.dp)) {
        Text("КОМАНДЫ", color = TXT, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(new, { new = it }, textStyle = TextStyle(color = TXT), cursorBrush = SolidColor(acc),
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(PANEL2).padding(12.dp))
            Text("＋", color = acc, fontSize = 24.sp, modifier = Modifier.padding(start = 10.dp)
                .clickable { if (new.isNotBlank()) { list.add(new.trim()); new = "" } })
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list) { Text(it, color = TXT, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PANEL2).padding(12.dp)) }
        }
    }
}

@Composable
fun SettingsView(pal: Int, acc: Color, setPal: (Int) -> Unit) {
    val tabs = listOf("Голос", "Нейросеть", "Дизайн", "Информация")
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.padding(top = 8.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEachIndexed { i, t ->
                val m = Modifier.clip(CircleShape)
                Text(t, color = if (i == tab) Color.White else MUTED, fontSize = 13.sp,
                    modifier = (if (i == tab) m.background(acc) else m.border(1.dp, LINE, CircleShape)).clickable { tab = i }.padding(14.dp, 8.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        when (tab) {
            0 -> { SwitchRow("Голосовой ответ", true, acc); SwitchRow("Голос: Romul", false, acc) }
            1 -> { SwitchRow("Помнить контекст", true, acc); SwitchRow("Умная модель", false, acc) }
            2 -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PALETTES.forEachIndexed { i, p ->
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(listOf(p.first, p.second)))
                        .border(if (i == pal) 3.dp else 0.dp, TXT, CircleShape).clickable { setPal(i) })
                }
            }
            else -> Text("Zepi 0.1\nИнтерфейс на Jetpack Compose.", color = MUTED, lineHeight = 22.sp)
        }
    }
}

@Composable
fun InputBar(grad: Brush, listening: Boolean, onMic: () -> Unit, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().padding(10.dp).clip(RoundedCornerShape(16.dp)).background(PANEL2).padding(6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("⚡", color = TXT, modifier = Modifier.padding(horizontal = 6.dp))
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) Text("Введите запрос…", color = MUTED, fontSize = 14.sp)
            BasicTextField(text, { text = it }, textStyle = TextStyle(color = TXT, fontSize = 14.sp), cursorBrush = SolidColor(TXT), singleLine = true)
        }
        Text("🎙", fontSize = 18.sp, modifier = Modifier.clip(CircleShape).background(if (listening) PANEL else Color.Transparent).clickable(onClick = onMic).padding(6.dp))
        Text("➤", color = Color.White, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(grad)
            .clickable { if (text.isNotBlank()) { onSend(text.trim()); text = "" } }.padding(12.dp, 8.dp))
    }
}
