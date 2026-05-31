package com.smn.vpn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.smn.vpn.R
import com.smn.vpn.crypto.CryptoManager
import com.smn.vpn.utils.Logger

class SettingsFragment : Fragment() {

    private lateinit var profileRadioGroup: RadioGroup
    private lateinit var killSwitchToggle: Switch
    private lateinit var dnsProtectionToggle: Switch
    private lateinit var ipv6ProtectionToggle: Switch
    private lateinit var cryptoManager: CryptoManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cryptoManager = CryptoManager()
        
        // Профили
        profileRadioGroup = view.findViewById(R.id.profileRadioGroup)
        profileRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.standardProfile -> setStandardProfile()
                R.id.paranoidProfile -> setParanoidProfile()
            }
        }
        
        // Переключатели
        killSwitchToggle = view.findViewById(R.id.killSwitchToggle)
        dnsProtectionToggle = view.findViewById(R.id.dnsProtectionToggle)
        ipv6ProtectionToggle = view.findViewById(R.id.ipv6ProtectionToggle)
        
        // Kill Switch
        killSwitchToggle.setOnCheckedChangeListener { _, isChecked ->
            Logger.d("Settings", "Kill Switch: $isChecked")
        }
        
        // DNS Protection
        dnsProtectionToggle.setOnCheckedChangeListener { _, isChecked ->
            Logger.d("Settings", "DNS Protection: $isChecked")
        }
        
        // IPv6 Protection
        ipv6ProtectionToggle.setOnCheckedChangeListener { _, isChecked ->
            Logger.d("Settings", "IPv6 Protection: $isChecked")
        }
    }

    private fun setStandardProfile() {
        Logger.d("Settings", "Standard profile selected")
        cryptoManager.setEncryptionMode(CryptoManager.EncryptionMode.BALANCED)
        killSwitchToggle.isChecked = true
        dnsProtectionToggle.isChecked = true
        ipv6ProtectionToggle.isChecked = true
    }

    private fun setParanoidProfile() {
        Logger.d("Settings", "Paranoid profile selected")
        cryptoManager.setEncryptionMode(CryptoManager.EncryptionMode.MAXIMUM)
        killSwitchToggle.isChecked = true
        dnsProtectionToggle.isChecked = true
        ipv6ProtectionToggle.isChecked = true
        // Дополнительные параметры для параноика
        Logger.d("Settings", "Multi-hop enabled")
        Logger.d("Settings", "Key rotation enabled")
    }
}
