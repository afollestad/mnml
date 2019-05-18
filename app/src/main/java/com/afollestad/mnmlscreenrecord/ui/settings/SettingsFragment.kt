/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.mnmlscreenrecord.ui.settings

import android.os.Bundle
import androidx.core.os.BuildCompat
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.ui.settings.base.BaseSettingsFragment
import com.afollestad.mnmlscreenrecord.ui.settings.sub.SettingsControlsFragment
import com.afollestad.mnmlscreenrecord.ui.settings.sub.SettingsQualityFragment
import com.afollestad.mnmlscreenrecord.ui.settings.sub.SettingsRecordingFragment
import com.afollestad.mnmlscreenrecord.ui.settings.sub.SettingsUiFragment

/** @author Aidan Follestad (afollestad) */
class SettingsFragment : BaseSettingsFragment() {

  override val isRoot: Boolean get() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    findPreference("recording").setOnPreferenceClickListener { navigateTo(it.key) }
    findPreference("quality").setOnPreferenceClickListener { navigateTo(it.key) }
    findPreference("controls").setOnPreferenceClickListener { navigateTo(it.key) }

    if (BuildCompat.isAtLeastQ()) {
      // Dark theme is based on system settings on Q+
      findPreference("ui").isVisible = false
    } else {
      findPreference("ui").setOnPreferenceClickListener { navigateTo(it.key) }
    }
  }

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) = setPreferencesFromResource(R.xml.settings, rootKey)

  private fun navigateTo(key: String): Boolean {
    val target = when (key) {
      "ui" -> SettingsUiFragment()
      "recording" -> SettingsRecordingFragment()
      "quality" -> SettingsQualityFragment()
      "controls" -> SettingsControlsFragment()
      else -> return false
    }

    val fm = fragmentManager ?: return false
    fm.beginTransaction()
        .setTransition(TRANSIT_FRAGMENT_OPEN)
        .replace(R.id.container, target)
        .addToBackStack(key)
        .commit()

    return true
  }
}
