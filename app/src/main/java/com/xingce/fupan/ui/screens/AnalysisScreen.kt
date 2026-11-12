package com.xingce.fupan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingce.fupan.data.Aggregate
import com.xingce.fupan.data.Aggregator
import com.xingce.fupan.data.Analyzer
import com.xingce.fupan.data.PaperRecord
import com.xingce.fupan.data.SectionResult
import com.xingce.fupan.ui.components.AcrylicCard
import com.xingce.fupan.ui.components.EmptyStateCard
import com.xingce.fupan.ui.components.GroupLabel
import com.xingce.fupan.ui.components.InsightRow
import com.xingce.fupan.ui.components.PillChip
import com.xingce.fupan.ui.components.ProgressRing
import com.xingce.fupan.ui.components.Reveal
import com.xingce.fupan.ui.components.RowDivider
import com.xingce.fupan.ui.components.ScreenTopBar
import com.xingce.fupan.ui.components.SectionBarRow
import com.xingce.fupan.ui.components.SectionTitle
import com.xingce.fupan.ui.components.TrendChart
import com.xingce.fupan.ui.components.VerticalRule
import com.xingce.fupan.ui.theme.LocalFluent
import com.xingce.fupan.util.Fmt

private enum class TimeRange(val label: String) {
    All("全部"),
    Days7("近 7 天"),
    Days30("近 30 天"),
    Days90("近 90 天"),
    Custom("自定义")
}

