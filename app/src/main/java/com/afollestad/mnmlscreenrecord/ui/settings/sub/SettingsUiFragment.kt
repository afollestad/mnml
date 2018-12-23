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
package com.afollestad.mnmlscreenrecord.ui.settings.sub

import android.os.Bundle
import androidx.preference.SwitchPreference
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.ui.settings.base.BaseSettingsFragment
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
class SettingsUiFragment : BaseSettingsFragment() {

  private val darkModePref by inject<Pref<Boolean>>(name = PrefNames.PREF_DARK_MODE)

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) {
    setPreferencesFromResource(R.xml.settings_ui, rootKey)

    val darkModeEntry = findPreference(PrefNames.PREF_DARK_MODE) as SwitchPreference
    darkModeEntry.setOnPreferenceChangeListener { _, newValue ->
      darkModePref.set(newValue as Boolean)
      settingsActivity.recreate()
      true
    }
    darkModePref.observe()
        .subscribe { darkModeEntry.isChecked = it }
        .attachLifecycle(this)
  }
}
