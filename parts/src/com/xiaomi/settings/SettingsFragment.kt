/*
 * Copyright (C) 2023-2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.xiaomi.settings.R
import com.xiaomi.settings.touch.TouchUtils

/** Main settings screen: touch and display controls. */
class SettingsFragment : PreferenceFragmentCompat() {

    private fun secureInt(context: Context, key: String, def: Int) =
        Settings.Secure.getInt(context.contentResolver, key, def)

    private fun securePut(context: Context, key: String, value: Int) {
        Settings.Secure.putInt(context.contentResolver, key, value)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_prefs)

        val ctx = requireContext()

        findPreference<SwitchPreferenceCompat>(KEY_TOUCH_POLLING_RATE)?.apply {
            isChecked = secureInt(ctx, KEY_TOUCH_POLLING_RATE, 0) != 0
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                securePut(ctx, KEY_TOUCH_POLLING_RATE, if (enabled) 1 else 0)
                true
            }
        }

        findPreference<SwitchPreferenceCompat>(KEY_GAME_MODE)?.apply {
            isChecked = secureInt(ctx, KEY_GAME_MODE, 0) != 0
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                securePut(ctx, KEY_GAME_MODE, if (enabled) 1 else 0)
                TouchUtils.setTouchMode(TouchUtils.MODE_GAME_MODE, if (enabled) 1 else 0)
                true
            }
        }

        findPreference<ListPreference>(KEY_TOUCH_EXPERT_MODE)?.apply {
            setValueIndex(secureInt(ctx, KEY_TOUCH_EXPERT_MODE, 0).coerceIn(0, 3))
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                val value = (newValue as String).toInt()
                securePut(ctx, KEY_TOUCH_EXPERT_MODE, value)
                TouchUtils.setTouchMode(TouchUtils.MODE_EXPERT_MODE, value)
                summary = entry
                true
            }
        }

        findPreference<SwitchPreferenceCompat>(KEY_DC_DIMMING)?.apply {
            val state = TouchUtils.getDispFeature(TouchUtils.FEATURE_ID_DC_DIMMING)
            if (state >= 0) isChecked = state != 0
            setOnPreferenceChangeListener { _, newValue ->
                TouchUtils.setDispFeature(
                    TouchUtils.FEATURE_ID_DC_DIMMING, if (newValue as Boolean) 1 else 0)
                true
            }
        }

        findPreference<SwitchPreferenceCompat>(KEY_HBM)?.apply {
            val state = TouchUtils.getDispFeature(TouchUtils.FEATURE_ID_HBM)
            if (state >= 0) isChecked = state != 0
            setOnPreferenceChangeListener { _, newValue ->
                TouchUtils.setDispFeature(
                    TouchUtils.FEATURE_ID_HBM, if (newValue as Boolean) 1 else 0)
                true
            }
        }
    }

    companion object {
        const val KEY_TOUCH_POLLING_RATE = "high_touch_polling_rate"
        const val KEY_GAME_MODE = "touch_game_mode"
        const val KEY_TOUCH_EXPERT_MODE = "touch_expert_mode"
        const val KEY_DC_DIMMING = "dc_dimming"
        const val KEY_HBM = "high_brightness_mode"
    }
}