/**
 * 综合分析：按时间范围（全部 / 近 7·30·90 天 / 自定义起止）统计各模块平均成绩。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    records: List<PaperRecord>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    val categories = remember(records) { records.map { it.category }.distinct() }
    var category by remember { mutableStateOf("全部") }
    var range by remember { mutableStateOf(TimeRange.All) }
    var customStart by remember { mutableStateOf<Long?>(null) }
    var customEnd by remember { mutableStateOf<Long?>(null) }
    var showPicker by remember { mutableStateOf(false) }

    val base = remember(records, category) {
        if (category == "全部") records else records.filter { it.category == category }
    }
    val now = System.currentTimeMillis()

    val selected: List<PaperRecord> = when (range) {
        TimeRange.All -> base
        TimeRange.Days7 -> base.filter { it.dateMillis >= now - 7L * 86_400_000L }
        TimeRange.Days30 -> base.filter { it.dateMillis >= now - 30L * 86_400_000L }
        TimeRange.Days90 -> base.filter { it.dateMillis >= now - 90L * 86_400_000L }
        TimeRange.Custom -> {
            val start = customStart?.let { Fmt.pickerMillisToLocalDate(it) }
            val end = (customEnd ?: customStart)?.let { Fmt.pickerMillisToLocalDate(it) }
            if (start == null || end == null) {
                emptyList()
            } else {
                base.filter {
                    val d = Fmt.toLocalDate(it.dateMillis)
                    !d.isBefore(start) && !d.isAfter(end)
                }
            }
        }
    }

    val agg = remember(selected) { Aggregator.of(selected) }
    val insights = remember(selected) { Analyzer.aggregateInsights(agg, null) }
    val chrono = remember(selected) { Analyzer.overallTrend(selected) }

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
            title = "综合分析",
            subtitle = when {
                records.isEmpty() -> "暂无记录"
                selected.isEmpty() -> "所选范围暂无记录"
                else -> "${selected.size} 次 · " +
                    "${agg.startDate?.let { Fmt.dateShort(it) } ?: ""} – " +
                    "${agg.endDate?.let { Fmt.dateShort(it) } ?: ""}"
            },
            onBack = onBack
        )

        Spacer(Modifier.height(16.dp))

        if (records.isEmpty()) {
            EmptyStateCard(
                title = "还没有可分析的数据",
                description = "记录一次模考后，这里可以按时间范围查看各模块的平均成绩与趋势。",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(30.dp))
            return@Column
        }

        // 时间范围 + 类别
        AcrylicCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
            Text(
                text = "时间范围",
                color = p.textTertiary,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.entries.forEach { item ->
                    PillChip(
                        text = item.label,
                        selected = item == range,
                        onClick = {
                            if (item == TimeRange.Custom) {
                                range = TimeRange.Custom
                                if (customStart == null) showPicker = true
                            } else {
                                range = item
                            }
                        }
                    )
                }
            }

            if (range == TimeRange.Custom) {
                Spacer(Modifier.height(10.dp))
                val startMillis = customStart
                val endMillis = customEnd ?: customStart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (startMillis != null && endMillis != null) {
                            val s = Fmt.pickerMillisToLocalDate(startMillis)
                            val e = Fmt.pickerMillisToLocalDate(endMillis)
                            "已选 ${s.year}.${s.monthValue}.${s.dayOfMonth} – ${e.monthValue}.${e.dayOfMonth} · ${selected.size} 场"
                        } else {
                            "还没有选择起止日期"
                        },
                        color = p.textTertiary,
                        fontSize = 10.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "选择日期",
                        color = p.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPicker = true }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            if (categories.size > 1) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "试卷类别",
                    color = p.textTertiary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (listOf("全部") + categories).forEach { item ->
                        PillChip(
                            text = item,
                            selected = item == category,
                            onClick = { category = item }
                        )
                    }
                }
            }
        }

        if (selected.isEmpty()) {
            Spacer(Modifier.height(14.dp))
            EmptyStateCard(
                title = "这个范围内没有记录",
                description = "换个时间范围或清除类别筛选再试试；也可以先去记一次模考。",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(30.dp))
            return@Column
        }

        GroupLabel("区间概览")
        Reveal(0) {
            SummaryCard(agg)
        }

        GroupLabel("各模块平均成绩 · 按题量加权")
        Reveal(70) {
            AcrylicCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
            ) {
                agg.sortedModules.forEachIndexed { index, module ->
                    if (index > 0) RowDivider()
                    SectionBarRow(
                        section = SectionResult(module.name, module.totalQuestions, module.totalCorrect),
                        delayMillis = 100 + index * 70,
                        highlight = index == agg.sortedModules.lastIndex && agg.sortedModules.size > 1,
                        countText = "场均 ${Fmt.num1(module.avgCorrect)}/${Fmt.num0(module.avgTotal)}",
                        trailingText = "${module.exams} 场",
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "「N 场」是该模块在所选范围内的作答场次；正确率按「总答对 ÷ 总题量」计算，不会被题量少的场次带偏。",
                    color = p.textTertiary,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }

        if (chrono.size >= 2) {
            GroupLabel("正确率走势")
            Reveal(140) {
                AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                    TrendChart(
                        values = chrono.map { it.accuracy },
                        labels = chrono.map { Fmt.dateDot(it.dateMillis) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        GroupLabel("诊断与建议")
        Reveal(200) {
            AcrylicCard(modifier = Modifier.fillMaxWidth()) {
                insights.forEach { InsightRow(it) }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "统计口径：按题量加权；场均答对 = 该模块总答对 ÷ 作答场次。",
            color = p.textTertiary,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )

        Spacer(Modifier.height(36.dp))
    }

    if (showPicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = customStart,
            initialSelectedEndDateMillis = customEnd
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val s = pickerState.selectedStartDateMillis
                    val e = pickerState.selectedEndDateMillis ?: s
                    if (s != null) {
                        customStart = s
                        customEnd = e
                        range = TimeRange.Custom
                    }
                    showPicker = false
                }) {
                    Text("确定", color = p.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPicker = false
                    if (customStart == null) range = TimeRange.All
                }) {
                    Text("取消", color = p.textSecondary)
                }
            }
        ) {
            DateRangePicker(
                state = pickerState,
                showModeToggle = false,
                modifier = Modifier.height(460.dp),
                title = {
                    Text(
                        text = "选择统计区间",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.textPrimary,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SummaryCard(agg: Aggregate) {
    val p = LocalFluent.current
    AcrylicCard(
        modifier = Modifier.fillMaxWidth(),
        strong = true,
        corner = 28.dp,
        padding = PaddingValues(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "区间加权正确率",
                    color = p.textTertiary,
                    fontSize = 10.5.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = Fmt.percent(agg.accuracy),
                    color = p.textPrimary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.2).sp
                )
                Text(
                    text = "共 ${agg.exams} 场 · 场均答对 ${Fmt.num1(agg.avgCorrect)} / ${Fmt.num0(agg.avgQuestions)} 题",
                    color = p.textSecondary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            ProgressRing(
                progress = agg.accuracy,
                modifier = Modifier.size(96.dp),
                strokeWidth = 9.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Fmt.num0(agg.avgCorrect),
                        color = p.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "场均答对",
                        color = p.textTertiary,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(p.divider)
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val best = agg.best
            val worst = agg.worst
            MiniStat(
                label = "最高",
                value = best?.let { Fmt.percent(it.accuracy, 0) } ?: "—",
                caption = best?.let { Fmt.dateShort(it.dateMillis) },
                color = p.success,
                modifier = Modifier.weight(1f)
            )
            VerticalRule(height = 30.dp)
            MiniStat(
                label = "最低",
                value = worst?.let { Fmt.percent(it.accuracy, 0) } ?: "—",
                caption = worst?.let { Fmt.dateShort(it.dateMillis) },
                color = p.danger,
                modifier = Modifier.weight(1f)
            )
            VerticalRule(height = 30.dp)
            MiniStat(
                label = "波动",
                value = if (agg.exams >= 2) "±${Fmt.num1(agg.spread * 100f)}" else "—",
                caption = if (agg.exams >= 2) "标准差" else "需 2 场以上",
                color = when {
                    agg.exams < 2 -> p.textTertiary
                    agg.spread >= 0.07f -> p.danger
                    agg.spread >= 0.04f -> p.warning
                    else -> p.success
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    caption: String?,
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
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            color = p.textTertiary,
            fontSize = 9.5.sp,
            modifier = Modifier.padding(top = 1.dp)
        )
        if (caption != null) {
            Text(
                text = caption,
                color = p.textTertiary.copy(alpha = 0.7f),
                fontSize = 8.5.sp,
                maxLines = 1
            )
        }
    }
}
