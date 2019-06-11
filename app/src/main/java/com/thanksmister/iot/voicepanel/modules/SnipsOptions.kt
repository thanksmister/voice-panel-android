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

import android.content.Context
import android.content.SharedPreferences
import com.thanksmister.iot.voicepanel.R
import javax.inject.Inject

class SnipsOptions @Inject
constructor(private val context: Context, private val sharedPreferences: SharedPreferences) {

    fun getCommandTopic(): String {
        return COMMAND_TOPIC_HERMES
    }

    var enableHotword: Boolean
        get() = getBoolPref(R.string.key_snips_hot_word, R.string.default_snips_hot_word)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_snips_hot_word), value).apply()
        }
    
    var enableSnipsWatchHtml: Boolean
        get() = getBoolPref(R.string.key_snips_watch_html, R.string.default_snips_watch_html)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_snips_watch_html), value).apply()
        }
    
    var enableDialogue: Boolean
        get() = getBoolPref(R.string.key_snips_dialog, R.string.default_key_snips_dialog)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_snips_dialog), value).apply()
        }
    
    var enableLogs: Boolean
        get() = getBoolPref(R.string.key_snips_logs, R.string.default_snips_logs)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_snips_logs), value).apply()
        }
    
    var enableStreaming: Boolean
        get() = getBoolPref(R.string.key_snips_streaming, R.string.default_snips_streaming)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_snips_streaming), value).apply()
        }
    
    var enableInjection: Boolean
        get() = getBoolPref(R.string.key_snips_injection,
                R.string.default_snips_injection)
        set(value) {
            sharedPreferences.edit().putBoolean(context.getString(R.string.key_snips_injection), value).apply()
        }
    
    var withHotwordSensitivity: Float
        get() : Float {
            val prefValue = sharedPreferences.getString(context.getString(R.string.key_snips_hotword_sensitivity), context.getString(R.string.default_snips_hotword_sensitivity))
            return if(prefValue != null)   {
                prefValue.toFloatOrNull()?:context.getString(R.string.default_snips_face_wake_interval).toFloat()
            } else {
                context.getString(R.string.default_snips_hotword_sensitivity).toFloat()
            }
        }
        set(value) {
            sharedPreferences.edit().putString(context.getString(R.string.key_snips_hotword_sensitivity), value.toString()).apply()
        }

    var nluProbability: Float
        get() : Float {
            val prefValue = sharedPreferences.getString(context.getString(R.string.key_snips_probability), context.getString(R.string.default_snips_snips_probability))
            return if(prefValue != null)   {
                prefValue.toFloatOrNull()?:context.getString(R.string.default_snips_snips_probability).toFloat()
            } else {
                context.getString(R.string.default_snips_snips_probability).toFloat()
            }
        }set(value) {
            sharedPreferences.edit().putString(context.getString(R.string.key_snips_probability), value.toString()).apply()
        }

    // The time between face wake detection hot word activation
    var faceWakeDelayTime: Int
        get() : Int {
            val prefValue = sharedPreferences.getString(context.getString(R.string.key_snips_face_wake_interval), context.getString(R.string.default_snips_face_wake_interval))
            return if(prefValue != null)   {
                prefValue.toIntOrNull()?:context.getString(R.string.default_snips_face_wake_interval).toInt()
            } else {
                context.getString(R.string.default_snips_face_wake_interval).toInt()
            }
        }
        set(value) {
            sharedPreferences.edit().putString(context.getString(R.string.key_snips_face_wake_interval), value.toString()).apply()
        }

    private fun getBoolPref(resId: Int, defId: Int): Boolean {
        return sharedPreferences.getBoolean(
                context.getString(resId),
                java.lang.Boolean.valueOf(context.getString(defId))
        )
    }

    companion object {
        const val COMMAND_TOPIC_HERMES = "hermes/intent/"
        const val SUBSCRIBE_TOPIC_HERMES = "hermes/dialogueManager/endSession"
        // hermes/tts/say in
        //hermes/tts/sayFinished in

        //topics.add("hermes/intent/#")
        //topics.add("hermes/asr/#")
        //topics.add("hermes/hotword/#")
        //topics.add("hermes/nlu/#")
    }
}