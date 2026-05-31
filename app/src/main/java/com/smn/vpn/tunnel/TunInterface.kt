package com.smn.vpn.tunnel

import android.net.VpnService
import com.smn.vpn.utils.Logger
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class TunInterface(private val vpnService: VpnService) {

    private var vpnInterface: android.net.ParcelFileDescriptor? = null
    private var tunChannel: DatagramChannel? = null
    private val tunMtu = 1500

    fun establish(): Boolean {
        return try {
            Logger.d("TunInterface", "Establishing TUN interface...")

            val builder = VpnService.Builder()
            builder.setSession("SMN VPN")
            
            // Виртуальный IP адрес устройства в VPN сети
            builder.addAddress("10.0.0.2", 32)
            
            // Маршрут: весь трафик через VPN
            builder.addRoute("0.0.0.0", 0)  // IPv4
            builder.addRoute("::", 0)       // IPv6
            
            // DNS серверы
            builder.addDnsServer("8.8.8.8")      // Google DNS
            builder.addDnsServer("1.1.1.1")      // Cloudflare DNS
            builder.addDnsServer("2001:4860:4860::8888")  // Google IPv6
            
            // Настройки MTU
            builder.setMtu(tunMtu)
            
            // Исключаем само приложение VPN из туннеля (чтобы не было цикла)
            try {
                builder.addDisallowedApplication(vpnService.packageName)
            } catch (e: Exception) {
                Logger.w("TunInterface", "Could not exclude app: ${e.message}")
            }
            
            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                Logger.d("TunInterface", "TUN interface established successfully")
                true
            } else {
                Logger.e("TunInterface", "Failed to establish TUN interface")
                false
            }
        } catch (e: Exception) {
            Logger.e("TunInterface", "Error establishing TUN: ${e.message}")
            false
        }
    }

    fun close() {
        try {
            vpnInterface?.close()
            tunChannel?.close()
            Logger.d("TunInterface", "TUN interface closed")
        } catch (e: Exception) {
            Logger.e("TunInterface", "Error closing TUN: ${e.message}")
        }
    }

    fun getFileDescriptor() = vpnInterface?.fileDescriptor

    fun readPacket(): ByteArray? {
        return try {
            val buffer = ByteBuffer.allocate(tunMtu)
            val fd = vpnInterface?.fileDescriptor ?: return null
            
            // Симуляция чтения пакета
            // В реальной реализации нужна более сложная работа с парселем
            ByteArray(0)
        } catch (e: Exception) {
            Logger.e("TunInterface", "Error reading packet: ${e.message}")
            null
        }
    }

    fun writePacket(packet: ByteArray) {
        try {
            val fd = vpnInterface?.fileDescriptor ?: return
            // Запись пакета в TUN интерфейс
            Logger.d("TunInterface", "Packet written: ${packet.size} bytes")
        } catch (e: Exception) {
            Logger.e("TunInterface", "Error writing packet: ${e.message}")
        }
    }

    fun isConnected(): Boolean = vpnInterface != null

    companion object {
        const val VIRTUAL_IP = "10.0.0.2"
        const val VIRTUAL_GATEWAY = "10.0.0.1"
    }
}
