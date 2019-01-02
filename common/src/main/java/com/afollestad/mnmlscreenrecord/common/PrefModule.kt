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
package com.afollestad.mnmlscreenrecord.common

import android.os.Environment.DIRECTORY_DCIM
import android.os.Environment.getExternalStoragePublicDirectory
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_ALWAYS_SHOW_NOTIFICATION
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_AUDIO_BIT_RATE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_COUNTDOWN
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE_AUTOMATIC
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE_END
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE_START
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_FRAME_RATE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORDINGS_FOLDER
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORD_AUDIO
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RESOLUTION_HEIGHT
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RESOLUTION_WIDTH
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_SHOW_TOUCHES
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SCREEN_OFF
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SHAKE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_VIDEO_BIT_RATE
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import org.koin.dsl.module.module
import java.io.File

/** @author Aidan Follestad (@afollestad) */
val prefModule = module {

  single { rxkPrefs(get(), "settings") }

  // UI
  factory(name = PREF_DARK_MODE) {
    get<RxkPrefs>().boolean(PREF_DARK_MODE, false)
  }

  factory(name = PREF_DARK_MODE_AUTOMATIC) {
    get<RxkPrefs>().boolean(PREF_DARK_MODE_AUTOMATIC, false)
  }

  factory(name = PREF_DARK_MODE_START) {
    get<RxkPrefs>().string(PREF_DARK_MODE_START, "20:00")
  }

  factory(name = PREF_DARK_MODE_END) {
    get<RxkPrefs>().string(PREF_DARK_MODE_END, "8:00")
  }

  // Quality
  factory(name = PREF_FRAME_RATE) {
    get<RxkPrefs>().integer(PREF_FRAME_RATE, 30)
  }

  factory(name = PREF_RESOLUTION_WIDTH) {
    get<RxkPrefs>().integer(PREF_RESOLUTION_WIDTH, 0)
  }

  factory(name = PREF_RESOLUTION_HEIGHT) {
    get<RxkPrefs>().integer(PREF_RESOLUTION_HEIGHT, 0)
  }

  factory(name = PREF_VIDEO_BIT_RATE) {
    get<RxkPrefs>().integer(PREF_VIDEO_BIT_RATE, 8_000_000)
  }

  factory(name = PREF_AUDIO_BIT_RATE) {
    get<RxkPrefs>().integer(PREF_AUDIO_BIT_RATE, 128_000)
  }

  // Recording
  factory(name = PREF_COUNTDOWN) {
    get<RxkPrefs>().integer(PREF_COUNTDOWN, 3)
  }

  factory(name = PREF_RECORDINGS_FOLDER) {
    val dcim = getExternalStoragePublicDirectory(DIRECTORY_DCIM)
    val default = File(dcim.parentFile, "MNML Screen Recorder")
    get<RxkPrefs>().string(PREF_RECORDINGS_FOLDER, default.absolutePath)
  }

  factory(name = PREF_RECORD_AUDIO) {
    get<RxkPrefs>().boolean(PREF_RECORD_AUDIO, false)
  }

  factory(name = PREF_SHOW_TOUCHES) {
    get<RxkPrefs>().boolean(PREF_SHOW_TOUCHES, false)
  }

  // Controls
  factory(name = PREF_STOP_ON_SCREEN_OFF) {
    get<RxkPrefs>().boolean(PREF_STOP_ON_SCREEN_OFF, true)
  }

  factory(name = PREF_ALWAYS_SHOW_NOTIFICATION) {
    get<RxkPrefs>().boolean(PREF_ALWAYS_SHOW_NOTIFICATION, false)
  }

  factory(name = PREF_STOP_ON_SHAKE) {
    get<RxkPrefs>().boolean(PREF_STOP_ON_SHAKE, true)
  }
}
