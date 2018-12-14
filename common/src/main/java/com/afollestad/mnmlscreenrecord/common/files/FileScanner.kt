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
package com.afollestad.mnmlscreenrecord.common.files

import android.app.Application
import android.media.MediaScannerConnection.scanFile
import android.net.Uri
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File
import timber.log.Timber.d as log

typealias UriCallback = ((Uri) -> Unit)?

/** @author Aidan Follestad (@afollestad) */
class FileScanner(private val app: Application) {

  private var onScanSubject = PublishSubject.create<Uri>()

  fun onScan(): Observable<Uri> = onScanSubject

  fun scan(
    file: File,
    cb: UriCallback = null
  ) {
    log("Scanning $file...")
    scanFile(app, arrayOf(file.toString()), null) { _, resultUri ->
      log("Scanned! Result: $resultUri")
      cb?.invoke(resultUri)
      onScanSubject.onNext(resultUri)
    }
  }
}
