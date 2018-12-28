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

import android.content.pm.PackageManager.FEATURE_MICROPHONE
import android.os.Bundle
import androidx.preference.SwitchPreference
import com.afollestad.assent.Permission.RECORD_AUDIO
import com.afollestad.assent.runWithPermissions
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_COUNTDOWN
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORDINGS_FOLDER
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORD_AUDIO
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.ui.settings.base.BaseSettingsFragment
import com.afollestad.mnmlscreenrecord.ui.settings.showNumberSelector
import com.afollestad.mnmlscreenrecord.ui.settings.showOutputFolderSelector
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
class SettingsRecordingFragment : BaseSettingsFragment() {

  private val countdownPref by inject<Pref<Int>>(name = PREF_COUNTDOWN)
  private val recordAudioPref by inject<Pref<Boolean>>(name = PREF_RECORD_AUDIO)
  internal val recordingsFolderPref by inject<Pref<String>>(name = PREF_RECORDINGS_FOLDER)

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) {
    setPreferencesFromResource(R.xml.settings_recording, rootKey)

    setupCountdownPref()
    setupRecordAudioPref()
    setupRecordingsFolderPref()
  }

  private fun setupCountdownPref() {
    val countdownEntry = findPreference(PREF_COUNTDOWN)
    countdownEntry.setOnPreferenceClickListener {
      showNumberSelector(
          title = countdownEntry.title.toString(),
          max = 10,
          current = countdownPref.get()
      ) { selection -> countdownPref.set(selection) }
      true
    }
    countdownPref.observe()
        .distinctUntilChanged()
        .subscribe {
          countdownEntry.summary = if (it == 0) {
            resources.getString(R.string.setting_countdown_disabled)
          } else {
            resources.getQuantityString(
                R.plurals.setting_countdown_desc, it, it
            )
          }
        }
        .attachLifecycle(this)
  }

  private fun setupRecordAudioPref() {
    val micPresent = settingsActivity.packageManager.hasSystemFeature(FEATURE_MICROPHONE)
    val recordAudioEntry = findPreference(PREF_RECORD_AUDIO) as SwitchPreference
    if (!micPresent) {
      recordAudioEntry.run {
        isEnabled = false
        summary = getString(R.string.setting_record_audio_no_mic)
      }
      return
    }

    recordAudioEntry.isEnabled = true
    recordAudioEntry.setOnPreferenceChangeListener { _, newValue ->
      if (newValue == true) {
        runWithPermissions(RECORD_AUDIO) {
          recordAudioPref.set(newValue as Boolean)
          recordAudioEntry.isChecked = true
        }
        false
      } else {
        recordAudioPref.set(false)
        true
      }
    }
    recordAudioPref.observe()
        .distinctUntilChanged()
        .subscribe { recordAudioEntry.isChecked = it }
        .attachLifecycle(this)
  }

  private fun setupRecordingsFolderPref() {
    val recordingsFolderEntry = findPreference(PREF_RECORDINGS_FOLDER)
    recordingsFolderEntry.setOnPreferenceClickListener {
      showOutputFolderSelector(recordingsFolderEntry.title.toString())
      true
    }
    recordingsFolderPref.observe()
        .distinctUntilChanged()
        .subscribe {
          recordingsFolderEntry.summary = resources.getString(
              R.string.setting_recordings_folder_desc, it
          )
        }
        .attachLifecycle(this)
  }
}
