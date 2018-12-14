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
package com.afollestad.mnmlscreenrecord.common.view

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView

/** Represents the width and height of something. */
data class Size(
  val width: Int,
  val height: Int
)

fun View.size() = Size(measuredWidth, measuredHeight)

fun View.show() {
  visibility = VISIBLE
}

fun View.hide() {
  visibility = GONE
}

fun View.showOrHide(show: Boolean) = if (show) show() else hide()

fun RecyclerView.onScroll(scroll: (Int) -> Unit) {
  addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)
      scroll(computeVerticalScrollOffset())
    }
  })
}

fun SeekBar.onProgressChanged(cb: (Int) -> Unit) {
  setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(
      seekBar: SeekBar?,
      progress: Int,
      fromUser: Boolean
    ) = cb(progress)

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
  })
}
