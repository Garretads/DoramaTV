package ru.garretech.garred.doramatv

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val crashlyticsKit = Crashlytics.Builder().core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()
        if (!BuildConfig.DEBUG) Fabric.with(baseContext, crashlyticsKit)
        if (!BuildConfig.DEBUG) RxJavaPlugins.setErrorHandler { t: Throwable? -> Log.e("RxJava error","Произошла ошибка",t) }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

}