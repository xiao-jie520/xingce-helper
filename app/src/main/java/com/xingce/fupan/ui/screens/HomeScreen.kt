package com.xingce.fupan.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingce.fupan.data.Analyzer
import com.xingce.fupan.data.PaperRecord
import com.xingce.fupan.ui.components.AboutDialog
import com.xingce.fupan.ui.components.AcrylicCard
import com.xingce.fupan.ui.components.AcrylicIconButton
import com.xingce.fupan.ui.components.EmptyStateCard
import com.xingce.fupan.ui.components.InsightRow
import com.xingce.fupan.ui.components.PillChip
import com.xingce.fupan.ui.components.ProgressRing
import com.xingce.fupan.ui.components.Reveal
import com.xingce.fupan.ui.components.SectionBarRow
import com.xingce.fupan.ui.components.SectionTitle
import com.xingce.fupan.ui.components.StatTile
import com.xingce.fupan.ui.components.TrendChart
import com.xingce.fupan.ui.components.accentGradient
import com.xingce.fupan.ui.theme.LocalFluent
import com.xingce.fupan.util.Fmt
import kotlin.math.abs

@Composable
fun HomeScreen(
    records: List<PaperRecord>,
    onOpenHistory: () -> Unit,
    onAdd: () -> Unit,
    onOpenReport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    val latest = records.firstOrNull()
    val previous = remember(latest?.id, records.size) {
        latest?.let { Analyzer.previousOf(it, records) }
    }
    val insights = remember(latest?.id, records.size) {
        latest?.let { Analyzer.insights(it, records) }.orEmpty()
    }
    val chronological = remember(records.size) { Analyzer.overallTrend(records) }

    val scroll = rememberScrollState()
    var showAbout by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0.0"
    }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(latest?.id) { started = true }
    val accAnim by animateFloatAsState(
        targetValue = if (started) (latest?.accuracy ?: 0f) else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "heroAcc"
    )

    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = { showAbout = true })
                        }
                ) {
                    Text(
                        text = "行测复盘",
                        color = p.textPrimary,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (records.isEmpty()) "把每一套卷子都变成经验" else "共 ${records.size} 次模考 · 最近更新 ${Fmt.dateShort(latest!!.dateMillis)}",
                        color = p.textTertiary,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                AcrylicIconButton(
                    icon = Icons.Rounded.History,
                    onClick = onOpenHistory,
                    contentDescription = "历史试卷"
                )
            }

            Spacer(Modifier.height(18.dp))

            if (latest == null) {
                Reveal(0) {
                    EmptyStateCard(
                        title = "还没有模考记录",
                        description = "点右下角的 + 号，选一套试卷模板，填进各板块的对错题数，立刻得到成绩报告。",
                        modifier = Modifier.fillMaxWidth(),
                        badges = listOf("国考", "省考", "事业单位")
                    )
                }
                Spacer(Modifier.height(14.dp))
                Reveal(90) {
                    AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle(title = "录入只要 30 秒", subtitle = "板块级勾选，不用逐题标记")
                        Spacer(Modifier.height(10.dp))
                        listOf(
                            "① 选模板：国考行政执法、联考、职测等预置题量",
                            "② 填对/错：一键切换录入口径，自动校验不超题量",
                            "③ 出报告：正确率、板块拆解、趋势与薄弱诊断"
                        ).forEach {
                            Text(
                                text = it,
                                color = p.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            } else {
                Reveal(0) {
                    HeroCard(
                        record = latest,
                        previous = previous,
                        animatedAccuracy = accAnim,
                        index = records.size,
                        onClick = { onOpenReport(latest.id) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Reveal(70) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val best = records.maxByOrNull { it.accuracy }
                        val avg = records.map { it.accuracy }.average().toFloat()
                        StatTile(
                            label = "历史最高",
                            value = best?.let { Fmt.percent(it.accuracy) } ?: "—",
                            caption = best?.let { Fmt.dateShort(it.dateMillis) },
                            accent = p.success,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "平均正确率",
                            value = Fmt.percent(avg),
                            caption = "近 ${records.size} 次",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Reveal(140) {
                    AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle(
                            title = "板块表现",
                            subtitle = "最近一次 · ${Fmt.dateFull(latest.dateMillis)}"
                        )
                        Spacer(Modifier.height(8.dp))
                        latest.answeredSections.forEachIndexed { index, section ->
                            SectionBarRow(
                                section = section,
                                delayMillis = 120 + index * 70,
                                highlight = section == latest.answeredSections.minByOrNull { it.accuracy }
                            )
                        }
                    }
                }

                if (chronological.size >= 1) {
                    Spacer(Modifier.height(12.dp))
                    Reveal(200) {
                        AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                            val shown = chronological.takeLast(8)
                            SectionTitle(
                                title = "正确率趋势",
                                subtitle = if (records.size < 2) "再记一次就能看到走势" else "最近 ${shown.size} 次模考"
                            )
                            Spacer(Modifier.height(14.dp))
                            TrendChart(
                                values = shown.map { it.accuracy },
                                labels = shown.map { Fmt.dateDot(it.dateMillis) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Reveal(260) {
                    AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle(title = "薄弱诊断", subtitle = "自动对比历史记录")
                        Spacer(Modifier.height(6.dp))
                        insights.take(2).forEach { InsightRow(it) }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = LocalIndication.current
                                ) { onOpenReport(latest.id) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "查看完整成绩报告",
                                color = p.accent,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = "→", color = p.accent, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(1.dp)
                        .background(p.divider)
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    text = "我不是冰墩墩   ·   Hu_Tao111",
                    color = p.textTertiary.copy(alpha = 0.55f),
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(118.dp))
        }

        FloatingAddButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 22.dp, bottom = 24.dp)
        )

        if (showAbout) {
            AboutDialog(versionName = versionName, onDismiss = { showAbout = false })
        }
    }
}

@Composable
private fun HeroCard(
    record: PaperRecord,
    previous: PaperRecord?,
    animatedAccuracy: Float,
    index: Int,
    onClick: () -> Unit
) {
    val p = LocalFluent.current
    val delta = previous?.let { record.accuracy - it.accuracy }
    AcrylicCard(
        modifier = Modifier.fillMaxWidth(),
        strong = true,
        corner = 28.dp,
        padding = PaddingValues(20.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PillChip(text = "最近一次")
            Spacer(Modifier.width(8.dp))
            if (delta != null && abs(delta) >= 0.005f) {
                val up = delta > 0
                val c = if (up) p.success else p.danger
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(c.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (up) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        contentDescription = null,
                        tint = c,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = Fmt.signedPercent(delta),
                        color = c,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "第 $index 次",
                color = p.textTertiary,
                fontSize = 10.5.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.name,
                    color = p.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = record.templateLabel,
                        color = p.textTertiary,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = " · ", color = p.textTertiary, fontSize = 10.5.sp)
                    Text(
                        text = Fmt.dateShort(record.dateMillis),
                        color = p.textTertiary,
                        fontSize = 10.5.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = Fmt.percent(animatedAccuracy),
                        color = p.textPrimary,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.4).sp
                    )
                }
                Text(
                    text = "总正确率 · 答对 ${record.totalCorrect} / 错 ${record.totalWrong}",
                    color = p.textSecondary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            ProgressRing(
                progress = record.accuracy,
                modifier = Modifier.size(98.dp),
                strokeWidth = 9.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${record.totalCorrect}",
                        color = p.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "/ ${record.totalQuestions} 题",
                        color = p.textTertiary,
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    val haptic = LocalHapticFeedback.current
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { ready = true }
    val scaleAnim by animateFloatAsState(
        targetValue = if (ready) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "fabIn"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .background(
                    Brush.radialGradient(
                        listOf(p.accent.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .scale(scaleAnim)
                .size(60.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = p.accent.copy(alpha = 0.45f),
                    spotColor = p.accent.copy(alpha = 0.55f)
                )
                .clip(CircleShape)
                .background(accentGradient())
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = LocalIndication.current
                ) {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "添加试卷记录",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
