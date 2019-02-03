/*
 * Copyright (c) 2018 ThanksMister LLC
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.thanksmister.iot.voicepanel.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.network.MQTTOptions
import com.thanksmister.iot.voicepanel.persistence.Configuration
import com.thanksmister.iot.voicepanel.ui.SettingsActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class MqttSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var mqttOptions: MQTTOptions

    private var brokerPreference: EditTextPreference? = null
    private var clientPreference: EditTextPreference? = null
    private var portPreference: EditTextPreference? = null
    private var userNamePreference: EditTextPreference? = null
    private var sslPreference: SwitchPreference? = null
    private var passwordPreference: EditTextPreference? = null
    private var baseTopicPreference: EditTextPreference? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set title bar
        if((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.title_mqtt_settings))
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey : String?) {
        addPreferencesFromResource(R.xml.preferences_mqtt)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        brokerPreference = findPreference(getString(R.string.key_setting_mqtt_servername)) as EditTextPreference
        clientPreference = findPreference(getString(R.string.key_setting_mqtt_clientid)) as EditTextPreference
        portPreference = findPreference(getString(R.string.key_setting_mqtt_serverport)) as EditTextPreference
        userNamePreference = findPreference(getString(R.string.key_setting_mqtt_username)) as EditTextPreference
        passwordPreference = findPreference(getString(R.string.key_setting_mqtt_password)) as EditTextPreference
        sslPreference = findPreference(getString(R.string.key_setting_mqtt_tls_enabled)) as SwitchPreference
        baseTopicPreference = findPreference(getString(R.string.key_setting_mqtt_basetopic)) as EditTextPreference

        baseTopicPreference!!.text = mqttOptions.getBaseTopic()
        brokerPreference!!.text = mqttOptions.getBroker()
        clientPreference!!.text = mqttOptions.getClientId()
        portPreference!!.text = mqttOptions.getPort().toString()
        userNamePreference!!.text = mqttOptions.getUsername()
        passwordPreference!!.text = mqttOptions.getPassword()
        sslPreference!!.isChecked = mqttOptions.getTlsConnection()

        mqttOptions.getBaseTopic().takeIf { !it.isEmpty() }.let {
            baseTopicPreference!!.setDefaultValue(mqttOptions.getBaseTopic())
            baseTopicPreference!!.text = mqttOptions.getBaseTopic()
            baseTopicPreference!!.summary = mqttOptions.getBaseTopic()
        }
        mqttOptions.getBroker().takeIf { !it.isEmpty() }.let {
            brokerPreference!!.text = mqttOptions.getBroker()
            brokerPreference!!.summary = mqttOptions.getBroker()
        }
        mqttOptions.getClientId().takeIf { !it.isEmpty() }.let {
            clientPreference!!.text = mqttOptions.getClientId()
            clientPreference!!.summary = mqttOptions.getClientId()
        }
        mqttOptions.getPort().toString().takeIf { !it.isEmpty() }.let {
            portPreference!!.text = mqttOptions.getPort().toString()
            portPreference!!.summary = mqttOptions.getPort().toString()
        }
        mqttOptions.getUsername().takeIf { !it.isEmpty() }.let {
            userNamePreference!!.text = mqttOptions.getUsername()
        }
        mqttOptions.getPassword().takeIf { !it.isEmpty() }.let {
            passwordPreference!!.text = mqttOptions.getPassword()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value: String
        when (key) {
            getString(R.string.key_setting_mqtt_servername) -> {
                value = brokerPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    mqttOptions.setBroker(value)
                    brokerPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                }
            }
            getString(R.string.key_setting_mqtt_clientid) -> {
                value = clientPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    mqttOptions.setClientId(value)
                    clientPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                    clientPreference!!.text = mqttOptions.getClientId()
                }
            }
            getString(R.string.key_setting_mqtt_serverport) -> {
                value = portPreference!!.text
                if (!TextUtils.isEmpty(value) && value.matches("[0-9]+".toRegex())) {
                    mqttOptions.setPort(Integer.valueOf(value)!!)
                    portPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    portPreference!!.text = mqttOptions.getPort().toString()
                }
            }
            getString(R.string.key_setting_mqtt_basetopic) -> {
                value = baseTopicPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    mqttOptions.setBaseTopic(value)
                    baseTopicPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                    baseTopicPreference!!.text = mqttOptions.getBaseTopic()
                    baseTopicPreference!!.summary = mqttOptions.getBaseTopic()
                }
            }
            getString(R.string.key_setting_mqtt_servername) -> {
                value = userNamePreference!!.text
                mqttOptions.setUsername(value)
                userNamePreference!!.text = value
            }
            getString(R.string.key_setting_mqtt_password) -> {
                value = passwordPreference!!.text
                mqttOptions.setPassword(value)
                passwordPreference!!.text = value
            }
            getString(R.string.key_setting_mqtt_tls_enabled) -> {
                val checked = sslPreference!!.isChecked
                mqttOptions.setTlsConnection(checked)
            }
        }
    }

    private fun toStars(textToStars: String?): String {
        var text = textToStars
        val sb = StringBuilder()
        for (i in 0 until text!!.length) {
            sb.append('*')
        }
        text = sb.toString()
        return text
    }
}