/*
 * Copyright (C) 2023-2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.xiaomi.settings.R

/** Main settings screen: touch and thermal controls. */

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_prefs)
    }
}
