package ru.garretech.garred.doramatv

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val crashlyticsKit = Crashlytics.Builder().core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()
        if (BuildConfig.DEBUG) Fabric.with(this, crashlyticsKit)
    }
}