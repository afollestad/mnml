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
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
import android.hardware.display.VirtualDisplay
import timber.log.Timber

internal fun RealCaptureEngine.createVirtualDisplayAndStart(context: Context) {
  display = createVirtualDisplay(context)

  // Tiny delay so we don't record the cast "start now" dialog.
  handler.postDelayed({
    try {
      log("createVirtualDisplayAndStart - recorder = $recorder")
      recorder?.start() ?: throw Exception(
          "Recorder is unexpectedly null, this appears to be a device-specific issue."
      )
    } catch (e: RuntimeException) {
      isStarted = false
      onCancel.onNext(Unit)
      onError.onNext(StartRecordingException(e))
    }
  }, 150)

  isStarted = true
  onStart.onNext(Unit)
  log("Media recorder started")
}

internal fun RealCaptureEngine.createVirtualDisplay(context: Context): VirtualDisplay {
  val recordingInfo = getRecordingInfo(context)
  log("createVirtualDisplay - recorder = $recorder")
  val engineRecorder = recorder ?: throw Exception(
      "Recorder is unexpectedly null, this appears to be a device-specific issue."
  )
  val surface = engineRecorder.surface ?: throw Exception(
      "Recorder Surface is unexpectedly null."
  )
  val width = recordingInfo.width
  val height = recordingInfo.height
  log("Virtual display dimensions: $width x $height")
  return projection?.createVirtualDisplay(
      "MNMLCaptureEngine",
      width,
      height,
      recordingInfo.density,
      VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
      surface,
      null,
      null
  ) ?: throw Exception(
      "Projection unexpectedly null, this appears to be a device-specific issue."
  )
}

private fun log(message: String) {
  Timber.tag("CaptureVirtualDisplay")
  Timber.d(message)
}
