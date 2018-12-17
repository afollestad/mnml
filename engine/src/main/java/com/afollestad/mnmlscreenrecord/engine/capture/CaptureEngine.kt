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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.MediaRecorder.OutputFormat.MPEG_4
import android.media.MediaRecorder.VideoEncoder.H264
import android.media.MediaRecorder.VideoSource.SURFACE
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.view.WindowManager
import androidx.annotation.CheckResult
import com.afollestad.mnmlscreenrecord.common.misc.timestampString
import com.afollestad.mnmlscreenrecord.engine.permission.CapturePermissionActivity
import com.afollestad.rxkprefs.Pref
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.FileNotFoundException
import java.util.Date
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
  private val windowManager: WindowManager,
  private val projectionManager: MediaProjectionManager,
  private val recordingsFolderPref: Pref<String>,
  private val bitRatePref: Pref<Int>,
  private val frameRatePref: Pref<Int>
) : CaptureEngine {

  private var recordingInfo: RecordingInfo? = null
  private val handler = Handler()

  private var recorder: MediaRecorder? = null
  private var projection: MediaProjection? = null
  private var display: VirtualDisplay? = null
  private var pendingFile: File? = null
  private val onStart = PublishSubject.create<Unit>()
  private val onStop = PublishSubject.create<File>()
  private val onCancel = PublishSubject.create<Unit>()
  private val onError = PublishSubject.create<Exception>()
  private var isStarted: Boolean = false

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
      context.startActivity(
          Intent(context, CapturePermissionActivity::class.java)
              .addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_MULTIPLE_TASK)
      )
      return
    }

    if (createAndPrepareRecorder(context)) {
      createVirtualDisplayAndStart(context)
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
    if (recordingInfo == null) {
      throw IllegalStateException("onActivityResult - recordingInfo is unexpectedly null!")
    }

    projection = projectionManager.getMediaProjection(resultCode, data)
        .apply {
          registerCallback(projectionCallback, null)
        }

    if (createAndPrepareRecorder(context)) {
      createVirtualDisplayAndStart(context)
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

  @CheckResult private fun createAndPrepareRecorder(context: Context): Boolean {
    val recordingInfo = ensureRecordingInfo(context)
    recorder = MediaRecorder().apply {
      setVideoSource(SURFACE)
      setOutputFormat(MPEG_4)

      val frameRate = frameRatePref.get()
      setVideoFrameRate(frameRate)
      log("Frame rate set to $frameRate")

      setVideoEncoder(H264)
      setVideoSize(recordingInfo.width, recordingInfo.height)

      val bitRate = bitRatePref.get()
      setVideoEncodingBitRate(bitRate)
      log("Bit rate set to $bitRate")

      val outputFolder = File(recordingsFolderPref.get()).apply { mkdirs() }
      val now = Date().timestampString()
      val outputFile = File(outputFolder, "MNML-$now.mp4")
      pendingFile = outputFile
      setOutputFile(outputFile.absolutePath)
      log("Recording to $outputFile")

      try {
        prepare()
        log("Media recorder prepared")
      } catch (fe: FileNotFoundException) {
        onError.onNext(Exception("MNML was unable to access your file system. ${fe.message}", fe))
        return false
      } catch (t: Throwable) {
        onError.onNext(Exception("MNML was unable to prepare for recording. $t", t))
        return false
      }
    }

    return true
  }

  @SuppressLint("CheckResult")
  private fun createVirtualDisplayAndStart(context: Context) {
    display = createVirtualDisplay(context)

    // Tiny delay so we don't record the cast "start now" dialog.
    handler.postDelayed({
      recorder?.start() ?: throw IllegalStateException(
          "createVirtualDisplayAndStart - Recorder is unexpectedly null!"
      )
    }, 150)

    isStarted = true
    onStart.onNext(Unit)
    log("Media recorder started")
  }

  private fun createVirtualDisplay(context: Context): VirtualDisplay {
    val recordingInfo = ensureRecordingInfo(context)
    val surface = recorder?.surface ?: throw IllegalStateException(
        "createVirtualDisplay - Recorder unexpectedly null!"
    )
    return projection?.createVirtualDisplay(
        "MNMLCaptureEngine",
        recordingInfo.width,
        recordingInfo.height,
        recordingInfo.density,
        VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        surface,
        null,
        null
    ) ?: throw IllegalStateException("createVirtualDisplay - projection unexpectedly null!")
  }

  private fun ensureRecordingInfo(context: Context): RecordingInfo {
    if (recordingInfo == null) {
      recordingInfo = RecordingInfo.get(context, windowManager)
    }
    return recordingInfo!!
  }

  private val projectionCallback = object : MediaProjection.Callback() {
    override fun onStop() {
      log("Got onStop() in projection callback")
      stop()
    }
  }
}
