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

package com.thanksmister.iot.voicepanel.di


import android.arch.lifecycle.ViewModel
import com.thanksmister.iot.voicepanel.BaseActivity
import com.thanksmister.iot.voicepanel.BaseFragment
import com.thanksmister.iot.voicepanel.network.VoicePanelService
import com.thanksmister.iot.voicepanel.ui.LiveCameraActivity
import com.thanksmister.iot.voicepanel.ui.SettingsActivity
import com.thanksmister.iot.voicepanel.ui.viewmodels.DetectionViewModel
import com.thanksmister.iot.voicepanel.ui.VoiceActivity
import com.thanksmister.iot.voicepanel.ui.fragments.*
import com.thanksmister.iot.voicepanel.ui.viewmodels.VoiceViewModel
import com.thanksmister.iot.voicepanel.ui.viewmodels.WeatherViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
internal abstract class AndroidBindingModule {

    @Binds
    @IntoMap
    @ViewModelKey(DetectionViewModel::class)
    abstract fun bindsDetectionViewModel(viewModel: DetectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VoiceViewModel::class)
    abstract fun bindsVoiceViewModel(viewModel: VoiceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WeatherViewModel::class)
    abstract fun bindsWeatherViewModel(viewModel: WeatherViewModel): ViewModel

    @ContributesAndroidInjector
    internal abstract fun service(): VoicePanelService

    @ContributesAndroidInjector
    internal abstract fun baseActivity(): BaseActivity

    @ContributesAndroidInjector
    internal abstract fun voiceActivity(): VoiceActivity

    @ContributesAndroidInjector
    internal abstract fun settingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    internal abstract fun liveCameraActivity(): LiveCameraActivity

    @ContributesAndroidInjector
    internal abstract fun baseFragment(): BaseFragment

    @ContributesAndroidInjector
    internal abstract fun baseSettingsFragment(): BaseSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun screenSettingsFragment(): ScreenSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun aboutFragment(): AboutFragment

    @ContributesAndroidInjector
    internal abstract fun settingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    internal abstract fun alarmSettingsFragment(): AlarmSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun notificationsSettingsFragment(): NotificationsSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun cameraSettingsFragment(): CameraSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun mqttSettingsFragment(): MqttSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun deviceSensorsFragment(): SensorsSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun weatherSettingsFragment(): WeatherSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun cameraCaptureSettingsFragment(): CaptureSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun faceSettingsFragment(): FaceSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun qrSettingsFragment(): QrSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun mjpegSettingsFragment(): MjpegSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun motionSettingsFragment(): MotionSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun assistantSettingsFragment(): AssistantSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun informationFragment(): InformationFragment
}