/*
 * Copyright (C) 2023-2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.content.Context
import android.provider.Settings
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.xiaomi.settings.R

/** Main settings screen: touch and thermal controls. */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_prefs)

        findPreference<SwitchPreferenceCompat>(KEY_TOUCH_POLLING_RATE)?.apply {
            isChecked = Settings.Secure.getInt(
                requireContext().contentResolver, KEY_TOUCH_POLLING_RATE, 0) != 0
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                Settings.Secure.putInt(
                    requireContext().contentResolver,
                    KEY_TOUCH_POLLING_RATE,
                    if (enabled) 1 else 0
                )
                true
            }
        }
    }

    companion object {
        const val KEY_TOUCH_POLLING_RATE = "high_touch_polling_rate"
    }
}
