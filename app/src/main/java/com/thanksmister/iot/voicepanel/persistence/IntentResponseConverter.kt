
package  com.thanksmister.iot.voicepanel.persistence

import android.arch.persistence.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thanksmister.iot.voicepanel.persistence.Intent

import java.lang.reflect.Type
import java.util.ArrayList

/**
 * Created by Michael Ritchie on 5/21/18.
 */
class IntentResponseConverter {
    @TypeConverter
    fun fromString(value: String): IntentResponse? {
        return Gson().fromJson(value, IntentResponse::class.java)
    }

    @TypeConverter
    fun toString(value: IntentResponse): String {
        return Gson().toJson(value)
    }
}
