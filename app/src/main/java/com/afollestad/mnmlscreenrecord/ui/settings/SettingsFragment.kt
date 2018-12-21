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
import android.view.View
import android.view.WindowManager
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.afollestad.assent.Permission.RECORD_AUDIO
import com.afollestad.assent.runWithPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.misc.otherwise
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_ALWAYS_SHOW_NOTIFICATION
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_AUDIO_BIT_RATE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_COUNTDOWN
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_FRAME_RATE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORDINGS_FOLDER
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORD_AUDIO
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RESOLUTION_HEIGHT
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RESOLUTION_WIDTH
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SCREEN_OFF
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SHAKE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_VIDEO_BIT_RATE
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.common.view.onScroll
import com.afollestad.rxkprefs.Pref
import io.reactivex.Observable.zip
import io.reactivex.functions.BiFunction
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (afollestad) */
class SettingsFragment : PreferenceFragmentCompat() {

  // UI
  private val darkModePref by inject<Pref<Boolean>>(name = PREF_DARK_MODE)
  // Quality
  private val frameRatePref by inject<Pref<Int>>(name = PREF_FRAME_RATE)
  private val resolutionWidthPref by inject<Pref<Int>>(name = PREF_RESOLUTION_WIDTH)
  private val resolutionHeightPref by inject<Pref<Int>>(name = PREF_RESOLUTION_HEIGHT)
  private val videoBitRatePref by inject<Pref<Int>>(name = PREF_VIDEO_BIT_RATE)
  private val audioBitRatePref by inject<Pref<Int>>(name = PREF_AUDIO_BIT_RATE)
  // Recording
  private val countdownPref by inject<Pref<Int>>(name = PREF_COUNTDOWN)
  private val recordAudioPref by inject<Pref<Boolean>>(name = PREF_RECORD_AUDIO)
  internal val recordingsFolderPref by inject<Pref<String>>(name = PREF_RECORDINGS_FOLDER)
  // Controls
  private val stopOnScreenOffPref by inject<Pref<Boolean>>(name = PREF_STOP_ON_SCREEN_OFF)
  private val alwaysShowNotificationPref by inject<Pref<Boolean>>(
      name = PREF_ALWAYS_SHOW_NOTIFICATION
  )
  private val stopOnShakePref by inject<Pref<Boolean>>(name = PREF_STOP_ON_SHAKE)

  private val windowManager by inject<WindowManager>()

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    listView.onScroll {
      (activity as? SettingsActivity)?.invalidateToolbarElevation(it)
    }
  }

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

    // RESOLUTION
    val resolutionEntry = findPreference("resolution")
    resolutionEntry.setOnPreferenceClickListener {
      val context = activity ?: return@setOnPreferenceClickListener true
      val options = windowManager.resolutionSettings(context)
          .map { size -> size.toString() }
          .toMutableList()
          .apply { add(0, getString(R.string.use_screen_resolution)) }

      val currentXByY = "${resolutionWidthPref.get()}x${resolutionHeightPref.get()}"
      val defaultIndex = options.indexOf(currentXByY)
          .otherwise(-1, 0)

      MaterialDialog(context).show {
        title(R.string.setting_resolution)
        listItemsSingleChoice(
            items = options,
            initialSelection = defaultIndex
        ) { _, which, text ->
          if (which == 0) {
            resolutionWidthPref.delete()
            resolutionHeightPref.delete()
          } else {
            val splitRes = text.split('x')
            resolutionWidthPref.set(splitRes[0].toInt())
            resolutionHeightPref.set(splitRes[1].toInt())
          }
        }
        positiveButton(R.string.select)
      }
      true
    }
    zip(resolutionWidthPref.observe(), resolutionHeightPref.observe(),
        BiFunction<Int, Int, Pair<Int, Int>> { w, h -> Pair(w, h) })
        .subscribe {
          if (it.first == 0 || it.second == 0) {
            resolutionEntry.summary =
                resources.getString(R.string.setting_resolution_current_screen)
          } else {
            resolutionEntry.summary = resources.getString(
                R.string.setting_resolution_desc, it.first, it.second
            )
          }
        }
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
    audioBitRateEntry.isVisible = recordAudioPref.get()
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
          if (it == 0) {
            countdownEntry.summary = resources.getString(R.string.setting_countdown_disabled)
          } else {
            countdownEntry.summary = resources.getQuantityString(
                R.plurals.setting_countdown_desc, it, it
            )
          }
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
        recordAudioPref.set(false)
        true
      }
    }
    recordAudioPref.observe()
        .subscribe {
          recordAudioEntry.isChecked = it
          audioBitRateEntry.isVisible = it
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
}
