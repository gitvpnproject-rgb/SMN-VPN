package com.smn.vpn.tunnel

import android.content.Context
import android.net.VpnService
import com.smn.vpn.service.KillSwitch
import com.smn.vpn.utils.Logger

class VpnTunnelManager(
    private val context: Context,
    private val vpnService: VpnService
) {

    private lateinit var routingManager: DeviceRoutingManager
    private lateinit var killSwitch: KillSwitch
    private var isConnected = false

    fun connect(): Boolean {
        return try {
            Logger.d("VpnTunnelManager", "Connecting VPN tunnel...")
            
            killSwitch = KillSwitch(context)
            killSwitch.enable()
            
            routingManager = DeviceRoutingManager(vpnService)
            if (!routingManager.initialize()) {
                Logger.e("VpnTunnelManager", "Failed to initialize routing")
                return false
            }
            
            routingManager.start()
            routingManager.printRoutingTable()
            
            isConnected = true
            Logger.d("VpnTunnelManager", "VPN tunnel connected")
            true
        } catch (e: Exception) {
            Logger.e("VpnTunnelManager", "Connection error: ${e.message}")
            isConnected = false
            false
        }
    }

    fun disconnect() {
        try {
            Logger.d("VpnTunnelManager", "Disconnecting VPN tunnel...")
            routingManager.stop()
            killSwitch.disable()
            isConnected = false
            Logger.d("VpnTunnelManager", "VPN tunnel disconnected")
        } catch (e: Exception) {
            Logger.e("VpnTunnelManager", "Disconnection error: ${e.message}")
        }
    }

    fun isConnected(): Boolean = isConnected

    fun getStatus(): String {
        return if (isConnected) {
            "Connected"
        } else {
            "Disconnected"
        }
    }
}
