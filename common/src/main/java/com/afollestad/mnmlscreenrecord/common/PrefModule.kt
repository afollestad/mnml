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

import android.os.Environment.getExternalStorageDirectory
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
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import org.koin.dsl.module.module
import java.io.File

/** @author Aidan Follestad (@afollestad) */
val prefModule = module {

  single { rxkPrefs(get(), "settings") }

  // Misc
  factory(name = PREF_DARK_MODE) {
    get<RxkPrefs>().boolean(PREF_DARK_MODE, false)
  }

  // Quality
  factory(name = PREF_VIDEO_BIT_RATE) {
    get<RxkPrefs>().integer(PREF_VIDEO_BIT_RATE, 8_000_000)
  }

  factory(name = PREF_AUDIO_BIT_RATE) {
    get<RxkPrefs>().integer(PREF_AUDIO_BIT_RATE, 128_000)
  }

  factory(name = PREF_FRAME_RATE) {
    get<RxkPrefs>().integer(PREF_FRAME_RATE, 30)
  }

  // Recording
  factory(name = PREF_COUNTDOWN) {
    get<RxkPrefs>().integer(PREF_COUNTDOWN, 0)
  }

  factory(name = PREF_RECORDINGS_FOLDER) {
    val default = File(getExternalStorageDirectory(), "MNML Screen Recorder")
    get<RxkPrefs>().string(PREF_RECORDINGS_FOLDER, default.absolutePath)
  }

  factory(name = PREF_RECORD_AUDIO) {
    get<RxkPrefs>().boolean(PREF_RECORD_AUDIO, false)
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
