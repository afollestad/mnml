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
package com.afollestad.mnmlscreenrecord.engine.overlay

import android.app.Application
import android.provider.Settings
import com.afollestad.rxkprefs.Pref

/** @author Aidan Follestad (@afollestad) */
interface ShowTouchesManager {

  fun canShowTouches(): Boolean

  fun maybeShowTouches()

  fun stopShowingTouches()
}

/** @author Aidan Follestad (@afollestad) */
class RealShowTouchesManager(
  private val app: Application,
  private val showTouchesPref: Pref<Boolean>
) : ShowTouchesManager {

  companion object {
    private const val KEY_SHOW_TOUCHES = "show_touches"
  }

  private val contentResolver = app.contentResolver

  override fun canShowTouches() = Settings.System.canWrite(app)

  override fun maybeShowTouches() {
    if (showTouchesPref.get()) {
      showTouches(true)
    }
  }

  override fun stopShowingTouches() {
    if (showTouchesPref.get()) {
      showTouches(false)
    }
  }

  private fun showTouches(show: Boolean) {
    require(canShowTouches()) { "Must have WRITE_SECURE_SETTINGS permission." }
    Settings.System.putInt(contentResolver, KEY_SHOW_TOUCHES, show.toInt())
  }

  private fun Boolean.toInt() = if (this) 1 else 0
}
