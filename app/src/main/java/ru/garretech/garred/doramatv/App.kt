package ru.garretech.garred.doramatv

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.yandex.mobile.ads.common.MobileAds
import io.reactivex.plugins.RxJavaPlugins


class App : Application() {

    private val YANDEX_MOBILE_ADS_TAG = "YandexMobileAds"

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)

        MobileAds.initialize(this) { Log.d(YANDEX_MOBILE_ADS_TAG, "SDK initialized") }

        // Инициализируем firebase логирование только если сборка в release варианте
        if (!BuildConfig.DEBUG) {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build()
            remoteConfig.setConfigSettingsAsync(configSettings)
        }

        if (!BuildConfig.DEBUG) RxJavaPlugins.setErrorHandler { t: Throwable? -> Log.e("RxJava error","Произошла ошибка",t) }
    }


}