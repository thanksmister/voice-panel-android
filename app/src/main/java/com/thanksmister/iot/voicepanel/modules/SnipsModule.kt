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

package com.thanksmister.iot.voicepanel.modules

import ai.snips.hermes.*
import ai.snips.platform.SnipsPlatformClient
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Process
import android.text.TextUtils
import com.thanksmister.iot.voicepanel.BuildConfig
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.utils.FileUtils
import timber.log.Timber
import java.lang.ref.WeakReference

class SnipsModule (base: Context?, private var options: SnipsOptions, var listener: SnipsListener) : ContextWrapper(base), LifecycleObserver {

    @Volatile
    private var continueStreaming = true
    private var snipsClient: SnipsPlatformClient? = null
    private var recorder: AudioRecord? = null
    private var snipsClientTask: SnipsUnzipAssistant? = null
    private var manuallyListening: Boolean = false
    private var lowProbability: Boolean = false

    interface SnipsListener {
        fun onSnipsPlatformReady()
        fun onSnipsPlatformError(error: String)
        fun onSnipsHotwordDetectedListener()
        fun onSnipsIntentDetectedListener(intentMessage: IntentMessage)
        fun onSnipsListeningStateChangedListener(isListening: Boolean)
        fun onSessionEndedListener(sessionEndedMessage: SessionEndedMessage)
        fun onSnipsWatchListener(s: String)
        fun onSnipsLowProbability()
    }

