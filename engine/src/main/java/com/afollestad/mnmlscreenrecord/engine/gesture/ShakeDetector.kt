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
package com.afollestad.mnmlscreenrecord.engine.gesture

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager.GRAVITY_EARTH

typealias ShakeCallback = (count: Int) -> Unit

/**
 * Cleaned up version of class from [http://demonuts.com/android-shake-detection].
 */
internal class ShakeDetector(
  private val callback: ShakeCallback
) : SensorEventListener {

  companion object {
    private const val SHAKE_THRESHOLD_GRAVITY = 2.25f
    private const val SHAKE_SLOP_TIME_MS = 500
    private const val SHAKE_COUNT_RESET_TIME_MS = 2000
  }

  private var timestamp: Long = 0
  private var count: Int = 0

  override fun onSensorChanged(event: SensorEvent) {
    val x = event.values[0]
    val y = event.values[1]
    val z = event.values[2]

    val gX = x / GRAVITY_EARTH
    val gY = y / GRAVITY_EARTH
    val gZ = z / GRAVITY_EARTH

    // gForce will be close to 1 when there is no movement.
    val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble())
        .toFloat()

    if (gForce > SHAKE_THRESHOLD_GRAVITY) {
      val now = System.currentTimeMillis()
      // ignore shake events too close to each other (500ms)
      if (timestamp + SHAKE_SLOP_TIME_MS > now) {
        return
      }

      // reset the shake count after 3 seconds of no shakes
      if (timestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
        count = 0
      }

      timestamp = now
      count++

      callback(count)
    }
  }

  override fun onAccuracyChanged(
    sensor: Sensor,
    accuracy: Int
  ) = Unit
}
