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
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioEncoder.AAC
import android.media.MediaRecorder.AudioSource.MIC
import android.media.MediaRecorder.OutputFormat.MPEG_4
import android.media.MediaRecorder.VideoEncoder.H264
import android.media.MediaRecorder.VideoSource.SURFACE
import androidx.annotation.CheckResult
import com.afollestad.mnmlscreenrecord.common.misc.timestampString
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.Date

@CheckResult
internal fun RealCaptureEngine.createAndPrepareRecorder(context: Context): Boolean {
  val recordingInfo = getRecordingInfo(context)
  recorder = MediaRecorder().apply {
    setVideoSource(SURFACE)
    if (recordAudioPref.get()) {
      try {
        log("Enabling the microphone")
        setAudioSource(MIC)
        log("Recording audio from the mic")
      } catch (t: Throwable) {
        recorder = null
        onError.onNext(Exception("Unable to set the audio source to your microphone!", t))
        return false
      }
    }
    setOutputFormat(MPEG_4)

    val frameRate = frameRatePref.get()
    setVideoFrameRate(frameRate)
    log("Frame rate set to $frameRate")

    setVideoEncoder(H264)
    if (recordAudioPref.get()) {
      setAudioEncoder(AAC)
    }

    setVideoSize(recordingInfo.width, recordingInfo.height)
    log("Video resolution set to ${recordingInfo.width} x ${recordingInfo.height}")

    val videoBitRate = videoBitRatePref.get()
    setVideoEncodingBitRate(videoBitRate)
    log("Video bit rate set to $videoBitRate")

    val audioBitRate = audioBitRatePref.get()
    setAudioEncodingBitRate(audioBitRate)
    log("Audio bit rate set to $audioBitRate")

    val outputFolder = File(recordingsFolderPref.get()).apply { mkdirs() }
    val now = Date().timestampString()
    val outputFile = File(outputFolder, "MNML-$now.mp4")
    pendingFile = outputFile
    setOutputFile(outputFile.absolutePath)
    log("Recording to $outputFile")

    try {
      log("Preparing the media recorder...")
      prepare()
      log("Media recorder prepared.")
    } catch (fe: FileNotFoundException) {
      log("Failed to prepare the media recorder. ${fe.message}")
      recorder = null
      onError.onNext(FileSystemException(fe))
      return false
    } catch (t: Throwable) {
      log("Failed to prepare the media recorder. ${t.message}")
      recorder = null
      onError.onNext(PrepareFailedException(t))
      return false
    }
  }

  return true
}

private fun log(message: String) {
  Timber.tag("CaptureEngineRecorder")
  Timber.d(message)
}
