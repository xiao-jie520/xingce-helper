package com.xingce.fupan

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xingce.fupan.data.RecordStore
import com.xingce.fupan.ui.AppRoot
import com.xingce.fupan.ui.theme.XingceTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestMaxRefreshRate()
        RecordStore.init(this)
        setContent {
            XingceTheme {
                val records by RecordStore.records.collectAsStateWithLifecycle()
                AppRoot(records = records)
            }
        }
    }

    /**
     * 高刷屏默认把普通应用限到 60Hz，这里显式申请屏幕支持的最高刷新率，
     * 否则动画写得再顺也只能跑 60 帧。
     */
    private fun requestMaxRefreshRate() {
        val maxRate = runCatching {
            val modes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display?.supportedModes
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.supportedModes
            }
            modes?.maxOfOrNull { it.refreshRate }
        }.getOrNull() ?: return

        if (maxRate <= 0f) return
        window.attributes = window.attributes.apply {
            preferredRefreshRate = maxRate
        }
        if (Build.VERSION.SDK_INT >= 35) {
            // 等 decorView 挂上窗口后再申请，避免被首次布局重置
            window.decorView.post {
                window.decorView.setRequestedFrameRate(maxRate)
            }
        }
    }
}
