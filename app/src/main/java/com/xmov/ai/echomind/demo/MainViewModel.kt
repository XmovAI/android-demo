package com.xmov.ai.echomind.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val logBuilder = StringBuilder()

    private val _state = MutableLiveData("IDLE")
    val state: LiveData<String> = _state

    private val _logs = MutableLiveData("")
    val logs: LiveData<String> = _logs

    private val _subtitle = MutableLiveData("")
    val subtitle: LiveData<String> = _subtitle

    fun setState(state: String) {
        _state.value = state
    }

    fun setSubtitle(text: String) {
        _subtitle.value = text
    }

    fun appendLog(message: String) {
        logBuilder.append(timeFormat.format(Date())).append(' ').append(message).append('\n')
        _logs.value = logBuilder.toString()
    }
}