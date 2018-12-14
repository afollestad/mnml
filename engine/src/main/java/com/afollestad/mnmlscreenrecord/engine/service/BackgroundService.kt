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

import android.app.Service
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.os.IBinder
import androidx.lifecycle.LifecycleOwner
import com.afollestad.mnmlscreenrecord.common.files.FileScanner
import com.afollestad.mnmlscreenrecord.common.intent.IntentReceiver
import com.afollestad.mnmlscreenrecord.common.lifecycle.SimpleLifecycle
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.engine.capture.CaptureEngine
import com.afollestad.mnmlscreenrecord.engine.overlay.OverlayManager
import com.afollestad.mnmlscreenrecord.notifications.DELETE_ACTION
import com.afollestad.mnmlscreenrecord.notifications.EXIT_ACTION
import com.afollestad.mnmlscreenrecord.notifications.EXTRA_STOP_FOREGROUND
import com.afollestad.mnmlscreenrecord.notifications.Notifications
import com.afollestad.mnmlscreenrecord.notifications.RECORD_ACTION
import com.afollestad.mnmlscreenrecord.notifications.STOP_ACTION
import org.koin.android.ext.android.inject
import timber.log.Timber.d as log

/**
 * The background service which foregrounds itself with a persistent notification to do screen
 * capture, even if the app isn't visible.
 *
 * @author Aidan Follestad (@afollestad)
 */
class BackgroundService : Service(), LifecycleOwner {

  companion object {
    private const val ID = 77

    const val PERMISSION_DENIED =
      "com.afollestad.mnmlscreenrecord.service.PERMISSION_DENIED"
    const val MAIN_ACTIVITY_CLASS = "main_activity_class"
  }

  private val lifecycle = SimpleLifecycle(this)
  private val overlayManager by inject<OverlayManager>()
  private val notifications by inject<Notifications>()
  private val captureEngine by inject<CaptureEngine>()
  private val uriScanner by inject<FileScanner>()
  private val mainActivityClass by inject<Class<*>>(name = MAIN_ACTIVITY_CLASS)

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int
  ): Int {
    log("onStartCommand(${intent?.action})")
    if (intent?.action == RECORD_ACTION) {
      overlayManager.countdown {
        captureEngine.start(this@BackgroundService)
        updateForeground(true)
      }
    }
    return START_STICKY
  }

  override fun onCreate() {
    super.onCreate()
    log("onCreate()")

    // Intent broadcasts
    IntentReceiver(this) {
      onAction(PERMISSION_DENIED) {
        captureEngine.cancel()
        updateForeground(false)
      }
      onAction(ACTION_SCREEN_OFF) {
        captureEngine.stop()
      }
      onAction(STOP_ACTION) {
        captureEngine.stop()
        if (notifications.isAppOpen() ||
            it.getBooleanExtra(EXTRA_STOP_FOREGROUND, false)) {
          stopForeground(true)
          stopSelf()
        }
      }
      onAction(DELETE_ACTION) {
        captureEngine.deleteLastRecording()
        notifications.cancelPostRecordNotification()
      }
      onAction(EXIT_ACTION) {
        captureEngine.cancel()
        stopForeground(true)
        stopSelf()
      }
    }

    // Lifecycle observers
    lifecycle.run {
      addObserver(captureEngine)
    }

    // Foreground notification
    updateForeground(false)
    lifecycle.onCreate()

    captureEngine.onStop()
        .subscribe { file ->
          updateForeground(false)
          uriScanner.scan(file) { resultUri ->
            notifications.showPostRecordNotification(resultUri)
          }
        }
        .attachLifecycle(this)
  }

  override fun onDestroy() {
    log("onDestroy()")
    lifecycle.onDestroy()
    super.onDestroy()
  }

  override fun getLifecycle() = lifecycle

  private fun updateForeground(recording: Boolean) {
    val action = if (recording) {
      STOP_ACTION
    } else {
      EXIT_ACTION
    }
    startForeground(
        ID,
        notifications.createWidgetServiceNotification(
            mainActivity = mainActivityClass,
            backgroundService = this::class.java,
            action = action,
            isRecording = recording
        )
    )
  }
}
