package com.aibuilt.jaiho777alwayswin.ui

import android.webkit.JavascriptInterface
import com.aibuilt.jaiho777alwayswin.viewmodel.MainViewModel

/**
 * Small JavaScript interface. The injected patch calls these methods; the app updates its state.
 */
class Bridge(private val vm: MainViewModel) {

    @JavascriptInterface
    fun log(message: String) {
        vm.note(message)
    }

    @JavascriptInterface
    fun ready() {
        vm.noteReady()
    }

    @JavascriptInterface
    fun disabled() {
        vm.noteDisabled()
    }

    @JavascriptInterface
    fun failed(reason: String) {
        vm.noteFailed(reason)
    }

    /** Always reads the current settings, so a reload picks up changes immediately. */
    @JavascriptInterface
    fun config(): String = vm.config.value.toPatchJson()
}
