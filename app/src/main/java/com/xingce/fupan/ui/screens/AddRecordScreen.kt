package com.xingce.fupan.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingce.fupan.data.PaperRecord
import com.xingce.fupan.data.PaperTemplate
import com.xingce.fupan.data.RecordStore
import com.xingce.fupan.data.SectionResult
import com.xingce.fupan.data.Templates
import com.xingce.fupan.ui.components.AcrylicCard
import com.xingce.fupan.ui.components.GradientButton
import com.xingce.fupan.ui.components.PillChip
import com.xingce.fupan.ui.components.ScreenTopBar
import com.xingce.fupan.ui.components.SectionTitle
import com.xingce.fupan.ui.components.SegmentedToggle
import com.xingce.fupan.ui.components.accuracyColor
import com.xingce.fupan.ui.theme.LocalFluent
import com.xingce.fupan.util.Fmt
import java.time.LocalDate
import java.util.UUID

private data class SectionDraft(
    val key: Long,
    val name: String,
    val total: String,
    val count: String
)

private data class RowState(
    val draft: SectionDraft,
    val total: Int,
    val count: Int?,
    val valid: Boolean,
    val accuracy: Float?
)

private fun defaultName(template: PaperTemplate): String {
    val base = if (template.label.contains("·")) template.label.substringAfter("·").trim() else template.label
    return "$base 模考"
}

private fun draftsFromTemplate(template: PaperTemplate, seed: Long): List<SectionDraft> =
    template.sections.mapIndexed { index, section ->
        SectionDraft(seed + index, section.name, section.total.toString(), "")
    }

