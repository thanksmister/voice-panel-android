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

package com.thanksmister.iot.voicepanel.ui

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.thanksmister.iot.voicepanel.R
import com.thanksmister.iot.voicepanel.network.VoicePanelService
import com.thanksmister.iot.voicepanel.persistence.Configuration
import com.thanksmister.iot.voicepanel.persistence.IntentDao
import com.thanksmister.iot.voicepanel.utils.DialogUtils
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SettingsActivity : DaggerAppCompatActivity() {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var initDao: IntentDao
    private val disposable = CompositeDisposable()
    private val inactivityHandler: Handler = Handler()
    private val inactivityCallback = Runnable {
        Toast.makeText(this@SettingsActivity, getString(R.string.toast_screen_timeout), Toast.LENGTH_LONG).show()
        dialogUtils.clearDialogs()
        finish()
    }

    public override fun onCreate(savedInstance: Bundle?) {

        super.onCreate(savedInstance)

        setContentView(R.layout.activity_settings)

        if (supportActionBar != null) {
            supportActionBar!!.show()
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        // Stop our service for performance reasons and to pick up changes
        val service = Intent(this, VoicePanelService::class.java)
        stopService(service)

        configuration.isFirstTime = false

        lifecycle.addObserver(dialogUtils)

        //initializeCommandList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        } else if (id == R.id.action_help) {
            support()
        } else if (id == R.id.action_logs) {
            logs()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        val itemLen = menu.size()
        for (i in 0 until itemLen) {
            val drawable = menu.getItem(i).icon
            if (drawable != null) {
                drawable.mutate()
                drawable.setColorFilter(resources.getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP)
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        inactivityHandler.postDelayed(inactivityCallback, 300000)
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityHandler.removeCallbacks(inactivityCallback)
        if(!disposable.isDisposed) {
            disposable.clear()
        }
    }

    override fun onUserInteraction() {
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, 300000)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Timber.d("onKeyDown")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish()
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun support() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL)))
        } catch (ex: android.content.ActivityNotFoundException) {
            Timber.e(ex.message)
        }
    }

    private fun logs() {
        startActivity(Intent(LogActivity.createStartIntent(this@SettingsActivity)))
    }

    // Initialize the command list when we initialize the assistant
    private fun initializeCommandList() {
        configuration.initializedVoice = false
        disposable.add(Completable.fromAction {
            initDao.deleteAllItems()
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    companion object {
        fun createStartIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
        const val SUPPORT_URL:String = "https://thanksmister.com/voice-panel-android/"
    }
}