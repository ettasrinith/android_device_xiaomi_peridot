/*
 * Copyright (C) 2023-2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.Display.HdrCapabilities
import com.xiaomi.settings.touch.TouchPollingRateService
import com.xiaomi.settings.touch.TouchUtils

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (DEBUG) Log.i(TAG, "Received intent: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> onLockedBootCompleted(context)
            Intent.ACTION_BOOT_COMPLETED -> onBootCompleted(context)
        }
    }

    private fun onLockedBootCompleted(context: Context) {
        // Re-apply persisted touch modes
        val gameMode = Settings.Secure.getInt(
            context.contentResolver, SettingsFragment.KEY_GAME_MODE, 0)
        if (gameMode != 0) {
            TouchUtils.setTouchMode(TouchUtils.MODE_GAME_MODE, 1)
        }
        val expertMode = Settings.Secure.getInt(
            context.contentResolver, SettingsFragment.KEY_TOUCH_EXPERT_MODE, 0)
        if (expertMode != 0) {
            TouchUtils.setTouchMode(TouchUtils.MODE_EXPERT_MODE, expertMode)
        }

        context.startServiceAsUser(
            Intent(context, TouchPollingRateService::class.java), UserHandle.CURRENT)

        val displayManager = context.getSystemService(DisplayManager::class.java)
        if (displayManager != null) {
            displayManager.overrideHdrTypes(
                Display.DEFAULT_DISPLAY,
                intArrayOf(
                    HdrCapabilities.HDR_TYPE_DOLBY_VISION,
                    HdrCapabilities.HDR_TYPE_HDR10,
                    HdrCapabilities.HDR_TYPE_HLG,
                    HdrCapabilities.HDR_TYPE_HDR10_PLUS
                )
            )
        } else {
            Log.e(TAG, "DisplayManager service not available")
        }
    }

    private fun onBootCompleted(context: Context) {
        // Intentionally left blank; add post-boot logic here if needed.
    }

    companion object {
        private const val TAG = "XiaomiParts"
        private const val DEBUG = true
    }
}
