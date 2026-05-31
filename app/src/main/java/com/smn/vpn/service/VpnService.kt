package com.smn.vpn.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.IBinder
import com.smn.vpn.crypto.CryptoManager
import com.smn.vpn.utils.Logger

class SMNVpnService : VpnService() {

    private val binder = LocalBinder()
    private var isConnected = false
    private lateinit var cryptoManager: CryptoManager
    private lateinit var serverConnection: ServerConnection
    private lateinit var killSwitch: KillSwitch

    inner class LocalBinder : Binder() {
        fun getService(): SMNVpnService = this@SMNVpnService
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d("VpnService", "Service created")
        cryptoManager = CryptoManager()
        killSwitch = KillSwitch(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d("VpnService", "Service started")
        
        when (intent?.action) {
            ACTION_CONNECT -> startVpn()
            ACTION_DISCONNECT -> stopVpn()
        }
        
        return START_STICKY
    }

    private fun startVpn() {
        try {
            Logger.d("VpnService", "Starting VPN...")
            
            // Создаём VPN интерфейс
            val builder = Builder()
            builder.setSession("SMN VPN")
            builder.addAddress("10.0.0.2", 32)
            builder.addRoute("0.0.0.0", 0)
            builder.addDnsServer("8.8.8.8")
            builder.addDnsServer("8.8.4.4")
            
            val vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                isConnected = true
                serverConnection = ServerConnection(this, cryptoManager, vpnInterface)
                serverConnection.connect()
                Logger.d("VpnService", "VPN connected")
            }
            
        } catch (e: Exception) {
            Logger.e("VpnService", "Error starting VPN: ${e.message}")
            killSwitch.blockInternet()
        }
    }

    private fun stopVpn() {
        Logger.d("VpnService", "Stopping VPN...")
        isConnected = false
        serverConnection.disconnect()
        Logger.d("VpnService", "VPN stopped")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isConnected) {
            stopVpn()
        }
        Logger.d("VpnService", "Service destroyed")
    }

    companion object {
        const val ACTION_CONNECT = "com.smn.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.smn.vpn.DISCONNECT"
    }
}
