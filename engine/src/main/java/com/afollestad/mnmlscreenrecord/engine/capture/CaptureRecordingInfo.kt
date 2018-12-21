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
package com.afollestad.mnmlscreenrecord.engine.capture

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import com.afollestad.mnmlscreenrecord.common.misc.displayInfo
import com.afollestad.mnmlscreenrecord.common.misc.otherwise
import timber.log.Timber

internal data class RecordingInfo(
  val width: Int,
  val height: Int,
  val density: Int
)

internal fun RealCaptureEngine.getRecordingInfo(context: Context): RecordingInfo {
  val displayInfo = windowManager.displayInfo()
  val displayWidth = displayInfo.width
  val displayHeight = displayInfo.height

  val configuration = context.resources.configuration
  val isLandscape = configuration.orientation == ORIENTATION_LANDSCAPE
  log("Display: $displayWidth x $displayHeight, landscape: $isLandscape")

  val widthSetting = resolutionWidthPref.get()
      .otherwise(shouldReturn = displayWidth)
  val heightSetting = resolutionHeightPref.get()
      .otherwise(shouldReturn = displayHeight)
  log("Resolution setting: $widthSetting x $heightSetting")

  val frameWidth = if (isLandscape) heightSetting else widthSetting
  val frameHeight = if (isLandscape) widthSetting else heightSetting

  log("Final recording info: $frameWidth x $frameHeight @ ${displayInfo.density}")
  return RecordingInfo(
      width = frameWidth,
      height = frameHeight,
      density = displayInfo.density
  )
}

private fun log(message: String) {
  Timber.tag("CaptureRecordingInfo")
  Timber.d(message)
}
