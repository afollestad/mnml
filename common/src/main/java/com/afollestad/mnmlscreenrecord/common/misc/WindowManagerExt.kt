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
package com.afollestad.mnmlscreenrecord.common.misc

import android.util.DisplayMetrics
import android.view.WindowManager
import timber.log.Timber

/** Wraps data for [displayInfo]. */
data class DisplayInfo(
  val width: Int,
  val height: Int,
  val density: Int
)

/** Retrieves a [DisplayInfo] instance from a [WindowManager]. */
fun WindowManager.displayInfo(): DisplayInfo {
  val metrics = DisplayMetrics()
  defaultDisplay.getRealMetrics(metrics)
  val info = DisplayInfo(
      metrics.widthPixels,
      metrics.heightPixels,
      metrics.densityDpi
  )
  Timber.tag("WindowManagerExt")
  Timber.d("Display: ${info.width} x ${info.height} @ ${info.density}")
  return info
}
