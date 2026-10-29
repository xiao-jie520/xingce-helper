package com.xingce.fupan.data

import com.xingce.fupan.util.Fmt
import kotlin.math.roundToInt

enum class Tone { GOOD, INFO, WARN, BAD }

data class Insight(
    val title: String,
    val detail: String,
    val tone: Tone,
    val badge: String? = null
)

enum class Grade(val label: String, val comment: String, val tone: Tone) {
    S("S", "顶尖水准，保持手感", Tone.GOOD),
    A("A", "具备进面竞争力", Tone.GOOD),
    B("B", "有竞争力，还能再提", Tone.INFO),
    C("C", "基础尚可，短板要补", Tone.WARN),
    D("D", "失分明显，先抓重点", Tone.WARN),
    E("E", "从基础题型重新铺一遍", Tone.BAD)
}

object Analyzer {

    fun grade(accuracy: Float): Grade = when {
        accuracy >= 0.85f -> Grade.S
        accuracy >= 0.78f -> Grade.A
        accuracy >= 0.70f -> Grade.B
        accuracy >= 0.60f -> Grade.C
        accuracy >= 0.50f -> Grade.D
        else -> Grade.E
    }

    fun previousOf(record: PaperRecord, all: List<PaperRecord>): PaperRecord? =
        all.filter {
            it.id != record.id && (it.dateMillis < record.dateMillis ||
                (it.dateMillis == record.dateMillis && it.createdAt < record.createdAt))
        }.maxWithOrNull(compareBy<PaperRecord> { it.dateMillis }.thenBy { it.createdAt })

    /** 各板块在本条记录之前的历史平均正确率 */
    fun sectionHistory(record: PaperRecord, all: List<PaperRecord>): Map<String, Float> {
        val history = all.filter { it.id != record.id }
        return record.answeredSections.mapNotNull { section ->
            val values = history.mapNotNull { r ->
                r.sections.firstOrNull { it.name == section.name && it.total > 0 }?.accuracy
            }
            if (values.isEmpty()) null else section.name to values.average().toFloat()
        }.toMap()
    }

    fun sectionAverage(all: List<PaperRecord>, sectionName: String): Float? {
        val values = all.mapNotNull { r ->
            r.sections.firstOrNull { it.name == sectionName && it.total > 0 }?.accuracy
        }
        return if (values.isEmpty()) null else values.average().toFloat()
    }

    fun overallTrend(records: List<PaperRecord>): List<PaperRecord> =
        records.sortedWith(compareBy<PaperRecord> { it.dateMillis }.thenBy { it.createdAt })

