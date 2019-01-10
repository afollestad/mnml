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
@file:Suppress("unused")

package com.afollestad.mnmlscreenrecord.engine.gesture

import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import com.squareup.seismic.ShakeDetector
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad) */
internal class ShakeListener(
  private val sensorManager: SensorManager,
  private val vibrator: Vibrator,
  private val callback: () -> Unit
) : ShakeDetector.Listener {

  companion object {
    private const val VIBRATION_DURATION = 200L
  }

  override fun hearShake() {
    log("Detected shake")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, DEFAULT_AMPLITUDE))
    } else {
      @Suppress("DEPRECATION")
      vibrator.vibrate(VIBRATION_DURATION)
    }
    callback.invoke()
  }

  private var shakeDetector: ShakeDetector? = null

  /** Initializes the shake detector. */
  fun start() {
    log("start()")
    if (shakeDetector != null) {
      return
    }
    shakeDetector = ShakeDetector(this).apply {
      start(sensorManager)
    }
  }

  /** Stops the shake detector. */
  fun stop() {
    log("stop()")
    shakeDetector?.stop()
    shakeDetector = null
  }
}
