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

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.thanksmister.iot.voicepanel.network.IntentConverter

/**
 * Created by Michael Ritchie on 10/15/18.
 */
@Entity(tableName = "Intents")
class IntentMessage {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @SerializedName("customData")
    @ColumnInfo(name = "customData")
    var customData: String? = null

    @ColumnInfo(name = "input")
    @SerializedName("input")
    var input: String? = null

    @ColumnInfo(name = "intent")
    @SerializedName("intent")
    @TypeConverters(IntentConverter::class)
    var intent: Intent? = null

    @ColumnInfo(name = "response")
    @SerializedName("response")
    @TypeConverters(IntentResponseConverter::class)
    var response: IntentResponse? = null

    @ColumnInfo(name = "sessionId")
    @SerializedName("sessionId")
    var sessionId: String? = null

    @ColumnInfo(name = "siteId")
    @SerializedName("siteId")
    var siteId: String? = null

    @ColumnInfo(name = "slots")
    @SerializedName("slots")
    @TypeConverters(SlotsConverter::class)
    var slots: ArrayList<Slot>? = null

    @ColumnInfo(name = "createdAt")
    var createdAt: String? = null
}