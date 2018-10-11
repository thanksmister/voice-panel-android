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

package com.thanksmister.iot.voicepanel.utils;


import com.thanksmister.iot.voicepanel.R;

public class WeatherUtils {

    public static int getIconForWeatherCondition(String condition) {
        switch (condition) {
            case "clear-day":
            case "sunny":
            case "exceptional":
                return R.drawable.ic_clear_day;
            case "clear-night":
                return R.drawable.ic_clear_night;
            case "lightning-rainy":
            case "thunderstorm":
            case "lightning":
                return R.drawable.ic_thunderstorm;
            case "pouring":
            case "sleet":
                return R.drawable.ic_sleet;
            case "partlycloudy":
            case "partly-cloudy-day":
                return R.drawable.ic_partly_cloudy_day;
            case "partly-cloudy-night":
                return R.drawable.ic_partly_cloudy_night;
            case "cloudy":
                return R.drawable.ic_cloudy;
            case "fog":
                return R.drawable.ic_fog;
            case "hail":
                return R.drawable.ic_hail;
            case "tornado":
                return R.drawable.ic_tornado;
            case "snowy-rainy":
            case "snowy":
            case "snow":
                return R.drawable.ic_snow;
            case "wind":
            case "windy":
            case "windy-variant":
                return R.drawable.ic_wind;
            case "rain":
                return R.drawable.ic_rain;
            default:
                return 0;
        }
    }
}
