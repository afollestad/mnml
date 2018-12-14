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
import android.os.Environment.getExternalStorageDirectory
import android.os.Handler
import android.view.WindowManager
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.afollestad.mnmlscreenrecord.common.misc.timestampString
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.Date
import timber.log.Timber.d as log

const val CAPTURE_FOLDER_NAME = "MNML Screen Recorder"

/** @author Aidan Follestad (@afollestad) */
class CaptureEngine(
  private val windowManager: WindowManager,
  private val projectionManager: MediaProjectionManager
) : LifecycleObserver {

  private lateinit var recordingInfo: RecordingInfo
  private val handler = Handler()

  private var recorder: MediaRecorder? = null
  private var projection: MediaProjection? = null
  private var display: VirtualDisplay? = null
  private var pendingFile: File? = null
  private var onStart = PublishSubject.create<Unit>()
  private var onStop = PublishSubject.create<File>()
  private var isStarted: Boolean = false

  fun onStart(): Observable<Unit> = onStart

  fun onStop(): Observable<File> = onStop

  fun isStarted() = isStarted

  fun start(context: Context) {
    log("start($context)")
    recordingInfo = RecordingInfo.get(context, windowManager)
    createAndPrepareRecorder(recordingInfo)

    if (projection == null) {
      log("Projection is null, requesting permission...")
      context.startActivity(
          Intent(context, CapturePermissionActivity::class.java)
              .addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_MULTIPLE_TASK)
      )
      return
    }

    createVirtualDisplayAndStart(recordingInfo)
  }

  fun requestPermission(
    context: Activity,
    requestCode: Int
  ) = context.startActivityForResult(projectionManager.createScreenCaptureIntent(), requestCode)

  fun onActivityResult(
    resultCode: Int,
    data: Intent
  ) {
    log("onActivityResult($resultCode, $data)")
    projection = projectionManager.getMediaProjection(resultCode, data)
        .apply {
          registerCallback(projectionCallback, null)
        }
    createVirtualDisplayAndStart(recordingInfo)
  }

  fun cancel() {
    if (pendingFile == null) {
      return
    }
    log("cancel()")
    pendingFile?.delete()
    pendingFile = null
    stop()
  }

  fun deleteLastRecording() {
    pendingFile?.delete()
    pendingFile = null
  }

  @OnLifecycleEvent(ON_DESTROY)
  fun stop() {
    if (recorder == null) {
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

    pendingFile?.let {
      log("Recorded to $it")
      onStop.onNext(it)
    }
  }

  private fun createAndPrepareRecorder(recordingInfo: RecordingInfo) {
    recorder = MediaRecorder().apply {
      setVideoSource(SURFACE)
      setOutputFormat(MPEG_4)
      setVideoFrameRate(recordingInfo.frameRate)
      setVideoEncoder(H264)
      setVideoSize(recordingInfo.width, recordingInfo.height)
      setVideoEncodingBitRate(8 * 1000 * 1000)

      val outputFolder = File(getExternalStorageDirectory(), CAPTURE_FOLDER_NAME).apply {
        mkdirs()
      }
      val now = Date().timestampString()
      val outputFile = File(outputFolder, "MNML-$now.mp4")
      pendingFile = outputFile
      setOutputFile(outputFile)

      try {
        prepare()
        log("Media recorder prepared")
      } catch (e: Throwable) {
        throw RuntimeException("Unable to prepare the MediaRecorder", e)
      }
    }
  }

  @SuppressLint("CheckResult")
  private fun createVirtualDisplayAndStart(recordingInfo: RecordingInfo) {
    display = createVirtualDisplay(recordingInfo)
    // Tiny delay so we don't record the cast "start now" dialog.
    handler.postDelayed({
      recorder!!.start()
    }, 150)
    onStart.onNext(Unit)
    isStarted = true
    log("Media recorder started")
  }

  private fun createVirtualDisplay(recordingInfo: RecordingInfo): VirtualDisplay {
    return projection!!.createVirtualDisplay(
        "MNMLCaptureEngine",
        recordingInfo.width,
        recordingInfo.height,
        recordingInfo.density,
        VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        recorder!!.surface,
        null,
        null
    )
  }

  private val projectionCallback = object : MediaProjection.Callback() {
    override fun onStop() {
      log("Got onStop() in projection callback")
      this@CaptureEngine.stop()
    }
  }
}
