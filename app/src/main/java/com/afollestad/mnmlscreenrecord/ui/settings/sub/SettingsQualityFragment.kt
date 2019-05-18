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
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_AUDIO_BIT_RATE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_FRAME_RATE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORD_AUDIO
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_VIDEO_BIT_RATE
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.ui.settings.base.BaseSettingsFragment
import com.afollestad.mnmlscreenrecord.ui.settings.bitRateString
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

/** @author Aidan Follestad (@afollestad) */
class SettingsQualityFragment : BaseSettingsFragment() {

  private val frameRatePref by inject<Pref<Int>>(named(PREF_FRAME_RATE))
  //private val resolutionWidthPref by inject<Pref<Int>>(named(PREF_RESOLUTION_WIDTH))
  //private val resolutionHeightPref by inject<Pref<Int>>(named(PREF_RESOLUTION_HEIGHT))
  private val videoBitRatePref by inject<Pref<Int>>(named(PREF_VIDEO_BIT_RATE))
  private val audioBitRatePref by inject<Pref<Int>>(named(PREF_AUDIO_BIT_RATE))
  private val recordAudioPref by inject<Pref<Boolean>>(named(PREF_RECORD_AUDIO))

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) {
    setPreferencesFromResource(R.xml.settings_quality, rootKey)

    setupFrameRatePref()
    setupResolutionPref()
    setupVideoBitRatePref()
    setupAudioBitRatePref()
  }

  private fun setupFrameRatePref() {
    val frameRateEntry = findPreference(PREF_FRAME_RATE)
    frameRateEntry.setOnPreferenceClickListener {
      val rawValues = resources.getIntArray(R.array.frame_rate_values)
      val currentValue = frameRatePref.get()
      val defaultIndex = rawValues.indexOf(currentValue)

      MaterialDialog(settingsActivity).show {
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
        .distinctUntilChanged()
        .subscribe {
          frameRateEntry.summary = getString(R.string.setting_framerate_desc, it)
        }
        .attachLifecycle(this)
  }

  private fun setupResolutionPref() {
    val resolutionEntry = findPreference("resolution")
    resolutionEntry.isEnabled = false
    resolutionEntry.summary =
        "Android makes getting this to work hard. Disabled for now. https://github.com/afollestad/mnml"

    /*
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
    */
  }

  private fun setupVideoBitRatePref() {
    val videoBitRateEntry = findPreference(PREF_VIDEO_BIT_RATE)
    videoBitRateEntry.setOnPreferenceClickListener {
      val rawValues = resources.getIntArray(R.array.bit_rate_values)
      val currentValue = videoBitRatePref.get()
      val defaultIndex = rawValues.indexOf(currentValue)

      MaterialDialog(settingsActivity).show {
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
        .distinctUntilChanged()
        .subscribe {
          videoBitRateEntry.summary = getString(R.string.setting_bitrate_desc, it.bitRateString())
        }
        .attachLifecycle(this)
  }

  private fun setupAudioBitRatePref() {
    val audioBitRateEntry = findPreference(PREF_AUDIO_BIT_RATE)
    audioBitRateEntry.isVisible = recordAudioPref.get()

    audioBitRateEntry.setOnPreferenceClickListener {
      val context = activity ?: return@setOnPreferenceClickListener false
      val rawValues = resources.getIntArray(R.array.audio_bit_rate_values)
      val currentValue = audioBitRatePref.get()
      val defaultIndex = rawValues.indexOf(currentValue)

      MaterialDialog(context).show {
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
        .distinctUntilChanged()
        .subscribe {
          audioBitRateEntry.summary =
              getString(R.string.setting_audio_bitrate_desc, it.bitRateString())
        }
        .attachLifecycle(this)
  }
}
