/*
 * Copyright (C) 2023 Paranoid Android
 * Copyright (C) 2025 Neoteric OS
 * Copyright (C) 2025 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touch

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.xiaomi.settings.utils.FileUtils

/**
 * Applies the high touch polling rate preference to the focaltech
 * touch driver (bump_sample_rate) while the screen is on.
 */
class TouchPollingRateService : Service() {

    private var isPollingEnabled = false
    private val handler = Handler(Looper.getMainLooper())
    private val screenStateReceiver = ScreenStateReceiver()
    private val settingsObserver = SettingsObserver(handler)

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) Log.d(TAG, "Creating service")
        settingsObserver.update()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (DEBUG) Log.d(TAG, "Starting service")
        screenStateReceiver.register()
        settingsObserver.register()
        return START_STICKY
    }

    override fun onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service")
        screenStateReceiver.unregister()
        settingsObserver.unregister()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updatePollingRate() {
        FileUtils.writeLine(
            TOUCH_POLLING_PATH,
            if (isPollingEnabled) 1 else 0
        )
    }

    private inner class SettingsObserver(val handler: Handler) : ContentObserver(handler) {
        fun register() {
            applicationContext.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(KEY_TOUCH_POLLING_RATE),
                false, this
            )
        }

        fun unregister() {
            applicationContext.contentResolver.unregisterContentObserver(this)
        }

        fun update() {
            isPollingEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver,
                KEY_TOUCH_POLLING_RATE, 0
            ) != 0
            handler.post { updatePollingRate() }
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            update()
        }
    }

    private inner class ScreenStateReceiver : BroadcastReceiver() {
        private var registered = false

        fun register() {
            if (registered) return
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            applicationContext.registerReceiver(this, filter)
            registered = true
        }

        fun unregister() {
            if (!registered) return
            applicationContext.unregisterReceiver(this)
            registered = false
        }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> handler.post { updatePollingRate() }
                Intent.ACTION_SCREEN_OFF -> FileUtils.writeLine(TOUCH_POLLING_PATH, 0)
            }
        }
    }

    companion object {
        private const val TAG = "TouchPollingRate"
        private const val DEBUG = true
        private const val KEY_TOUCH_POLLING_RATE = "high_touch_polling_rate"
        private const val TOUCH_POLLING_PATH =
            "/sys/devices/virtual/touch/touch_dev/bump_sample_rate"
    }
}
