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
import com.thanksmister.iot.voicepanel.persistence.*
import com.thanksmister.iot.voicepanel.utils.AlarmUtils
import com.thanksmister.iot.voicepanel.utils.AlarmUtils.Companion.ALARM_TYPE
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Michael Ritchie on 6/28/18.
 */
class VoiceViewModel @Inject
constructor(application: Application, private val dataSource: IntentDao, private val sunSource: SunDao,
            private val messageDataSource: MessageDao) : AndroidViewModel(application) {

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

    public override fun onCleared() {
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }

    fun getIntentMessages(): Flowable<List<IntentMessage>> {
        return dataSource.getItems()
    }

    fun getSun(): Flowable<Sun> {
        return sunSource.getItems()
                .filter {items -> items.isNotEmpty()}
                .map { items -> items[items.size - 1] }
    }

    fun getAlarmState():Flowable<String> {
        return messageDataSource.getMessages(ALARM_TYPE)
                .filter {messages -> messages.isNotEmpty()}
                .map {messages -> messages[messages.size - 1]}
                .map {message ->
                    Timber.d("state: " + message.payload)
                    message.payload
                }
    }

    fun clearCommands() {
        disposable.add(Completable.fromAction {
            dataSource.deleteAllItems()
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    companion object {

    }
}