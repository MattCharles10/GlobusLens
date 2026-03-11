package com.globuslens.analytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporting @Inject constructor() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun logException(throwable: Throwable) {
        crashlytics.recordException(throwable)
        Log.e("CrashReporting", "Exception logged", throwable)
    }

    fun logMessage(message: String) {
        crashlytics.log(message)
        Log.d("CrashReporting", message)
    }

    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun logScreenView(screenName: String) {
        crashlytics.log("Screen viewed: $screenName")
    }
}