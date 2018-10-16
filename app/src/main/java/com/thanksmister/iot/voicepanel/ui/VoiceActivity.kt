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

package com.thanksmister.iot.voicepanel.ui

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.thanksmister.iot.voicepanel.BaseActivity
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.network.MQTTOptions
import com.thanksmister.iot.voicepanel.network.VoicePanelService
import com.thanksmister.iot.voicepanel.network.VoicePanelService.Companion.BROADCAST_ACTION_LISTENING_END
import com.thanksmister.iot.voicepanel.network.VoicePanelService.Companion.BROADCAST_ACTION_LISTENING_START
import com.thanksmister.iot.voicepanel.network.VoicePanelService.Companion.BROADCAST_ACTION_LOADING_COMPLETE
import com.thanksmister.iot.voicepanel.network.VoicePanelService.Companion.BROADCAST_ACTION_LOADING_START
import com.thanksmister.iot.voicepanel.network.VoicePanelService.Companion.BROADCAST_ALERT_MESSAGE
import com.thanksmister.iot.voicepanel.network.VoicePanelService.Companion.BROADCAST_EVENT_ALARM_MODE
import com.thanksmister.iot.voicepanel.network.VoicePanelService.Companion.BROADCAST_SCREEN_WAKE
import com.thanksmister.iot.voicepanel.network.VoicePanelService.Companion.BROADCAST_TOAST_MESSAGE
import com.thanksmister.iot.voicepanel.persistence.Configuration
import com.thanksmister.iot.voicepanel.ui.adapters.CommandAdapter
import com.thanksmister.iot.voicepanel.ui.viewmodels.VoiceViewModel
import com.thanksmister.iot.voicepanel.ui.views.AlarmDisableView
import com.thanksmister.iot.voicepanel.ui.views.ArmOptionsView
import com.thanksmister.iot.voicepanel.utils.AlarmUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_voice.*
import timber.log.Timber
import javax.inject.Inject

class VoiceActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: VoiceViewModel
    @Inject
    lateinit var mqttOptions: MQTTOptions
    private var localBroadCastManager: LocalBroadcastManager? = null
    private var voicePanelService: Intent? = null

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BROADCAST_ALERT_MESSAGE == intent.action) {
                val message = intent.getStringExtra(BROADCAST_ALERT_MESSAGE)
                dialogUtils.showAlertDialog(this@VoiceActivity, message)
            } else if (BROADCAST_TOAST_MESSAGE == intent.action) {
                val message = intent.getStringExtra(BROADCAST_ALERT_MESSAGE)
                Toast.makeText(this@VoiceActivity, message, Toast.LENGTH_SHORT).show()
            } else if (BROADCAST_SCREEN_WAKE == intent.action) {
                resetInactivityTimer()
            } else if (BROADCAST_ACTION_LOADING_START == intent.action) {
                loadingIcon.visibility = View.VISIBLE
                dotsIcon.visibility = View.GONE
            } else if (BROADCAST_ACTION_LOADING_COMPLETE == intent.action) {
                loadingIcon.visibility = View.GONE
                dotsIcon.visibility = View.VISIBLE
            } else if (BROADCAST_ACTION_LISTENING_START == intent.action) {
                loadingIcon.visibility = View.VISIBLE
                dotsIcon.visibility = View.GONE
            } else if (BROADCAST_ACTION_LISTENING_END == intent.action) {
                loadingIcon.visibility = View.GONE
                dotsIcon.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_voice)

        // Filter messages from service
        val filter = IntentFilter()
        filter.addAction(BROADCAST_ALERT_MESSAGE)
        filter.addAction(BROADCAST_TOAST_MESSAGE)
        filter.addAction(BROADCAST_SCREEN_WAKE)
        filter.addAction(BROADCAST_ACTION_LOADING_START)
        filter.addAction(BROADCAST_ACTION_LOADING_COMPLETE)
        filter.addAction(BROADCAST_ACTION_LISTENING_START)
        filter.addAction(BROADCAST_ACTION_LISTENING_END)
        localBroadCastManager = LocalBroadcastManager.getInstance(this)
        localBroadCastManager!!.registerReceiver(mBroadcastReceiver, filter)

        if(configuration.cameraEnabled && configuration.hasCameraDetections()) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }

        if (configuration.appPreventSleep) {
            window.addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if(configuration.isFirstTime) {
            AlertDialog.Builder(this@VoiceActivity, R.style.CustomAlertDialog)
                    .setMessage(getString(R.string.text_welcome_description))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        requestPermissions()
                    }
                    .show()
        } else if (!mqttOptions.isValid) {
            AlertDialog.Builder(this@VoiceActivity, R.style.CustomAlertDialog)
                    .setMessage(getString(R.string.dialog_connect_mqtt_broker))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val intent = SettingsActivity.createStartIntent(this@VoiceActivity)
                        startActivity(intent)
                    }
                    .show()
        }

        buttonSettings.setOnClickListener {launchSettings()}
        buttonAlarm.setOnClickListener {alarmCommand()}

        commandList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        commandList.layoutManager = linearLayoutManager

        // We must be sure we have the instantiated the view model before we observe.
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VoiceViewModel::class.java)
        lifecycle.addObserver(dialogUtils)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        if(configuration.nightModeChanged) {
            configuration.nightModeChanged = false // reset
            dayNightModeChanged() // reset screen brightness if day/night mode inactive
        }
        Timber.d("configuration.alarmEnabled ${configuration.alarmEnabled}")
        if(configuration.alarmEnabled) {
            buttonAlarm.visibility = View.VISIBLE
        } else {
            buttonAlarm.visibility = View.GONE
        }

        if(configuration.showIntentList) {
            listContainer.visibility = View.VISIBLE
        } else {
            listContainer.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        if (!configuration.isFirstTime && mqttOptions.isValid) {
            voicePanelService = Intent(this, VoicePanelService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(voicePanelService)
            } else {
                startService(voicePanelService)
            }
        }
    }

    override fun onDestroy() {
        if(localBroadCastManager != null) {
            localBroadCastManager!!.unregisterReceiver(mBroadcastReceiver)
        }
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val visibility: Int
        if (hasFocus ) {
            val decorView = window.decorView
            visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            decorView?.systemUiVisibility = visibility
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        Timber.d("onActivityResult requestCode: $requestCode")
        Timber.d("onActivityResult resultCode: $resultCode")
        if (requestCode == PERMISSIONS_REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(applicationContext)) {
                    Toast.makeText(this, getString(R.string.toast_write_permissions_granted), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.toast_write_permissions_denied), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeViewModel() {

        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({state ->
                    this@VoiceActivity.runOnUiThread {
                        Timber.d("onStart state: " + state)
                        if(configuration.alarmEnabled) {
                            when (state) {
                                AlarmUtils.STATE_DISARM -> {
                                    resetInactivityTimer()
                                    buttonAlarm.visibility = View.VISIBLE
                                    buttonAlarm.setColorFilter(ContextCompat.getColor(this@VoiceActivity, R.color.body_text), android.graphics.PorterDuff.Mode.SRC_IN);
                                }
                                AlarmUtils.STATE_ARM_AWAY -> {
                                    resetInactivityTimer()
                                    buttonAlarm.visibility = View.VISIBLE
                                    buttonAlarm.setColorFilter(ContextCompat.getColor(this@VoiceActivity, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                                }
                                AlarmUtils.STATE_ARM_HOME -> {
                                    resetInactivityTimer()
                                    buttonAlarm.visibility = View.VISIBLE
                                    buttonAlarm.setColorFilter(ContextCompat.getColor(this@VoiceActivity, R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);
                                }
                                AlarmUtils.STATE_TRIGGERED -> {
                                    resetInactivityTimer()
                                    buttonAlarm.visibility = View.VISIBLE
                                    buttonAlarm.setColorFilter(ContextCompat.getColor(this@VoiceActivity, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                                }
                                AlarmUtils.STATE_PENDING -> {
                                    resetInactivityTimer()
                                    buttonAlarm.visibility = View.VISIBLE
                                    buttonAlarm.setColorFilter(ContextCompat.getColor(this@VoiceActivity, R.color.body_text), android.graphics.PorterDuff.Mode.SRC_IN);
                                }
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get alarm state: " + error)}))

        disposable.add(viewModel.getIntentMessages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({commands ->
                    this@VoiceActivity.runOnUiThread {
                        Timber.d("commands: " + commands)
                        commandList.adapter = CommandAdapter(commands, null)
                        commandList.invalidate()
                        if(!TextUtils.isEmpty(commands[0].input)) {
                            textOutput.text = getString(R.string.text_command_input, commands[0].input)
                        }
                    }
                }, { error -> Timber.e("Database error: " + error)}))

        disposable.add(viewModel.getSun()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({sun ->
                    this@VoiceActivity.runOnUiThread {
                        if(configuration.useNightDayMode) {
                            dayNightModeCheck(sun.sun)
                        }
                    }
                }, { error -> Timber.e("Sun Data error: " + error)}))


        viewModel.getAlertMessage().observe(this, Observer { message ->
            Timber.d("getAlertMessage")
            dialogUtils.showAlertDialog(this@VoiceActivity, message!!)

        })

        viewModel.getToastMessage().observe(this, Observer { message ->
            Timber.d("getToastMessage")
            Toast.makeText(this@VoiceActivity, message, Toast.LENGTH_LONG).show()
        })
    }

    private fun dayNightModeCheck(sunValue:String?) {
        Timber.d("dayNightModeCheck")
        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if(sunValue == Configuration.SUN_BELOW_HORIZON && uiMode == android.content.res.Configuration.UI_MODE_NIGHT_NO) {
            Timber.d("Tis the night!")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        } else if (sunValue == Configuration.SUN_ABOVE_HORIZON && uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            Timber.d("Tis the day!")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }
    }

    private fun dayNightModeChanged() {
        Timber.d("dayNightModeChanged")
        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (!configuration.useNightDayMode && uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            Timber.d("Tis the day!")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }
    }

    private fun requestPermissions(): Boolean {
        Timber.d("requestPermissions")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this@VoiceActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@VoiceActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@VoiceActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS)
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty()) {
                    var permissionsDenied = false
                    for (permission in grantResults) {
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            permissionsDenied = true;
                            break
                        }
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun launchSettings() {
        val intent = SettingsActivity.createStartIntent(this@VoiceActivity)
        startActivity(intent)
    }

    // This is used as a backup to disable the alarm in case voice assistant doesn't work or
    // the alarm is too loud to hear
    private fun alarmCommand() {
        Timber.d("alarmCommand alarm state: ${configuration.alarmState}")
        if(configuration.alarmState == AlarmUtils.STATE_ARM_HOME || configuration.alarmState == AlarmUtils.STATE_ARM_AWAY ||
                configuration.alarmState == AlarmUtils.STATE_PENDING || configuration.alarmState == AlarmUtils.STATE_TRIGGERED) {
            dialogUtils.showAlarmDisableDialog(this@VoiceActivity, object : AlarmDisableView.ViewListener {
                override fun onComplete(code: Int) {
                    dialogUtils.clearDialogs()
                    val intent = Intent(BROADCAST_EVENT_ALARM_MODE)
                    intent.putExtra(BROADCAST_EVENT_ALARM_MODE, AlarmUtils.COMMAND_DISARM)
                    val bm = LocalBroadcastManager.getInstance(applicationContext)
                    bm.sendBroadcast(intent)
                }
                override fun onError() {
                    Toast.makeText(this@VoiceActivity, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show()
                }
                override fun onCancel() {
                    dialogUtils.clearDialogs()
                }
            }, configuration.alarmCode)
        } else if (configuration.alarmState == AlarmUtils.STATE_DISARM) {
            dialogUtils.showArmOptionsDialog(this@VoiceActivity, object : ArmOptionsView.ViewListener {
                override fun onArmHome() {
                    val intent = Intent(BROADCAST_EVENT_ALARM_MODE)
                    intent.putExtra(BROADCAST_EVENT_ALARM_MODE, AlarmUtils.COMMAND_ARM_HOME)
                    val bm = LocalBroadcastManager.getInstance(applicationContext)
                    bm.sendBroadcast(intent)
                    dialogUtils.clearDialogs()
                }
                override fun onArmAway() {
                    val intent = Intent(BROADCAST_EVENT_ALARM_MODE)
                    intent.putExtra(BROADCAST_EVENT_ALARM_MODE, AlarmUtils.COMMAND_ARM_AWAY)
                    val bm = LocalBroadcastManager.getInstance(applicationContext)
                    bm.sendBroadcast(intent)
                    dialogUtils.clearDialogs()
                }
            })
        }
    }

    companion object {
        fun createStartIntent(context: Context): Intent {
            return Intent(context, VoiceActivity::class.java)
        }
        const val PERMISSIONS_REQUEST_WRITE_SETTINGS = 200
        const val REQUEST_PERMISSIONS = 88
    }
}
