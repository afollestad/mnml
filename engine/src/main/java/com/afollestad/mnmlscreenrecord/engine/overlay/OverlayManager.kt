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

import android.annotation.SuppressLint
import android.graphics.PixelFormat.TRANSLUCENT
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
import android.widget.TextView
import com.afollestad.mnmlscreenrecord.common.misc.inflateAs
import com.afollestad.mnmlscreenrecord.common.providers.SdkProvider
import com.afollestad.mnmlscreenrecord.engine.R
import com.afollestad.rxkprefs.Pref

/** @author Aidan Follestad (@afollestad) **/
interface OverlayManager {

  /**
   * Returns true if a countdown is in progress.
   */
  fun isCountingDown(): Boolean

  /**
   * Returns true if a countdown is configured when recording starts.
   */
  fun willCountdown(): Boolean

  /**
   * Counts down starting at the value of the countdown preference, showing a red number in the
   * middle of the screen for each second. The given [finished] callback is invoked when we reach 0.
   */
  fun countdown(finished: () -> Unit)
}

/** @author Aidan Follestad (@afollestad) **/
class RealOverlayManager(
  private val windowManager: WindowManager,
  private val layoutInflater: LayoutInflater,
  private val countdownPref: Pref<Int>,
  private val sdkProvider: SdkProvider
) : OverlayManager {
  companion object {
    private const val SECOND = 1000L
  }

  private var isCountingDown: Boolean = false

  override fun isCountingDown() = isCountingDown

  override fun willCountdown() = countdownPref.get() > 0

  override fun countdown(finished: () -> Unit) {
    isCountingDown = true
    val time = countdownPref.get()
    if (time <= 0) {
      isCountingDown = false
      finished()
      return
    }

    val textView: TextView = layoutInflater.inflateAs(R.layout.countdown_textview)
    textView.text = "$time"

    @Suppress("DEPRECATION")
    @SuppressLint("InlinedApi")
    val type = if (sdkProvider.hasAndroidO()) {
      TYPE_APPLICATION_OVERLAY
    } else {
      TYPE_SYSTEM_OVERLAY
    }
    val params = LayoutParams(
        WRAP_CONTENT, // width
        WRAP_CONTENT, // height
        type,
        FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL, // flags
        TRANSLUCENT // format
    )
    windowManager.addView(textView, params)

    nextCountdown(textView, time, finished)
  }

  private fun nextCountdown(
    view: TextView,
    nextSecond: Int,
    finished: () -> Unit
  ) {
    view.text = "$nextSecond"
    if (nextSecond == 0) {
      windowManager.removeView(view)
      isCountingDown = false
      finished()
      return
    }
    view.postDelayed({
      nextCountdown(view, nextSecond - 1, finished)
    }, SECOND)
  }
}
