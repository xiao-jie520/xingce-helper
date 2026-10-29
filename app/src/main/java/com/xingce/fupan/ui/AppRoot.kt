package com.xingce.fupan.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.xingce.fupan.data.PaperRecord
import com.xingce.fupan.data.RecordStore
import com.xingce.fupan.ui.components.FluentBackground
import com.xingce.fupan.ui.screens.AddRecordScreen
import com.xingce.fupan.ui.screens.HistoryScreen
import com.xingce.fupan.ui.screens.HomeScreen
import com.xingce.fupan.ui.screens.ReportScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface Screen {
    val depth: Int

    data object Home : Screen {
        override val depth: Int get() = 0
    }

    data object History : Screen {
        override val depth: Int get() = 1
    }

    data class Add(val editId: String? = null) : Screen {
        override val depth: Int get() = 2
    }

    data class Report(val id: String) : Screen {
        override val depth: Int get() = 2
    }
}

@Composable
fun AppRoot(records: List<PaperRecord>) {
    var stack by remember { mutableStateOf<List<Screen>>(listOf(Screen.Home)) }
    val screen = stack.last()
    val scope = rememberCoroutineScope()

    val push: (Screen) -> Unit = { next -> stack = stack + next }
    val pop: () -> Unit = { if (stack.size > 1) stack = stack.dropLast(1) }

    BackHandler(enabled = stack.size > 1) { pop() }

    FluentBackground {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                val forward = targetState.depth >= initialState.depth
                val dir = if (forward) 1 else -1
                (fadeIn(tween(240, delayMillis = 30)) +
                    slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 320f)
                    ) { full -> dir * full / 7 } +
                    scaleIn(
                        initialScale = 0.965f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ))
                    .togetherWith(
                        fadeOut(tween(140)) +
                            slideOutHorizontally(
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) { full -> -dir * full / 16 } +
                            scaleOut(targetScale = 0.98f, animationSpec = tween(280))
                    )
            },
            modifier = Modifier.fillMaxSize(),
            label = "screen"
        ) { target ->
            when (target) {
                is Screen.Home -> HomeScreen(
                    records = records,
                    onOpenHistory = { push(Screen.History) },
                    onAdd = { push(Screen.Add()) },
                    onOpenReport = { id -> push(Screen.Report(id)) }
                )

                is Screen.History -> HistoryScreen(
                    records = records,
                    onBack = pop,
                    onOpen = { id -> push(Screen.Report(id)) },
                    onEdit = { id -> push(Screen.Add(id)) },
                    onDelete = { id -> RecordStore.delete(id) }
                )

                is Screen.Add -> AddRecordScreen(
                    existing = target.editId?.let { id -> records.firstOrNull { it.id == id } },
                    onBack = pop,
                    onSaved = { id ->
                        stack = stack.dropLast(1) + Screen.Report(id)
                    }
                )

                is Screen.Report -> ReportScreen(
                    record = records.firstOrNull { it.id == target.id },
                    all = records,
                    onBack = pop,
                    onEdit = { id -> push(Screen.Add(id)) },
                    onDelete = { id ->
                        pop()
                        // 等页面退场动画结束再删除，避免离场时闪出空状态
                        scope.launch {
                            delay(360)
                            RecordStore.delete(id)
                        }
                    },
                    onAdd = { push(Screen.Add()) }
                )
            }
        }
    }
}
