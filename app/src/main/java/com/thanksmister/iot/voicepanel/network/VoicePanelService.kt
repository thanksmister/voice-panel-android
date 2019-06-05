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

package com.thanksmister.iot.voicepanel.network


import ai.snips.hermes.SessionEndedMessage
import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.ByteBufferList
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.modules.*
import com.thanksmister.iot.voicepanel.modules.SnipsOptions.Companion.SUBSCRIBE_TOPIC_HERMES
import com.thanksmister.iot.voicepanel.persistence.*
import com.thanksmister.iot.voicepanel.ui.adapters.MessageAdapter
import com.thanksmister.iot.voicepanel.utils.*
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_ALERT
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_AUDIO
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_CAPTURE
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_NOTIFICATION
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_SENSOR
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_SENSOR_FACE
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_SENSOR_MOTION
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_SENSOR_QR_CODE
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_SPEAK
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_STATE
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_SUN
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_WAKE
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.COMMAND_WEATHER
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.VALUE

import dagger.android.AndroidInjection
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_logs.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.collections.ArrayList

class VoicePanelService : LifecycleService(), MQTTModule.MQTTListener,
        SnipsModule.SnipsListener, TextToSpeechModule.TextToSpeechListener {

    @Inject
    lateinit var configuration: Configuration
    @Inject
    lateinit var cameraReader: CameraReader
    @Inject
    lateinit var sensorReader: SensorReader
    @Inject
    lateinit var mqttOptions: MQTTOptions
    @Inject
    lateinit var snipsOptions: SnipsOptions
    @Inject
    lateinit var messageDataSource: MessageDao
    @Inject
    lateinit var commandDataSource: IntentDao
    @Inject
    lateinit var weatherDao: WeatherDao
    @Inject
    lateinit var sunDao: SunDao
    @Inject
    lateinit var initDao: IntentDao
    @Inject
    lateinit var notifications: NotificationUtils

    private val disposable = CompositeDisposable()
    private val mJpegSockets = ArrayList<AsyncHttpServerResponse>()
    private var partialWakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    @Suppress("DEPRECATION")
    private var keyguardLock: KeyguardManager.KeyguardLock? = null
    private var audioPlayer: MediaPlayer? = null
    private var audioPlayerBusy: Boolean = false
    private var httpServer: AsyncHttpServer? = null
    private val mBinder = VoicePanelServiceBinder()
    private val motionClearHandler = Handler()
    private val faceClearHandler = Handler()
    private var textToSpeechModule: TextToSpeechModule? = null

   // private var textToSpeechModule: TextToSpeechModule? = null
    private var mqttModule: MQTTModule? = null
    private var snipsModule: SnipsModule? = null
    private var connectionLiveData: ConnectionLiveData? = null
    private var hasNetwork = AtomicBoolean(true)
    private var motionDetected: Boolean = false
    private var faceDetected: Boolean = false
    private val reconnectHandler = Handler()
    private var appLaunchUrl: String? = null
    private var localBroadCastManager: LocalBroadcastManager? = null
    private var syncMap = HashMap<String, Boolean>() // init sync map
    private var mediaPlayer: MediaPlayer? = null
    private var lastSessionId: String? = null
    private var mqttAlertMessageShown = false
    private var mqttConnected = false

    inner class VoicePanelServiceBinder : Binder() {
        val service: VoicePanelService
            get() = this@VoicePanelService
    }

    override fun onCreate() {
        super.onCreate()

        Timber.d("onCreate")

        AndroidInjection.inject(this)

        // prepare the lock types we may use
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        //noinspection deprecation
        partialWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "voicePanel:partialWakeLock")

        // wifi lock
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "voicePanel:wifiLock")

        // Some Amazon devices are not seeing this permission so we are trying to check
        val permission = "android.permission.DISABLE_KEYGUARD"
        val checkSelfPermission = ContextCompat.checkSelfPermission(this@VoicePanelService, permission)
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardLock = keyguardManager.newKeyguardLock("ALARM_KEYBOARD_LOCK_TAG")
            keyguardLock!!.disableKeyguard()
        }

        this.appLaunchUrl = configuration.webUrl

        startForeground()
        initializeCommandList()
        configureMqtt()
        configureVoice()
        configureTextToSpeech()
        configurePowerOptions()
        startHttp()
        configureCamera()
        configureAudioPlayer()
        startSensors()

        val filter = IntentFilter()
        filter.addAction(BROADCAST_EVENT_URL_CHANGE)
        filter.addAction(BROADCAST_EVENT_SCREEN_TOUCH)
        filter.addAction(BROADCAST_EVENT_ALARM_MODE)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        localBroadCastManager = LocalBroadcastManager.getInstance(this)
        localBroadCastManager?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        if (!disposable.isDisposed) {
            disposable.clear()
        }
        mqttModule?.let {
            it.pause()
            mqttModule = null
        }

        localBroadCastManager?.unregisterReceiver(mBroadcastReceiver)
        cameraReader.stopCamera()
        sensorReader.stopReadings()
        if(snipsModule != null) {
            snipsModule!!.stop()
        }
        stopHttp()
        stopPowerOptions()
        reconnectHandler.removeCallbacks(restartMqttRunnable)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return mBinder
    }

    /**
     * Keep a map of all syncing calls to update sync status and
     * broadcast when no more syncs running
     */
    fun updateSyncMap(key: String, value: Boolean) {
        Timber.d("updateSyncMap: $key value: $value")
        syncMap[key] = value
        if (!isSyncing()) {
            resetSyncing()
            sendLoadingComplete()
        } else {
            sendLoadingStart()
        }
    }

    /**
     * Prints the sync map for debugging
     */
    private fun printSyncMap() {
        for (o in syncMap.entries) {
            val pair = o as Map.Entry<*, *>
            Timber.d("Sync Map>>>>>> " + pair.key + " = " + pair.value)
        }
    }

    /**
     * Checks if any active syncs are going one
     */
    private fun isSyncing(): Boolean {
        printSyncMap()
        Timber.d("isSyncing: " + syncMap.containsValue(true))
        return syncMap.containsValue(true)
    }

    /**
     * Resets the syncing map
     */
    private fun resetSyncing() {
        syncMap = HashMap()
    }

    private val isScreenOn: Boolean
        get() {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isInteractive || powerManager.isScreenOn
        }

    private val screenBrightness: Int
        get() {
            Timber.d("getScreenBrightness")
            var brightness = 0
            try {
                brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return brightness
        }

    private val state: JSONObject
        get() {
            val state = JSONObject()
            try {
                state.put(MqttUtils.STATE_CURRENT_URL, appLaunchUrl)
                state.put(MqttUtils.STATE_SCREEN_ON, isScreenOn)
                state.put(MqttUtils.STATE_BRIGHTNESS, screenBrightness)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return state
        }

    private fun startForeground() {
        Timber.d("startForeground")
        val notification = notifications.createOngoingNotification(getString(R.string.app_name), getString(R.string.service_notification_message))
        startForeground(ONGOING_NOTIFICATION_ID, notification)
        // listen for network connectivity changes
        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData?.observe(this, Observer { connected ->
            if(connected!!) {
                handleNetworkConnect()
            } else {
                handleNetworkDisconnect()
            }
        })
    }

    private fun handleNetworkConnect() {
        Timber.w("handleNetworkConnect")
        mqttModule.takeIf { !hasNetwork.get() }?.restart()
        hasNetwork.set(true)
    }

    private fun handleNetworkDisconnect() {
        Timber.w("handleNetworkDisconnect")
        mqttModule.takeIf { hasNetwork.get() }?.pause()
        hasNetwork.set(false)
    }

    private fun hasNetwork(): Boolean {
        return hasNetwork.get()
    }

    private fun configurePowerOptions() {
        Timber.d("configurePowerOptions")
        partialWakeLock?.let {
            if(!it.isHeld) {
                it.acquire(3000)
            }
        }
        wifiLock?.let {
            if(!it.isHeld) {
                it.acquire()
            }
        }
        try {
            keyguardLock?.disableKeyguard()
        } catch (ex: Exception) {
            Timber.i("Disabling keyguard didn't work")
            ex.printStackTrace()
        }
    }

    private fun stopPowerOptions() {
        Timber.i("Releasing Screen/WiFi Locks")
        partialWakeLock?.let {
            if(it.isHeld) {
                it.release()
            }
        }
        wifiLock?.let {
            if(it.isHeld) {
                it.release()
            }
        }
        try {
            keyguardLock?.reenableKeyguard()
        } catch (ex: Exception) {
            Timber.i("Enabling keyguard didn't work")
            ex.printStackTrace()
        }
    }

    private fun startSensors() {
        if (configuration.sensorsEnabled && mqttOptions.isValid) {
            sensorReader.startReadings(configuration.mqttSensorFrequency, sensorCallback)
        }
    }

    private fun initializeCommandList() {
        configuration.initializedVoice = false
        disposable.add(Completable.fromAction {
            //initDao.deleteAllItems()
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    // TODO we need to first make sure we have audio record permission
    private fun configureVoice() {
        Timber.d("configureVoice")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this@VoicePanelService, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        if (snipsModule == null && mqttOptions.isValid) {
            updateSyncMap(INIT_VOICE, true)
            snipsModule = SnipsModule(this@VoicePanelService.applicationContext, snipsOptions,this@VoicePanelService)
            lifecycle.addObserver(snipsModule!!)
        }
    }

    override fun onSnipsPlatformReady() {
        Timber.d("onSnipsPlatformReady")
        updateSyncMap(INIT_VOICE, false)
    }

    override fun onSnipsPlatformError(error: String) {
        Timber.d("Error " + error)
        updateSyncMap(INIT_VOICE, false)
    }

    override fun onSnipsHotwordDetectedListener() {
        Timber.d("a hotword was detected!")
        switchScreenOn(SCREEN_WAKE_TIME)
        val intent = Intent(BROADCAST_ACTION_LISTENING_START)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun onSnipsLowProbability() {
        val intent = Intent(BROADCAST_ACTION_LISTENING_END)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    // TODO let's only save this intent on success
    // TODO check the probability is high or ask to repeat message
    override fun onSnipsIntentDetectedListener(intentJson: String) {
        Timber.d("onSnipsIntentDetectedListener")
        Timber.d("intent detected!")
        Timber.d("intent json: $intentJson")
        val gson = GsonBuilder().disableHtmlEscaping().serializeNulls().create()
        val intentMessage = gson.fromJson<IntentMessage>(intentJson, IntentMessage::class.java)
        intentMessage.createdAt = DateUtils.generateCreatedAtDate()
        intentMessage.response = IntentResponse()
        intentMessage.siteId = configuration.mqttBaseTopic
        publishMessage(snipsOptions.getCommandTopic() + intentMessage.intent?.intentName, intentJson)
        lastSessionId = intentMessage.sessionId
        insertHermes(intentMessage)
    }

    override fun onSnipsListeningStateChangedListener(isListening: Boolean) {
        Timber.d("listening: " + isListening)
        if(isListening && configuration.hasHotwordResponse) {
            speakResponse(configuration.hotwordResponse)
        }
    }

    override fun onSessionEndedListener(sessionEndedMessage: SessionEndedMessage) {
        val intent = Intent(BROADCAST_ACTION_LISTENING_END)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun onSnipsWatchListener(s: String) {
        Timber.d("dialogue watch output: $s")
    }

    private fun configureMqtt() {
        Timber.d("configureMqtt")
        if (mqttModule == null && mqttOptions.isValid) {
            updateSyncMap(INIT_MQTT, true)
            mqttModule = MQTTModule(this@VoicePanelService.applicationContext, mqttOptions,this@VoicePanelService)
            lifecycle.addObserver(mqttModule!!)
        }
    }

    override fun onMQTTConnect() {
        Timber.w("onMQTTConnect")
        publishMessage(COMMAND_STATE, state.toString())
        clearFaceDetected()
        clearMotionDetected()
        updateSyncMap(INIT_MQTT, false)
    }

    override fun onMQTTDisconnect() {
        Timber.e("onMQTTDisconnect")
        if(hasNetwork()) {
            if(!mqttAlertMessageShown && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                mqttAlertMessageShown = true
                sendAlertMessage(getString(R.string.error_mqtt_connection))
            }
        }
        updateSyncMap(INIT_MQTT, false)
    }

    override fun onMQTTException(message: String) {
        Timber.e("onMQTTException: $message")
        if(hasNetwork()) {
            if(!mqttAlertMessageShown && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                mqttAlertMessageShown = true
                sendAlertMessage(getString(R.string.error_mqtt_exception))
            }
        }
        updateSyncMap(INIT_MQTT, false)
    }

    private val restartMqttRunnable = Runnable {
        mqttModule?.restart()
    }

    // TODO don't pass alarm mqtt as command
    override fun onMQTTMessage(id: String, topic: String, payload: String) {
        /*Timber.i("onMQTTMessage id: $id")
        Timber.i("onMQTTMessage topic: $topic")
        Timber.i("onMQTTMessage payload: $payload")*/
        if(SUBSCRIBE_TOPIC_HERMES == topic) {
            processHermes(payload)
        } else if(AlarmUtils.hasSupportedStates(payload)) {
            if (configuration.alarmEnabled && AlarmUtils.ALARM_STATE_TOPIC == topic && AlarmUtils.hasSupportedStates(payload)) {
                when (payload) {
                    AlarmUtils.STATE_DISARM -> {
                        switchScreenOn(SCREEN_WAKE_TIME)
                        if (configuration.hasSystemAlerts()) {
                            notifications.clearNotification()
                        }
                        stopContinuousAlarm()
                    }
                    AlarmUtils.STATE_ARM_AWAY,
                    AlarmUtils.STATE_ARM_HOME -> {
                        switchScreenOn(SCREEN_WAKE_TIME)
                        stopContinuousAlarm()
                    }
                    AlarmUtils.STATE_TRIGGERED -> {
                        switchScreenOn(TRIGGERED_AWAKE_TIME) // 3 hours
                        if (configuration.alarmState == AlarmUtils.MODE_TRIGGERED && configuration.hasSystemAlerts()) {
                            notifications.createAlarmNotification(getString(R.string.text_notification_trigger_title), getString(R.string.text_notification_trigger_description))
                            playContinuousAlarm()
                        }
                    }
                    AlarmUtils.STATE_PENDING -> {
                        switchScreenOn(SCREEN_WAKE_TIME)
                        if ((configuration.alarmState == AlarmUtils.MODE_ARM_HOME || configuration.alarmState == AlarmUtils.MODE_ARM_AWAY) && configuration.hasSystemAlerts()) {
                            notifications.createAlarmNotification(getString(R.string.text_notification_entry_title), getString(R.string.text_notification_entry_description))
                            playContinuousAlarm()
                        }
                    }
                }
                configuration.alarmState = payload
                insertMessage(id, topic, payload, AlarmUtils.ALARM_TYPE)
            }
        } else {
            processCommand(id, topic, payload)
        }
    }

    private fun publishAlarm(command: String) {
        mqttModule?.let {
            Timber.d("publishAlarm $command")
            it.publishAlarm(command)
            if(command == AlarmUtils.COMMAND_DISARM) {
                captureImageTask()
            }
        }
    }

    private fun publishMessage(command: String, data: JSONObject) {
        publishMessage(command, data.toString())
    }

    private fun publishMessage(command: String, message: String) {
        mqttModule?.publishState(command, message)
    }

    private fun configureCamera() {
        Timber.d("configureCamera ${configuration.cameraEnabled}")
        if (configuration.cameraEnabled) {
            updateSyncMap(INIT_CAMERA, true)
            cameraReader.startCamera(cameraDetectorCallback, configuration)
            updateSyncMap(INIT_CAMERA, false)
        }
    }

    private fun configureTextToSpeech() {
        Timber.d("configureTextToSpeach")
        if (textToSpeechModule == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mqttOptions.isValid) {
            updateSyncMap(INIT_SPEECH, true)
            textToSpeechModule = TextToSpeechModule(this, this)
            lifecycle.addObserver(textToSpeechModule!!)
        }
    }

    override fun onTextToSpeechInitialized() {
        updateSyncMap(INIT_SPEECH, false)
    }

    override fun onTextToSpeechError() {
        updateSyncMap(INIT_SPEECH, false)
    }

    private fun configureAudioPlayer() {
        audioPlayer = MediaPlayer()
        audioPlayer?.setOnPreparedListener { audioPlayer ->
            Timber.d("audioPlayer: File buffered, playing it now")
            audioPlayerBusy = false
            audioPlayer.start()
        }
        audioPlayer?.setOnCompletionListener { audioPlayer ->
            Timber.d("audioPlayer: Cleanup")
            if (audioPlayer.isPlaying) {  // should never happen, just in case
                audioPlayer.stop()
            }
            audioPlayer.reset()
            audioPlayerBusy = false
        }
        audioPlayer?.setOnErrorListener { audioPlayer, i, i1 ->
            Timber.d("audioPlayer: Error playing file")
            audioPlayerBusy = false
            false
        }
    }

    private fun startHttp() {
        if (httpServer == null && configuration.httpMJPEGEnabled) {
            updateSyncMap(INIT_HTTP_SERVER, true)
            Timber.d("startHttp")
            httpServer = AsyncHttpServer()
            if (configuration.httpMJPEGEnabled) {
                startMJPEG()
                httpServer?.addAction("GET", "/camera/stream") { _, response ->
                    Timber.i("GET Arrived (/camera/stream)")
                    startMJPEG(response)
                }
                Timber.i("Enabled MJPEG Endpoint")
            }
            httpServer?.addAction("*", "*") { request, response ->
                Timber.i("Unhandled Request Arrived")
                response.code(404)
                response.send("")
            }
            httpServer?.listen(AsyncServer.getDefault(), configuration.httpPort)
            Timber.i("Started HTTP server on " + configuration.httpPort)
            updateSyncMap(INIT_HTTP_SERVER, false)
        }
    }

    private fun stopHttp() {
        Timber.d("stopHttp")
        httpServer?.let {
            stopMJPEG()
            it.stop()
            httpServer = null
        }
    }

    private fun startMJPEG() {
        Timber.d("startMJPEG")
        cameraReader.getJpeg().observe(this, Observer { jpeg ->
            if (mJpegSockets.size > 0 && jpeg != null) {
                Timber.d("mJpegSockets")
                var i = 0
                while (i < mJpegSockets.size) {
                    val s = mJpegSockets[i]
                    val bb = ByteBufferList()
                    if (s.isOpen) {
                        bb.recycle()
                        bb.add(ByteBuffer.wrap("--jpgboundary\r\nContent-Type: image/jpeg\r\n".toByteArray()))
                        bb.add(ByteBuffer.wrap(("Content-Length: " + jpeg.size + "\r\n\r\n").toByteArray()))
                        bb.add(ByteBuffer.wrap(jpeg))
                        bb.add(ByteBuffer.wrap("\r\n".toByteArray()))
                        s.write(bb)
                    } else {
                        mJpegSockets.removeAt(i)
                        i--
                        Timber.i("MJPEG Session Count is " + mJpegSockets.size)
                    }
                    i++
                }
            }
        })
    }

    private fun stopMJPEG() {
        Timber.d("stopMJPEG Called")
        mJpegSockets.clear()
    }

    private fun startMJPEG(response: AsyncHttpServerResponse) {
        Timber.d("startmJpeg Called")
        if (mJpegSockets.size < configuration.httpMJPEGMaxStreams) {
            Timber.i("Starting new MJPEG stream")
            response.headers.add("Cache-Control", "no-cache")
            response.headers.add("Connection", "close")
            response.headers.add("Pragma", "no-cache")
            response.setContentType("multipart/x-mixed-replace; boundary=--jpgboundary")
            response.code(200)
            response.writeHead()
            mJpegSockets.add(response)
        } else {
            Timber.i("MJPEG stream limit was reached, not starting")
            response.send("Max streams exceeded")
            response.end()
        }
        Timber.i("MJPEG Session Count is " + mJpegSockets.size)
    }

    private fun processCommand(id: String, topic: String, commandJson: JSONObject) {
       /* Timber.d("processCommand topic $topic")
        Timber.d("processCommand commandJson ${commandJson.toString()}")*/
        var payload: String = ""
        try {
            if (commandJson.has(COMMAND_WAKE)) {
                payload = commandJson.getString(COMMAND_WAKE)
                insertMessage(id, topic, payload, COMMAND_WAKE)
                switchScreenOn(SCREEN_WAKE_TIME)
            }
            if (commandJson.has(COMMAND_AUDIO)) {
                payload = commandJson.getString(COMMAND_AUDIO)
                insertMessage(id, topic, payload, COMMAND_AUDIO)
                playAudio(payload)
            }
            if (commandJson.has(COMMAND_SPEAK)) {
                payload = commandJson.getString(COMMAND_SPEAK)
                insertMessage(id, topic, payload, COMMAND_SPEAK)
                speakMessage(payload)
            }
            if (commandJson.has(COMMAND_AUDIO)) {
                payload = commandJson.getString(COMMAND_AUDIO)
                insertMessage(id, topic, payload, COMMAND_AUDIO)
                playAudio(commandJson.getString(COMMAND_AUDIO))
            }
            if (commandJson.has(COMMAND_NOTIFICATION)) {
                payload = commandJson.getString(COMMAND_SPEAK)
                insertMessage(id, topic, payload, COMMAND_NOTIFICATION)
                val title = getString(R.string.notification_title)
                notifications.createAlarmNotification(title, payload)
            }
            if (commandJson.has(COMMAND_ALERT)) {
                payload = commandJson.getString(COMMAND_ALERT)
                insertMessage(id, topic, payload, COMMAND_ALERT)
                sendAlertMessage(payload)
            }
            if (commandJson.has(COMMAND_CAPTURE)) {
                payload = commandJson.getString(COMMAND_CAPTURE)
                insertMessage(id, topic, payload, COMMAND_CAPTURE)
                captureImageTask()
            }
            if (commandJson.has(COMMAND_WEATHER) && configuration.showWeatherModule) {
                payload = commandJson.getString(COMMAND_WEATHER)
                insertWeather(payload)
            }
            if (commandJson.has(COMMAND_SUN)) {
                payload = commandJson.getString(COMMAND_SUN)
                insertSun(payload)
            }
        } catch (ex: JSONException) {
            Timber.e("Invalid JSON passed as a command: " + commandJson.toString())
        }
    }

    private fun processCommand(id: String, topic: String, command: String) {
        //Timber.d("processCommand")
        return try {
            processCommand(id, topic, JSONObject(command))
        } catch (ex: JSONException) {
            Timber.e("Invalid JSON passed as a command: $command")
        }
    }

    // TODO update the saved intent and check that the session Id matches
    private fun processHermes(payload:String) {
        val gson = GsonBuilder().disableHtmlEscaping().serializeNulls().create()
        val response = gson.fromJson<IntentResponse>(payload, IntentResponse::class.java)
        snipsModule?.let {
            if(!TextUtils.isEmpty(response.sessionId) && lastSessionId == response.sessionId) {
                insertIntentResponse(response.sessionId!!, response)
                if(!TextUtils.isEmpty(response.text)) {
                    it.endSession(response.sessionId!!, response.text!!)
                }
            }
        }
    }

    private fun insertIntentResponse(sessionId: String, response: IntentResponse) {
        Timber.d("Session Id: $sessionId")
        disposable.add(Completable.fromAction {
            commandDataSource.getItemById(sessionId)
                    .subscribe({
                        it.response = response
                        commandDataSource.insertItem(it)
                    }, { error -> Timber.e("Unable to insert response: " + error)})
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Intent Response error" + error.message) }))
    }

    private fun insertMessage(messageId: String, topic: String, payload: String, type: String) {
        Timber.d("insertMessage: " + topic)
        Timber.d("insertMessage: " + payload)
        disposable.add(Completable.fromAction {
            val createdAt = DateUtils.generateCreatedAtDate()
            val message = MessageMqtt()
            message.type = type
            message.topic = topic
            message.payload = payload
            message.messageId = messageId
            message.createdAt = createdAt
            messageDataSource.insertMessage(message)
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    // TODO add the intent response
    private fun insertHermes(intentMessage: IntentMessage) {
        Timber.d("insertHermes: $intentMessage")
        disposable.add(Completable.fromAction {
            commandDataSource.insertItem(intentMessage)
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    private fun insertSun(payload: String) {
        Timber.d("insertSun $payload")
        disposable.add(Completable.fromAction {
            val sun = Sun()
            sun.sun = payload
            sun.createdAt = DateUtils.generateCreatedAtDate()
            sunDao.updateItem(sun)
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    private fun insertWeather(payload: String) {
        Timber.d("insertWeather")
        val gson = GsonBuilder().serializeNulls().create()
        val weather = gson.fromJson<Weather>(payload, Weather::class.java)
        disposable.add(Completable.fromAction {
            weather.createdAt = DateUtils.generateCreatedAtDate()
            weatherDao.updateItem(weather)
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    private fun playAudio(audioUrl: String) {
        Timber.d("audioPlayer")
        if (audioPlayerBusy) {
            Timber.d("audioPlayer: Cancelling all previous buffers because new audio was requested")
            audioPlayer?.reset()
        } else if (audioPlayer!= null && audioPlayer!!.isPlaying) {
            Timber.d("audioPlayer: Stopping all media playback because new audio was requested")
            audioPlayer?.stop()
            audioPlayer?.reset()
        }

        audioPlayerBusy = true
        try {
            audioPlayer?.setDataSource(audioUrl)
        } catch (e: IOException) {
            Timber.e("audioPlayer: An error occurred while preparing audio (" + e.message + ")")
            audioPlayerBusy = false
            audioPlayer?.reset()
            return
        }

        Timber.d("audioPlayer: Buffering $audioUrl")
        audioPlayer?.prepareAsync()
    }

    private fun speakMessage(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("speakMessage: $message")
            snipsModule?.startNotification(message)
        }
    }

    private fun speakResponse(message: String) {
        Timber.d("speakResponse: $message")
        textToSpeechModule?.speakText(message)
    }

    @SuppressLint("WakelockTimeout")
    private fun switchScreenOn(awakeTime: Long) {
        Timber.d("switchScreenOn")
        partialWakeLock?.let {
            if (!it.isHeld) {
                Timber.d("partialWakeLock")
                it.acquire(SCREEN_WAKE_TIME)
            } else if (it.isHeld) {
                Timber.d("new partialWakeLock")
                it.release()
                it.acquire(SCREEN_WAKE_TIME)
            }
        }
        sendWakeScreen()
    }

    private fun publishMotionDetected() {
        val delay = (configuration.motionResetTime * 1000).toLong()
        if (!motionDetected) {
            //Timber.d("publishMotionDetected")
            val data = JSONObject()
            try {
                data.put(MqttUtils.VALUE, true)
            } catch (ex: JSONException) {
                ex.printStackTrace()
            }
            motionDetected = true
            publishMessage(COMMAND_SENSOR_MOTION, data)
            motionClearHandler.postDelayed({ clearMotionDetected() }, delay)
        }
    }

    private fun publishFaceDetected() {
        if (!faceDetected) {
            faceDetected = true
            if (configuration.cameraFaceWake) {
                configurePowerOptions()
                switchScreenOn(SCREEN_WAKE_TIME)
            }
            if(configuration.faceWakeWord && snipsModule != null) {
                snipsModule!!.startManualListening()
            }
            val data = JSONObject()
            try {
                data.put(MqttUtils.VALUE, true)
            } catch (ex: JSONException) {
                ex.printStackTrace()
            }
            publishMessage(mqttOptions.getBaseTopic() + COMMAND_SENSOR_FACE, data)
            faceClearHandler.postDelayed({ clearFaceDetected() }, FACE_DETECTION_INTERVAL)
        }
    }

    private fun clearMotionDetected() {
        //Timber.d("Clearing motion detected status")
        if(motionDetected) {
            motionDetected = false
            val data = JSONObject()
            try {
                data.put(VALUE, false)
            } catch (ex: JSONException) {
                ex.printStackTrace()
            }
            publishMessage(mqttOptions.getBaseTopic() + COMMAND_SENSOR_MOTION, data)
        }
    }

    private fun clearFaceDetected() {
        if(faceDetected) {
            //Timber.d("Clearing face detected status")
            val data = JSONObject()
            try {
                data.put(VALUE, false)
            } catch (ex: JSONException) {
                ex.printStackTrace()
            }
            faceDetected = false
            publishMessage(mqttOptions.getBaseTopic() + COMMAND_SENSOR_FACE, data)
        }
    }

    private fun publishQrCode(data: String) {
        Timber.d("publishQrCode")
        val jdata = JSONObject()
        try {
            jdata.put(VALUE, data)
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
        publishMessage(mqttOptions.getBaseTopic() + COMMAND_SENSOR_QR_CODE, jdata)
    }

    private fun sendAlertMessage(message: String) {
        Timber.d("sendAlertMessage")
        val intent = Intent(BROADCAST_ALERT_MESSAGE)
        intent.putExtra(BROADCAST_ALERT_MESSAGE, message)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    private fun sendWakeScreen() {
        Timber.d("sendWakeScreen")
        val intent = Intent(BROADCAST_SCREEN_WAKE)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    private fun sendLoadingStart() {
        Timber.d("sendLoadingStart")
        val intent = Intent(BROADCAST_ACTION_LOADING_START)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    private fun sendLoadingComplete() {
        Timber.d("sendLoadingComplete")
        val intent = Intent(BROADCAST_ACTION_LOADING_COMPLETE)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
        if(!configuration.initializedVoice) {
            configuration.initializedVoice = true;
            val intentMessage = IntentMessage()
            intentMessage.slots = ArrayList<Slot>()
            intentMessage.intent = com.thanksmister.iot.voicepanel.persistence.Intent()
            intentMessage.response = IntentResponse()
            intentMessage.intent!!.intentName =  ComponentUtils.COMPONENT_SNIPS_INIT
            intentMessage.input = getString(R.string.text_snips_welcome)
            intentMessage.createdAt = DateUtils.generateCreatedAtDate()
            insertHermes(intentMessage)
        }
    }

    private fun sendToastMessage(message: String) {
        Timber.d("sendToastMessage")
        val intent = Intent(BROADCAST_TOAST_MESSAGE)
        intent.putExtra(BROADCAST_TOAST_MESSAGE, message)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    private fun stopContinuousAlarm() {
        mediaPlayer?.stop()
    }

    private fun playContinuousAlarm() {
        if(configuration.systemSounds) {
            try {
                var alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                if (alert == null) {
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    if (alert == null) {
                        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    }
                }
                mediaPlayer = MediaPlayer.create(this@VoicePanelService, alert)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e: SecurityException) {
                playContinuousBeep()
            } catch(e: FileNotFoundException) {
                playContinuousBeep()
            }
        } else {
            playContinuousBeep()
        }
    }

    private fun playContinuousBeep() {
        Timber.d("playContinuousBeep")
        mediaPlayer = MediaPlayer.create(this@VoicePanelService, R.raw.beep_loop)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    /**
     * Capture and send image if user has setup this feature.
     */
    private fun captureImageTask() {
        if(configuration.captureCameraImage()) {
            val bitmapTask = BitmapTask(object : OnCompleteListener {
                override fun onComplete(bitmap: Bitmap?) {
                    if(bitmap != null) {
                        // TODO what will we do with this captured image?
                        //sendCapturedImage(bitmap)
                    }
                }
            })
            if(cameraReader.getJpeg().value != null) {
                bitmapTask.execute(cameraReader.getJpeg().value)
            }
        }
    }

    interface OnCompleteListener {
        fun onComplete(bitmap: Bitmap?)
    }

    /**
     * Convert byte array to bitmap for disarmed image
     */
    class BitmapTask(private val onCompleteListener: OnCompleteListener) : AsyncTask<Any, Void, Bitmap>() {
        override fun doInBackground(vararg params: kotlin.Any): Bitmap? {
            if (isCancelled) {
                return null
            }
            try {
                val byteArray = params[0] as ByteArray
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size);
                return bitmap
            } catch (e: Exception) {
                return null
            }
        }
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if (isCancelled) {
                return
            }
            onCompleteListener.onComplete(result)
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BROADCAST_EVENT_URL_CHANGE == intent.action) {
                appLaunchUrl = intent.getStringExtra(BROADCAST_EVENT_URL_CHANGE)
                if (appLaunchUrl != configuration.webUrl) {
                    Timber.i("Url changed to $appLaunchUrl")
                    publishMessage(mqttOptions.getBaseTopic() + COMMAND_STATE, state.toString())
                }
            } else if (Intent.ACTION_SCREEN_OFF == intent.action ||
                    intent.action == Intent.ACTION_SCREEN_ON ||
                    intent.action == Intent.ACTION_USER_PRESENT) {
                Timber.i("Screen state changed")
                publishMessage(mqttOptions.getBaseTopic() + COMMAND_STATE, state.toString())
            } else if (BROADCAST_EVENT_SCREEN_TOUCH == intent.action) {
                Timber.i("Screen touched")
                publishMessage(mqttOptions.getBaseTopic() + COMMAND_STATE, state.toString())
            }  else if (BROADCAST_EVENT_ALARM_MODE == intent.action) {
                Timber.i("Alarm mode changed")
                val alarmMode = intent.getStringExtra(BROADCAST_EVENT_ALARM_MODE)
                publishAlarm(alarmMode)
            }
        }
    }

    private val sensorCallback = object : SensorCallback {
        override fun publishSensorData(sensorName: String, sensorData: JSONObject) {
            publishMessage(mqttOptions.getBaseTopic() + COMMAND_SENSOR + sensorName, sensorData)
        }
    }

    private val cameraDetectorCallback = object : CameraCallback {
        override fun onCameraInit() {
            updateSyncMap(INIT_CAMERA, false)
        }
        override fun onDetectorError() {
            sendToastMessage(getString(R.string.error_missing_vision_lib))
            updateSyncMap(INIT_CAMERA, false)
        }
        override fun onCameraError() {
            sendToastMessage(this@VoicePanelService.getString(R.string.toast_camera_source_error))
            updateSyncMap(INIT_CAMERA, false)
        }
        override fun onMotionDetected() {
            Timber.i("Motion detected")
            if (configuration.cameraMotionWake) {
                switchScreenOn(SCREEN_WAKE_TIME)
            }
            publishMotionDetected()
        }
        override fun onTooDark() {
            // Timber.i("Too dark for motion detection")
        }
        override fun onFaceDetected() {
            Timber.i("Face detected")
            publishFaceDetected()
        }
        override fun onQRCode(data: String) {
            Timber.i("QR Code Received: $data")
            Toast.makeText(this@VoicePanelService, getString(R.string.toast_qr_code_read), Toast.LENGTH_SHORT).show()
            publishQrCode(data)
        }
    }

    companion object {
        const val FACE_DETECTION_INTERVAL: Long = 5000L // 3 SECONDS
        const val SCREEN_WAKE_TIME: Long = 30000L // 30 SECONDS
        const val TRIGGERED_AWAKE_TIME: Long = 10800000L // 3 HOURS
        const val INIT_SPEECH = "com.thanksmister.iot.voicepanel.INIT_SPEECH"
        const val INIT_VOICE = "com.thanksmister.iot.voicepanel.INIT_VOICE"
        const val INIT_MQTT = "com.thanksmister.iot.voicepanel.INIT_MQTT"
        const val INIT_CAMERA = "com.thanksmister.iot.voicepanel.INIT_CAMERA"
        const val INIT_HTTP_SERVER = "om.thanksmister.iot.voicepanel.INIT_HTTP_SERVER"
        const val ONGOING_NOTIFICATION_ID = 19
        const val BROADCAST_EVENT_URL_CHANGE = "BROADCAST_EVENT_URL_CHANGE"
        const val BROADCAST_EVENT_SCREEN_TOUCH = "BROADCAST_EVENT_SCREEN_TOUCH"
        const val BROADCAST_ALERT_MESSAGE = "BROADCAST_ALERT_MESSAGE"
        const val BROADCAST_TOAST_MESSAGE = "BROADCAST_TOAST_MESSAGE"
        const val BROADCAST_SCREEN_WAKE = "BROADCAST_SCREEN_WAKE"
        const val BROADCAST_ACTION_LOADING_START = "BROADCAST_ACTION_LOADING_START"
        const val BROADCAST_ACTION_LOADING_COMPLETE = "BROADCAST_ACTION_LOADING_COMPLETE"
        const val BROADCAST_ACTION_LISTENING_START = "BROADCAST_ACTION_LISTENING_START"
        const val BROADCAST_ACTION_LISTENING_END = "BROADCAST_ACTION_LISTENING_END"
        const val BROADCAST_EVENT_ALARM_MODE = "BROADCAST_EVENT_ALARM_MODE"
    }
}