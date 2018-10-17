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

package com.thanksmister.iot.voicepanel.utils

import android.text.TextUtils
import com.thanksmister.iot.voicepanel.persistence.Slot
import org.json.JSONArray
import timber.log.Timber
import org.json.JSONObject

/**
 * Created by Michael Ritchie on 7/9/18.
 */
class IntentUtils {

    companion object {
        @Throws(RuntimeException::class)
        fun getIntentRawValue(intentJson: String): String {
            Timber.d("intentJson $intentJson")
            try {
                val intentObj = JSONObject(intentJson)
                val slotsArray = intentObj.getJSONArray("slots")
                val slotsObject = slotsArray.getJSONObject(0)
                return slotsObject.getString("rawValue")
            } catch (e: Exception) {
                Timber.e("JSON parse error: " + e.message)
            }
            return ""
        }

        fun getStatusSlotText(slots: ArrayList<Slot>?):String {
            var slotText = ""
            Timber.d("slots: $slots")
            if(slots != null) {
                for(slot in slots) {
                    Timber.d("slot.entity: ${slot.entity}")
                    Timber.d("slot.rawValue: ${slot.rawValue}")
                    if(slot.entity == "entity_locale" && !TextUtils.isEmpty(slot.rawValue)) {
                        slotText = slot.rawValue!! + " " + slotText
                    } else if (slot.entity == "entity_id" && !TextUtils.isEmpty(slot.rawValue)) {
                        slotText = slotText +  " " + slot.rawValue!!
                    }
                }
            }
            return slotText
        }

        fun getHomeAssistantSlotText(slots: ArrayList<Slot>?):String  {
            var slotText = ""
            Timber.d("slots: $slots")
            if(slots != null) {
                for(slot in slots) {
                    Timber.d("slot.entity: ${slot.entity}")
                    Timber.d("slot.rawValue: ${slot.rawValue}")
                    if(slot.entity == "hass_entity" && !TextUtils.isEmpty(slot.rawValue)) {
                        slotText = slot.rawValue!!
                    }
                }
            }
            return slotText
        }
    }
}
