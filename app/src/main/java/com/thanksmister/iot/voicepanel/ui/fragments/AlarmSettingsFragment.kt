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

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.thanksmister.iot.voicepanel.BaseActivity
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.network.MQTTOptions
import com.thanksmister.iot.voicepanel.persistence.Configuration
import com.thanksmister.iot.voicepanel.persistence.Configuration.Companion.PREF_COMMAND_TOPIC
import com.thanksmister.iot.voicepanel.persistence.Configuration.Companion.PREF_STATE_TOPIC
import com.thanksmister.iot.voicepanel.persistence.Configuration.Companion.PREF_SYSTEM_NOTIFICATIONS
import com.thanksmister.iot.voicepanel.persistence.Configuration.Companion.PREF_SYSTEM_SOUNDS
import com.thanksmister.iot.voicepanel.ui.views.AlarmCodeView
import com.thanksmister.iot.voicepanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class AlarmSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var mqttOptions: MQTTOptions
    @Inject lateinit var dialogUtils: DialogUtils

    private var alarmPreference: SwitchPreference? = null
    private var stateTopicPreference: EditTextPreference? = null
    private var commandTopicPreference: EditTextPreference? = null
    private var systemPreference: SwitchPreference? = null
    private var soundPreference: SwitchPreference? = null

    private var defaultCode: Int = 0
    private var tempCode: Int = 0
    private var confirmCode = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey : String?) {
        addPreferencesFromResource(R.xml.preferences_alarm)
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
        alarmPreference = findPreference(getString(R.string.key_setting_alarm_enabled)) as SwitchPreference
        stateTopicPreference = findPreference(PREF_STATE_TOPIC) as EditTextPreference
        soundPreference = findPreference(Configuration.PREF_SYSTEM_SOUNDS) as SwitchPreference
        systemPreference = findPreference(Configuration.PREF_SYSTEM_NOTIFICATIONS) as SwitchPreference
        systemPreference!!.isChecked = configuration.hasSystemAlerts()
        soundPreference!!.isChecked = configuration.systemSounds

        commandTopicPreference!!.text = mqttOptions.getAlarmCommandTopic()
        commandTopicPreference!!.summary = mqttOptions.getAlarmCommandTopic()

        stateTopicPreference!!.text = mqttOptions.getAlarmStateTopic()
        stateTopicPreference!!.summary = mqttOptions.getAlarmStateTopic()

        val buttonPreference = findPreference(Configuration.PREF_ALARM_CODE)
        buttonPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showAlarmCodeDialog()
            true
        }
    }

    @SuppressLint("InlinedApi")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.key_setting_alarm_enabled) -> {
                val value = alarmPreference!!.isChecked
                configuration.alarmEnabled = value
            }
            PREF_STATE_TOPIC -> {
                val value = stateTopicPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    mqttOptions.setAlarmTopic(value)
                    stateTopicPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                    stateTopicPreference!!.text = mqttOptions.getAlarmStateTopic()
                }
            }
            PREF_COMMAND_TOPIC -> {
                val value = commandTopicPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    mqttOptions.setCommandTopic(value)
                    commandTopicPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                    commandTopicPreference!!.text = mqttOptions.getAlarmCommandTopic()
                }
            }
            PREF_SYSTEM_SOUNDS -> {
                val sounds = soundPreference!!.isChecked
                configuration.systemSounds = sounds
            }
            PREF_SYSTEM_NOTIFICATIONS -> {
                val checked = systemPreference!!.isChecked
                configuration.systemAlerts = checked
            }
        }
    }

    private fun showAlarmCodeDialog() {
        // store the default alarm code
        defaultCode = configuration.alarmCode
        if (activity != null && isAdded) {
            dialogUtils.showCodeDialog(activity as BaseActivity, confirmCode, object : AlarmCodeView.ViewListener {
                override fun onComplete(code: Int) {
                    if (code == defaultCode) {
                        confirmCode = false
                        dialogUtils.clearDialogs()
                        Toast.makeText(activity, R.string.toast_code_match, Toast.LENGTH_LONG).show()
                    } else if (!confirmCode) {
                        tempCode = code
                        confirmCode = true
                        dialogUtils.clearDialogs()
                        if (activity != null && isAdded) {
                            showAlarmCodeDialog()
                        }
                    } else if (code == tempCode) {
                        configuration.isFirstTime = false;
                        configuration.alarmCode = tempCode
                        tempCode = 0
                        confirmCode = false
                        dialogUtils.clearDialogs()
                        Toast.makeText(activity, R.string.toast_code_changed, Toast.LENGTH_LONG).show()
                    } else {
                        tempCode = 0
                        confirmCode = false
                        dialogUtils.clearDialogs()
                        Toast.makeText(activity, R.string.toast_code_not_match, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onError() {}
                override fun onCancel() {
                    confirmCode = false
                    dialogUtils.clearDialogs()
                    Toast.makeText(activity, R.string.toast_code_unchanged, Toast.LENGTH_SHORT).show()
                }
            }, DialogInterface.OnCancelListener {
                confirmCode = false
                Toast.makeText(activity, R.string.toast_code_unchanged, Toast.LENGTH_SHORT).show()
            }, configuration.systemSounds)
        }
    }
}