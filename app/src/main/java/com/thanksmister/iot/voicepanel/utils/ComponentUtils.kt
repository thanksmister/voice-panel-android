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

/**
 * Just a utility class to work with the multiple topics and commands for different components.
 */
class ComponentUtils {
    companion object {

        const val BASE_TOPIC = "voicepanel"

        const val COMPONENT_WEATHER_FORECAST_TYPE = "searchWeatherForecast"
        const val COMPONENT_WEATHER_FORECAST_CONDITION_TYPE = "searchWeatherForecastCondition"
        const val COMPONENT_WEATHER_FORECAST_TEMPERATURE_TYPE = "searchWeatherForecastTemperature"
        const val COMPONENT_WEATHER_FORECAST_ITEM_TYPE = "searchWeatherForecastItem"
        const val COMPONENT_HA_ALARM_DISARM = "thanksmister:HaAlarmDisarm"
        const val COMPONENT_HA_ALARM_DISARM_CODE = "thanksmister:HaAlarmDisarmCode"
        const val COMPONENT_HA_ALARM_HOME = "thanksmister:HaAlarmHome"
        const val COMPONENT_HA_ALARM_AWAY = "thanksmister:HaAlarmAway"
        const val COMPONENT_HA_ALARM_STATUS = "thanksmister:HaAlarmStatus"
        const val COMPONENT_STATUS = "thanksmister:getStatus"
        const val COMPONENT_CAMERA_CAPTURE = "thanksmister:cameraCapture"
        const val COMPONENT_CAMERA_ACTION = "thanksmister:cameraAction"
        const val COMPONENT_SNIPS_INIT = "initSnips"
        const val COMPONENT_HASS_TURN_ON = "hass:HassTurnOn"
        const val COMPONENT_HASS_TURN_OFF = "hass:HassTurnOff"
        const val COMPONENT_HASS_LIGHT_SET = "hass:HassLightSet"
        const val COMPONENT_HASS_OPEN_COVER = "hass:HassOpenCover"
        const val COMPONENT_HASS_CLOSE_COVER = "hass:HassCloseCover"
        const val COMPONENT_HASS_SHOPPING_LIST = "hass:HassShoppingListAddItem"
        const val COMPONENT_LIGHTS_TURN_OFF = "lightsTurnOff"
        const val COMPONENT_LIGHTS_SHIFT = "lightsShift"
        const val COMPONENT_LIGHTS_SET = "lightsSet"
        const val COMPONENT_SET_THERMOSTAT = "TSchmidty:setThermostat"
    }
}