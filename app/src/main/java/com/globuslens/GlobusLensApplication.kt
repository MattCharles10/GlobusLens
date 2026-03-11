package com.globuslens

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GlobusLensApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Enable Crashlytics collection
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

            // Optional: Log that app started
            FirebaseCrashlytics.getInstance().log("App started")
        } catch (e: Exception) {
            Log.e("GlobusLensApp", "Failed to initialize Firebase", e)
        }
    }
}