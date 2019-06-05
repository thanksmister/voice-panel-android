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
import com.thanksmister.iot.voicepanel.modules.SnipsOptions
import com.thanksmister.iot.voicepanel.persistence.Configuration
import com.thanksmister.iot.voicepanel.persistence.Configuration.Companion.PREF_FACE_WAKE_WORD
import com.thanksmister.iot.voicepanel.persistence.Configuration.Companion.PREF_HOTWORD_RESPONSE
import com.thanksmister.iot.voicepanel.persistence.Configuration.Companion.PREF_SHOW_INTENT_LIST
import com.thanksmister.iot.voicepanel.persistence.Configuration.Companion.PREF_HOTWORD_RESPONSE_ENABLED
import com.thanksmister.iot.voicepanel.ui.SettingsActivity
import com.thanksmister.iot.voicepanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AssistantSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var snipsOptions: SnipsOptions
    @Inject lateinit var dialogUtils: DialogUtils

    private var hotwordSensitivityPreference: EditTextPreference? = null
    private var faceWakeWordPreference: SwitchPreference? = null
    private var intentListPreference: SwitchPreference? = null
    private var probabilityPreference: EditTextPreference? = null
    private var faceWakeIntervalPreference: EditTextPreference? = null

    private var enableHotwordResponse: SwitchPreference? = null
    private var hotwordResponsePreference: EditTextPreference? = null

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
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.title_voice_assistant))
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_assistant)
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

        probabilityPreference = findPreference(getString(R.string.key_snips_probability)) as EditTextPreference
        probabilityPreference!!.setDefaultValue(snipsOptions.nluProbability.toString())
        probabilityPreference!!.text = snipsOptions.nluProbability.toString()

        faceWakeIntervalPreference = findPreference(getString(R.string.key_snips_face_wake_interval)) as EditTextPreference
        faceWakeIntervalPreference!!.setDefaultValue(snipsOptions.faceWakeDelayTime.toString())
        faceWakeIntervalPreference!!.text = snipsOptions.faceWakeDelayTime.toString()

        faceWakeWordPreference = findPreference(PREF_FACE_WAKE_WORD) as SwitchPreference
        faceWakeWordPreference!!.isChecked = configuration.faceWakeWord

        intentListPreference = findPreference(PREF_SHOW_INTENT_LIST) as SwitchPreference
        intentListPreference!!.isChecked = configuration.showIntentList

        hotwordSensitivityPreference = findPreference(getString(R.string.key_snips_hotword_sensitivity)) as EditTextPreference
        hotwordSensitivityPreference!!.setDefaultValue(snipsOptions.withHotwordSensitivity.toString())
        hotwordSensitivityPreference!!.text = snipsOptions.withHotwordSensitivity.toString()

        enableHotwordResponse = findPreference(PREF_HOTWORD_RESPONSE_ENABLED) as SwitchPreference
        enableHotwordResponse?.isChecked = configuration.hasHotwordResponse

        hotwordResponsePreference = findPreference(PREF_HOTWORD_RESPONSE) as EditTextPreference
        hotwordResponsePreference?.setDefaultValue(configuration.hotwordResponse)
        hotwordResponsePreference?.text = configuration.hotwordResponse
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.key_snips_hotword_sensitivity) -> {
                val value = hotwordSensitivityPreference!!.text
                try {
                    if(!TextUtils.isEmpty(value)) {
                        snipsOptions.withHotwordSensitivity = value.toFloat()
                        if(!(value.toFloat() in 0.0..1.0)) {
                            Toast.makeText(activity, getString(R.string.error_hotword_sensitivity), Toast.LENGTH_LONG).show()
                        }
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        hotwordSensitivityPreference!!.setDefaultValue(snipsOptions.withHotwordSensitivity.toString())
                        hotwordSensitivityPreference!!.text = snipsOptions.withHotwordSensitivity.toString()
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        hotwordSensitivityPreference!!.setDefaultValue(snipsOptions.withHotwordSensitivity.toString())
                        hotwordSensitivityPreference!!.text = snipsOptions.withHotwordSensitivity.toString()
                    }
                }
            }
            getString(R.string.key_snips_probability) -> {
                val value = probabilityPreference!!.text
                try {
                    val floatValue = value.toFloatOrNull()
                    if(floatValue != null) {
                        snipsOptions.nluProbability = floatValue
                        if(!(value.toFloat() in 0.0..1.0)) {
                           Toast.makeText(activity, getString(R.string.error_snips_probability), Toast.LENGTH_LONG).show()
                        }
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        probabilityPreference!!.setDefaultValue(snipsOptions.nluProbability.toString())
                        probabilityPreference!!.text = snipsOptions.nluProbability.toString()

                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        probabilityPreference!!.setDefaultValue(snipsOptions.nluProbability.toString())
                        probabilityPreference!!.text = snipsOptions.nluProbability.toString()
                    }
                }
            }
            getString(R.string.key_snips_face_wake_interval) -> {
                val value = faceWakeIntervalPreference!!.text
                try {
                    if(!TextUtils.isEmpty(value)) {
                        snipsOptions.faceWakeDelayTime = value.toInt()
                        if(!(value.toInt() in 0..60)) {
                            Toast.makeText(activity, getString(R.string.error_snips_wake_interval), Toast.LENGTH_LONG).show()
                        }
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        faceWakeIntervalPreference!!.setDefaultValue(snipsOptions.faceWakeDelayTime.toString())
                        faceWakeIntervalPreference!!.text = snipsOptions.faceWakeDelayTime.toString()

                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        faceWakeIntervalPreference!!.setDefaultValue(snipsOptions.faceWakeDelayTime.toString())
                        faceWakeIntervalPreference!!.text = snipsOptions.faceWakeDelayTime.toString()
                    }
                }
            }
            PREF_FACE_WAKE_WORD -> {
                configuration.faceWakeWord = faceWakeWordPreference!!.isChecked
            }
            PREF_SHOW_INTENT_LIST -> {
                configuration.showIntentList = intentListPreference!!.isChecked
            }
            PREF_HOTWORD_RESPONSE -> {
                val value = hotwordResponsePreference?.text
                if(value.isNullOrEmpty()) {
                    val default = getString(android.R.string.yes)
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                    hotwordResponsePreference?.setDefaultValue(default)
                    hotwordResponsePreference?.text = default
                } else {
                    configuration.hotwordResponse = value
                }
            }
        }
    }
}