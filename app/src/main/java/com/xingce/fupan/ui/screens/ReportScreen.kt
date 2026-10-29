package com.xingce.fupan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingce.fupan.data.Analyzer
import com.xingce.fupan.data.PaperRecord
import com.xingce.fupan.ui.components.AcrylicCard
import com.xingce.fupan.ui.components.AcrylicIconButton
import com.xingce.fupan.ui.components.ConfirmDialog
import com.xingce.fupan.ui.components.GradientButton
import com.xingce.fupan.ui.components.InsightRow
import com.xingce.fupan.ui.components.PillChip
import com.xingce.fupan.ui.components.ProgressRing
import com.xingce.fupan.ui.components.Reveal
import com.xingce.fupan.ui.components.ScreenTopBar
import com.xingce.fupan.ui.components.SectionBarRow
import com.xingce.fupan.ui.components.SectionTitle
import com.xingce.fupan.ui.components.TrendChart
import com.xingce.fupan.ui.components.VerticalRule
import com.xingce.fupan.ui.components.toneColor
import com.xingce.fupan.ui.theme.LocalFluent
import com.xingce.fupan.util.Fmt

@Composable
fun ReportScreen(
    record: PaperRecord?,
    all: List<PaperRecord>,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    if (record == null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            ScreenTopBar(title = "成绩报告", onBack = onBack)
            Spacer(Modifier.height(40.dp))
            Text(text = "这条记录已经不在了", color = p.textSecondary, fontSize = 14.sp)
        }
        return
    }

    val grade = Analyzer.grade(record.accuracy)
    val insights = remember(record.id, all.size) { Analyzer.insights(record, all) }
    val previous = remember(record.id, all.size) { Analyzer.previousOf(record, all) }
    val sectionHistory = remember(record.id, all.size) { Analyzer.sectionHistory(record, all) }
    val chronological = remember(record.id, all.size) { Analyzer.overallTrend(all) }
    val trendShown = chronological.takeLast(10)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        ScreenTopBar(
            title = "成绩报告",
            subtitle = Fmt.dateFull(record.dateMillis),
            onBack = onBack,
            actions = {
                AcrylicIconButton(
                    icon = Icons.Rounded.Share,
                    onClick = {
                        val text = Analyzer.shareText(record, all)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, record.name)
                            putExtra(android.content.Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(intent, "分享成绩报告")
                        )
                    },
                    contentDescription = "分享"
                )
                Spacer(Modifier.width(8.dp))
                Box {
                    AcrylicIconButton(
                        icon = Icons.Rounded.MoreVert,
                        onClick = { menuOpen = true },
                        contentDescription = "更多"
                    )
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑这条记录", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuOpen = false
                                onEdit(record.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除这条记录", fontSize = 13.sp, color = p.danger) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = p.danger,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            }
                        )
                    }
                }
            }
        )

        Spacer(Modifier.height(18.dp))

        Reveal(0) {
            AcrylicCard(modifier = Modifier.fillMaxWidth(), strong = true, corner = 28.dp, padding = PaddingValues(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row {
                            PillChip(text = record.category)
                            Spacer(Modifier.width(6.dp))
                            if (record.durationMinutes != null) {
                                PillChip(text = "${record.durationMinutes} 分钟")
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = record.name,
                            color = p.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${record.templateLabel} · 共 ${record.totalQuestions} 题",
                            color = p.textTertiary,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(toneColor(grade.tone).copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = grade.label,
                                color = toneColor(grade.tone),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = grade.comment,
                                color = toneColor(grade.tone),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    ProgressRing(
                        progress = record.accuracy,
                        modifier = Modifier.size(104.dp),
                        strokeWidth = 9.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = Fmt.percent(record.accuracy),
                                color = p.textPrimary,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.6).sp
                            )
                            Text(
                                text = "总正确率",
                                color = p.textTertiary,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Reveal(70) {
            AcrylicCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniStat("答对", "${record.totalCorrect}", p.success, Modifier.weight(1f))
                    VerticalRule(height = 30.dp)
                    MiniStat("答错", "${record.totalWrong}", p.danger, Modifier.weight(1f))
                    VerticalRule(height = 30.dp)
                    MiniStat("题量", "${record.totalQuestions}", p.textPrimary, Modifier.weight(1f))
                    if (record.durationMinutes != null) {
                        VerticalRule(height = 30.dp)
                        MiniStat("用时", "${record.durationMinutes}′", p.textPrimary, Modifier.weight(1f))
                    }
                    if (previous != null) {
                        VerticalRule(height = 30.dp)
                        val delta = record.accuracy - previous.accuracy
                        MiniStat(
                            label = "较上次",
                            value = Fmt.signedPercent(delta, 0),
                            color = if (delta >= 0) p.success else p.danger,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Reveal(140) {
            AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(
                    title = "板块拆解",
                    subtitle = "板块右侧为与历史平均的差值"
                )
                Spacer(Modifier.height(8.dp))
                record.answeredSections.forEachIndexed { index, section ->
                    val historyAvg = sectionHistory[section.name]
                    val deltaText = historyAvg?.let { Fmt.signedPercent(section.accuracy - it, 0) }
                    SectionBarRow(
                        section = section,
                        delayMillis = 100 + index * 70,
                        highlight = section == record.answeredSections.minByOrNull { it.accuracy },
                        trailingText = deltaText
                    )
                }
            }
        }

        if (trendShown.size >= 2) {
            Spacer(Modifier.height(12.dp))
            Reveal(200) {
                AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(
                        title = "正确率趋势",
                        subtitle = "最近 ${trendShown.size} 次模考"
                    )
                    Spacer(Modifier.height(14.dp))
                    TrendChart(
                        values = trendShown.map { it.accuracy },
                        labels = trendShown.map { Fmt.dateDot(it.dateMillis) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Reveal(260) {
            AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                SectionTitle(
                    title = "诊断与建议",
                    subtitle = "结合本次与历史记录"
                )
                Spacer(Modifier.height(6.dp))
                insights.forEach { InsightRow(it) }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.accent.copy(alpha = 0.07f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = buildString {
                            append("复盘提示：先把「")
                            append(record.answeredSections.minByOrNull { it.accuracy }?.name ?: "最弱板块")
                            append("」的错题按题型归类，再决定下一套卷子的做题顺序。")
                        },
                        color = p.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Reveal(320) {
            GradientButton(
                text = "再记一次模考",
                icon = Icons.Rounded.Add,
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(36.dp))
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "删除这条记录？",
            message = "「${record.name}」的记录会从本机移除，且无法恢复。",
            confirmText = "删除",
            danger = true,
            onConfirm = {
                confirmDelete = false
                onDelete(record.id)
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )
        Text(
            text = label,
            color = p.textTertiary,
            fontSize = 9.5.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
