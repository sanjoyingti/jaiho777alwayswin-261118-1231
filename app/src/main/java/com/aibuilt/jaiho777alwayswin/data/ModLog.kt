package com.aibuilt.jaiho777alwayswin.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogLine(val time: String, val text: String)

class ModLog {

    private val stamp = SimpleDateFormat("HH:mm:ss", Locale.US)

    private val _lines = MutableStateFlow<List<LogLine>>(emptyList())
    val lines: StateFlow<List<LogLine>> = _lines.asStateFlow()

    private val _status = MutableStateFlow(PatchStatus.IDLE)
    val status: StateFlow<PatchStatus> = _status.asStateFlow()

    fun setStatus(value: PatchStatus) {
        _status.value = value
    }

    fun add(text: String) {
        val entry = LogLine(stamp.format(Date()), text)
        val next = (listOf(entry) + _lines.value).take(MAX)
        _lines.value = next
    }

    fun clear() {
        _lines.value = emptyList()
    }

    companion object {
        private const val MAX = 300
    }
}

enum class PatchStatus {
    IDLE,
    INJECTING,
    ACTIVE,
    FAILED
}
