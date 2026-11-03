package com.xingce.fupan.data

import kotlin.math.roundToInt
import kotlin.math.sqrt

/** 某板块在一段区间内的汇总 */
data class ModuleStat(
    val name: String,
    val exams: Int,
    val totalQuestions: Int,
    val totalCorrect: Int
) {
    val accuracy: Float
        get() = if (totalQuestions <= 0) 0f else totalCorrect.toFloat() / totalQuestions.toFloat()
    val accuracyPercent: Int get() = (accuracy * 100f).roundToInt()

    /** 场均答对题数 */
    val avgCorrect: Float get() = if (exams <= 0) 0f else totalCorrect.toFloat() / exams.toFloat()

    /** 场均题量 */
    val avgTotal: Float get() = if (exams <= 0) 0f else totalQuestions.toFloat() / exams.toFloat()

    /** 场均丢分题数 */
    val avgWrong: Float get() = (avgTotal - avgCorrect).coerceAtLeast(0f)
}

/** 一组试卷的加权汇总（按题量加权，而不是百分比简单平均） */
data class Aggregate(
    val records: List<PaperRecord>,
    val modules: List<ModuleStat>
) {
    val exams: Int get() = records.size
    val totalQuestions: Int get() = modules.sumOf { it.totalQuestions }
    val totalCorrect: Int get() = modules.sumOf { it.totalCorrect }

    val accuracy: Float
        get() = if (totalQuestions <= 0) 0f else totalCorrect.toFloat() / totalQuestions.toFloat()
    val accuracyPercent: Int get() = (accuracy * 100f).roundToInt()

    val avgCorrect: Float get() = if (exams <= 0) 0f else totalCorrect.toFloat() / exams.toFloat()
    val avgQuestions: Float get() = if (exams <= 0) 0f else totalQuestions.toFloat() / exams.toFloat()

    val best: PaperRecord? get() = records.maxByOrNull { it.accuracy }
    val worst: PaperRecord? get() = records.minByOrNull { it.accuracy }
    val startDate: Long? get() = records.minOfOrNull { it.dateMillis }
    val endDate: Long? get() = records.maxOfOrNull { it.dateMillis }

    /** 单场正确率的标准差，用来看发挥是否稳定 */
    val spread: Float
        get() {
            if (records.size < 2) return 0f
            val avg = records.map { it.accuracy }.average()
            val variance = records.map { (it.accuracy - avg) * (it.accuracy - avg) }.average()
            return sqrt(variance).toFloat()
        }

    val sortedModules: List<ModuleStat> get() = modules.sortedByDescending { it.accuracy }

    fun module(name: String): ModuleStat? = modules.firstOrNull { it.name == name }
}

object Aggregator {

    private val canonicalOrder = listOf(
        "政治理论",
        "常识判断",
        "言语理解与表达",
        "数量关系",
        "判断推理",
        "资料分析"
    )

    fun of(records: List<PaperRecord>): Aggregate {
        val sorted = records.sortedWith(
            compareBy<PaperRecord> { it.dateMillis }.thenBy { it.createdAt }
        )
        val acc = LinkedHashMap<String, IntArray>()
        sorted.forEach { record ->
            record.answeredSections.forEach { section ->
                val bucket = acc.getOrPut(section.name) { IntArray(3) }
                bucket[0] += 1
                bucket[1] += section.total
                bucket[2] += section.correct
            }
        }
        val modules = acc.map { (name, b) -> ModuleStat(name, b[0], b[1], b[2]) }
            .sortedBy { m ->
                val idx = canonicalOrder.indexOf(m.name)
                if (idx < 0) canonicalOrder.size else idx
            }
        return Aggregate(sorted, modules)
    }
}
