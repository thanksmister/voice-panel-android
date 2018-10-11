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

package com.thanksmister.iot.voicepanel.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.thanksmister.iot.voicepanel.architecture.AlertMessage
import com.thanksmister.iot.voicepanel.architecture.ToastMessage
import com.thanksmister.iot.voicepanel.persistence.Configuration
import com.thanksmister.iot.voicepanel.persistence.Weather
import com.thanksmister.iot.voicepanel.persistence.WeatherDao
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.UndeliverableException
import timber.log.Timber
import javax.inject.Inject

class WeatherViewModel @Inject
constructor(application: Application, private val dataSource: WeatherDao, private val configuration: Configuration) : AndroidViewModel(application) {

    private val toastText = ToastMessage()
    private val alertText = AlertMessage()
    private val disposable = CompositeDisposable()

    fun getToastMessage(): ToastMessage {
        return toastText
    }

    fun getAlertMessage(): AlertMessage {
        return alertText
    }

    init {
    }

    private fun showAlertMessage(message: String?) {
        Timber.d("showAlertMessage")
        alertText.value = message
    }

    private fun showToastMessage(message: String?) {
        Timber.d("showToastMessage")
        toastText.value = message
    }

    /**
     * Get the last item.
     * @return a [Flowable] that will emit every time the weather has been updated.
     */
    fun getLatestItem():Flowable<Weather> {
        return dataSource.getItems()
                .filter {items -> items.isNotEmpty() }
                .map { items -> items[0] }
    }


    public override fun onCleared() {
        Timber.d("onCleared")
        //prevents memory leaks by disposing pending observable objects
        if (!disposable.isDisposed) {
            try {
                disposable.clear()
            } catch (e: UndeliverableException) {
                Timber.e(e.message)
            }
        }
    }

    /**
     * Determines if today is a good day to take your umbrella
     * Adapted from https://github.com/HannahMitt/HomeMirror/.
     * @return
     */
    public fun shouldTakeUmbrellaToday(precipitation: Double?): Boolean {
        if(precipitation != null) {
            return precipitation > PRECIP_AMOUNT
        }
        return false
    }

    /**
     * Network connectivity receiver to notify client of the network disconnect issues and
     * to clear any network notifications when reconnected. It is easy for network connectivity
     * to run amok that is why we only notify the user once for network disconnect with
     * a boolean flag.
     */
    companion object {
        const val PRECIP_AMOUNT: Double = 0.3 // rain probability
    }
}