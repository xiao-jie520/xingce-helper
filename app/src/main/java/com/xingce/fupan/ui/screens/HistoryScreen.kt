package com.xingce.fupan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Visibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xingce.fupan.data.PaperRecord
import com.xingce.fupan.ui.components.AcrylicCard
import com.xingce.fupan.ui.components.ConfirmDialog
import com.xingce.fupan.ui.components.EmptyStateCard
import com.xingce.fupan.ui.components.PillChip
import com.xingce.fupan.ui.components.Reveal
import com.xingce.fupan.ui.components.ScreenTopBar
import com.xingce.fupan.ui.components.accuracyColor
import com.xingce.fupan.ui.theme.LocalFluent
import com.xingce.fupan.util.Fmt

@Composable
fun HistoryScreen(
    records: List<PaperRecord>,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalFluent.current
    var filter by remember { mutableStateOf("全部") }
    var pendingDelete by remember { mutableStateOf<PaperRecord?>(null) }

    val categories = remember(records.size) {
        listOf("全部") + records.map { it.category }.distinct()
    }
    val filtered = if (filter == "全部") records else records.filter { it.category == filter }
    val groups = filtered.groupBy { Fmt.monthLabel(it.dateMillis) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(10.dp))
            ScreenTopBar(
                title = "历史试卷",
                subtitle = "共 ${records.size} 次记录",
                onBack = onBack
            )
        }

        Spacer(Modifier.height(14.dp))

        if (categories.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    PillChip(
                        text = category,
                        selected = category == filter,
                        onClick = { filter = category }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (filtered.isEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                EmptyStateCard(
                    title = "这里还是空的",
                    description = "记录第一次模考后，所有试卷都会按月份归档在这里。",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groups.forEach { (month, list) ->
                    item(key = "h_$month") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = month,
                                color = p.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${list.size} 次",
                                color = p.textTertiary,
                                fontSize = 10.5.sp
                            )
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    items(list, key = { it.id }) { record ->
                        RecordCard(
                            record = record,
                            onClick = { onOpen(record.id) },
                            onEdit = { onEdit(record.id) },
                            onDelete = { pendingDelete = record }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        ConfirmDialog(
            title = "删除这条记录？",
            message = "「${target.name}」会从本机移除，且无法恢复。",
            confirmText = "删除",
            danger = true,
            onConfirm = {
                val id = target.id
                pendingDelete = null
                onDelete(id)
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun RecordCard(
    record: PaperRecord,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val p = LocalFluent.current
    var menuOpen by remember { mutableStateOf(false) }
    val color = accuracyColor(record.accuracy)

    AcrylicCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 20.dp,
        padding = PaddingValues(14.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(color.copy(alpha = 0.16f), color.copy(alpha = 0.07f))
                        )
                    )
                    .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${record.accuracyPercent}%",
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.name,
                    color = p.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${Fmt.dateShort(record.dateMillis)} ${Fmt.weekday(record.dateMillis)} · " +
                        "${record.totalQuestions} 题 · 错 ${record.totalWrong}",
                    color = p.textTertiary,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PillChip(text = record.category)
                    if (record.durationMinutes != null) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${record.durationMinutes} 分钟",
                            color = p.textTertiary,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
            Box {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { menuOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "更多操作",
                        tint = p.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("查看报告", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("编辑记录", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除", fontSize = 13.sp, color = p.danger) },
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
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