    init {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun start() {
        Timber.d("start")
        Timber.d("BuildConfig.VERSION_NAME: " + BuildConfig.ASSISTANT_VERSION)
        Timber.d("unzip assistant folder: " + FileUtils.unzipAssistantDirectory(baseContext, BuildConfig.ASSISTANT_VERSION))
        if(FileUtils.unzipAssistantDirectory(baseContext, BuildConfig.ASSISTANT_VERSION)) {
            Timber.d("Unzip new asssistant into directory")
            snipsClientTask = SnipsUnzipAssistant(baseContext, object : SnipsTaskListener {
                override fun onSnipsUnzipComplete(unzipped: Boolean) {
                   if(unzipped) {
                       Timber.d("Zip file unzipped into directory")
                       initClient()
                   } else {
                       Timber.e("Problem unzipping the file.")
                       listener.onSnipsPlatformError(getString(R.string.error_unzip_assistant))
                   }
                }
            })
            if(snipsClientTask != null) {
                snipsClientTask!!.execute()
            }
        } else {
            initClient()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun pause() {
        Timber.d("stop")
        if(snipsClientTask != null) {
            snipsClientTask!!.cancel(true)
            snipsClientTask = null
        }
        continueStreaming = false
        if (snipsClient != null) {
            snipsClient!!.pause()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun resume() {
        Timber.d("stop")
        if (snipsClient != null) {
            startStreaming()
            snipsClient!!.resume()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stop() {
        Timber.d("stop")
        continueStreaming = false
        if(snipsClientTask != null) {
            snipsClientTask!!.cancel(true)
            snipsClientTask = null
        }
        if (snipsClient != null) {
            snipsClient!!.disconnect()
        }
    }

    // Start a session manually
    fun startManualListening() {
        if(snipsClient != null && !manuallyListening) {
            manuallyListening = true
            snipsClient?.startSession(null, ArrayList<String>(), false, null)
        }
    }

    fun startNotification(message: String) {
        if(snipsClient != null) {
            snipsClient!!.startNotification(message, null)
        }
    }

    fun endSession(sessionId: String, message: String) {
        if(snipsClient != null) {
            snipsClient!!.endSession(sessionId, message)
        }
    }

    private fun initClient() {
        Timber.d("initClient")
        if(snipsClient == null) {
            //val oldAssistantDir = File(Environment.getExternalStorageDirectory().toString(), "snips_android_assistant")
            val assistantDir = FileUtils.getAssistantDirectory(baseContext)
            snipsClient = SnipsPlatformClient.Builder(assistantDir)
                    .enableDialogue(options.enableDialogue) // defaults to true
                    .enableHotword(options.enableHotword) // defaults to true
                    .enableSnipsWatchHtml(options.enableSnipsWatchHtml) // defaults to false
                    .enableLogs(options.enableLogs) // defaults to false
                    .withHotwordSensitivity(options.withHotwordSensitivity) // defaults to 0.5
                    .enableStreaming(options.enableStreaming) // defaults to false
                    .enableInjection(options.enableInjection) // defaults to false
                    .build();

            snipsClient!!.onPlatformReady = fun(): Unit {
                listener.onSnipsPlatformReady()
            }
            snipsClient!!.onPlatformError = fun(snipsPlatformError: SnipsPlatformClient.SnipsPlatformError): Unit {
                if(!TextUtils.isEmpty(snipsPlatformError.message)) {
                    listener.onSnipsPlatformError(snipsPlatformError.message!!)
                }
            }
            snipsClient!!.onHotwordDetectedListener = fun(): Unit {
                Timber.d("a hotword was detected !")
                // Do your magic here :D
                // TODO play a sound or make some icon change
                manuallyListening = false
                listener.onSnipsHotwordDetectedListener()
            }
            snipsClient!!.onIntentDetectedListener = fun(intentMessage: IntentMessage): Unit {
                Timber.d("received an intent: $intentMessage")
                //Timber.d("json output: $json")
                Timber.d("The probability was ${intentMessage.intent.probability}")
                //if(intentMessage.intent.probability > .070) {
                val lowerValue = options.nluProbability
                val higherValue = 1.0f
                if(intentMessage.intent.probability in lowerValue..higherValue) {
                    lowProbability = false
                    listener.onSnipsIntentDetectedListener(intentMessage)
                } else {
                    Timber.w("The probability was too low.")
                    lowProbability = true
                    listener.onSnipsLowProbability()
                }
            }
            snipsClient!!.onListeningStateChangedListener = fun(isListening: Boolean): Unit {
                Timber.d("asr listening state: " + isListening)
                // Do you magic here :D
                listener.onSnipsListeningStateChangedListener(isListening)
            }
            snipsClient!!.onSessionStartedListener = fun(sessionStartedMessage: SessionStartedMessage): Unit {
                Timber.d("dialogue session started: $sessionStartedMessage")
                //listener.onSessionStartedListener(sessionStartedMessage)
            }
            snipsClient!!.onSessionQueuedListener = fun(sessionQueuedMessage: SessionQueuedMessage): Unit {
                Timber.d("dialogue session queued: $sessionQueuedMessage")
                //listener.onSessionQueuedListener(sessionQueuedMessage)
            }
            snipsClient!!.onSessionEndedListener = fun(sessionEndedMessage: SessionEndedMessage): Unit {
                Timber.d("dialogue session ended: $sessionEndedMessage")
                Timber.d("termination type: ${sessionEndedMessage.termination.type}")
                if(SessionTermination.Type.INTENT_NOT_RECOGNIZED == sessionEndedMessage.termination.type
                        && !manuallyListening && !lowProbability) {
                    snipsClient!!.startNotification("Sorry, I didn't understand.", null)
                } else if (SessionTermination.Type.TIMEOUT == sessionEndedMessage.termination.type
                        && !manuallyListening && !lowProbability) {
                    snipsClient!!.startNotification("Sorry, I don't know how to do that.", null)
                } else if (SessionTermination.Type.NOMINAL == sessionEndedMessage.termination.type) {
                    //snipsClient!!.startNotification("Assistant initialized and ready.", null)
                }
                manuallyListening = false
                listener.onSessionEndedListener(sessionEndedMessage)
            }
            snipsClient!!.onSnipsWatchListener = fun(s: String): Unit {
                listener.onSnipsWatchListener(s)
            }

            // We enabled steaming in the builder, so we need to provide the platform an audio stream. If you don't want
            // to manage the audio stream do no enable the option, and the snips platform will grab the mic by itself
            startStreaming()

            snipsClient!!.connect(applicationContext)
        }
    }

    private fun startStreaming() {
        continueStreaming = true
        object : Thread() {
            override fun run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                runStreaming()
            }
        }.start()
    }

    // TODO we need to check permissions granted for audio recording first before calling this
    private fun runStreaming() {
        Timber.d("starting audio streaming")
        val minBufferSizeInBytes = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING)
        Timber.d("minBufferSizeInBytes: $minBufferSizeInBytes")
        object : Thread() {
            override fun run() {
                recorder = AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL, ENCODING, minBufferSizeInBytes)
                recorder!!.startRecording()
                while (continueStreaming) {
                    val buffer = ShortArray(minBufferSizeInBytes / 2)
                    recorder!!.read(buffer, 0, buffer.size)
                    if (snipsClient != null) {
                        snipsClient!!.sendAudioBuffer(buffer)
                    }
                }
                recorder!!.stop()
                Timber.d("audio streaming stopped")
            }
        }.start()
    }

    interface SnipsTaskListener {
        fun onSnipsUnzipComplete(unzipped: Boolean)
    }

    class SnipsUnzipAssistant(context: Context, private val listener: SnipsTaskListener) : AsyncTask<Any, Void, Boolean>() {
        private val contextRef: WeakReference<Context> = WeakReference(context)
        override fun doInBackground(vararg params: Any?): Boolean {
            if (isCancelled) {
                return false
            }
            return FileUtils.doUnzipAssistantDirectory(contextRef.get()!!, BuildConfig.ASSISTANT_VERSION)
        }
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (isCancelled || result == null) {
                return
            }
            listener.onSnipsUnzipComplete(result)
        }
    }

    companion object {
        private val AUDIO_ECHO_REQUEST = 0
        private val FREQUENCY = 16000
        private val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }
}