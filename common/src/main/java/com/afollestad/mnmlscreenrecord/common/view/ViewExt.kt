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
@file:JvmName("LivedataViewExtKt")

package com.afollestad.mnmlscreenrecord.common.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.SeekBar
import androidx.annotation.DimenRes
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

data class Size(
  val width: Int,
  val height: Int
)

data class Coordinate(
  val x: Int,
  val y: Int
)

fun Coordinate.isNear(
  other: Coordinate,
  threshold: Int
): Boolean {
  return abs(x - other.x) <= threshold &&
      abs(y - other.y) <= threshold
}

fun View.size() = Size(measuredWidth, measuredHeight)

fun View.locationOnScreen(): Coordinate {
  val location = IntArray(2)
  getLocationOnScreen(location)
  return Coordinate(location[0], location[1])
}

fun View.show() {
  visibility = VISIBLE
}

fun View.conceal() {
  visibility = INVISIBLE
}

fun View.hide() {
  visibility = GONE
}

fun View.enable() {
  isEnabled = true
}

fun View.disable() {
  isEnabled = false
}

fun <T : View> View.childAt(index: Int): T {
  @Suppress("UNCHECKED_CAST")
  return (this as ViewGroup).getChildAt(index) as T
}

fun View.showOrHide(show: Boolean) = if (show) show() else hide()

fun View.onLayout(cb: (view: View) -> Unit) {
  if (this.viewTreeObserver.isAlive) {
    this.viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
          override fun onGlobalLayout() {
            cb(this@onLayout)
            this@onLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
          }
        })
  }
}

fun View.dimenFloat(@DimenRes res: Int) = resources.getDimension(res)

fun View.dimenInt(@DimenRes res: Int) = resources.getDimensionPixelSize(res)

fun EditText.onTextChanged(
  @IntRange(from = 0, to = 10000) debounce: Int = 0,
  cb: (String) -> Unit
) {
  addTextChangedListener(object : TextWatcher {
    val callbackRunner = Runnable {
      cb(text.trim().toString())
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(
      s: CharSequence,
      start: Int,
      count: Int,
      after: Int
    ) = Unit

    override fun onTextChanged(
      s: CharSequence,
      start: Int,
      before: Int,
      count: Int
    ) {
      removeCallbacks(callbackRunner)
      if (debounce == 0) {
        callbackRunner.run()
      } else {
        postDelayed(callbackRunner, debounce.toLong())
      }
    }
  })
}

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
