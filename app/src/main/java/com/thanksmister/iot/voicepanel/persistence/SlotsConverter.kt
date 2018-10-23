
package com.thanksmister.iot.voicepanel.persistence

import android.arch.persistence.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList

/**
 * Created by Michael Ritchie on 5/21/18.
 */
class SlotsConverter {
    @TypeConverter
    fun fromString(value: String): ArrayList<Slot>? {
        val listType = object : TypeToken<ArrayList<Slot>>() {

        }.type
        return Gson().fromJson<ArrayList<Slot>>(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<Slot>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
