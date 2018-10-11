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
import android.os.Bundle
import android.support.v7.preference.Preference
import android.view.*
import androidx.navigation.Navigation
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.ui.SettingsActivity
import dagger.android.support.AndroidSupportInjection

class SettingsFragment : BaseSettingsFragment() {

    private var assistantPreference: Preference? = null
    private var displayPreference: Preference? = null
    private var notificationPreference: Preference? = null
    private var alarmPreference: Preference? = null
    private var cameraPreference: Preference? = null
    private var mqttPreference: Preference? = null
    private var weatherPreference: Preference? = null
    private var sensorsPreference: Preference? = null
    private var aboutPreference: Preference? = null

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
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.title_settings))
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        assistantPreference = findPreference("button_key_voice")
        alarmPreference = findPreference("button_key_alarm")
        notificationPreference = findPreference("button_key_notifications")
        displayPreference = findPreference("button_key_display")
        cameraPreference = findPreference("button_key_camera")
        mqttPreference = findPreference("button_key_mqtt")
        sensorsPreference = findPreference("button_key_sensors")
        aboutPreference = findPreference("button_key_about")
        weatherPreference = findPreference("button_key_weather")

        assistantPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.assistant_action) }
            false
        }
        alarmPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.alarm_action) }
            false
        }
        notificationPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.notifications_action) }
            false
        }
        cameraPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.camera_action) }
            false
        }
        mqttPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.mqtt_action) }
            false
        }
        sensorsPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.sensors_action) }
            false
        }
        aboutPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.about_action) }
            false
        }
        weatherPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.weather_action) }
            false
        }
        displayPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.display_action) }
            false
        }
    }

    companion object {
    }
}