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
import android.media.CamcorderProfile
import android.media.CamcorderProfile.QUALITY_HIGH
import android.view.WindowManager
import com.afollestad.mnmlscreenrecord.common.misc.DisplayInfo
import com.afollestad.mnmlscreenrecord.common.misc.displayInfo
import com.afollestad.mnmlscreenrecord.engine.capture.RecordingInfo.Companion
import timber.log.Timber

/**
 * Holds settings that will be used to capture the device's screen.
 *
 * @author Aidan Follestad (@afollestad)
 */
internal data class RecordingInfo(
  val width: Int,
  val height: Int,
  val frameRate: Int,
  val density: Int
) {
  companion object
}

/**
 * Gets an instance of [RecordingInfo], holding settings that fit the current device.
 */
internal fun Companion.get(
  context: Context,
  windowManager: WindowManager
): RecordingInfo {
  val displayInfo = windowManager.displayInfo()

  val configuration = context.resources.configuration
  val isLandscape = configuration.orientation == ORIENTATION_LANDSCAPE
  log("Display landscape: $isLandscape")

  // Get the best camera profile available. We assume MediaRecorder supports the highest.
  val camcorderProfile = CamcorderProfile.get(QUALITY_HIGH)
  val cameraWidth = camcorderProfile?.videoFrameWidth ?: -1
  val cameraHeight = camcorderProfile?.videoFrameHeight ?: -1
  val cameraFrameRate = camcorderProfile?.videoFrameRate ?: 30
  log("Camera size: $cameraWidth x $cameraHeight frameRate: $cameraFrameRate")

  val sizePercentage = 100 // TODO
  log("Size percentage: $sizePercentage")

  return calculateRecordingInfo(
      displayInfo, isLandscape, cameraWidth, cameraHeight,
      cameraFrameRate, sizePercentage
  )
}

private fun Companion.log(message: String) {
  Timber.tag("RecordingInfo")
  Timber.d(message)
}

private fun Companion.calculateRecordingInfo(
  displayInfo: DisplayInfo,
  isLandscapeDevice: Boolean,
  cameraWidth: Int,
  cameraHeight: Int,
  cameraFrameRate: Int,
  sizePercentage: Int
): RecordingInfo {
  // Scale the display size before any maximum size calculations.
  val displayWidth = displayInfo.width * sizePercentage / 100
  val displayHeight = displayInfo.height * sizePercentage / 100

  if (cameraWidth == -1 && cameraHeight == -1) {
    // No cameras. Fall back to the display size.
    return RecordingInfo(
        displayWidth, displayHeight, cameraFrameRate, displayInfo.density
    )
  }

  var frameWidth = if (isLandscapeDevice) cameraWidth else cameraHeight
  var frameHeight = if (isLandscapeDevice) cameraHeight else cameraWidth
  if (frameWidth >= displayWidth && frameHeight >= displayHeight) {
    // Frame can hold the entire display. Use exact values.
    return RecordingInfo(
        displayWidth, displayHeight, cameraFrameRate, displayInfo.density
    )
  }

  // Calculate new width or height to preserve aspect ratio.
  if (isLandscapeDevice) {
    frameWidth = displayWidth * frameHeight / displayHeight
  } else {
    frameHeight = displayHeight * frameWidth / displayWidth
  }
  return RecordingInfo(
      frameWidth, frameHeight, cameraFrameRate, displayInfo.density
  )
}