private fun draftsFromRecord(record: PaperRecord, seed: Long): List<SectionDraft> =
    record.sections.mapIndexed { index, section ->
        SectionDraft(seed + index, section.name, section.total.toString(), section.correct.toString())
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    existing: PaperRecord?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    val haptic = LocalHapticFeedback.current

    val editKey = existing?.id ?: "new"
    var templateId by remember(editKey) { mutableStateOf(existing?.templateId ?: Templates.all.first().id) }
    var name by remember(editKey) { mutableStateOf(existing?.name ?: defaultName(Templates.byId(existing?.templateId))) }
    var nameEdited by remember(editKey) { mutableStateOf(existing != null) }
    var dateMillis by remember(editKey) {
        mutableStateOf(existing?.dateMillis ?: Fmt.localDateToMillis(LocalDate.now()))
    }
    var durationText by remember(editKey) {
        mutableStateOf(existing?.durationMinutes?.toString().orEmpty())
    }
    var mode by remember(editKey) { mutableStateOf(0) }
    var drafts by remember(editKey) {
        mutableStateOf(
            existing?.let { draftsFromRecord(it, 0L) } ?: draftsFromTemplate(Templates.byId(templateId), 0L)
        )
    }
    var keySeed by remember(editKey) { mutableStateOf(100L) }
    var showDatePicker by remember { mutableStateOf(false) }

    val template = Templates.byId(templateId)
    val custom = template.custom

    val rowStates = drafts.map { draft ->
        val total = draft.total.toIntOrNull() ?: 0
        val count = draft.count.toIntOrNull()
        val valid = if (total <= 0) true else (count != null && count in 0..total)
        val accuracy = if (total > 0 && count != null && count in 0..total) {
            val correct = if (mode == 0) count else total - count
            correct.toFloat() / total.toFloat()
        } else null
        RowState(draft, total, count, valid, accuracy)
    }
    val activeRows = rowStates.filter { it.total > 0 }
    val invalidCount = activeRows.count { !it.valid }
    val nameOk = name.trim().isNotEmpty()
    val canSubmit = nameOk && invalidCount == 0 && activeRows.isNotEmpty()
    val previewTotal = activeRows.sumOf { it.total }
    val previewCorrect = activeRows.sumOf { row ->
        val count = row.count ?: 0
        if (mode == 0) count else row.total - count
    }
    val previewAccuracy = if (previewTotal > 0) previewCorrect.toFloat() / previewTotal.toFloat() else 0f

    val submitHint = when {
        !nameOk -> "先给这次模考起个名字"
        activeRows.isEmpty() -> "至少填写一个板块的题量"
        invalidCount > 0 -> "还有 $invalidCount 个板块的题数没填好"
        else -> null
    }

    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scroll)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(10.dp))
            ScreenTopBar(
                title = if (existing == null) "记录试卷" else "编辑记录",
                subtitle = if (existing == null) "选模板 · 填板块 · 出报告" else "修改后会重新生成报告",
                onBack = onBack,
                backIcon = Icons.Rounded.Close
            )
            Spacer(Modifier.height(18.dp))
            SectionTitle(
                title = "选择试卷模板",
                subtitle = "题量已按真题预填，可随时改"
            )
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(Templates.all, key = { it.id }) { item ->
                TemplateCard(
                    template = item,
                    selected = item.id == templateId,
                    onSelect = {
                        templateId = item.id
                        keySeed += 100L
                        drafts = draftsFromTemplate(item, keySeed)
                        mode = 0
                        if (!nameEdited) name = defaultName(item)
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            AcrylicCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                SectionTitle(title = "基本信息")
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.track)
                        .border(1.dp, p.strokeBottom.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(text = "试卷名称", color = p.textTertiary, fontSize = 9.5.sp)
                        BasicTextField(
                            value = name,
                            onValueChange = {
                                name = it.take(40)
                                nameEdited = true
                            },
                            textStyle = TextStyle(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (nameOk) p.textPrimary else p.danger
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(p.accent),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box {
                                    if (name.isEmpty()) {
                                        Text(
                                            text = "例如：行政执法模考 第 3 套",
                                            color = p.textTertiary.copy(alpha = 0.7f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(p.track)
                            .border(1.dp, p.strokeBottom.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = p.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(text = "模考日期", color = p.textTertiary, fontSize = 9.5.sp)
                            Text(
                                text = Fmt.dateFull(dateMillis),
                                color = p.textPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                        modifier = Modifier
                            .width(104.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(p.track)
                            .border(1.dp, p.strokeBottom.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "用时（分钟）", color = p.textTertiary, fontSize = 9.5.sp)
                        BasicTextField(
                            value = durationText,
                            onValueChange = { v -> durationText = v.filter { it.isDigit() }.take(3) },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = p.textPrimary
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(p.accent),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box {
                                    if (durationText.isEmpty()) {
                                        Text(text = "可选", color = p.textTertiary.copy(alpha = 0.7f), fontSize = 13.sp)
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AcrylicCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                SectionTitle(
                    title = "作答情况",
                    subtitle = if (custom) "自定义板块，可增删" else "按板块填写，不用逐题标记"
                )
                Spacer(Modifier.height(12.dp))
                SegmentedToggle(
                    options = listOf("填对题数", "填错题数"),
                    selectedIndex = mode,
                    onSelect = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        mode = it
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (mode == 0) "现在填写每个板块「答对」的题数" else "现在填写每个板块「答错」的题数，对题数自动换算",
                    color = p.textTertiary,
                    fontSize = 10.5.sp
                )

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "板块",
                        color = p.textTertiary,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "题量", color = p.textTertiary, fontSize = 10.sp, modifier = Modifier.width(52.dp), textAlign = TextAlign.Center)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (mode == 0) "对" else "错",
                        color = p.accent,
                        fontSize = 10.sp,
                        modifier = Modifier.width(58.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "正确率",
                        color = p.textTertiary,
                        fontSize = 10.sp,
                        modifier = Modifier.width(44.dp),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(Modifier.height(6.dp))

                drafts.forEachIndexed { index, draft ->
                    val state = rowStates[index]
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (custom) {
                            Box(modifier = Modifier.weight(1f)) {
                                NameInput(
                                    value = draft.name,
                                    onValueChange = { v ->
                                        drafts = drafts.toMutableList().also {
                                            it[index] = it[index].copy(name = v.take(10))
                                        }
                                    }
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable(enabled = drafts.size > 1) {
                                        drafts = drafts.filterIndexed { i, _ -> i != index }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "删除板块",
                                    tint = if (drafts.size > 1) p.textTertiary else p.textTertiary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                        } else {
                            Text(
                                text = draft.name,
                                color = p.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        NumberInput(
                            value = draft.total,
                            onValueChange = { v ->
                                drafts = drafts.toMutableList().also {
                                    it[index] = it[index].copy(total = v)
                                }
                            },
                            modifier = Modifier.width(52.dp),
                            placeholder = "0"
                        )
                        Spacer(Modifier.width(8.dp))
                        NumberInput(
                            value = draft.count,
                            onValueChange = { v ->
                                drafts = drafts.toMutableList().also {
                                    it[index] = it[index].copy(count = v)
                                }
                            },
                            modifier = Modifier.width(58.dp),
                            placeholder = "—",
                            isError = state.total > 0 && state.count != null && !state.valid,
                            emphasized = true
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = state.accuracy?.let { Fmt.percent(it, 0) } ?: "—",
                            color = state.accuracy?.let { accuracyColor(it) } ?: p.textTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(44.dp)
                        )
                    }
                }

                if (custom) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(p.track)
                            .clickable {
                                keySeed += 1
                                drafts = drafts + SectionDraft(keySeed, "新板块", "10", "")
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = p.accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(text = "添加板块", color = p.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "本卷合计",
                        color = p.textTertiary,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (previewTotal > 0 && invalidCount == 0) {
                            "$previewTotal 题 · 预计 ${Fmt.percent(previewAccuracy, 0)}"
                        } else {
                            "$previewTotal 题已登记"
                        },
                        color = if (previewTotal > 0 && invalidCount == 0) {
                            accuracyColor(previewAccuracy)
                        } else p.textSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (submitHint != null) {
                Text(
                    text = submitHint,
                    color = if (nameOk && activeRows.isNotEmpty()) p.warning else p.textTertiary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }

            GradientButton(
                text = if (existing == null) "添加并生成报告" else "保存修改",
                onClick = {
                    val sections = activeRows.mapNotNull { row ->
                        val count = row.count
                        if (count == null || row.total <= 0) {
                            null
                        } else {
                            val correct = if (mode == 0) count else row.total - count
                            SectionResult(
                                name = row.draft.name.trim().ifBlank { "板块" },
                                total = row.total,
                                correct = correct.coerceIn(0, row.total)
                            )
                        }
                    }
                    if (sections.isNotEmpty()) {
                        val record = PaperRecord(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            templateId = template.id,
                            templateLabel = template.label,
                            category = template.category,
                            dateMillis = dateMillis,
                            durationMinutes = durationText.toIntOrNull()?.takeIf { it > 0 },
                            sections = sections,
                            createdAt = existing?.createdAt ?: System.currentTimeMillis()
                        )
                        if (existing == null) RecordStore.add(record) else RecordStore.update(record)
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onSaved(record.id)
                    }
                },
                enabled = canSubmit,
                hint = if (canSubmit) "${previewCorrect}/${previewTotal}" else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "数据仅保存在本机，卸载会一并清除。",
                color = p.textTertiary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(40.dp))
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { picked ->
                        dateMillis = Fmt.localDateToMillis(Fmt.pickerMillisToLocalDate(picked))
                    }
                    showDatePicker = false
                }) { Text("确定", color = p.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = p.textSecondary)
                }
            }
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }
}

@Composable
private fun TemplateCard(
    template: PaperTemplate,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val p = LocalFluent.current
    val checkScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "checkScale"
    )
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .width(158.dp)
            .height(122.dp)
            .clip(shape)
            .background(
                if (selected) {
                    Brush.linearGradient(
                        listOf(p.accent.copy(alpha = 0.10f), p.accentLight.copy(alpha = 0.06f))
                    )
                } else {
                    Brush.verticalGradient(listOf(p.acrylic, p.acrylicSoft))
                }
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) p.accent else p.strokeBottom.copy(alpha = 0.6f),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onSelect
            )
            .padding(13.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillChip(
                    text = template.category,
                    selected = selected
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier
                        .size(16.dp)
                        .scale(checkScale)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = template.label,
                color = p.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                lineHeight = 17.sp,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = template.subtitle,
                color = p.textTertiary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "0",
    isError: Boolean = false,
    emphasized: Boolean = false
) {
    val p = LocalFluent.current
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(
                if (emphasized && !isError) p.accent.copy(alpha = 0.08f)
                else p.track
            )
            .border(
                1.dp,
                when {
                    isError -> p.danger
                    emphasized -> p.accent.copy(alpha = 0.35f)
                    else -> p.strokeBottom.copy(alpha = 0.5f)
                },
                shape
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { v -> onValueChange(v.filter { it.isDigit() }.take(3)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) p.danger else p.textPrimary,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(p.accent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = p.textTertiary.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun NameInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    val p = LocalFluent.current
    Box(
        modifier = Modifier
            .padding(end = 4.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(p.track)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = p.textPrimary
            ),
            singleLine = true,
            cursorBrush = SolidColor(p.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(text = "板块名", color = p.textTertiary, fontSize = 12.sp)
                    }
                    inner()
                }
            }
        )
    }
}
