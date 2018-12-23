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
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_ALWAYS_SHOW_NOTIFICATION
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SCREEN_OFF
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SHAKE
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.ui.settings.base.BaseSettingsFragment
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
class SettingsControlsFragment : BaseSettingsFragment() {

  private val stopOnScreenOffPref by inject<Pref<Boolean>>(name = PREF_STOP_ON_SCREEN_OFF)
  private val alwaysShowNotificationPref by inject<Pref<Boolean>>(
      name = PREF_ALWAYS_SHOW_NOTIFICATION
  )
  private val stopOnShakePref by inject<Pref<Boolean>>(name = PREF_STOP_ON_SHAKE)

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) {
    setPreferencesFromResource(R.xml.settings_controls, rootKey)

    setupAlwaysShowNotificationPref()
    setupStopOnScreenOffPref()
    setupStopOnShakePref()
  }

  private fun setupAlwaysShowNotificationPref() {
    val alwaysShowNotificationEntry =
      findPreference(PREF_ALWAYS_SHOW_NOTIFICATION) as SwitchPreference
    alwaysShowNotificationEntry.setOnPreferenceChangeListener { _, newValue ->
      alwaysShowNotificationPref.set(newValue as Boolean)
      true
    }
    alwaysShowNotificationPref.observe()
        .subscribe { alwaysShowNotificationEntry.isChecked = it }
        .attachLifecycle(this)
  }

  private fun setupStopOnScreenOffPref() {
    val stopOnScreenOffEntry = findPreference(PREF_STOP_ON_SCREEN_OFF) as SwitchPreference
    stopOnScreenOffEntry.setOnPreferenceChangeListener { _, newValue ->
      stopOnScreenOffPref.set(newValue as Boolean)
      true
    }
    stopOnScreenOffPref.observe()
        .subscribe { stopOnScreenOffEntry.isChecked = it }
        .attachLifecycle(this)
  }

  private fun setupStopOnShakePref() {
    val stopOnShakeEntry = findPreference(PREF_STOP_ON_SHAKE) as SwitchPreference
    stopOnShakeEntry.setOnPreferenceChangeListener { _, newValue ->
      stopOnShakePref.set(newValue as Boolean)
      true
    }
    stopOnShakePref.observe()
        .subscribe { stopOnShakeEntry.isChecked = it }
        .attachLifecycle(this)
  }
}
