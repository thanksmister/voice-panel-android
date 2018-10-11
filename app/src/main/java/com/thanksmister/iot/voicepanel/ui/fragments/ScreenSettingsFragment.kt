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
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.persistence.Configuration
import com.thanksmister.iot.voicepanel.ui.SettingsActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ScreenSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration

    private var fullScreenPreference: SwitchPreference? = null
    private var dayNightPreference: SwitchPreference? = null
    private var preventSleepPreference: SwitchPreference? = null

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
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.preference_title_display))
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_screen)
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

        fullScreenPreference = findPreference(Configuration.PREF_FULL_SCXREEN) as SwitchPreference
        fullScreenPreference!!.isChecked = configuration.fullScreen

        preventSleepPreference = findPreference(getString(R.string.key_setting_app_preventsleep)) as SwitchPreference
        preventSleepPreference!!.isChecked = configuration.appPreventSleep

        dayNightPreference = findPreference(Configuration.PREF_DAY_NIGHT_MODE) as SwitchPreference
        dayNightPreference!!.isChecked = configuration.useNightDayMode
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Configuration.PREF_FULL_SCXREEN -> {
                val fullscreen = fullScreenPreference!!.isChecked
                configuration.fullScreen = fullscreen
            }
            Configuration.PREF_DAY_NIGHT_MODE -> {
                val checked = dayNightPreference!!.isChecked
                configuration.useNightDayMode = checked
                configuration.nightModeChanged = true
            }
            getString(R.string.key_setting_app_preventsleep) -> {
                val checked = preventSleepPreference!!.isChecked
                configuration.appPreventSleep = checked
            }
        }
    }
}