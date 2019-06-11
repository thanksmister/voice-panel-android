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
import com.thanksmister.iot.voicepanel.persistence.IntentDao
import com.thanksmister.iot.voicepanel.persistence.IntentMessageModel
import com.thanksmister.iot.voicepanel.persistence.MessageDao
import com.thanksmister.iot.voicepanel.persistence.MessageMqtt
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class LogsViewModel @Inject
constructor(application: Application, private val messageDataSource: MessageDao, private val dataSource: IntentDao) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()
    private val toastText = ToastMessage()
    private val alertText = AlertMessage()

    fun getToastMessage(): ToastMessage {
        return toastText
    }

    fun getAlertMessage(): AlertMessage {
        return alertText
    }

    private fun showAlertMessage(message: String) {
        alertText.value = message
    }

    private fun showToastMessage(message: String) {
        toastText.value = message
    }

    init {
    }

    fun getMessages():Flowable<List<MessageMqtt>> {
        return messageDataSource.getMessages()
                .filter {messages -> messages.isNotEmpty()}
    }

    fun getIntentMessages(): Flowable<List<IntentMessageModel>> {
        return dataSource.getItems()
    }

    fun clearMessages():Completable {
        return Completable.fromAction {
            messageDataSource.deleteAllMessages()
        }
    }

    fun clearCommands():Completable  {
        return Completable.fromAction {
            dataSource.deleteAllItems()
        }
    }

    public override fun onCleared() {
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }

    companion object {
    }
}