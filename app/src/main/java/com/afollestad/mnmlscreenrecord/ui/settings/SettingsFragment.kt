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
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.afollestad.assent.Permission.RECORD_AUDIO
import com.afollestad.assent.Permission.WRITE_EXTERNAL_STORAGE
import com.afollestad.assent.isAllGranted
import com.afollestad.assent.runWithPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_ALWAYS_SHOW_NOTIFICATION
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_AUDIO_BIT_RATE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_COUNTDOWN
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_FRAME_RATE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORDINGS_FOLDER
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORD_AUDIO
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SCREEN_OFF
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SHAKE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_VIDEO_BIT_RATE
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.common.view.onProgressChanged
import com.afollestad.rxkprefs.Pref
import kotlinx.android.synthetic.main.dialog_number_selector.view.label
import kotlinx.android.synthetic.main.dialog_number_selector.view.seeker
import org.koin.android.ext.android.inject
import java.io.File

/** @author Aidan Follestad (afollestad) */
class SettingsFragment : PreferenceFragmentCompat() {

  // UI
  private val darkModePref by inject<Pref<Boolean>>(name = PREF_DARK_MODE)
  // Quality
  private val videoBitRatePref by inject<Pref<Int>>(name = PREF_VIDEO_BIT_RATE)
  private val audioBitRatePref by inject<Pref<Int>>(name = PREF_AUDIO_BIT_RATE)
  private val frameRatePref by inject<Pref<Int>>(name = PREF_FRAME_RATE)
  // Recording
  private val countdownPref by inject<Pref<Int>>(name = PREF_COUNTDOWN)
  private val recordAudioPref by inject<Pref<Boolean>>(name = PREF_RECORD_AUDIO)
  private val recordingsFolderPref by inject<Pref<String>>(name = PREF_RECORDINGS_FOLDER)
  // Controls
  private val stopOnScreenOffPref by inject<Pref<Boolean>>(name = PREF_STOP_ON_SCREEN_OFF)
  private val alwaysShowNotificationPref by inject<Pref<Boolean>>(
      name = PREF_ALWAYS_SHOW_NOTIFICATION
  )
  private val stopOnShakePref by inject<Pref<Boolean>>(name = PREF_STOP_ON_SHAKE)

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) {
    setPreferencesFromResource(R.xml.settings, rootKey)

    // DARK MODE
    val darkModeEntry = findPreference(PREF_DARK_MODE) as SwitchPreference
    darkModeEntry.setOnPreferenceChangeListener { _, newValue ->
      darkModePref.set(newValue as Boolean)
      activity?.recreate()
      true
    }
    darkModePref.observe()
        .subscribe { darkModeEntry.isChecked = it }
        .attachLifecycle(this)

    // VIDEO BIT RATE
    val videoBitRateEntry = findPreference(PREF_VIDEO_BIT_RATE)
    videoBitRateEntry.setOnPreferenceClickListener {
      val rawValues = resources.getIntArray(R.array.bit_rate_values)
      val currentValue = videoBitRatePref.get()
      val defaultIndex = rawValues.indexOf(currentValue)

      MaterialDialog(activity!!).show {
        title(R.string.setting_bitrate)
        listItemsSingleChoice(
            res = R.array.bit_rate_options,
            initialSelection = defaultIndex
        ) { _, which, _ ->
          videoBitRatePref.set(rawValues[which])
        }
        positiveButton(R.string.select)
      }
      true
    }
    videoBitRatePref.observe()
        .subscribe {
          videoBitRateEntry.summary = getString(R.string.setting_bitrate_desc, it.bitRateString())
        }
        .attachLifecycle(this)

    // AUDIO BIT RATE
    val audioBitRateEntry = findPreference(PREF_AUDIO_BIT_RATE)
    audioBitRateEntry.setOnPreferenceClickListener {
      val rawValues = resources.getIntArray(R.array.audio_bit_rate_values)
      val currentValue = audioBitRatePref.get()
      val defaultIndex = rawValues.indexOf(currentValue)

      MaterialDialog(activity!!).show {
        title(R.string.setting_audio_bitrate)
        listItemsSingleChoice(
            res = R.array.audio_bit_rate_options,
            initialSelection = defaultIndex
        ) { _, which, _ ->
          audioBitRatePref.set(rawValues[which])
        }
        positiveButton(R.string.select)
      }
      true
    }
    audioBitRatePref.observe()
        .subscribe {
          audioBitRateEntry.summary =
              getString(R.string.setting_audio_bitrate_desc, it.bitRateString())
        }
        .attachLifecycle(this)

    // FRAME RATE
    val frameRateEntry = findPreference(PREF_FRAME_RATE)
    frameRateEntry.setOnPreferenceClickListener {
      val rawValues = resources.getIntArray(R.array.frame_rate_values)
      val currentValue = frameRatePref.get()
      val defaultIndex = rawValues.indexOf(currentValue)

      MaterialDialog(activity!!).show {
        title(R.string.setting_framerate)
        listItemsSingleChoice(
            res = R.array.frame_rate_options,
            initialSelection = defaultIndex
        ) { _, which, _ ->
          frameRatePref.set(rawValues[which])
        }
        positiveButton(R.string.select)
      }
      true
    }
    frameRatePref.observe()
        .subscribe {
          frameRateEntry.summary = getString(R.string.setting_framerate_desc, it)
        }
        .attachLifecycle(this)

    // COUNT DOWN
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
        .subscribe {
          countdownEntry.summary = resources.getQuantityString(
              R.plurals.setting_countdown_desc, it, it
          )
        }
        .attachLifecycle(this)

    // RECORD AUDIO
    val recordAudioEntry = findPreference(PREF_RECORD_AUDIO) as SwitchPreference
    recordAudioEntry.setOnPreferenceChangeListener { _, newValue ->
      if (newValue == true) {
        runWithPermissions(RECORD_AUDIO) {
          recordAudioPref.set(newValue as Boolean)
          recordAudioEntry.isChecked = true
        }
        false
      } else {
        true
      }
    }
    recordAudioPref.observe()
        .subscribe {
          recordAudioEntry.isChecked = it
        }
        .attachLifecycle(this)

    // RECORDINGS FOLDER
    val recordingsFolderEntry = findPreference(PREF_RECORDINGS_FOLDER)
    recordingsFolderEntry.setOnPreferenceClickListener {
      showOutputFolderSelector(recordingsFolderEntry.title.toString())
      true
    }
    recordingsFolderPref.observe()
        .subscribe {
          recordingsFolderEntry.summary = resources.getString(
              R.string.setting_recordings_folder_desc, it
          )
        }
        .attachLifecycle(this)

    // STOP ON SCREEN OFF
    val stopOnScreenOffEntry = findPreference(PREF_STOP_ON_SCREEN_OFF) as SwitchPreference
    stopOnScreenOffEntry.setOnPreferenceChangeListener { _, newValue ->
      stopOnScreenOffPref.set(newValue as Boolean)
      true
    }
    stopOnScreenOffPref.observe()
        .subscribe { stopOnScreenOffEntry.isChecked = it }
        .attachLifecycle(this)

    // ALWAYS SHOW NOTIFICATION
    val alwaysShowNotificationEntry =
      findPreference(PREF_ALWAYS_SHOW_NOTIFICATION) as SwitchPreference
    alwaysShowNotificationEntry.setOnPreferenceChangeListener { _, newValue ->
      alwaysShowNotificationPref.set(newValue as Boolean)
      true
    }
    alwaysShowNotificationPref.observe()
        .subscribe { alwaysShowNotificationEntry.isChecked = it }
        .attachLifecycle(this)

    // STOP ON SHAKE
    val stopOnShakeEntry = findPreference(PREF_STOP_ON_SHAKE) as SwitchPreference
    stopOnShakeEntry.setOnPreferenceChangeListener { _, newValue ->
      stopOnShakePref.set(newValue as Boolean)
      true
    }
    stopOnShakePref.observe()
        .subscribe { stopOnShakeEntry.isChecked = it }
        .attachLifecycle(this)
  }

  private fun showOutputFolderSelector(title: String) {
    if (!isAllGranted(WRITE_EXTERNAL_STORAGE)) {
      runWithPermissions(WRITE_EXTERNAL_STORAGE) {
        showOutputFolderSelector(title)
      }
      return
    }

    val initialFolder = File(recordingsFolderPref.get()).apply {
      mkdirs()
    }
    MaterialDialog(activity!!).show {
      title(text = title)
      folderChooser(
          allowFolderCreation = true,
          initialDirectory = initialFolder
      ) { _, folder ->
        recordingsFolderPref.set(folder.absolutePath)
      }
      positiveButton(R.string.select)
    }
  }

  private fun showNumberSelector(
    title: String,
    max: Int,
    current: Int,
    onSelection: (Int) -> Unit
  ) {
    val dialog = MaterialDialog(activity!!).show {
      title(text = title)
      message(R.string.setting_countdown_zero_note)
      customView(R.layout.dialog_number_selector)
      positiveButton(android.R.string.ok) {
        val seekBar = getCustomView()!!.seeker
        onSelection(seekBar.progress)
      }
    }

    val customView = dialog.getCustomView() ?: return
    customView.label.text = "$current"
    customView.seeker.max = max
    customView.seeker.progress = current
    customView.seeker.onProgressChanged {
      customView.label.text = "$it"
    }
  }

  private fun Int.bitRateString(): String {
    return if (this >= 1_000_000) {
      "${this / 1_000_000}mbps"
    } else {
      "${this / 1_000}kbps"
    }
  }
}
