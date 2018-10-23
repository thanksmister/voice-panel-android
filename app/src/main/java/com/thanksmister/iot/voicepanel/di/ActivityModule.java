

package com.thanksmister.iot.voicepanel.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.LocationManager;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;

import com.thanksmister.iot.voicepanel.modules.CameraReader;
import com.thanksmister.iot.voicepanel.modules.SensorReader;
import com.thanksmister.iot.voicepanel.modules.SnipsOptions;
import com.thanksmister.iot.voicepanel.network.MQTTOptions;
import com.thanksmister.iot.voicepanel.persistence.Configuration;
import com.thanksmister.iot.voicepanel.utils.DialogUtils;
import com.thanksmister.iot.voicepanel.utils.NotificationUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class ActivityModule {

    @Provides
    static DialogUtils providesDialogUtils(Application application) {
        return new DialogUtils(application);
    }

    @Provides
    static Resources providesResources(Application application) {
        return application.getResources();
    }

    @Provides
    static LayoutInflater providesInflater(Application application) {
        return (LayoutInflater) application.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Provides
    static LocationManager provideLocationManager(Application application) {
        return (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());
    }

    @Provides
    static Configuration provideConfiguration(Application app, SharedPreferences sharedPreferences) {
        return new Configuration(app, sharedPreferences);
    }

    @Provides
    static CameraReader provideCameraReader(Application app) {
        return new CameraReader(app);
    }

    @Provides
    static SensorReader provideSensorReader(Application app) {
        return new SensorReader(app);
    }

    @Provides
    static MQTTOptions provideMQTTOptions(Configuration configuration) {
        return new MQTTOptions(configuration);
    }

    @Provides
    static NotificationUtils notificationUtils(Application application) {
        return new NotificationUtils(application);
    }

    @Provides
    static SnipsOptions provideSnipsOptions(Application app, SharedPreferences sharedPreferences) {
        return new SnipsOptions(app, sharedPreferences);
    }
}