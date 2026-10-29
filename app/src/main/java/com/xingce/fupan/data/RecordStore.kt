package com.xingce.fupan.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import java.io.File

/**
 * 极简本地存储：全部记录序列化到 files/records.json。
 * 写入采用「临时文件 + 重命名」保证原子性，避免中途崩溃导致数据损坏。
 */
object RecordStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _records = MutableStateFlow<List<PaperRecord>>(emptyList())
    val records: StateFlow<List<PaperRecord>> = _records.asStateFlow()

    private var storeFile: File? = null

    fun init(context: Context) {
        if (storeFile != null) return
        val f = File(context.applicationContext.filesDir, "records.json")
        storeFile = f
        scope.launch {
            val loaded = runCatching { readFrom(f) }.getOrDefault(emptyList())
            _records.value = sort(loaded)
        }
    }

    fun add(record: PaperRecord) {
        _records.value = sort(_records.value.filterNot { it.id == record.id } + record)
        persist()
    }

    fun update(record: PaperRecord) {
        val exists = _records.value.any { it.id == record.id }
        _records.value = sort(if (exists) _records.value.map { if (it.id == record.id) record else it } else _records.value + record)
        persist()
    }

    fun delete(id: String) {
        _records.value = _records.value.filterNot { it.id == id }
        persist()
    }

    fun find(id: String?): PaperRecord? = _records.value.firstOrNull { it.id == id }

    fun clear() {
        _records.value = emptyList()
        persist()
    }

    private fun sort(list: List<PaperRecord>): List<PaperRecord> =
        list.sortedWith(compareByDescending<PaperRecord> { it.dateMillis }.thenByDescending { it.createdAt })

    private fun persist() {
        val snapshot = _records.value
        val target = storeFile ?: return
        scope.launch {
            mutex.withLock {
                runCatching {
                    val arr = JSONArray()
                    snapshot.forEach { arr.put(it.toJson()) }
                    val tmp = File(target.parentFile, target.name + ".tmp")
                    tmp.writeText(arr.toString())
                    if (target.exists()) target.delete()
                    tmp.renameTo(target)
                }
            }
        }
    }

    private fun readFrom(file: File): List<PaperRecord> {
        if (!file.exists()) return emptyList()
        val text = file.readText()
        if (text.isBlank()) return emptyList()
        val arr = JSONArray(text)
        val out = ArrayList<PaperRecord>(arr.length())
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.let { out.add(PaperRecord.fromJson(it)) }
        }
        return out
    }
}
