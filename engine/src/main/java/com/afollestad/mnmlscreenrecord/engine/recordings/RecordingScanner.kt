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
package com.afollestad.mnmlscreenrecord.engine.recordings

import android.app.Application
import android.media.MediaScannerConnection.scanFile
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File
import timber.log.Timber.d as log

typealias RecordingCallback = ((Recording) -> Unit)?

/** @author Aidan Follestad (@afollestad) */
interface RecordingScanner {

  /** A global callback observable that emits whenever a file is scanned. */
  fun onScan(): Observable<Recording>

  /**
   * Tells the system to scan a file and add it to the media content provider, optionally
   * invoking a callback when it's finished.
   */
  fun scan(
    file: File,
    cb: RecordingCallback = null
  )
}

/** @author Aidan Follestad (@afollestad) */
class RealRecordingScanner(
  private val app: Application,
  private val recordingManager: RecordingManager
) : RecordingScanner {

  private var onScanSubject = PublishSubject.create<Recording>()

  override fun onScan(): Observable<Recording> = onScanSubject

  override fun scan(
    file: File,
    cb: RecordingCallback
  ) {
    log("Scanning $file...")
    scanFile(app, arrayOf(file.toString()), null) { _, resultUri ->
      val recording = recordingManager.getRecording(resultUri)
      if (recording == null) {
        log("Scanned uri: $resultUri, but unable to get the associated Recording!")
        return@scanFile
      }

      log("Scanned uri: $resultUri, recording = $recording")
      cb?.invoke(recording)
      onScanSubject.onNext(recording)
    }
  }
}
