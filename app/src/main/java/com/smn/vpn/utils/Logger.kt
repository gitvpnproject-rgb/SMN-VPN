package com.smn.vpn.utils

import android.util.Log

object Logger {
    private const val TAG = "SMN-VPN"

    fun d(tag: String, message: String) {
        Log.d("$TAG-$tag", message)
    }

    fun e(tag: String, message: String) {
        Log.e("$TAG-$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("$TAG-$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("$TAG-$tag", message)
    }
}
