package com.xingce.fupan.data

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/** 单个板块的作答结果 */
data class SectionResult(
    val name: String,
    val total: Int,
    val correct: Int
) {
    val wrong: Int get() = (total - correct).coerceAtLeast(0)
    val accuracy: Float get() = if (total <= 0) 0f else correct.toFloat() / total.toFloat()
    val accuracyPercent: Int get() = (accuracy * 100f).roundToInt()

    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("total", total)
        put("correct", correct)
    }

    companion object {
        fun fromJson(o: JSONObject): SectionResult = SectionResult(
            name = o.optString("name", "板块"),
            total = o.optInt("total", 0),
            correct = o.optInt("correct", 0)
        )
    }
}

/** 一次模考的完整记录 */
data class PaperRecord(
    val id: String,
    val name: String,
    val templateId: String,
    val templateLabel: String,
    val category: String,
    val dateMillis: Long,
    val durationMinutes: Int?,
    val sections: List<SectionResult>,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalQuestions: Int get() = sections.sumOf { it.total }
    val totalCorrect: Int get() = sections.sumOf { it.correct }
    val totalWrong: Int get() = (totalQuestions - totalCorrect).coerceAtLeast(0)
    val accuracy: Float get() = if (totalQuestions <= 0) 0f else totalCorrect.toFloat() / totalQuestions.toFloat()
    val accuracyPercent: Int get() = (accuracy * 100f).roundToInt()
    val answeredSections: List<SectionResult> get() = sections.filter { it.total > 0 }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("templateId", templateId)
        put("templateLabel", templateLabel)
        put("category", category)
        put("dateMillis", dateMillis)
        put("durationMinutes", durationMinutes ?: -1)
        put("createdAt", createdAt)
        put("sections", JSONArray().also { arr -> sections.forEach { arr.put(it.toJson()) } })
    }

    companion object {
        fun fromJson(o: JSONObject): PaperRecord {
            val arr = o.optJSONArray("sections") ?: JSONArray()
            val list = ArrayList<SectionResult>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { list.add(SectionResult.fromJson(it)) }
            }
            val duration = o.optInt("durationMinutes", -1)
            return PaperRecord(
                id = o.optString("id", java.util.UUID.randomUUID().toString()),
                name = o.optString("name", "未命名试卷"),
                templateId = o.optString("templateId", "custom"),
                templateLabel = o.optString("templateLabel", "自定义"),
                category = o.optString("category", "其他"),
                dateMillis = o.optLong("dateMillis", System.currentTimeMillis()),
                durationMinutes = if (duration > 0) duration else null,
                sections = list,
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
