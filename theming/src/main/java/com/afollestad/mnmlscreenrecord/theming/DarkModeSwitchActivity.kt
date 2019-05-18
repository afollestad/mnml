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
package com.afollestad.mnmlscreenrecord.theming

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE_AUTOMATIC
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE_END
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE_START
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.rxkprefs.Pref
import io.reactivex.Observable.combineLatest
import io.reactivex.functions.Function4
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import timber.log.Timber.d as log

/** @author Aidan Follestad (afollestad) */
abstract class DarkModeSwitchActivity : AppCompatActivity() {

  private var isDarkModeEnabled: Boolean = false
  private val darkModePref by inject<Pref<Boolean>>(named(PREF_DARK_MODE))
  private val darkModeAutomaticPref by inject<Pref<Boolean>>(named(PREF_DARK_MODE_AUTOMATIC))
  private val darkModeStartPref by inject<Pref<String>>(named(PREF_DARK_MODE_START))
  private val darkModeEndPref by inject<Pref<String>>(named(PREF_DARK_MODE_END))

  override fun onCreate(savedInstanceState: Bundle?) {
    isDarkModeEnabled = isDarkMode()
    setTheme(themeRes())
    super.onCreate(savedInstanceState)

    val darkMode = darkModePref.observe()
    val darkModeAutomatic = darkModeAutomaticPref.observe()
    val darkModeStart = darkModeStartPref.observe()
    val darkModeEnd = darkModeEndPref.observe()

    if (!BuildCompat.isAtLeastQ()) {
      // Below Q, we do not base dark theming on the system dark mode
      combineLatest(darkMode, darkModeAutomatic, darkModeStart, darkModeEnd,
          Function4<Boolean, Boolean, String, String, DarkModeSettings> { on, auto, start, end ->
            DarkModeSettings(on, auto, start, end)
          })
          .filter { it.isEnabled() != isDarkModeEnabled }
          .subscribe {
            log("Theme changed, recreating Activity.")
            recreate()
          }
          .attachLifecycle(this)
    }
  }

  private fun isDarkMode(): Boolean {
    if (BuildCompat.isAtLeastQ()) {
      // Let the system handle it with resource qualifiers
      return false
    }
    val settings = DarkModeSettings(
        on = darkModePref.get(),
        auto = darkModeAutomaticPref.get(),
        start = darkModeStartPref.get(),
        end = darkModeEndPref.get()
    )
    return settings.isEnabled()
  }

  protected open fun themeRes() = if (isDarkMode()) {
    R.style.AppTheme_Dark
  } else {
    R.style.AppTheme
  }
}
