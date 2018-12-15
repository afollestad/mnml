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
package com.afollestad.mnmlscreenrecord.engine.service

import android.app.Application
import android.content.Intent
import com.afollestad.mnmlscreenrecord.notifications.EXTRA_STOP_FOREGROUND
import com.afollestad.mnmlscreenrecord.notifications.RECORD_ACTION
import com.afollestad.mnmlscreenrecord.notifications.STOP_ACTION

/** @author Aidan Follestad (afollestad) */
interface ServiceController {

  /**
   * Starts the background service and brings it into the foreground (show
   * a persistent notification which keeps it open).
   */
  fun startService()

  /**
   * Tells the service to start screen capture. If the service isn't already
   * running, it is started as well.
   */
  fun startRecording()

  /**
   * Tells the service to stop screen capture. If [stopService] is true,
   * the service is also stopped and brought out of the foreground.
   */
  fun stopRecording(stopService: Boolean = false)

  /**
   * Tells the service to exit, bringing it out of the foreground as well. Any active
   * capture will stop.
   */
  fun stopService()
}

/** @author Aidan Follestad (afollestad) */
class RealServiceController(
  private val app: Application
) : ServiceController {

  override fun startService() {
    app.startService(Intent(app, BackgroundService::class.java))
  }

  override fun startRecording() {
    app.startService(Intent(app, BackgroundService::class.java).apply {
      action = RECORD_ACTION
    })
  }

  override fun stopRecording(stopService: Boolean) {
    app.sendBroadcast(Intent(STOP_ACTION).apply {
      putExtra(EXTRA_STOP_FOREGROUND, stopService)
    })
  }

  override fun stopService() = stopRecording(true)
}
