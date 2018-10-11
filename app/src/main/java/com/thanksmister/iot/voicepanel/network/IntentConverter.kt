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

import ai.snips.hermes.IntentMessage
import android.arch.persistence.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class IntentConverter {
    @TypeConverter
    fun fromString(value: String): IntentMessage? {
        return Gson().fromJson(value, IntentMessage::class.java)
    }
    @TypeConverter
    fun toString(value: IntentMessage): String {
        val gson = GsonBuilder().disableHtmlEscaping().serializeNulls().create()
        return gson.toJson(value)
    }
}