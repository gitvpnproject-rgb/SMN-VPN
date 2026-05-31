package com.smn.vpn.node

import com.smn.vpn.utils.Logger
import kotlin.concurrent.thread

class NodeRegistry {

    private val nodes = mutableMapOf<String, NodeInfo>()
    private val nodeConnections = mutableMapOf<String, MutableList<String>>()  // граф связей

    fun registerNode(nodeInfo: NodeInfo) {
        nodes[nodeInfo.id] = nodeInfo
        nodeConnections[nodeInfo.id] = mutableListOf()
        Logger.d("NodeRegistry", "Registered node: ${nodeInfo.name}")
    }

    fun unregisterNode(nodeId: String) {
        nodes.remove(nodeId)
        nodeConnections.remove(nodeId)
        
        // Удаляем из всех соединений
        nodeConnections.values.forEach { it.remove(nodeId) }
        
        Logger.d("NodeRegistry", "Unregistered node: $nodeId")
    }

    fun getNode(nodeId: String): NodeInfo? = nodes[nodeId]

    fun getAllNodes(): List<NodeInfo> = nodes.values.toList()

    fun getAliveNodes(): List<NodeInfo> = nodes.values.filter { it.isAlive }

    fun addConnection(nodeId1: String, nodeId2: String) {
        nodeConnections[nodeId1]?.add(nodeId2)
        Logger.d("NodeRegistry", "Connection added: $nodeId1 → $nodeId2")
    }

    fun removeConnection(nodeId1: String, nodeId2: String) {
        nodeConnections[nodeId1]?.remove(nodeId2)
        Logger.d("NodeRegistry", "Connection removed: $nodeId1 → $nodeId2")
    }

    fun getConnections(nodeId: String): List<String> =
        nodeConnections[nodeId]?.toList() ?: emptyList()

    fun getNearestNode(sourceNodeId: String, excludeNodes: Set<String> = emptySet()): NodeInfo? {
        val connections = getConnections(sourceNodeId)
        return connections
            .mapNotNull { getNode(it) }
            .filter { !excludeNodes.contains(it.id) && it.isAlive }
            .minByOrNull { it.latency }
    }

    fun findRoute(
        from: String,
        to: String,
        maxHops: Int = 3
    ): List<String>? {
        // Простой BFS для поиска маршрута
        val route = mutableListOf<String>()
        val visited = mutableSetOf<String>()
        
        if (bfs(from, to, route, visited, maxHops)) {
            return route
        }
        return null
    }

    private fun bfs(
        current: String,
        target: String,
        route: MutableList<String>,
        visited: MutableSet<String>,
        maxHops: Int
    ): Boolean {
        if (route.size > maxHops) return false
        if (current == target) {
            route.add(current)
            return true
        }
        
        visited.add(current)
        route.add(current)
        
        val neighbors = getConnections(current)
        for (neighbor in neighbors) {
            if (!visited.contains(neighbor)) {
                if (bfs(neighbor, target, route, visited, maxHops)) {
                    return true
                }
            }
        }
        
        route.removeAt(route.size - 1)
        return false
    }

    fun printTopology() {
        Logger.d("NodeRegistry", "=== Network Topology ===")
        Logger.d("NodeRegistry", "Total nodes: ${nodes.size}")
        Logger.d("NodeRegistry", "Alive nodes: ${getAliveNodes().size}")
        
        nodes.values.forEach { node ->
            val connections = getConnections(node.id)
            Logger.d(
                "NodeRegistry",
                "${node.name}: ${connections.size} connections, latency=${node.latency}ms"
            )
        }
    }
}
