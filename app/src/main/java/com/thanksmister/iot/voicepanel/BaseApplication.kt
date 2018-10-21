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

package com.thanksmister.iot.voicepanel

import android.content.Context
import android.os.Process
import android.support.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.thanksmister.iot.voicepanel.di.DaggerApplicationComponent
import com.thanksmister.iot.voicepanel.utils.CrashlyticsTree

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.fabric.sdk.android.Fabric

import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class BaseApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder().create(this);
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build())
        } else {
            Fabric.with(this, Crashlytics())
            Timber.plant(CrashlyticsTree())
        }

        // Snips launches a second process to run the platform, this allows to completely free the memory used by Snips
        // when not using it (the .so are quite huge, and you can't unload them from a process on Android) and it
        // isolates you app from potential crashes of the platform (we strive no to crash but this can happen, and the
        // OS is less forgiving with a segfault in native code than with an uncaught exception in Java... )
        // The application is instantiated in both the main process and the snips one. If you need to initialize things
        // here, check you're not in the snips process
        if (!isSnipsProcess()) {
            Timber.d("in the main process")
            // do some init here
        } else {
            Timber.d("in the snips process")
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun isSnipsProcess(): Boolean {
        val cmdline = File("/proc/" + Process.myPid() + "/cmdline")
        try {
            BufferedReader(FileReader(cmdline)).use { reader -> return reader.readLine().contains(":snipsProcessingService") }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }
}