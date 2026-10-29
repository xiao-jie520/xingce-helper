package com.xingce.fupan.data

/** 模板内的板块定义 */
data class TemplateSection(
    val name: String,
    val total: Int
) {
    val shortName: String
        get() = when {
            name.contains("政治") -> "政治"
            name.contains("常识") -> "常识"
            name.contains("言语") -> "言语"
            name.contains("数量") -> "数量"
            name.contains("判断") -> "判断"
            name.contains("资料") -> "资料"
            else -> name.take(2)
        }
}

/** 内置试卷模板 */
data class PaperTemplate(
    val id: String,
    val label: String,
    val category: String,
    val minutes: Int?,
    val sections: List<TemplateSection>,
    val custom: Boolean = false
) {
    val totalQuestions: Int get() = sections.sumOf { it.total }

    val subtitle: String
        get() = if (custom) "自己定义板块与题量" else "${totalQuestions} 题" + (minutes?.let { " · $it 分钟" } ?: "")
}

/**
 * 2025 年起国考行测新增「政治理论」20 题，常识判断降到 15、言语降到 30、判断降到 35。
 * 下面的题量按 2026 年国考大纲整理，旧大纲试卷单独留了一套，方便刷早年真题。
 */
object Templates {

    val CUSTOM_ID = "custom"

    private val gk2026 = listOf(
        TemplateSection("政治理论", 20),
        TemplateSection("常识判断", 15),
        TemplateSection("言语理解与表达", 30),
        TemplateSection("数量关系", 10),
        TemplateSection("判断推理", 35),
        TemplateSection("资料分析", 20)
    )

    val all: List<PaperTemplate> = listOf(
        PaperTemplate(
            id = "gk_xzzf",
            label = "国考 · 行政执法",
            category = "国考",
            minutes = 120,
            sections = gk2026
        ),
        PaperTemplate(
            id = "gk_dsj",
            label = "国考 · 地市级",
            category = "国考",
            minutes = 120,
            sections = gk2026
        ),
        PaperTemplate(
            id = "gk_fsj",
            label = "国考 · 副省级",
            category = "国考",
            minutes = 120,
            sections = listOf(
                TemplateSection("政治理论", 20),
                TemplateSection("常识判断", 15),
                TemplateSection("言语理解与表达", 30),
                TemplateSection("数量关系", 15),
                TemplateSection("判断推理", 35),
                TemplateSection("资料分析", 20)
            )
        ),
        PaperTemplate(
            id = "sk_lk",
            label = "省考 · 联考通用",
            category = "省考",
            minutes = 120,
            sections = listOf(
                TemplateSection("政治理论", 20),
                TemplateSection("常识判断", 15),
                TemplateSection("言语理解与表达", 30),
                TemplateSection("数量关系", 15),
                TemplateSection("判断推理", 35),
                TemplateSection("资料分析", 20)
            )
        ),
        PaperTemplate(
            id = "sy_zc_a",
            label = "事业单位 · 职测A",
            category = "事业单位",
            minutes = 90,
            sections = listOf(
                TemplateSection("常识判断", 20),
                TemplateSection("言语理解与表达", 20),
                TemplateSection("数量关系", 10),
                TemplateSection("判断推理", 35),
                TemplateSection("资料分析", 15)
            )
        ),
        PaperTemplate(
            id = "gk_old",
            label = "国考 · 2024 前旧卷",
            category = "国考",
            minutes = 120,
            sections = listOf(
                TemplateSection("常识判断", 20),
                TemplateSection("言语理解与表达", 40),
                TemplateSection("数量关系", 10),
                TemplateSection("判断推理", 40),
                TemplateSection("资料分析", 20)
            )
        ),
        PaperTemplate(
            id = CUSTOM_ID,
            label = "自定义试卷",
            category = "自定义",
            minutes = null,
            sections = gk2026,
            custom = true
        )
    )

    fun byId(id: String?): PaperTemplate = all.firstOrNull { it.id == id } ?: all.first()

    /** 用于自定义模板的空白板块 */
    fun blankSections(): List<TemplateSection> = listOf(TemplateSection("板块一", 10))
}
