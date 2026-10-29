package com.xingce.fupan.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object Fmt {

    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    /** Material DatePicker 返回的是 UTC 零点毫秒 */
    fun pickerMillisToLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun localDateToMillis(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun dateFull(millis: Long): String =
        toLocalDate(millis).format(DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日", Locale.CHINA))

    fun dateShort(millis: Long): String =
        toLocalDate(millis).format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA))

    fun dateDot(millis: Long): String =
        toLocalDate(millis).format(DateTimeFormatter.ofPattern("M.d", Locale.CHINA))

    fun monthLabel(millis: Long): String {
        val d = toLocalDate(millis)
        return "${d.year} 年 ${d.monthValue} 月"
    }

    fun weekday(millis: Long): String {
        val d = toLocalDate(millis)
        return when (d.dayOfWeek.value) {
            1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
            5 -> "周五"; 6 -> "周六"; else -> "周日"
        }
    }

    fun percent(value: Float, digits: Int = 1): String {
        val v = value * 100f
        return if (digits == 0) "${v.roundToInt()}%"
        else String.format(Locale.CHINA, "%.${digits}f%%", v)
    }

    fun signedPercent(value: Float, digits: Int = 1): String {
        val v = value * 100f
        val s = String.format(Locale.CHINA, "%.${digits}f", v)
        return if (v >= 0) "+$s%" else "$s%"
    }

    fun duration(minutes: Int?): String = when {
        minutes == null || minutes <= 0 -> "未记录用时"
        minutes >= 60 -> "${minutes / 60} 小时${if (minutes % 60 == 0) "" else " ${minutes % 60} 分"}"
        else -> "$minutes 分钟"
    }
}
