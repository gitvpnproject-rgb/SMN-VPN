package com.smn.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.smn.vpn.service.SMNVpnService

class MainActivity : Activity() {

    companion object {
        const val TAG = "MainActivity"
        const val VPN_REQUEST_CODE = 1001
    }

    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvNodes: TextView
    private lateinit var tvEncryption: TextView

    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)
        tvNodes = findViewById(R.id.tvNodes)
        tvEncryption = findViewById(R.id.tvEncryption)

        // Обработка smn://join/TOKEN ссылок
        intent?.data?.let { uri ->
            if (uri.scheme == "smn" && uri.host == "join") {
                val token = uri.lastPathSegment
                Log.d(TAG, "SMN join token: $token")
                handleJoinToken(token)
            }
        }

        btnConnect.setOnClickListener {
            if (isConnected) {
                disconnectVpn()
            } else {
                connectVpn()
            }
        }

        updateUI(false)
    }

    private fun connectVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            startVpnService()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVpnService()
            } else {
                tvStatus.text = "Разрешение VPN отклонено"
            }
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, SMNVpnService::class.java).apply {
            action = SMNVpnService.ACTION_CONNECT
        }
        startService(intent)
        updateUI(true)
    }

    private fun disconnectVpn() {
        val intent = Intent(this, SMNVpnService::class.java).apply {
            action = SMNVpnService.ACTION_DISCONNECT
        }
        startService(intent)
        updateUI(false)
    }

    private fun updateUI(connected: Boolean) {
        isConnected = connected
        if (connected) {
            btnConnect.text = "ОТКЛЮЧИТЬ"
            tvStatus.text = "🔐 ПОДКЛЮЧЕНО"
            tvNodes.text = "Узлы: 3 активных"
            tvEncryption.text = "AES-128 Balanced"
        } else {
            btnConnect.text = "ПОДКЛЮЧИТЬ"
            tvStatus.text = "Не подключено"
            tvNodes.text = "Узлы: —"
            tvEncryption.text = "—"
        }
    }

    private fun handleJoinToken(token: String?) {
        if (token == null) return
        // TODO: добавить сервер по токену
        Log.d(TAG, "Adding node from token: $token")
    }
}