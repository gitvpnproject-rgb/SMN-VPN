package com.smn.vpn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.smn.vpn.R

/**
 * Настройки SMN VPN
 *
 * Два профиля:
 * 🟢 Обычный  — для всех, простые переключатели
 * 🔴 Параноик — продвинутые функции для тех кто знает зачем
 */
class SettingsActivity : AppCompatActivity() {

    // ─── Обычный профиль ───────────────────────────────────────
    // Kill Switch
    // DNS leak protection
    // IPv6 leak protection
    // Авто-переподключение
    // Умный выбор сервера

    // ─── Параноик профиль ──────────────────────────────────────
    // Multi-hop VPN (2-3 сервера)
    // Smart routing
    // Авто-ротация ключей
    // Обфускация трафика (против DPI)
    // Высокая анонимность на уровне сети

    private lateinit var tabNormal: TextView
    private lateinit var tabParanoid: TextView
    private lateinit var layoutNormal: LinearLayout
    private lateinit var layoutParanoid: LinearLayout

    // Обычные настройки
    private lateinit var switchKillSwitch: Switch
    private lateinit var switchDnsLeak: Switch
    private lateinit var switchIpv6Leak: Switch
    private lateinit var switchAutoReconnect: Switch
    private lateinit var switchSmartServer: Switch

    // Параноик настройки
    private lateinit var switchMultiHop: Switch
    private lateinit var spinnerHops: Spinner
    private lateinit var switchSmartRouting: Switch
    private lateinit var switchKeyRotation: Switch
    private lateinit var switchObfuscation: Switch
    private lateinit var switchHighAnon: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        loadSettings()
        setupTabs()
        setupListeners()
    }

    private fun initViews() {
        tabNormal = findViewById(R.id.tabNormal)
        tabParanoid = findViewById(R.id.tabParanoid)
        layoutNormal = findViewById(R.id.layoutNormal)
        layoutParanoid = findViewById(R.id.layoutParanoid)

        // Обычные
        switchKillSwitch = findViewById(R.id.switchKillSwitch)
        switchDnsLeak = findViewById(R.id.switchDnsLeak)
        switchIpv6Leak = findViewById(R.id.switchIpv6Leak)
        switchAutoReconnect = findViewById(R.id.switchAutoReconnect)
        switchSmartServer = findViewById(R.id.switchSmartServer)

        // Параноик
        switchMultiHop = findViewById(R.id.switchMultiHop)
        spinnerHops = findViewById(R.id.spinnerHops)
        switchSmartRouting = findViewById(R.id.switchSmartRouting)
        switchKeyRotation = findViewById(R.id.switchKeyRotation)
        switchObfuscation = findViewById(R.id.switchObfuscation)
        switchHighAnon = findViewById(R.id.switchHighAnon)
    }

    private fun setupTabs() {
        // По умолчанию — обычный профиль
        showNormalProfile()

        tabNormal.setOnClickListener { showNormalProfile() }
        tabParanoid.setOnClickListener { showParanoidProfile() }
    }

    private fun showNormalProfile() {
        layoutNormal.visibility = View.VISIBLE
        layoutParanoid.visibility = View.GONE
        tabNormal.setBackgroundResource(R.drawable.tab_active)
        tabParanoid.setBackgroundResource(R.drawable.tab_inactive)
    }

    private fun showParanoidProfile() {
        layoutNormal.visibility = View.GONE
        layoutParanoid.visibility = View.VISIBLE
        tabNormal.setBackgroundResource(R.drawable.tab_inactive)
        tabParanoid.setBackgroundResource(R.drawable.tab_active)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("smn_settings", MODE_PRIVATE)

        // Обычные — включены по умолчанию
        switchKillSwitch.isChecked = prefs.getBoolean("kill_switch", true)
        switchDnsLeak.isChecked = prefs.getBoolean("dns_leak", true)
        switchIpv6Leak.isChecked = prefs.getBoolean("ipv6_leak", true)
        switchAutoReconnect.isChecked = prefs.getBoolean("auto_reconnect", true)
        switchSmartServer.isChecked = prefs.getBoolean("smart_server", true)

        // Параноик — выключены по умолчанию
        switchMultiHop.isChecked = prefs.getBoolean("multi_hop", false)
        switchSmartRouting.isChecked = prefs.getBoolean("smart_routing", false)
        switchKeyRotation.isChecked = prefs.getBoolean("key_rotation", false)
        switchObfuscation.isChecked = prefs.getBoolean("obfuscation", false)
        switchHighAnon.isChecked = prefs.getBoolean("high_anon", false)

        // Количество хопов (2 или 3)
        val hops = prefs.getInt("hops_count", 2)
        val hopsAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            listOf("2 сервера", "3 сервера"))
        hopsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerHops.adapter = hopsAdapter
        spinnerHops.setSelection(if (hops == 3) 1 else 0)

        // Спиннер хопов активен только если multi-hop включён
        spinnerHops.isEnabled = switchMultiHop.isChecked
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("smn_settings", MODE_PRIVATE)

        // ─── Обычные настройки ───────────────────────
        switchKillSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("kill_switch", checked).apply()
            // Применяем немедленно если VPN активен
            applyKillSwitch(checked)
        }

        switchDnsLeak.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("dns_leak", checked).apply()
        }

        switchIpv6Leak.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("ipv6_leak", checked).apply()
        }

        switchAutoReconnect.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_reconnect", checked).apply()
        }

        switchSmartServer.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("smart_server", checked).apply()
        }

        // ─── Параноик настройки ───────────────────────
        switchMultiHop.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("multi_hop", checked).apply()
            spinnerHops.isEnabled = checked
        }

        spinnerHops.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                prefs.edit().putInt("hops_count", if (pos == 1) 3 else 2).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        switchSmartRouting.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("smart_routing", checked).apply()
        }

        switchKeyRotation.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("key_rotation", checked).apply()
        }

        switchObfuscation.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("obfuscation", checked).apply()
            if (checked) {
                Toast.makeText(this,
                    "Обфускация замедляет соединение, но скрывает VPN от DPI",
                    Toast.LENGTH_LONG).show()
            }
        }

        switchHighAnon.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("high_anon", checked).apply()
        }
    }

    private fun applyKillSwitch(enabled: Boolean) {
        // TODO: сообщить SMNVpnService об изменении Kill Switch
        // через LocalBroadcastManager или SharedPreferences
    }

    // Читается из SMNVpnService при старте
    companion object {
        fun isKillSwitchEnabled(context: android.content.Context): Boolean {
            return context.getSharedPreferences("smn_settings", MODE_PRIVATE)
                .getBoolean("kill_switch", true)
        }

        fun isMultiHopEnabled(context: android.content.Context): Boolean {
            return context.getSharedPreferences("smn_settings", MODE_PRIVATE)
                .getBoolean("multi_hop", false)
        }

        fun getHopsCount(context: android.content.Context): Int {
            return context.getSharedPreferences("smn_settings", MODE_PRIVATE)
                .getInt("hops_count", 2)
        }

        fun isObfuscationEnabled(context: android.content.Context): Boolean {
            return context.getSharedPreferences("smn_settings", MODE_PRIVATE)
                .getBoolean("obfuscation", false)
        }
    }
}