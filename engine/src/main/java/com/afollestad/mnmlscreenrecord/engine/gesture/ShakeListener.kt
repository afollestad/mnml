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

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_UI
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad) */
internal class ShakeListener(
  private val sensorManager: SensorManager,
  callback: ShakeCallback
) {

  private val shakeDetector = ShakeDetector(callback)
  private var accelerometer: Sensor? = null

  /** Initializes the shake detector. */
  fun start() {
    log("start()")
    accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
    sensorManager.registerListener(shakeDetector, accelerometer, SENSOR_DELAY_UI)
  }

  /** Stops the shake detector. */
  fun stop() {
    if (accelerometer == null) {
      return
    }
    log("stop()")
    sensorManager.unregisterListener(shakeDetector)
    accelerometer = null
  }
}
