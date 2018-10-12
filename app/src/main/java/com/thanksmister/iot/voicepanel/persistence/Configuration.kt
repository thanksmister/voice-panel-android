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

package com.thanksmister.iot.voicepanel.persistence

import android.content.Context
import android.content.SharedPreferences
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.utils.AlarmUtils
import com.thanksmister.iot.voicepanel.utils.AlarmUtils.Companion.MODE_DISARM
import com.thanksmister.iot.voicepanel.utils.AlarmUtils.Companion.STATE_DISARM
import javax.inject.Inject

class Configuration @Inject
constructor(private val context: Context, private val sharedPreferences: SharedPreferences) {

    var webUrl: String?
        get() = getStringPref(PREF_WEB_URL, null)
        set(value) = this.setStringPref(PREF_WEB_URL, value)

    var appPreventSleep: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_app_preventsleep), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_app_preventsleep), value)

    var useNightDayMode: Boolean
        get() = getBoolPref(PREF_DAY_NIGHT_MODE, false)
        set(value) = this.setBoolPref(PREF_DAY_NIGHT_MODE, value)

    var dayNightMode: String
        get() = getStringPref(DISPLAY_MODE_DAY_NIGHT, SUN_ABOVE_HORIZON)
        set(value) = this.setStringPref(DISPLAY_MODE_DAY_NIGHT, value)

    var nightModeChanged: Boolean
        get() = getBoolPref(DISPLAY_MODE_DAY_NIGHT_CHANGED, false)
        set(value) = this.setBoolPref(DISPLAY_MODE_DAY_NIGHT_CHANGED, value)

    var dayNightModeStartTime: String
        get() = getStringPref(PREF_MODE_DAY_NIGHT_START, DAY_NIGHT_START_VALUE_DEFAULT)
        set(value) = this.setStringPref(PREF_MODE_DAY_NIGHT_START, value)

    var dayNightModeEndTime: String
        get() = getStringPref(PREF_MODE_DAY_NIGHT_END, DAY_NIGHT_END_VALUE_DEFAULT)
        set(value) = this.setStringPref(PREF_MODE_DAY_NIGHT_END, value)

    var alarmState: String
        get() = getStringPref(PREF_ALARM_STATE, STATE_DISARM)
        set(value) = this.setStringPref(PREF_ALARM_STATE, value)

    var alarmStateTopic: String
        get() = getStringPref(PREF_STATE_TOPIC, AlarmUtils.ALARM_STATE_TOPIC)
        set(value) = this.setStringPref(PREF_STATE_TOPIC, value)

    var alarmCommandTopic: String
        get() = getStringPref(PREF_COMMAND_TOPIC, AlarmUtils.ALARM_COMMAND_TOPIC)
        set(value) = this.setStringPref(PREF_COMMAND_TOPIC, value)

    var fullScreen: Boolean
        get() = getBoolPref(PREF_FULL_SCXREEN, true)
        set(value) = this.setBoolPref(PREF_FULL_SCXREEN, value)

    var systemAlerts: Boolean
        get() = getBoolPref(PREF_SYSTEM_NOTIFICATIONS, false)
        set(value) = this.setBoolPref(PREF_SYSTEM_NOTIFICATIONS, value)

    var inactivityTime: Long
        get() = getPrefLong(PREF_INACTIVITY_TIME, 300000)
        set(value) = setPrefLong(PREF_INACTIVITY_TIME, value)

    var isFirstTime: Boolean
        get() = getBoolPref(PREF_FIRST_TIME, true)
        set(value) = setBoolPref(PREF_FIRST_TIME, value)

    var initializedVoice: Boolean
        get() = getBoolPref(PREF_VOICE_INIT, false)
        set(value) = setBoolPref(PREF_VOICE_INIT, value)

    var alarmEnabled: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_alarm_enabled), false)
        set(value) = setBoolPref(context.getString(R.string.key_setting_alarm_enabled), value)

    var fingerPrint: Boolean
        get() = getBoolPref(PREF_FINGERPRINT, false)
        set(value) = setBoolPref(PREF_FINGERPRINT, value)

    var pendingTime: Int
        get() = getPrefInt(PREF_PENDING_TIME, AlarmUtils.PENDING_TIME)
        set(value) = setPrefInt(PREF_PENDING_TIME, value)

    var delayTime: Int
        get() = getPrefInt(PREF_DELAY_TIME, AlarmUtils.DELAY_TIME)
        set(value) = setPrefInt(PREF_DELAY_TIME, value)

    var delayHomeTime: Int
        get() = getPrefInt(PREF_HOME_DELAY_TIME, AlarmUtils.DELAY_HOME_TIME)
        set(value) = setPrefInt(PREF_HOME_DELAY_TIME, value)

    var delayAwayTime: Int
        get() = getPrefInt(PREF_AWAY_DELAY_TIME, AlarmUtils.DELAY_AWAY_TIME)
        set(value) = setPrefInt(PREF_AWAY_DELAY_TIME, value)

    var pendingHomeTime: Int
        get() = getPrefInt(PREF_HOME_PENDING_TIME, AlarmUtils.PENDING_HOME_TIME)
        set(value) = setPrefInt(PREF_HOME_PENDING_TIME, value)

    var pendingAwayTime: Int
        get() = getPrefInt(PREF_AWAY_PENDING_TIME, AlarmUtils.PENDING_AWAY_TIME)
        set(value) = setPrefInt(PREF_AWAY_PENDING_TIME, value)

    var disableTime: Int
        get() = getPrefInt(PREF_DISABLE_DIALOG_TIME, AlarmUtils.DISABLE_TIME)
        set(value) = setPrefInt(PREF_DISABLE_DIALOG_TIME, value)

    var faceWakeWord: Boolean
        get() = getBoolPref(PREF_FACE_WAKE_WORD, false)
        set(value) = setBoolPref(PREF_FACE_WAKE_WORD, value)

    var showIntentList: Boolean
        get() = getBoolPref(PREF_SHOW_INTENT_LIST, true)
        set(value) = setBoolPref(PREF_SHOW_INTENT_LIST, value)

    var alarmCode: Int
        get() = getPrefInt(PREF_ALARM_CODE, 1234)
        set(value) = setPrefInt(PREF_ALARM_CODE, value)

    var platformBar: Boolean
        get() = getBoolPref(PREF_PLATFORM_BAR, true)
        set(value) = this.setBoolPref(PREF_PLATFORM_BAR, value)

    var systemSounds: Boolean
        get() = getBoolPref(PREF_SYSTEM_SOUNDS, true)
        set(value) = this.setBoolPref(PREF_SYSTEM_SOUNDS, value)

    var telegramChatId: String
        get() = getStringPref(PREF_TELEGRAM_CHAT_ID, "")
        set(value) = this.setStringPref(PREF_TELEGRAM_CHAT_ID, value)

    var telegramToken: String
        get() = getStringPref(PREF_TELEGRAM_TOKEN, "")
        set(value) = this.setStringPref(PREF_TELEGRAM_TOKEN, value)

    var cameraMotionWake: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_camera_motionwake), false)
        set(value) = setBoolPref(context.getString(R.string.key_setting_camera_motionwake), value)

    var cameraFaceWake: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_camera_facewake), false)
        set(value) = setBoolPref(context.getString(R.string.key_setting_camera_facewake), value)

    var cameraFPS: Float
        get() = getStringPref(context.getString(R.string.key_setting_camera_fps), "15f").toFloat()
        set(value) = this.setStringPref(context.getString(R.string.key_setting_camera_fps), value.toString())

    var cameraId: Int
        get() = getPrefInt(context.getString(R.string.key_setting_camera_cameraid), 0)
        set(value) = setPrefInt(context.getString(R.string.key_setting_camera_cameraid), value)

    var cameraMotionMinLuma: Int
        get() = getPrefInt(context.getString(R.string.key_setting_camera_motionminluma),
                context.getString(R.string.default_setting_camera_motionminluma).toInt())
        set(value) = setPrefInt(context.getString(R.string.key_setting_camera_motionminluma), value)

    var mqttSensorFrequency: Int
        get() = getPrefInt(context.getString(R.string.key_setting_mqtt_sensorfrequency),
                context.getString(R.string.default_setting_mqtt_sensorfrequency).toInt())
        set(value) = setPrefInt(context.getString(R.string.key_setting_mqtt_sensorfrequency), value)

    var httpPort: Int
        get() = getPrefInt(context.getString(R.string.key_setting_http_port),
                context.getString(R.string.default_setting_http_port).toInt())
        set(value) = setPrefInt(context.getString(R.string.key_setting_http_port), value)

    var cameraMotionLeniency: Int
        get() = getPrefInt(context.getString(R.string.key_setting_camera_motionleniency),
                context.getString(R.string.default_setting_camera_motionleniency).toInt())
        set(value) = setPrefInt(context.getString(R.string.key_setting_camera_motionleniency), value)

    var httpMJPEGMaxStreams: Int
        get() = getPrefInt(context.getString(R.string.key_setting_http_mjpegmaxstreams),
                context.getString(R.string.default_setting_http_mjpegmaxstreams).toInt())
        set(value) = setPrefInt(context.getString(R.string.key_setting_http_mjpegmaxstreams), value)

    var motionResetTime: Int
        get() = getPrefInt(context.getString(R.string.key_setting_motion_clear),
                context.getString(R.string.default_motion_clear).toInt())
        set(value) = setPrefInt(context.getString(R.string.key_setting_motion_clear), value)

    var cameraEnabled: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_camera_enabled), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_camera_enabled), value)

    var cameraMotionEnabled: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_camera_motionenabled), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_camera_motionenabled), value)

    var cameraMotionWakeEnabled: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_camera_motionwake), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_camera_motionwake), value)

    var sensorsEnabled: Boolean
        get() = getBoolPref(PREF_SENSOR_ENABLED, false)
        set(value) = this.setBoolPref(PREF_SENSOR_ENABLED, value)

    var cameraFaceEnabled: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_camera_faceenabled), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_camera_faceenabled), value)

    var cameraQRCodeEnabled: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_camera_qrcodeenabled), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_camera_qrcodeenabled), value)

    var httpMJPEGEnabled: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_http_mjpegenabled), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_http_mjpegenabled), value)

    var deviceSensors: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_sensors_enabled), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_sensors_enabled), value)

    var deviceSensorFrequency: Int
        get() = getPrefInt(context.getString(R.string.key_setting_mqtt_sensorfrequency),
                context.getString(R.string.default_setting_mqtt_sensorfrequency).toInt())
        set(value) = setPrefInt(context.getString(R.string.key_setting_mqtt_sensorfrequency), value)

    var appShowActivity: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_app_showactivity), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_app_showactivity), value)

    var testZoomLevel: Float
        get() = getStringPref(context.getString(R.string.key_setting_test_zoomlevel),
                context.getString(R.string.default_setting_test_zoomlevel)).toFloat()
        set(value) = this.setStringPref(context.getString(R.string.key_setting_test_zoomlevel), value.toString())

    var browserUserAgent: String
        get() = getStringPref(context.getString(R.string.key_setting_browser_user_agent),
                context.getString(R.string.default_browser_user_agent))
        set(value) = this.setStringPref(context.getString(R.string.key_setting_browser_user_agent), value)

    var mqttBroker: String
        get() = getStringPref(context.getString(R.string.key_setting_mqtt_servername), context.getString(R.string.default_setting_mqtt_servername))
        set(value) = this.setStringPref(context.getString(R.string.key_setting_mqtt_servername), value)

    var mqttServerPort: Int
        get() = getStringPref(context.getString(R.string.key_setting_mqtt_serverport), context.getString(R.string.default_setting_mqtt_serverport)).trim().toInt()
        set(value) = this.setPrefInt(context.getString(R.string.key_setting_mqtt_serverport), value)

    var mqttBaseTopic: String
        get() = getStringPref(context.getString(R.string.key_setting_mqtt_basetopic),
                context.getString(R.string.default_setting_mqtt_basetopic))
        set(value) = this.setStringPref(context.getString(R.string.key_setting_mqtt_basetopic), value)

    var mqttTlsEnabled: Boolean
        get() = getBoolPref(context.getString(R.string.key_setting_mqtt_tls_enabled), false)
        set(value) = this.setBoolPref(context.getString(R.string.key_setting_mqtt_tls_enabled), value)

    var mqttClientId: String
        get() = getStringPref(context.getString(R.string.key_setting_mqtt_clientid),
                context.getString(R.string.default_setting_mqtt_clientid))
        set(value) = this.setStringPref(context.getString(R.string.key_setting_mqtt_clientid), value)

    var mqttUsername: String
        get() = getStringPref(context.getString(R.string.key_setting_mqtt_username),
                context.getString(R.string.default_setting_mqtt_username))
        set(value) = this.setStringPref(context.getString(R.string.key_setting_mqtt_username), value)

    var mqttPassword: String
        get() = getStringPref(context.getString(R.string.key_setting_mqtt_password),
                context.getString(R.string.default_setting_mqtt_password))
        set(value) = this.setStringPref(context.getString(R.string.key_setting_mqtt_password), value)

    fun hasPlatformModule(): Boolean {
        return getBoolPref(PREF_MODULE_WEB, false)
    }

    fun setWebModule(value: Boolean) {
        this.setBoolPref(PREF_MODULE_WEB, value)
    }

    fun hasPlatformChange(): Boolean {
        return getBoolPref(PREF_PLATFORM_CHANGED, false)
    }

    fun setHasPlatformChange(value: Boolean) {
        setBoolPref(PREF_PLATFORM_CHANGED, value)
    }

    fun setTssModule(value: Boolean) {
        this.setBoolPref(PREF_MODULE_TSS, value)
    }

    fun hasSystemAlerts(): Boolean {
        return getBoolPref(PREF_SYSTEM_NOTIFICATIONS, false)
    }

    fun showClockScreenSaverModule(): Boolean {
        return getBoolPref(PREF_MODULE_CLOCK_SAVER, false)
    }

    fun setClockScreenSaverModule(value: Boolean) {
        this.setBoolPref(PREF_MODULE_CLOCK_SAVER, value)
    }

    fun showPhotoScreenSaver(): Boolean {
        return getBoolPref(PREF_MODULE_PHOTO_SAVER, false)
    }

    fun setPhotoScreenSaver(value: Boolean) {
        this.setBoolPref(PREF_MODULE_PHOTO_SAVER, value)
    }

    var showWeatherModule: Boolean
        get() = getBoolPref(PREF_MODULE_WEATHER, false)
        set(value) = this.setBoolPref(PREF_MODULE_WEATHER, value)


    fun hasCameraCapture(): Boolean {
        return getBoolPref(PREF_CAMERA_CAPTURE, false)
    }

    fun setHasCameraCapture(value: Boolean) {
        setBoolPref(PREF_CAMERA_CAPTURE, value)
    }

    fun captureCameraImage() : Boolean {
        return cameraEnabled && hasCameraCapture()
    }

    fun hasCameraDetections() : Boolean {
        return cameraEnabled && (cameraMotionEnabled || cameraQRCodeEnabled || cameraFaceEnabled || httpMJPEGEnabled)
    }

    fun hasScreenSaver() : Boolean {
        return (showPhotoScreenSaver() || showClockScreenSaverModule())
    }

    fun isAlarmTriggeredMode(): Boolean {
        return alarmState == AlarmUtils.MODE_TRIGGERED
                || alarmState == AlarmUtils.MODE_HOME_TRIGGERED_PENDING
                || alarmState == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmState == AlarmUtils.MODE_TRIGGERED_PENDING
    }

    fun isAlarmPendingMode(): Boolean {
        return (alarmState == AlarmUtils.MODE_ARM_AWAY_PENDING
                || alarmState == AlarmUtils.MODE_ARM_HOME_PENDING
                || alarmState == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmState == AlarmUtils.MODE_HOME_TRIGGERED_PENDING)
    }

    fun isAlarmDisableMode(): Boolean {
        return (alarmState == AlarmUtils.MODE_ARM_HOME
                || alarmState == AlarmUtils.MODE_ARM_AWAY
                || alarmState == AlarmUtils.MODE_HOME_TRIGGERED_PENDING
                || alarmState == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmState == AlarmUtils.MODE_TRIGGERED_PENDING)
    }

    private fun getStringPref(key: String, defaultValue: String?): String {
        if(defaultValue == null) {
            return ""
        }
        return sharedPreferences.getString(key, defaultValue)
    }

    private fun setStringPref(key: String, defaultValue: String?) {
        sharedPreferences.edit().putString(key, defaultValue).apply()
    }

    private fun getBoolPref(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    private fun setBoolPref(key: String, defaultValue: Boolean) {
        sharedPreferences.edit().putBoolean(key, defaultValue).apply()
    }

    private fun getPrefInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getString(key, defaultValue.toString()).toInt()
    }

    private fun setPrefInt(key: String, defaultValue: Int) {
        sharedPreferences.edit().putString(key, defaultValue.toString()).apply()
    }

    private fun getPrefLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getString(key, defaultValue.toString()).toLong()
    }

    private fun setPrefLong(key: String, defaultValue: Long) {
        sharedPreferences.edit().putString(key, defaultValue.toString()).apply()
    }
    
    fun reset() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        const val PREF_FINGERPRINT = "pref_fingerprint"
        const val PREF_VOICE_INIT = "pref_voice_initialized"
        const val PREF_PENDING_TIME = "pref_pending_time"
        const val PREF_HOME_PENDING_TIME = "pref_home_pending_time"
        const val PREF_AWAY_PENDING_TIME = "pref_away_pending_time"
        const val PREF_DELAY_TIME = "pref_delay_time"
        const val PREF_HOME_DELAY_TIME = "pref_home_delay_time"
        const val PREF_AWAY_DELAY_TIME = "pref_away_delay_time"
        const val PREF_ALARM_STATE = "pref_alarm_state"
        const val PREF_ALARM_CODE = "pref_alarm_code"
        const val PREF_MODULE_CLOCK_SAVER = "pref_module_saver_clock"
        const val PREF_MODULE_PHOTO_SAVER = "pref_module_saver_photo"
        const val PREF_IMAGE_SOURCE = "pref_image_source"
        const val PREF_IMAGE_FIT_SIZE = "pref_image_fit"
        const val PREF_IMAGE_ROTATION = "pref_image_rotation"
        const val PREF_IMAGE_CLIENT_ID = "pref_image_client_id"
        const val PREF_INACTIVITY_TIME = "pref_inactivity_time"
        const val PREF_FULL_SCXREEN = "pref_full_screen"
        const val PREF_SYSTEM_SOUNDS = "pref_system_sounds"
        const val PREF_MODULE_TSS = "pref_module_tss"
        const val PREF_SYSTEM_NOTIFICATIONS = "pref_system_notifications"

        const val PREF_DISABLE_DIALOG_TIME = "pref_disable_dialog_time" // this isn't configurable
        const val PREF_CAMERA_CAPTURE = "pref_module_camera"
        const val PREF_CAMERA_ROTATE = "pref_camera_rotate"
        const val PREF_MODULE_WEATHER = "pref_module_weather"
        const val PREF_MODULE_WEB = "pref_module_web"
        const val PREF_WEB_URL = "pref_web_url"
        const val PREF_FIRST_TIME = "pref_first_time"
        const val PREF_WEATHER_WEATHER = "pref_weather_module"
        const val PREF_PLATFORM_BAR = "pref_platform_bar"
        const val PREF_TELEGRAM_MODULE = "pref_telegram_module"
        const val PREF_TELEGRAM_CHAT_ID = "pref_telegram_chat_id"
        const val PREF_TELEGRAM_TOKEN = "pref_telegram_token"
        const val PREF_DAY_NIGHT_MODE = "pref_day_night_mode"
        const val PREF_MODE_DAY_NIGHT_END = "mode_day_night_end"
        const val PREF_MODE_DAY_NIGHT_START = "mode_day_night_start"
        const val DISPLAY_MODE_DAY_NIGHT = "mode_day_night"
        const val DISPLAY_MODE_DAY_NIGHT_CHANGED = "mode_day_night_changed"
        const val SUN_ABOVE_HORIZON = "above_horizon"
        const val SUN_BELOW_HORIZON = "below_horizon"
        const val DAY_NIGHT_START_VALUE_DEFAULT = "19:00"
        const val DAY_NIGHT_END_VALUE_DEFAULT = "6:00"
        const val PREF_SENSOR_ENABLED = "pref_device_sensors_enabled"
        const val PREF_PLATFORM_CHANGED = "pref_platform_changed"
        const val PREF_COMMAND_TOPIC = "pref_command_topic"
        const val PREF_STATE_TOPIC = "pref_alarm_topic"
        const val PREF_FACE_WAKE_WORD = "pref_face_wakeword"
        const val PREF_SHOW_INTENT_LIST  = "pref_show_intent_list"

    }
}
