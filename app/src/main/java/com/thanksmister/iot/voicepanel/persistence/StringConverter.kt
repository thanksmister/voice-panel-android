
package com.thanksmister.iot.voicepanel.persistence

import android.arch.persistence.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type
import java.util.ArrayList

/**
 * Created by Michael Ritchie on 5/21/18.
 */
class StringConverter {
    @TypeConverter
    fun fromString(value: String): ArrayList<String>? {
        val listType = object : TypeToken<ArrayList<String>>() {

        }.type
        return Gson().fromJson<ArrayList<String>>(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
