package com.smn.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smn.vpn.service.SMNVpnService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Автозапуск VPN при включении устройства (если был включён)
            val prefs = context.getSharedPreferences("smn_prefs", Context.MODE_PRIVATE)
            val autoStart = prefs.getBoolean("auto_start", false)
            if (autoStart) {
                val vpnIntent = Intent(context, SMNVpnService::class.java).apply {
                    action = SMNVpnService.ACTION_CONNECT
                }
                context.startForegroundService(vpnIntent)
            }
        }
    }
}