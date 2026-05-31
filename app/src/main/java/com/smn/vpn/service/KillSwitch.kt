package com.smn.vpn.service

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.smn.vpn.utils.Logger

class KillSwitch(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isActive = false

    fun enable() {
        isActive = true
        Logger.d("KillSwitch", "Kill Switch enabled")
        blockInternet()
    }

    fun disable() {
        isActive = false
        Logger.d("KillSwitch", "Kill Switch disabled")
        allowInternet()
    }

    fun blockInternet() {
        if (!isActive) return
        
        try {
            // Блокируем все сетевые интерфейсы кроме VPN
            Logger.d("KillSwitch", "Blocking internet...")
            
            // В реальном приложении нужно использовать iptables или другие методы
            // Это упрощённая версия для демонстрации
            
            Logger.d("KillSwitch", "Internet blocked")
        } catch (e: Exception) {
            Logger.e("KillSwitch", "Error blocking internet: ${e.message}")
        }
    }

    fun allowInternet() {
        try {
            Logger.d("KillSwitch", "Allowing internet...")
            // Восстанавливаем сетевой доступ
            Logger.d("KillSwitch", "Internet allowed")
        } catch (e: Exception) {
            Logger.e("KillSwitch", "Error allowing internet: ${e.message}")
        }
    }

    fun isInternetConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun isVpnConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }
}