    /** 核心诊断：2~4 条，按重要性排序 */
    fun insights(record: PaperRecord, all: List<PaperRecord>): List<Insight> {
        val out = ArrayList<Insight>()
        val sections = record.answeredSections
        if (sections.isEmpty()) {
            return listOf(Insight("数据不足", "本次没有填写板块作答情况，补全后才能生成诊断。", Tone.INFO))
        }

        val history = all.filter { it.id != record.id }
        val weakest = sections.minByOrNull { it.accuracy }!!
        val strongest = sections.maxByOrNull { it.accuracy }!!
        val others = sections.filter { it.name != weakest.name }
        val othersAvg = if (others.isEmpty()) 0f else others.map { it.accuracy }.average().toFloat()

        // 1. 短板诊断
        if (sections.size >= 2 && othersAvg > weakest.accuracy) {
            val gain = ((othersAvg - weakest.accuracy) * weakest.total).roundToInt().coerceAtLeast(1)
            out += Insight(
                title = "补短板 · ${weakest.name}",
                detail = "正确率 ${Fmt.percent(weakest.accuracy)}，${weakest.total} 题错 ${weakest.wrong} 题，" +
                    "是本卷最大失分区。若把它拉到其余板块平均水平（${Fmt.percent(othersAvg)}），可多拿约 $gain 题。",
                tone = toneOf(weakest.accuracy),
                badge = "可提 $gain 题"
            )
        }

        // 2. 与历史对比
        val prev = previousOf(record, all)
        if (prev != null) {
            val delta = record.accuracy - prev.accuracy
            val recent = history.sortedByDescending { it.dateMillis }.take(3)
            val recentAvg = recent.map { it.accuracy }.average().toFloat()
            if (kotlin.math.abs(delta) >= 0.02f) {
                val up = delta > 0
                out += Insight(
                    title = if (up) "较上次提升" else "较上次回落",
                    detail = "本次 ${Fmt.percent(record.accuracy)}，上次 ${Fmt.percent(prev.accuracy)}（${Fmt.dateShort(prev.dateMillis)}），" +
                        "${if (up) "高出" else "低了"} ${Fmt.percent(kotlin.math.abs(delta))}" +
                        if (up) "。保持这个节奏，把稳定性做出来。" else "。先回看上次到这次错题类型有没有共性。",
                    tone = if (up) Tone.GOOD else Tone.WARN,
                    badge = Fmt.signedPercent(delta)
                )
            } else if (recent.isNotEmpty()) {
                out += Insight(
                    title = "状态基本持平",
                    detail = "本次与上次相差不到 2 个百分点，近 ${recent.size} 次平均 ${Fmt.percent(recentAvg)}。" +
                        "正确率卡住时，通常不是知识问题，而是时间分配。",
                    tone = Tone.INFO,
                    badge = Fmt.signedPercent(delta)
                )
            }
        } else {
            out += Insight(
                title = "已建立基准线",
                detail = "这是第一条记录，之后每次模考都会自动和这次对比，生成趋势与板块进退步。",
                tone = Tone.INFO,
                badge = "首次"
            )
        }

        // 3. 板块历史进退步
        val sectionHistory = sectionHistory(record, all)
        val moved = sections.mapNotNull { s ->
            val h = sectionHistory[s.name] ?: return@mapNotNull null
            Triple(s, s.accuracy - h, h)
        }.filter { kotlin.math.abs(it.second) >= 0.05f }
            .sortedByDescending { kotlin.math.abs(it.second) }
        moved.firstOrNull()?.let { (s, d, h) ->
            out += Insight(
                title = if (d > 0) "${s.name} 在进步" else "${s.name} 在下滑",
                detail = "历史平均 ${Fmt.percent(h)}，本次 ${Fmt.percent(s.accuracy)}，" +
                    "波动 ${Fmt.percent(kotlin.math.abs(d))}。" +
                    if (d > 0) "把有效方法固定下来，别只靠手感。" else "这个板块需要单独拿 30 分钟做专项。",
                tone = if (d > 0) Tone.GOOD else Tone.BAD,
                badge = Fmt.signedPercent(d)
            )
        }

        // 4. 整体判断
        val allHigh = sections.all { it.accuracy >= 0.70f }
        val allLow = sections.count { it.accuracy < 0.60f } >= 2
        when {
            allHigh -> out += Insight(
                title = "板块均衡，缺一把尖刀",
                detail = "五个板块都在 70% 以上，没有明显短板。想继续提分，优先把「${strongest.name}」" +
                    "（当前 ${Fmt.percent(strongest.accuracy)}）推到 85% 以上，比补弱项更快。",
                tone = Tone.GOOD,
                badge = "均衡"
            )
            allLow -> out += Insight(
                title = "多个板块同时失守",
                detail = "有 ${sections.count { it.accuracy < 0.60f }} 个板块低于 60%，" +
                    "不建议平均用力：先挑题量最大且提分最快的「${sections.filter { it.accuracy < 0.6f }.maxByOrNull { it.total }?.name}」做专项。",
                tone = Tone.BAD,
                badge = "重点"
            )
            else -> {
                val wrongShare = sections.sortedByDescending { it.wrong }.take(2)
                if (wrongShare.size >= 2 && record.totalWrong > 0) {
                    val share = wrongShare.sumOf { it.wrong }.toFloat() / record.totalWrong
                    out += Insight(
                        title = "错题集中在这两块",
                        detail = "本卷共错 ${record.totalWrong} 题，其中「${wrongShare[0].name}」${wrongShare[0].wrong} 题、" +
                            "「${wrongShare[1].name}」${wrongShare[1].wrong} 题，合计占 ${Fmt.percent(share, 0)}。复盘时先啃这两块。",
                        tone = Tone.INFO,
                        badge = "占 ${Fmt.percent(share, 0)}"
                    )
                }
            }
        }

        return out.take(4)
    }

    fun toneOf(accuracy: Float): Tone = when {
        accuracy >= 0.75f -> Tone.GOOD
        accuracy >= 0.60f -> Tone.INFO
        accuracy >= 0.45f -> Tone.WARN
        else -> Tone.BAD
    }

    /** 分享文本 */
    fun shareText(record: PaperRecord, all: List<PaperRecord>): String {
        val sb = StringBuilder()
        sb.append("【行测复盘】${record.name}\n")
        sb.append("${Fmt.dateFull(record.dateMillis)}｜总正确率 ${Fmt.percent(record.accuracy)}\n")
        sb.append("答对 ${record.totalCorrect} / ${record.totalQuestions} 题\n")
        sb.append("—— 板块明细 ——\n")
        record.answeredSections.forEach {
            sb.append("${it.name}：${it.correct}/${it.total}　${Fmt.percent(it.accuracy)}\n")
        }
        val ins = insights(record, all)
        if (ins.isNotEmpty()) {
            sb.append("—— 诊断 ——\n")
            ins.take(3).forEach { sb.append("· ${it.title}：${it.detail}\n") }
        }
        sb.append("\n—— 行测复盘 · Hu_Tao111")
        return sb.toString().trim()
    }
}
