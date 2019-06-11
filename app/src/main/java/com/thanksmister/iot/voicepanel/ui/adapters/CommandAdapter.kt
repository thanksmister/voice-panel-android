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

package com.thanksmister.iot.voicepanel.ui.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.persistence.IntentMessageModel
import com.thanksmister.iot.voicepanel.utils.ComponentUtils
import com.thanksmister.iot.voicepanel.utils.ComponentUtils.Companion.COMPONENT_HASS_SHOPPING_LIST_LAST_ITEMS
import com.thanksmister.iot.voicepanel.utils.DateUtils
import com.thanksmister.iot.voicepanel.utils.IntentUtils
import com.thanksmister.iot.voicepanel.utils.StringUtils
import kotlinx.android.synthetic.main.adapter_commands.view.*
import timber.log.Timber

class CommandAdapter(private val items: List<IntentMessageModel>?, private val listener: OnItemClickListener?) : RecyclerView.Adapter<CommandAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: IntentMessageModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandAdapter.ViewHolder {
        Timber.d("onCreateViewHolder")
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_commands, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        if (items == null) return 0
        return if (items.isNotEmpty()) items.size else 0
    }

    override fun onBindViewHolder(holder: CommandAdapter.ViewHolder, position: Int) {
        holder.bindItems(items!![position], position, listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindItems(item: IntentMessageModel, position: Int, listener: OnItemClickListener?) {
            if(item.intent != null) {
                val itemValue = ""
                when {
                    item.intent!!.intentName == ComponentUtils.COMPONENT_WEATHER_FORECAST_TYPE ||
                    item.intent!!.intentName == ComponentUtils.COMPONENT_WEATHER_FORECAST_CONDITION_TYPE ||
                    item.intent!!.intentName == ComponentUtils.COMPONENT_WEATHER_FORECAST_ITEM_TYPE ||
                    item.intent!!.intentName == ComponentUtils.COMPONENT_WEATHER_FORECAST_TEMPERATURE_TYPE -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_cloudy)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_weather)
                        val date = DateUtils.parseLocaleDateTime(item.createdAt)
                        itemView.commandItem.visibility = View.GONE
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HA_ALARM_DISARM ||
                            item.intent!!.intentName == ComponentUtils.COMPONENT_HA_ALARM_DISARM_CODE -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_lock_outline)
                        itemView.commandTitle.text = StringUtils.toTitleCase(itemView.context.getString(R.string.text_disarmed))
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HA_ALARM_HOME -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_lock_outline)
                        itemView.commandTitle.text = StringUtils.toTitleCase(itemView.context.getString(R.string.text_arm_home))
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HA_ALARM_AWAY  -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_lock_outline)
                        itemView.commandTitle.text = StringUtils.toTitleCase(itemView.context.getString(R.string.text_arm_away))
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HA_ALARM_STATUS -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_lock_outline)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_alarm)
                        val date = DateUtils.parseLocaleDateTime(item.createdAt)
                        itemView.commandItem.visibility = View.VISIBLE
                        itemView.commandItem.text = itemView.context.getString(R.string.text_command_status).toLowerCase()
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_LIGHTS_TURN_OFF ||
                            item.intent!!.intentName == ComponentUtils.COMPONENT_LIGHTS_SHIFT ||
                            item.intent!!.intentName == ComponentUtils.COMPONENT_LIGHTS_SET-> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_lightbulb)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_lights)
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HASS_OPEN_COVER -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_window_open)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_open_cover)
                        val slotText = IntentUtils.getHomeAssistantSlotText(item.slots)
                        if(!TextUtils.isEmpty(slotText)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = slotText
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HASS_CLOSE_COVER  -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_window_closed)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_close_cover)
                        val slotText = IntentUtils.getHomeAssistantSlotText(item.slots)
                        if(!TextUtils.isEmpty(slotText)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = slotText
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HASS_LIGHT_SET   -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_lightbulb)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_light_set)
                        val slotText = IntentUtils.getHomeAssistantSlotText(item.slots)
                        if(!TextUtils.isEmpty(slotText)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = slotText
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HASS_TURN_ON   -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_light_switch)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_turn_on)
                        val slotText = IntentUtils.getHomeAssistantSlotText(item.slots)
                        if(!TextUtils.isEmpty(slotText)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = slotText
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HASS_TURN_OFF  -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_light_switch)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_turn_off)
                        val slotText = IntentUtils.getHomeAssistantSlotText(item.slots)
                        if(!TextUtils.isEmpty(slotText)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = slotText
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_SNIPS_INIT -> {
                        itemView.typeIcon.setImageResource(R.drawable.small_logo)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_assistant)
                        val date = DateUtils.parseLocaleDateTime(item.createdAt)
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_HASS_SHOPPING_LIST_ADD_ITEM ||
                    item.intent!!.intentName == COMPONENT_HASS_SHOPPING_LIST_LAST_ITEMS -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_cart_outline)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_add_item)
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_STATUS -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_info_outline)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_status)
                        val slotText = IntentUtils.getStatusSlotText(item.slots)
                        if(!TextUtils.isEmpty(slotText)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = slotText
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_SET_THERMOSTAT -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_thermometer_lines)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_thermostat)
                        val date = DateUtils.parseLocaleDateTime(item.createdAt)
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_CAMERA_CAPTURE ||
                            item.intent!!.intentName == ComponentUtils.COMPONENT_CAMERA_ACTION-> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_cctv)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_camera)
                        val date = DateUtils.parseLocaleDateTime(item.createdAt)
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    item.intent!!.intentName == ComponentUtils.COMPONENT_GET_CURRENT_TIME ||
                            item.intent!!.intentName == ComponentUtils.COMPONENT_GET_CURRENT_DAY ||
                            item.intent!!.intentName == ComponentUtils.COMPONENT_GET_CURRENT_DATE -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_access_time)
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_time)
                        val date = DateUtils.parseLocaleDateTime(item.createdAt)
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                    else -> {
                        itemView.typeIcon.setImageResource(R.drawable.ic_hearing) // generic sensor icon
                        itemView.commandTitle.text = itemView.context.getString(R.string.text_command_what)
                        val date = DateUtils.parseLocaleDateTime(item.createdAt)
                        if(!TextUtils.isEmpty(itemValue)) {
                            itemView.commandItem.visibility = View.VISIBLE
                            itemView.commandItem.text = itemValue
                        } else {
                            itemView.commandItem.visibility = View.GONE
                        }
                    }
                }
            }
            if (listener != null) {
                itemView.setOnClickListener {
                    listener.onItemClick(item)
                }
            }
        }
    }
}