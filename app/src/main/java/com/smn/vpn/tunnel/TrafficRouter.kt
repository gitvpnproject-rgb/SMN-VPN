package com.smn.vpn.tunnel

import com.smn.vpn.utils.Logger
import java.net.InetAddress
import java.net.NetworkInterface

data class Route(
    val destination: String,
    val gateway: String,
    val netmask: String,
    val metric: Int
)

class TrafficRouter {

    private val routes = mutableListOf<Route>()
    private var defaultGateway: String? = null

    init {
        initializeRoutes()
    }

    private fun initializeRoutes() {
        Logger.d("TrafficRouter", "Initializing routing table...")
        
        // Добавляем маршрут по умолчанию через VPN
        addRoute(
            destination = "0.0.0.0",
            gateway = TunInterface.VIRTUAL_GATEWAY,
            netmask = "0.0.0.0",
            metric = 1
        )
        
        // Локальная сеть
        addRoute(
            destination = "127.0.0.1",
            gateway = "127.0.0.1",
            netmask = "255.255.255.255",
            metric = 256
        )
        
        // IPv6 маршрут по умолчанию
        addRoute(
            destination = "::",
            gateway = "fe80::1",
            netmask = "::/0",
            metric = 1
        )
        
        Logger.d("TrafficRouter", "Routing table initialized with ${routes.size} routes")
    }

    fun addRoute(
        destination: String,
        gateway: String,
        netmask: String,
        metric: Int = 0
    ) {
        val route = Route(destination, gateway, netmask, metric)
        routes.add(route)
        Logger.d("TrafficRouter", "Route added: $destination via $gateway")
    }

    fun removeRoute(destination: String) {
        routes.removeAll { it.destination == destination }
        Logger.d("TrafficRouter", "Route removed: $destination")
    }

    fun findRoute(destinationIp: String): Route? {
        // Ищем наиболее специфичный маршрут
        return routes
            .sortedByDescending { it.metric }
            .firstOrNull { matchesRoute(destinationIp, it) }
    }

    private fun matchesRoute(ip: String, route: Route): Boolean {
        try {
            val destAddr = InetAddress.getByName(ip)
            val routeDest = InetAddress.getByName(route.destination)
            val routeMask = InetAddress.getByName(route.netmask)
            
            return matchesSubnet(destAddr, routeDest, routeMask)
        } catch (e: Exception) {
            Logger.e("TrafficRouter", "Error matching route: ${e.message}")
            return false
        }
    }

    private fun matchesSubnet(
        address: InetAddress,
        subnet: InetAddress,
        mask: InetAddress
    ): Boolean {
        val addressBytes = address.address
        val subnetBytes = subnet.address
        val maskBytes = mask.address
        
        if (addressBytes.size != subnetBytes.size || addressBytes.size != maskBytes.size) {
            return false
        }
        
        for (i in addressBytes.indices) {
            val maskedAddress = addressBytes[i].toInt() and maskBytes[i].toInt()
            val maskedSubnet = subnetBytes[i].toInt() and maskBytes[i].toInt()
            
            if (maskedAddress != maskedSubnet) {
                return false
            }
        }
        
        return true
    }

    fun getAllRoutes(): List<Route> = routes.toList()

    fun getDefaultGateway(): String? {
        return routes.firstOrNull { it.destination == "0.0.0.0" }?.gateway
    }

    fun routePacket(sourceIp: String, destinationIp: String): Route? {
        val route = findRoute(destinationIp)
        Logger.d("TrafficRouter", "Routing $sourceIp → $destinationIp via ${route?.gateway}")
        return route
    }

    fun printRoutingTable() {
        Logger.d("TrafficRouter", "=== Routing Table ===")
        routes.forEach { route ->
            Logger.d(
                "TrafficRouter",
                "${route.destination}/${route.netmask} via ${route.gateway} metric=${route.metric}"
            )
        }
    }
}
