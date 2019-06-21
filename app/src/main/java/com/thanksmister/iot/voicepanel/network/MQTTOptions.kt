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

import android.text.TextUtils
import com.thanksmister.iot.voicepanel.modules.SnipsOptions.Companion.SUBSCRIBE_TOPIC_HERMES
import com.thanksmister.iot.voicepanel.persistence.Configuration
import com.thanksmister.iot.voicepanel.utils.DeviceUtils
import com.thanksmister.iot.voicepanel.utils.MqttUtils.Companion.TOPIC_COMMAND
import timber.log.Timber

import java.util.*
import javax.inject.Inject

class MQTTOptions @Inject
constructor(private val configuration: Configuration) {

    val brokerUrl: String
        get() = if (!TextUtils.isEmpty(getBroker())) {
            if (getBroker().contains("http://") || getBroker().contains("https://")) {
                String.format(Locale.getDefault(), HTTP_BROKER_URL_FORMAT, getBroker(), getPort())
            } else if (getTlsConnection()) {
                String.format(Locale.getDefault(), SSL_BROKER_URL_FORMAT, getBroker(), getPort())
            } else {
                String.format(Locale.getDefault(), TCP_BROKER_URL_FORMAT, getBroker(), getPort())
            }
        } else ""

    val isValid: Boolean
        get() = if (getTlsConnection()) {
            !TextUtils.isEmpty(getBroker()) &&
                    !TextUtils.isEmpty(getClientId()) &&
                    getStateTopics().isNotEmpty() &&
                    !TextUtils.isEmpty(getAlarmCommandTopic()) &&
                    !TextUtils.isEmpty(getAlarmStateTopic()) &&
                    !TextUtils.isEmpty(getUsername()) &&
                    !TextUtils.isEmpty(getPassword())
        } else !TextUtils.isEmpty(getBroker()) &&
                !TextUtils.isEmpty(getClientId()) &&
                getStateTopics().isNotEmpty() &&
                !TextUtils.isEmpty(getAlarmCommandTopic())

    fun getBroker(): String {
        Timber.d("getBroker ${configuration.mqttBroker}")
        return configuration.mqttBroker
    }

    fun getBaseTopic(): String {
        return configuration.mqttBaseTopic
    }

    fun getStateTopic(): String {
        return getBaseTopic() + "/" + TOPIC_COMMAND
    }

    fun getClientId(): String {
        var clientId = configuration.mqttClientId
        if (TextUtils.isEmpty(clientId)) {
            clientId = DeviceUtils.uuIdHash
        }
        return clientId
    }

    fun getAlarmCommandTopic(): String {
        return configuration.alarmCommandTopic
    }

    fun getAlarmStateTopic(): String {
        return configuration.alarmStateTopic
    }

    fun getStateTopics(): Array<String> {
        val topics = ArrayList<String>()
        topics.add(getStateTopic())
        topics.add(SUBSCRIBE_TOPIC_HERMES)
        topics.add(getAlarmStateTopic())
        return topics.toArray(arrayOf<String>())
    }

    fun getUsername(): String {
        return configuration.mqttUsername
    }

    fun getPassword(): String {
        return configuration.mqttPassword
    }

    fun getPort(): Int {
        val port = configuration.mqttServerPort
        return port
    }

    fun getTlsConnection(): Boolean {
        return configuration.mqttTlsEnabled
    }

    fun setUsername(value: String) {
        configuration.mqttUsername = value
    }

    fun setClientId(value: String) {
        configuration.mqttClientId = value
    }

    fun setBroker(value: String) {
        Timber.d("setBroker $value")
        configuration.mqttBroker = value
    }

    fun setPort(value: Int) {
        configuration.mqttServerPort = value
    }

    fun setPassword(value: String) {
        configuration.mqttPassword = value
    }

    fun setBaseTopic(value: String) {
        configuration.mqttBaseTopic = value
    }

    fun setCommandTopic(value: String) {
        configuration.alarmCommandTopic = value
    }

    fun setAlarmTopic(value: String) {
        configuration.alarmStateTopic = value
    }

    fun setTlsConnection(value: Boolean) {
        configuration.mqttTlsEnabled = value
    }

    companion object {
        const val SSL_BROKER_URL_FORMAT = "ssl://%s:%d"
        const val TCP_BROKER_URL_FORMAT = "tcp://%s:%d"
        const val HTTP_BROKER_URL_FORMAT = "%s:%d"
    }
}