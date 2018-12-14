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
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_DARK_MODE
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject
import timber.log.Timber.d as log

/** @author Aidan Follestad (afollestad) */
abstract class DarkModeSwitchActivity : AppCompatActivity() {

  private var isDark: Boolean = false
  private val darkModePref by inject<Pref<Boolean>>(name = PREF_DARK_MODE)

  override fun onCreate(savedInstanceState: Bundle?) {
    isDark = darkModePref.get()
    setTheme(themeRes())
    super.onCreate(savedInstanceState)

    darkModePref.observe()
        .filter { it != isDark }
        .subscribe {
          log("Theme changed, recreating Activity.")
          recreate()
        }
        .attachLifecycle(this)
  }

  fun setDarkMode(dark: Boolean) = darkModePref.set(dark)

  fun isDarkMode() = darkModePref.get()

  fun toggleDarkMode() = setDarkMode(!isDarkMode())

  private fun themeRes() = if (darkModePref.get()) {
    R.style.AppTheme_Dark
  } else {
    R.style.AppTheme
  }
}
