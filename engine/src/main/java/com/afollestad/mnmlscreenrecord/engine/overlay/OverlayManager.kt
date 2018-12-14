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

import android.graphics.PixelFormat.TRANSLUCENT
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.widget.TextView
import com.afollestad.mnmlscreenrecord.common.misc.inflate
import com.afollestad.mnmlscreenrecord.engine.R
import com.afollestad.rxkprefs.Pref

/** @author Aidan Follestad (@afollestad) **/
class OverlayManager(
  private val windowManager: WindowManager,
  private val layoutInflater: LayoutInflater,
  private val countdownPref: Pref<Int>
) {
  companion object {
    private const val SECOND = 1000L
  }

  fun countdown(finished: () -> Unit) {
    val time = countdownPref.get()
    if (time <= 0) {
      finished()
      return
    }

    val textView: TextView = layoutInflater.inflate(R.layout.countdown_textview)
    textView.text = "$time"

    val params = LayoutParams(
        WRAP_CONTENT, // width
        WRAP_CONTENT, // height
        TYPE_APPLICATION_OVERLAY, // type
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
      finished()
      return
    }
    view.postDelayed({
      nextCountdown(view, nextSecond - 1, finished)
    }, SECOND)
  }
}
