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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.view.WindowManager
import com.afollestad.mnmlscreenrecord.common.misc.startActivity
import com.afollestad.mnmlscreenrecord.engine.permission.CapturePermissionActivity
import com.afollestad.rxkprefs.Pref
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File
import timber.log.Timber.d as log

/**
 * Handles core screen capture logic.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface CaptureEngine {

  /**
   * Returns an Observable that emits when capture starts.
   */
  fun onStart(): Observable<Unit>

  /**
   * Returns an Observable that emits when capture stops. The emission is the file
   * containing the finished screen recording.
   */
  fun onStop(): Observable<File>

  /**
   * Returns an Observable that emits when capture is cancelled, e.g. if permissions
   * are denied.
   */
  fun onCancel(): Observable<Unit>

  /**
   * Returns an Observable that emits when an error occurs that should be recoverable, and
   * displayed to the user.
   */
  fun onError(): Observable<Exception>

  /**
   * Returns true if capture is currently in0-progress.
   */
  fun isStarted(): Boolean

  /**
   * Starts screen capture.
   */
  fun start(context: Context)

  /**
   * Requests permission to capture the screen. Shows the system cast dialog,
   * prompting to "start now" - unless the user checks the box to not show again.
   */
  fun requestPermission(
    context: Activity,
    requestCode: Int
  )

  /**
   * A delegate from the activity - notifies when capture permission is received.
   */
  fun onActivityResult(
    context: Context,
    resultCode: Int,
    data: Intent
  )

  /**
   * Cancels screen capture - deleting any previously created file and signaling cancellation.
   */
  fun cancel()

  /**
   * Stops screen capture - commits the capture file and emits into the stop signal.
   */
  fun stop()
}

/** @author Aidan Follestad (@afollestad) */
class RealCaptureEngine(
  internal val windowManager: WindowManager,
  private val projectionManager: MediaProjectionManager,
  internal val recordingsFolderPref: Pref<String>,
  internal val videoBitRatePref: Pref<Int>,
  internal val frameRatePref: Pref<Int>,
  internal val recordAudioPref: Pref<Boolean>,
  internal val audioBitRatePref: Pref<Int>,
  internal val resolutionWidthPref: Pref<Int>,
  internal val resolutionHeightPref: Pref<Int>
) : CaptureEngine {

  internal val handler = Handler()

  internal val onStart = PublishSubject.create<Unit>()
  internal val onCancel = PublishSubject.create<Unit>()
  internal val onError = PublishSubject.create<Exception>()
  private val onStop = PublishSubject.create<File>()

  internal var display: VirtualDisplay? = null
  internal var projection: MediaProjection? = null
  internal var isStarted: Boolean = false
  internal var recorder: MediaRecorder? = null
  internal var pendingFile: File? = null

  override fun onStart(): Observable<Unit> = onStart

  override fun onStop(): Observable<File> = onStop

  override fun onCancel(): Observable<Unit> = onCancel

  override fun onError(): Observable<Exception> = onError

  override fun isStarted(): Boolean = isStarted

  override fun start(context: Context) {
    if (isStarted) {
      log("start($context) - already started! No-op")
      return
    }
    log("start($context)")

    if (projection == null) {
      log("Projection is null, requesting permission...")
      context.startActivity<CapturePermissionActivity>(
          flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_MULTIPLE_TASK
      )
      return
    }

    try {
      if (createAndPrepareRecorder(context)) {
        createVirtualDisplayAndStart(context)
      }
    } catch (e: Exception) {
      onError.onNext(e)
    }
  }

  override fun requestPermission(
    context: Activity,
    requestCode: Int
  ) = context.startActivityForResult(projectionManager.createScreenCaptureIntent(), requestCode)

  override fun onActivityResult(
    context: Context,
    resultCode: Int,
    data: Intent
  ) {
    log("onActivityResult($resultCode, $data)")
    projection = projectionManager.getMediaProjection(resultCode, data)
        .apply {
          registerCallback(projectionCallback, null)
        }

    try {
      if (createAndPrepareRecorder(context)) {
        createVirtualDisplayAndStart(context)
      }
    } catch (e: Exception) {
      onError.onNext(e)
    }
  }

  override fun cancel() {
    if (pendingFile == null) {
      onCancel.onNext(Unit)
      return
    }
    log("cancel()")
    pendingFile?.delete()
    pendingFile = null
    stop()
  }

  override fun stop() {
    if (recorder == null) {
      onCancel.onNext(Unit)
      return
    }
    isStarted = false
    log("stop()")

    try {
      projection?.stop()
      recorder?.stop()
    } catch (e: Exception) {
      log("Got an exception when releasing the media recorder... ${e.message}")
      e.printStackTrace()
    }

    recorder?.release()
    recorder = null
    display?.release()
    display = null
    projection = null

    val fileToSend = pendingFile
    if (fileToSend != null) {
      log("Recorded to $fileToSend")
      onStop.onNext(fileToSend)
      pendingFile = null
    } else {
      onCancel.onNext(Unit)
    }
  }

  private val projectionCallback = object : MediaProjection.Callback() {
    override fun onStop() {
      log("Got onStop() in projection callback")
      stop()
    }
  }
}
