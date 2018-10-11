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
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Date utils
 */
object DateUtils {

    var SECONDS_VALUE = 60000
    var MINUTES_VALUE = 1800000

    fun parseDate(dateString: String?): Date {
        if(TextUtils.isEmpty(dateString)) return Date()
        val fmt = DateTimeFormat.patternForStyle("SS", Locale.getDefault())
        val dateTime = DateTime(dateString)
        return dateTime.toDate()
    }

    fun parseCreatedAtDate(dateString: String?): String? {
        if(TextUtils.isEmpty(dateString)) return dateString
        val fmt = DateTimeFormat.patternForStyle("SS", Locale.getDefault())
        val dateTime = DateTime(dateString)
        dateTime.toLocalDateTime()
        return dateTime.toLocalDateTime().toString(fmt)
    }

    fun parseCreatedAtDateToLong(dateString: String?): Long {
        if(TextUtils.isEmpty(dateString)) return Date().time
        val fmt = DateTimeFormat.patternForStyle("SS", Locale.getDefault())
        val dateTime = DateTime(dateString)
        dateTime.toLocalDateTime()
        return dateTime.toLocalDateTime().toDate().time
    }

    fun generateCreatedAtDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        return dateFormat.format(Date())
    }

    fun generateCreatedAtDate(dateString: String?): String {
        val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        return dateFormat.parse(dateString).toString()
    }

    fun padTimePickerOutput(timeValue: String): String {
        var value = timeValue
        if (value.length == 1) {
            value = "0$value"
        }
        return value
    }

    fun getHourFromTimePicker(timeValue: String): Int {
        val values = timeValue.split(":")
        if(values.size > 1) {
            val value = values[0].toInt()
            return value
        }
        return 0
    }

    fun getMinutesFromTimePicker(timeValue: String): Int {
        val values = timeValue.split(":")
        if(values.size > 1) {
            val value = values[1].toInt()
            return value
        }
        return 0
    }

    fun getHourAndMinutesFromTimePicker(timePickerValue: String): Float {
        return timePickerValue.replace(":", ".").toFloat()
    }

    /**
     * This converts the milliseconds to a day of the week, but we try to account
     * for time that is shorter than expected from DarkSky API .
     * @param apiTime
     * @return
     */
    fun dayOfWeek(apiTime: Long): String {
        var time = apiTime
        if (apiTime.toString().length == 10) {
            time = apiTime * 1000
        }
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(time))
    }

    fun convertInactivityTime(inactivityValue: Long): String {
        return if (inactivityValue < SECONDS_VALUE) {
            TimeUnit.MILLISECONDS.toSeconds(inactivityValue).toString()
        } else if (inactivityValue > MINUTES_VALUE) {
            TimeUnit.MILLISECONDS.toHours(inactivityValue).toString()
        } else {
            TimeUnit.MILLISECONDS.toMinutes(inactivityValue).toString()
        }
    }

    fun parseLocalDateISO(dateTime: String): Date {
        var dateTime = dateTime
        try {
            if (dateTime.contains(".")) {
                val period = dateTime.indexOf(".")
                Timber.d("Date: $dateTime")
                Timber.d("period: $period")

                if (period > 0) {
                    val replace = dateTime.substring(period, period + 5)
                    Timber.d("replace: $replace")
                    dateTime = dateTime.replace(replace, "")
                }
            }
            return ISO8601.toCalendar(dateTime).time
        } catch (e: Exception) {
            Timber.e(e.message)
        }

        return Date()
    }

    fun parseLocalDateStringAbbreviatedTime(dateTime: String): String {

        var dateString: String
        // Date
        val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        try {
            dateString = dateFormat.format(ISO8601.toCalendar(dateTime).time)
        } catch (e: ParseException) {
            val date = Date()
            dateString = dateFormat.format(date.time)
        }

        return dateString
    }

    fun parseLocaleDate(dateTime: String): String {
        var dateString: String

        // Date
        val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
        try {
            dateString = dateFormat.format(ISO8601.toCalendar(dateTime).time)
        } catch (e: ParseException) {
            val date = Date()
            dateString = dateFormat.format(date.time)
        }

        return dateString
    }

    fun parseLocaleDateTime(dateTime: String?): String {
        var dateString: String

        // Date
        val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        try {
            dateString = dateFormat.format(ISO8601.toCalendar(dateTime).time)
        } catch (e: ParseException) {
            val date = Date()
            dateString = dateFormat.format(date.time)
        }

        return dateString
    }

    fun parseLastSeenDate(dateTime: String?): Date {
        try {
            return ISO8601.toCalendar(dateTime).time
        } catch (e: ParseException) {
            Timber.d("Error parsing last seen date")
        }

        return Date()
    }

}