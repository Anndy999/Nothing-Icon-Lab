package com.anndy999.nothingiconlab

import android.app.Application
import android.util.Log

class NothingIconLabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i(LabLog.TAG, "Nothing Icon Lab ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    }
}
