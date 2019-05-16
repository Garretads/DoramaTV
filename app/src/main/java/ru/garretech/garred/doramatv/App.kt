package ru.garretech.garred.doramatv

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val crashlyticsKit = Crashlytics.Builder().core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()
        if (!BuildConfig.DEBUG) Fabric.with(baseContext, crashlyticsKit)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

}